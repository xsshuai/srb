package com.cloud.srb.core.service;

import com.cloud.srb.core.pojo.entity.Dict;
import com.baomidou.mybatisplus.extension.service.IService;

import java.io.InputStream;

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
}
