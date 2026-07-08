package hgc.flowsyncapi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 用户实体类 —— 对应数据库中的 user 表
 * <p>
 * 【新手必读】
 * 实体类（Entity）是用来"装数据"的 Java 类，它的每个字段对应数据库表中的一列。
 * MyBatis-Plus 会自动将实体类对象与数据库表之间进行映射转换，
 * 这样我们就可以用"面向对象"的方式操作数据库，而不用写繁琐的 SQL 了。
 * <p>
 * 例如：这个类的 username 字段对应 user 表的 username 列，
 * 当我们保存一个 User 对象时，MyBatis-Plus 会自动生成 INSERT 语句。
 */
@TableName("sys_user") // 指定该实体类对应的数据库表名
public class User {

    /**
     * 用户唯一 ID（主键）
     * @TableId 标注该字段是主键，type = IdType.AUTO 表示使用数据库自增策略
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名（登录使用）
     */
    private String username;

    /**
     * 密码（明文存储，教学演示用；生产环境必须加密）
     */
    private String password;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 用户角色（如：admin=管理员，user=普通用户）
     */
    private String role;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 电子邮箱
     */
    private String email;

    /**
     * 创建时间（LocalDateTime 是 Java 8 新增的日期时间类型）
     */
    private LocalDateTime createTime;

    // ==================== Getter 和 Setter 方法 ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
