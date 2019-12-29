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

    public M(int serialNumber) {
        super("M");
        this.serialNumber = serialNumber;
        currentTick = 0;
    }

    @Override
    protected void initialize() {
        subscribeToTimeTick();
        subscribeToFinalTickBroadcast();
        subscribeToMissionAvailableEvent();
    }

    /**
     * Subscribes itself to {@code MissionAvailableEvent} and providing the callback specified in the assignment.
     */
    private void subscribeToMissionAvailableEvent() {
        subscribeEvent(MissionReceivedEvent.class, (event) -> {
            //assigning help-variables.
            diary.incrementTotal();
            MissionInfo missionInfo = event.getMissionInfo();
            SimplePublisher publish = getSimplePublisher();
            List<String> serials = missionInfo.getSerialAgentsNumbers();
            int missionDuration = missionInfo.getDuration();

            Future<AgentsAvailableResult> agentsAvailableFuture = publish.sendEvent(new AgentsAvailableEvent(serials));

            /*
                if agentsAvailableFuture == null - no one is in AgentsAvailableEvent's queue.
                if agentsAvailableFuture.get() == null - Moneypenny unregistered.
             */

            if (agentsAvailableFuture == null || agentsAvailableFuture.get() == null) {
                terminate();
                return;
            }
            // if agentsAvailableFuture.get().getResult() == false - then the Agents specified does not exist.
            if (!agentsAvailableFuture.get().getResult()) {
                return;
            }

            Future<Pair<Boolean, Integer>> gadgetAvailableFuture = publish.sendEvent(new GadgetAvailableEvent(missionInfo.getGadget()));

             /*
                if gadgetAvailableFuture == null - no one is in GadgetAvailableEvent's queue.
                if gadgetAvailableFuture.get() == null - Q unregistered.
             */
            if (gadgetAvailableFuture == null || gadgetAvailableFuture.get() == null) {
                terminate();
                return;
            }

            /*
                if agentsAvailableFuture.get().getResult() == false - then the gadget specified does not exist.
                agents acquired need to be released.
             */

            if (!gadgetAvailableFuture.get().getFirst()) {
                publish.sendEvent(new ReleaseAgentsEvent(serials));
                return;
            }

            Pair<Boolean, Integer> qResult = gadgetAvailableFuture.get();

            //if the currentTick > missionInfo.getTimeExpired() - mission aborted - agents acquired need to be released.
            if (currentTick > missionInfo.getTimeExpired()) {
                publish.sendEvent(new ReleaseAgentsEvent(serials));
                return;
            }

            Future<Boolean> agentsSentFuture = publish.sendEvent(new SendAgentsEvent(serials, missionDuration));

            //agentsSentFuture == null - no one is in SendAgentsEvent's queue. need to terminate.
            if (agentsSentFuture == null) {
                terminate();
                return;
            }
            //adds report to the Diary.
            addReportToDiary(missionInfo, serials, agentsAvailableFuture, qResult);

            //mark mission as complete.
            complete(event, true);
        });
    }

    /**
     * Adds the completed mission as a {@code Report} to the {@code Diary}.
     * @param missionInfo - info about the mission.
     * @param serials - the serials of the agent sent to the mission.
     * @param agentsAvailableFuture - An object which contains a {@code List of AgentNames}, the {@code serial of Moneypenny}
     *                              and the result - true or false.
     * @param qResult - {@code Pair} of the {@code timeTick } which {@Code Q} got the {@code GadgetAvailableEvent} and
     *      *                              a {@code Future} which contains true.
     * @throws InterruptedException
     */
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
            terminate();
        });
    }

    private void setCurrentTick(int timeTick) {
        this.currentTick = timeTick;
    }
}
