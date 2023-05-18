package com.example.product.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.example.product.model.Stock;

@Repository
public interface StockRepository extends CrudRepository<Stock, String>{
	
	
	@Query(value = "SELECT * FROM ESPM_STOCK WHERE PRODUCT_ID = ?1", nativeQuery = true)
	Stock findStockByProductId();
	
	@Query()
	Iterable<Stock> findStockForAllProducts();
}
