package com.cloud.srb.core.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName BorrowerApprovalVO
 * @Author xsshuai
 * @Date 2021/8/6 10:24 上午
 **/
@Data
@ApiModel(description = "借款人审批")
public class BorrowerApprovalVO {

    @ApiModelProperty(value = "id")
    private Long borrowerId;

    @ApiModelProperty(value = "状态")
    private Integer status;

    @ApiModelProperty(value = "身份信息是否正确")
    private Boolean isIdCardOk;

    @ApiModelProperty(value = "房屋信息是否正确")
    private Boolean isHouseOk;

    @ApiModelProperty(value = "车辆信息是否正确")
    private Boolean isCarOk;

    @ApiModelProperty(value = "基本信息积分")
    private Integer infoIntegral;
}
