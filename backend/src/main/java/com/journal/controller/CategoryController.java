package com.journal.controller;

import com.journal.dto.ApiResponse;
import com.journal.dto.CategoryRequest;
import com.journal.dto.CategoryResponse;
import com.journal.service.CategoryService;
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
 * 分类控制器
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "分类管理", description = "日记分类的增删改查接口")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @Operation(summary = "创建分类", description = "创建新的日记分类")
    public ResponseEntity<ApiResponse<CategoryResponse>> create(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.create(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success("创建成功", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新分类", description = "更新指定分类的信息")
    public ResponseEntity<ApiResponse<CategoryResponse>> update(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.update(userDetails.getUsername(), id, request);
        return ResponseEntity.ok(ApiResponse.success("更新成功", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除分类", description = "删除指定分类")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        categoryService.delete(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }

    @GetMapping
    @Operation(summary = "获取分类列表", description = "获取当前用户的所有分类")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getList(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<CategoryResponse> response = categoryService.getList(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取分类详情", description = "获取指定分类的详细信息")
    public ResponseEntity<ApiResponse<CategoryResponse>> getById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        CategoryResponse response = categoryService.getById(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
