package org.example.financetracker.service;

import org.example.financetracker.entity.Category;
import org.example.financetracker.entity.User;
import org.example.financetracker.repository.CategoryRepository;
import org.example.financetracker.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public CategoryService(CategoryRepository categoryRepository, UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    public List<Category> getUserCategories(User user) {
        return categoryRepository.findByUser(user);
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category createCategory(String name, String description, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        category.setUser(user);
        return categoryRepository.save(category);
    }

    public Category updateCategory(Long id, String name, String description, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        Category category = categoryRepository.findById(id)
                .filter(c -> c.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Категория не найдена"));
        category.setName(name);
        category.setDescription(description);
        return categoryRepository.save(category);
    }

    public void deleteCategory(Long id, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        Category category = categoryRepository.findById(id)
                .filter(c -> c.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Категория не найдена"));
        categoryRepository.delete(category);
    }

    public Category findByIdAndUser(Long categoryId, User user) {
        return categoryRepository.findById(categoryId)
                .filter(category -> category.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new SecurityException("Категория не найдена или доступ запрещён"));
    }

    public List<Category> findAllByUser(User user) {
        return categoryRepository.findByUser(user);
    }


}