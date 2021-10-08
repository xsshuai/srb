package com.cloud.srb.core.controller.admin;

import com.cloud.common.result.Result;
import com.cloud.srb.core.pojo.entity.BorrowInfo;
import com.cloud.srb.core.pojo.vo.BorrowInfoApprovalVO;
import com.cloud.srb.core.pojo.vo.BorrowerApprovalVO;
import com.cloud.srb.core.service.BorrowInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @ClassName AdminBorrowInfoController
 * @Author xsshuai
 * @Date 2021/8/24 11:08 下午
 **/
@Api(tags = "借款管理")
@RestController
@RequestMapping("/admin/core/borrowInfo")
@Slf4j
public class AdminBorrowInfoController {

    @Resource
    private BorrowInfoService borrowInfoService;

    @ApiOperation("借款信息列表")
    @GetMapping("/list")
    public Result list() {
        List<BorrowInfo> borrowInfoList = borrowInfoService.selectList();
        return Result.sucess().data("list", borrowInfoList);
    }

    @ApiOperation("借款信息详情")
    @GetMapping("/show/{id}")
    public Result show(
            @ApiParam(value = "借款信息id", required = true)
            @PathVariable Long id) {
        Map<String, Object> borrowInfoDetail = borrowInfoService.getBorrowInfoDetail(id);
        return Result.sucess().data("borrowInfoDetail", borrowInfoDetail);
    }

    @ApiOperation("借款信息审批")
    @PostMapping("/approval")
    public Result approval(@RequestBody BorrowInfoApprovalVO borrowInfoApprovalVO) {

        borrowInfoService.approval(borrowInfoApprovalVO);
        return Result.sucess().message("审批完成");
    }
}
