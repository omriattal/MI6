package bgu.spl.mics.application.subscribers;

import bgu.spl.mics.MessageBrokerImpl;
import bgu.spl.mics.Subscriber;
import bgu.spl.mics.application.messages.AgentsAvailableEvent;
import bgu.spl.mics.application.messages.ReleaseAgentsEvent;
import bgu.spl.mics.application.messages.SendAgentsEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.passiveObjects.AgentsAvailableRport;
import bgu.spl.mics.application.passiveObjects.Squad;

import java.util.List;

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
        if (serialNumber % 2 == 0) {
            subscribeToAgentsAvailableEvent();
        }
        else {
            subscribeToReleasingEvents();
        }
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
            complete(event,new AgentsAvailableRport(serialNumber,result,agentNames));

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
        subscribeBroadcast(TickBroadcast.class, (broadcast)->{
            setCurrentTick(broadcast.getTimeTick());
        });
    }

    private void setCurrentTick(int timeTick) {
        this.currentTick = timeTick;
    }
}
