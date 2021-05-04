package com.cloud.srb.sms.service;

import java.util.Map;

/**
 * @ClassName SmsService
 * @Author xsshuai
 * @Date 2021/5/4 12:45 下午
 **/
public interface SmsService {

    void send(String mobile, String templateCode,
              Map<String, Object> param);
}
