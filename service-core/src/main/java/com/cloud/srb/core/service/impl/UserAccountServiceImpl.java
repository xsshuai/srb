package com.cloud.srb.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.common.exception.Assert;
import com.cloud.common.result.ResponseEnum;
import com.cloud.srb.core.enums.TransTypeEnum;
import com.cloud.srb.core.hfb.FormHelper;
import com.cloud.srb.core.hfb.HfbConst;
import com.cloud.srb.core.hfb.RequestHelper;
import com.cloud.srb.core.mapper.UserAccountMapper;
import com.cloud.srb.core.mapper.UserInfoMapper;
import com.cloud.srb.core.pojo.bo.TransFlowBO;
import com.cloud.srb.core.pojo.entity.UserAccount;
import com.cloud.srb.core.pojo.entity.UserInfo;
import com.cloud.srb.core.service.TransFlowService;
import com.cloud.srb.core.service.UserAccountService;
import com.cloud.srb.core.util.LendNoUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 用户账户 服务实现类
 * </p>
 *
 * @author xsshuai
 * @since 2021-04-24
 */
@Service
public class UserAccountServiceImpl extends ServiceImpl<UserAccountMapper, UserAccount> implements UserAccountService {

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private TransFlowService transFlowService;

    @Override
    public String commitCharge(BigDecimal chargeAmt, Long userId) {
        UserInfo userInfo = userInfoMapper.selectById(userId);
        Map<String, Object> params = new HashMap<>();
        params.put("agentId", HfbConst.AGENT_ID);
        params.put("agentBillNo", LendNoUtils.getChargeNo());
        params.put("bindCode", userInfo.getBindCode());
        params.put("chargeAmt", chargeAmt);
        params.put("feeAmt", new BigDecimal(0));
        params.put("notifyUrl", HfbConst.RECHARGE_NOTIFY_URL);
        params.put("returnUrl", HfbConst.RECHARGE_RETURN_URL);
        params.put("timestamp", RequestHelper.getTimestamp());
        params.put("sign", RequestHelper.getSign(params));

        return FormHelper.buildForm(HfbConst.RECHARGE_URL, params);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String notify(Map<String, Object> paramMap) {

        //接口调用幂等性处理 判断交易流水是否存在
        String agentBillNo = (String) paramMap.get("agentBillNo");
        if (!transFlowService.isSaveTransFlow(agentBillNo)) {
            //账户处理
            String bindCode = (String) paramMap.get("bindCode");
            String chargeAmt = (String) paramMap.get("chargeAmt");
            baseMapper.updateAccount(bindCode, new BigDecimal(chargeAmt), new BigDecimal(0));
            //记录账户流水
            TransFlowBO transFlowBO = new TransFlowBO(agentBillNo, bindCode, new BigDecimal(chargeAmt), TransTypeEnum.RECHARGE, "用户充值");
            transFlowService.saveTransFlow(transFlowBO);
        } else {
            log.warn("幂等性返回！");
        }
        return "success";
    }

    @Override
    public BigDecimal getAccount(Long userId) {
        QueryWrapper<UserAccount> userAccountQueryWrapper = new QueryWrapper<>();
        userAccountQueryWrapper.eq("user_id", userId);
        return baseMapper.selectOne(userAccountQueryWrapper).getAmount();
    }

    @Override
    public String commitWithdraw(BigDecimal fetchAmt, Long userId) {
        BigDecimal account = this.getAccount(userId);
        Assert.isTrue(account.doubleValue() >= fetchAmt.doubleValue(),
                ResponseEnum.NOT_SUFFICIENT_FUNDS_ERROR);
        UserInfo userInfo = userInfoMapper.selectById(userId);
        Map<String, Object> params = new HashMap<>();
        params.put("agentId", HfbConst.AGENT_ID);
        params.put("agentBillNo", LendNoUtils.getWithdrawNo());
        params.put("bindCode", userInfo.getBindCode());
        params.put("fetchAmt", fetchAmt);
        params.put("feeAmt", new BigDecimal(0));
        params.put("notifyUrl", HfbConst.WITHDRAW_NOTIFY_URL);
        params.put("returnUrl", HfbConst.WITHDRAW_RETURN_URL);
        params.put("timestamp", RequestHelper.getTimestamp());
        params.put("sign", RequestHelper.getSign(params));
        return FormHelper.buildForm(HfbConst.WITHDRAW_URL, params);
    }

    @Override
    public String notifyWithdraw(Map<String, Object> paramMap) {
        //接口调用幂等性处理 判断交易流水是否存在
        String agentBillNo = (String) paramMap.get("agentBillNo");
        if (!transFlowService.isSaveTransFlow(agentBillNo)) {
            //账户处理
            String bindCode = (String) paramMap.get("bindCode");
            String fetchAmt = (String) paramMap.get("fetchAmt");
            baseMapper.updateAccount(bindCode, new BigDecimal(fetchAmt).negate(), new BigDecimal(0));
            //记录账户流水
            TransFlowBO transFlowBO = new TransFlowBO(agentBillNo, bindCode, new BigDecimal(fetchAmt), TransTypeEnum.WITHDRAW, "用户提现");
            transFlowService.saveTransFlow(transFlowBO);
        } else {
            log.warn("幂等性返回！");
        }
        return "success";
    }
}
