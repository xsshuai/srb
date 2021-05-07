package com.cloud.srb.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloud.common.exception.Assert;
import com.cloud.common.result.ResponseEnum;
import com.cloud.common.util.MD5;
import com.cloud.srb.core.mapper.UserAccountMapper;
import com.cloud.srb.core.pojo.entity.UserAccount;
import com.cloud.srb.core.pojo.entity.UserInfo;
import com.cloud.srb.core.mapper.UserInfoMapper;
import com.cloud.srb.core.pojo.vo.RegisterVO;
import com.cloud.srb.core.service.UserInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * <p>
 * 用户基本信息 服务实现类
 * </p>
 *
 * @author xsshuai
 * @since 2021-04-24
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {

    @Resource
    private UserAccountMapper userAccountMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void register(RegisterVO registerVO) {

        //检查手机号是否已注册
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("mobile", registerVO.getMobile());
        Integer count = baseMapper.selectCount(queryWrapper);
        Assert.isTrue(count == 0, ResponseEnum.MOBILE_EXIST_ERROR);

        //插入用户记录信息
        UserInfo userInfo = new UserInfo();
        userInfo.setMobile(registerVO.getMobile());
        userInfo.setPassword(MD5.encrypt(registerVO.getPassword()));
        userInfo.setName(registerVO.getMobile());
        userInfo.setNickName(registerVO.getMobile());
        userInfo.setUserType(registerVO.getUserType());
        userInfo.setStatus(UserInfo.STATUS_NORMAL);
        userInfo.setHeadImg(UserInfo.USER_AVATAR);
        baseMapper.insert(userInfo);

        //插入用户账户记录
        UserAccount userAccount = new UserAccount();
        userAccount.setUserId(userInfo.getId());
        userAccountMapper.insert(userAccount);
    }
}
