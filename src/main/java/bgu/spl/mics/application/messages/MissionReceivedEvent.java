package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.passiveObjects.MissionInfo;

/**
 * Event sent by {@link bgu.spl.mics.application.subscribers.Intelligence} that indicates a mission with given info was
 * received.
 *
 * @param <T> no particular type as this event should never be completed.
 */
public class MissionReceivedEvent<T> implements Event<T> {
    MissionInfo missionInfo;

    public MissionReceivedEvent(MissionInfo missionInfo) {
        this.missionInfo = missionInfo;
    }

    public MissionInfo getMissionInfo() {
        return missionInfo;
    }
}
