package bgu.spl.mics.application.passiveObjects;

import java.util.List;

public class AgentsAvailableRport {
    private int moneypenny;
    private Boolean result;
    private List<String> agentNames;

    public AgentsAvailableRport(int moneypenny, Boolean result, List<String> agentNames) {
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

    public void setAgentNames(List<String> agentNames) {
        this.agentNames = agentNames;
    }
}
