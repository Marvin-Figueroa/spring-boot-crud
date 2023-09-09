package dev.marvin.crud.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductDTO {
    @NotBlank
    private String name;
    @NotNull
    private Double price;

    public ProductDTO(@NotBlank String name, @NotNull Double price) {
        this.name = name;
        this.price = price;
    }
}
