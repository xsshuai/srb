package com.cloud.srb.sms.client.fallback;

import com.cloud.srb.sms.client.CoreUserInfoClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @ClassName CoreUserInfoClientFallback
 * @Author xsshuai
 * @Date 2021/5/13 10:03 下午
 **/
@Service
@Slf4j
public class CoreUserInfoClientFallback implements CoreUserInfoClient {
    @Override
    public boolean checkMobile(String mobile) {
        log.error("远程调用失败，服务熔断");
        return false;
    }
}
