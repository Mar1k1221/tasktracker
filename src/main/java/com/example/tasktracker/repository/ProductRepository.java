package com.example.tasktracker.repository;

import com.example.tasktracker.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product,Integer> {
List<Product> findByPriceGreaterThan(BigDecimal price);
List<Product> findByCategoryOrderByPriceAsc(String category);
 long countByCategory(String category);
}
