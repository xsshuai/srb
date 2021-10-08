package com.cloud.srb.core.controller.admin;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.common.result.Result;
import com.cloud.srb.base.util.JwtUtils;
import com.cloud.srb.core.pojo.entity.Borrower;
import com.cloud.srb.core.pojo.vo.BorrowerApprovalVO;
import com.cloud.srb.core.pojo.vo.BorrowerDetailVO;
import com.cloud.srb.core.pojo.vo.BorrowerVO;
import com.cloud.srb.core.service.BorrowerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 借款人 前端控制器
 * </p>
 *
 * @author xsshuai
 * @since 2021-04-24
 */
@Api(tags = "借款人管理")
@RestController
@RequestMapping("/admin/core/borrower")
@Slf4j
public class AdminBorrowerController {


    @Resource
    private BorrowerService borrowerService;


    @ApiOperation("借款人分页展示")
    @GetMapping("/list/{page}/{limit}")
    public Result listPage(
            @ApiParam(value = "当前页码", required = true)
            @PathVariable Long page,

            @ApiParam(value = "每页记录数", required = true)
            @PathVariable Long limit,

            @ApiParam(value = "查询关键字", required = false)
            @RequestParam String keyWord) {

        Page<Borrower> pageParam = new Page<>(page, limit);
        IPage<Borrower> pageModel = borrowerService.listPage(pageParam, keyWord);
        return Result.sucess().data("pageModel", pageModel);
    }

    @ApiOperation("获取借款人信息")
    @GetMapping("/show/{id}")
    public Result show(
            @ApiParam(value = "借款人id", required = true)
            @PathVariable Long id) {

        BorrowerDetailVO borrowerDetailVO = borrowerService.getBorrowerDetailVOById(id);
        return Result.sucess().data("borrowerDetailVO", borrowerDetailVO);
    }

    @ApiOperation("借款额度审批")
    @PostMapping("/approval")
    public Result approval(@RequestBody BorrowerApprovalVO borrowerApprovalVO) {

        borrowerService.approval(borrowerApprovalVO);
        return Result.sucess().message("审批完成");
    }

}

