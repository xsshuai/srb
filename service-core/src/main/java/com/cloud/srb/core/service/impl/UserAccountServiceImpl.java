package com.cloud.srb.core.service.impl;

import com.cloud.srb.core.pojo.entity.UserAccount;
import com.cloud.srb.core.mapper.UserAccountMapper;
import com.cloud.srb.core.service.UserAccountService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

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

}
