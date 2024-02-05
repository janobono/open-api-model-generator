package sk.janobono.example.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import sk.janobono.example.client.model.full.ProductClientDto;
import sk.janobono.example.client.model.full.ProductCurrency;
import sk.janobono.example.client.model.full.ProductDataClientDto;
import sk.janobono.example.client.model.full.ProductStatus;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class ProductControllerTest {

    @Value("${local.server.port}")
    public int serverPort;

    @Autowired
    public ObjectMapper objectMapper;

    @Autowired
    public RestTemplate restTemplate;

    @Test
    public void testProductController() {
        final ProductClientDto addedProduct = addProduct(ProductDataClientDto.builder()
                .status(ProductStatus.ON_STOCK)
                .code("code01")
                .name("name01")
                .price(BigDecimal.ONE)
                .currency(ProductCurrency.VALUE_0)
                .build());
        assertThat(addedProduct).isNotNull();
        assertThat(addedProduct.id()).isEqualTo(1L);
        assertThat(addedProduct.status()).isEqualTo(ProductStatus.ON_STOCK);
        assertThat(addedProduct.code()).isEqualTo("code01");
        assertThat(addedProduct.name()).isEqualTo("name01");
        assertThat(addedProduct.price()).isEqualTo(BigDecimal.ONE);
        assertThat(addedProduct.currency()).isEqualTo(ProductCurrency.VALUE_0);

        final ProductClientDto changedProduct = setProduct(1L, ProductDataClientDto.builder()
                .status(ProductStatus.TO_ORDER)
                .code("code01")
                .name("name01")
                .price(BigDecimal.ONE)
                .currency(ProductCurrency.VALUE_0)
                .build());
        assertThat(changedProduct).isNotNull();
        assertThat(changedProduct.id()).isEqualTo(1L);
        assertThat(changedProduct.status()).isEqualTo(ProductStatus.TO_ORDER);
    }

    private ProductClientDto addProduct(final ProductDataClientDto data) {
        final ResponseEntity<ProductClientDto> response = restTemplate.exchange(
                getURI("/products"), HttpMethod.POST, new HttpEntity<>(data), ProductClientDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        return response.getBody();
    }

    private ProductClientDto setProduct(final Long id, final ProductDataClientDto data) {
        final ResponseEntity<ProductClientDto> response = restTemplate.exchange(
                getURI("/products/{id}", Map.of("id", id.toString())),
                HttpMethod.PUT,
                new HttpEntity<>(data),
                ProductClientDto.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        return response.getBody();
    }

    private URI getURI(final String path) {
        return UriComponentsBuilder.fromHttpUrl("http://localhost:" + serverPort)
                .path("/api" + path).build().toUri();
    }

    private URI getURI(final String path, final Map<String, String> pathVars) {
        return UriComponentsBuilder.fromHttpUrl("http://localhost:" + serverPort)
                .path("/api" + path).buildAndExpand(pathVars).toUri();
    }
}
