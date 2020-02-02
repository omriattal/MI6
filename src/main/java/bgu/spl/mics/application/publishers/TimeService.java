package bgu.spl.mics.application.publishers;

import bgu.spl.mics.Publisher;
import bgu.spl.mics.SimplePublisher;
import bgu.spl.mics.application.messages.FinalTickBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;

/**
 * TimeService is the global system timer There is only one instance of this Publisher.
 * It keeps track of the amount of ticks passed since initialization and notifies
 * all other subscribers about the current time tick using {@link TickBroadcast}.
 * This class may not hold references for objects which it is not responsible for.
 * <p>
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class TimeService extends Publisher {
    private static TimeService instance = null;
    private int currentTick;
    private int totalTicks;

    public TimeService(int totalTicks) {
        super("TimeService");
        currentTick = 0;
        this.totalTicks = totalTicks;
    }

    @Override
    protected void initialize() {
    }

    /**
     * Sends a time-tick in every 100 milliseconds.
     * when {@code currentTick > totalTicks} - sents {@code FinalTickBroadcast} to every {@code Subscribers}
     */
    @Override
    public void run() {
        SimplePublisher publisher = getSimplePublisher();
        while (currentTick <= totalTicks) {
            try {
                publisher.sendBroadcast(new TickBroadcast(currentTick));
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
            }
            currentTick++; 
        }
        publisher.sendBroadcast(new FinalTickBroadcast());
    }
}
