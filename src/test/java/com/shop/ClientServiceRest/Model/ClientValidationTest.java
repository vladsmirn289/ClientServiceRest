package com.shop.ClientServiceRest.Model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class ClientValidationTest {
    private Validator validator;
    private Client client;
    private Order order;
    private Item item;

    @BeforeEach
    void init() {
        LocalValidatorFactoryBean localValidatorFactoryBean = new LocalValidatorFactoryBean();
        localValidatorFactoryBean.afterPropertiesSet();
        this.validator = localValidatorFactoryBean;

        Category books = new Category("Books");
        this.item = new Item("Spring 5", 50L, 1.592D, 3300D, "1234567");
        this.item.setDescription("..........");
        this.item.setCharacteristics("....");
        this.item.setCategory(books);
        ClientItem clientItem = new ClientItem(item, 1);
        Contacts contacts = new Contacts("123456", "Russia",
                "Moscow", "Bolotnaya street", "+7-499-123-45-67");
        this.order = new Order(Collections.singleton(clientItem), contacts, "C.O.D");

        Client client = new Client("i@gmail.com", "12345",
                "Igor", "Key", "IK");
        client.setPatronymic("P");
        client.setId(1L);
        client.setBasket(Collections.singleton(clientItem));
        client.setOrders(Collections.singleton(order));
        client.setRoles(Collections.singleton(Role.USER));
        client.setConfirmationCode("123");
        this.client = client;
    }

    @Test
    void ShouldValidateWithCorrectData() {
        Set<ConstraintViolation<Client>> constraintViolations = validator.validate(client);
        assertThat(constraintViolations).hasSize(0);
    }

    @Test
    void ShouldNotValidateWithSmallFirstName() {
        client.setFirstName("K");

        Set<ConstraintViolation<Client>> constraintViolations = validator.validate(client);
        assertThat(constraintViolations).hasSize(1);

        ConstraintViolation<Client> violation = constraintViolations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("firstName");
        assertThat(violation.getMessage()).isEqualTo("Имя должно состоять как минимум из 3 символов");
    }

    @Test
    void ShouldNotValidateWithSmallLastName() {
        client.setLastName("K");

        Set<ConstraintViolation<Client>> constraintViolations = validator.validate(client);
        assertThat(constraintViolations).hasSize(1);

        ConstraintViolation<Client> violation = constraintViolations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("lastName");
        assertThat(violation.getMessage()).isEqualTo("Фамилия должна состоять как минимум из 3 символов");
    }

    @Test
    void ShouldNotValidateWithBlankLogin() {
        client.setLogin("  ");

        Set<ConstraintViolation<Client>> constraintViolations = validator.validate(client);
        assertThat(constraintViolations).hasSize(1);

        ConstraintViolation<Client> violation = constraintViolations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("login");
        assertThat(violation.getMessage()).isEqualTo("Логин не может быть пустым");
    }

    @Test
    void ShouldNotValidateWithSmallPassword() {
        client.setPassword("123");

        Set<ConstraintViolation<Client>> constraintViolations = validator.validate(client);
        assertThat(constraintViolations).hasSize(1);

        ConstraintViolation<Client> violation = constraintViolations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("password");
        assertThat(violation.getMessage()).isEqualTo("Пароль должен состоять из как минимум 5 символов");
    }

    @Test
    void ShouldNotValidateWithNullEmail() {
        client.setEmail(null);

        Set<ConstraintViolation<Client>> constraintViolations = validator.validate(client);
        assertThat(constraintViolations).hasSize(1);

        ConstraintViolation<Client> violation = constraintViolations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("email");
        assertThat(violation.getMessage()).isEqualTo("Email не может быть пустым");
    }

    @Test
    void ShouldNotValidateWithEmptyEmail() {
        client.setEmail("");

        Set<ConstraintViolation<Client>> constraintViolations = validator.validate(client);
        assertThat(constraintViolations).hasSize(1);

        ConstraintViolation<Client> violation = constraintViolations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("email");
        assertThat(violation.getMessage()).isEqualTo("Email не может быть пустым");
    }

    @Test
    void ShouldNotValidateWithIncorrectEmail() {
        client.setEmail("iw");

        Set<ConstraintViolation<Client>> constraintViolations = validator.validate(client);
        assertThat(constraintViolations).hasSize(1);

        ConstraintViolation<Client> violation = constraintViolations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("email");
        assertThat(violation.getMessage()).isEqualTo("Неправильный email");
    }

    @Test
    public void getterTests() {
        assertThat(client.getId()).isEqualTo(1L);
        assertThat(client.getFirstName()).isEqualTo("Igor");
        assertThat(client.getLastName()).isEqualTo("Key");
        assertThat(client.getLogin()).isEqualTo("IK");
        assertThat(client.getPassword()).isEqualTo("12345");
        assertThat(client.getEmail()).isEqualTo("i@gmail.com");
        assertThat(client.getPatronymic()).isEqualTo("P");
        assertThat(client.getBasket()).isEqualTo(Collections.singleton(new ClientItem(item, 1)));
        assertThat(client.getOrders()).isEqualTo(Collections.singleton(order));
        assertThat(client.getRoles()).isEqualTo(Collections.singleton(Role.USER));
        assertThat(client.getConfirmationCode()).isEqualTo("123");
    }

    @Test
    void shouldSetIsNonLocked() {
        client.setNonLocked(false);

        assertThat(client.isAccountNonLocked()).isFalse();
    }

    @Test
    void isMangerTest() {
        assertThat(client.isManager()).isFalse();

        client.setRoles(new HashSet<>(Arrays.asList(Role.USER, Role.MANAGER)));
        assertThat(client.isManager()).isTrue();
    }

    @Test
    void isAdminTest() {
        assertThat(client.isAdmin()).isFalse();

        client.setRoles(new HashSet<>(Arrays.asList(Role.USER, Role.ADMIN)));
        assertThat(client.isAdmin()).isTrue();
    }
}
