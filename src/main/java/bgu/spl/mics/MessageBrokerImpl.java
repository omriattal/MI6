package bgu.spl.mics;

import java.util.Map;
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

    /**
     * Constructor is private as this is a thread safe singleton.
     */
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

    /**
     * Adds given subscriber to the subscriber list of given type of message. If there is no list assigned for the type
     * yet then this method will add one for it.
     *
     * @param type the type of Message given, a class that extends Message.
     * @param m    the subscriber to add to the type's list.
     * @throws InterruptedException
     */
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

    /**
     * Gets the subscriber list of given topic from the topic map.
     *
     * @param topic the class extending Message that is the topic.
     * @return a {@link ConcurrentLinkedQueue<Subscriber>} that's the list of subscribers for the topic.
     */
    private ConcurrentLinkedQueue<Subscriber> getTopicQueue(Class<? extends Message> topic) {
        Pair<Semaphore, ConcurrentLinkedQueue<Subscriber>> topicPair = topicMap.get(topic);
        return topicPair.getSecond();
    }

    /**
     * Get's the fair {@link Semaphore} of a given topic.
     *
     * @param topic the topic to get the {@link Semaphore} of
     * @return a {@link Semaphore} that represents a fair lock for the given topic.
     */
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

    /**
     * Assigns the event {@param e} to the first subscriber in it's subscriber queue, then requeue the subscriber to
     * ensure the events are assigned in a round robin manner.
     *
     * @param e   the event to assign to the subscriber.
     * @param <T> the type of given event.
     * @throws InterruptedException
     */
    private <T> void assignEventAndRequeueSubscriber(Event<T> e) throws InterruptedException {
        Subscriber firstSub = getTopicQueue(e.getClass()).poll();
        getTopicQueue(e.getClass()).add(firstSub);

        addToSubQueue(e, firstSub);
    }

    /**
     * Adds {@param message} the the Message queue of given subscriber, in the map.
     *
     * @param message    to add to the queue.
     * @param subscriber to add the message to.
     * @throws InterruptedException
     */
    private void addToSubQueue(Message message, Subscriber subscriber) throws InterruptedException {
        BlockingQueue<Message> subQueue = getSubQueue(subscriber);
        subQueue.put(message);
    }

    @Override
    public void register(Subscriber m) {
        BlockingQueue<Message> subQueue = new LinkedBlockingQueue<>();
        subscriberMap.putIfAbsent(m, subQueue);
    }

    @Override
    public void unregister(Subscriber m) throws InterruptedException {
        //We acquire the topic map lock to make sure no event will be assigned while unregister happens.
        topicMapLock.acquire();
        try {
            if (subscriberMap.containsKey(m)) {
                removeFromTopicMap(m);
            }
        } finally {
            topicMapLock.release();
        }
        // We complete all the events as null to signal the publisher that the event will not be completed with wanted
        // result.
        completeAllEventsOfSubscriberAsNull(m);
        subscriberMap.remove(m);
    }

    /**
     * Iterates over the message list of given {@link Subscriber} to complete all the futures assigned to its events
     * as null.
     *
     * @param sub the {@link Subscriber} to do the action on.
     */
    private void completeAllEventsOfSubscriberAsNull(Subscriber sub) {
        BlockingQueue<Message> subQueue = getSubQueue(sub);
        for (Message message : subQueue) {
            if (message instanceof Event) {
                complete((Event) message, null);
            }
        }
    }

    /**
     * Iterates over the {@code topicMap} Removes given {@link Subscriber} from all the topics it's subscribed to.
     *
     * @param sub the subscriber to remove.
     * @throws InterruptedException
     */
    private void removeFromTopicMap(Subscriber sub) throws InterruptedException {
        Pair<Semaphore, ConcurrentLinkedQueue<Subscriber>> topicPair;
        Semaphore topicSemaphore;
        for (Map.Entry<Class<? extends Message>, Pair<Semaphore, ConcurrentLinkedQueue<Subscriber>>> entry : topicMap.entrySet()) {
            topicPair = entry.getValue();
            topicSemaphore = topicPair.getFirst();

            topicSemaphore.acquire();
            try {
                topicPair.getSecond().remove(sub);
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

    /**
     * Gets the {@link Message} queue of given subscriber.
     *
     * @param sub the given sub.
     * @return the queue of given sub.
     */
    private BlockingQueue<Message> getSubQueue(Subscriber sub) {
        return subscriberMap.get(sub);
    }

    /**
     * The class holding the single instance of the thread safe singleton.
     */
    private static class Instance {
        private static MessageBroker instance = new MessageBrokerImpl();
    }

    public void clear(){
        eventMap.clear();
        subscriberMap.clear();
        topicMap.clear();
    }
}
