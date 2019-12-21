package bgu.spl.mics.application;


import bgu.spl.mics.Subscriber;
import bgu.spl.mics.application.passiveObjects.*;
import bgu.spl.mics.application.publishers.TimeService;
import bgu.spl.mics.application.subscribers.Intelligence;
import bgu.spl.mics.application.subscribers.M;
import bgu.spl.mics.application.subscribers.Moneypenny;
import bgu.spl.mics.application.subscribers.Q;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This is the Main class of the application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output serialized objects.
 */
public class MI6Runner {
    public static void main(String[] args) {
        String filePath = args[0];
        JsonObject jsonObject = null;
        try {
            jsonObject = JsonParser.parseReader(new FileReader(filePath)).getAsJsonObject();
            loadInventory(jsonObject);
            loadSquad(jsonObject);
            List<Subscriber> subscribers = loadSubscribers(jsonObject);
            List<Thread> threadsList = new ArrayList<>();

            Thread newThread;
            for (Subscriber subscriber : subscribers) {
                newThread = new Thread(subscriber);
                threadsList.add(newThread);
                newThread.start();
            }

            confirmThreadsStarted(threadsList);

            TimeService timeService = createTimeService(jsonObject);
            Thread timeServiceThread = new Thread(timeService);
            timeServiceThread.start();

            for (Thread thread : threadsList) {
                thread.join();
            }

            Inventory.getInstance().printToFile(args[1]);
            Diary.getInstance().printToFile(args[2]);
        } catch (FileNotFoundException | InterruptedException ignored) {
        }
    }

    private static void confirmThreadsStarted(List<Thread> threadList) {
        boolean threadsInitialized = false;
        while (!threadsInitialized) {
            threadsInitialized = true;
            for (Thread thread : threadList) {
                if (thread.getState() != Thread.State.WAITING) {
                    threadsInitialized = false;
                    break;
                }
            }
        }
    }

    private static void loadInventory(JsonObject jsonObject) {
        JsonArray inventory = jsonObject.getAsJsonArray("inventory");
        String[] gadgets = new String[inventory.size()];
        int index = 0;
        for (JsonElement gadget : inventory) {
            String nameOfGadget = gadget.getAsJsonObject().get("name").getAsString();
            gadgets[index] = nameOfGadget;
            index++;
        }
        Inventory.getInstance().load(gadgets);
    }

    private static void loadSquad(JsonObject jsonObject) {
        JsonArray squad = jsonObject.getAsJsonArray("squad");
        Agent[] agents = new Agent[squad.size()];
        Agent agent;
        int index = 0;
        for (JsonElement agentElement : squad) {
            agent = new Agent();
            agent.setName(agentElement.getAsJsonObject().get("name").getAsString());
            agent.setSerialNumber(agentElement.getAsJsonObject().get("serialNumber").getAsString());
            agents[index] = agent;
            index++;
        }
        Squad.getInstance().load(agents);
    }

    private static List<Subscriber> loadSubscribers(JsonObject jsonObject) {
        JsonObject services = jsonObject.getAsJsonObject("services");
        int amountOfM = services.get("M").getAsInt();
        JsonArray intelligences = services.get("intelligence").getAsJsonArray();
        int amountOfMoneyPenny = services.get("Moneypenny").getAsInt();
        List<Subscriber> subscribers = new ArrayList<>();

        loadMoneypennySubs(amountOfMoneyPenny, subscribers);

        loadMSubs(amountOfM, subscribers);

        loadIntelligenceSubs(intelligences, subscribers);

        subscribers.add(new Q());
        return subscribers;
    }

    private static void loadIntelligenceSubs(JsonArray intelligences, List<Subscriber> subscribers) {
        List<MissionInfo> missionInfoList;
        MissionInfo missionInfo;
        JsonArray missions;
        JsonObject mission;
        List<String> agentsSerials;
        int intelId = 0;
        for (JsonElement intelligence : intelligences) {
            missionInfoList = new ArrayList<>();
            missions = intelligence.getAsJsonObject().get("missions").getAsJsonArray();
            populateMissionInfoList(missionInfoList, missions);
            subscribers.add(new Intelligence(intelId, missionInfoList));
            intelId++;
        }
    }

    static void populateMissionInfoList(List<MissionInfo> missionInfoList, JsonArray missions) {
        MissionInfo missionInfo;
        JsonObject mission;
        List<String> agentsSerials;
        for (int i = 0; i < missions.size(); i++) {
            missionInfo = new MissionInfo();
            mission = missions.get(i).getAsJsonObject();
            agentsSerials = new ArrayList<>();
            populateAgentsSerials(mission, agentsSerials);
            missionInfo.setSerialAgentsNumbers(agentsSerials);
            missionInfo.setDuration(mission.get("duration").getAsInt());
            missionInfo.setGadget(mission.get("gadget").getAsString());
            missionInfo.setMissionName(mission.get("name").getAsString());
            missionInfo.setTimeExpired(mission.get("timeExpired").getAsInt());
            missionInfoList.add(missionInfo);
        }
    }

    private static void populateAgentsSerials(JsonObject mission, List<String> agentsSerials) {
        for (JsonElement agentSerialsElement : mission.get("agents").getAsJsonArray()) {
            agentsSerials.add(agentSerialsElement.getAsJsonObject().get("serialNumber").getAsString());
        }
    }

    private static void loadMSubs(int amountOfM, List<Subscriber> subscribers) {
        for (int i = 0; i < amountOfM; i++) {
            subscribers.add(new M(i));
        }
    }

    private static void loadMoneypennySubs(int amountOfMoneyPenny, List<Subscriber> subscribers) {
        for (int i = 0; i < amountOfMoneyPenny; i++) {
            subscribers.add(new Moneypenny(i));
        }
    }

    private static TimeService createTimeService(JsonObject jsonObject) {
        JsonObject services = jsonObject.getAsJsonObject("services");
        int time = services.get("time").getAsInt();
        return new TimeService(time);
    }

}
