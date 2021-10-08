package com.cloud.srb.core.pojo.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @ClassName InvestVO
 * @Author xsshuai
 * @Date 2021/8/31 2:11 下午
 **/
@Data
@ApiModel(description = "投标信息")
public class InvestVO {

    private Long lendId;

    private String investAmount;

    private Long investUserId;

    private String investUserName;
}
