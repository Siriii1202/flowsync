package hgc.flowsyncapi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import hgc.flowsyncapi.entity.User;

/**
 * 用户 Mapper 接口 —— 操作 user 表的数据库访问层
 * <p>
 * 【新手必读】
 * Mapper 是 MyBatis-Plus 中的"数据访问层"，负责与数据库打交道。
 * 我们只需要让这个接口继承 BaseMapper&lt;User&gt;，MyBatis-Plus 就会
 * 自动帮我们实现常见的增删改查方法（如 insert、selectById、update 等），
 * 完全不需要写 SQL 语句！
 * <p>
 * 如果以后需要自定义查询，可以在这里添加自定义方法，
 * 然后在 resources/mapper 目录下编写对应的 XML 映射文件。
 */
public interface UserMapper extends BaseMapper<User> {
}
