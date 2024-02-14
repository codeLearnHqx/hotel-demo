package cn.itcast.hotel.pojo;

import lombok.Data;

/**
 * 前端查询请求参数
 */
@Data
public class RequestParams {
    private String key;
    private Integer page;
    private Integer size;
    private String sortBy;
    private String city;
    private String brand;
    private String startName;
    private Integer minPrice;
    private Integer maxPrice;
    private String location;

}
