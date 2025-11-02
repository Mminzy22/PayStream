package com.example.inventory.product.repository;

import com.example.inventory.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Product findByName(String name);

}
