package bgu.spl.mics.application.subscribers;

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
    private List<String> agentsSerialsList;
    private int currentTick;
    /**
     * A private field that counts the number of moneypennys with an even {@code serialNumber}.
     * If the counter is > 0 then all "Releasing" Events moneypennys are restricted from terminating.
     */
    private static final AtomicInteger moneypennyCounter = new AtomicInteger(0);

    public Moneypenny(int serialNumber) {
        super("Moneypenny");
        this.serialNumber = serialNumber;
        squad = Squad.getInstance();
        currentTick = 0;
    }

    /**
     * Subscribes itself to the {@code timeTickBroadcast and FinalTickBroadcast}.
     * If the {@code serialNumber} is even - subscribes itself only to {@code AgentAvailableEvent}.
     * If not - subscribes itself to {@code SendAgentsEvent and ReleaseAgentEvent}.
     */
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

    /**
     * Subscribes itself to {@code FinalTickBroadcast}.
     * if the {@code serialNumber} is even - decrements the {@code moneypennycounter} and notify other "releasing" {@code Moneypenny}.
     * else - if the {@code moneypennycounter > 0} - means there are more "AgentAvailable" {@code Moneypennys}  releases all {@code agents}
     * and waits.
     */
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

    /**
     * Releases all {@code Agents} from the {@code Squad}.
     */
    private void releaseAllAgents() {
        if (agentsSerialsList == null) {
            agentsSerialsList = new ArrayList<>();
            for (Map.Entry<String, Agent> agentEntry : squad.getAgentsMap().entrySet()) {
                agentsSerialsList.add(agentEntry.getKey());
            }
        }
        squad.releaseAgents(agentsSerialsList);
    }

    /**
     * Subscribes itself to the {@code SendAgentsEvent and ReleaseAgentEvents}
     */
    private void subscribeToReleasingEvents() {
        subscribeToSendAgentsEvent();
        subscribeToReleaseAgentsEvent();
    }

    private void subscribeToAgentsAvailableEvent() {
        subscribeEvent(AgentsAvailableEvent.class, (event) -> {
            List<String> agentsToGet = event.getSerials();
            List<String> agentNames = squad.getAgentsNames(agentsToGet);
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
        subscribeBroadcast(TickBroadcast.class, (broadcast) -> setCurrentTick(broadcast.getTimeTick()));
    }

    private void setCurrentTick(int timeTick) {
        this.currentTick = timeTick;
    }
}
