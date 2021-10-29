package com.adolf.chaos.configure;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.adolf.chaos.cache.RedisHashHandler;
import com.adolf.chaos.cache.RedisListHandler;
import com.adolf.chaos.cache.RedisStringHandler;
import com.adolf.chaos.cache.comp.CacheHashCompRidesImpl;
import com.adolf.chaos.cache.comp.CacheListCompRidesImpl;
import com.adolf.chaos.cache.comp.CacheStringCompRidesImpl;
import com.adolf.chaos.configure.props.RedisConfiguration;
import com.adolf.chaos.lock.redis.RedisLockerHandler;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SentinelServersConfig;
import org.redisson.config.SingleServerConfig;
import org.redisson.config.TransportMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.List;

/**
 * <br>
 * <p>
 *     Auto-configuration for Redis support.
 * </p><br>
 *
 * @author panrusen
 * @version 1.0
 * @date 2021/6/3 上午9:58
 */
@Configuration
@ConditionalOnClass(RedisConfiguration.class)
public class RedisAutoConfiguration {

    @Autowired
    private RedisConfiguration redisConfiguration;

    @Bean
    @ConditionalOnMissingBean(RedissonClient.class)
    @ConditionalOnProperty(prefix = "spring.redis", name = "enable-lock", havingValue = "true")
    @Order(Ordered.LOWEST_PRECEDENCE - 200)
    public RedissonClient redissonClient() {
        Config config = new Config();
        //sentinel
        if (redisConfiguration.getSentinel() != null) {
            SentinelServersConfig sentinelServersConfig = config.useSentinelServers();
            sentinelServersConfig.setMasterName(redisConfiguration.getSentinel().getMaster());
            List<String> nodes = redisConfiguration.getSentinel().getNodes();
            if (CollectionUtil.isNotEmpty(nodes)) {
                sentinelServersConfig.addSentinelAddress(nodes.toArray(new String[nodes.size()]));
            }
            sentinelServersConfig.setDatabase(redisConfiguration.getDatabase());
            if (StrUtil.isNotBlank(redisConfiguration.getPassword())) {
                sentinelServersConfig.setPassword(redisConfiguration.getPassword());
            }
        } else {
            //single server
            SingleServerConfig singleServerConfig = config.useSingleServer();
            String schema = redisConfiguration.isSsl() ? "rediss://" : "redis://";
            singleServerConfig.setAddress(schema + redisConfiguration.getHost() + ":" + redisConfiguration.getPort());
            singleServerConfig.setDatabase(redisConfiguration.getDatabase());
            if (StrUtil.isNotBlank(redisConfiguration.getPassword())) {
                singleServerConfig.setPassword(redisConfiguration.getPassword());
            }
        }
        config.setLockWatchdogTimeout(15*1000);
        String os = System.getProperty("os.name");
        config.setTransportMode(StrUtil.isNotBlank(os) && StrUtil.containsAnyIgnoreCase(os, "Linux")?
                TransportMode.EPOLL:
                TransportMode.NIO);
        return Redisson.create(config);
    }

    @Bean
    @ConditionalOnMissingBean(RedisLockerHandler.class)
    @ConditionalOnProperty(prefix = "spring.redis", name = "enable-lock", havingValue = "true")
    @Order(Ordered.LOWEST_PRECEDENCE - 100)
    public RedisLockerHandler redisLockerHandler() {
        return new RedisLockerHandler();
    }

    @Bean
    @ConditionalOnMissingBean(RedisStringHandler.class)
    @ConditionalOnProperty(prefix = "spring.redis", name = "enable-handler", havingValue = "true")
    @Order(Ordered.LOWEST_PRECEDENCE - 100)
    public RedisStringHandler redisStringHandler() {
        return new CacheStringCompRidesImpl();
    }

    @Bean
    @ConditionalOnMissingBean(RedisListHandler.class)
    @ConditionalOnProperty(prefix = "spring.redis", name = "enable-handler", havingValue = "true")
    @Order(Ordered.LOWEST_PRECEDENCE - 100)
    public RedisListHandler redisListHandler() {
        return new CacheListCompRidesImpl();
    }

    @Bean
    @ConditionalOnMissingBean(RedisHashHandler.class)
    @ConditionalOnProperty(prefix = "spring.redis", name = "enable-handler", havingValue = "true")
    @Order(Ordered.LOWEST_PRECEDENCE - 100)
    public RedisHashHandler redisHashHandler() {
        return new CacheHashCompRidesImpl();
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE - 100)
    @ConditionalOnMissingBean(RedisTemplate.class)
    @ConditionalOnExpression("'${spring.redis.host}'!='localhost'")
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory lettuceConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(lettuceConnectionFactory);

        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.activateDefaultTyping(LaissezFaireSubTypeValidator.instance , ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.WRAPPER_ARRAY);
        jackson2JsonRedisSerializer.setObjectMapper(om);

        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.setHashKeySerializer(stringRedisSerializer);
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.afterPropertiesSet();

        return redisTemplate;
    }
}
