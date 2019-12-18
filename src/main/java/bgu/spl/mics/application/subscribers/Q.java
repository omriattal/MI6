package bgu.spl.mics.application.subscribers;

import bgu.spl.mics.MessageBroker;
import bgu.spl.mics.MessageBrokerImpl;
import bgu.spl.mics.Subscriber;
import bgu.spl.mics.application.messages.GadgetAvailableEvent;
import bgu.spl.mics.application.passiveObjects.Inventory;

/**
 * Q is the only Subscriber\Publisher that has access to the {@link bgu.spl.mics.application.passiveObjects.Inventory}.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class Q extends Subscriber {
	public Q() {
		super("Q");
	}

	@Override
	protected void initialize() {
		MessageBrokerImpl.getInstance().register(this); //registers Q in the MessageBroker
		subscribeEvent(GadgetAvailableEvent.class, (event) -> {
		boolean result = Inventory.getInstance().getItem(event.getGadget()); //checks availability of the gadget.
		complete(event,result);
		});
	}

}
