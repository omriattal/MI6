package bgu.spl.mics.application.subscribers;

import bgu.spl.mics.Publisher;
import bgu.spl.mics.Subscriber;

/**
 * Subscriber/Publisher
 * Holds a list of Info objects and sends them
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class Intelligence extends Subscriber {
	int serialNumber;
	public Intelligence(int serialNumber) {
		super("Change_This_Name");
		this.serialNumber = serialNumber;
		// TODO Implement this
	}

	@Override
	protected void initialize() {
		// TODO Implement this
	}
}
