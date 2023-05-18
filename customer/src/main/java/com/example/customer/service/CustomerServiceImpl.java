package com.example.customer.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.customer.model.Cart;
import com.example.customer.model.Customer;
import com.example.customer.repository.CartRepository;
import com.example.customer.repository.CustomerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

@Service
public class CustomerServiceImpl implements CustomerService{
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private CustomerRepository customerRepository;
	
	@Autowired
	private CartRepository cartRepository;

	@Override
	public Customer saveCustomer(Customer customer) {
		return customerRepository.save(customer);
	}

	@Override
	public Customer getCustomerById(String customerId) {
		return customerRepository.findCustomerById(customerId);
	}

	@Override
	public Customer getCustomerByEmialId(String emailAddress) {
		return customerRepository.findCustomerByEmailAddress(emailAddress);
	}

	@Override
	public Cart saveCart(Cart cart) {
		return cartRepository.save(cart);
	}

	@Override
	public void deleteCart(String itemId) {
		cartRepository.deleteById(itemId);
	}

	@Override
	public Iterable<Cart> getCartByCustomerId(String CustomerId) {
		return cartRepository.findCartByCustomerId(CustomerId);
	}

	@Override
	public boolean cartItemExists(String itemId) {
		return cartRepository.existsById(itemId);
	}

	@Override
	public void saveCustomer(List<Customer> listOfCustomers) {
		customerRepository.saveAll(listOfCustomers);	
	}

	@Override
	public void loadCustomer(String filePath) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		TypeReference<List<Customer>> typeReference = new TypeReference<List<Customer>>() {
			
		};
		InputStream inputStream = null;
		try {
			inputStream = TypeReference.class.getResourceAsStream(filePath);
			List<Customer> listOfCustomers = mapper.readValue(inputStream, typeReference);
			saveCustomer(listOfCustomers);
		} catch (IOException e) {
			logger.error("loading of customer data failed");
			throw e;
		}
		
	}

}
