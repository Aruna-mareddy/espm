package com.example.product.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.example.product.model.Product;

@Repository
public interface ProductRepository extends CrudRepository<Product, String>{
	
	@Query(value = "SELECT * FROM ESPM_PRODUCT WHERE PRODUCT_ID ID =?1", nativeQuery = true)
	Product findProductById();
}
