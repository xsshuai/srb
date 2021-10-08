package com.cloud.srb.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloud.common.exception.Assert;
import com.cloud.common.result.ResponseEnum;
import com.cloud.srb.core.enums.BorrowInfoStatusEnum;
import com.cloud.srb.core.enums.BorrowerStatusEnum;
import com.cloud.srb.core.enums.UserBindEnum;
import com.cloud.srb.core.enums.UserInfoStatusEnum;
import com.cloud.srb.core.mapper.*;
import com.cloud.srb.core.pojo.entity.*;
import com.cloud.srb.core.pojo.vo.BorrowInfoApprovalVO;
import com.cloud.srb.core.pojo.vo.BorrowerApprovalVO;
import com.cloud.srb.core.pojo.vo.BorrowerDetailVO;
import com.cloud.srb.core.service.BorrowInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.srb.core.service.BorrowerService;
import com.cloud.srb.core.service.DictService;
import com.cloud.srb.core.service.LendService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 借款信息表 服务实现类
 * </p>
 *
 * @author xsshuai
 * @since 2021-04-24
 */
@Service
public class BorrowInfoServiceImpl extends ServiceImpl<BorrowInfoMapper, BorrowInfo> implements BorrowInfoService {

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private UserIntegralMapper userIntegralMapper;

    @Resource
    private IntegralGradeMapper integralGradeMapper;

    @Resource
    private BorrowerMapper borrowerMapper;

    @Resource
    private DictService dictService;

    @Resource
    private BorrowerService borrowerService;

    @Resource
    private LendService lendService;

    @Override
    public BigDecimal getBorrowAmount(Long userId) {
        UserInfo userInfo = userInfoMapper.selectById(userId);
        QueryWrapper<IntegralGrade> integralGradeQueryWrapper = new QueryWrapper<>();
        integralGradeQueryWrapper
                .le("integral_start", userInfo.getIntegral())
                .ge("integral_end", userInfo.getIntegral());
        IntegralGrade integralGrade = integralGradeMapper.selectOne(integralGradeQueryWrapper);
        if (integralGrade == null) {
            return new BigDecimal(0);
        }
        QueryWrapper<BorrowInfo> borrowInfoQueryWrapper = new QueryWrapper<>();
        borrowInfoQueryWrapper.eq("user_id", userId);
        List<BorrowInfo> borrowInfoList = baseMapper.selectList(borrowInfoQueryWrapper);
        BigDecimal nowBorrowAmount = integralGrade.getBorrowAmount();
        if (borrowInfoList != null) {
            for (BorrowInfo borrowInfo : borrowInfoList) {
                nowBorrowAmount = nowBorrowAmount.subtract(borrowInfo.getAmount());
            }
        }
        return nowBorrowAmount;
    }

    @Override
    public void saveBorrowInfo(BorrowInfo borrowInfo, Long userId) {

        UserInfo userInfo = userInfoMapper.selectById(userId);
        //判断用户是否锁定
        Assert.isTrue(userInfo.getStatus() == UserInfoStatusEnum.NOMAL.getStatus().intValue(),
                ResponseEnum.LOGIN_LOKED_ERROR);
        //判断用户绑定状态
        Assert.isTrue(userInfo.getBindStatus() == UserBindEnum.BIND_OK.getStatus().intValue(),
                ResponseEnum.USER_NO_BIND_ERROR);
        //判断借款人额度审批状态
        Assert.isTrue(userInfo.getBorrowAuthStatus() == BorrowerStatusEnum.AUTH_OK.getStatus().intValue(),
                ResponseEnum.USER_NO_AMOUNT_ERROR);
        //判断借款人额度是否充足
        Assert.isTrue(this.getBorrowAmount(userId).compareTo(borrowInfo.getAmount()) > -1,
                ResponseEnum.USER_AMOUNT_LESS_ERROR);
        //存储借款信息
        borrowInfo.setUserId(userId);
        borrowInfo.setBorrowYearRate(borrowInfo.getBorrowYearRate().divide(new BigDecimal(100)));
        borrowInfo.setStatus(BorrowInfoStatusEnum.CHECK_RUN.getStatus());
        baseMapper.insert(borrowInfo);

    }

    @Override
    public Integer getStatusByUserId(Long userId) {
        return null;
    }

    @Override
    public List<BorrowInfo> selectList() {
        List<BorrowInfo> borrowInfoList = baseMapper.selectBorrowInfoList();
        for (BorrowInfo borrowInfo : borrowInfoList) {
            String returnMethod = dictService.getNameByParentDictCodeAndValue("returnMethod", borrowInfo.getReturnMethod());
            String moneyUse = dictService.getNameByParentDictCodeAndValue("moneyUse", borrowInfo.getMoneyUse());
            String status = BorrowInfoStatusEnum.getMsgByStatus(borrowInfo.getStatus());
            borrowInfo.getParam().put("returnMethod", returnMethod);
            borrowInfo.getParam().put("moneyUse", moneyUse);
            borrowInfo.getParam().put("status", status);
        }
        return borrowInfoList;
    }

    @Override
    public Map<String, Object> getBorrowInfoDetail(Long id) {

        //查询借款对象：borrowInfo
        BorrowInfo borrowInfo = baseMapper.selectById(id);
        String returnMethod = dictService.getNameByParentDictCodeAndValue("returnMethod", borrowInfo.getReturnMethod());
        String moneyUse = dictService.getNameByParentDictCodeAndValue("moneyUse", borrowInfo.getMoneyUse());
        String status = BorrowInfoStatusEnum.getMsgByStatus(borrowInfo.getStatus());
        borrowInfo.getParam().put("returnMethod", returnMethod);
        borrowInfo.getParam().put("moneyUse", moneyUse);
        borrowInfo.getParam().put("status", status);

        //查询借款人对象：borrower(borrowDetailVO)
        QueryWrapper<Borrower> borrowerQueryWrapper = new QueryWrapper<>();
        borrowerQueryWrapper.eq("user_id", borrowInfo.getUserId());
        Borrower borrower = borrowerMapper.selectOne(borrowerQueryWrapper);
        BorrowerDetailVO borrowerDetailVO= borrowerService.getBorrowerDetailVOById(borrower.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("borrowInfo", borrowInfo);
        result.put("borrower", borrowerDetailVO);
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void approval(BorrowInfoApprovalVO borrowInfoApprovalVO) {

        //修改借款信息审批状态
        BorrowInfo borrowInfo = baseMapper.selectById(borrowInfoApprovalVO.getId());
        borrowInfo.setStatus(borrowInfoApprovalVO.getStatus());
        baseMapper.updateById(borrowInfo);
        //如果审核通过，生成新的标的记录
        if (borrowInfoApprovalVO.getStatus().intValue() == BorrowInfoStatusEnum.CHECK_OK.getStatus().intValue() ) {
            //创建标的
            lendService.create(borrowInfoApprovalVO, borrowInfo);
        }
    }
}
