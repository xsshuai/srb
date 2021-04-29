package com.cloud.common.result;

import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName Result
 * @Author xsshuai
 * @Date 2021/4/25 11:31 上午
 **/
@Data
public class Result {

    private Integer code;
    private String message;
    private Map<String,Object> data = new HashMap<>();

    /**
     * 构造函数私有化
     */
    private Result(){}

    /**
     * 返回成功结果
     * @return
     */
    public static Result sucess() {
        Result result = new Result();
        result.setCode(ResponseEnum.SUCCESS.getCode());
        result.setMessage(ResponseEnum.SUCCESS.getMessage());
        return result;
    }

    /**
     * 返回失败结果
     * @return
     */
    public static Result error() {
        Result result = new Result();
        result.setCode(ResponseEnum.ERROR.getCode());
        result.setMessage(ResponseEnum.ERROR.getMessage());
        return result;
    }

    /**
     * 设置特定返回结果
     * @param resultEnum
     * @return
     */
    public static Result setResult(ResponseEnum resultEnum) {
        Result result = new Result();
        result.setCode(resultEnum.getCode());
        result.setMessage(resultEnum.getMessage());
        return result;
    }

    public Result data(String key,Object value) {
        this.data.put(key,value);
        return this;
    }

    public Result data(Map<String, Object> map) {
        this.setData(map);
        return this;
    }

    /**
     * 设置特定返回码
     * @param code
     * @return
     */
    public Result code(Integer code) {
        this.setCode(code);
        return this;
    }

    /**
     * 设置特定消息
     * @param message
     * @return
     */
    public Result message(String message) {
        this.setMessage(message);
        return this;
    }
}
