package com.journal.controller;

import com.journal.dto.ApiResponse;
import com.journal.dto.TagRequest;
import com.journal.dto.TagResponse;
import com.journal.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 标签控制器
 */
@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
@Tag(name = "标签管理", description = "日记标签的增删改查接口")
public class TagController {

    private final TagService tagService;

    @PostMapping
    @Operation(summary = "创建标签", description = "创建新的日记标签")
    public ResponseEntity<ApiResponse<TagResponse>> create(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody TagRequest request) {
        TagResponse response = tagService.create(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success("创建成功", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新标签", description = "更新指定标签的信息")
    public ResponseEntity<ApiResponse<TagResponse>> update(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody TagRequest request) {
        TagResponse response = tagService.update(userDetails.getUsername(), id, request);
        return ResponseEntity.ok(ApiResponse.success("更新成功", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除标签", description = "删除指定标签")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        tagService.delete(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }

    @GetMapping
    @Operation(summary = "获取标签列表", description = "获取当前用户的所有标签")
    public ResponseEntity<ApiResponse<List<TagResponse>>> getList(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<TagResponse> response = tagService.getList(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取标签详情", description = "获取指定标签的详细信息")
    public ResponseEntity<ApiResponse<TagResponse>> getById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        TagResponse response = tagService.getById(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
