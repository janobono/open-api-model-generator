package sk.janobono.example.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import sk.janobono.example.api.model.Product;
import sk.janobono.example.api.model.ProductData;
import sk.janobono.example.api.service.ProductApiService;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/products")
public class ProductController {

    private final ProductApiService productApiService;

    @GetMapping
    public Page<Product> getProducts(
            @RequestParam(value = "code", required = false) final String code,
            @RequestParam(value = "name", required = false) final String name,
            final Pageable pageable
    ) {
        return productApiService.getProducts(code, name, pageable);
    }

    @GetMapping("/{id}")
    public Product getProduct(@PathVariable("id") final Long id) {
        return productApiService.getProduct(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Product addProduct(@Valid @RequestBody final ProductData data) {
        return productApiService.addProduct(data);
    }

    @PutMapping("/{id}")
    public Product setProduct(@PathVariable("id") final Long id, @Valid @RequestBody final ProductData data) {
        return productApiService.setProduct(id, data);
    }

    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable("id") final Long id) {
        productApiService.deleteProduct(id);
    }
}
