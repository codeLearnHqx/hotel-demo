package cn.itcast.hotel;

import cn.itcast.hotel.pojo.Hotel;
import cn.itcast.hotel.pojo.HotelDoc;
import cn.itcast.hotel.service.IHotelService;
import com.alibaba.fastjson.JSON;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;


@SpringBootTest
public class HotelDocumentTest {
    @Autowired
    private IHotelService hotelService;
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
    void testAddDocument() throws IOException {
        // 根据id查询酒店数据
        Hotel hotel = hotelService.getById(61083L);
        // 转换成文档类型
        HotelDoc hotelDoc = new HotelDoc(hotel);
        // 1. 准备Request对象
        IndexRequest request = new IndexRequest("hotel").id(hotelDoc.getId().toString());
        // 2. 准备json文档
        request.source(JSON.toJSONString(hotelDoc), XContentType.JSON);
        // 3. 发送请求
        client.index(request, RequestOptions.DEFAULT);
    }

    @Test
    void testGetDocumentById() throws IOException {
        // 1. 准备Request
        GetRequest request = new GetRequest("hotel", "61083");
        // 2. 发送请求，得到相应
        GetResponse response = client.get(request, RequestOptions.DEFAULT);
        // 3. 解析相应结果
        String json = response.getSourceAsString();
        HotelDoc hotelDoc = JSON.parseObject(json, HotelDoc.class);
        System.out.println(hotelDoc);
    }


    @Test
    void testUpdateDocumentById() throws IOException {
        // 1. 创建Request对象
        UpdateRequest request = new UpdateRequest("hotel", "61083");
        // 2. 准备参数，每2个参数为一队  key  value
        request.doc(
                "price", "10086",
                "score", "100"
        );
        // 3. 发送请求，更新文档
        client.update(request, RequestOptions.DEFAULT);
    }

    @Test
    void testDeleteDocument() throws IOException {
        // 1.准备Request
        DeleteRequest request = new DeleteRequest("hotel", "61083");
        // 2.发送请求
        client.delete(request, RequestOptions.DEFAULT);
    }

    @Test
    void testBulkRequest() throws IOException {
        // 批量查询酒店数据
        List<Hotel> hotels = hotelService.list();
        // 转换成文档类型HotelDoc
        List<HotelDoc> hotelDocs = hotels.stream().map(HotelDoc::new).collect(Collectors.toList());
        // 1. 创建Request对象
        BulkRequest request = new BulkRequest();
        // 2. 准备参数，添加多个新增的Request
        hotelDocs.forEach(hotelDoc -> {
            request.add(new IndexRequest("hotel")
                    .id(hotelDoc.getId().toString())
                    .source(JSON.toJSONString(hotelDoc), XContentType.JSON));
        });
        // 3. 发送请求
        client.bulk(request, RequestOptions.DEFAULT);
    }


}
