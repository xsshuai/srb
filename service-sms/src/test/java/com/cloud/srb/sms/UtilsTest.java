package com.cloud.srb.sms;

import com.cloud.srb.sms.util.SmsProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @ClassName UtilsTest
 * @Author xsshuai
 * @Date 2021/5/4 12:11 下午
 **/

@SpringBootTest
@RunWith(SpringRunner.class)
public class UtilsTest {

    @Test
    public void testProperties() {
        System.out.println(SmsProperties.REGION_ID);
        System.out.println(SmsProperties.KEY_SECRET);
        System.out.println(SmsProperties.SIGN_NAME);
        System.out.println(SmsProperties.TEMPLATE_CODE);
        System.out.println(SmsProperties.KEY_ID);
    }
}
