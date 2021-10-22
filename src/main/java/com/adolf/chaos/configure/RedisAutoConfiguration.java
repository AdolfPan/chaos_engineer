package com.adolf.chaos.configure;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.adolf.chaos.configure.props.RedisConfiguration;
import com.adolf.chaos.lock.redis.RedisLockerHandler;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

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
    @ConditionalOnExpression("${spring.redis.enableLock:false}")
    @ConditionalOnMissingBean(RedissonClient.class)
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
        config.setTransportMode(StrUtil.isNotBlank(os) && !os.toLowerCase().startsWith("windows")?
                TransportMode.EPOLL:
                TransportMode.NIO);
        return Redisson.create(config);
    }

    @Bean
    @ConditionalOnExpression("${spring.redis.lockEnabled:false}")
    @ConditionalOnMissingBean(RedisLockerHandler.class)
    @Order(Ordered.LOWEST_PRECEDENCE - 100)
    public RedisLockerHandler redisLockerHandler() {
        return new RedisLockerHandler();
    }


}
