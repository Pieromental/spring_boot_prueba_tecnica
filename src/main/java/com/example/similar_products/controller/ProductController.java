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
        // Obtener los IDs de productos similares
        String[] similarIds = restTemplate.getForObject(SIMILAR_IDS_URL, String[].class, productId);

        // Obtener los detalles de cada producto similar en paralelo
        List<CompletableFuture<ProductDetail>> futures = Arrays.stream(similarIds)
                .map(id -> CompletableFuture.supplyAsync(() -> {
                    try {
                        return restTemplate.getForObject(PRODUCT_DETAIL_URL, ProductDetail.class, id);
                    } catch (Exception e) {
                        // Manejar excepciones para productos no encontrados o errores del servidor
                        System.err.println("Error fetching product detail for ID: " + id + " - " + e.getMessage());
                        return null;
                    }
                }))
                .collect(Collectors.toList());

        // Esperar a que todas las llamadas se completen y recolectar resultados
        return futures.stream()
                .map(CompletableFuture::join)
                .filter(productDetail -> productDetail != null) // Filtrar productos nulos
                .collect(Collectors.toList());
    }

    public List<ProductDetail> fallbackGetSimilarProducts(String productId, Throwable throwable) {
        // Lógica de fallback en caso de fallo
        System.out.println("Fallback method called due to: " + throwable.getMessage());
        return List.of();
    }
}