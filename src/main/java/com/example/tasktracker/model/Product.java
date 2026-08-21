package com.example.tasktracker.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name="product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(nullable = false,length = 100)
    private String title;
    @Column(nullable = false,length = 100)
    private String description;
    @Column(precision = 10,scale = 2,nullable = false)
    private BigDecimal price;

    public Product(int id, String title, String description, BigDecimal price) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.price = price;
    }
protected Product(){

}
    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getTitle() {
        return title;
    }

    public BigDecimal getPrice() {
        return price;
    }
}
