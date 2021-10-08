package com.cloud.srb.core.controller.admin;


import com.cloud.common.exception.Assert;
import com.cloud.common.exception.BusinessException;
import com.cloud.common.result.ResponseEnum;
import com.cloud.common.result.Result;
import com.cloud.srb.core.pojo.entity.IntegralGrade;
import com.cloud.srb.core.service.IntegralGradeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 积分等级表 前端控制器
 * </p>
 *
 * @author xsshuai
 * @since 2021-04-24
 */
@Api(tags = "积分等级管理")
//@CrossOrigin
@RestController
@RequestMapping("admin/core/integralGrade")
@Slf4j
public class AdminIntegralGradeController {

    @Resource
    private IntegralGradeService integralGradeService;

    /**
     * 积分等级列表展示
     * @return
     */
    @ApiOperation("积分等级列表")
    @GetMapping("/list")
    public Result listAll() {
        List<IntegralGrade> integralGrades = integralGradeService.list();
        return Result.sucess().data("integralGrades",integralGrades).message("获取列表成功");
    }

    /**
     * 新增积分等级
     * @param integralGrade
     * @return
     */
    @ApiOperation(value = "新增积分等级对象")
    @PostMapping("/save")
    public Result save(
            @ApiParam(value = "积分等级对象",required = true)
            @RequestBody IntegralGrade integralGrade) {
//        if(integralGrade.getBorrowAmount() == null) {
//            throw new BusinessException(ResponseEnum.BORROW_AMOUNT_NULL_ERROR);
//        }
        Assert.notNull(integralGrade,ResponseEnum.BORROW_AMOUNT_NULL_ERROR);
        boolean result = integralGradeService.save(integralGrade);
        if (!result) {
            return Result.error().message("新增积分等级失败");
        }
        return Result.sucess().message("新增积分等级成功").data("integralGrade",integralGrade);
    }

    /**
     * 根据id查询积分等级
     * @param id
     * @return
     */
    @ApiOperation(value = "根据id查询积分等级")
    @GetMapping("/get/{id}")
    public Result getById(
            @ApiParam(value = "积分等级id",required = true)
            @PathVariable Long id) {
        IntegralGrade integralGrade = integralGradeService.getById(id);
        if(integralGrade == null) {
            return Result.error().message("查询失败");
        }
        return Result.sucess().message("查询成功").data("integralGrade",integralGrade);
    }

    /**
     * 根据id更新积分等级
     * @param integralGrade
     * @return
     */
    @ApiOperation(value = "根据id更新积分等级")
    @PutMapping("/update")
    public Result updateById(
            @ApiParam(value = "积分等级对象",required = true)
            @RequestBody IntegralGrade integralGrade) {
        boolean result = integralGradeService.updateById(integralGrade);
        if (!result) {
            return Result.error().message("更新失败");
        }
        return Result.sucess().message("更新成功").data("integralGrade",integralGrade);
    }

    /**
     * 根据id删除积分等级
     * @param id
     * @return
     */
    @ApiOperation(value = "根据id删除积分等级",notes = "逻辑删除")
    @DeleteMapping("/delete/{id}")
    public Result removeById(
            @ApiParam(value = "积分等级id",example = "1",required = true)
            @PathVariable Long id) {
        boolean result = integralGradeService.removeById(id);
        if(!result) {
           return Result.error().message("调用逻辑删除借口失败");
        }
        return Result.sucess().message("删除成功！");
    }
}

