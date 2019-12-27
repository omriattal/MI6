package bgu.spl.mics.application.subscribers;

import bgu.spl.mics.MessageBrokerImpl;
import bgu.spl.mics.Subscriber;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.passiveObjects.*;

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
    private List<String> agentsSerialsList;
    private int currentTick;
    private static final AtomicInteger moneypennyCounter = new AtomicInteger(0);

    public Moneypenny(int serialNumber) {
        super("Moneypenny");
        this.serialNumber = serialNumber;
        squad = Squad.getInstance();
        currentTick = 0;
    }

    @Override
    protected void initialize() {
        subscribeToTimeTick();
        subscribeToFinalTickBroadcast();
        if (serialNumber % 2 == 0) {
            subscribeToAgentsAvailableEvent();
            moneypennyCounter.incrementAndGet();
        } else {
            subscribeToReleasingEvents();
        }
    }

    private void subscribeToFinalTickBroadcast() {
        subscribeBroadcast(FinalTickBroadcast.class, (FinalTickBroadcast) -> {
            terminate();
            if (serialNumber % 2 == 0) {
                moneypennyCounter.decrementAndGet();
                synchronized (moneypennyCounter) {
                    moneypennyCounter.notifyAll();
                }
            } else {
                synchronized (moneypennyCounter) {
                    while (moneypennyCounter.get() > 0) {
                        releaseAllAgents();
                        moneypennyCounter.wait();
                    }
                }
            }
        });
    }

    private void releaseAllAgents() {
        if (agentsSerialsList == null) {
            agentsSerialsList = new ArrayList<>();
            for (Map.Entry<String, Agent> agentEntry : squad.getAgentsMap().entrySet()) {
                agentsSerialsList.add(agentEntry.getKey());
            }
        }
        squad.releaseAgents(agentsSerialsList);
    }

    private void subscribeToReleasingEvents() {
        subscribeToSendAgentsEvent();
        subscribeToReleaseAgentsEvent();
    }

    private void subscribeToAgentsAvailableEvent() {
        subscribeEvent(AgentsAvailableEvent.class, (event) -> {
            List<String> agentsToGet = event.getSerials();
//            System.out.println("@@@@@ MP @@@@@ moneypenny: " + serialNumber + " getting agents " + agentsToGet); //TODO: delete
            List<String> agentNames = squad.getAgentsNames(agentsToGet);
//            System.out.println("@@@@@ MP @@@@@ moneypenny: " + serialNumber + " got agents " + agentsToGet); //TODO: delete
            boolean result = squad.getAgents(agentsToGet);
            synchronized (moneypennyCounter) {
                moneypennyCounter.notifyAll();
            }
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
