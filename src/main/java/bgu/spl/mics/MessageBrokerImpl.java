package bgu.spl.mics;

import javafx.util.Pair;

import java.util.Map;
import java.util.concurrent.*;

/**
 * The {@link MessageBrokerImpl class is the implementation of the MessageBroker interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBrokerImpl implements MessageBroker {
    private ConcurrentHashMap<Subscriber, Pair<Semaphore, BlockingQueue<Message>>> subscriberMap;
    private ConcurrentHashMap<Class<? extends Message>, Pair<Semaphore, ConcurrentLinkedQueue<Subscriber>>> topicMap;
    private ConcurrentHashMap<Event, Future> eventMap;
    private Semaphore topicMapLock;

    private MessageBrokerImpl() {
        subscriberMap = new ConcurrentHashMap<>();
        topicMap = new ConcurrentHashMap<>();
        eventMap = new ConcurrentHashMap<>();
        topicMapLock = new Semaphore(1);
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
            if (!topicMap.contains(type)) {
                Pair<Semaphore, ConcurrentLinkedQueue<Subscriber>> newTopicPair = new Pair<>(new Semaphore(1), new ConcurrentLinkedQueue<>());
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
        return topicPair.getValue();
    }

    private Semaphore getTopicQueueLock(Class<? extends Message> topic) {
        Pair<Semaphore, ConcurrentLinkedQueue<Subscriber>> topicPair = topicMap.get(topic);
        return topicPair.getKey();
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
            if (!topicMap.contains(b.getClass())) {
                System.out.println("No subscriber is registered to this topic.");
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
            if (!topicMap.contains(e.getClass())) return null;
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
        Subscriber first = getTopicQueue(e.getClass()).poll();
        getTopicQueue(e.getClass()).add(first);

        addToSubQueue(e, first);
    }

    private void addToSubQueue(Message b, Subscriber subscriber) throws InterruptedException {
        Pair<Semaphore, BlockingQueue<Message>> subPair = subscriberMap.get(subscriber);
        Semaphore subSemaphore = subPair.getKey();

        subSemaphore.acquire();
        try {
            subPair.getValue().put(b);
        } finally {
            subSemaphore.release();
        }
    }

    @Override
    public void register(Subscriber m) {
        Pair<Semaphore, BlockingQueue<Message>> subPair = new Pair<>(new Semaphore(1), new LinkedBlockingQueue<>());
        subscriberMap.putIfAbsent(m, subPair);
    }

    @Override
    public void unregister(Subscriber m) throws InterruptedException {
        topicMapLock.acquire();
        try {
            if (subscriberMap.contains(m)){
                removeFromTopicMap(m);
                subscriberMap.remove(m);
            }
        } finally {
            topicMapLock.release();
        }
    }

    private void removeFromTopicMap(Subscriber m) throws InterruptedException {
        for (Map.Entry<Class<? extends Message>, Pair<Semaphore, ConcurrentLinkedQueue<Subscriber>>> entry : topicMap.entrySet()) {
            Pair<Semaphore, ConcurrentLinkedQueue<Subscriber>> topicPair = entry.getValue();
            topicPair.getValue().remove(m);
        }
    }

    @Override
    public Message awaitMessage(Subscriber m) throws InterruptedException {
        if (!subscriberMap.contains(m)) {
            throw new IllegalStateException("No such Subscriber registered: " + m.getName());
        }

        Pair<Semaphore, BlockingQueue<Message>> subPair = subscriberMap.get(m);
        Semaphore subSemaphore = subPair.getKey();

        subSemaphore.acquire();
        try {
            return getSubQueue(m).take();
        } finally {
            subSemaphore.release();
        }
    }

    private BlockingQueue<Message> getSubQueue(Subscriber m) {
        Pair<Semaphore, BlockingQueue<Message>> subPair = subscriberMap.get(m);
        return subPair.getValue();
    }

    private static class Instance {
        private static MessageBroker instance = new MessageBrokerImpl();
    }
}
