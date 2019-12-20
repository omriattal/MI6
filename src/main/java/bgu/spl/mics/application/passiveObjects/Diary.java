package bgu.spl.mics.application.passiveObjects;

import java.util.List;

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
    private int total;

    /**
     * Retrieves the single instance of this class.
     */
    public static Diary getInstance() {
        return Instance.instance;
    }

    public List<Report> getReports() { return reports; }

    /**
     * adds a report to the diary
     *
     * @param reportToAdd - the report to add
     */
    public void addReport(Report reportToAdd) {
        reports.add(reportToAdd);
    }

    /**
     * <p>
     * Prints to a file name @filename a serialized object List<Report> which is a
     * List of all the reports in the diary.
     * This method is called by the main method in order to generate the output.
     */
    public void printToFile(String filename) {
        //TODO: Implement this
    }

    /**
     * Gets the total number of received missions (executed / aborted) be all the M-instances.
     *
     * @return the total number of received missions (executed / aborted) be all the M-instances.
     */
    public int getTotal() {
        return total;
    }
    /**
     * Increments the total number of received missions by 1
     */
    public void incrementTotal(){
        total++;
    }

    private static class Instance {
        private static Diary instance = new Diary();
    }
}
