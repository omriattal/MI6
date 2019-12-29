package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.Pair;

/**
 * An event sent by M to check if given gadget is available.
 */
public class GadgetAvailableEvent implements Event<Pair<Boolean, Integer>> {
    private String gadget;

    public GadgetAvailableEvent(String gadget) {
        this.gadget = gadget;
    }

    public String getGadget() {
        return gadget;
    }

}
