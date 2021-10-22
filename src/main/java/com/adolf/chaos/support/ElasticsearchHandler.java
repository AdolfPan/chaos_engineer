package com.adolf.chaos.support;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.bulk.*;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.core.TimeValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

/**
 * <br>
 * <p>
 *     es api handler
 * </p>
 *
 * <br>
 *
 * @author mason
 * @version 1.0
 * @date 2021/10/12 下午4:08
 */
@Slf4j
@Component
@ConditionalOnExpression("${es.client:false}")
@ConditionalOnClass(RestHighLevelClient.class)
public class ElasticsearchHandler {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    private BulkProcessor bulkProcessor;

    @PostConstruct
    public void init() {
        //build listener
        BulkProcessor.Listener listener = new BulkProcessor.Listener() {
            @Override
            public void beforeBulk(long executionId, BulkRequest request) {
                //重写beforeBulk,在每次bulk request发出前执行,在这个方法里面可以知道在本次批量操作中有多少操作数
                int numberOfActions = request.numberOfActions();
                log.info("Executing bulk [{}] with {} requests", executionId, numberOfActions);
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
                //重写afterBulk方法，每次批量请求结束后执行，可以在这里知道是否有错误发生。
                if (response.hasFailures()) {
                    log.error("Bulk [{}] executed with failures,response = {}", executionId, response.buildFailureMessage());
                } else {
                    log.info("Bulk [{}] completed in {} milliseconds", executionId, response.getTook().getMillis());
                }
                BulkItemResponse[] responses = response.getItems();
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
                //重写方法，如果发生错误就会调用。
                log.error("Failed to execute bulk", failure);
            }
        };

        BiConsumer<BulkRequest, ActionListener<BulkResponse>> bulkConsumer =
                (request, bulkListener) ->
                        restHighLevelClient.bulkAsync(request, RequestOptions.DEFAULT, bulkListener);

        //在这里调用build()方法构造bulkProcessor,在底层实际上是用了bulk的异步操作
        BulkProcessor bulkProcessor = BulkProcessor.builder(bulkConsumer, listener, "BProcessor-" + Thread.currentThread().getName())
                // 1000条数据请求执行一次bulk
                .setBulkActions(1000)
                // 5mb的数据刷新一次bulk
                .setBulkSize(new ByteSizeValue(5L, ByteSizeUnit.MB))
                // 并发请求数量, 0不并发, 1并发允许执行
                .setConcurrentRequests(0)
                // 固定1s必须刷新一次
                .setFlushInterval(TimeValue.timeValueSeconds(1L))
                // 重试5次，间隔1s
                .setBackoffPolicy(BackoffPolicy.constantBackoff(TimeValue.timeValueSeconds(1L), 5))
                .build();
        this.bulkProcessor = bulkProcessor;

    }

    /**
     * 修改
     * @param request
     */
    public void update(UpdateRequest request) {
        this.bulkProcessor.add(request);
    }

    /**
     * 新增
     * @param request
     */
    public void insert(IndexRequest request) {
        this.bulkProcessor.add(request);
    }

    @PreDestroy
    public void destroy() {
        try {
            bulkProcessor.awaitClose(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("Failed to close bulkProcessor", e);
        }
        log.info("bulkProcessor closed!");
    }


}