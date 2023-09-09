package dev.marvin.crud.controller;

import dev.marvin.crud.dto.Message;
import dev.marvin.crud.dto.ProductDTO;
import dev.marvin.crud.entity.Product;
import dev.marvin.crud.exception.ResourceNotFoundException;
import dev.marvin.crud.service.ProductService;

import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
@CrossOrigin(origins = "*")
public class ProductController {
    @Autowired
    ProductService productService;

    @GetMapping("")
    public ResponseEntity<List<Product>> findAll() {
        List<Product> products = productService.getProducts();
        return new ResponseEntity<List<Product>>(products, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable("id") int id) {
        var product = productService.getProduct(id)
                .orElseThrow(() -> new ResourceNotFoundException("The product with the id [ " + id + " ] does not exist"));;;
        return new ResponseEntity<>(product, HttpStatus.OK);
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<Product> getProductByName(@PathVariable("name") String name) {
        var product = productService.getProductByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("The product with the name [ " + name + " ] does not exist"));;
        return new ResponseEntity<>(product, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("")
    public ResponseEntity<Message> createProduct(@Valid @RequestBody ProductDTO productDTO, BindingResult  bindingResult) {
        if(bindingResult.hasErrors()) {
            return new ResponseEntity<>(new Message("The product name and price are required"), HttpStatus.BAD_REQUEST);
        }
        if(productDTO.getPrice() <= 0) {
            return new ResponseEntity<>(new Message("The product price must be greater than 0"), HttpStatus.BAD_REQUEST);
        }
        if(productService.existsByName(productDTO.getName())) {
            return new ResponseEntity<>(new Message("The product with the name [ " + productDTO.getName() + " ] already exists"), HttpStatus.BAD_REQUEST);
        }

        Product product = new Product(productDTO.getName(), productDTO.getPrice());
        productService.save(product);

        return new ResponseEntity<>(new Message("The product was created successfully"), HttpStatus.CREATED);

    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Message> updateProduct(@PathVariable("id") int id, @Valid @RequestBody ProductDTO productDTO, BindingResult bindingResult) {
        if(bindingResult.hasErrors()) {
            return new ResponseEntity<>(new Message("The product name and price are required"), HttpStatus.BAD_REQUEST);
        }

        if(!productService.existsById(id)) {
            return new ResponseEntity<>(new Message("The product with the id [ " + id + " ] does not exist"), HttpStatus.NOT_FOUND);
        }

        if(StringUtils.isBlank(productDTO.getName())) {
            return new ResponseEntity<>(new Message("The product name is required"), HttpStatus.BAD_REQUEST);
        }

        if(productDTO.getPrice() <= 0) {
            return new ResponseEntity<>(new Message("The product price must be greater than 0"), HttpStatus.BAD_REQUEST);
        }

        if(productService.existsByName(productDTO.getName()) && productService.getProductByName(productDTO.getName()).get().getId() == id) {
            return new ResponseEntity<>(new Message("The product with the name [ " + productDTO.getName() + " ] already exists"), HttpStatus.BAD_REQUEST);
        }

        Product product = productService.getProduct(id).get();
        product.setName(productDTO.getName());
        product.setPrice(productDTO.getPrice());
        productService.save(product);

        return new ResponseEntity<>(new Message("The product was updated successfully"), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Message> deleteProduct(@PathVariable("id") int id) {
        if(!productService.existsById(id)) {
            return new ResponseEntity<>(new Message("The product with the id [ " + id + " ] does not exist"), HttpStatus.NOT_FOUND);
        }
        productService.delete(id);
        return new ResponseEntity<>(new Message("The product was deleted successfully"), HttpStatus.OK);
    }
}
