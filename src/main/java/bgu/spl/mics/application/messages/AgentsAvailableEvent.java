package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.passiveObjects.AgentsAvailableResult;

import java.util.List;

/**
 * An event sent by M to check if given agents are available.
 */
public class AgentsAvailableEvent implements Event<AgentsAvailableResult> {
    List<String> serials;

    public AgentsAvailableEvent(List<String> serials){
        this.serials = serials;
    }

    public List<String> getSerials() {
        return serials;
    }
}
