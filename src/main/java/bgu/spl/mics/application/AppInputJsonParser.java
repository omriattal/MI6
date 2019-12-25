package bgu.spl.mics.application;

import bgu.spl.mics.Subscriber;
import bgu.spl.mics.application.passiveObjects.Agent;
import bgu.spl.mics.application.passiveObjects.MissionInfo;
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
import java.util.List;

public class AppInputJsonParser {
    String[] gadgets;
    Agent[] agents;
    List<Subscriber> subscribers;
    TimeService timeService;

    AppInputJsonParser(String filePath) {
        try {
            JsonObject inputJsonObject = JsonParser.parseReader(new FileReader(filePath)).getAsJsonObject();
            gadgets = createGadgetsArray(inputJsonObject);
            agents = createAgentsArray(inputJsonObject);
            subscribers = createSubscribersList(inputJsonObject);
            timeService = createTimeService(inputJsonObject);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
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
            missionInfo.setTimeIssued(mission.get("timeIssued").getAsInt());
            missionInfoList.add(missionInfo);
        }
    }

    private static void populateAgentsSerials(JsonObject mission, List<String> agentsSerials) {
        for (JsonElement agentSerialsElement : mission.get("serialAgentsNumbers").getAsJsonArray()) {
            agentsSerials.add(agentSerialsElement.getAsString());
        }
    }

    private static void createMSubs(int amountOfM, List<Subscriber> subscribers) {
        for (int i = 0; i < amountOfM; i++) {
            subscribers.add(new M(i));
        }
    }

    private static void createMoneypennySubs(int amountOfMoneyPenny, List<Subscriber> subscribers) {
        for (int i = 0; i < amountOfMoneyPenny; i++) {
            subscribers.add(new Moneypenny(i));
        }
    }

    private List<Subscriber> createSubscribersList(JsonObject inputJsonObject) {
        JsonObject services = inputJsonObject.getAsJsonObject("services");
        int amountOfM = services.get("M").getAsInt();
        JsonArray intelligences = services.get("intelligence").getAsJsonArray();
        int amountOfMoneyPenny = services.get("Moneypenny").getAsInt();
        List<Subscriber> subscribers = new ArrayList<>();

        createMoneypennySubs(amountOfMoneyPenny, subscribers);

        createMSubs(amountOfM, subscribers);

        createIntelligence(intelligences, subscribers);

        subscribers.add(new Q());
        return subscribers;
    }

    private void createIntelligence(JsonArray intelligences, List<Subscriber> subscribers) {
        List<MissionInfo> missionInfoList;
        JsonArray missions;
        int intelId = 0;
        for (JsonElement intelligence : intelligences) {
            missionInfoList = new ArrayList<>();
            missions = intelligence.getAsJsonObject().get("missions").getAsJsonArray();
            populateMissionInfoList(missionInfoList, missions);
            subscribers.add(new Intelligence(intelId, missionInfoList));
            intelId++;
        }
    }

    private String[] createGadgetsArray(JsonObject inputJsonObject) {
        JsonArray inventory = inputJsonObject.getAsJsonArray("inventory");
        String[] gadgets = new String[inventory.size()];
        int index = 0;
        for (JsonElement gadget : inventory) {
            String nameOfGadget = gadget.getAsString();
            gadgets[index] = nameOfGadget;
            index++;
        }
        return gadgets;
    }

    private Agent[] createAgentsArray(JsonObject inputJsonObject) {
        JsonArray squad = inputJsonObject.getAsJsonArray("squad");
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
        return agents;
    }

    private static TimeService createTimeService(JsonObject jsonObject) {
        JsonObject services = jsonObject.getAsJsonObject("services");
        int time = services.get("time").getAsInt();
        return new TimeService(time);
    }

    public Agent[] getAgents() {
        return agents;
    }

    public List<Subscriber> getSubscribers() {
        return subscribers;
    }

    public String[] getGadgets() {
        return gadgets;
    }

    public TimeService getTimeService() {
        return timeService;
    }
}
