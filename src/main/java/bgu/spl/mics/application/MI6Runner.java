package bgu.spl.mics.application;


import bgu.spl.mics.Subscriber;
import bgu.spl.mics.application.passiveObjects.Diary;
import bgu.spl.mics.application.passiveObjects.Inventory;
import bgu.spl.mics.application.passiveObjects.Squad;
import bgu.spl.mics.application.publishers.TimeService;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the Main class of the application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output serialized objects.
 */
public class MI6Runner {
    public static void main(String[] args) {
        try {
            AppInputJsonParser inputParser = new AppInputJsonParser(args[0]);
            Squad.getInstance().load(inputParser.getAgents());
            Inventory.getInstance().load(inputParser.getGadgets());
            List<Subscriber> subscribers = inputParser.getSubscribers();
            List<Thread> threadsList = new ArrayList<>();

            Thread newThread;
            for (Subscriber subscriber : subscribers) {
                newThread = new Thread(subscriber);
                threadsList.add(newThread);
                newThread.start();
            }

            //Sleep to give time for all the threads to finish
            //their init before we start the time service
            Thread.sleep(100);

            TimeService timeService = inputParser.getTimeService();
            Thread timeServiceThread = new Thread(timeService);
            timeServiceThread.start();

            joinOtherRunningThreads(threadsList);

            Inventory.getInstance().printToFile(args[1]);
            Diary.getInstance().printToFile(args[2]);
        } catch (InterruptedException ignored) {
        }
    }

    private static void joinOtherRunningThreads(List<Thread> threadsList) throws InterruptedException {
        for (Thread thread : threadsList) {
            thread.join();
        }
    }
}
