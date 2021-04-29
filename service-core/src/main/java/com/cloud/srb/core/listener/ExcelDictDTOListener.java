package com.cloud.srb.core.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.cloud.srb.core.mapper.DictMapper;
import com.cloud.srb.core.pojo.dto.ExcelDictDTO;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName ExcelDTOListener
 * @Author xsshuai
 * @Date 2021/4/28 8:24 下午
 **/
@Slf4j
@NoArgsConstructor
public class ExcelDictDTOListener extends AnalysisEventListener<ExcelDictDTO> {

    private DictMapper dictMapper;
    /**
     * 数据列表
     */
    List<ExcelDictDTO> list = new ArrayList<ExcelDictDTO>();
    /**
     * 每隔5条存储数据库，实际使用中可以3000条，然后清理list ，方便内存回收
     */
    private static final int BATCH_COUNT = 5;

    public ExcelDictDTOListener(DictMapper dictMapper) {
        this.dictMapper = dictMapper;
    }

    @Override
    public void invoke(ExcelDictDTO excelDictDTO, AnalysisContext analysisContext) {
        log.info("解析到一条记录:{}",excelDictDTO);
        list.add(excelDictDTO);
        // 达到BATCH_COUNT了，需要去存储一次数据库，防止数据几万条数据在内存，容易OOM
        if (list.size() >= BATCH_COUNT) {
            saveData();
            // 存储完成清理 list
            list.clear();
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        saveData();
        log.info("所有数据解析完成！");
    }

    /**
     * 加上存储数据库
     */
    private void saveData() {
        log.info("{}条数据，开始存储数据库！", list.size());
        dictMapper.insertBatch(list);
        log.info("存储数据库成功！");
    }
}
