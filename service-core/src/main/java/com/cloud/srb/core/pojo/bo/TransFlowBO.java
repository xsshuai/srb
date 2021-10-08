package com.cloud.srb.core.pojo.bo;

import com.cloud.srb.core.enums.TransTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @ClassName TransFlowBO
 * @Author xsshuai
 * @Date 2021/8/30 9:57 下午
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransFlowBO {

    private String agentBillNo;

    private String bindCode;

    private BigDecimal amount;

    private TransTypeEnum transTypeEnum;

    private String memo;
}
