package com.adolf.chaos.configure;

import cn.hutool.core.util.StrUtil;
import com.adolf.chaos.configure.props.EsConfiguration;
import com.adolf.chaos.support.ElasticsearchHandler;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * <br>
 * <p>es client auto </p>
 *
 * <br>
 *
 * @author mason
 * @version 1.0
 * @date 2021/10/12 下午1:33
 */
@Configuration
@ConditionalOnExpression("${es.client:false}")
@ConditionalOnClass(EsConfiguration.class)
public class EsClientAutoConfiguration {

    @Autowired
    private EsConfiguration esConfiguration;

    private List<HttpHost> httpHosts = new ArrayList<>();

    @Bean
    @ConditionalOnExpression("${es.client:false}")
    @ConditionalOnMissingBean(RestHighLevelClient.class)
    public RestHighLevelClient restHighLevelClient() {
        List<String> clusterNodes = esConfiguration.getClusterNodes();
        if (clusterNodes.isEmpty()) {
            throw new RuntimeException("集群节点不允许为空");
        }
        clusterNodes.forEach(node -> {
            try {
                String[] parts = StringUtils.split(node, ":");
                Assert.notNull(parts, "Must defined");
                Assert.state(parts.length == 2, "Must be defined as 'host:port'");
                httpHosts.add(new HttpHost(parts[0], Integer.parseInt(parts[1]), esConfiguration.getSchema()));
            } catch (Exception e) {
                throw new IllegalStateException("Invalid ES nodes " + "property '" + node + "'", e);
            }
        });
        RestClientBuilder builder = RestClient.builder(httpHosts.toArray(new HttpHost[0]));
        return getRestHighLevelClient(builder, esConfiguration);
    }

    @Bean
    @ConditionalOnExpression("${es.client:false}")
    @ConditionalOnMissingBean(ElasticsearchHandler.class)
    public ElasticsearchHandler elasticsearchHandler() {
        return new ElasticsearchHandler();
    }

    /**
     * get restHistLevelClient
     * @return
     */
    private static RestHighLevelClient getRestHighLevelClient(RestClientBuilder builder, EsConfiguration elasticsearchProperties) {
        // Callback used the default {@link RequestConfig} being set to the {@link CloseableHttpClient}
        builder.setRequestConfigCallback(requestConfigBuilder -> {
            requestConfigBuilder.setConnectTimeout(elasticsearchProperties.getConnectTimeout());
            requestConfigBuilder.setSocketTimeout(elasticsearchProperties.getSocketTimeout());
            requestConfigBuilder.setConnectionRequestTimeout(elasticsearchProperties.getConnectionRequestTimeout());
            return requestConfigBuilder;
        });
        // Callback used to customize the {@link CloseableHttpClient} instance used by a {@link RestClient} instance.
        builder.setHttpClientConfigCallback(httpClientBuilder -> {
            httpClientBuilder.setMaxConnTotal(elasticsearchProperties.getMaxConnectTotal());
            httpClientBuilder.setMaxConnPerRoute(elasticsearchProperties.getMaxConnectPerRoute());
            return httpClientBuilder;
        });
        // Callback used the basic credential auth
        if (StrUtil.isNotBlank(elasticsearchProperties.getUserName())
                && !StringUtils.isEmpty(elasticsearchProperties.getUserName())) {
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(elasticsearchProperties.getUserName(), elasticsearchProperties.getPassword()));
            builder.setHttpClientConfigCallback(httpAsyncClientBuilder -> {
                //这里可以设置一些参数，比如cookie存储、代理等等
                httpAsyncClientBuilder.disableAuthCaching();
                return httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            });
        }
        return new RestHighLevelClient(builder);
    }



}
