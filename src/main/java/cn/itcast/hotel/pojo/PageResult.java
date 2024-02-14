package cn.itcast.hotel.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 返回前端的查询分页信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResult {
    private Long total;
    private List<HotelDoc> hotels;
}
