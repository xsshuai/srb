package com.cloud.srb.core.service;

import com.cloud.srb.core.pojo.entity.UserInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.srb.core.pojo.vo.RegisterVO;

/**
 * <p>
 * 用户基本信息 服务类
 * </p>
 *
 * @author xsshuai
 * @since 2021-04-24
 */
public interface UserInfoService extends IService<UserInfo> {

    void register(RegisterVO registerVO);
}
