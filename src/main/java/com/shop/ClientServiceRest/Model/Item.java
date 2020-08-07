package com.shop.ClientServiceRest.Model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.shop.ClientServiceRest.Jackson.ItemDeserializer;
import com.shop.ClientServiceRest.Jackson.ItemSerializer;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@JsonSerialize(using = ItemSerializer.class)
@JsonDeserialize(using = ItemDeserializer.class)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @NotBlank(message = "Название не может быть пустым")
    private String name;

    @PositiveOrZero(message = "Количество должно быть положительным числом, или равно нулю")
    private Long count;

    @Positive(message = "Вес должен быть положительным")
    private Double weight;

    @Positive(message = "Цена должна быть положительной")
    private Double price;

    @Size(min = 10, max = 50_000, message = "Описание должно быть от 10 символов")
    private String description;

    @NotBlank(message = "Характеристики должны быть заданы")
    @Size(max = 50_000)
    private String characteristics;

    private String image;

    @OneToMany(fetch = FetchType.EAGER,
               cascade = {CascadeType.REMOVE})
    @JsonIgnore
    private Set<Image> additionalImages = new HashSet<>();

    @NotBlank(message = "Код товара должен быть задан")
    private String code;

    @NotNull(message = "Категория должна существовать")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime createdOn;


    protected Item() {}

    public Item(String name,
                Long count,
                Double weight,
                Double price,
                String code) {
        this.name = name;
        this.count = count;
        this.weight = weight;
        this.price = price;
        this.code = code;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCharacteristics() {
        return characteristics;
    }

    public void setCharacteristics(String characteristics) {
        this.characteristics = characteristics;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Set<Image> getAdditionalImages() {
        return additionalImages;
    }

    public void setAdditionalImages(Set<Image> additionalImages) {
        this.additionalImages = additionalImages;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return code.equals(item.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }
}
