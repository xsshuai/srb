package com.cloud.srb.core.controller.api;


import com.alibaba.fastjson.JSON;
import com.cloud.common.result.Result;
import com.cloud.srb.base.util.JwtUtils;
import com.cloud.srb.core.enums.NotifyStatusEnum;
import com.cloud.srb.core.hfb.RequestHelper;
import com.cloud.srb.core.service.UserAccountService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Map;

/**
 * <p>
 * 用户账户 前端控制器
 * </p>
 *
 * @author xsshuai
 * @since 2021-04-24
 */
@Api(tags = "会员账户")
@RestController
@RequestMapping("/api/core/userAccount")
@Slf4j
public class UserAccountController {

    @Resource
    private UserAccountService userAccountService;

    @ApiOperation("充值")
    @PostMapping("/auth/commitCharge/{chargeAmt}")
    public Result commitCharge(
            @ApiParam(value = "充值金额", required = true)
            @PathVariable BigDecimal chargeAmt, HttpServletRequest request) {
        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);
        String formStr = userAccountService.commitCharge(chargeAmt, userId);
        return Result.sucess().data("formStr", formStr);
    }

    @ApiOperation(value = "用户充值异步回调")
    @PostMapping("/notify")
    public String notify(HttpServletRequest request) {
        Map<String, Object> paramMap = RequestHelper.switchMap(request.getParameterMap());
        log.info("用户充值异步回调：" + JSON.toJSONString(paramMap));

        //验证签名
        if (RequestHelper.isSignEquals(paramMap)) {
            //判断充值是否成功
            if (paramMap.get("resultCode").equals(NotifyStatusEnum.NOTIFY_SUCCESS.getStatus())) {
                //同步账户数据
                return userAccountService.notify(paramMap);
            }else {
                return "success";
            }
        }else {
            return "fail";
        }
    }

    @ApiOperation("账户余额查询")
    @GetMapping("/auth/getAccount")
    public Result getAccount(HttpServletRequest request) {
        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);
        BigDecimal account = userAccountService.getAccount(userId);
        return Result.sucess().data("account", account);
    }

    @ApiOperation("提现")
    @PostMapping("/auth/commitWithdraw/{fetchAmt}")
    public Result commitWithdraw(
            @ApiParam(value = "提现金额",required = true)
            @PathVariable BigDecimal fetchAmt, HttpServletRequest request) {
        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);
        String formStr = userAccountService.commitWithdraw(fetchAmt, userId);
        return Result.sucess().data("formStr", formStr);
    }

    @ApiOperation(value = "用户充值异步回调")
    @PostMapping("/notifyWithdraw")
    public String notifyWithdraw(HttpServletRequest request) {
        Map<String, Object> paramMap = RequestHelper.switchMap(request.getParameterMap());
        log.info("用户充值异步回调：" + JSON.toJSONString(paramMap));

        //验证签名
        if (RequestHelper.isSignEquals(paramMap)) {
            //判断充值是否成功
            if (paramMap.get("resultCode").equals(NotifyStatusEnum.NOTIFY_SUCCESS.getStatus())) {
                //同步账户数据
                return userAccountService.notifyWithdraw(paramMap);
            }else {
                return "success";
            }
        }else {
            return "fail";
        }
    }
}

