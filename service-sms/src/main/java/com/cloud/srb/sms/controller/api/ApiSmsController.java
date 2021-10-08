package com.cloud.srb.sms.controller.api;

import com.cloud.common.exception.Assert;
import com.cloud.common.result.ResponseEnum;
import com.cloud.common.result.Result;
import com.cloud.common.util.RandomUtils;
import com.cloud.common.util.RegexValidateUtils;
import com.cloud.srb.sms.client.CoreUserInfoClient;
import com.cloud.srb.sms.service.SmsService;
import com.cloud.srb.sms.util.SmsProperties;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName ApiSmsController
 * @Author xsshuai
 * @Date 2021/5/4 1:17 下午
 **/
@RestController
@RequestMapping("/api/sms")
@Api(tags = "短信管理")
//@CrossOrigin
@Slf4j
public class ApiSmsController {

    @Resource
    private SmsService smsService;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private CoreUserInfoClient coreUserInfoClient;

    @ApiOperation("获取验证码")
    @GetMapping("/send/{mobile}")
    public Result send(
            @ApiParam(value = "手机号",required = true)
            @PathVariable String mobile) {

        //校验手机号码不能为空
        Assert.notEmpty(mobile, ResponseEnum.MOBILE_NULL_ERROR);

        //校验手机号码的合法性
        Assert.isTrue(RegexValidateUtils.checkCellphone(mobile),ResponseEnum.MOBILE_ERROR);

        //判断手机号码是否注册
        boolean result = coreUserInfoClient.checkMobile(mobile);
        Assert.isTrue(result == false, ResponseEnum.MOBILE_EXIST_ERROR);

        String code = RandomUtils.getSixBitRandom();
        HashMap<String, Object> map = new HashMap<>();
        map.put("code",code);
        //smsService.send(mobile, SmsProperties.TEMPLATE_CODE, map);

        //将验证码存入redis
        redisTemplate.opsForValue().set("srb:sms:code:" + mobile, code, 5, TimeUnit.MINUTES);
        return Result.sucess().message("短信发送成功");
    }
}
