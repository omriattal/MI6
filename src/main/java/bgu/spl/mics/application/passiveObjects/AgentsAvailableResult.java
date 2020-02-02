package bgu.spl.mics.application.passiveObjects;

import java.util.List;

/**
 * A result type of {@code AgentAvailableEvent} resolved by {@code Moneypenny}.
 * I.E sent inside a Future object.
 */
public class AgentsAvailableResult {

     // the serialNumber of Moneypenny received the {@code AgentAvailableEvent}.
    private int moneypenny;
    // the result of the action - true if the agent exists and false otherwise.
    private Boolean result;
    // the name of the agents specified by M, in the missionInfo.
    private List<String> agentNames;

    public AgentsAvailableResult(int moneypenny, Boolean result, List<String> agentNames) {
        this.moneypenny = moneypenny;
        this.result = result;
        this.agentNames = agentNames;
    }
    public int getMoneypenny() { return moneypenny;}

    public Boolean getResult() {
        return result;
    }

    public List<String> getAgentNames() {
        return agentNames;
    }

}
