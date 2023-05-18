package com.example.customer.controller;

import java.net.URI;

import java.time.Duration;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.customer.model.Cart;
import com.example.customer.model.Customer;
import com.example.customer.service.CustomerService;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.vavr.control.Try;

@RestController
@RequestMapping(path = CustomerController.API)
public class CustomerController {
	
	private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);
	
	protected static final String API = "/customer.svc/api/v1";
	protected static final String customer_API = "/customers/";
	protected static final String cart_API = "/carts/";
	
	
	@Autowired
	CustomerService customerService;
	
	private final RateLimiter rateLimiter = ConfigureRateLimiter();
	
	@GetMapping(CustomerController.customer_API + "{emailId}")
	public ResponseEntity<?> getCustomerByEmailId(@PathVariable("emailId") final String emailId) {
		try {
			Customer customer = getCustomer(emailId);
			if(customer != null) 
				  return new ResponseEntity<Customer>(customer, HttpStatus.OK);		
				return new ResponseEntity<String>("Customer Not Found", HttpStatus.NOT_FOUND);
		}catch(DataAccessException ex) {
				logger.error("Database is Down");
				return new ResponseEntity<String>("Database is temporarily down.Please try after some time.", HttpStatus.SERVICE_UNAVAILABLE);
			}	
	}
	
	@GetMapping(CustomerController.customer_API + "{customerId}" + CustomerController.cart_API)
	public ResponseEntity<?> getCartByCustomerId(@PathVariable("customerId") final String customerId) {
		try {
			Iterable<Cart> cart = customerService.getCartByCustomerId(customerId);
			if(cart != null) 
				  return new ResponseEntity<Iterable<Cart>>(cart, HttpStatus.OK);		
				return new ResponseEntity<String>("Cart is Empty", HttpStatus.NO_CONTENT);
		}catch(DataAccessException ex) {
				logger.error("Database is Down");
				return new ResponseEntity<String>("Database is temporarily down.Please try after some time.", HttpStatus.SERVICE_UNAVAILABLE);
			}	
	}
	
	@PostMapping(CustomerController.customer_API + "{customerId}" + CustomerController.cart_API )
	public ResponseEntity<?> addCart(@PathVariable("customerId") final String customerId, @RequestBody final Cart cart){
			try{
				final String itemId = java.util.UUID.randomUUID().toString();
				cart.setItemId(itemId);
				Customer customer = customerService.getCustomerById(customerId);
				cart.setCustomer(customer);
				customerService.saveCart(cart);
				return new ResponseEntity<String>("Item is added to cart", HttpStatus.OK);
			}catch(DataAccessException ex) {
				logger.error("Database is Down");
				return new ResponseEntity<String>("Database is temporarily down.Please try after some time.", HttpStatus.SERVICE_UNAVAILABLE);
			}
	}
	
	@PostMapping(CustomerController.customer_API )
	public ResponseEntity<Customer> addCustomer(@RequestBody final Customer customer){
			customerService.saveCustomer(customer);
			
			URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
					  .path("/{id}").buildAndExpand(customer.getCustomerId()).toUri();
			return ResponseEntity.created(uri).body(customer);
		
	}
	
	@PutMapping(CustomerController.customer_API + "{emailId}")
	public ResponseEntity<String> updateCustomerDetails(@RequestBody final Customer customer, @PathVariable("emailId") final String emailId){
		
			Customer get_customer = customerService.getCustomerByEmialId(emailId);
			if(get_customer.getPhoneNumber().equals(customer.getPhoneNumber())) {
				if(get_customer.getCity().equals(customer.getCity())) {
					return new ResponseEntity<String>("Data Provides is same with the previous one. please provide the new Data", HttpStatus.NOT_MODIFIED);
				}
				else{
					get_customer.setCity(customer.getCity());
					return new ResponseEntity<String>("City is Updated" , HttpStatus.CREATED);
				}
			}
			else {
				get_customer.setPhoneNumber(customer.getPhoneNumber());
				return new ResponseEntity<String>("PhoneNumber is Updated" , HttpStatus.CREATED);
			}
		
	}
	
	@PutMapping(CustomerController.customer_API + "{customerId}" + CustomerController.cart_API +"{itemId}" )
	public ResponseEntity<?> updateCart(@PathVariable("customerId") final String customerId, @RequestBody Cart cart, @PathVariable("itemId") final String itemId){
		try {
			Customer customer = customerService.getCustomerById(customerId);
			if(customerService.cartItemExists(itemId)) {
				cart.setItemId(itemId);
				cart.setCustomer(customer);
				customerService.saveCart(cart);
				return new ResponseEntity<String>("Cart item is Updated" + itemId, HttpStatus.ACCEPTED);
			}
			return new ResponseEntity<String>("Cart with" + itemId + "Not Found", HttpStatus.NOT_FOUND);
		}catch(DataAccessException ex) {
			logger.error("Database is Down");
			return new ResponseEntity<String>("Database is temporarily down.Please try after some time.", HttpStatus.SERVICE_UNAVAILABLE);
		}
	}
	
	@DeleteMapping(CustomerController.customer_API + "customerId" + CustomerController.cart_API + "{itemId}")
	public ResponseEntity<String> deleteCart(@PathVariable("customerId") final String customerId, @PathVariable("itemId") final String itemId){
		try {
			if(customerService.cartItemExists(itemId)) {
				customerService.deleteCart(itemId);;
				return new ResponseEntity<String>("Cart item is Deleted" + itemId, HttpStatus.ACCEPTED);
			}
			return new ResponseEntity<String>("Cart with" + itemId + "Not Found", HttpStatus.NOT_FOUND);
		}catch(DataAccessException ex) {
			logger.error("Database is Down");
			return new ResponseEntity<String>("Database is temporarily down.Please try after some time.", HttpStatus.SERVICE_UNAVAILABLE);
		}
	}
	
	public RateLimiter ConfigureRateLimiter() {
		RateLimiterConfig config = RateLimiterConfig.custom()
				               .timeoutDuration(Duration.ofMillis(500))
				               .limitRefreshPeriod(Duration.ofSeconds(1))
				               .limitForPeriod(5).build();
		
		RateLimiterRegistry rateLimiterRegistry = RateLimiterRegistry.of(config);
		RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter("customerService");
		return rateLimiter;
	}

	private Customer getCustomer(String emailId) {
		Supplier<Customer> customerSupplier = RateLimiter.decorateSupplier(rateLimiter, () -> customerService.getCustomerByEmialId(emailId));
		Customer customer = Try.ofSupplier(customerSupplier).get();
		return customer;
	}
	
	

}
