package bgu.spl.mics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FutureTest {
    private Future<Boolean> future;

    @BeforeEach
    public void setUp(){
        future = new Future<>();
    }

    @Test
    public void testResolve(){
        future.resolve(true);
        assertTrue(future.isDone());
    }

    @Test
    public void testGet(){
        Thread testT = new Thread(() -> future.resolve(true));
        testT.start();
        assertTrue(future.get());
    }
}