package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;

public class GadgetAvailableEvent<T> implements Event<T> {
    private String gadget;

    public GadgetAvailableEvent(String gadget) {
        this.gadget = gadget;
    }

    public String getGadget() {
        return gadget;
    }

}
