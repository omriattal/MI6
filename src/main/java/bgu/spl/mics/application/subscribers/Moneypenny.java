package bgu.spl.mics.application.subscribers;

import bgu.spl.mics.MessageBrokerImpl;
import bgu.spl.mics.Subscriber;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.passiveObjects.Agent;
import bgu.spl.mics.application.passiveObjects.AgentsAvailableResult;
import bgu.spl.mics.application.passiveObjects.Squad;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Only this type of Subscriber can access the squad.
 * There are several Moneypenny-instances - each of them holds a unique serial number that will later be printed on the report.
 * <p>
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class Moneypenny extends Subscriber {
    int serialNumber;
    private Squad squad;
    private int currentTick;
    private static final AtomicInteger moneypennyCounter = new AtomicInteger(0);
    private static final Object moneypennyCounterLock = new Object();

    public Moneypenny(int serialNumber) {
        super("Moneypenny");
        this.serialNumber = serialNumber;
        squad = Squad.getInstance();
        currentTick = 0;
    }

    @Override
    protected void initialize() {
        MessageBrokerImpl.getInstance().register(this);
        subscribeToTimeTick();
        subscribeToFinalTickBroadcast();
        if (serialNumber % 2 == 0) {
            subscribeToAgentsAvailableEvent();
            moneypennyCounter.incrementAndGet();
        }
        else {
            subscribeToReleasingEvents();
        }
    }

    private void subscribeToFinalTickBroadcast() {
        subscribeBroadcast(FinalTickBroadcast.class, (FinalTickBroadcast) -> {
            MessageBrokerImpl.getInstance().unregister(this);
            terminate();
            if (serialNumber % 2 == 0) {
                moneypennyCounter.decrementAndGet();
                synchronized (moneypennyCounterLock) {
                    moneypennyCounterLock.notifyAll();
                }
            }
            else {
                synchronized (moneypennyCounterLock) {
                    while (moneypennyCounter.get() > 0) {
                        releaseAllAgents();
                        moneypennyCounterLock.wait();
                    }
                }
            }
        });
    }

    private void releaseAllAgents() {
        List<String> agentsNames = new ArrayList<>();
        for (Map.Entry<String, Agent> agentEntry : squad.getAgentsMap().entrySet()) {
            agentsNames.add(agentEntry.getKey());
        }
        squad.releaseAgents(agentsNames);
    }

    private void subscribeToReleasingEvents() {
        subscribeToSendAgentsEvent();
        subscribeToReleaseAgentsEvent();
    }

    private void subscribeToAgentsAvailableEvent() {
        subscribeEvent(AgentsAvailableEvent.class, (event) -> {
            List<String> agentsToCheck = event.getSerials();
            List<String> agentNames = squad.getAgentsNames(agentsToCheck);
            boolean result = squad.getAgents(agentsToCheck);
            complete(event, new AgentsAvailableResult(serialNumber, result, agentNames));
        });
    }

    private void subscribeToReleaseAgentsEvent() {
        subscribeEvent(ReleaseAgentsEvent.class, (event) -> {
            List<String> serials = event.getSerials();
            squad.releaseAgents(serials);
            complete(event, true);
        });
    }

    private void subscribeToSendAgentsEvent() {
        subscribeEvent(SendAgentsEvent.class, (event) -> {
            List<String> agentsToSend = event.getSerials();
            squad.sendAgents(agentsToSend, event.getDuration());
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
