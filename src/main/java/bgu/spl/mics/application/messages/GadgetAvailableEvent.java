package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import jdk.incubator.http.internal.common.Pair;

public class GadgetAvailableEvent implements Event<Pair<Boolean, Integer>> {
    private String gadget;

    public GadgetAvailableEvent(String gadget) {
        this.gadget = gadget;
    }

    public String getGadget() {
        return gadget;
    }

}
