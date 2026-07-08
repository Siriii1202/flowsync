package hgc.flowsyncapi.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import hgc.flowsyncapi.dto.PasswordUpdateRequest;
import hgc.flowsyncapi.entity.User;
import hgc.flowsyncapi.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户服务 —— 处理用户管理相关的业务逻辑
 * <p>
 * 【新手必读】
 * 这个 Service 负责所有与"用户"相关的业务操作，包括：
 * - 查询用户列表（管理员查看所有用户）
 * - 修改密码（用户修改自己的登录密码）
 * <p>
 * 按照"单一职责原则"，每个 Service 只负责管理自己对应的实体。
 */
@Service
public class UserService {

    /**
     * 用户 Mapper，用于操作 user 表
     */
    private final UserMapper userMapper;

    /**
     * 构造方法注入
     */
    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * 查询所有用户列表
     * <p>
     * selectList(null) 表示查询全部记录，相当于 SQL 中的 SELECT * FROM user。
     * MyBatis-Plus 会自动将查询结果转换为 List&lt;User&gt; 对象。
     *
     * @return 所有用户的列表
     */
    public List<User> listUsers() {
        return userMapper.selectList(null);
    }

    /**
     * 修改用户密码
     * <p>
     * 处理流程：
     * 1. 根据 userId 查询用户是否存在
     * 2. 校验旧密码是否正确
     * 3. 旧密码正确则更新为新密码
     * 4. 如果用户不存在或旧密码错误，返回 false
     *
     * @param request 修改密码请求（包含 userId、旧密码、新密码）
     * @return 修改成功返回 true，失败返回 false
     */
    public boolean updatePassword(PasswordUpdateRequest request) {
        // 1. 根据用户 ID 查询用户
        User user = userMapper.selectById(request.getUserId());

        // 2. 用户不存在或旧密码不匹配，返回失败
        if (user == null || !user.getPassword().equals(request.getOldPassword())) {
            return false;
        }

        // 3. 更新密码
        user.setPassword(request.getNewPassword());
        userMapper.updateById(user);
        return true;
    }
}
