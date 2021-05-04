package com.cloud.srb.oss.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.CannedAccessControlList;
import com.cloud.srb.oss.service.FileService;
import com.cloud.srb.oss.util.OssProperties;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.UUID;

/**
 * @ClassName FileServiceImpl
 * @Author xsshuai
 * @Date 2021/5/4 6:26 下午
 **/
@Service
public class FileServiceImpl implements FileService {
    @Override
    public String upload(InputStream inputStream, String module, String fileName) {

        //Endpoint以杭州为例，其他Region请按实际情况填写
        String endpoint = OssProperties.END_POINT;

        //阿里云主账号AccessKey拥有所有API的访问权限，风险很高，强烈建议您创建并使用RAM账号进行API访问或日常运维，请登录RAM控制台创建RAM账号
        String accessKeyId = OssProperties.KEY_ID;
        String accessKeySecret = OssProperties.KEY_SECRET;

        //创建OSSClient实例
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        //判断BUCKET_NAME是否存在
        if(!ossClient.doesBucketExist(OssProperties.BUCKET_NAME)) {
            ossClient.createBucket(OssProperties.BUCKET_NAME);
            ossClient.setBucketAcl(OssProperties.BUCKET_NAME, CannedAccessControlList.PublicRead);
        }

        //上传文件目录解构：avatar/2021/05/04/uuid.jpg
        //构建日期路径
        String timeFolder = new DateTime().toString("/yyyy/MM/dd");
        
        //生成文件名
        String upFileName = UUID.randomUUID().toString() + fileName.substring(fileName.lastIndexOf("."));
        String key = module + timeFolder + upFileName;
        //上传文件流
        ossClient.putObject(OssProperties.BUCKET_NAME, key, inputStream);

        //关闭OSSClient
        ossClient.shutdown();

        //文件的url地址
        return "https://" + OssProperties.BUCKET_NAME + "." + OssProperties.END_POINT + "/" + key;
    }

    @Override
    public void remove(String url) {
        //Endpoint以杭州为例，其他Region请按实际情况填写
        String endpoint = OssProperties.END_POINT;

        //阿里云主账号AccessKey拥有所有API的访问权限，风险很高，强烈建议您创建并使用RAM账号进行API访问或日常运维，请登录RAM控制台创建RAM账号
        String accessKeyId = OssProperties.KEY_ID;
        String accessKeySecret = OssProperties.KEY_SECRET;

        //创建OSSClient实例
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        String host = "https://" + OssProperties.BUCKET_NAME + "." + OssProperties.END_POINT + "/";
        String objectName = url.substring(host.length());

        //删除文件
        ossClient.deleteObject(OssProperties.BUCKET_NAME, objectName);

        //关闭OSSClient
        ossClient.shutdown();
    }
}
