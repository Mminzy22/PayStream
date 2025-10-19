package com.example.inventory.repository;

import com.example.inventory.entity.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Product findByName(String name);

}
