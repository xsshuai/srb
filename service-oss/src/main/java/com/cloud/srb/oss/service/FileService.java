package com.cloud.srb.oss.service;

import java.io.InputStream;

/**
 * @ClassName FileService
 * @Author xsshuai
 * @Date 2021/5/4 6:25 下午
 **/
public interface FileService {
    /**
     * 文件上传到阿里云
     * @param inputStream
     * @param module
     * @param fileName
     * @return
     */
    String upload(InputStream inputStream, String module, String fileName);

    void remove(String url);
}
