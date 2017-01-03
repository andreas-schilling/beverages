package org.kiirun.beverages.infrastructure;

public enum Addresses {
    BEVERAGE_SALE("beverages.core.sell"),

    INFRASTRUCTURE("beverages.core.infrastructure"),;

    private String address;

    private Addresses(final String address) {
        this.address = address;
    }

    public String address() {
        return address;
    }
}
