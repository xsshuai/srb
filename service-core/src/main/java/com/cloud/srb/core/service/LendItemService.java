package com.cloud.srb.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.srb.core.pojo.entity.LendItem;
import com.cloud.srb.core.pojo.vo.InvestVO;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 标的出借记录表 服务类
 * </p>
 *
 * @author xsshuai
 * @since 2021-04-24
 */
public interface LendItemService extends IService<LendItem> {

    String commitInvest(InvestVO investVO);

    String notify(Map<String, Object> paramMap);

    List<LendItem> selectByLendId(Long lendId, Integer status);

    List<LendItem> selectByLendId(Long lendId);

    LendItem selectByReturnNo(String returnNo);
}
