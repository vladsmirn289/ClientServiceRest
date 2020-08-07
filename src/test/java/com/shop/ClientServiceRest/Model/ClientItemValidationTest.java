package com.shop.ClientServiceRest.Model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class ClientItemValidationTest {
    private Validator validator;
    private ClientItem clientItem;
    private Order order;
    private Item item;
    private Category books;

    @BeforeEach
    void init() {
        LocalValidatorFactoryBean localValidatorFactoryBean = new LocalValidatorFactoryBean();
        localValidatorFactoryBean.afterPropertiesSet();
        this.validator = localValidatorFactoryBean;

        this.books = new Category("Books");
        this.item = new Item("Spring 5", 50L, 1.592D, 3300D, "1234567");
        item.setDescription("..........");
        item.setCharacteristics("....");
        item.setCategory(books);

        ClientItem clientItem = new ClientItem(item, 1);
        clientItem.setId(1L);

        Contacts contacts = new Contacts("123456", "Russia",
                "Moscow", "Bolotnaya street", "+7-499-123-45-67");
        this.order = new Order(Collections.singleton(clientItem), contacts, "C.O.D");

        clientItem.setOrder(order);
        this.clientItem = clientItem;
    }

    @Test
    void shouldValidateWithCorrectData() {
        Set<ConstraintViolation<ClientItem>> constraintViolations = validator.validate(clientItem);
        assertThat(constraintViolations).hasSize(0);
    }

    @Test
    void shouldNotValidateWithNullItem() {
        clientItem.setItem(null);
        Set<ConstraintViolation<ClientItem>> constraintViolations = validator.validate(clientItem);
        assertThat(constraintViolations).hasSize(1);

        ConstraintViolation<ClientItem> violation = constraintViolations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("item");
        assertThat(violation.getMessage()).isEqualTo("Предмет должен быть задан");
    }

    @Test
    void shouldNotValidateWithNegativeQuantity() {
        clientItem.setQuantity(-1);
        Set<ConstraintViolation<ClientItem>> constraintViolations = validator.validate(clientItem);
        assertThat(constraintViolations).hasSize(1);

        ConstraintViolation<ClientItem> violation = constraintViolations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("quantity");
        assertThat(violation.getMessage()).isEqualTo("Количество должно быть положительным числом");
    }

    @Test
    void shouldNotValidateWithZeroQuantity() {
        clientItem.setQuantity(0);
        Set<ConstraintViolation<ClientItem>> constraintViolations = validator.validate(clientItem);
        assertThat(constraintViolations).hasSize(1);

        ConstraintViolation<ClientItem> violation = constraintViolations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("quantity");
        assertThat(violation.getMessage()).isEqualTo("Количество должно быть положительным числом");
    }

    @Test
    public void getterTests() {
        assertThat(clientItem.getId()).isEqualTo(1L);
        assertThat(clientItem.getItem()).isEqualTo(item);
        assertThat(clientItem.getOrder()).isEqualTo(order);
        assertThat(clientItem.getQuantity()).isEqualTo(1);
    }
}
