package bgu.spl.mics.application.subscribers;

import bgu.spl.mics.MessageBrokerImpl;
import bgu.spl.mics.Subscriber;
import bgu.spl.mics.application.messages.AgentsAvailableEvent;
import bgu.spl.mics.application.messages.ReleaseAgentsEvent;
import bgu.spl.mics.application.messages.SendAgentsEvent;
import bgu.spl.mics.application.passiveObjects.Squad;

import java.util.List;

/**
 * Only this type of Subscriber can access the squad.
 * There are several Moneypenny-instances - each of them holds a unique serial number that will later be printed on the report.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class Moneypenny extends Subscriber {
	private Squad squad;
	public Moneypenny() {
		super("Moneypenny");
		squad = Squad.getInstance();
	}

	@Override
	protected void initialize() {
		MessageBrokerImpl.getInstance().register(this);
		subscribeToEvents();
	}

	private void subscribeToEvents() {
		subscribeToAgentsAvailableEvent();
		subscribeToSendAgentsEvent();
		subscribeToReleaseAgentsEvent();
	}

	private void subscribeToAgentsAvailableEvent() {
		subscribeEvent(AgentsAvailableEvent.class, (event) -> {
			List<String> agentsToCheck = event.getSerials();
			boolean result = squad.getAgents(agentsToCheck);
			complete(event,result);
		});
	}
	private void subscribeToReleaseAgentsEvent() {
		subscribeEvent(ReleaseAgentsEvent.class, (event) -> {
		List<String> serials = event.getSerials();
		squad.releaseAgents(serials);
		complete(event,true);
		});
	}

	private void subscribeToSendAgentsEvent() {
		subscribeEvent(SendAgentsEvent.class,(event) -> {
		List<String> agentsToSend = event.getSerials();
		squad.sendAgents(agentsToSend,event.getDuration());
		complete(event,true);
		});
	}




}
