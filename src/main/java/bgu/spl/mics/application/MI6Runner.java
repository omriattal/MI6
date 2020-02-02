package bgu.spl.mics.application;


import bgu.spl.mics.Subscriber;
import bgu.spl.mics.application.passiveObjects.Diary;
import bgu.spl.mics.application.passiveObjects.Inventory;
import bgu.spl.mics.application.passiveObjects.Squad;
import bgu.spl.mics.application.publishers.TimeService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * This is the Main class of the application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output serialized objects.
 */
public class MI6Runner {
    public static CountDownLatch latch = null;

    public static void main(String[] args) {
        try {
            AppInputJsonParser inputParser = new AppInputJsonParser(args[0]);
            Squad.getInstance().load(inputParser.getAgents());
            Inventory.getInstance().load(inputParser.getGadgets());
            List<Subscriber> subscribers = inputParser.getSubscribers();
            List<Thread> threadsList = new ArrayList<>();

            latch = new CountDownLatch(subscribers.size());
            Thread newThread;
            for (Subscriber subscriber : subscribers) {
                newThread = new Thread(subscriber);
                threadsList.add(newThread);
                newThread.start();
            }

            //We wait for all the threads to finish initializing before we continue to the TimeService.
            latch.await();

            TimeService timeService = inputParser.getTimeService();
            Thread timeServiceThread = new Thread(timeService);
            timeServiceThread.start();

            joinOtherRunningThreads(threadsList);

            Inventory.getInstance().printToFile(args[1]);
            Diary.getInstance().printToFile(args[2]);
        } catch (InterruptedException ignored) {
        }
    }

    /**
     * Iterates over given threads list and joins each one.
     *
     * @param threadsList th threads to join.
     * @throws InterruptedException
     */
    private static void joinOtherRunningThreads(List<Thread> threadsList) throws InterruptedException {
        for (Thread thread : threadsList) {
            thread.join();
        }
    }
}
