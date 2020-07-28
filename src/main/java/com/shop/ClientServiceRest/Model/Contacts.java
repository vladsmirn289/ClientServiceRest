package com.shop.ClientServiceRest.Model;

import javax.persistence.Embeddable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Objects;

@Embeddable
public class Contacts {
    @Size(min = 4, max = 9, message = "Почтовый индекс должен быть от 4 до 9 символов")
    private String zipCode;

    @NotBlank(message = "Страна должна быть задана")
    private String country;

    @NotBlank(message = "Город должен быть задан")
    private String city;

    @NotBlank(message = "Улица должна быть задана")
    private String street;

    @NotBlank(message = "Номер телефона должен быть задан")
    private String phoneNumber;


    protected Contacts() {}

    public Contacts(String zipCode,
                    String country,
                    String city,
                    String street,
                    String phoneNumber) {
        this.zipCode = zipCode;
        this.country = country;
        this.city = city;
        this.street = street;
        this.phoneNumber = phoneNumber;
    }


    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Contacts contacts = (Contacts) o;
        return zipCode.equals(contacts.zipCode) &&
                country.equals(contacts.country) &&
                city.equals(contacts.city) &&
                street.equals(contacts.street) &&
                phoneNumber.equals(contacts.phoneNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(zipCode, country, city, street, phoneNumber);
    }
}
