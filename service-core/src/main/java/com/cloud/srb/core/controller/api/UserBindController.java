package com.cloud.srb.core.controller.api;


import com.alibaba.fastjson.JSON;
import com.cloud.common.result.Result;
import com.cloud.srb.base.util.JwtUtils;
import com.cloud.srb.core.hfb.RequestHelper;
import com.cloud.srb.core.pojo.vo.UserBindVO;
import com.cloud.srb.core.service.UserBindService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * <p>
 * 用户绑定表 前端控制器
 * </p>
 *
 * @author xsshuai
 * @since 2021-04-24
 */
@Api(tags = "会员账号绑定")
@RestController
@RequestMapping("/api/core/userBind")
@Slf4j
public class UserBindController {

    @Resource
    private UserBindService userBindService;

    @ApiOperation("账户绑定提交数据")
    @PostMapping("/auth/bind")
    public Result bind(@RequestBody UserBindVO userBindVO, HttpServletRequest request) {
        //从head中获取token，并对token进行校验，确保用户已登录，并从token中提取userId
        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);

        //根据userId做账户绑定,生成动态表单的字符串
        String formStr = userBindService.commitBindUser(userBindVO, userId);
        return Result.sucess().data("formStr", formStr);
    }

    @ApiOperation("账户绑定异步回调")
    @PostMapping("/notify")
    public String notify(HttpServletRequest request) {

        //汇付宝向尚融宝发起回调请求所携带参数
        Map<String, Object> paramMap = RequestHelper.switchMap(request.getParameterMap());
        log.info("账户绑定异步回调接收的参数：" + JSON.toJSONString(paramMap));

        //签名校验
        if (!RequestHelper.isSignEquals(paramMap)) {
            log.info("账户绑定异步回调签名校验失败：" + JSON.toJSONString(paramMap));
            return "fail";
        }
        log.info("账户绑定异步回调签名校验成功，开始用户信息绑定。。。。。。。");

        userBindService.notify(paramMap);
        return "success";
    }
}

