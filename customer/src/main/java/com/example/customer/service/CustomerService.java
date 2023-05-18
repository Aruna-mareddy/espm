package com.example.customer.service;

import java.io.IOException;

import java.util.List;

import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.dao.DataAccessException;



import com.example.customer.model.Cart;
import com.example.customer.model.Customer;


public interface CustomerService {
	
	Customer saveCustomer(Customer customer);
	
	@Retryable(retryFor = DataAccessException.class, maxAttempts =2, backoff = @Backoff(delay = 200))
	Customer getCustomerById(String customerId);
	
	@Retryable(retryFor = DataAccessException.class, maxAttempts =2, backoff = @Backoff(delay = 200))
	Customer getCustomerByEmialId(String emailAddress);
	
	Cart saveCart(Cart cart);

	void deleteCart(String itemId);
	
	Iterable<Cart> getCartByCustomerId(String CustomerId);
	
	boolean cartItemExists(String itemId);
	
	void saveCustomer(List<Customer> listOfCustomers);
	
	void loadCustomer(String filePath) throws IOException;
}
