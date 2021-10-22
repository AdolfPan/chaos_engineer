package com.adolf.chaos.configure.props;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * <br>
 * <p>redis cfg</p>
 *
 * <br>
 *
 * @author mason
 * @version 1.0
 * @date 2021/10/12 下午1:22
 */
@Getter
@Setter
@Primary
@Configuration
@ConfigurationProperties(prefix = "spring.redis")
public class RedisConfiguration extends RedisProperties {
    private boolean enableLock;
    private boolean enableHandler;


}
