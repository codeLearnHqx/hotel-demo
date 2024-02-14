package cn.itcast.hotel.service;

import cn.itcast.hotel.pojo.Hotel;
import cn.itcast.hotel.pojo.PageResult;
import cn.itcast.hotel.pojo.RequestParams;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

public interface IHotelService extends IService<Hotel> {

    /**
     * 搜索内容（分页）
     * @param params 搜索内容请求参数
     * @return 分页内容
     */
    PageResult search(RequestParams params);


    /**
     * 获取条件聚合后的结果
     */
    Map<String, List<String>> filters(RequestParams params);

    /**
     * 获取自动补全结果
     */
    List<String> getSuggestions(String prefix);

    void insetById(Long id);

    void deleteById(Long id);
}
