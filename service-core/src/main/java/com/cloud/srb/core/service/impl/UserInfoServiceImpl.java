package com.cloud.srb.core.service.impl;

import com.cloud.srb.core.pojo.entity.UserInfo;
import com.cloud.srb.core.mapper.UserInfoMapper;
import com.cloud.srb.core.service.UserInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

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

}
