package com.journal.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 日记实体类
 */
@Entity
@Table(name = "journals")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Journal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200个字符")
    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "LONGTEXT")
    private String content;

    /**
     * 心情: happy, sad, neutral, excited, anxious, calm, angry, tired
     */
    @Column(length = 50)
    private String mood;

    /**
     * 天气: sunny, cloudy, rainy, snowy, windy, foggy
     */
    @Column(length = 50)
    private String weather;

    /**
     * 是否私密
     */
    @Builder.Default
    @Column(name = "is_private", nullable = false)
    private Boolean isPrivate = true;

    /**
     * 日记日期
     */
    @Column(name = "journal_date", nullable = false)
    private LocalDate journalDate;

    /**
     * 封面图片
     */
    @Column(length = 500)
    private String coverImage;

    /**
     * 字数统计
     */
    @Builder.Default
    @Column(name = "word_count")
    private Integer wordCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "journal_tags",
            joinColumns = @JoinColumn(name = "journal_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 计算字数
     */
    @PrePersist
    @PreUpdate
    public void calculateWordCount() {
        if (content != null) {
            // 移除HTML标签后计算字数
            String plainText = content.replaceAll("<[^>]*>", "").replaceAll("\\s+", "");
            this.wordCount = plainText.length();
        }
    }
}
