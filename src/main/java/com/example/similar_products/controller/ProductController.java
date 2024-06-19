package com.example.similar_products.controller;

import com.example.similar_products.model.ProductDetail;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
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
    public List<ProductDetail> getSimilarProducts(@PathVariable String productId) {
        String[] similarIds = restTemplate.getForObject(SIMILAR_IDS_URL, String[].class, productId);

        List<CompletableFuture<ProductDetail>> futures = Arrays.stream(similarIds)
                .map(id -> CompletableFuture.supplyAsync(() -> restTemplate.getForObject(PRODUCT_DETAIL_URL, ProductDetail.class, id)))
                .collect(Collectors.toList());

        return futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    public List<ProductDetail> fallbackGetSimilarProducts(String productId, Throwable throwable) {
        // Lógica de fallback en caso de fallo
        return List.of();
    }
}