package cn.itcast.hotel.controller;

import cn.itcast.hotel.pojo.Hotel;
import cn.itcast.hotel.service.IHotelService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

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

    @GetMapping("/{id}")
    public Hotel getHotel(@PathVariable Long id) {
        return hotelService.getById(id);
    }



}
