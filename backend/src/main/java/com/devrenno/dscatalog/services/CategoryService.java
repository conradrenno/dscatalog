package com.devrenno.dscatalog.services;

import com.devrenno.dscatalog.dto.CategoryDTO;
import com.devrenno.dscatalog.entities.Category;
import com.devrenno.dscatalog.repositories.CategoryRepository;
import com.devrenno.dscatalog.services.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository repository;

    @Transactional(readOnly = true)
    public List<CategoryDTO> findAll() {
        List<Category> result = repository.findAll();
        List<CategoryDTO> list = result.stream().map(x -> new CategoryDTO(x)).toList();
        return list;
    }

    @Transactional(readOnly = true)
    public CategoryDTO findById(Long id) {
        Category category = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Resource not found"));
        return new CategoryDTO(category);
    }
}
