package com.journal.repository;

import com.journal.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 标签数据访问接口
 */
@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    List<Tag> findByUserIdOrderByNameAsc(Long userId);

    Optional<Tag> findByIdAndUserId(Long id, Long userId);

    boolean existsByNameAndUserId(String name, Long userId);

    @Query("SELECT t, COUNT(j) FROM Tag t LEFT JOIN t.journals j WHERE t.user.id = :userId GROUP BY t ORDER BY COUNT(j) DESC")
    List<Object[]> findTagsWithJournalCount(@Param("userId") Long userId);

    List<Tag> findByIdInAndUserId(List<Long> ids, Long userId);
}
