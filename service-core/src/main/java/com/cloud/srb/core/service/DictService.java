package com.cloud.srb.core.service;

import com.cloud.srb.core.pojo.dto.ExcelDictDTO;
import com.cloud.srb.core.pojo.entity.Dict;
import com.baomidou.mybatisplus.extension.service.IService;

import java.io.InputStream;
import java.util.List;

/**
 * <p>
 * 数据字典 服务类
 * </p>
 *
 * @author xsshuai
 * @since 2021-04-24
 */
public interface DictService extends IService<Dict> {

    void importData(InputStream inputStream);

    List<Dict> listByParentId(Long parentId);

    List<ExcelDictDTO> listDictData();

    List<Dict> findByDictCode(String dictCode);

    String getNameByParentDictCodeAndValue(String dictCode, Integer value);
}
