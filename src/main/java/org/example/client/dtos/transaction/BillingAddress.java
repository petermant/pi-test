package org.example.client.dtos.transaction;

import java.util.Objects;

public class BillingAddress {
    private final String address1;
    private final String city;
    private final String postalCode;
    private final String country;

    public BillingAddress(final String address1, final String city, final String postalCode, final String country) {
        this.address1 = address1;
        this.city = city;
        this.postalCode = postalCode;
        this.country = country;
    }

    public String getAddress1() {
        return address1;
    }

    public String getCity() {
        return city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getCountry() {
        return country;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof BillingAddress)) return false;
        final BillingAddress that = (BillingAddress) o;
        return Objects.equals(address1, that.address1) && Objects.equals(city, that.city) && Objects.equals(postalCode, that.postalCode) && Objects.equals(country, that.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address1, city, postalCode, country);
    }
}
