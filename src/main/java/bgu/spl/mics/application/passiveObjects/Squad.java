package bgu.spl.mics.application.passiveObjects;

import java.util.*;

/**
 * Passive data-object representing a information about an agent in MI6.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You may add ONLY private fields and methods to this class.
 */
public class Squad {

    private Map<String, Agent> agents;

    /**
     * Private constructor as this is a thread safe singleton.
     */
    private Squad() {
        agents = new HashMap<>();
    }

    /**
     * Retrieves the single instance of this class.
     */
    public static Squad getInstance() {
        return Instance.instance;
    }

    /**
     * Initializes the squad. This method adds all the agents to the squad.
     * <p>
     *
     * @param agents Data structure containing all data necessary for initialization
     *               of the squad.
     */
    public void load(Agent[] agents) {
        if (agents == null) {
            return;
        }
        for (Agent agent : agents) {
            if (agent == null) {
                this.agents.clear();
                return;
            }
            this.agents.putIfAbsent(agent.getSerialNumber(), agent);
        }
    }

    /**
     * Releases agents.
     */
    public void releaseAgents(List<String> serials) {
        for (String serial : serials) {
            Agent agent = agents.get(serial);
            if (agent != null) {
                agent.release();
                synchronized (agent) {
                    agent.notifyAll();
                }
            }
        }
    }

    /**
     * simulates executing a mission by calling sleep.
     *
     * @param time time ticks to sleep
     */
    public void sendAgents(List<String> serials, int time) {
        try {
            Thread.sleep(time * 100);
        } catch (InterruptedException ignored) {
        } finally {
            releaseAgents(serials);
        }
    }

    /**
     * acquires an agent, i.e. holds the agent until the caller is done with it
     * <p>
     *
     * @param serials the serial numbers of the agents
     * @return ‘false’ if an agent of serialNumber ‘serial’ is missing, and ‘true’ otherwise
     */
    public boolean getAgents(List<String> serials) {
        serials.sort(Comparator.naturalOrder());
        for (String serial : serials) {
            Agent agent = agents.get(serial);
            if (agent == null) {
                releaseAgents(serials);
                return false;
            }
            synchronized (agent) {
                while (!agent.isAvailable()) {
                    try {
                        agent.wait();
                    } catch (InterruptedException ignored) {
                    }
                }
                agent.acquire();
            }
        }
        return true;
    }

    /**
     * gets the agents names
     *
     * @param serials the serial numbers of the agents
     * @return a list of the names of the agents with the specified serials.
     */
    public List<String> getAgentsNames(List<String> serials) {
        List<String> names = new ArrayList<>();
        for (String serial : serials) {
            Agent agent = agents.get(serial);
            if (agent != null)
                names.add(agent.getName());
        }
        return names;
    }

    public Map<String, Agent> getAgentsMap() {
        return agents;
    }

    /**
     * A class holding the single instance of {@link Squad}.
     */
    private static class Instance {
        private static Squad instance = new Squad();
    }

}
