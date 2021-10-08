package com.cloud.srb.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
@Getter
public enum MakeLoadReturnStatusEnum {
    MAKE_LOAD_SUCESS("0000", "放款成功"),
    AUTH_ERROR("E100","商户授权错误"),
    SIGN_ERROR("E101", "签名错误"),
    TIMESTAMP_VALID("E103", "时间戳无效"),
    PARAMS_ILLEGAL("E104","参数不全或不合法"),
    MAKE_LOAD_FAIL("105", "接收放款失败"),
    UNKNOWN_ERROR("U999", "未知错误"),
    ;
    //返回码
    private String code;
    //返回信息
    private String message;
}
