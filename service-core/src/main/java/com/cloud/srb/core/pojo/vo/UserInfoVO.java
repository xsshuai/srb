package com.cloud.srb.core.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName UserInfoVO
 * @Author xsshuai
 * @Date 2021/5/7 9:49 上午
 **/
@Data
@ApiModel(description = "登录用户信息")
public class UserInfoVO {

    @ApiModelProperty(value = "用户姓名")
    private String name;

    @ApiModelProperty(value = "用户昵称")
    private String nickName;

    @ApiModelProperty(value = "手机号")
    private String mobile;

    @ApiModelProperty(value = "用户类型")
    private Integer userType;

    @ApiModelProperty(value = "用户头像")
    private String headImg;

    @ApiModelProperty(value = "访问令牌")
    private String token;
}
