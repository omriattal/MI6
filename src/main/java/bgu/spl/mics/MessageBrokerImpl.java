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
    private Semaphore eventMapLock;

    private MessageBrokerImpl() {
        subscriberMap = new ConcurrentHashMap<>();
        topicMap = new ConcurrentHashMap<>();
        eventMap = new ConcurrentHashMap<>();
    }

    /**
     * Retrieves the single instance of this class.
     */
    public static MessageBroker getInstance() {
        return Instance.instance;
    }

    @Override
    public <T> void subscribeEvent(Class<? extends Event<T>> type, Subscriber m) {
        try {
            subscribeTopic(type, m);
        } catch (InterruptedException ignored) {
        }
    }

    @Override
    public void subscribeBroadcast(Class<? extends Broadcast> type, Subscriber m) {
        try {
            subscribeTopic(type, m);
        } catch (InterruptedException ignored) {
        }
    }

    private void subscribeTopic(Class<? extends Message> type, Subscriber m) throws InterruptedException {
        if (!topicMap.contains(type)) {
            Pair<Semaphore, ConcurrentLinkedQueue<Subscriber>> newTopicPair = new Pair<>(new Semaphore(1), new ConcurrentLinkedQueue<>());
            topicMap.put(type, newTopicPair);
        }

        Pair<Semaphore, ConcurrentLinkedQueue<Subscriber>> topicPair = topicMap.get(type);
        topicPair.getKey().acquire();
        try {
            topicPair.getValue().add(m);
        } finally {
            topicPair.getKey().release();
        }
    }

    @Override
    public <T> void complete(Event<T> e, T result) {
        Future<T> toResolve = eventMap.get(e);
        toResolve.resolve(result);
    }

    @Override
    public void sendBroadcast(Broadcast b) throws InterruptedException {
        if (!topicMap.contains(b.getClass())) {
            System.out.println("No subscriber is registered to this topic.");
            return;
        }

        Pair<Semaphore, ConcurrentLinkedQueue<Subscriber>> topicPair = topicMap.get(b.getClass());
        Semaphore topicSemaphore = topicPair.getKey();
        topicSemaphore.acquire();
        try {
            for (Subscriber subscriber : topicPair.getValue()) {
                addToSubQueue(b, subscriber);
            }
        } finally {
            topicSemaphore.release();
        }
    }

    @Override
    public <T> Future<T> sendEvent(Event<T> e) throws InterruptedException {
        if(!eventMap.contains(e)){
            return null;
        }
        assignEventAndRequeueSubscriber(e);
        Future<T> future = new Future<>();
        eventMap.put(e, future);
        return future;
    }

    private <T> void assignEventAndRequeueSubscriber(Event<T> e) throws InterruptedException {
        Pair<Semaphore, ConcurrentLinkedQueue<Subscriber>> topicPair = topicMap.get(e.getClass());
        Semaphore topicSemaphore = topicPair.getKey();

        topicSemaphore.acquire();
        try {
            Subscriber first = topicPair.getValue().poll();
            topicPair.getValue().add(first);

            addToSubQueue(e, first);
        } finally {
            topicSemaphore.release();
        }
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
        Pair<Semaphore, BlockingQueue<Message>> subPair = new Pair<>(new Semaphore(1) , new LinkedBlockingQueue<>());
        subscriberMap.put(m, subPair);
    }

    @Override
    public void unregister(Subscriber m) throws InterruptedException {
        if (subscriberMap.remove(m) != null) removeFromTopicMap(m);
    }

    private void removeFromTopicMap(Subscriber m) throws InterruptedException {
        for (Map.Entry<Class<? extends Message>, Pair<Semaphore, ConcurrentLinkedQueue<Subscriber>>> entry : topicMap.entrySet()) {
            Pair<Semaphore, ConcurrentLinkedQueue<Subscriber>> topicPair = entry.getValue();
            Semaphore topicSemaphore = topicPair.getKey();

            topicSemaphore.acquire();
            try {
                topicPair.getValue().remove(m);
            } finally {
                topicSemaphore.release();
            }
        }
    }

    @Override
    public Message awaitMessage(Subscriber m) throws InterruptedException {
        Pair<Semaphore ,BlockingQueue<Message>> subPair = subscriberMap.get(m);
        Semaphore subSemaphore = subPair.getKey();

        subSemaphore.acquire();
        try{
            return subPair.getValue().take();
        } finally {
            subSemaphore.release();
        }
    }

    public static class Instance {
        private static MessageBroker instance = new MessageBrokerImpl();
    }
}
