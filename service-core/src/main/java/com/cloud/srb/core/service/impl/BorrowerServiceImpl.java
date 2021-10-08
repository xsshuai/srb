package com.cloud.srb.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.srb.core.enums.BorrowerStatusEnum;
import com.cloud.srb.core.enums.IntegralEnum;
import com.cloud.srb.core.mapper.BorrowerAttachMapper;
import com.cloud.srb.core.mapper.BorrowerMapper;
import com.cloud.srb.core.mapper.UserInfoMapper;
import com.cloud.srb.core.mapper.UserIntegralMapper;
import com.cloud.srb.core.pojo.entity.Borrower;
import com.cloud.srb.core.pojo.entity.BorrowerAttach;
import com.cloud.srb.core.pojo.entity.UserInfo;
import com.cloud.srb.core.pojo.entity.UserIntegral;
import com.cloud.srb.core.pojo.vo.BorrowerApprovalVO;
import com.cloud.srb.core.pojo.vo.BorrowerDetailVO;
import com.cloud.srb.core.pojo.vo.BorrowerVO;
import com.cloud.srb.core.service.BorrowerAttachService;
import com.cloud.srb.core.service.BorrowerService;
import com.cloud.srb.core.service.DictService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 借款人 服务实现类
 * </p>
 *
 * @author xsshuai
 * @since 2021-04-24
 */
@Service
public class BorrowerServiceImpl extends ServiceImpl<BorrowerMapper, Borrower> implements BorrowerService {

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private BorrowerAttachMapper borrowerAttachMapper;

    @Resource
    private UserIntegralMapper userIntegralMapper;

    @Resource
    private DictService dictService;

    @Resource
    private BorrowerAttachService borrowerAttachService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveBorrowerVOByUserId(BorrowerVO borrowerVO, Long userId) {

        //获取用户基本信息
        UserInfo userInfo = userInfoMapper.selectById(userId);

        //保存借款人信息
        Borrower borrower = new Borrower();
        BeanUtils.copyProperties(borrowerVO, borrower);
        borrower.setUserId(userId);
        borrower.setName(userInfo.getName());
        borrower.setIdCard(userInfo.getIdCard());
        borrower.setMobile(userInfo.getMobile());
        borrower.setStatus(BorrowerStatusEnum.AUTH_RUN.getStatus());
        baseMapper.insert(borrower);

        //保存附件
        List<BorrowerAttach> borrowerAttachList = borrowerVO.getBorrowerAttachList();
        borrowerAttachList.forEach(borrowerAttach -> {
            borrowerAttach.setBorrowerId(borrower.getId());
            borrowerAttachMapper.insert(borrowerAttach);
        });

        //更新UserInfo中借款人认证状态
        userInfo.setBorrowAuthStatus(BorrowerStatusEnum.AUTH_RUN.getStatus());
        userInfoMapper.updateById(userInfo);
    }

    @Override
    public Integer getStatusByUserId(Long userId) {
         QueryWrapper<Borrower> queryWrapper = new QueryWrapper<>();
         queryWrapper.select("status").eq("user_id", userId);
        List<Object> objects = baseMapper.selectObjs(queryWrapper);
        if (objects.size() == 0) {
            return BorrowerStatusEnum.NO_AUTH.getStatus();
        }
        return (Integer)objects.get(0);
    }

    @Override
    public IPage<Borrower> listPage(Page<Borrower> pageParam, String keyWord) {
        if (StringUtils.isBlank(keyWord)) {
            return baseMapper.selectPage(pageParam, null);
        }
        QueryWrapper<Borrower> queryWrapper = new QueryWrapper<>();
        queryWrapper
                .like("name", keyWord)
                .or().like("id_card", keyWord)
                .or().like("mobile", keyWord)
                .orderByDesc("id");
        return baseMapper.selectPage(pageParam, queryWrapper);
    }

    @Override
    public BorrowerDetailVO getBorrowerDetailVOById(Long id) {

        //获取借款人信息
        Borrower borrower = baseMapper.selectById(id);

        //填充借款人信息
        BorrowerDetailVO borrowerDetailVO = new BorrowerDetailVO();
        BeanUtils.copyProperties(borrower, borrowerDetailVO);

        //婚否
        borrowerDetailVO.setMarry(borrower.getMarry()? "是":"否");
        //男女
        borrowerDetailVO.setSex(borrower.getSex() == 1 ? "男":"女");
        //下拉列表
        borrowerDetailVO.setEducation(dictService.getNameByParentDictCodeAndValue("education", borrower.getEducation()));
        borrowerDetailVO.setIndustry(dictService.getNameByParentDictCodeAndValue("industry", borrower.getIndustry()));
        borrowerDetailVO.setIncome(dictService.getNameByParentDictCodeAndValue("income", borrower.getIncome()));
        borrowerDetailVO.setReturnSource(dictService.getNameByParentDictCodeAndValue("returnSource", borrower.getReturnSource()));
        borrowerDetailVO.setContactsRelation(dictService.getNameByParentDictCodeAndValue("relation", borrower.getContactsRelation()));
        //审批状态
        borrowerDetailVO.setStatus(BorrowerStatusEnum.getMsgByStatus(borrower.getStatus()));
        //附件
        borrowerDetailVO.setBorrowerAttachVOList(borrowerAttachService.selectBorrowerAttachVOList(id));
        return borrowerDetailVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approval(BorrowerApprovalVO borrowerApprovalVO) {

        Borrower borrower = baseMapper.selectById(borrowerApprovalVO.getBorrowerId());
        borrower.setStatus(borrowerApprovalVO.getStatus());
        baseMapper.updateById(borrower);

        //积分计算
        UserIntegral userIntegral = new UserIntegral();
        userIntegral.setUserId(borrower.getUserId());
        userIntegral.setIntegral(borrowerApprovalVO.getInfoIntegral());
        userIntegral.setContent("借款人基本信息积分");
        userIntegralMapper.insert(userIntegral);
        Integer addIntegral = borrowerApprovalVO.getInfoIntegral();

        //身份信息积分
        if (borrowerApprovalVO.getIsIdCardOk()) {
            userIntegral = new UserIntegral();
            userIntegral.setUserId(borrower.getUserId());
            userIntegral.setIntegral(IntegralEnum.BORROWER_IDCARD.getIntegral());
            userIntegral.setContent(IntegralEnum.BORROWER_IDCARD.getMsg());
            userIntegralMapper.insert(userIntegral);
            addIntegral += IntegralEnum.BORROWER_IDCARD.getIntegral();
        }
        //房产信息积分
        if (borrowerApprovalVO.getIsHouseOk()) {
            userIntegral = new UserIntegral();
            userIntegral.setUserId(borrower.getUserId());
            userIntegral.setIntegral(IntegralEnum.BORROWER_HOUSE.getIntegral());
            userIntegral.setContent(IntegralEnum.BORROWER_HOUSE.getMsg());
            userIntegralMapper.insert(userIntegral);
            addIntegral += IntegralEnum.BORROWER_HOUSE.getIntegral();
        }
        //车辆信息积分
        if (borrowerApprovalVO.getIsCarOk()) {
            userIntegral = new UserIntegral();
            userIntegral.setUserId(borrower.getUserId());
            userIntegral.setIntegral(IntegralEnum.BORROWER_CAR.getIntegral());
            userIntegral.setContent(IntegralEnum.BORROWER_CAR.getMsg());
            userIntegralMapper.insert(userIntegral);
            addIntegral += IntegralEnum.BORROWER_CAR.getIntegral();
        }

        //用户表积分信息更新
        UserInfo userInfo = userInfoMapper.selectById(borrower.getUserId());
        userInfo.setIntegral(userInfo.getIntegral() + addIntegral);

        //用户表审核状态更新
        userInfo.setBorrowAuthStatus(borrowerApprovalVO.getStatus());
        userInfoMapper.updateById(userInfo);
    }
}
