package com.cloud.srb.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum NotifyStatusEnum {

    NOTIFY_SUCCESS("0001", "远程调用成功"),
    NOTIFY_FAIL("0002", "远程调用失败"),
    ;

    private String status;
    private String msg;
}

