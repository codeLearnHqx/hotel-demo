package cn.itcast.hotel.service.impl;

import cn.itcast.hotel.mapper.HotelMapper;
import cn.itcast.hotel.pojo.Hotel;
import cn.itcast.hotel.pojo.HotelDoc;
import cn.itcast.hotel.pojo.PageResult;
import cn.itcast.hotel.pojo.RequestParams;
import cn.itcast.hotel.service.IHotelService;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
public class HotelService extends ServiceImpl<HotelMapper, Hotel> implements IHotelService {


    @Resource
    private RestHighLevelClient client;

    @Override
    public PageResult search(RequestParams params) {

        String key = params.getKey();
        Integer page = params.getPage();
        String sortBy = params.getSortBy();
        Integer size = params.getSize();
        // 1. 准备Request请求
        SearchRequest request = new SearchRequest("hotel");
        // 2. 准备DSL
        buildBasicQuery(params, key, request);
        // 2.2 排序 （按照地理坐标排序）
        String location = params.getLocation();
        if (location != null && !"".equals(location)) {
            request.source().sort(SortBuilders
                    .geoDistanceSort("location", new GeoPoint(location))
                    .order(SortOrder.ASC)
                    .unit(DistanceUnit.KILOMETERS)
            );
        }
        // 2.3 分页参数
        request.source().from((page - 1) * size).size(size);
        // 3. 发送请求
        try {
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            // 4. 解析响应
            SearchHits hits = response.getHits();
            // 总数
            long total = hits.getTotalHits().value;
            SearchHit[] searchHits = hits.getHits();
            List<HotelDoc> hotelDocs = Arrays.stream(searchHits)
                    .map(hit -> {
                        // 反序列化
                        HotelDoc hotelDoc = JSON.parseObject(hit.getSourceAsString(), HotelDoc.class);
                        // 获取排序值
                        Object[] sortValues = hit.getSortValues();
                        if (sortValues.length > 0) {
                            Object sortValue = sortValues[0];
                            hotelDoc.setDistance(sortValue);
                        }
                        return hotelDoc;
                    })
                    .collect(Collectors.toList());
            return new PageResult(total, hotelDocs);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Map<String, List<String>> filters(RequestParams params) {
        try {
            // 1. 准备Request
            SearchRequest request = new SearchRequest("hotel");
            // 2. 准备DSL
            // 2.1 query （保证聚合时的查询条件和搜索的查询条件一致）
            buildBasicQuery(params, "", request);
            // 2.2 设置size
            request.source().size(0); // 只需要聚合结果，不需要文档
            // 2.3 聚合
            buildAggregation(request);
            // 3. 发出请求
            SearchResponse response = null;
            response = client.search(request, RequestOptions.DEFAULT);
            // 4. 解析结果
            Aggregations aggregations = response.getAggregations();
            HashMap<String, List<String>> map = new HashMap<>();
            // 根据聚合名称获取聚合结果
            List<String> brandList = getAggByName(aggregations, "brandAgg");
            List<String> cityList = getAggByName(aggregations, "cityAgg");
            List<String> startList = getAggByName(aggregations, "starAgg");

            // 4.4 放入map
            map.put("brand", brandList);
            map.put("city", cityList);
            map.put("starName", startList);
            return map;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> getSuggestions(String prefix) {
        try {
            // 1. 准备Request
            SearchRequest request = new SearchRequest("hotel");
            // 2. 准备DSL
            request.source().suggest(new SuggestBuilder().addSuggestion(
                    "suggestions", // 自动补全的查询名称
                    SuggestBuilders.completionSuggestion("suggestion")
                            .prefix(prefix)
                            .skipDuplicates(true)
                            .size(10)
            ));
            // 3. 发起请求
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            // 4. 解析结果
            Suggest suggest = response.getSuggest();
            // 4.1 根据自动补全的查询名称获取补全结果
            CompletionSuggestion suggestion = suggest.getSuggestion("suggestions");
            // 4.2 获取options
            List<CompletionSuggestion.Entry.Option> options = suggestion.getOptions();
            // 4.3 将结果集转换成集合
            List<String> result = options
                    .stream()
                    .map(option -> option.getText().toString())
                    .collect(Collectors.toList());

            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void insetById(Long id) {
        try {
            // 1. 根据id查询酒店数据
            Hotel hotel = this.getById(id);
            // 2. 转成文档类型
            HotelDoc hotelDoc = new HotelDoc(hotel);
            // 3. 准备request
            IndexRequest request = new IndexRequest("hotel").id(hotel.getId().toString());
            // 4. 准备json文档
            request.source(JSON.toJSONString(hotelDoc), XContentType.JSON);
            // 5. 发送请求
            client.index(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteById(Long id) {
        try {
            // 1. 准备Request
            DeleteRequest request = new DeleteRequest("hotel", id.toString());
            // 2. 准备发送请求
            DeleteResponse response = client.delete(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 通过聚合名称获取聚合结果
     */
    private static List<String> getAggByName(Aggregations aggregations, String aggName) {
        // 4.1 根据聚合名称获取聚合结果
        Terms terms = aggregations.get(aggName);
        // 4.2 获取buckets（聚合结果集）
        List<? extends Terms.Bucket> buckets = terms.getBuckets();

        // 4.3 遍历
        List<String> list = buckets
                .stream()
                .map(bucket -> bucket.getKeyAsString())
                .collect(Collectors.toList());
        return list;
    }

    /**
     * 聚合多个字段
     */
    private static void buildAggregation(SearchRequest request) {
        request.source().aggregation(AggregationBuilders
                .terms("brandAgg") // 聚合名称（自定义）
                .field("brand") // 需要对该字段进行聚合
                .size(100) // 聚合结果数量
        );
        request.source().aggregation(AggregationBuilders
                .terms("cityAgg") // 聚合名称（自定义）
                .field("city") // 需要对该字段进行聚合
                .size(100) // 聚合结果数量
        );
        request.source().aggregation(AggregationBuilders
                .terms("starAgg") // 聚合名称（自定义）
                .field("starName") // 需要对该字段进行聚合
                .size(100) // 聚合结果数量
        );
    }

    private static void buildBasicQuery(RequestParams params, String key, SearchRequest request) {
        // 1. 构建 BooleanQuery (多条件使用复合查询)
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        // 关键字搜索 must
        if (key == null || "".equals(key)) { // key为空
            boolQuery.must(QueryBuilders.matchAllQuery());
        } else {
            boolQuery.must(QueryBuilders.matchQuery("all", key));
        }
        // 关键字搜索 filter
        // 条件过滤
        if (params.getCity() != null && !"".equals(params.getCity())) {
            boolQuery.filter(QueryBuilders.termQuery("city", params.getCity()));
        }
        // 品牌过滤
        if (params.getBrand() != null && !"".equals(params.getBrand())) {
            boolQuery.filter(QueryBuilders.termQuery("brand", params.getBrand()));
        }
        // 星级过滤
        if (params.getStartName() != null && !"".equals(params.getStartName())) {
            boolQuery.filter(QueryBuilders.termQuery("startName", params.getStartName()));
        }
        // 范围搜索 range
        // 价格过滤
        if (params.getMinPrice() != null && params.getMaxPrice() != null) {
            boolQuery.filter(QueryBuilders.rangeQuery("price").gte(params.getMinPrice()).lte(params.getMaxPrice()));
        }

        // 2. 算分控制
        FunctionScoreQueryBuilder functionScoreQueryBuilder = QueryBuilders.functionScoreQuery(
                // 原始查询
                boolQuery,
                // function score 的数组
                new FunctionScoreQueryBuilder.FilterFunctionBuilder[]{
                        // 文档存在属性isAD=true，算分会乘以10
                        // 其中的一个 function score 元素
                        new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                                // 过滤条件
                                QueryBuilders.termQuery("isAD", true),
                                // 算分函数
                                ScoreFunctionBuilders.weightFactorFunction(10)
                        )
                }
        );

        // 放入 source
        request.source().query(functionScoreQueryBuilder);
    }
}
