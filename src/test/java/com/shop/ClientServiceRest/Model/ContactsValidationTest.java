package com.shop.ClientServiceRest.Model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class ContactsValidationTest {
    private Validator validator;
    private Contacts contacts;

    @BeforeEach
    void init() {
        LocalValidatorFactoryBean localValidatorFactoryBean = new LocalValidatorFactoryBean();
        localValidatorFactoryBean.afterPropertiesSet();
        this.validator = localValidatorFactoryBean;

        this.contacts = new Contacts("123456", "Russia",
                "Moscow", "Bolotnaya street", "+7-499-123-45-67");
    }

    @Test
    void ShouldValidateWithCorrectData() {
        Set<ConstraintViolation<Contacts>> constraintViolations = validator.validate(contacts);
        assertThat(constraintViolations).hasSize(0);
    }

    @Test
    void ShouldNotValidateWithSmallZipCode() {
        contacts.setZipCode("123");

        Set<ConstraintViolation<Contacts>> constraintViolations = validator.validate(contacts);
        assertThat(constraintViolations).hasSize(1);

        ConstraintViolation<Contacts> violation = constraintViolations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("zipCode");
        assertThat(violation.getMessage()).isEqualTo("Почтовый индекс должен быть от 4 до 9 символов");
    }

    @Test
    void ShouldNotValidateWithBigZipCode() {
        contacts.setZipCode("1234567890");

        Set<ConstraintViolation<Contacts>> constraintViolations = validator.validate(contacts);
        assertThat(constraintViolations).hasSize(1);

        ConstraintViolation<Contacts> violation = constraintViolations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("zipCode");
        assertThat(violation.getMessage()).isEqualTo("Почтовый индекс должен быть от 4 до 9 символов");
    }

    @Test
    void ShouldNotValidateWithBlankCountry() {
        contacts.setCountry("  ");

        Set<ConstraintViolation<Contacts>> constraintViolations = validator.validate(contacts);
        assertThat(constraintViolations).hasSize(1);

        ConstraintViolation<Contacts> violation = constraintViolations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("country");
        assertThat(violation.getMessage()).isEqualTo("Страна должна быть задана");
    }

    @Test
    void ShouldNotValidateWithBlankCity() {
        contacts.setCity("  ");

        Set<ConstraintViolation<Contacts>> constraintViolations = validator.validate(contacts);
        assertThat(constraintViolations).hasSize(1);

        ConstraintViolation<Contacts> violation = constraintViolations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("city");
        assertThat(violation.getMessage()).isEqualTo("Город должен быть задан");
    }

    @Test
    void ShouldNotValidateWithBlankStreet() {
        contacts.setStreet("  ");

        Set<ConstraintViolation<Contacts>> constraintViolations = validator.validate(contacts);
        assertThat(constraintViolations).hasSize(1);

        ConstraintViolation<Contacts> violation = constraintViolations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("street");
        assertThat(violation.getMessage()).isEqualTo("Улица должна быть задана");
    }

    @Test
    void ShouldNotValidateWithBlankPhoneNumber() {
        contacts.setPhoneNumber("  ");

        Set<ConstraintViolation<Contacts>> constraintViolations = validator.validate(contacts);
        assertThat(constraintViolations).hasSize(1);

        ConstraintViolation<Contacts> violation = constraintViolations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("phoneNumber");
        assertThat(violation.getMessage()).isEqualTo("Номер телефона должен быть задан");
    }

    @Test
    void getterTests() {
        assertThat(contacts.getZipCode()).isEqualTo("123456");
        assertThat(contacts.getCountry()).isEqualTo("Russia");
        assertThat(contacts.getCity()).isEqualTo("Moscow");
        assertThat(contacts.getStreet()).isEqualTo("Bolotnaya street");
        assertThat(contacts.getPhoneNumber()).isEqualTo("+7-499-123-45-67");
    }
}
