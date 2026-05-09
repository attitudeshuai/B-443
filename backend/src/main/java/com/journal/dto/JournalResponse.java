package com.journal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 日记响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JournalResponse {

    private Long id;
    private String title;
    private String content;
    private String mood;
    private String weather;
    private Boolean isPrivate;
    private LocalDate journalDate;
    private String coverImage;
    private Integer wordCount;
    private CategoryDTO category;
    private List<TagDTO> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryDTO {
        private Long id;
        private String name;
        private String color;
        private String icon;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TagDTO {
        private Long id;
        private String name;
        private String color;
    }
}
