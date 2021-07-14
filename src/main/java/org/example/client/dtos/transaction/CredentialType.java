package org.example.client.dtos.transaction;

public class CredentialType {
    private final String cofUsage;
    private final String initiatedType;
    private final String mitType = "Unscheduled";

    public CredentialType(String cofUsage, String initiatedType) {
        this.cofUsage = cofUsage;
        this.initiatedType = initiatedType;
    }


    public String getCofUsage() {
        return cofUsage;
    }

    public String getInitiatedType() {
        return initiatedType;
    }

    public String getMitType() {
        return mitType;
    }
}
