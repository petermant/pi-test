package org.example.client.dtos.transaction;

public class AvsCvcCheck {
    private String status;
    private String address;
    private String postalCode;
    private String securityCode;

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(final String address) {
        this.address = address;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(final String postalCode) {
        this.postalCode = postalCode;
    }

    public String getSecurityCode() {
        return securityCode;
    }

    public void setSecurityCode(final String securityCode) {
        this.securityCode = securityCode;
    }

    @Override
    public String toString() {
        return "AcsCvcCheck{" +
                "status='" + status + '\'' +
                ", address='" + address + '\'' +
                ", postalCode='" + postalCode + '\'' +
                ", securityCode='" + securityCode + '\'' +
                '}';
    }
}
