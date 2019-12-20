package bgu.spl.mics.application.passiveObjects;

import java.util.List;

public class AgentsAvailableRport {
    private int moneypennyId;
    private Boolean result;
    private List<String> agentNames;

    public AgentsAvailableRport(int moneypennyId, Boolean result, List<String> agentNames) {
        this.moneypennyId = moneypennyId;
        this.result = result;
        this.agentNames = agentNames;
    }
    public int getMoneypennyId() {
        return moneypennyId;
    }

    public Boolean getResult() {
        return result;
    }

    public List<String> getAgentNames() {
        return agentNames;
    }

}
