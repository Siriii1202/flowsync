package hgc.flowsyncapi.dto;

/**
 * 注册请求 DTO
 * <p>
 * 用户注册时前端传递的数据对象，包含用户名、密码、姓名、手机、邮箱。
 */
public class RegisterRequest {

    private String username;
    private String password;
    private String realName;
    private String phone;
    private String email;

    public RegisterRequest() {}

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
