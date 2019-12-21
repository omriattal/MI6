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
            // System.out.println("@@@@@ M @@@@@ Mission received. M: " + serialNumber + ", Mission name: " + missionInfo.getMissionName()); TODO: clean this

            Future<AgentsAvailableResult> agentsAvailableFuture = publish.sendEvent(new AgentsAvailableEvent(serials));
            // System.out.println("@@@@@ M @@@@@ Trying to get AgentsAvailable");
            if (agentsAvailableFuture == null || agentsAvailableFuture.get() == null) {
                //System.out.println("@@@@@ M @@@@@ No moneypenny to check availability");
                MessageBrokerImpl.getInstance().unregister(this);
                terminate();
                return;
            }
            if (!agentsAvailableFuture.get().getResult()) {
                //System.out.println("@@@@@ M @@@@@ Agents not available for mission " + missionInfo.getMissionName()); TODO: clean this
                return;
            }

            Future<Pair<Boolean, Integer>> gadgetAvailableFuture = publish.sendEvent(new GadgetAvailableEvent(missionInfo.getGadget()));
            // System.out.println("@@@@@ M @@@@@ trying to get GadgetsAvailable"); TODO: clean this
            if (gadgetAvailableFuture == null || gadgetAvailableFuture.get() == null) {
                //System.out.println("@@@@@ M @@@@@ No Q available"); TODO: clean this
                MessageBrokerImpl.getInstance().unregister(this);
                terminate();
                return;
            }
            if (!gadgetAvailableFuture.get().getFirst()) {
                //System.out.println("@@@@@ M @@@@@ Gadget not available for mission " + missionInfo.getMissionName()); TODO: clean this
                publish.sendEvent(new ReleaseAgentsEvent(serials));
                return;
            }
            ;
            Pair<Boolean, Integer> qResult = gadgetAvailableFuture.get();
            if (currentTick > missionInfo.getTimeExpired()) {
                //System.out.println("@@@@@ M @@@@@ TIME EXPIRED mission was aborted. qTime: " + qResult.getSecond()); TODO: clean this
                publish.sendEvent(new ReleaseAgentsEvent(serials));
                return;
            }

            Future<Boolean> agentsSentFuture = publish.sendEvent(new SendAgentsEvent(serials, missionDuration));
            if (agentsSentFuture == null) {
                //System.out.println("@@@@@ M @@@@@ No moneypenny to send agents"); TODO: clean this
                MessageBrokerImpl.getInstance().unregister(this);
                terminate();
                return;
            }

            addReportToDiary(missionInfo, serials, agentsAvailableFuture, qResult);
            complete(event, true);
            //System.out.println("@@@@@ M @@@@@ Mission completed " + missionInfo.getMissionName()); TODO: clean this
        });
    }

    private void addReportToDiary(MissionInfo missionInfo, List<String> serials, Future<AgentsAvailableResult> agentsAvailableFuture, Pair<Boolean, Integer> qResult) {
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
