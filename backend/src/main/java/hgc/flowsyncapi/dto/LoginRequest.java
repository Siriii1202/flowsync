package hgc.flowsyncapi.dto;

/**
 * 登录请求 DTO（Data Transfer Object，数据传输对象）
 * <p>
 * 【新手必读】
 * DTO 是专门用于"在不同层之间传递数据"的对象。
 * 比如前端登录时传 username 和 password，后端就用这个类来接收。
 * DTO 和 Entity 不同：Entity 对应数据库表，DTO 只关心"接口需要什么数据"。
 */
public class LoginRequest {

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    // ==================== 构造方法 ====================

    public LoginRequest() {
    }

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // ==================== Getter 和 Setter ====================

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
}
