package hgc.flowsyncapi.common;

/**
 * 统一响应结果封装类
 * <p>
 * 【新手必读】
 * 这个类用于统一后端返回给前端的数据格式。所有的 Controller 方法
 * 都返回 ApiResponse 对象，这样前端收到的数据格式就是统一的，
 * 方便前端进行数据处理和异常判断。
 * <p>
 * 返回给前端的 JSON 格式示例：
 * {
 *   "success": true,        // 请求是否成功
 *   "message": "操作成功",  // 提示消息
 *   "data": { ... }         // 实际数据（可以是对象、列表、null 等）
 * }
 *
 * @param <T> 数据的类型（泛型），可以是任意 Java 类型
 */
public class ApiResponse<T> {

    /**
     * 请求是否成功（true=成功，false=失败）
     */
    private boolean success;

    /**
     * 提示消息（成功时的提示或失败时的错误信息）
     */
    private String message;

    /**
     * 实际返回的数据（可以是单个对象、列表、null 等）
     */
    private T data;

    /**
     * 私有构造方法 —— 不允许外部直接 new，必须通过静态方法创建
     */
    private ApiResponse() {
    }

    /**
     * 全参构造方法
     *
     * @param success 是否成功
     * @param message 提示消息
     * @param data    返回数据
     */
    private ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // ==================== 静态工厂方法（推荐使用） ====================

    /**
     * 操作成功（无返回数据）
     * <p>
     * 适用于删除、更新等不需要返回数据的操作。
     *
     * @param <T> 数据类型
     * @return 成功的响应对象
     */
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(true, "操作成功", null);
    }

    /**
     * 操作成功（带返回数据）
     * <p>
     * 适用于查询等需要返回数据的操作。
     *
     * @param data 要返回的数据
     * @param <T>  数据类型
     * @return 成功的响应对象
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "操作成功", data);
    }

    /**
     * 操作失败（带错误消息）
     * <p>
     * 适用于参数校验失败、业务异常等情况。
     *
     * @param message 错误提示消息
     * @param <T>     数据类型
     * @return 失败的响应对象
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }

    // ==================== Getter 和 Setter 方法 ====================

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
