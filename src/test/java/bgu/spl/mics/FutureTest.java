package bgu.spl.mics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

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

    @Test
    public void testGetTimeout(){
        Thread testT = new Thread(() -> future.resolve(true));
        testT.start();
        Boolean get = future.get(1000, TimeUnit.MILLISECONDS);
        if(get != null){
            assertTrue(future.isDone());
        }
    }
}