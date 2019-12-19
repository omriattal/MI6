package bgu.spl.mics.application.subscribers;

import bgu.spl.mics.MessageBrokerImpl;
import bgu.spl.mics.Subscriber;
import bgu.spl.mics.application.messages.GadgetAvailableEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.passiveObjects.Inventory;
import javafx.util.Pair;

/**
 * Q is the only Subscriber\Publisher that has access to the {@link bgu.spl.mics.application.passiveObjects.Inventory}.
 * <p>
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class Q extends Subscriber {
    private static Q instance = null;
    private int currentTick;

    private Q() {
        super("Q");
    }

    public static Q getInstance() {
        if (instance == null) instance = new Q();
        return instance;
    }

    @Override
    protected void initialize() {
        MessageBrokerImpl.getInstance().register(this); //registers Q to the messagebroker
        subscribeToGadgetAvailableEvent();
        subscribeToTimeTick();
    }

    private void subscribeToTimeTick() {
        subscribeBroadcast(TickBroadcast.class, (broadcast)->{
            setCurrentTick(broadcast.getTimeTick());
        });
    }

    private void setCurrentTick(int currentTick) {
        this.currentTick = currentTick;
    }

    private void subscribeToGadgetAvailableEvent() {
        subscribeEvent(GadgetAvailableEvent.class, (event) -> {
            boolean answer = Inventory.getInstance().getItem(event.getGadget()); //checks availability of the gadget.
            Pair<Boolean, Integer> result = new Pair<>(answer, currentTick);
            complete(event, result);
        });
    }
}
