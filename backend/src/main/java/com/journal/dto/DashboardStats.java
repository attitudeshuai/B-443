package com.journal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 仪表板统计数据DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStats {

    private Long totalJournals;
    private Long totalWords;
    private Long totalCategories;
    private Long totalTags;
    private Integer currentStreak;
    private Integer longestStreak;
    private Map<String, Long> moodStats;
    private List<JournalResponse> recentJournals;
    private List<DailyCount> dailyCounts;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyCount {
        private String date;
        private Long count;
    }
}
