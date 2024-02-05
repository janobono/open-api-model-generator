package sk.janobono.example.api.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductData {
    @NotEmpty
    private String code;

    @NotEmpty
    private String name;

    @NotNull
    private ProductStatus status;

    @NotNull
    private BigDecimal price;

    @NotNull
    private Currency currency;
}
