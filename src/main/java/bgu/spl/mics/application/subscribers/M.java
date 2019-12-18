package bgu.spl.mics.application.subscribers;

import bgu.spl.mics.Future;
import bgu.spl.mics.MessageBrokerImpl;
import bgu.spl.mics.Subscriber;
import bgu.spl.mics.application.messages.AgentsAvailableEvent;
import bgu.spl.mics.application.messages.MissionReceivedEvent;
import bgu.spl.mics.application.passiveObjects.Diary;

/**
 * M handles MissionAvailableEvent - fills a report and sends agents to mission.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class M extends Subscriber {
	Diary diary = Diary.getInstance();

	public M() {
		super("M");
	}

	@Override
	protected void initialize() {
		MessageBrokerImpl.getInstance().register(this);

		subscribeEvent(MissionReceivedEvent.class, (event) ->{
			Future<Boolean> agentsAvialble = getSimplePublisher().sendEvent(new AgentsAvailableEvent());
		});
		
	}

}
