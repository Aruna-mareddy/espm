package com.example.customer.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.example.customer.model.Customer;

@Repository
public interface CustomerRepository extends CrudRepository<Customer, String>{
	
	@Query(value="SELECT * FROM ESPM_CUSTOMER WHERE EMAIL_ADDRESS = ?1", nativeQuery = true)
	Customer findCustomerByEmailAddress(String emialAddress);
	
	@Query(value="SELECT * FROM ESPM_CUSTOMER WHERE CUSTOMER_ID = ?1", nativeQuery = true)
	Customer findCustomerById(String customerId);

}
