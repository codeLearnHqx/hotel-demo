package cn.itcast.hotel;

import cn.itcast.hotel.pojo.HotelDoc;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.http.HttpHost;
import org.apache.lucene.search.TotalHits;
import org.apache.lucene.util.CollectionUtil;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@SpringBootTest
public class HotelSearchTest {
    private RestHighLevelClient client;

    // 每个测试方法执行之前执行
    @BeforeEach
    void setUp() {
        // 初始化客户端
        this.client = new RestHighLevelClient(RestClient.builder(
                HttpHost.create("http://192.168.10.100:9200")
        ));
    }

    // 每个测试方法执行后执行
    @AfterEach
    void tearDown() throws IOException {
        // 关闭流资源
        this.client.close();
    }

    @Test
    void testMatchAll() throws IOException {
        // 1. 准备Request对象
        SearchRequest request = new SearchRequest("hotel");
        // 2. 准备DSL
        request.source().query(QueryBuilders.matchAllQuery()); // 查询全部数据（默认10条）
        // 3. 发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        // 4. 取出响应结果中的 his 数组中的数据并转成 HotelDoc 类型
        SearchHits hits = response.getHits();
        SearchHit[] searchHits = hits.getHits();
        List<HotelDoc> hotelDocs = Arrays.stream(searchHits)
                // 反序列化
                .map(hit -> JSON.parseObject(hit.getSourceAsString(), HotelDoc.class))
                .collect(Collectors.toList());
        // 打印
        hotelDocs.forEach(System.out::println);

    }

    @Test
    void testMatch() throws IOException {
        // 1. 准备Request对象
        SearchRequest request = new SearchRequest("hotel");
        // 2. 准备DSL
        request.source().query(QueryBuilders.matchQuery("all", "如家")); // 查询全部数据（默认10条）
        // 3. 发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        // 4. 取出响应结果中的 his 数组中的数据并转成 HotelDoc 类型
        SearchHits hits = response.getHits();
        // 打印总数
        System.out.println(hits.getTotalHits().value);
        SearchHit[] searchHits = hits.getHits();
        List<HotelDoc> hotelDocs = Arrays.stream(searchHits)
                // 反序列化
                .map(hit -> JSON.parseObject(hit.getSourceAsString(), HotelDoc.class))
                .collect(Collectors.toList());
        // 打印
        hotelDocs.forEach(System.out::println);

    }

    @Test
    void testMultiMatch() throws IOException {
        // 1. 准备Request对象
        SearchRequest request = new SearchRequest("hotel");
        // 2. 准备DSL
        request.source().query(QueryBuilders.multiMatchQuery("如家", "name", "brand")); // 查询全部数据（默认10条）
        // 3. 发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        // 4. 取出响应结果中的 his 数组中的数据并转成 HotelDoc 类型
        SearchHits hits = response.getHits();
        // 打印总数
        System.out.println(hits.getTotalHits().value);
        SearchHit[] searchHits = hits.getHits();
        List<HotelDoc> hotelDocs = Arrays.stream(searchHits)
                // 反序列化
                .map(hit -> JSON.parseObject(hit.getSourceAsString(), HotelDoc.class))
                .collect(Collectors.toList());
        // 打印
        hotelDocs.forEach(System.out::println);

    }

    @Test
    void testBoolQuery() throws IOException {
        // 1. 准备Request对象
        SearchRequest request = new SearchRequest("hotel");
        // 2. 准备DSL
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        boolQuery.must(QueryBuilders.termQuery("city", "上海"));
        boolQuery.filter(QueryBuilders.rangeQuery("price").lte(250));
        request.source().query(boolQuery); // 查询全部数据（默认10条）
        // 3. 发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        // 4. 取出响应结果中的 his 数组中的数据并转成 HotelDoc 类型
        SearchHits hits = response.getHits();
        // 打印总数
        System.out.println(hits.getTotalHits().value);
        SearchHit[] searchHits = hits.getHits();
        List<HotelDoc> hotelDocs = Arrays.stream(searchHits)
                // 反序列化
                .map(hit -> JSON.parseObject(hit.getSourceAsString(), HotelDoc.class))
                .collect(Collectors.toList());
        // 打印
        hotelDocs.forEach(System.out::println);

    }

    @Test
    void testPageQuery() throws IOException {
        // 1. 准备Request对象
        SearchRequest request = new SearchRequest("hotel");

        // 2. 准备DSL
        request.source().query(QueryBuilders.matchAllQuery());
        // 2.1 排序
        request.source().sort("price", SortOrder.DESC);
        // 2.2 分页
        request.source().from(0).size(5);
        // 3. 发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        // 4. 取出响应结果中的 his 数组中的数据并转成 HotelDoc 类型
        SearchHits hits = response.getHits();
        // 打印总数
        System.out.println(hits.getTotalHits().value);
        SearchHit[] searchHits = hits.getHits();
        List<HotelDoc> hotelDocs = Arrays.stream(searchHits)
                // 反序列化
                .map(hit -> JSON.parseObject(hit.getSourceAsString(), HotelDoc.class))
                .collect(Collectors.toList());
        // 打印
        hotelDocs.forEach(System.out::println);

    }

    @Test
    void testHighlightQuery() throws IOException {
        // 1. 准备Request对象
        SearchRequest request = new SearchRequest("hotel");

        // 2. 准备DSL
        request.source().query(QueryBuilders.matchQuery("all", "如家"));
        // 高亮
        request.source().highlighter(new HighlightBuilder().field("name").requireFieldMatch(false));
        // 3. 发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        // 4. 取出响应结果中的 his 数组中的数据并转成 HotelDoc 类型
        SearchHits hits = response.getHits();
        // 打印总数
        System.out.println(hits.getTotalHits().value);
        SearchHit[] searchHits = hits.getHits();
        List<HotelDoc> hotelDocs = Arrays.stream(searchHits)
                // 反序列化
                .map(hit -> {
                    // 转成 HotelDoc 类型
                    HotelDoc hotelDoc = JSON.parseObject(hit.getSourceAsString(), HotelDoc.class);
                    // 封装高亮字段数据
                    Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                    if (!CollectionUtils.isEmpty(highlightFields)) {
                        HighlightField highlightField = highlightFields.get("name");
                        if (ObjectUtils.isNotEmpty(highlightField)) {
                            String name = highlightField.getFragments()[0].toString();
                            hotelDoc.setName(name);
                        }
                    }
                    return hotelDoc;
                })
                .collect(Collectors.toList());
        // 打印
        hotelDocs.forEach(System.out::println);

    }



}
