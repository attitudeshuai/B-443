package com.journal.repository;

import com.journal.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 分类数据访问接口
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByUserIdOrderBySortOrderAsc(Long userId);

    Optional<Category> findByIdAndUserId(Long id, Long userId);

    boolean existsByNameAndUserId(String name, Long userId);

    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.journals WHERE c.id = :id AND c.user.id = :userId")
    Optional<Category> findByIdAndUserIdWithJournals(@Param("id") Long id, @Param("userId") Long userId);

    @Query("SELECT c, COUNT(j) FROM Category c LEFT JOIN c.journals j WHERE c.user.id = :userId GROUP BY c ORDER BY c.sortOrder")
    List<Object[]> findCategoriesWithJournalCount(@Param("userId") Long userId);
}
