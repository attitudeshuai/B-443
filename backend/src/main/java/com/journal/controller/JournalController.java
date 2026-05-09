package com.journal.controller;

import com.journal.dto.ApiResponse;
import com.journal.dto.JournalRequest;
import com.journal.dto.JournalResponse;
import com.journal.service.JournalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 日记控制器
 */
@RestController
@RequestMapping("/api/journals")
@RequiredArgsConstructor
@Tag(name = "日记管理", description = "日记的增删改查接口")
public class JournalController {

    private final JournalService journalService;

    @PostMapping
    @Operation(summary = "创建日记", description = "创建一篇新日记")
    public ResponseEntity<ApiResponse<JournalResponse>> create(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody JournalRequest request) {
        JournalResponse response = journalService.create(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success("创建成功", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新日记", description = "更新指定日记的内容")
    public ResponseEntity<ApiResponse<JournalResponse>> update(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody JournalRequest request) {
        JournalResponse response = journalService.update(userDetails.getUsername(), id, request);
        return ResponseEntity.ok(ApiResponse.success("更新成功", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除日记", description = "删除指定日记")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        journalService.delete(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取日记详情", description = "获取指定日记的详细内容")
    public ResponseEntity<ApiResponse<JournalResponse>> getById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        JournalResponse response = journalService.getById(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "获取日记列表", description = "分页获取日记列表，可按分类筛选")
    public ResponseEntity<ApiResponse<Page<JournalResponse>>> getList(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<JournalResponse> response = journalService.getList(
                userDetails.getUsername(), categoryId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/search")
    @Operation(summary = "搜索日记", description = "根据关键词搜索日记")
    public ResponseEntity<ApiResponse<Page<JournalResponse>>> search(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<JournalResponse> response = journalService.search(
                userDetails.getUsername(), keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/by-tag/{tagId}")
    @Operation(summary = "按标签获取日记", description = "获取指定标签下的所有日记")
    public ResponseEntity<ApiResponse<Page<JournalResponse>>> getByTag(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long tagId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<JournalResponse> response = journalService.getByTag(
                userDetails.getUsername(), tagId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/by-date")
    @Operation(summary = "按日期范围获取日记", description = "获取指定日期范围内的所有日记")
    public ResponseEntity<ApiResponse<List<JournalResponse>>> getByDateRange(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<JournalResponse> response = journalService.getByDateRange(
                userDetails.getUsername(), startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/recent")
    @Operation(summary = "获取最近日记", description = "获取最近的5篇日记")
    public ResponseEntity<ApiResponse<List<JournalResponse>>> getRecent(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<JournalResponse> response = journalService.getRecent(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
