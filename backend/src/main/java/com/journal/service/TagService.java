package com.journal.service;

import com.journal.dto.TagRequest;
import com.journal.dto.TagResponse;
import com.journal.entity.Tag;
import com.journal.entity.User;
import com.journal.exception.BusinessException;
import com.journal.repository.TagRepository;
import com.journal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 标签服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final UserRepository userRepository;

    /**
     * 创建标签
     */
    @Transactional
    public TagResponse create(String username, TagRequest request) {
        log.info("创建标签: user={}, name={}", username, request.getName());

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> BusinessException.notFound("用户不存在"));

        // 检查标签名是否已存在
        if (tagRepository.existsByNameAndUserId(request.getName(), user.getId())) {
            throw BusinessException.badRequest("标签名称已存在");
        }

        Tag tag = Tag.builder()
                .name(request.getName())
                .color(request.getColor() != null ? request.getColor() : "#10B981")
                .user(user)
                .build();

        tag = tagRepository.save(tag);
        log.info("标签创建成功: id={}", tag.getId());

        return toTagResponse(tag, 0L);
    }

    /**
     * 更新标签
     */
    @Transactional
    public TagResponse update(String username, Long tagId, TagRequest request) {
        log.info("更新标签: user={}, tagId={}", username, tagId);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> BusinessException.notFound("用户不存在"));

        Tag tag = tagRepository.findByIdAndUserId(tagId, user.getId())
                .orElseThrow(() -> BusinessException.notFound("标签不存在"));

        // 检查标签名是否已被其他标签使用
        if (!tag.getName().equals(request.getName()) &&
                tagRepository.existsByNameAndUserId(request.getName(), user.getId())) {
            throw BusinessException.badRequest("标签名称已存在");
        }

        tag.setName(request.getName());
        if (request.getColor() != null) {
            tag.setColor(request.getColor());
        }

        tag = tagRepository.save(tag);
        log.info("标签更新成功: id={}", tag.getId());

        return toTagResponse(tag, (long) tag.getJournals().size());
    }

    /**
     * 删除标签
     */
    @Transactional
    public void delete(String username, Long tagId) {
        log.info("删除标签: user={}, tagId={}", username, tagId);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> BusinessException.notFound("用户不存在"));

        Tag tag = tagRepository.findByIdAndUserId(tagId, user.getId())
                .orElseThrow(() -> BusinessException.notFound("标签不存在"));

        // 移除日记与标签的关联
        tag.getJournals().forEach(journal -> journal.getTags().remove(tag));

        tagRepository.delete(tag);
        log.info("标签删除成功: id={}", tagId);
    }

    /**
     * 获取标签列表
     */
    @Transactional(readOnly = true)
    public List<TagResponse> getList(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> BusinessException.notFound("用户不存在"));

        List<Object[]> results = tagRepository.findTagsWithJournalCount(user.getId());

        return results.stream()
                .map(row -> {
                    Tag tag = (Tag) row[0];
                    Long count = (Long) row[1];
                    return toTagResponse(tag, count);
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取标签详情
     */
    @Transactional(readOnly = true)
    public TagResponse getById(String username, Long tagId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> BusinessException.notFound("用户不存在"));

        Tag tag = tagRepository.findByIdAndUserId(tagId, user.getId())
                .orElseThrow(() -> BusinessException.notFound("标签不存在"));

        return toTagResponse(tag, (long) tag.getJournals().size());
    }

    private TagResponse toTagResponse(Tag tag, Long journalCount) {
        return TagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .color(tag.getColor())
                .journalCount(journalCount)
                .createdAt(tag.getCreatedAt())
                .build();
    }
}
