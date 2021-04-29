package com.cloud.srb.core.service.impl;

import com.alibaba.excel.EasyExcel;
import com.cloud.srb.core.listener.ExcelDictDTOListener;
import com.cloud.srb.core.pojo.dto.ExcelDictDTO;
import com.cloud.srb.core.pojo.entity.Dict;
import com.cloud.srb.core.mapper.DictMapper;
import com.cloud.srb.core.service.DictService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;

/**
 * <p>
 * 数据字典 服务实现类
 * </p>
 *
 * @author xsshuai
 * @since 2021-04-24
 */
@Slf4j
@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void importData(InputStream inputStream) {
        EasyExcel.read(inputStream, ExcelDictDTO.class, new ExcelDictDTOListener(baseMapper)).sheet().doRead();
        log.info("Excel导入成功");
    }
}
