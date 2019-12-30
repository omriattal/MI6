package bgu.spl.mics;

import bgu.spl.mics.application.MI6Runner;
import bgu.spl.mics.application.passiveObjects.Diary;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO - READ ME
 * For running the tests do the following:
 *  1. Add MI6test file under bgu.spl.mics
 *  2. args[0] - Path for the main folder extracted from zip (which jsons and output folders inside)
 *  3. Since we are working with singleton in order to run this test
 *  please add 'clear' function for Diary and MessageBrokerImpl which will clear all the map and least (i.e. report.clear();)
 *  DO NOT forget to delete them after tests
 *
 * NOTE:All the json files generated using RANDOM parameters (with some basic logic).
 * They should be valid to your program, but output will not necessarily make sense (number of mission executed out of all,etc).
 * Therefore this tester design to find deadlock and flaws in your code. For performance compare the output result with friends (fullreport.json).
 * Feel free to add custom debugging prints such as:
 * "System.out.println(super.getName() + " Asking for gadget " + mission.getGadget());" (For M in example)
 */
public class MI6tests {
    public static void main(String[] args) throws InterruptedException {
        MessageBroker broker = MessageBrokerImpl.getInstance();
        Diary diary = Diary.getInstance();
        int counter = 1;
        List<String> testReport = new ArrayList<>();
        while (counter <= 100){
            System.out.println("Show Me What You Got !");
            String inputFile = args[0] + "/jsons/input" +counter+".json";
            String inventoryOutputFile = args[0] +"/output/inventoryOutputFile"+counter+".json";
            String diaryOutputFile = args[0] +"/output/diaryOutputFile"+counter+".json";
            String[] MI6args = new String[]{inputFile,inventoryOutputFile,diaryOutputFile};
            long start = System.currentTimeMillis();
            MI6Runner.main(MI6args);
            long done = System.currentTimeMillis();
            long took = done - start;
            System.out.println("Run number "+ counter + " completed in " + took+" Millis\n\n\n");
            testReport.add("Test with input" +counter+".json completed - " + diary.getReports().size() + "/" +diary.getTotal() + " completed out of total attempted missions. Total runtime "+ took+" Millis");
            Thread.sleep(1000);
            diary.clear(); //TODO must be added for tests
            broker.clear(); //TODO must be added for tests
            Thread.sleep(1000);
            counter++;
        }
        String fullreport = args[0] +"/fullreport.json";
        String finalReport = new Gson().toJson(testReport);
        File f = new File(fullreport);
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(fullreport, "UTF-8");
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        assert writer != null;
        writer.write(finalReport);
        writer.close();
    }
}
