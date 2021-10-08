package com.cloud.srb.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.common.exception.Assert;
import com.cloud.common.result.ResponseEnum;
import com.cloud.srb.core.hfb.FormHelper;
import com.cloud.srb.core.hfb.HfbConst;
import com.cloud.srb.core.hfb.RequestHelper;
import com.cloud.srb.core.mapper.LendMapper;
import com.cloud.srb.core.mapper.LendReturnMapper;
import com.cloud.srb.core.mapper.UserAccountMapper;
import com.cloud.srb.core.mapper.UserInfoMapper;
import com.cloud.srb.core.pojo.entity.Lend;
import com.cloud.srb.core.pojo.entity.LendItem;
import com.cloud.srb.core.pojo.entity.LendReturn;
import com.cloud.srb.core.pojo.entity.UserInfo;
import com.cloud.srb.core.service.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 还款记录表 服务实现类
 * </p>
 *
 * @author xsshuai
 * @since 2021-04-24
 */
@Service
public class LendReturnServiceImpl extends ServiceImpl<LendReturnMapper, LendReturn> implements LendReturnService {

    @Resource
    private LendMapper lendMapper;

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private UserAccountService userAccountService;

    @Resource
    private LendItemReturnService lendItemReturnService;

    @Resource
    private TransFlowService transFlowService;

    @Resource
    private LendItemService lendItemService;

    @Resource
    private UserAccountMapper userAccountMapper;

    @Override
    public List<LendReturn> selectByLendId(Long lendId) {
        QueryWrapper<LendReturn> lendReturnQueryWrapper = new QueryWrapper<>();
        lendReturnQueryWrapper.eq("lend_id", lendId);
        return baseMapper.selectList(lendReturnQueryWrapper);
    }

    @Override
    public String commitReturn(Long lendReturnId, Long userId) {
        //调用汇付宝还款接口

        LendReturn lendReturn = baseMapper.selectById(lendReturnId);
        Lend lend = lendMapper.selectById(lendReturn.getLendId());
        UserInfo userInfo = userInfoMapper.selectById(userId);

        BigDecimal account = userAccountService.getAccount(userId);
        Assert.isTrue(account.doubleValue() >=lendReturn.getTotal().doubleValue(),
                ResponseEnum.NOT_SUFFICIENT_FUNDS_ERROR);
        Map<String, Object> params = new HashMap<>();
        params.put("agentId", HfbConst.AGENT_ID);
        params.put("agentGoodsName", lend.getTitle());
        params.put("agentBatchNo", lendReturn.getReturnNo());
        params.put("fromBindCode", userInfo.getBindCode());
        params.put("totalAmt", lendReturn.getTotal());
        params.put("note", "用户还款");
        params.put("voteFeeAmt", new BigDecimal(0));
        params.put("notifyUrl", HfbConst.BORROW_RETURN_NOTIFY_URL);
        params.put("returnUrl", HfbConst.BORROW_RETURN_RETURN_URL);
        params.put("timestamp", RequestHelper.getTimestamp());
        params.put("data", lendItemReturnService.addReturnDetail(lendReturnId));
        params.put("sign", RequestHelper.getSign(params));
        return FormHelper.buildForm(HfbConst.BORROW_RETURN_URL, params);
    }

    @Override
    public String notifyReturn(Map<String, Object> paramMap) {
        //接口调用幂等性处理 判断交易流水是否存在
        String agentBatchNo = (String) paramMap.get("agentBatchNo");
        if (!transFlowService.isSaveTransFlow(agentBatchNo)) {
            //还款处理
            String totalAmt = (String) paramMap.get("fetchAmt");
            //更新还款计划
            LendItem lendItem = lendItemService.selectByReturnNo(agentBatchNo);


            //userAccountMapper.updateAccount(bindCode, new BigDecimal(fetchAmt).negate(), new BigDecimal(0));
            //记录账户流水
            //TransFlowBO transFlowBO = new TransFlowBO(agentBillNo, bindCode, new BigDecimal(fetchAmt), TransTypeEnum.WITHDRAW, "用户提现");
            //transFlowService.saveTransFlow(transFlowBO);
        } else {
            log.warn("幂等性返回！");
        }
        return "success";
    }
}
