package com.example.similar_products.controller;

import com.example.similar_products.model.ProductDetail;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
public class ProductController {

    @Autowired
    private RestTemplate restTemplate;

    private static final String SIMILAR_IDS_URL = "http://localhost:3001/product/{productId}/similarids";
    private static final String PRODUCT_DETAIL_URL = "http://localhost:3001/product/{productId}";

    @CircuitBreaker(name = "similarProducts", fallbackMethod = "fallbackGetSimilarProducts")
    @GetMapping("/product/{productId}/similar")
    public ResponseEntity<List<ProductDetail>> getSimilarProducts(@PathVariable String productId) {
        try {
            // Obtener los IDs de productos similares
            String[] similarIds = restTemplate.getForObject(SIMILAR_IDS_URL, String[].class, productId);

            if (similarIds == null || similarIds.length == 0) {
                return ResponseEntity.ok(List.of()); 
            }

  
            List<CompletableFuture<ProductDetail>> futures = Arrays.stream(similarIds)
                    .map(id -> CompletableFuture.supplyAsync(() -> {
                        try {
                            return restTemplate.getForObject(PRODUCT_DETAIL_URL, ProductDetail.class, id);
                        } catch (HttpClientErrorException e) {
                      
                            System.err.println("Client error while fetching product detail for ID: " + id + " - "
                                    + e.getMessage());
                            return null;
                        } catch (Exception e) {
                     
                            System.err.println(
                                    "Error while fetching product detail for ID: " + id + " - " + e.getMessage());
                            return null;
                        }
                    }))
                    .collect(Collectors.toList());

         
            List<ProductDetail> similarProducts = futures.stream()
                    .map(CompletableFuture::join)
                    .filter(productDetail -> productDetail != null) 
                    .collect(Collectors.toList());

            return ResponseEntity.ok(similarProducts);
        } catch (Exception e) {
          
            System.err.println("An error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of());
        }
    }

    public ResponseEntity<List<ProductDetail>> fallbackGetSimilarProducts(String productId, Throwable throwable) {
        // Lógica de fallback en caso de fallo
        System.err.println("Fallback method called due to: " + throwable.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(List.of()); 
    }
}