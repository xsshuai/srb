package com.cloud.srb.core.controller.api;


import com.alibaba.fastjson.JSON;
import com.cloud.common.result.Result;
import com.cloud.srb.base.util.JwtUtils;
import com.cloud.srb.core.enums.NotifyStatusEnum;
import com.cloud.srb.core.hfb.RequestHelper;
import com.cloud.srb.core.pojo.entity.LendItem;
import com.cloud.srb.core.pojo.vo.InvestVO;
import com.cloud.srb.core.service.LendItemService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 标的出借记录表 前端控制器
 * </p>
 *
 * @author xsshuai
 * @since 2021-04-24
 */
@RestController
@RequestMapping("/api/core/lendItem")
@Slf4j
public class LendItemController {

    @Resource
    private LendItemService lendItemService;

    @ApiOperation("会员投资提交")
    @PostMapping("/auth/commitInvest")
    public Result commitInvest(@RequestBody InvestVO investVO, HttpServletRequest request) {
        String token = request.getHeader("token");
        investVO.setInvestUserId(JwtUtils.getUserId(token));
        investVO.setInvestUserName(JwtUtils.getUserName(token));
        String formStr = lendItemService.commitInvest(investVO);
        return Result.sucess().data("formStr", formStr);
    }

    @ApiOperation("会员投资异步回调")
    @PostMapping("/notify")
    public String notify(HttpServletRequest request) {
        Map<String, Object> paramMap = RequestHelper.switchMap(request.getParameterMap());
        log.info("用户投资异步回调：" + JSON.toJSONString(paramMap));

        //校验签名
        if (RequestHelper.isSignEquals(paramMap)) {
            //判断调用是否成功
            if (paramMap.get("resultCode").equals(NotifyStatusEnum.NOTIFY_SUCCESS.getStatus())) {
                return lendItemService.notify(paramMap);
            }else {
                return "success";
            }
        }else {
            return "fail";
        }
    }

    @ApiOperation("获取投资列表")
    @GetMapping("/list/{lendId}")
    public Result list(
            @ApiParam(value = "标的id", required = true)
            @PathVariable Long lendId) {
        List<LendItem> list = lendItemService.selectByLendId(lendId);
        return Result.sucess().data("list", list);
    }
}

