package com.journal.service;

import com.journal.dto.JournalRequest;
import com.journal.dto.JournalResponse;
import com.journal.entity.Category;
import com.journal.entity.Journal;
import com.journal.entity.Tag;
import com.journal.entity.User;
import com.journal.exception.BusinessException;
import com.journal.repository.CategoryRepository;
import com.journal.repository.JournalRepository;
import com.journal.repository.TagRepository;
import com.journal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 日记服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JournalService {

    private final JournalRepository journalRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;

    /**
     * 创建日记
     */
    @Transactional
    public JournalResponse create(String username, JournalRequest request) {
        log.info("创建日记: user={}, title={}", username, request.getTitle());

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> BusinessException.notFound("用户不存在"));

        Journal journal = Journal.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .mood(request.getMood())
                .weather(request.getWeather())
                .isPrivate(request.getIsPrivate() != null ? request.getIsPrivate() : true)
                .journalDate(request.getJournalDate())
                .coverImage(request.getCoverImage())
                .user(user)
                .build();

        // 设置分类
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findByIdAndUserId(request.getCategoryId(), user.getId())
                    .orElseThrow(() -> BusinessException.notFound("分类不存在"));
            journal.setCategory(category);
        }

        // 设置标签
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            List<Tag> tags = tagRepository.findByIdInAndUserId(request.getTagIds(), user.getId());
            journal.setTags(new HashSet<>(tags));
        }

        journal = journalRepository.save(journal);
        log.info("日记创建成功: id={}", journal.getId());

        return toJournalResponse(journal);
    }

    /**
     * 更新日记
     */
    @Transactional
    public JournalResponse update(String username, Long journalId, JournalRequest request) {
        log.info("更新日记: user={}, journalId={}", username, journalId);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> BusinessException.notFound("用户不存在"));

        Journal journal = journalRepository.findById(journalId)
                .orElseThrow(() -> BusinessException.notFound("日记不存在"));

        // 检查权限
        if (!journal.getUser().getId().equals(user.getId())) {
            throw BusinessException.forbidden("没有权限修改此日记");
        }

        // 更新字段
        journal.setTitle(request.getTitle());
        journal.setContent(request.getContent());
        journal.setMood(request.getMood());
        journal.setWeather(request.getWeather());
        journal.setIsPrivate(request.getIsPrivate() != null ? request.getIsPrivate() : true);
        journal.setJournalDate(request.getJournalDate());
        journal.setCoverImage(request.getCoverImage());

        // 更新分类
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findByIdAndUserId(request.getCategoryId(), user.getId())
                    .orElseThrow(() -> BusinessException.notFound("分类不存在"));
            journal.setCategory(category);
        } else {
            journal.setCategory(null);
        }

        // 更新标签
        journal.getTags().clear();
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            List<Tag> tags = tagRepository.findByIdInAndUserId(request.getTagIds(), user.getId());
            journal.setTags(new HashSet<>(tags));
        }

        journal = journalRepository.save(journal);
        log.info("日记更新成功: id={}", journal.getId());

        return toJournalResponse(journal);
    }

    /**
     * 删除日记
     */
    @Transactional
    public void delete(String username, Long journalId) {
        log.info("删除日记: user={}, journalId={}", username, journalId);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> BusinessException.notFound("用户不存在"));

        Journal journal = journalRepository.findById(journalId)
                .orElseThrow(() -> BusinessException.notFound("日记不存在"));

        // 检查权限
        if (!journal.getUser().getId().equals(user.getId())) {
            throw BusinessException.forbidden("没有权限删除此日记");
        }

        journalRepository.delete(journal);
        log.info("日记删除成功: id={}", journalId);
    }

    /**
     * 获取日记详情
     */
    @Transactional(readOnly = true)
    public JournalResponse getById(String username, Long journalId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> BusinessException.notFound("用户不存在"));

        Journal journal = journalRepository.findById(journalId)
                .orElseThrow(() -> BusinessException.notFound("日记不存在"));

        // 检查权限
        if (!journal.getUser().getId().equals(user.getId())) {
            throw BusinessException.forbidden("没有权限查看此日记");
        }

        return toJournalResponse(journal);
    }

    /**
     * 获取日记列表（分页）
     */
    @Transactional(readOnly = true)
    public Page<JournalResponse> getList(String username, Long categoryId, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> BusinessException.notFound("用户不存在"));

        Page<Journal> journals;
        if (categoryId != null) {
            journals = journalRepository.findByUserIdAndCategoryIdOrderByJournalDateDesc(
                    user.getId(), categoryId, pageable);
        } else {
            journals = journalRepository.findByUserIdOrderByJournalDateDesc(user.getId(), pageable);
        }

        return journals.map(this::toJournalResponse);
    }

    /**
     * 搜索日记
     */
    @Transactional(readOnly = true)
    public Page<JournalResponse> search(String username, String keyword, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> BusinessException.notFound("用户不存在"));

        Page<Journal> journals = journalRepository.searchByKeyword(user.getId(), keyword, pageable);
        return journals.map(this::toJournalResponse);
    }

    /**
     * 按标签获取日记
     */
    @Transactional(readOnly = true)
    public Page<JournalResponse> getByTag(String username, Long tagId, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> BusinessException.notFound("用户不存在"));

        Page<Journal> journals = journalRepository.findByUserIdAndTagId(user.getId(), tagId, pageable);
        return journals.map(this::toJournalResponse);
    }

    /**
     * 按日期范围获取日记
     */
    @Transactional(readOnly = true)
    public List<JournalResponse> getByDateRange(String username, LocalDate startDate, LocalDate endDate) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> BusinessException.notFound("用户不存在"));

        List<Journal> journals = journalRepository.findByUserIdAndJournalDateBetweenOrderByJournalDateDesc(
                user.getId(), startDate, endDate);

        return journals.stream()
                .map(this::toJournalResponse)
                .collect(Collectors.toList());
    }

    /**
     * 获取最近的日记
     */
    @Transactional(readOnly = true)
    public List<JournalResponse> getRecent(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> BusinessException.notFound("用户不存在"));

        List<Journal> journals = journalRepository.findTop5ByUserIdOrderByCreatedAtDesc(user.getId());

        return journals.stream()
                .map(this::toJournalResponse)
                .collect(Collectors.toList());
    }

    private JournalResponse toJournalResponse(Journal journal) {
        JournalResponse.CategoryDTO categoryDTO = null;
        if (journal.getCategory() != null) {
            categoryDTO = JournalResponse.CategoryDTO.builder()
                    .id(journal.getCategory().getId())
                    .name(journal.getCategory().getName())
                    .color(journal.getCategory().getColor())
                    .icon(journal.getCategory().getIcon())
                    .build();
        }

        List<JournalResponse.TagDTO> tagDTOs = journal.getTags().stream()
                .map(tag -> JournalResponse.TagDTO.builder()
                        .id(tag.getId())
                        .name(tag.getName())
                        .color(tag.getColor())
                        .build())
                .collect(Collectors.toList());

        return JournalResponse.builder()
                .id(journal.getId())
                .title(journal.getTitle())
                .content(journal.getContent())
                .mood(journal.getMood())
                .weather(journal.getWeather())
                .isPrivate(journal.getIsPrivate())
                .journalDate(journal.getJournalDate())
                .coverImage(journal.getCoverImage())
                .wordCount(journal.getWordCount())
                .category(categoryDTO)
                .tags(tagDTOs)
                .createdAt(journal.getCreatedAt())
                .updatedAt(journal.getUpdatedAt())
                .build();
    }
}
