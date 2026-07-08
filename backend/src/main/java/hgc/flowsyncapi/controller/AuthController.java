package hgc.flowsyncapi.controller;

import hgc.flowsyncapi.common.ApiResponse;
import hgc.flowsyncapi.dto.LoginRequest;
import hgc.flowsyncapi.dto.RegisterRequest;
import hgc.flowsyncapi.entity.User;
import hgc.flowsyncapi.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器 —— 处理用户登录相关请求
 * <p>
 * 【新手必读】
 * 控制器（Controller）是后端程序的"入口"，
 * 负责接收前端发来的 HTTP 请求，调用对应的 Service 处理业务逻辑，
 * 最后将处理结果返回给前端。
 * <p>
 * ┌──────────────────────────────────────────────────────────────┐
 * │ 接口列表：                                                    │
 * │ POST /api/auth/login  — 用户登录，验证用户名和密码              │
 * └──────────────────────────────────────────────────────────────┘
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    /**
     * 认证服务 —— 处理登录验证等业务逻辑
     * <p>
     * 【新手必读】
     * @Autowired 是 Spring 框架的注解，它会自动将 AuthService 的实例
     * "注入"到这个字段中。你不需要手动 new AuthService()，
     * Spring 会帮你管理对象的创建和生命周期，这叫"依赖注入"（DI）。
     */
    @Autowired
    private AuthService authService;

    /**
     * 用户登录接口
     * <p>
     * 【接口说明】
     * 前端在登录页面输入用户名和密码后，调用此接口进行身份验证。
     * 验证通过后返回用户信息；验证失败返回错误提示。
     * <p>
     * 【请求方式】POST
     * 【请求路径】/api/auth/login
     * 【请求参数】@RequestBody LoginRequest — 包含 username 和 password 的 JSON 对象
     * 【返回结果】
     *   - 成功：ApiResponse.success(data) — data 为登录用户的信息
     *   - 失败：ApiResponse.error("用户名或密码错误")
     *
     * @param loginRequest 登录请求数据（用户名和密码）
     * @return 统一响应结果
     */
    @PostMapping("/login")
    public ApiResponse<?> login(@RequestBody LoginRequest loginRequest) {
        // 调用 AuthService 的 login 方法进行登录验证
        Object result = authService.login(loginRequest);
        // 如果返回 null，说明用户名或密码不正确
        if (result == null) {
            return ApiResponse.error("用户名或密码错误");
        }
        // 登录成功，返回用户数据
        return ApiResponse.success(result);
    }

    /**
     * 用户注册接口
     * <p>
     * 新用户注册，注册成功后自动以 member 身份加入系统。
     *
     * 【请求方式】POST
     * 【请求路径】/api/auth/register
     * 【请求参数】@RequestBody RegisterRequest — 用户名、密码、姓名、手机、邮箱
     * 【返回结果】
     *   - 成功：ApiResponse.success(data) — data 为新注册的用户信息
     *   - 失败：ApiResponse.error("用户名已存在")
     *
     * @param registerRequest 注册请求数据
     * @return 统一响应结果
     */
    @PostMapping("/register")
    public ApiResponse<?> register(@RequestBody RegisterRequest registerRequest) {
        try {
            User user = authService.register(registerRequest);
            return ApiResponse.success(user);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
