package bgu.spl.mics.application.subscribers;

import bgu.spl.mics.*;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.passiveObjects.AgentsAvailableRport;
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
        currentTick = 0;
    }

    @Override
    protected void initialize() {
        MessageBrokerImpl.getInstance().register(this);
        subscribeToTimeTick();
        subscribeToMissionAvailableEvent();
    }

    private void subscribeToMissionAvailableEvent() {
        subscribeEvent(MissionReceivedEvent.class, (event) -> {
            MissionInfo missionInfo = event.getMissionInfo();
            SimplePublisher publish = getSimplePublisher();
            List<String> serials = missionInfo.getSerialAgentsNumbers();
            int missionDuration = missionInfo.getDuration();

            Future<AgentsAvailableRport> agentsAvailableFuture = publish.sendEvent(new AgentsAvailableEvent(serials));
            if (agentsAvailableFuture == null) {
                MessageBrokerImpl.getInstance().unregister(this);
                terminate();
                return;
            }
            if (agentsAvailableFuture.get() == null || !agentsAvailableFuture.get().getResult()) {
                return;
            }

            Future<Pair<Boolean, Integer>> gadgetAvailableFuture = publish.sendEvent(new GadgetAvailableEvent(missionInfo.getGadget()));
            if (gadgetAvailableFuture == null) {
                MessageBrokerImpl.getInstance().unregister(this);
                terminate();
                return;
            }
            if (gadgetAvailableFuture.get() == null || !gadgetAvailableFuture.get().getFirst()) {
                Future<Boolean> release = publish.sendEvent(new ReleaseAgentsEvent(serials));
                while (release != null && release.get() == null) {
                    release = publish.sendEvent(new ReleaseAgentsEvent(serials));
                }
                return;
            }

            Pair<Boolean, Integer> qResult = gadgetAvailableFuture.get();
            if (qResult.getSecond() > missionInfo.getTimeExpired()) {
                Future<Boolean> release = publish.sendEvent(new ReleaseAgentsEvent(serials));
                while (release != null && release.get() == null) {
                    release = publish.sendEvent(new ReleaseAgentsEvent(serials));
                }
                return;
            }

            Future<Boolean> agentsSentFuture = publish.sendEvent(new SendAgentsEvent(serials, missionDuration));
            if (agentsSentFuture == null) {
                publish.sendEvent(new ReleaseAgentsEvent(serials));
                return;
            }
            complete(event, true);
        });
    }

    private void subscribeToTimeTick() {
        subscribeBroadcast(TickBroadcast.class, (broadcast) -> {
            setCurrentTick(broadcast.getTimeTick());
        });
    }

    private void setCurrentTick(int timeTick) {
        this.currentTick = timeTick;
    }
}
