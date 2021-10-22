package com.adolf.chaos.support;

import cn.hutool.core.util.ArrayUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.bulk.*;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
@ConditionalOnClass(RestHighLevelClient.class)
public class ElasticsearchHandler {

    @Autowired
    private RestHighLevelClient restHighLevelClient;
    private final RequestOptions options = RequestOptions.DEFAULT;

    /**
     * 写入数据
     */
    public boolean insert(String indexName, Map<String,Object> dataMap) {
        try {
            IndexRequest indexRequest = new IndexRequest(indexName);
            if (dataMap.containsKey("id")) {
                indexRequest.id(dataMap.remove("id").toString());
            }
            indexRequest.opType("create").source(dataMap, XContentType.JSON);

            BulkRequest request = new BulkRequest();
            request.add(indexRequest);
            BulkResponse responses = this.restHighLevelClient.bulk(request, options);
            return Objects.nonNull(responses) && responses.hasFailures();
        } catch (Exception e){
            log.error("elasticsearch insert error. e: {}", e);
        }
        return Boolean.FALSE;
    }

    /**
     * 批量写入数据
     */
    public boolean batchInsert(String indexName, List<Map<String,Object>> userIndexList) {
        try {
            BulkRequest request = new BulkRequest();
            for (Map<String,Object> dataMap:userIndexList){
                IndexRequest indexRequest = new IndexRequest(indexName);
                if (dataMap.containsKey("id")) {
                    indexRequest.id(dataMap.remove("id").toString());
                }
                indexRequest.opType("create").source(dataMap, XContentType.JSON);
                request.add(indexRequest);
            }
            BulkResponse responses = this.restHighLevelClient.bulk(request, options);
            return Objects.nonNull(responses) && responses.hasFailures();
        } catch (Exception e){
            log.error("elasticsearch batchInsert error. e: {}", e);
        }
        return Boolean.FALSE;
    }

    /**
     * 更新数据，可以直接修改索引结构
     */
    public boolean update(String indexName, Map<String,Object> dataMap){
        try {
            UpdateRequest updateRequest = new UpdateRequest();
            if (dataMap.containsKey("id")) {
                updateRequest = new UpdateRequest(indexName, dataMap.remove("id").toString());
            }
            updateRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
            updateRequest.doc(dataMap) ;
            this.restHighLevelClient.update(updateRequest, options);
            return Boolean.TRUE ;
        } catch (Exception e) {
            log.error("elasticsearch update error. e: {}", e);
        }
        return Boolean.FALSE;
    }

    /**
     * 更新数据，可以直接修改索引结构
     */
    public boolean batchUpdate(String indexName, List<Map<String,Object>> dataMaps){
        try {
            //异步执行
            //DeleteResponse  的典型监听器如下所示：
            //异步方法不会阻塞并立即返回。
            ActionListener<UpdateResponse > listener = new ActionListener<UpdateResponse>() {
                @Override
                public void onResponse(UpdateResponse updateResponse) {
                    log.info("elasticsearch batchUpdate onResponse. Resp: {}", updateResponse);
                }
                @Override
                public void onFailure(Exception e) {
                    log.error("elasticsearch batchUpdate.onFailure error e: {}", e);
                }
            };
            for (Map<String, Object> dataMap : dataMaps) {
                UpdateRequest updateRequest = new UpdateRequest();
                if (dataMap.containsKey("id")) {
                    updateRequest = new UpdateRequest(indexName, dataMap.remove("id").toString());
                }
                updateRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
                updateRequest.doc(dataMap);
                this.restHighLevelClient.updateAsync(updateRequest, options, listener);
            }
            return Boolean.TRUE ;
        } catch (Exception e){
            log.error("elasticsearch batchUpdate error. e: {}", e);
        }
        return Boolean.FALSE;
    }


    /**
     * 删除数据
     */
    public boolean delete(String indexName, String id){
        try {
            DeleteRequest deleteRequest = new DeleteRequest(indexName, id);
            this.restHighLevelClient.delete(deleteRequest, options);
            return Boolean.TRUE ;
        } catch (Exception e){
            e.printStackTrace();
        }
        return Boolean.FALSE;
    }

    /**
     * 查询总数
     * @param indexName
     * @param queryBuilder
     *      BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
     *      queryBuilder.must(QueryBuilders.termQuery("createTime", 1611378102795L));
     */
    public Long count(String indexName, BoolQueryBuilder queryBuilder){
        CountRequest countRequest = new CountRequest(indexName);
        countRequest.query(queryBuilder);
        try {
            CountResponse countResponse = this.restHighLevelClient.count(countRequest, options);
            return countResponse.getCount();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0L;
    }

    /**
     * 查询集合
     * @param indexName
     * @param queryBuilder
     *      BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
     *      queryBuilder.must(QueryBuilders.termQuery("createTime", 1611378102795L));
     *      queryBuilder.mustNot(QueryBuilders.termQuery("name","北京-李四"));
     * @return
     */
    public List<Map<String, Object>> list(String indexName, BoolQueryBuilder queryBuilder) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(queryBuilder);
        SearchRequest searchRequest = new SearchRequest(indexName);
        searchRequest.source(sourceBuilder);
        try {
            SearchResponse searchResp = this.restHighLevelClient.search(searchRequest, options);
            List<Map<String,Object>> data = new ArrayList<>() ;
            SearchHit[] searchHitArr = searchResp.getHits().getHits();
            for (SearchHit searchHit:searchHitArr){
                Map<String,Object> temp = searchHit.getSourceAsMap();
                data.add(temp);
            }
            return data;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null ;
    }

    /**
     * 分页查询
     * @param indexName
     * @param offset
     * @param size
     * @param queryBuilder
     * @param sorts
     * @return
     */
    public List<Map<String,Object>> page(String indexName, int offset, int size, BoolQueryBuilder queryBuilder, Sort... sorts) {
        offset = offset < 0? 0: offset;
        size = size <= 0? 10: size;
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.from(offset);
        sourceBuilder.size(size);
        if (Objects.nonNull(queryBuilder)) {
            sourceBuilder.query(queryBuilder);
        }
        if (Objects.nonNull(sorts) && ArrayUtil.isNotEmpty(sorts)) {
            for (Sort sort : sorts) {
                sourceBuilder.sort(sort.getSortFieldName(), sort.getSortOrder());
            }
        }
        SearchRequest searchRequest = new SearchRequest(indexName);
        searchRequest.source(sourceBuilder);
        try {
            SearchResponse searchResp = this.restHighLevelClient.search(searchRequest, options);
            List<Map<String,Object>> data = new ArrayList<>() ;
            SearchHit[] searchHitArr = searchResp.getHits().getHits();
            for (SearchHit searchHit: searchHitArr){
                Map<String,Object> temp = searchHit.getSourceAsMap();
                data.add(temp);
            }
            return data;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null ;
    }

    @Data
    @Builder
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Sort{
        private String sortFieldName;
        private SortOrder sortOrder;
    }

}
