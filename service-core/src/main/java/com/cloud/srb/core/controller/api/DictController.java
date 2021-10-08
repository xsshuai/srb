package com.cloud.srb.core.controller.api;

import com.cloud.common.result.Result;
import com.cloud.srb.core.pojo.entity.Dict;
import com.cloud.srb.core.service.DictService;
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

/**
 * @ClassName DictController
 * @Author xsshuai
 * @Date 2021/5/24 10:11 下午
 **/
@Api(tags = "数据字典管理")
@RestController
@RequestMapping("api/core/dict")
@Slf4j
public class DictController {

    @Resource
    private DictService dictService;


    @ApiOperation("数据字典查询")
    @GetMapping("/findByDictCode/{dictCode}")
    public Result findByDictCode(
            @ApiParam(value = "数据字典编码",required = true)
            @PathVariable String dictCode) {
            List<Dict> dictList = dictService.findByDictCode(dictCode);
            return Result.sucess().data("dictList", dictList);
    }
}
