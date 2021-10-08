package com.cloud.srb.core.controller.admin;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.common.result.Result;
import com.cloud.srb.core.pojo.entity.UserInfo;
import com.cloud.srb.core.pojo.query.UserInfoQuery;
import com.cloud.srb.core.service.UserInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
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
@RequestMapping("/admin/core/userInfo")
@Slf4j
//@CrossOrigin
public class AdminUserInfoController {

    @Resource
    private UserInfoService userInfoService;

    @ApiOperation("获取会员分页列表")
    @GetMapping("/list/{page}/{limit}")
    public Result listPage(
                @ApiParam(value = "当前页码", required = true)
                @PathVariable Long page,
                @ApiParam(value = "每页记录数", required = true)
                @PathVariable Long limit,
                @ApiParam(value = "查询对象")
                UserInfoQuery userInfoQuery
                           ) {
        Page<UserInfo> pageParam = new Page<>(page, limit);
        IPage<UserInfo> pageModel = userInfoService.listPage(pageParam, userInfoQuery);
        return Result.sucess().data("pageModel", pageModel);

    }

    @ApiOperation("用户锁定和解锁")
    @PutMapping("/lock/{id}/{status}")
    public Result lock(
            @ApiParam(value = "用户id", required = true)
            @PathVariable Long id,
            @ApiParam(value = "用户状态", required = true)
            @PathVariable Integer status){
        userInfoService.lock(id, status);

        return Result.sucess().message(status == 1?"用户解锁成功!":"用户锁定成功!");
    }
}

