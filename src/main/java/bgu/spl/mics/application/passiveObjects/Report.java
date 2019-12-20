package bgu.spl.mics.application.passiveObjects;


import bgu.spl.mics.application.subscribers.M;
import bgu.spl.mics.application.subscribers.Moneypenny;

import java.util.List;

/**
 * Passive data-object representing a delivery vehicle of the store.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You may add ONLY private fields and methods to this class.
 */
public class Report {
    private String missionName;
    private int mId;
    private int moneypennyId;
    private List<String> agentSerials;
    private List<String> agentNames;
    private String gadget;
    private int qTime;
    private int timeIssued;
    private int timeCreated;

    /**
     * Retrieves the mission name.
     */
    public String getMissionName() { return missionName;}

    /**
     * Sets the mission name.
     */
    public void setMissionName(String missionName) { this.missionName = missionName;}

    /**
     * Retrieves the M's id.
     */
    public int getM() {return mId;}

    /**
     * Sets the M's id.
     */
    public void setM(int m) {this.mId =m;}


    /**
     * Retrieves the Moneypenny's id.
     */
    public int getMoneypenny() {return moneypennyId; }

    /**
     * Sets the Moneypenny's id.
     */
    public void setMoneypenny(int moneypenny) { this.moneypennyId = moneypenny; }

    /**
     * Retrieves the serial numbers of the agents.
     * <p>
     *
     * @return The serial numbers of the agents.
     */
    public List<String> getAgentsSerialNumbers() { return agentSerials; }

    /**
     * Sets the serial numbers of the agents.
     */
    public void setAgentsSerialNumbers(List<String> agentsSerialNumbers) { this.agentSerials = agentsSerialNumbers; }

    /**
     * Retrieves the agents names.
     * <p>
     *
     * @return The agents names.
     */
    public List<String> getAgentsNames() { return agentNames; }

    /**
     * Sets the agents names.
     */
    public void setAgentsNames(List<String> agentsNames) { this.agentNames = agentsNames; }

    /**
     * Retrieves the name of the gadget.
     * <p>
     *
     * @return the name of the gadget.
     */
    public String getGadgetName() { return  gadget;}

    /**
     * Sets the name of the gadget.
     */
    public void setGadgetName(String gadgetName) { this.gadget = gadget; }

    /**
     * Retrieves the time-tick in which Q Received the GadgetAvailableEvent for that mission.
     */
    public int getQTime() { return  qTime;}

    /**
     * Sets the time-tick in which Q Received the GadgetAvailableEvent for that mission.
     */
    public void setQTime(int qTime) { this.qTime = qTime; }

    /**
     * Retrieves the time when the mission was sent by an Intelligence Publisher.
     */
    public int getTimeIssued() { return timeIssued; }

    /**
     * Sets the time when the mission was sent by an Intelligence Publisher.
     */
    public void setTimeIssued(int timeIssued) { this.timeIssued = timeIssued; }

    /**
     * Retrieves the time-tick when the report has been created.
     */
    public int getTimeCreated() { return timeCreated; }

    /**
     * Sets the time-tick when the report has been created.
     */
    public void setTimeCreated(int timeCreated) { this.timeCreated = timeCreated; }
}
