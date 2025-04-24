package org.example.financetracker.repository;

import org.example.financetracker.entity.Goal;
import org.example.financetracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface GoalRepository extends JpaRepository<Goal, Long> {

    List<Goal> findByUserAndMonth(User user, LocalDate month);

    List<Goal> findByUserId(Long userId);

    List<Goal> findByUserIdAndMonthAndType(Long userId, LocalDate month, String type);

    List<Goal> findByUserIdAndMonthAndCategoryId(Long userId, LocalDate month, Long categoryId);

    boolean existsByUserIdAndCategoryIdAndMonthAndType(Long userId, Long categoryId, LocalDate month, String type);

    List<Goal> findByUserIdAndMonthLessThanEqualAndMonthGreaterThanEqual(
            Long userId, LocalDate endMonth, LocalDate startMonth);

    List<Goal> findByUserIdAndMonthBetweenOrderByMonthAsc(Long userId, LocalDate start, LocalDate end);

    List<Goal> findByUserIdOrderByAmountDesc(Long userId);
}