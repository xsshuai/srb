package com.cloud.srb.core.controller.api;


import com.cloud.common.exception.Assert;
import com.cloud.common.result.ResponseEnum;
import com.cloud.common.result.Result;
import com.cloud.common.util.RegexValidateUtils;
import com.cloud.srb.core.pojo.vo.RegisterVO;
import com.cloud.srb.core.service.UserInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <p>
 * 用户基本信息 前端控制器
 * </p>
 *
 * @author xsshuai
 * @since 2021-04-24
 */
@Api(tags = "会员接口")
@RestController
@RequestMapping("/api/core/userInfo")
@Slf4j
@CrossOrigin
public class UserInfoController {

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private RedisTemplate redisTemplate;

    @ApiOperation("会员注册")
    @PostMapping("/register")
    public Result register(@RequestBody RegisterVO registerVO) {

        String mobile = registerVO.getMobile();
        String password = registerVO.getPassword();
        String code = registerVO.getCode();


        Assert.notEmpty(mobile, ResponseEnum.MOBILE_NULL_ERROR);
        Assert.notEmpty(password, ResponseEnum.PASSWORD_NULL_ERROR);
        Assert.notEmpty(code, ResponseEnum.CODE_NULL_ERROR);

        Assert.isTrue(RegexValidateUtils.checkTelephone(mobile), ResponseEnum.MOBILE_ERROR);
        //校验验证码
        String codeGen = (String) redisTemplate.opsForValue().get("srb:sms:code:" + mobile);
        Assert.equals(code, codeGen, ResponseEnum.CODE_ERROR);

        //注册
        userInfoService.register(registerVO);
        return Result.sucess().message("注册成功！");
    }
}

