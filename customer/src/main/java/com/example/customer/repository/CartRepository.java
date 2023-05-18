package com.example.customer.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.example.customer.model.Cart;

public interface CartRepository extends CrudRepository<Cart, String>{
	
	@Query(value= "SELECT * FROM ESPM_CART WHERE CUSTOMER_ID = ?1", nativeQuery = true)
	Iterable<Cart> findCartByCustomerId(String customerId);

}
