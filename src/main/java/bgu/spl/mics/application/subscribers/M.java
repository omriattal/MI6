package bgu.spl.mics.application.subscribers;

import bgu.spl.mics.*;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.passiveObjects.Diary;
import bgu.spl.mics.application.passiveObjects.MissionInfo;

import java.util.List;

/**
 * M handles MissionAvailableEvent - fills a report and sends agents to mission.
 * <p>
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class M extends Subscriber {
	Diary diary = Diary.getInstance();
	int serialNumber;
    private int currentTick;

    //TODO: Refactor and update diary.
	public M(int serialNumber) {
		super("M");
		this.serialNumber = serialNumber;

	}

    @Override
    protected void initialize() {
        MessageBrokerImpl.getInstance().register(this);
        subscribeToMissionAvailableEvent();
    }

    private void subscribeToMissionAvailableEvent() {
        subscribeEvent(MissionReceivedEvent.class, (event) -> {
            MissionInfo missionInfo = event.getMissionInfo();
            SimplePublisher publish = getSimplePublisher();
            List<String> serials = missionInfo.getSerialAgentsNumbers();
            int missionDuration = missionInfo.getDuration();

            Future<Boolean> agentsAvailableFuture = publish.sendEvent(new AgentsAvailableEvent(serials));
            if (agentsAvailableFuture == null || !agentsAvailableFuture.get()) {
                complete(event, false);
                return;
            }

            Future<Boolean> gadgetAvailableFuture = publish.sendEvent(new GadgetAvailableEvent(missionInfo.getGadget()));
            if (gadgetAvailableFuture == null || !gadgetAvailableFuture.get()) {
                publish.sendEvent(new ReleaseAgentsEvent(serials));
                complete(event, false);
                return;
            }

            if (timePassed()) {
                publish.sendEvent(new ReleaseAgentsEvent(serials));
                complete(event, false);
                return;
            }

            Future<Boolean> agentsSentFuture = publish.sendEvent(new SendAgentsEvent(serials, missionDuration));
            if (agentsSentFuture == null) {
                publish.sendEvent(new ReleaseAgentsEvent(serials));
                complete(event, false);
                return;
            }
            complete(event, true);
        });
    }

    private void subscribeToTimeTick() {
        subscribeBroadcast(TickBroadcast.class, (broadcast)->{
            setCurrentTick(broadcast.getTimeTick());
        });
    }

    private void setCurrentTick(int timeTick) {
        this.currentTick = timeTick;
    }

    private boolean timePassed() {
        //TODO: Implement timePassed
        return false;
    }

}
