package com.pyy.blog.service;


import com.alibaba.fastjson.JSON;
import com.pyy.blog.domain.BlogPaper;
import com.pyy.blog.domain.Pagination;
import com.pyy.blog.domain.Result;
import com.pyy.blog.domain.dto.BlogPaperDTO;
import com.pyy.blog.domain.mapper.BlogPaperMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 文档业务逻辑类
 *
 * @author dengweiping
 * @date 2021/1/19 14:26
 */
@Slf4j
@Service
public class DocService {
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    /**
     * 增加文档信息
     */
    public Result addDocument(BlogPaper blog) {
        try {
            IndexRequest indexRequest = new IndexRequest("blog");
            // 将对象转换为 byte 数组
            byte[] json = JSON.toJSONBytes(blog);
            // 设置文档内容
            indexRequest.id(blog.getId());
            indexRequest.source(json, XContentType.JSON);
            // 执行增加文档
            IndexResponse response = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
            return Result.success(response);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("创建文档失败", e.getMessage());
        }
    }

    /**
     * 获取文档信息
     */
    public BlogPaperDTO getDocById(String id) {
        try {
            // 获取请求对象
            GetRequest getRequest = new GetRequest("blog", id);
            // 获取文档信息
            GetResponse getResponse = restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);
            // 将 JSON 转换成对象
            if (getResponse.isExists()) {
                BlogPaper responseBlog = JSON.parseObject(getResponse.getSourceAsBytes(), BlogPaper.class);
                return BlogPaperMapper.toDTO(responseBlog);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 分页查询
     */
    public Result queryDoc(BlogPaper queryDTO, Pagination pagination) {
        List<BlogPaperDTO> data = new ArrayList<>();
        try {
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            if (queryDTO != null) {
                BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
                if (StringUtils.isNotBlank(queryDTO.getAuthor())) {
                    boolQueryBuilder.must(QueryBuilders.wildcardQuery("author", queryDTO.getAuthor()));
                }
                if (StringUtils.isNotBlank(queryDTO.getTitle())) {
                    boolQueryBuilder.must(QueryBuilders.wildcardQuery("title", queryDTO.getTitle()));
                }
                if (StringUtils.isNotBlank(queryDTO.getDescription())) {
                    boolQueryBuilder.must(QueryBuilders.wildcardQuery("description", queryDTO.getDescription()));
                }
                if (StringUtils.isNotBlank(queryDTO.getContent())) {
                    boolQueryBuilder.must(QueryBuilders.wildcardQuery("content", queryDTO.getContent()));
                }
                sourceBuilder.query(boolQueryBuilder);
            } else {
                // 默认全部
                sourceBuilder.query(QueryBuilders.matchAllQuery());
            }

            int page = pagination.getPage() - 1;
            int pageSize = pagination.getPageSize();
            sourceBuilder.from(page * pageSize);
            sourceBuilder.size(pageSize);
            sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

            // 按创建时间排序
            FieldSortBuilder sortBuilder = new FieldSortBuilder("createDate").order(SortOrder.DESC);
            sourceBuilder.sort(sortBuilder);

            //返回字段
            String[] includeFields = new String[]{"id", "title", "labels", "description", "author","createDate"};
            //排除字段
            String[] excludeFields = new String[]{"content"};
            sourceBuilder.fetchSource(includeFields, excludeFields);

            //查询请求
            SearchResponse response = this.search("blog", sourceBuilder);
            // 查询结果
            SearchHits hits = response.getHits();
            SearchHit[] searchHits = hits.getHits();
            for (SearchHit hit : searchHits) {
                BlogPaper responseBlog = JSON.parseObject(hit.getSourceAsString(), BlogPaper.class);
                data.add(BlogPaperMapper.toDTO(responseBlog));
            }
            TotalHits totalHits = hits.getTotalHits();
            long numHits = totalHits.value;
            Map<String, Object> result = new HashMap<>();
            Map<String, Object> newPagination = new HashMap<>();
            newPagination.put("page", page);
            newPagination.put("pageSize", pageSize);
            newPagination.put("total", numHits);
            result.put("pagination", newPagination);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
        return Result.success(data);
    }

    /**
     * 使用分词查询,并分页
     *
     * @return`
     */
    public SearchResponse search(String index, SearchSourceBuilder sourceBuilder) throws IOException {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(index);
        searchRequest.source(sourceBuilder);

        return restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
    }

    /**
     * 更新文档信息
     */
    public void updateDocument(BlogPaper blogPaper) {
        try {
            // 创建索引请求对象
            UpdateRequest updateRequest = new UpdateRequest("blog", blogPaper.getId());
            // 将对象转换为 byte 数组
            byte[] json = JSON.toJSONBytes(blogPaper);
            // 设置更新文档内容
            updateRequest.doc(json, XContentType.JSON);
            // 执行更新文档
            UpdateResponse response = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
            log.info("创建状态：{}", response.status());
        } catch (Exception e) {
            log.error("", e);
        }
    }

    /**
     * 删除文档信息
     */
    public void deleteDocument(String id) {
        try {
            // 创建删除请求对象
            DeleteRequest deleteRequest = new DeleteRequest("blog", id);
            // 执行删除文档
            DeleteResponse response = restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
            log.info("删除状态：{}", response.status());
        } catch (IOException e) {
            log.error("", e);
        }
    }
}
