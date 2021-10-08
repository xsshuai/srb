package com.cloud.srb.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum LendItemStatusEnum {

    NEW(0, "新建"),
    PAID(1, "已支付"),
    RETUENED(2, "已还款"),
    ;
    private Integer status;
    private String msg;
}
