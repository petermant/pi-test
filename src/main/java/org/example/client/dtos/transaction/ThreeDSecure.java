package org.example.client.dtos.transaction;

public class ThreeDSecure {
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "ThreeDSecure{" +
                "status='" + status + '\'' +
                '}';
    }
}
