package bgu.spl.mics;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The {@link MessageBrokerImpl class is the implementation of the MessageBroker interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBrokerImpl implements MessageBroker {
    private ConcurrentHashMap<Subscriber, BlockingQueue<Message>> subscriberMap;
    private ConcurrentHashMap<Class<? extends Message>, ConcurrentLinkedQueue<Subscriber>> topicMap;
    private ConcurrentHashMap<Event, Future> eventMap;

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
        subscribeTopic(type, m);
    }

    @Override
    public void subscribeBroadcast(Class<? extends Broadcast> type, Subscriber m) {
        subscribeTopic(type, m);
    }

    private void subscribeTopic(Class<? extends Message> type, Subscriber m) {
        if (!topicMap.contains(type)) {
            topicMap.put(type, new ConcurrentLinkedQueue<>());
        }
        ConcurrentLinkedQueue<Subscriber> topicQueue = topicMap.get(type);
        topicQueue.add(m);
    }

    @Override
    public <T> void complete(Event<T> e, T result) {
        Future<T> toResolve = eventMap.get(e);
        toResolve.resolve(result);
    }

    @Override
    public void sendBroadcast(Broadcast b) {
        if (!topicMap.contains(b.getClass())) {
            System.out.println("No subscriber is registered to this topic.");
            return;
        }
        ConcurrentLinkedQueue<Subscriber> topicQueue = topicMap.get(b.getClass());
        for (Subscriber subscriber : topicQueue) {
            addMessageToSubQueue(b, subscriber);
        }
    }

    private void addMessageToSubQueue(Broadcast b, Subscriber subscriber) {
        BlockingQueue<Message> subQueue = subscriberMap.get(subscriber);
        try {
            subQueue.put(b);
        } catch (InterruptedException ignored) {
        }
    }

    @Override
    public <T> Future<T> sendEvent(Event<T> e) {
        assignEventAndRequeueSubscriber(e);
        Future<T> future = new Future<>();
        eventMap.put(e, future);
        return future;
    }

    private <T> void assignEventAndRequeueSubscriber(Event<T> e) {
        ConcurrentLinkedQueue<Subscriber> subQueue = topicMap.get(e.getClass());

        try {
            Subscriber first = subQueue.poll();
            BlockingQueue<Message> messageQueue = subscriberMap.get(first);
            messageQueue.put(e);
            subQueue.add(first);
        } catch (InterruptedException ignored) {
        }
    }

    @Override
    public void register(Subscriber m) {
        subscriberMap.put(m, new LinkedBlockingQueue<>());
    }

    @Override
    public void unregister(Subscriber m) {
        if (subscriberMap.remove(m) != null) removeFromTopicMap(m);
    }

    private void removeFromTopicMap(Subscriber m) {
        for (Map.Entry<Class<? extends Message>, ConcurrentLinkedQueue<Subscriber>> entry : topicMap.entrySet()) {
            ConcurrentLinkedQueue<Subscriber> currentQueue = entry.getValue();
            currentQueue.remove(m);
        }
    }

    @Override
    public Message awaitMessage(Subscriber m) throws InterruptedException {
        BlockingQueue<Message> messageQueue = subscriberMap.get(m);
        return messageQueue.take();
    }

    public static class Instance {
        private static MessageBroker instance = new MessageBrokerImpl();
    }
}
