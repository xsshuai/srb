package com.cloud.srb.core.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.common.exception.BusinessException;
import com.cloud.common.util.DateConverUtils;
import com.cloud.srb.core.enums.LendStatusEnum;
import com.cloud.srb.core.enums.MakeLoadReturnStatusEnum;
import com.cloud.srb.core.enums.ReturnMethodEnum;
import com.cloud.srb.core.enums.TransTypeEnum;
import com.cloud.srb.core.hfb.HfbConst;
import com.cloud.srb.core.hfb.RequestHelper;
import com.cloud.srb.core.mapper.*;
import com.cloud.srb.core.pojo.bo.TransFlowBO;
import com.cloud.srb.core.pojo.entity.*;
import com.cloud.srb.core.pojo.vo.BorrowInfoApprovalVO;
import com.cloud.srb.core.pojo.vo.BorrowerDetailVO;
import com.cloud.srb.core.service.*;
import com.cloud.srb.core.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 标的准备表 服务实现类
 * </p>
 *
 * @author xsshuai
 * @since 2021-04-24
 */
@Service
@Slf4j
public class LendServiceImpl extends ServiceImpl<LendMapper, Lend> implements LendService {

    @Resource
    private DictService dictService;

    @Resource
    private BorrowerService borrowerService;

    @Resource
    private BorrowerMapper borrowerMapper;

    @Resource
    private UserAccountMapper userAccountMapper;

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private TransFlowMapper transFlowMapper;

    @Resource
    private LendItemMapper lendItemMapper;

    @Resource
    private TransFlowService transFlowService;

    @Resource
    private LendReturnService lendReturnService;

    @Resource
    private LendItemService lendItemService;

    @Resource
    private LendItemReturnService lendItemReturnService;

    @Override
    public void create(BorrowInfoApprovalVO borrowInfoApprovalVO, BorrowInfo borrowInfo) {
        Lend lend = new Lend();
        lend.setUserId(borrowInfo.getUserId());
        lend.setBorrowInfoId(borrowInfo.getId());
        lend.setLendNo(LendNoUtils.getLendNo());
        lend.setAmount(borrowInfo.getAmount());
        lend.setPeriod(borrowInfo.getPeriod());
        lend.setReturnMethod(borrowInfo.getReturnMethod());
        lend.setTitle(borrowInfoApprovalVO.getTitle());
        lend.setLendYearRate(borrowInfoApprovalVO.getLendYearRate().divide(new BigDecimal(100)));
        lend.setServiceRate(borrowInfoApprovalVO.getServiceRate().divide(new BigDecimal(100)));
        lend.setLowestAmount(new BigDecimal(100));
        lend.setInvestAmount(new BigDecimal(0));
        lend.setInvestNum(0);
        lend.setPublishDate(LocalDateTime.now());
        lend.setLendStartDate(DateConverUtils.dateConver(borrowInfoApprovalVO.getLendStartDate()));
        lend.setLendEndDate(DateConverUtils.dateConver(borrowInfoApprovalVO.getLendStartDate()).plusMonths(borrowInfo.getPeriod()));
        lend.setLendInfo(borrowInfoApprovalVO.getLendInfo());
        //平台预期收益 = 标的金额 * 服务年化利率 / 12 * 期数
        BigDecimal monthRate = lend.getServiceRate().divide(new BigDecimal(12), 8, BigDecimal.ROUND_DOWN);
        BigDecimal expectAmount = borrowInfo.getAmount().multiply(monthRate.multiply(new BigDecimal(borrowInfo.getPeriod())));
        lend.setExpectAmount(expectAmount);
        //平台实际收益
        lend.setRealAmount(new BigDecimal(0));
        lend.setStatus(LendStatusEnum.INVEST_RUN.getStatus());
        lend.setCheckTime(LocalDateTime.now());
        lend.setCheckAdminId(1L);


        baseMapper.insert(lend);
    }

    @Override
    public List<Lend> selectList() {

        List<Lend> lendList = baseMapper.selectList(null);
        for (Lend lend:lendList
             ) {
            String returnMethod = dictService.getNameByParentDictCodeAndValue("returnMethod", lend.getReturnMethod());
            String status = LendStatusEnum.getMsgByStatus(lend.getStatus());
            lend.getParam().put("returnMethod", returnMethod);
            lend.getParam().put("status", status);
        }
        return lendList;
    }

    @Override
    public Map<String, Object> getLendDetail(Long id) {

        //查询标的lend
        Lend lend = baseMapper.selectById(id);
        String returnMethod = dictService.getNameByParentDictCodeAndValue("returnMethod", lend.getReturnMethod());
        String status = LendStatusEnum.getMsgByStatus(lend.getStatus());
        lend.getParam().put("returnMethod", returnMethod);
        lend.getParam().put("status", status);

        //查询借款人对象：borrower(borrowDetailVO)
        QueryWrapper<Borrower> borrowerQueryWrapper = new QueryWrapper<>();
        borrowerQueryWrapper.eq("user_id", lend.getUserId());
        Borrower borrower = borrowerMapper.selectOne(borrowerQueryWrapper);
        BorrowerDetailVO borrowerDetailVO= borrowerService.getBorrowerDetailVOById(borrower.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("lend", lend);
        result.put("borrower", borrowerDetailVO);
        return result;
    }

    @Override
    public BigDecimal getInterestCount(BigDecimal invest, BigDecimal yearRate, Integer totalMonth, Integer returnMethod) {
        BigDecimal interestCount = new BigDecimal(0);
        if (returnMethod.intValue() == ReturnMethodEnum.ONE.getMethod()) {
            interestCount = Amount1Helper.getInterestCount(invest, yearRate, totalMonth);
        }else if (returnMethod.intValue() == ReturnMethodEnum.TWO.getMethod()) {
            interestCount = Amount2Helper.getInterestCount(invest, yearRate, totalMonth);
        }else if (returnMethod.intValue() == ReturnMethodEnum.THREE.getMethod()) {
            interestCount = Amount3Helper.getInterestCount(invest, yearRate, totalMonth);
        }else {
            interestCount = Amount4Helper.getInterestCount(invest, yearRate, totalMonth);
        }
        return interestCount;
    }

    @Override
    public void makeLoan(Long id) {

        //获取标的信息
        Lend lend = baseMapper.selectById(id);

        //调用汇付宝放款接口
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("agentId", HfbConst.AGENT_ID);
        paramMap.put("agentProjectCode", lend.getLendNo());
        paramMap.put("agentBillNo", LendNoUtils.getLoanNo());
        //平台服务费月利率
        BigDecimal monthRate = lend.getServiceRate().divide(new BigDecimal(12), 8, BigDecimal.ROUND_DOWN);
        //平台服务费月金额：月利率 * 已投金额 * 时长
        BigDecimal realAmount = lend.getInvestAmount().multiply(monthRate).multiply(new BigDecimal(lend.getPeriod()));
        paramMap.put("mchFee", realAmount);
        paramMap.put("timestamp", RequestHelper.getTimestamp());
        paramMap.put("sign", RequestHelper.getSign(paramMap));

        //向汇付宝提交远程请求
       JSONObject result = RequestHelper.sendRequest(paramMap, HfbConst.MAKE_LOAD_URL);
       log.info("汇付宝放款结果返回：" + result.toJSONString());

       if (!result.getString("resultCode").equals(MakeLoadReturnStatusEnum.MAKE_LOAD_SUCESS.getCode())) {
           throw new BusinessException(result.getString("resultMsg"));
       }

       //放款成功
       //修改标的状态，并计算平台收益
       lend.setStatus(LendStatusEnum.PAY_RUN.getStatus());
       lend.setRealAmount(realAmount);
       lend.setPaymentTime(LocalDateTime.now());
       baseMapper.updateById(lend);

       //给借款账户打入金额
       UserInfo borrower = userInfoMapper.selectById(lend.getUserId());
       //放款金额
       String voteAmount = result.getString("voteAmt");
       userAccountMapper.updateAccount(borrower.getBindCode(), new BigDecimal(voteAmount), new BigDecimal(0));

       //添加借款人交易流水
       TransFlowBO transFlowBO = new TransFlowBO(
               LendNoUtils.getTransNo(),
               borrower.getBindCode(),
               new BigDecimal(voteAmount),
               TransTypeEnum.BORROW_BACK,
               "放款到账-" + lend.getLendNo() + lend.getTitle()
               );
       transFlowService.saveTransFlow(transFlowBO);
//       TransFlow transFlow = new TransFlow();
//       transFlow.setTransNo(LendNoUtils.getTransNo());
//       transFlow.setTransType(TransTypeEnum.BORROW_BACK.getTransType());
//       transFlow.setTransTypeName(TransTypeEnum.BORROW_BACK.getTransTypeName());
//       transFlow.setTransAmount(new BigDecimal(voteAmount));
//       transFlow.setCreateTime(LocalDateTime.now());
//       transFlow.setUserId(borrower.getId());
//       transFlow.setUserName(borrower.getName());
//       transFlow.setMemo(lend.getLendNo() + "-放款到账");
//       transFlowMapper.insert(transFlow);

       //解冻并扣除投资人冻结金额
       QueryWrapper<LendItem> lendItemQueryWrapper = new QueryWrapper<>();
       lendItemQueryWrapper.eq("lend_id", id);
       List<LendItem> lendItemList = lendItemMapper.selectList(lendItemQueryWrapper);
       lendItemList.stream().forEach(lendItem -> {
           UserInfo voter = userInfoMapper.selectById(lendItem.getInvestUserId());
           String bindCode = voter.getBindCode();
           userAccountMapper.updateAccount(bindCode, new BigDecimal(0), lendItem.getInvestAmount().negate());
           //添加投资人交易流水
           TransFlowBO investTransFlowBO = new TransFlowBO(
                   LendNoUtils.getTransNo(),
                   bindCode,
                   lendItem.getInvestAmount(),
                   TransTypeEnum.INVEST_UNLOCK,
                   "投资放款" + lend.getLendNo() + lend.getTitle()
           );
           transFlowService.saveTransFlow(investTransFlowBO);
//           transFlow = new TransFlow();
//           transFlow.setTransNo(LendNoUtils.getTransNo());
//           transFlow.setTransType(TransTypeEnum.INVEST_UNLOCK.getTransType());
//           transFlow.setTransTypeName(TransTypeEnum.INVEST_UNLOCK.getTransTypeName());
//           transFlow.setTransAmount(new BigDecimal(voteAmount));
//           transFlow.setCreateTime(LocalDateTime.now());
//           transFlow.setUserId(voter.getId());
//           transFlow.setUserName(voter.getName());
//           transFlow.setMemo(lend.getLendNo() + "-投资放款");
       });

       //生成借款人还款计划和投资人回款计划
        this.repaymentPlan(lend);

    }

    /**
     * 还款计划
     * @param lend
     */
    private void repaymentPlan(Lend lend) {
        //还款计划列表
        List<LendReturn> lendReturnList = new ArrayList<>();

        //根据还款时间生成还款计划
        int periods = lend.getPeriod();
        for (int i = 1; i <= periods; i++) {
            LendReturn lendReturn = new LendReturn();
            lendReturn.setReturnNo(LendNoUtils.getReturnNo());
            lendReturn.setLendId(lend.getId());
            lendReturn.setBorrowInfoId(lend.getBorrowInfoId());
            lendReturn.setUserId(lend.getUserId());
            lendReturn.setAmount(lend.getAmount());
            lendReturn.setBaseAmount(lend.getInvestAmount());
            lendReturn.setLendYearRate(lend.getLendYearRate());
            lendReturn.setReturnMethod(lend.getReturnMethod());
            lendReturn.setCurrentPeriod(i);
            //应还本金
            //lendReturn.setPrincipal();
            //应还利息
            //lendReturn.setInterest();
            //应还本息
            //lendReturn.setTotal();
            lendReturn.setFee(new BigDecimal(0));
            lendReturn.setReturnDate(lend.getLendStartDate().plusMonths(i));//第二个月开始还款
            lendReturn.setOverdue(false);

            if (i == periods) {
                //最后一个月还剩下的全部
                lendReturn.setLast(true);
            }else {
                lendReturn.setLast(false);
            }
            lendReturn.setStatus(0);
            lendReturnList.add(lendReturn);
        }
        lendReturnService.saveBatch(lendReturnList);

        //获取lendReturnList中还款期数与还款计划id对应的map
        Map<Integer, Long> lendReturnMap = lendReturnList.stream().collect(
                Collectors.toMap(LendReturn::getCurrentPeriod, LendReturn::getId)
        );
        //获取所有投资者，生成回款计划
        //回款计划列表
        List<LendItemReturn> lendItemReturnAllList = new ArrayList<>();
        //获取投资成功的投资记录
        List<LendItem> lendItemList = lendItemService.selectByLendId(lend.getId(), 1);

        for (LendItem lendItem:lendItemList) {
            //创建回款计划列表
            List<LendItemReturn> returnInvest = this.returnInvest(lendItem.getId(), lendReturnMap, lend);
            lendItemReturnAllList.addAll(returnInvest);
        }

        //遍历还款记录列表
        for (LendReturn lendReturn : lendReturnList) {

            //通过filter、map、reduce将相关期数的回款数据过滤出来
            //将当前期数的所有投资人的数据相加，就是当前期数的所有投资人的回款数据（本金、利息、总金额）
            BigDecimal sumPrincipal = lendItemReturnAllList.stream()
                    .filter(item -> item.getLendReturnId().longValue() == lendReturn.getId().longValue())
                    .map(LendItemReturn::getPrincipal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal sumInterest = lendItemReturnAllList.stream()
                    .filter(item -> item.getLendReturnId().longValue() == lendReturn.getId().longValue())
                    .map(LendItemReturn::getInterest)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal sumTotal = lendItemReturnAllList.stream()
                    .filter(item -> item.getLendReturnId().longValue() == lendReturn.getId().longValue())
                    .map(LendItemReturn::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);


            //将计算出的数据填充入还款计划记录：设置本金、利息、总金额
            lendReturn.setPrincipal(sumPrincipal);
            lendReturn.setInterest(sumInterest);
            lendReturn.setTotal(sumTotal);
        }
        //批量更新还款计划列表
        lendReturnService.updateBatchById(lendReturnList);
    }

    /**
     * 回款计划
     * @param lendItemId
     * @param lendReturnMap
     * @param lend
     * @return
     */
    private List<LendItemReturn> returnInvest(Long lendItemId, Map<Integer, Long> lendReturnMap, Lend lend) {
        //获取当前投资记录信息
        LendItem lendItem = lendItemService.getById(lendItemId);

        //调用工具类计算还款本金和利息，存储为集合
        // {key：value}
        // {期数：本金|利息}
        BigDecimal amount = lendItem.getInvestAmount();
        BigDecimal yearRate = lendItem.getLendYearRate();
        Integer totalMonth = lend.getPeriod();

        Map<Integer, BigDecimal> mapInterest = null;  //还款期数 -> 利息
        Map<Integer, BigDecimal> mapPrincipal = null; //还款期数 -> 本金

        //根据还款方式计算本金和利息
        if (lend.getReturnMethod().intValue() == ReturnMethodEnum.ONE.getMethod()) {
            //利息
            mapInterest = Amount1Helper.getPerMonthInterest(amount, yearRate, totalMonth);
            //本金
            mapPrincipal = Amount1Helper.getPerMonthPrincipal(amount, yearRate, totalMonth);
        } else if (lend.getReturnMethod().intValue() == ReturnMethodEnum.TWO.getMethod()) {
            mapInterest = Amount2Helper.getPerMonthInterest(amount, yearRate, totalMonth);
            mapPrincipal = Amount2Helper.getPerMonthPrincipal(amount, yearRate, totalMonth);
        } else if (lend.getReturnMethod().intValue() == ReturnMethodEnum.THREE.getMethod()) {
            mapInterest = Amount3Helper.getPerMonthInterest(amount, yearRate, totalMonth);
            mapPrincipal = Amount3Helper.getPerMonthPrincipal(amount, yearRate, totalMonth);
        } else {
            mapInterest = Amount4Helper.getPerMonthInterest(amount, yearRate, totalMonth);
            mapPrincipal = Amount4Helper.getPerMonthPrincipal(amount, yearRate, totalMonth);
        }

        //创建回款计划列表
        List<LendItemReturn> lendItemReturnList = new ArrayList<>();

        for (Map.Entry<Integer, BigDecimal> entry : mapInterest.entrySet()) {
            Integer currentPeriod = entry.getKey();//当前期数
            // 根据当前期数，获取还款计划的id
            Long lendReturnId = lendReturnMap.get(currentPeriod);

            //创建回款计划记录
            LendItemReturn lendItemReturn = new LendItemReturn();
            //将还款记录关联到回款记录
            lendItemReturn.setLendReturnId(lendReturnId);
            //设置回款记录的基本属性
            lendItemReturn.setLendItemId(lendItemId);
            lendItemReturn.setInvestUserId(lendItem.getInvestUserId());
            lendItemReturn.setLendId(lendItem.getLendId());
            lendItemReturn.setInvestAmount(lendItem.getInvestAmount());
            lendItemReturn.setLendYearRate(lend.getLendYearRate());
            lendItemReturn.setCurrentPeriod(currentPeriod);
            lendItemReturn.setReturnMethod(lend.getReturnMethod());

            //计算回款本金、利息和总额（注意最后一个月的计算）
            if(currentPeriod.intValue() == lend.getPeriod().intValue()){//最后一期
                //本金
                BigDecimal sumPrincipal = lendItemReturnList.stream()
                        .map(LendItemReturn::getPrincipal)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal lastPrincipal = lendItem.getInvestAmount().subtract(sumPrincipal);
                lendItemReturn.setPrincipal(lastPrincipal);

                //利息
                BigDecimal sumInterest = lendItemReturnList.stream()
                        .map(LendItemReturn::getInterest)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal lastInterest = lendItem.getExpectAmount().subtract(sumInterest);
                lendItemReturn.setInterest(lastInterest);

            }else{//非最后一期
                //本金
                lendItemReturn.setPrincipal(mapPrincipal.get(currentPeriod));
                //利息
                lendItemReturn.setInterest(mapInterest.get(currentPeriod));
            }

            //回款总金额
            lendItemReturn.setTotal(lendItemReturn.getPrincipal().add(lendItemReturn.getInterest()));

            //设置回款状态和是否逾期等其他属性
            lendItemReturn.setFee(new BigDecimal("0"));
            lendItemReturn.setReturnDate(lend.getLendStartDate().plusMonths(currentPeriod));
            lendItemReturn.setOverdue(false);
            lendItemReturn.setStatus(0);

            //将回款记录放入回款列表
            lendItemReturnList.add(lendItemReturn);
        }

        //批量保存
        lendItemReturnService.saveBatch(lendItemReturnList);

        return lendItemReturnList;

    }
}
