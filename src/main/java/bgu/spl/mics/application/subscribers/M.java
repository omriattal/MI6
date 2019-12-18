package bgu.spl.mics.application.subscribers;

import bgu.spl.mics.Future;
import bgu.spl.mics.MessageBrokerImpl;
import bgu.spl.mics.SimplePublisher;
import bgu.spl.mics.Subscriber;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.passiveObjects.Diary;
import bgu.spl.mics.application.passiveObjects.MissionInfo;

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
			MissionInfo missionInfo = event.getMissionInfo();
			SimplePublisher publish = getSimplePublisher();

			Future<Boolean> agentsAvailableFuture = publish.sendEvent(new AgentsAvailableEvent(missionInfo.getSerialAgentsNumbers()));
			if(agentsAvailableFuture == null || !agentsAvailableFuture.get()){
				//TODO: implement mission failed because there is no subscriber, or agents don't exist
				return;
			}
			Future<Boolean> gadgetAvailableFuture = publish.sendEvent(new GadgetAvailableEvent(missionInfo.getGadget()));
			if(gadgetAvailableFuture == null || !gadgetAvailableFuture.get()){
				//TODO: implement mission failed because there is no subscriber, or gadget doesn't exist
				publish.sendEvent(new ReleaseAgentsEvent());
				return;
			}
			if(timePassed()){
				//TODO: implement mission failed because there is no subscriber, or gadget doesn't exist
				return;
			}
			publish.sendEvent(new SendAgentsEvent());
		});
		
	}

	private boolean timePassed() {
		//TODO: Implement timePassed
		return false;
	}

}
