package com.cloud.srb.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum UserInfoStatusEnum {
    LOCKED(0, "锁定状态"),
    NOMAL(1, "正常状态"),
    ;
    private Integer status;
    private String msg;

    public static String getMsgByStatus(int status) {
        UserInfoStatusEnum arrObj[] = UserInfoStatusEnum.values();
        for (UserInfoStatusEnum obj : arrObj) {
            if (status == obj.getStatus().intValue()) {
                return obj.getMsg();
            }
        }
        return "";
    }
}
