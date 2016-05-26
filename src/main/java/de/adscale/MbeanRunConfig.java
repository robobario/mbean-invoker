package de.adscale;

import java.util.List;
import java.util.Map;

public class MbeanRunConfig {
    private List<App> applications;

    private Map<String, OperationConfig> operations;

    public List<App> getApplications() {
        return applications;
    }

    public void setApplications(List<App> applications) {
        this.applications = applications;
    }

    public Map<String, OperationConfig> getOperations() {
        return operations;
    }

    public void setOperations(Map<String, OperationConfig> operations) {
        this.operations = operations;
    }
}
