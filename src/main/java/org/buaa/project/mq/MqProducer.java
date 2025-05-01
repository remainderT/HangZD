package org.buaa.project.mq;

import com.alibaba.fastjson2.JSON;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static org.buaa.project.common.consts.RedisCacheConstants.MESSAGE_SEND_STREAM_KEY;

@Component
@RequiredArgsConstructor
public class MqProducer {

    private final StringRedisTemplate stringRedisTemplate;

    public void messageSend(MqEvent event) {
        Map<String, String> producerMap = new HashMap<>();
        producerMap.put("event", JSON.toJSONString(event));
        stringRedisTemplate.opsForStream().add(MESSAGE_SEND_STREAM_KEY, producerMap);
    }

}
