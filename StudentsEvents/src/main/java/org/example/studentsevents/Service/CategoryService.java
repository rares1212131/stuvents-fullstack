package org.example.studentsevents.Service;

import lombok.RequiredArgsConstructor;
import org.example.studentsevents.DTORequest.CategoryRequest;
import org.example.studentsevents.DTOResponse.CategoryResponse;
import org.example.studentsevents.Repository.CategoryRepository;
import org.example.studentsevents.Repository.EventRepository;
import org.example.studentsevents.model.Category;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;
    private final EventRepository eventRepository;

    @Transactional(readOnly=true)
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Category not found with id: " + id));
        return modelMapper.map(category, CategoryResponse.class);
    }

    @Transactional (readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(category -> modelMapper.map(category, CategoryResponse.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoryResponse addCategory(CategoryRequest categoryRequest) {
        if(categoryRepository.existsByName(categoryRequest.getName())) {
            throw new IllegalStateException("Category  already exists");
        }
        Category category = modelMapper.map(categoryRequest, Category.class);
        Category savedCategory = categoryRepository.save(category);
        return modelMapper.map(savedCategory, CategoryResponse.class);
    }

    @Transactional
    public CategoryResponse updateCategory(CategoryRequest categoryRequest, long id) {
       Category existingCategory = categoryRepository.findById(id)
               .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
       existingCategory.setName(categoryRequest.getName());
       Category savedCategory = categoryRepository.save(existingCategory);
       return modelMapper.map(savedCategory, CategoryResponse.class);
    }

    @Transactional
    public void deleteCategory(long id) {
        if (eventRepository.existsByCategoryId(id)) {
            throw new IllegalStateException("Cannot delete this category because it is currently in use by one or more events.");
        }

        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Category not found with id: " + id);
        }

        categoryRepository.deleteById(id);
    }
}
