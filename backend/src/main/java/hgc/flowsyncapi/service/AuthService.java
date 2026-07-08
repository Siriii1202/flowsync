package hgc.flowsyncapi.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import hgc.flowsyncapi.dto.LoginRequest;
import hgc.flowsyncapi.dto.PasswordUpdateRequest;
import hgc.flowsyncapi.dto.RegisterRequest;
import hgc.flowsyncapi.entity.User;
import hgc.flowsyncapi.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证服务 —— 处理用户登录、身份验证等业务逻辑
 * <p>
 * 【新手必读】
 * Service 是 Spring Boot 中的"业务逻辑层"，负责处理具体的业务规则。
 * Controller（控制器）负责接收请求和返回响应，但不处理业务逻辑；
 * Service 负责处理业务逻辑，并调用 Mapper 操作数据库。
 * <p>
 * 这种分层设计的好处是：
 * 1. 职责清晰——每个类只做自己该做的事
 * 2. 便于复用——多个 Controller 可以调用同一个 Service 方法
 * 3. 便于测试——可以单独测试 Service 层的业务逻辑
 */
@Service // @Service 注解告诉 Spring：这个类是一个 Service 组件，Spring 会自动创建它的实例并管理它
public class AuthService {

    /**
     * 注入 UserMapper
     * <p>
     * 通过 @Autowired 注入（这里使用构造方法注入的方式），
     * Spring 会自动将 UserMapper 的实例"注入"到这个字段中，
     * 这样我们就可以在 AuthService 中使用 UserMapper 操作数据库了。
     */
    private final UserMapper userMapper;

    /**
     * 构造方法注入（Spring 推荐的注入方式）
     * <p>
     * 相比 @Autowired 字段注入，构造方法注入有以下优点：
     * 1. 可以在构造时明确知道需要哪些依赖
     * 2. 方便编写单元测试（可以直接传入 mock 对象）
     * 3. 避免循环依赖问题
     */
    public AuthService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * 用户登录
     * <p>
     * 处理流程：
     * 1. 根据用户名查询用户信息
     * 2. 如果用户不存在，返回 null
     * 3. 如果用户存在，比对密码是否匹配
     * 4. 密码匹配则返回用户信息（密码置空）+ 简易 token
     * 5. 密码不匹配则返回 null
     *
     * @param request 登录请求（包含用户名和密码）
     * @return 登录成功返回包含用户信息和 token 的 Map，失败返回 null
     */
    public Map<String, Object> login(LoginRequest request) {
        // 1. 根据用户名查询用户
        // LambdaQueryWrapper 是 MyBatis-Plus 提供的"条件构造器"，
        // 它的 eq 方法表示"等于"条件，这里构造了 WHERE username = 'xxx' 的查询条件
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, request.getUsername());
        User user = userMapper.selectOne(wrapper);

        // 2. 用户不存在，登录失败
        if (user == null) {
            return null;
        }

        // 3. 比对密码（明文比对，教学演示用）
        // 注意：生产环境必须使用 BCrypt 等加密算法对密码进行加密存储和比对
        if (!user.getPassword().equals(request.getPassword())) {
            return null;
        }

        // 4. 登录成功，构建返回信息
        // 将密码置空，避免将敏感信息返回给前端
        user.setPassword(null);

        // 5. 生成简易 token（教学环境使用固定格式）
        // 实际生产环境应使用 JWT 等成熟的 token 方案
        String token = "flowsync_token_" + user.getId() + "_" + System.currentTimeMillis();

        // 将用户信息和 token 放入 Map 中返回
        Map<String, Object> result = new HashMap<>();
        result.put("user", user);
        result.put("token", token);

        return result;
    }

    /**
     * 用户注册
     * <p>
     * 处理流程：
     * 1. 检查用户名是否已被注册
     * 2. 创建新用户，默认角色为 member
     * 3. 保存到数据库
     * 4. 返回用户信息（密码置空）
     *
     * @param request 注册请求（用户名、密码、姓名、手机、邮箱）
     * @return 注册成功的用户信息（不含密码）
     * @throws RuntimeException 如果用户名已存在
     */
    public User register(RegisterRequest request) {
        // 1. 检查用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, request.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("用户名已存在");
        }

        // 2. 创建新用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setRole("member"); // 新注册用户默认为成员
        user.setCreateTime(LocalDateTime.now());

        // 3. 插入数据库
        userMapper.insert(user);

        // 4. 返回用户信息（密码置空）
        user.setPassword(null);
        return user;
    }
}
