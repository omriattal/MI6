package bgu.spl.mics.application.subscribers;

import bgu.spl.mics.Pair;
import bgu.spl.mics.Subscriber;
import bgu.spl.mics.application.messages.FinalTickBroadcast;
import bgu.spl.mics.application.messages.GadgetAvailableEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.passiveObjects.Inventory;

/**
 * Q is the only Subscriber\Publisher that has access to the {@link bgu.spl.mics.application.passiveObjects.Inventory}.
 * <p>
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class Q extends Subscriber {
    private int currentTick;

    public Q() {
        super("Q");
        currentTick = 0;
    }


    @Override
    protected void initialize() {
        subscribeToTimeTick();
        subscribeToFinalTickBroadcast();
        subscribeToGadgetAvailableEvent();
    }

    private void subscribeToFinalTickBroadcast() {
        subscribeBroadcast(FinalTickBroadcast.class, (FinalTickBroadcast) -> terminate());
    }

    private void subscribeToTimeTick() {
        subscribeBroadcast(TickBroadcast.class, (broadcast)-> setCurrentTick(broadcast.getTimeTick()));
    }

    private void setCurrentTick(int currentTick) {
        this.currentTick = currentTick;
    }

    /**
     * Subscribes itself to the {@code GadgetAvailableEvent}.
     * Checks the availability of the {@code gadget} in the {@code Inventory}.
     */
    private void subscribeToGadgetAvailableEvent() {
        subscribeEvent(GadgetAvailableEvent.class, (event) -> {
            boolean answer = Inventory.getInstance().getItem(event.getGadget());
            Pair<Boolean, Integer> result = new Pair<>(answer, currentTick);
            complete(event, result);
        });
    }
}
