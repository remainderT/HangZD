package org.buaa.project.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.buaa.project.mq.MqConsumer;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.SmartLifecycle;
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
        AtomicInteger idx = new AtomicInteger();
        return new ThreadPoolExecutor(
                2, 2,
                60, TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                r -> {
                    Thread t = new Thread(r, "stream_consumer_" + idx.incrementAndGet());
                    t.setDaemon(true);
                    return t;
                },
                new ThreadPoolExecutor.DiscardOldestPolicy()
        );
    }

    @Bean("streamContainer")
    public StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamMessageListenerContainer(
            RedisConnectionFactory factory,
            ExecutorService asyncStreamConsumer) {

        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> opts =
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
                        .batchSize(10)
                        .executor(asyncStreamConsumer)
                        .pollTimeout(Duration.ofSeconds(3))
                        .build();

        return StreamMessageListenerContainer.create(factory, opts);
    }

    @Bean
    public Subscription shortLinkStatsSaveConsumerSubscription(
            @Qualifier("streamContainer") StreamMessageListenerContainer<String, MapRecord<String, String, String>> container) {

        StreamMessageListenerContainer.StreamReadRequest<String> req =
                StreamMessageListenerContainer.StreamReadRequest.builder(
                                StreamOffset.create(MESSAGE_SEND_STREAM_KEY, ReadOffset.lastConsumed()))
                        .consumer(Consumer.from("message-send-consumer-group", "consumer1"))
                        .autoAcknowledge(true)
                        .cancelOnError(e -> false)
                        .build();

        return container.register(req, mqConsumer);
    }

    @Bean
    public SmartLifecycle streamAndRedissonLifecycle(
            @Qualifier("streamContainer") StreamMessageListenerContainer<?, ?> streamContainer,
            RedissonClient redissonClient) {

        return new SmartLifecycle() {
            private volatile boolean running = false;

            @Override
            public void start() {
                streamContainer.start();
                running = true;
            }

            @Override
            public void stop() {
                // 先停流监听
                streamContainer.stop();
                // 再关闭 Redisson 客户端
                redissonClient.shutdown();
                running = false;
            }

            @Override
            public boolean isRunning() {
                return running;
            }

            @Override
            public boolean isAutoStartup() {
                return true;
            }

            @Override
            public int getPhase() {
                // 单一包装，在 MAX−50 调用 stop()
                return Integer.MAX_VALUE - 50;
            }

            @Override
            public void stop(Runnable callback) {
                stop();
                callback.run();
            }
        };
    }
}
