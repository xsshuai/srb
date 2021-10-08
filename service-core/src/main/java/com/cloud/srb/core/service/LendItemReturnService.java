package com.cloud.srb.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.srb.core.pojo.entity.LendItemReturn;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 标的出借回款记录表 服务类
 * </p>
 *
 * @author xsshuai
 * @since 2021-04-24
 */
public interface LendItemReturnService extends IService<LendItemReturn> {

    List<LendItemReturn> selectByLendId(Long lendId, Long userId);
    List<LendItemReturn> selectByLendReturnId(Long lendReturnId);
    List<Map<String, Object>> addReturnDetail(Long lendReturnId);
}
