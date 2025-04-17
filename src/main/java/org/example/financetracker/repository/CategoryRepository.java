package org.example.financetracker.repository;

import org.example.financetracker.entity.Category;
import org.example.financetracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByUser(User user);

    @Query("SELECT c FROM Category c WHERE c.isDefault = true OR c.user.id = :userId")
    List<Category> findByIsDefaultTrueOrUserId(Long userId);
}