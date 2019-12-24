package bgu.spl.mics.application.subscribers;

import bgu.spl.mics.*;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.passiveObjects.AgentsAvailableResult;
import bgu.spl.mics.application.passiveObjects.Diary;
import bgu.spl.mics.application.passiveObjects.MissionInfo;
import bgu.spl.mics.application.passiveObjects.Report;

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
        subscribeToFinalTickBroadcast();
        subscribeToMissionAvailableEvent();
    }

    private void subscribeToMissionAvailableEvent() {
        subscribeEvent(MissionReceivedEvent.class, (event) -> {
            diary.incrementTotal();
            MissionInfo missionInfo = event.getMissionInfo();
            SimplePublisher publish = getSimplePublisher();
            List<String> serials = missionInfo.getSerialAgentsNumbers();
            int missionDuration = missionInfo.getDuration();

            Future<AgentsAvailableResult> agentsAvailableFuture = publish.sendEvent(new AgentsAvailableEvent(serials));
            if (agentsAvailableFuture == null || agentsAvailableFuture.get() == null) {
                MessageBrokerImpl.getInstance().unregister(this);
                terminate();
                return;
            }
            if (Thread.currentThread().isInterrupted() && !agentsAvailableFuture.get().getResult()) {
                return;
            }

            Future<Pair<Boolean, Integer>> gadgetAvailableFuture = publish.sendEvent(new GadgetAvailableEvent(missionInfo.getGadget()));
            if (!gadgetAvailableFuture.get().getFirst()) {
                publish.sendEvent(new ReleaseAgentsEvent(serials));
                return;
            }

            Pair<Boolean, Integer> qResult = gadgetAvailableFuture.get();
            if (currentTick > missionInfo.getTimeExpired()) {
                publish.sendEvent(new ReleaseAgentsEvent(serials));
                return;
            }

            Future<Boolean> agentsSentFuture = publish.sendEvent(new SendAgentsEvent(serials, missionDuration));
            if (agentsSentFuture == null) {
                MessageBrokerImpl.getInstance().unregister(this);
                terminate();
                return;
            }

            addReportToDiary(missionInfo, serials, agentsAvailableFuture, qResult);
            complete(event, true);
        });
    }

    private void addReportToDiary(MissionInfo missionInfo, List<String> serials, Future<AgentsAvailableResult> agentsAvailableFuture, Pair<Boolean, Integer> qResult) throws InterruptedException {
        Report missionReport = new Report();
        AgentsAvailableResult agentsAvailableResult = agentsAvailableFuture.get();
        missionReport.setAgentsNames(agentsAvailableResult.getAgentNames());
        missionReport.setAgentsSerialNumbers(serials);
        missionReport.setGadgetName(missionInfo.getGadget());
        missionReport.setM(serialNumber);
        missionReport.setMissionName(missionInfo.getMissionName());
        missionReport.setMoneypenny(agentsAvailableResult.getMoneypenny());
        missionReport.setQTime(qResult.getSecond());
        missionReport.setTimeCreated(currentTick);
        missionReport.setTimeIssued(missionInfo.getTimeIssued());

        diary.addReport(missionReport);
    }

    private void subscribeToTimeTick() {
        subscribeBroadcast(TickBroadcast.class, (broadcast) -> {
            setCurrentTick(broadcast.getTimeTick());
        });
    }

    private void subscribeToFinalTickBroadcast() {
        subscribeBroadcast(FinalTickBroadcast.class, (FinalTickBroadcast) -> {
            MessageBrokerImpl.getInstance().unregister(this);
            terminate();
        });
    }

    private void setCurrentTick(int timeTick) {
        this.currentTick = timeTick;
    }
}
