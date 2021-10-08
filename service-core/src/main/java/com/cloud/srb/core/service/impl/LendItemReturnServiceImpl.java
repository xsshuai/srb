package com.cloud.srb.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.srb.core.mapper.LendItemReturnMapper;
import com.cloud.srb.core.mapper.LendMapper;
import com.cloud.srb.core.mapper.LendReturnMapper;
import com.cloud.srb.core.mapper.UserInfoMapper;
import com.cloud.srb.core.pojo.entity.Lend;
import com.cloud.srb.core.pojo.entity.LendItemReturn;
import com.cloud.srb.core.pojo.entity.LendReturn;
import com.cloud.srb.core.pojo.entity.UserInfo;
import com.cloud.srb.core.service.LendItemReturnService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 标的出借回款记录表 服务实现类
 * </p>
 *
 * @author xsshuai
 * @since 2021-04-24
 */
@Service
public class LendItemReturnServiceImpl extends ServiceImpl<LendItemReturnMapper, LendItemReturn> implements LendItemReturnService {

    @Resource
    private LendMapper lendMapper;

    @Resource
    private LendReturnMapper lendReturnMapper;

    @Resource
    private UserInfoMapper userInfoMapper;

    @Override
    public List<LendItemReturn> selectByLendId(Long lendId, Long userId) {
        QueryWrapper<LendItemReturn> lendItemReturnQueryWrapper = new QueryWrapper<>();
        lendItemReturnQueryWrapper
                .eq("invest_user_id", userId)
                .eq("lend_id", lendId)
                .orderByAsc("current_period");
        return baseMapper.selectList(lendItemReturnQueryWrapper);
    }

    @Override
    public List<LendItemReturn> selectByLendReturnId(Long lendReturnId) {
        QueryWrapper<LendItemReturn> lendItemReturnQueryWrapper = new QueryWrapper<>();
        lendItemReturnQueryWrapper.eq("lend_return_id", lendReturnId);
        return baseMapper.selectList(lendItemReturnQueryWrapper);
    }

    @Override
    public List<Map<String, Object>> addReturnDetail(Long lendReturnId) {
        List<LendItemReturn> lendItemReturnList = this.selectByLendReturnId(lendReturnId);
        LendReturn lendReturn = lendReturnMapper.selectById(lendReturnId);
        Lend lend = lendMapper.selectById(lendReturn.getLendId());
        List<Map<String, Object>> dataList = new ArrayList<>();
        for (LendItemReturn item : lendItemReturnList) {
            Map<String, Object> data = new HashMap<>(0);
            data.put("agentProjectCode", lend.getLendNo());
            data.put("voteBillNo", lendReturn.getReturnNo());
            UserInfo userInfo = userInfoMapper.selectById(item.getInvestUserId());
            data.put("toBindCode", userInfo.getBindCode());
            data.put("transitAmt", lend.getLendNo());
            data.put("baseAmt", lend.getLendNo());
            data.put("benifitAmt", lend.getLendNo());
            data.put("feeAmt", lend.getLendNo());
            dataList.add(data);
        }
        return dataList;
    }
}
