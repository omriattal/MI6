package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;

import java.util.List;

public class SendAgentsEvent implements Event<Boolean> {
    private List<String> serials;
    private int duration;

    public SendAgentsEvent(List<String> serials,int duration) {
        this.serials = serials;
        this.duration=duration;
    }
    public List<String> getSerials() {
        return serials;
    }

    public int getDuration() {
        return duration;
    }
}
