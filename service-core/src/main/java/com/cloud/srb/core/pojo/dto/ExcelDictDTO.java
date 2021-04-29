package com.cloud.srb.core.pojo.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @ClassName ExcelDictDTO
 * @Author xsshuai
 * @Date 2021/4/28 8:15 下午
 **/
@Data
public class ExcelDictDTO {
    @ExcelProperty("id")
    private Long id;

    @ExcelProperty("上级id")
    private Long parentId;

    @ExcelProperty("名称")
    private String name;

    @ExcelProperty("值")
    private Integer value;

    @ExcelProperty("编码")
    private String dictCode;

}
