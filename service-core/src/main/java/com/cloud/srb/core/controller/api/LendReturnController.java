package com.cloud.srb.core.controller.api;


import com.alibaba.fastjson.JSON;
import com.cloud.common.result.Result;
import com.cloud.srb.base.util.JwtUtils;
import com.cloud.srb.core.enums.NotifyStatusEnum;
import com.cloud.srb.core.hfb.RequestHelper;
import com.cloud.srb.core.pojo.entity.LendReturn;
import com.cloud.srb.core.service.LendReturnService;
import io.swagger.annotations.Api;
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
 * 还款记录表 前端控制器
 * </p>
 *
 * @author xsshuai
 * @since 2021-04-24
 */
@Api(tags = "还款记录")
@RestController
@RequestMapping("api/core/lendReturn")
@Slf4j
public class LendReturnController {

    @Resource
    private LendReturnService lendReturnService;

    @ApiOperation("获取还款列表")
    @GetMapping("/list/{lendId}")
    public Result list(
            @ApiParam(value = "标的id", required = true)
            @PathVariable Long lendId) {
        List<LendReturn> list = lendReturnService.selectByLendId(lendId);
        return Result.sucess().data("list", list);
    }

    @ApiOperation("用户还款")
    @PostMapping("auth/commitReturn/{lendReturnId}")
    public Result commitReturn(
            @ApiParam(value = "id", required = true)
            @PathVariable Long lendReturnId, HttpServletRequest request) {
        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);
        String formStr = lendReturnService.commitReturn(lendReturnId, userId);
        return Result.sucess().data("formStr", formStr);
    }

    public String notifyReturn(HttpServletRequest request) {
        Map<String, Object> paramMap = RequestHelper.switchMap(request.getParameterMap());
        log.info("用户充值异步回调：" + JSON.toJSONString(paramMap));

        //验证签名
        if (RequestHelper.isSignEquals(paramMap)) {
            //判断充值是否成功
            if (paramMap.get("resultCode").equals(NotifyStatusEnum.NOTIFY_SUCCESS.getStatus())) {
                //同步账户数据
                return lendReturnService.notifyReturn(paramMap);
            }else {
                return "success";
            }
        }else {
            return "fail";
        }
    }
}

