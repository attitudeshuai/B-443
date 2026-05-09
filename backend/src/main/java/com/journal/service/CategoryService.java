package com.journal.service;

import com.journal.dto.CategoryRequest;
import com.journal.dto.CategoryResponse;
import com.journal.entity.Category;
import com.journal.entity.User;
import com.journal.exception.BusinessException;
import com.journal.repository.CategoryRepository;
import com.journal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 分类服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    /**
     * 创建分类
     */
    @Transactional
    public CategoryResponse create(String username, CategoryRequest request) {
        log.info("创建分类: user={}, name={}", username, request.getName());

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> BusinessException.notFound("用户不存在"));

        // 检查分类名是否已存在
        if (categoryRepository.existsByNameAndUserId(request.getName(), user.getId())) {
            throw BusinessException.badRequest("分类名称已存在");
        }

        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .color(request.getColor() != null ? request.getColor() : "#3B82F6")
                .icon(request.getIcon())
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .user(user)
                .build();

        category = categoryRepository.save(category);
        log.info("分类创建成功: id={}", category.getId());

        return toCategoryResponse(category, 0L);
    }

    /**
     * 更新分类
     */
    @Transactional
    public CategoryResponse update(String username, Long categoryId, CategoryRequest request) {
        log.info("更新分类: user={}, categoryId={}", username, categoryId);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> BusinessException.notFound("用户不存在"));

        Category category = categoryRepository.findByIdAndUserId(categoryId, user.getId())
                .orElseThrow(() -> BusinessException.notFound("分类不存在"));

        // 检查分类名是否已被其他分类使用
        if (!category.getName().equals(request.getName()) &&
                categoryRepository.existsByNameAndUserId(request.getName(), user.getId())) {
            throw BusinessException.badRequest("分类名称已存在");
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        if (request.getColor() != null) {
            category.setColor(request.getColor());
        }
        category.setIcon(request.getIcon());
        if (request.getSortOrder() != null) {
            category.setSortOrder(request.getSortOrder());
        }

        category = categoryRepository.save(category);
        log.info("分类更新成功: id={}", category.getId());

        return toCategoryResponse(category, (long) category.getJournals().size());
    }

    /**
     * 删除分类
     */
    @Transactional
    public void delete(String username, Long categoryId) {
        log.info("删除分类: user={}, categoryId={}", username, categoryId);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> BusinessException.notFound("用户不存在"));

        Category category = categoryRepository.findByIdAndUserId(categoryId, user.getId())
                .orElseThrow(() -> BusinessException.notFound("分类不存在"));

        // 将该分类下的日记的分类设为null
        category.getJournals().forEach(journal -> journal.setCategory(null));

        categoryRepository.delete(category);
        log.info("分类删除成功: id={}", categoryId);
    }

    /**
     * 获取分类列表
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getList(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> BusinessException.notFound("用户不存在"));

        List<Object[]> results = categoryRepository.findCategoriesWithJournalCount(user.getId());

        return results.stream()
                .map(row -> {
                    Category category = (Category) row[0];
                    Long count = (Long) row[1];
                    return toCategoryResponse(category, count);
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取分类详情
     */
    @Transactional(readOnly = true)
    public CategoryResponse getById(String username, Long categoryId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> BusinessException.notFound("用户不存在"));

        Category category = categoryRepository.findByIdAndUserId(categoryId, user.getId())
                .orElseThrow(() -> BusinessException.notFound("分类不存在"));

        return toCategoryResponse(category, (long) category.getJournals().size());
    }

    private CategoryResponse toCategoryResponse(Category category, Long journalCount) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .color(category.getColor())
                .icon(category.getIcon())
                .sortOrder(category.getSortOrder())
                .journalCount(journalCount)
                .createdAt(category.getCreatedAt())
                .build();
    }
}
