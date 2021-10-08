package com.cloud.srb.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.common.exception.Assert;
import com.cloud.common.result.ResponseEnum;
import com.cloud.srb.core.enums.LendItemStatusEnum;
import com.cloud.srb.core.enums.LendStatusEnum;
import com.cloud.srb.core.enums.TransTypeEnum;
import com.cloud.srb.core.hfb.FormHelper;
import com.cloud.srb.core.hfb.HfbConst;
import com.cloud.srb.core.hfb.RequestHelper;
import com.cloud.srb.core.mapper.LendItemMapper;
import com.cloud.srb.core.mapper.LendMapper;
import com.cloud.srb.core.mapper.UserAccountMapper;
import com.cloud.srb.core.pojo.bo.TransFlowBO;
import com.cloud.srb.core.pojo.entity.Lend;
import com.cloud.srb.core.pojo.entity.LendItem;
import com.cloud.srb.core.pojo.vo.InvestVO;
import com.cloud.srb.core.service.*;
import com.cloud.srb.core.util.LendNoUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 标的出借记录表 服务实现类
 * </p>
 *
 * @author xsshuai
 * @since 2021-04-24
 */
@Service
public class LendItemServiceImpl extends ServiceImpl<LendItemMapper, LendItem> implements LendItemService {

    @Resource
    private LendMapper lendMapper;

    @Resource
    private UserAccountService userAccountService;

    @Resource
    private LendItemMapper lendItemMapper;

    @Resource
    private LendService lendService;

    @Resource
    private UserBindService userBindService;

    @Resource
    private TransFlowService transFlowService;

    @Resource
    private UserAccountMapper userAccountMapper;

    @Override
    public String commitInvest(InvestVO investVO) {
        //健壮性校验
        Lend lend = lendMapper.selectById(investVO.getLendId());

        //判断标的状态是否是募资中
        Assert.isTrue(lend.getStatus().intValue() == LendStatusEnum.INVEST_RUN.getStatus(),
                ResponseEnum.LEND_INVEST_ERROR);
        //判断是否超卖：已投金额 + 当前投资金额 <= 标的金额（正常）
        BigDecimal sum = lend.getInvestAmount().add(new BigDecimal(investVO.getInvestAmount()));
        Assert.isTrue(sum.doubleValue() <= lend.getAmount().doubleValue(),
                ResponseEnum.LEND_FULL_SCALE_ERROR);
        //用户余额是否充足：当前用户的余额 >= 当前投资金额
        BigDecimal account = userAccountService.getAccount(investVO.getInvestUserId());
        Assert.isTrue(account.doubleValue() >= Double.parseDouble(investVO.getInvestAmount()),
                ResponseEnum.NOT_SUFFICIENT_FUNDS_ERROR);
        //获取paramMap中需要的参数
        //生成标的下的投资记录
        LendItem lendItem = new LendItem();
        lendItem.setInvestUserId(investVO.getInvestUserId());//投资人id
        lendItem.setInvestName(investVO.getInvestUserName());//投资人名字
        String lendItemNo = LendNoUtils.getLendItemNo();
        lendItem.setLendItemNo(lendItemNo); //投资条目编号（一个Lend对应一个或多个LendItem）
        lendItem.setLendId(investVO.getLendId());//对应的标的id
        lendItem.setInvestAmount(new BigDecimal(investVO.getInvestAmount())); //此笔投资金额
        lendItem.setLendYearRate(lend.getLendYearRate());//年化
        lendItem.setInvestTime(LocalDateTime.now()); //投资时间
        lendItem.setLendStartDate(lend.getLendStartDate()); //开始时间
        lendItem.setLendEndDate(lend.getLendEndDate()); //结束时间
        //预期收益
        BigDecimal expectAmount = lendService.getInterestCount(
                lendItem.getInvestAmount(),
                lendItem.getLendYearRate(),
                lend.getPeriod(),
                lend.getReturnMethod());
        lendItem.setExpectAmount(expectAmount);

        //实际收益
        lendItem.setRealAmount(new BigDecimal(0));

        //投资记录的状态
        lendItem.setStatus(LendItemStatusEnum.NEW.getStatus());//刚刚创建投资记录，账户信息尚未修改
        baseMapper.insert(lendItem);//存入数据库

        //获取投资人的bindCode
        String bindCode = userBindService.getBindCodeByUserId(investVO.getInvestUserId());
        //获取借款人的bindCode
        String benefitBindCode = userBindService.getBindCodeByUserId(lend.getUserId());
        //封装提交至汇付宝的参数
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("agentId", HfbConst.AGENT_ID);
        paramMap.put("voteBindCode", bindCode);
        paramMap.put("benefitBindCode",benefitBindCode);
        paramMap.put("agentProjectCode", lend.getLendNo());//项目标号
        paramMap.put("agentProjectName", lend.getTitle());

        //在资金托管平台上的投资订单的唯一编号，可以独立生成，不一定非要和lendItemNo保持一致，但是可以一致。
        paramMap.put("agentBillNo", lendItemNo);//订单编号
        paramMap.put("voteAmt", investVO.getInvestAmount());
        paramMap.put("votePrizeAmt", "0");
        paramMap.put("voteFeeAmt", "0");
        paramMap.put("projectAmt", lend.getAmount()); //标的总金额
        paramMap.put("note", "");
        paramMap.put("notifyUrl", HfbConst.INVEST_NOTIFY_URL); //检查常量是否正确
        paramMap.put("returnUrl", HfbConst.INVEST_RETURN_URL);
        paramMap.put("timestamp", RequestHelper.getTimestamp());
        String sign = RequestHelper.getSign(paramMap);
        paramMap.put("sign", sign);

        //构建充值自动提交表单
        return FormHelper.buildForm(HfbConst.INVEST_URL, paramMap);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String notify(Map<String, Object> paramMap) {
        //接口调用幂等性处理 判断交易流水是否存在
        String agentBillNo = (String) paramMap.get("agentBillNo");
        if (!transFlowService.isSaveTransFlow(agentBillNo)) {
            //账户处理
            String bindCode = (String) paramMap.get("voteBindCode");
            String freezeAmt = (String) paramMap.get("voteAmt");
            userAccountMapper.updateAccount(bindCode, new BigDecimal(freezeAmt).negate(), new BigDecimal(freezeAmt));

            //修改投资记录状态
            LendItem lendItem = this.getByLendItemNo(agentBillNo);
            lendItem.setStatus(LendItemStatusEnum.PAID.getStatus());
            baseMapper.updateById(lendItem);

            //修改标的记录：投资人数、已投金额
             Lend lend = lendMapper.selectById(lendItem.getLendId());
             lend.setInvestAmount(lend.getInvestAmount().add(lendItem.getInvestAmount()));
             lend.setInvestNum(lend.getInvestNum() + 1);
             lendMapper.updateById(lend);

            //记录账户流水
            TransFlowBO transFlowBO = new TransFlowBO(
                    agentBillNo,
                    bindCode,
                    new BigDecimal(freezeAmt),
                    TransTypeEnum.INVEST_LOCK,
                    "用户投资:项目编号-" + lend.getLendNo() + "项目名称-" + lend.getTitle());
            transFlowService.saveTransFlow(transFlowBO);
        } else {
            log.warn("幂等性返回！");
        }
        return "success";
    }

    @Override
    public List<LendItem> selectByLendId(Long lendId, Integer status) {
        QueryWrapper<LendItem> lendItemQueryWrapper = new QueryWrapper<>();
        lendItemQueryWrapper
                .eq("lend_id", lendId)
                .eq("status", status);
        return baseMapper.selectList(lendItemQueryWrapper);
    }

    @Override
    public List<LendItem> selectByLendId(Long lendId) {
        QueryWrapper<LendItem> lendItemQueryWrapper = new QueryWrapper<>();
        lendItemQueryWrapper.eq("lend_id", lendId);
        return baseMapper.selectList(lendItemQueryWrapper);
    }

    @Override
    public LendItem selectByReturnNo(String returnNo) {
        QueryWrapper<LendItem> lendItemQueryWrapper = new QueryWrapper<>();
        lendItemQueryWrapper.eq("return_no", returnNo);
        return baseMapper.selectOne(lendItemQueryWrapper);
    }

    private LendItem getByLendItemNo(String lendItemNo) {
        QueryWrapper<LendItem> lendItemQueryWrapper = new QueryWrapper<>();
        lendItemQueryWrapper.eq("lend_item_no", lendItemNo);
        return baseMapper.selectOne(lendItemQueryWrapper);
    }
}
