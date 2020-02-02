package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

/**
 * The broadcast of the time tick.
 */
public class TickBroadcast implements Broadcast {
    int timeTick;

    public TickBroadcast(int timeTick){
        this.timeTick = timeTick;
    }

    public int getTimeTick() {
        return timeTick;
    }
}


