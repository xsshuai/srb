package com.cloud.srb.core;

import com.cloud.srb.core.mapper.DictMapper;
import com.cloud.srb.core.pojo.entity.Dict;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName RedisTemplateTests
 * @Author xsshuai
 * @Date 2021/5/1 10:37 上午
 **/

@SpringBootTest
@RunWith(SpringRunner.class)
public class RedisTemplateTests {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private DictMapper dictMapper;

    @Test
    public void saveDict() {
        Dict dict = dictMapper.selectById(1);
        redisTemplate.opsForValue().set("dict",dict,5, TimeUnit.MINUTES);
    }

    @Test
    public void getDict() {
        Dict dict = (Dict)redisTemplate.opsForValue().get("dict");
        System.out.println(dict);
    }
}
