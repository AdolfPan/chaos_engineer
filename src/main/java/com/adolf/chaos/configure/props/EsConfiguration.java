package com.adolf.chaos.configure.props;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * <br>
 * <p>
 *     es cfg info
 * </p>
 *
 * <br>
 *
 * @author mason
 * @version 1.0
 * @date 2021/10/12 下午1:19
 */
@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "es")
public class EsConfiguration {

    /**
     * 是否开启es客户端
     */
    private boolean client;

    /**
     * 请求协议
     */
    private String schema = "http";

    /**
     * 用户名
     */
    private String userName;

    /**
     * 密码
     */
    private String password;

    /**
     * 集群名称
     */
    private String clusterName = "elasticsearch";

    /**
     * 集群节点
     */
    private List<String> clusterNodes = Lists.newArrayList();

    /**
     * 连接超时时间(毫秒)
     */
    private Integer connectTimeout = 1000;

    /**
     * socket 超时时间
     */
    private Integer socketTimeout = 30000;

    /**
     * 连接请求超时时间
     */
    private Integer connectionRequestTimeout = 500;

    /**
     * 每个路由的最大连接数量
     */
    private Integer maxConnectPerRoute = 10;

    /**
     * 最大连接总数量
     */
    private Integer maxConnectTotal = 30;



}
