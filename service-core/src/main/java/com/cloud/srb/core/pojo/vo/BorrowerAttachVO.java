package com.cloud.srb.core.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName BorrowerAttachVO
 * @Author xsshuai
 * @Date 2021/6/2 9:24 上午
 **/

@Data
@ApiModel(value = "借款人附件资料")
public class BorrowerAttachVO {

    @ApiModelProperty(value = "图片类型(idCard1:身份证正面，idCard2:身份证反面，house:房产证，car:车)")
    private String imageType;

    @ApiModelProperty(value = "图片路径")
    private String imageUrl;
}
