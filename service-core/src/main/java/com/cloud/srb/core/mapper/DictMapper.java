package com.cloud.srb.core.mapper;

import com.cloud.srb.core.pojo.dto.ExcelDictDTO;
import com.cloud.srb.core.pojo.entity.Dict;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 * 数据字典 Mapper 接口
 * </p>
 *
 * @author xsshuai
 * @since 2021-04-24
 */
public interface DictMapper extends BaseMapper<Dict> {

    void insertBatch(List<ExcelDictDTO> list);
    List<Dict> listByParentId(Long parentId);
}
