package bgu.spl.mics.application.subscribers;

import bgu.spl.mics.MessageBrokerImpl;
import bgu.spl.mics.SimplePublisher;
import bgu.spl.mics.Subscriber;
import bgu.spl.mics.application.messages.FinalTickBroadcast;
import bgu.spl.mics.application.messages.MissionReceivedEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.passiveObjects.MissionInfo;

import java.util.Comparator;
import java.util.List;

/**
 * Subscriber/Publisher
 * Holds a list of Info objects and sends them
 * <p>
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class Intelligence extends Subscriber {
    private int currentTick;
    private int serialNumber;
    private List<MissionInfo> missionInfoList;

    public Intelligence(int serialNumber, List<MissionInfo> missionInfoList) {
        super("Intelligence");
        this.serialNumber = serialNumber;
        currentTick = 0;
        this.missionInfoList = missionInfoList;
        sortMissions();
    }

    /**
     * Sorts the {@code missionInfoList} by the {@code timeIssued}.
     */
    private void sortMissions() {
        missionInfoList.sort(Comparator.comparingInt(MissionInfo::getTimeIssued));
    }

    @Override
    protected void initialize() {
        subscribeToTimeTick();
        subscribeToFinalTickBroadcast();
    }

    private void subscribeToFinalTickBroadcast() {
        subscribeBroadcast(FinalTickBroadcast.class, (FinalTickBroadcast) -> {
            terminate();
        });
    }

    /**
     * Subscribes itself to the {@code TimeTickBroadcast}.
     * Updates the {@code currentTick} and acts as follows:
     * If the {@code missionInfoList} is not empty - checks if {@code currentTick} equals to a timeIssued of the first mission.
     * If it does - sends a new {@code MissionReceivedEvent} to the {@code MessageBroker} and removes the mission from the list.
     */
    private void subscribeToTimeTick() {
        SimplePublisher publisher = getSimplePublisher();
        subscribeBroadcast(TickBroadcast.class, (broadcast) -> {
            setCurrentTick(broadcast.getTimeTick());
            while (!missionInfoList.isEmpty()) {
                MissionInfo missionInfo = missionInfoList.get(0);
                if (currentTick == missionInfo.getTimeIssued()) {
//                    System.out.println("-------------- intel " + serialNumber + " sending mission: " + missionInfo.getMissionName());TODO: delete
                    publisher.sendEvent(new MissionReceivedEvent<>(missionInfo));
                    missionInfoList.remove(missionInfo);
                }
                else {
                    break;
                }
            }
        });
    }

    private void setCurrentTick(int timeTick) {
        this.currentTick = timeTick;
    }
}
