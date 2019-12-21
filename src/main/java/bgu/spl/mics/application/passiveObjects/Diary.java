package bgu.spl.mics.application.passiveObjects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Passive object representing the diary where all reports are stored.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You can add ONLY private fields and methods to this class as you see fit.
 */
public class Diary {
    private List<Report> reports;
    private transient ReadWriteLock reportsLock;
    private AtomicInteger total;

    private Diary() {
        reports = new ArrayList<>();
        total = new AtomicInteger(0);
        reportsLock = new ReentrantReadWriteLock();
    }

    /**
     * Retrieves the single instance of this class.
     */
    public static Diary getInstance() {
        return Instance.instance;
    }

    public List<Report> getReports() {
        reportsLock.readLock().lock();
        try {
            return reports;
        } finally {
            reportsLock.readLock().unlock();
        }
    }

    /**
     * adds a report to the diary
     *
     * @param reportToAdd - the report to add
     */
    public void addReport(Report reportToAdd) {
        reportsLock.writeLock().lock();
        try {
            reports.add(reportToAdd);
        } finally {
            reportsLock.writeLock().unlock();
        }
    }

    /**
     * <p>
     * Prints to a file name @filename a serialized object List<Report> which is a
     * List of all the reports in the diary.
     * This method is called by the main method in order to generate the output.
     */
    public void printToFile(String filename) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {

            String jsonOutput = gson.toJson(this);
            Files.write(Paths.get(filename), jsonOutput.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the total number of received missions (executed / aborted) be all the M-instances.
     *
     * @return the total number of received missions (executed / aborted) be all the M-instances.
     */
    public int getTotal() {
        return total.get();
    }

    /**
     * Increments the total number of received missions by 1
     */
    public void incrementTotal() {
        //TODO: scream at Dasha if this doesn't work
        total.incrementAndGet();
    }

    private static class Instance {
        private static Diary instance = new Diary();
    }
}
