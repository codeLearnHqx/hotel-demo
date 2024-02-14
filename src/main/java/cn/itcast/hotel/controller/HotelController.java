package cn.itcast.hotel.controller;

import cn.itcast.hotel.pojo.Hotel;
import cn.itcast.hotel.pojo.PageResult;
import cn.itcast.hotel.pojo.RequestParams;
import cn.itcast.hotel.service.IHotelService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @Description
 * @Create by hqx
 * @Date 2024/1/7 13:51
 */
@RestController
@RequestMapping("/hotel")
public class HotelController {

    @Resource
    private IHotelService hotelService;


    @PostMapping("/list")
    public PageResult search(@RequestBody RequestParams params) {
        return hotelService.search(params);
    }

    @PostMapping("/filters")
    public Map<String, List<String>> getFilters(@RequestBody RequestParams params) {
        Map<String, List<String>> filters = hotelService.filters(params);
        return filters;
    }

    @GetMapping("/suggestion")
    public List<String> getSuggestions(@RequestParam("key") String prefix) {
        return hotelService.getSuggestions(prefix);
    }


}
