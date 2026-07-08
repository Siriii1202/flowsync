package hgc.flowsyncapi.controller;

import hgc.flowsyncapi.common.ApiResponse;
import hgc.flowsyncapi.service.OverviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 概览控制器 —— 处理首页/仪表盘概览数据请求
 * <p>
 * 【新手必读】
 * 概览（Overview）是用户登录后看到的"首页"或"仪表盘"页面。
 * 它会展示一些汇总统计数据，比如：
 * - 总共有多少个项目
 * - 有多少任务待办、进行中、已完成
 * - 最近的动态等
 * <p>
 * 这些数据能帮助用户快速了解整体工作进展。
 * <p>
 * ┌──────────────────────────────────────────────────────────────┐
 * │ 接口列表：                                                    │
 * │ GET /api/overview  — 获取概览数据                             │
 * └──────────────────────────────────────────────────────────────┘
 */
@RestController
@RequestMapping("/api/overview")
public class OverviewController {

    /**
     * 概览服务 —— 处理概览数据的业务逻辑
     */
    @Autowired
    private OverviewService overviewService;

    /**
     * 获取概览数据
     * <p>
     * 【接口说明】
     * 获取仪表盘所需的汇总数据，包括项目总数、任务统计、
     * 最近动态等信息。前端页面加载时会调用此接口来展示概览。
     * 可通过请求头 userId 过滤，只返回该用户相关的统计数据。
     * <p>
     * 【请求方式】GET
     * 【请求路径】/api/overview
     * 【请求头】userId（可选，为空时返回全局统计）
     * 【返回结果】ApiResponse.success(data) — data 为概览数据对象
     *
     * @param userId 当前用户 ID（从请求头获取，可选）
     * @return 统一响应结果，包含概览数据
     */
    @GetMapping
    public ApiResponse<?> getOverview(@RequestHeader(value = "userId", required = false) Long userId) {
        return ApiResponse.success(overviewService.getOverview(userId));
    }
}
