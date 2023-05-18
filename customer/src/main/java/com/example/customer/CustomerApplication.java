package com.example.customer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

import com.example.customer.service.CustomerService;

@SpringBootApplication
@EnableRetry
public class CustomerApplication implements CommandLineRunner{
	
	private static final String Customer_Data_Location = "/customer.json";

	@Autowired
	CustomerService customerService;
	
	
	public static void main(String[] args) {
		SpringApplication.run(CustomerApplication.class, args);
	}


	@Override
	public void run(String... args) throws Exception {
		customerService.loadCustomer(Customer_Data_Location);
	}

	
}
