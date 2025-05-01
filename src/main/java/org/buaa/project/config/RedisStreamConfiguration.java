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
        createStreamAndGroupIfNotExists(MESSAGE_SEND_STREAM_KEY, "message-send-consumer-group");
    }

    private void createStreamAndGroupIfNotExists(String streamKey, String groupName) {
        StreamOperations<String, String, String> streamOperations = stringRedisTemplate.opsForStream();
        if (!stringRedisTemplate.hasKey(streamKey)) {
            Map<String, String> message = new HashMap<>();
            message.put("status", "initialization");
            streamOperations.add(streamKey, message);
        }

        try {
            streamOperations.createGroup(streamKey, ReadOffset.from("0-0"), groupName);
        } catch (Exception e) {
            // 如果消费者组已经存在，忽略异常
            if (!e.getMessage().contains("BUSYGROUP")) {
                throw e;
            }
        }
        log.info("Stream {} and group {} initialization completed", streamKey, groupName);
    }

    @Bean
    public ExecutorService asyncStreamConsumer() {
        AtomicInteger index = new AtomicInteger();
        return new ThreadPoolExecutor(2,
                2,
                60,
                TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                runnable -> {
                    Thread thread = new Thread(runnable);
                    thread.setName("stream_consumer_" + index.incrementAndGet());
                    thread.setDaemon(true);
                    return thread;
                },
                new ThreadPoolExecutor.DiscardOldestPolicy()
        );
    }

    @Bean
    public Subscription shortLinkStatsSaveConsumerSubscription(ExecutorService asyncStreamConsumer) {
        return createStreamConsumer(MESSAGE_SEND_STREAM_KEY, "message-send-consumer-group", asyncStreamConsumer, "consumer1");
    }


    private Subscription createStreamConsumer(String streamKey, String groupKey, ExecutorService asyncStreamConsumer, String consumerName) {
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options =
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions
                        .builder()
                        // 批量拉取消息的大小为 10 条
                        .batchSize(10)
                        .executor(asyncStreamConsumer)
                        // 设置拉取消息的超时时间为 3 秒
                        .pollTimeout(Duration.ofSeconds(3))
                        .build();

        StreamMessageListenerContainer.StreamReadRequest<String> streamReadRequest =
                StreamMessageListenerContainer.StreamReadRequest.builder(StreamOffset.create(streamKey, ReadOffset.lastConsumed()))
                        .cancelOnError(throwable -> false)
                        .consumer(Consumer.from(groupKey, consumerName))
                        // 自动确认消息
                        .autoAcknowledge(true)
                        .build();

        StreamMessageListenerContainer<String, MapRecord<String, String, String>> listenerContainer = StreamMessageListenerContainer.create(redisConnectionFactory, options);
        Subscription subscription = listenerContainer.register(streamReadRequest, mqConsumer);
        listenerContainer.start();
        return subscription;
    }

}
