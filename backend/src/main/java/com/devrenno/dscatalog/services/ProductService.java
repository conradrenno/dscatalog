package com.devrenno.dscatalog.services;

import com.devrenno.dscatalog.dto.CategoryDTO;
import com.devrenno.dscatalog.dto.ProductDTO;
import com.devrenno.dscatalog.entities.Category;
import com.devrenno.dscatalog.entities.Product;
import com.devrenno.dscatalog.repositories.CategoryRepository;
import com.devrenno.dscatalog.repositories.ProductRepository;
import com.devrenno.dscatalog.services.exceptions.DatabaseException;
import com.devrenno.dscatalog.services.exceptions.ResourceNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository repository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public Page<ProductDTO> findAll(Pageable pageable) {
        Page<Product> result = repository.findAll(pageable);
        Page<ProductDTO> page = result.map(x -> new ProductDTO(x));
        return page;
    }

    @Transactional(readOnly = true)
    public ProductDTO findById(Long id) {
        Product product = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Resource not found"));
        return new ProductDTO(product, product.getCategories());
    }

    @Transactional
    public ProductDTO insert(ProductDTO dto) {
        Product product = new Product();
        dtoToProduct(product, dto);
        product = repository.save(product);
        return new ProductDTO(product);
    }

    @Transactional
    public ProductDTO update(Long id, ProductDTO dto) {
        try {
            Product product = repository.getReferenceById(id);
            dtoToProduct(product, dto);
            product = repository.save(product);
            return new ProductDTO(product);
        } catch (EntityNotFoundException e){
            throw new ResourceNotFoundException("Resource not found");
        }
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)){
            throw new ResourceNotFoundException("Resource not found");
        }
        try {
            repository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Integrity violation");
        }
    }

    private void dtoToProduct(Product product, ProductDTO dto) {
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setImgUrl(dto.getImgUrl());

        product.getCategories().clear();
        for (CategoryDTO catDto : dto.getCategories()) {
            Category category = categoryRepository.getReferenceById(catDto.getId());
            product.getCategories().add(category);
        }

    }

}
