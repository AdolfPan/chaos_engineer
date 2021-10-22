package com.adolf.chaos.configure.props;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * <br>
 * <p>
 *     etcd props info
 * </p>
 *
 * <br>
 * @author mason
 * @version 1.0
 * @date 2021/10/13 上午9:58
 */
@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "etcd")
public class EtcdConfiguration {

    private boolean client;

}
