package sk.janobono.example.api.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import sk.janobono.example.api.model.Product;
import sk.janobono.example.api.model.ProductData;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ProductApiService {

    private final AtomicLong ID = new AtomicLong(1);
    private final Map<Long, Product> PRODUCT_MAP = new ConcurrentHashMap<>();

    public Page<Product> getProducts(final String code, final String name, final Pageable pageable) {
        final List<Product> data = PRODUCT_MAP.values().stream()
                .filter(product -> {
                    if (StringUtils.isNotBlank(code)) {
                        return product.getCode().equals(code);
                    }
                    return true;
                })
                .filter(product -> {
                    if (StringUtils.isNotBlank(name)) {
                        return product.getName().equals(name);
                    }
                    return true;
                })
                .toList();
        return new PageImpl<>(data, Pageable.unpaged(), PRODUCT_MAP.size());
    }

    public Product getProduct(final Long id) {
        return getProductById(id);
    }

    public Product addProduct(final ProductData data) {
        final Long id = ID.getAndIncrement();
        final Product product = new Product();
        product.setId(id);
        setProductData(data, product);
        PRODUCT_MAP.put(id, product);
        return product;
    }

    public Product setProduct(final Long id, final ProductData data) {
        final Product product = getProductById(id);
        setProductData(data, product);
        return product;
    }

    public void deleteProduct(final Long id) {
        getProductById(id);
        PRODUCT_MAP.remove(id);
    }

    private Product getProductById(final Long id) {
        if (PRODUCT_MAP.containsKey(id)) {
            return PRODUCT_MAP.get(id);
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
    }

    private void setProductData(final ProductData data, final Product product) {
        product.setCode(data.getCode());
        product.setName(data.getName());
        product.setStatus(data.getStatus());
        product.setPrice(data.getPrice());
        product.setCurrency(data.getCurrency());
    }
}
