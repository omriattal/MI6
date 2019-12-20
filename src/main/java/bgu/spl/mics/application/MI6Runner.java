package bgu.spl.mics.application;


import bgu.spl.mics.Subscriber;
import bgu.spl.mics.application.passiveObjects.Agent;
import bgu.spl.mics.application.publishers.TimeService;
import bgu.spl.mics.application.subscribers.M;
import bgu.spl.mics.application.subscribers.Moneypenny;
import bgu.spl.mics.application.subscribers.Q;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileNotFoundException;
import java.io.FileReader;

/** This is the Main class of the application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output serialized objects.
 *
 */
public class   MI6Runner {
    public static void main(String[] args) {
        String filePath = args[0];
        JsonObject jsonObject = null;
        try{
            jsonObject = JsonParser.parseReader(new FileReader(filePath)).getAsJsonObject();
        }
        catch (FileNotFoundException ignored) {}

    }
    private static void loadInventory(JsonObject jsonObject) {
        JsonArray inventory = jsonObject.getAsJsonArray("Inventory");
        String[] gadgets = new String[inventory.size()];
        int index = 0;
        for (JsonElement gadget: inventory) {
            String nameOfGadget = gadget.getAsJsonObject().get("name").getAsString();
            gadgets[index] = nameOfGadget;
        }
    }
    private static void loadSquad(JsonObject jsonObject) {
        JsonArray squad = jsonObject.getAsJsonArray("squad");
        Agent[] agents = new Agent[squad.size()];
        Agent agent;
        int index = 0;
        for (JsonElement agentElement: squad) {
            agent = new Agent();
            agent.setName(agentElement.getAsJsonObject().get("name").getAsString());
            agent.setSerialNumber(agentElement.getAsJsonObject().get("serialNumber").getAsString());
            agents[index] = agent;
            index++;
        }
    }
    //TODO: refactor
    private static Subscriber[] loadSubscribers (JsonObject jsonObject) {
        JsonObject services = jsonObject.getAsJsonObject("services");
        int amountOfM = services.get("M").getAsInt();
        int amountOfMoneyPenny = services.get("moneypenny").getAsInt();
        JsonArray missions = services.get("missions").getAsJsonArray();
        int amountOfQ = 1, amountOfTimeService = 1;
        int amountOfThreads = amountOfM+amountOfMoneyPenny + amountOfQ +amountOfTimeService;
        Subscriber[] subscribers = new Subscriber[amountOfThreads];
        int index = 0;
        for (int i = 0; i < amountOfM; i++) {
           subscribers[index] = new M(i);
           index++;
        }
        for (int i = 0; i < amountOfMoneyPenny; i++) {
            subscribers[index] = new Moneypenny(i);
            index++;
        }
        subscribers[index] = new Q();
        return subscribers;
    }
    private static TimeService createTimeService(JsonObject jsonObject) {
        JsonObject services = jsonObject.getAsJsonObject("services");
        int time = services.get("time").getAsInt();
        return new TimeService(time);

    }

}
