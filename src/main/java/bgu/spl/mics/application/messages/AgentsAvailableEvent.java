package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.passiveObjects.AgentsAvailableRport;

import java.util.List;

public class AgentsAvailableEvent implements Event<AgentsAvailableRport> {
    List<String> serials;

    public AgentsAvailableEvent(List<String> serials){
        this.serials = serials;
    }

    public List<String> getSerials() {
        return serials;
    }
}
