package bgu.spl.mics.application.passiveObjects;

import java.util.List;

/**
 * Passive data-object representing information about a mission.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You may add ONLY private fields and methods to this class.
 */
public class MissionInfo {
    String missionName;
    List<String> serialAgentsNumbers;
    String gadget;
    int timeIssued;
    int timeExpired;
    int duration;

    /**
     * Retrieves the name of the mission.
     */
    public String getMissionName() {
        return missionName;
    }

    /**
     * Sets the name of the mission.
     */
    public void setMissionName(String missionName) {
        this.missionName = missionName;
    }

    /**
     * Retrieves the serial agent number.
     */
    public List<String> getSerialAgentsNumbers() {
        return serialAgentsNumbers;
    }

    /**
     * Sets the serial agent number.
     */
    public void setSerialAgentsNumbers(List<String> serialAgentsNumbers) {
        this.serialAgentsNumbers = serialAgentsNumbers;
    }

    /**
     * Retrieves the gadget name.
     */
    public String getGadget() {
        return gadget;
    }

    /**
     * Sets the gadget name.
     */
    public void setGadget(String gadget) {
        this.gadget = gadget;
    }

    /**
     * Retrieves the time the mission was issued in milliseconds.
     */
    public int getTimeIssued() {
        return timeIssued;
    }

    /**
     * Sets the time the mission was issued in milliseconds.
     */
    public void setTimeIssued(int timeIssued) {
        this.timeIssued = timeIssued;
    }

    /**
     * Retrieves the time that if it that time passed the mission should be aborted.
     */
    public int getTimeExpired() {
        return timeExpired;
    }

    /**
     * Sets the time that if it that time passed the mission should be aborted.
     */
    public void setTimeExpired(int timeExpired) {
        this.timeExpired = timeExpired;
    }

    /**
     * Retrieves the duration of the mission in time-ticks.
     */
    public int getDuration() {
        return duration;
    }

    /**
     * Sets the duration of the mission in time-ticks.
     */
    public void setDuration(int duration) {
        this.duration = duration;
    }
}
