package com.journal.service;

import com.journal.dto.DashboardStats;
import com.journal.dto.JournalResponse;
import com.journal.entity.Journal;
import com.journal.entity.User;
import com.journal.exception.BusinessException;
import com.journal.repository.CategoryRepository;
import com.journal.repository.JournalRepository;
import com.journal.repository.TagRepository;
import com.journal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 仪表板服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final JournalRepository journalRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;

    /**
     * 获取仪表板统计数据
     */
    @Transactional(readOnly = true)
    public DashboardStats getStats(String username) {
        log.info("获取仪表板统计: user={}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> BusinessException.notFound("用户不存在"));

        Long userId = user.getId();

        // 总日记数
        long totalJournals = journalRepository.countByUserId(userId);

        // 总字数
        Long totalWords = journalRepository.getTotalWordCountByUserId(userId);
        if (totalWords == null) totalWords = 0L;

        // 分类数
        long totalCategories = categoryRepository.findByUserIdOrderBySortOrderAsc(userId).size();

        // 标签数
        long totalTags = tagRepository.findByUserIdOrderByNameAsc(userId).size();

        // 心情统计
        Map<String, Long> moodStats = new HashMap<>();
        List<Object[]> moodResults = journalRepository.getMoodStatsByUserId(userId);
        for (Object[] row : moodResults) {
            String mood = (String) row[0];
            Long count = (Long) row[1];
            moodStats.put(mood, count);
        }

        // 最近日记
        List<Journal> recentJournals = journalRepository.findTop5ByUserIdOrderByCreatedAtDesc(userId);
        List<JournalResponse> recentJournalDTOs = recentJournals.stream()
                .map(this::toJournalResponse)
                .collect(Collectors.toList());

        // 过去30天的日记数统计
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);
        List<Object[]> dailyResults = journalRepository.getJournalCountByDateRange(userId, startDate, endDate);

        List<DashboardStats.DailyCount> dailyCounts = dailyResults.stream()
                .map(row -> DashboardStats.DailyCount.builder()
                        .date(row[0].toString())
                        .count((Long) row[1])
                        .build())
                .collect(Collectors.toList());

        // 计算连续写日记天数
        int currentStreak = calculateCurrentStreak(userId);
        int longestStreak = calculateLongestStreak(userId);

        return DashboardStats.builder()
                .totalJournals(totalJournals)
                .totalWords(totalWords)
                .totalCategories(totalCategories)
                .totalTags(totalTags)
                .moodStats(moodStats)
                .recentJournals(recentJournalDTOs)
                .dailyCounts(dailyCounts)
                .currentStreak(currentStreak)
                .longestStreak(longestStreak)
                .build();
    }

    private int calculateCurrentStreak(Long userId) {
        LocalDate today = LocalDate.now();
        int streak = 0;

        for (int i = 0; i < 365; i++) {
            LocalDate date = today.minusDays(i);
            if (journalRepository.existsByUserIdAndJournalDate(userId, date)) {
                streak++;
            } else if (i > 0) {
                // 如果今天没写但之前连续写了，还是算当前连续
                break;
            } else {
                // 今天还没写，检查昨天
                continue;
            }
        }

        return streak;
    }

    private int calculateLongestStreak(Long userId) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusYears(1);

        List<Object[]> results = journalRepository.getJournalCountByDateRange(userId, startDate, endDate);
        Set<LocalDate> journalDates = results.stream()
                .map(row -> (LocalDate) row[0])
                .collect(Collectors.toSet());

        int longestStreak = 0;
        int currentStreak = 0;
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            if (journalDates.contains(currentDate)) {
                currentStreak++;
                longestStreak = Math.max(longestStreak, currentStreak);
            } else {
                currentStreak = 0;
            }
            currentDate = currentDate.plusDays(1);
        }

        return longestStreak;
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
