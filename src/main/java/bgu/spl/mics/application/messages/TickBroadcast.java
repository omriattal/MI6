package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

public class TickBroadcast implements Broadcast {
    int timeTick;

    TickBroadcast(int timeTick){
        this.timeTick = timeTick;
    }

    public int getTimeTick() {
        return timeTick;
    }
}


