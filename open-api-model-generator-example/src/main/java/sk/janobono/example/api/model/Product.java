package sk.janobono.example.api.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = "id", callSuper = false)
public class Product extends ProductData {
    private Long id;
}
