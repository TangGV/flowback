package cn.flowback.common.utils;

/**
 * http请求响应对象
 * @author Tang
 */
public class ResponseResult {

    private Integer statusCode;

    private String result;

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
