package bgu.spl.mics;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;

/**
 * The {@link MessageBrokerImpl class is the implementation of the MessageBroker interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBrokerImpl implements MessageBroker {
    private ConcurrentHashMap<Subscriber, BlockingQueue<Message>> subscriberMap;
    private ConcurrentHashMap<Class<? extends Message>, Pair<Semaphore, ConcurrentLinkedQueue<Subscriber>>> topicMap;
    private ConcurrentHashMap<Event, Future> eventMap;
    private Semaphore topicMapLock;

    private MessageBrokerImpl() {
        subscriberMap = new ConcurrentHashMap<>();
        topicMap = new ConcurrentHashMap<>();
        eventMap = new ConcurrentHashMap<>();
        topicMapLock = new Semaphore(1, true);
    }

    /**
     * Retrieves the single instance of this class.
     */
    public static MessageBroker getInstance() {
        return Instance.instance;
    }

    @Override
    public <T> void subscribeEvent(Class<? extends Event<T>> type, Subscriber m) throws InterruptedException {
        subscribeTopic(type, m);
    }

    @Override
    public void subscribeBroadcast(Class<? extends Broadcast> type, Subscriber m) throws InterruptedException {
        subscribeTopic(type, m);
    }

    private void subscribeTopic(Class<? extends Message> type, Subscriber m) throws InterruptedException {
        topicMapLock.acquire();
        try {
            if (!topicMap.containsKey(type)) {
                Pair<Semaphore, ConcurrentLinkedQueue<Subscriber>> newTopicPair = new Pair<>(new Semaphore(1, true), new ConcurrentLinkedQueue<>());
                topicMap.put(type, newTopicPair);
            }
            getTopicQueueLock(type).acquire();
        } finally {
            topicMapLock.release();
        }

        try {
            getTopicQueue(type).add(m);
        } finally {
            getTopicQueueLock(type).release();
        }
    }

    private ConcurrentLinkedQueue<Subscriber> getTopicQueue(Class<? extends Message> topic) {
        Pair<Semaphore, ConcurrentLinkedQueue<Subscriber>> topicPair = topicMap.get(topic);
        return topicPair.getSecond();
    }

    private Semaphore getTopicQueueLock(Class<? extends Message> topic) {
        Pair<Semaphore, ConcurrentLinkedQueue<Subscriber>> topicPair = topicMap.get(topic);
        return topicPair.getFirst();
    }

    @Override
    public <T> void complete(Event<T> e, T result) {
        Future<T> toResolve = eventMap.get(e);
        toResolve.resolve(result);
        eventMap.remove(e);
    }

    @Override
    public void sendBroadcast(Broadcast b) throws InterruptedException {
        topicMapLock.acquire();
        try {
            if (!topicMap.containsKey(b.getClass())) {
                return;
            }
        } finally {
            topicMapLock.release();
        }

        getTopicQueueLock(b.getClass()).acquire();
        try {
            for (Subscriber subscriber : getTopicQueue(b.getClass())) {
                addToSubQueue(b, subscriber);
            }
        } finally {
            getTopicQueueLock(b.getClass()).release();
        }
    }

    @Override
    public <T> Future<T> sendEvent(Event<T> e) throws InterruptedException {
        topicMapLock.acquire();
        try {
            if (!topicMap.containsKey(e.getClass())) return null;
        } finally {
            topicMapLock.release();
        }

        getTopicQueueLock(e.getClass()).acquire();
        try {
            if (getTopicQueue(e.getClass()).isEmpty()) {
                return null;
            }

            Future<T> future = new Future<>();
            eventMap.put(e, future);

            assignEventAndRequeueSubscriber(e);

            return future;
        } finally {
            getTopicQueueLock(e.getClass()).release();
        }
    }

    private <T> void assignEventAndRequeueSubscriber(Event<T> e) throws InterruptedException {
        Subscriber firstSub = getTopicQueue(e.getClass()).poll();
        getTopicQueue(e.getClass()).add(firstSub);

        addToSubQueue(e, firstSub);
    }

    private void addToSubQueue(Message b, Subscriber subscriber) throws InterruptedException {
        BlockingQueue<Message> subQueue = getSubQueue(subscriber);
        subQueue.put(b);
    }

    @Override
    public void register(Subscriber m) {
        BlockingQueue<Message> subQueue = new LinkedBlockingQueue<>();
        subscriberMap.putIfAbsent(m, subQueue);
    }

    @Override
    public void unregister(Subscriber m) throws InterruptedException {
        topicMapLock.acquire();
        try {
            if (subscriberMap.containsKey(m)) {
                removeFromTopicMap(m);
            }
        } finally {
            topicMapLock.release();
        }
        completeAllEventsOfSubscriberAsNull(m);
        subscriberMap.remove(m);
    }

    private void completeAllEventsOfSubscriberAsNull(Subscriber sub) {
        BlockingQueue<Message> subQueue = getSubQueue(sub);
        for (Message message : subQueue) {
            if (message instanceof Event) {
                complete((Event) message, null);
            }
        }
    }

    private void removeFromTopicMap(Subscriber m) throws InterruptedException {
        Pair<Semaphore, ConcurrentLinkedQueue<Subscriber>> topicPair;
        Semaphore topicSemaphore;
        for (Map.Entry<Class<? extends Message>, Pair<Semaphore, ConcurrentLinkedQueue<Subscriber>>> entry : topicMap.entrySet()) {
            topicPair = entry.getValue();
            topicSemaphore = topicPair.getFirst();

            topicSemaphore.acquire();
            try {
                topicPair.getSecond().remove(m);
            } finally {
                topicSemaphore.release();
            }
        }
    }

    @Override
    public Message awaitMessage(Subscriber m) throws InterruptedException {
        if (!subscriberMap.containsKey(m)) {
            throw new IllegalStateException("No such Subscriber registered: " + m.getName());
        }

        return getSubQueue(m).take();
    }

    private BlockingQueue<Message> getSubQueue(Subscriber m) {
        return subscriberMap.get(m);
    }

    private static class Instance {
        private static MessageBroker instance = new MessageBrokerImpl();
    }

    //TODO: delete this
    public void clear(){
        subscriberMap.clear();
        topicMap.clear();
        eventMap.clear();
    }
}
