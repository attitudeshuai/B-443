package com.journal.repository;

import com.journal.entity.Journal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 日记数据访问接口
 */
@Repository
public interface JournalRepository extends JpaRepository<Journal, Long> {

    Page<Journal> findByUserIdOrderByJournalDateDesc(Long userId, Pageable pageable);

    Page<Journal> findByUserIdAndCategoryIdOrderByJournalDateDesc(Long userId, Long categoryId, Pageable pageable);

    List<Journal> findByUserIdAndJournalDateBetweenOrderByJournalDateDesc(
            Long userId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT j FROM Journal j WHERE j.user.id = :userId AND " +
            "(LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(j.content) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Journal> searchByKeyword(@Param("userId") Long userId, @Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT j FROM Journal j JOIN j.tags t WHERE j.user.id = :userId AND t.id = :tagId ORDER BY j.journalDate DESC")
    Page<Journal> findByUserIdAndTagId(@Param("userId") Long userId, @Param("tagId") Long tagId, Pageable pageable);

    @Query("SELECT COUNT(j) FROM Journal j WHERE j.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    @Query("SELECT SUM(j.wordCount) FROM Journal j WHERE j.user.id = :userId")
    Long getTotalWordCountByUserId(@Param("userId") Long userId);

    @Query("SELECT j.mood, COUNT(j) FROM Journal j WHERE j.user.id = :userId AND j.mood IS NOT NULL GROUP BY j.mood")
    List<Object[]> getMoodStatsByUserId(@Param("userId") Long userId);

    @Query("SELECT j.journalDate, COUNT(j) FROM Journal j WHERE j.user.id = :userId " +
            "AND j.journalDate BETWEEN :startDate AND :endDate GROUP BY j.journalDate")
    List<Object[]> getJournalCountByDateRange(@Param("userId") Long userId,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);

    List<Journal> findTop5ByUserIdOrderByCreatedAtDesc(Long userId);

    boolean existsByUserIdAndJournalDate(Long userId, LocalDate journalDate);
}
