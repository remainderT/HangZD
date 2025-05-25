package org.buaa.project.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.buaa.project.mq.MqConsumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.buaa.project.common.consts.RedisCacheConstants.MESSAGE_SEND_STREAM_KEY;

/**
 * Redis Stream 消息队列配置
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class RedisStreamConfiguration {

    private final RedisConnectionFactory redisConnectionFactory;
    private final MqConsumer mqConsumer;
    private final StringRedisTemplate stringRedisTemplate;

    @PostConstruct
    public void init() {
        StreamOperations<String, String, String> ops = stringRedisTemplate.opsForStream();
        if (!stringRedisTemplate.hasKey(MESSAGE_SEND_STREAM_KEY)) {
            Map<String, String> msg = new HashMap<>();
            msg.put("status", "init");
            ops.add(MESSAGE_SEND_STREAM_KEY, msg);
        }
        try {
            ops.createGroup(MESSAGE_SEND_STREAM_KEY, ReadOffset.from("0-0"), "message-send-consumer-group");
        } catch (Exception e) {
            if (!e.getMessage().contains("BUSYGROUP")) {
                throw e;
            }
        }
        log.info("Stream init done");
    }


    @Bean
    public ExecutorService asyncStreamConsumer() {
        AtomicInteger index = new AtomicInteger();
        return new ThreadPoolExecutor(1,
                1,
                60,
                TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                runnable -> {
                    Thread thread = new Thread(runnable);
                    thread.setName("stream_consumer_stats_" + index.incrementAndGet());
                    thread.setDaemon(true);
                    return thread;
                },
                new ThreadPoolExecutor.DiscardOldestPolicy()
        );
    }

    @Bean
    public Subscription ConsumerSubscription(ExecutorService asyncStreamConsumer) {
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options =
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions
                        .builder()
                        // 一次最多获取多少条消息
                        .batchSize(10)
                        // 执行从 Stream 拉取到消息的任务流程
                        .executor(asyncStreamConsumer)
                        // 如果没有拉取到消息，需要阻塞的时间。不能大于 ${spring.data.redis.timeout}，否则会超时
                        .pollTimeout(Duration.ofSeconds(3))
                        .build();
        StreamMessageListenerContainer.StreamReadRequest<String> streamReadRequest =
                StreamMessageListenerContainer.StreamReadRequest.builder(StreamOffset.create(MESSAGE_SEND_STREAM_KEY, ReadOffset.lastConsumed()))
                        .cancelOnError(throwable -> false)
                        .consumer(Consumer.from("message-send-consumer-group", "stats-consumer"))
                        .autoAcknowledge(true)
                        .build();
        StreamMessageListenerContainer<String, MapRecord<String, String, String>> listenerContainer = StreamMessageListenerContainer.create(redisConnectionFactory, options);
        Subscription subscription = listenerContainer.register(streamReadRequest, mqConsumer);
        listenerContainer.start();
        return subscription;
    }
}
