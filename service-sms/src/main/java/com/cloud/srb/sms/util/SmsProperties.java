package com.cloud.srb.sms.util;

import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @ClassName SmsProperties
 * @Author xsshuai
 * @Date 2021/5/4 11:59 上午
 **/
@Data
@Component
@ConfigurationProperties(prefix = "aliyun.sms")
public class SmsProperties implements InitializingBean {

//    region-id: cn-hangzhou
//    key-id: LTAI4G5Svnb2TWBMuKnNT6jY
//    key-secret: N7v6R4V3EJ1SGDZlsqtqo8QyVVMmtQ
//    template-code: SMS_96695065
//    sign-name: 谷粒

    private String regionId;
    private String keyId;
    private String keySecret;
    private String templateCode;
    private String signName;

    public static String REGION_ID;
    public static String KEY_ID;
    public static String KEY_SECRET;
    public static String TEMPLATE_CODE;
    public static String SIGN_NAME;

    /**
     * 当私有成员被赋值后，此方法自动被调用，从而初始化常量
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        REGION_ID = regionId;
        KEY_ID = keyId;
        KEY_SECRET = keySecret;
        TEMPLATE_CODE = templateCode;
        SIGN_NAME = signName;
    }
}
