package bgu.spl.mics.application.passiveObjects;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Passive data-object representing a information about an agent in MI6.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You may add ONLY private fields and methods to this class.
 */
public class Agent {
    private String name;
    private String serialNumber;
    private boolean available = true;
    private ReadWriteLock nameLock = new ReentrantReadWriteLock();
    private ReadWriteLock serialNumberLock = new ReentrantReadWriteLock();
    private ReadWriteLock availabilityLock = new ReentrantReadWriteLock(true);

    /**
     * Sets the serial number of an agent.
     */
    public void setSerialNumber(String serialNumber) {
    	serialNumberLock.writeLock().lock();
    	try{
			this.serialNumber = serialNumber;
		} finally {
    		serialNumberLock.writeLock().unlock();
		}
    }

    /**
     * Retrieves the serial number of an agent.
     * <p>
     *
     * @return The serial number of an agent.
     */
    public String getSerialNumber() {
    	serialNumberLock.readLock().lock();
    	try {
			return serialNumber;
		} finally {
    		serialNumberLock.readLock().unlock();
		}
    }

    /**
     * Sets the name of the agent.
     */
    public void setName(String name) {
    	nameLock.writeLock().lock();
    	try{
			this.name = name;
		} finally {
    		nameLock.writeLock().unlock();
		}
    }

    /**
     * Retrieves the name of the agent.
     * <p>
     *
     * @return the name of the agent.
     */
    public String getName() {
    	nameLock.readLock().lock();
    	try{
    		return name;
		} finally {
    		nameLock.readLock().unlock();
		}
    }

    /**
     * Retrieves if the agent is available.
     * <p>
     *
     * @return if the agent is available.
     */
    public boolean isAvailable() {
        availabilityLock.readLock().lock();
        try {
            return available;
        } finally {
            availabilityLock.readLock().unlock();
        }
    }

    /**
     * Acquires an agent.
     */
    public void acquire() {
        availabilityLock.writeLock().lock();
        try {
            available = false;
        } finally {
            availabilityLock.writeLock().unlock();
        }
    }

    /**
     * Releases an agent.
     */
    public void release() {
        availabilityLock.writeLock().lock();
        try {
            available = true;
        } finally {
            availabilityLock.writeLock().unlock();
        }
    }
}
