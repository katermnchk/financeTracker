package org.example.financetracker.repository;

import org.example.financetracker.entity.Category;
import org.example.financetracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
   List<Category> findByUser(User user);
}
