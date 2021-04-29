package com.cloud.srb.core.controller.admin;

import com.cloud.common.exception.BusinessException;
import com.cloud.common.result.ResponseEnum;
import com.cloud.common.result.Result;
import com.cloud.srb.core.service.DictService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;

/**
 * <p>
 * 数据字典 前端控制器
 * </p>
 *
 * @author xsshuai
 * @since 2021-04-24
 */
@Api(tags = "数据字典管理")
@RestController
@RequestMapping("admin/core/dict")
@Slf4j
@CrossOrigin
public class AdminDictController {

    @Resource
    private DictService dictService;

    @ApiOperation("Excel数据字典批量导入")
    @PostMapping("/import")
    public Result batchImport(
            @ApiParam(value = "Excel数据字典文件",required = true)
            @RequestParam("file") MultipartFile file) {
        try {
            InputStream inputStream = file.getInputStream();
            dictService.importData(inputStream);
            return Result.sucess().message("Excel数据字典批量导入成功");
        } catch (Exception e) {
            throw new BusinessException(ResponseEnum.UPLOAD_ERROR,e);
        }
    }

}
