package com.cloud.srb.core.controller.admin;


import com.cloud.common.result.Result;
import com.cloud.srb.core.pojo.entity.Lend;
import com.cloud.srb.core.service.LendService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 标的准备表 前端控制器
 * </p>
 *
 * @author xsshuai
 * @since 2021-04-24
 */
@Api(tags = "标的管理")
@RestController
@RequestMapping("/admin/core/lend")
@Slf4j
public class AdminLendController {

    @Resource
    private LendService lendService;

    @ApiOperation("标的列表")
    @GetMapping("/list")
    public Result list() {
        List<Lend> lendList = lendService.selectList();
        return Result.sucess().data("list", lendList);
    }

    @ApiOperation("标的详情")
    @GetMapping("/show/{id}")
    public Result show(
            @ApiParam(value = "标的id")
            @PathVariable Long id) {
        Map<String, Object> result = lendService.getLendDetail(id);
        return Result.sucess().data("lendDetail", result);
    }
    @ApiOperation("标的放款")
    @GetMapping("/makeLoan/{id}")
    public Result makeLoan(
            @ApiParam(value = "标的id", required = true)
            @PathVariable Long id) {
        lendService.makeLoan(id);
        return Result.sucess().message("放款成功");
    }
}

