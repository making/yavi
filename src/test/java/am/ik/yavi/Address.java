package am.ik.yavi;

public class Address {
    private final Country country;
    private final String street;
    private final PhoneNumber phoneNumber;

    public Address(Country country, String street, PhoneNumber phoneNumber) {
        this.country = country;
        this.street = street;
        this.phoneNumber = phoneNumber;
    }

    public Country country() {
        return this.country;
    }

    public String street() {
        return this.street;
    }

    public PhoneNumber phoneNumber() {
        return this.phoneNumber;
    }
}
