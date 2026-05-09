package com.journal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * 日记请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JournalRequest {

    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200个字符")
    private String title;

    private String content;

    private String mood;

    private String weather;

    @Builder.Default
    private Boolean isPrivate = true;

    @NotNull(message = "日记日期不能为空")
    private LocalDate journalDate;

    private String coverImage;

    private Long categoryId;

    private List<Long> tagIds;
}
