package com.cloud.srb.core.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloud.srb.core.listener.ExcelDictDTOListener;
import com.cloud.srb.core.pojo.dto.ExcelDictDTO;
import com.cloud.srb.core.pojo.entity.Dict;
import com.cloud.srb.core.mapper.DictMapper;
import com.cloud.srb.core.service.DictService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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

    @Resource
    private DictMapper dictMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void importData(InputStream inputStream) {
        EasyExcel.read(inputStream, ExcelDictDTO.class, new ExcelDictDTOListener(baseMapper)).sheet().doRead();
        log.info("Excel导入成功");
    }

    @Override
    public List<Dict> listByParentId(Long parentId) {

        QueryWrapper<Dict> dictQueryWrapper = new QueryWrapper<>();
        dictQueryWrapper.eq("parent_id",parentId);
        List<Dict> dictList = baseMapper.selectList(dictQueryWrapper);
        //填充hasChildren字段
        dictList.forEach(dict -> {
            //判断当前节点下是否有子节点
            Boolean hasChildren = this.hasChildren(dict.getId());
            dict.setHasChildren(hasChildren);
        });
        return dictList;
    }


    /**
     * 判断当前节点是否有子节点
     * @param id
     * @return
     */
    private Boolean hasChildren(Long id) {
        QueryWrapper<Dict> dictQueryWrapper = new QueryWrapper<>();
        dictQueryWrapper.eq("parent_id",id);
        Integer count = baseMapper.selectCount(dictQueryWrapper);
        return count > 0;
    }

    @Override
    public List<ExcelDictDTO> listDictData() {
        List<Dict> dictList = dictMapper.selectList(null);
        List<ExcelDictDTO>excelDictDTOList = new ArrayList<>(dictList.size());
        dictList.forEach(dict -> {
            ExcelDictDTO excelDictDTO = new ExcelDictDTO();
            BeanUtils.copyProperties(dict,excelDictDTO);
            excelDictDTOList.add(excelDictDTO);
        });
        return excelDictDTOList;
    }
}
