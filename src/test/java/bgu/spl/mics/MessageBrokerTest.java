package bgu.spl.mics;

import bgu.spl.mics.application.subscribers.M;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MessageBrokerTest {
    MessageBroker mb;

    @BeforeEach
    public void setUp() {
        mb = MessageBrokerImpl.getInstance();
    }

    @Test
    public void testSubscribeEvent() {
        Event<Integer> integerEvent = new IntEvent();
        Event<Boolean> booleanEvent = new BoolEvent();
        Subscriber subscriber = new M();

        mb.subscribeEvent(IntEvent.class, subscriber);
        mb.sendEvent(integerEvent);
        mb.sendEvent(booleanEvent);

        try {
            assertEquals(IntEvent.class, mb.awaitMessage(subscriber).getClass());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testComplete() {
        Event<Integer> integerEvent = new IntEvent();
        Future<Integer> integerFuture = mb.sendEvent(integerEvent);

        mb.complete(integerEvent, 42);

        assertTrue(integerFuture.isDone());
    }
    @Test
    public void testSendEvent() {
        Event<Integer> integerEvent = new IntEvent();
        Future<Integer> future = mb.sendEvent(integerEvent);
        assertEquals(42,future.get());
    }

    @Test
    public void testAwaitMessage() {

        
    }


}
