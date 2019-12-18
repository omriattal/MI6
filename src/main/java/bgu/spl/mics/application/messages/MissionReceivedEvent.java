package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.passiveObjects.MissionInfo;

public class MissionReceivedEvent<T> implements Event<T> {
    MissionInfo missionInfo;

    public MissionReceivedEvent(MissionInfo missionInfo){
        this.missionInfo = missionInfo;
    }

    public MissionInfo getMissionInfo() {
        return missionInfo;
    }
}
