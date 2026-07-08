package hgc.flowsyncapi.dto;

/**
 * 修改密码请求 DTO
 * <p>
 * 【新手必读】
 * 当用户想要修改密码时，前端会发送 userId、oldPassword、newPassword
 * 三个字段给后端，后端校验旧密码正确后，将密码更新为新密码。
 */
public class PasswordUpdateRequest {

    /**
     * 用户 ID（要修改密码的用户）
     */
    private Long userId;

    /**
     * 旧密码（用于验证身份）
     */
    private String oldPassword;

    /**
     * 新密码（要设置的新密码）
     */
    private String newPassword;

    // ==================== 构造方法 ====================

    public PasswordUpdateRequest() {
    }

    public PasswordUpdateRequest(Long userId, String oldPassword, String newPassword) {
        this.userId = userId;
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }

    // ==================== Getter 和 Setter ====================

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
