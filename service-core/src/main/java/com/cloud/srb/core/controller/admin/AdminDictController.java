package com.cloud.srb.core.controller.admin;

import com.alibaba.excel.EasyExcel;
import com.cloud.common.exception.BusinessException;
import com.cloud.common.result.ResponseEnum;
import com.cloud.common.result.Result;
import com.cloud.srb.core.pojo.dto.ExcelDictDTO;
import com.cloud.srb.core.pojo.entity.Dict;
import com.cloud.srb.core.service.DictService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.List;

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

    @ApiOperation("数据字典批量导出到excel")
    @GetMapping("export")
    public void export(HttpServletResponse response) throws IOException {
        // 这里注意 有同学反应使用swagger 会导致各种问题，请直接用浏览器或者用postman
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
        String fileName = URLEncoder.encode("mydict", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        EasyExcel.write(response.getOutputStream(), ExcelDictDTO.class).sheet("模板").doWrite(dictService.listDictData());
    }

    @ApiOperation("数据字典列表")
    @GetMapping("/list")
    public Result listAll() {
        List<Dict> dictList = dictService.list();
        return Result.sucess().data("dictList",dictList).message("获取列表成功");
    }

    @ApiOperation("数据字典列表parentId")
    @GetMapping("/get/{parentId}")
    public Result listByParentId(
            @ApiParam(value = "数据字典parentId",required = true)
            @PathVariable Long parentId
    ) {
        List<Dict> dictList = dictService.listByParentId(parentId);
        return Result.sucess().data("dictList",dictList).message("获取列表成功");
    }

}
