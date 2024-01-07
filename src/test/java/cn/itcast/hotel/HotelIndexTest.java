package cn.itcast.hotel;

import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static cn.itcast.hotel.constants.HotelConstants.MAPPING_TEMPLATE;

@SpringBootTest
public class HotelIndexTest {

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
    void testInit() {
        System.out.println(client);
    }

    @Test
    void createHotelIndex() throws IOException {
        // 1. 创建Request对象
        CreateIndexRequest request = new CreateIndexRequest("hotel");
        // 2. 准备请求的参数：DSL语句
        request.source(MAPPING_TEMPLATE, XContentType.JSON);
        // 3. 发送请求
        client.indices().create(request, RequestOptions.DEFAULT);
    }


    @Test
    void deleteHotelIndex() throws IOException {
        // 1. 创建Request对象
        DeleteIndexRequest request = new DeleteIndexRequest("hotel");
        // 2. 发送请求
        client.indices().delete(request, RequestOptions.DEFAULT);
    }

    @Test
    void existHotelIndex() throws IOException {
        // 1. 创建Request对象
        GetIndexRequest request = new GetIndexRequest("hotel");
        // 2. 发送请求
        boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
        // 3. 查看结果
        System.out.println(exists);
    }

}
