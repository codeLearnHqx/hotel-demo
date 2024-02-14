package cn.itcast.hotel.mq;

import cn.itcast.hotel.constants.MqConstants;
import cn.itcast.hotel.service.IHotelService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Description 监听mq队列数据
 * @Create by hqx
 * @Date 2024/2/14 22:12
 */
@Component
public class HotelListener {

    @Resource
    private IHotelService hotelService;

    /**
     * 监听酒店数据的新增和修改
     * @param id 数据的id
     */
    @RabbitListener(queues = MqConstants.HOTEL_INSERT_QUEUE)
    public void listenHotelInsertOrUpdate(Long id) {
        hotelService.insetById(id);
    }

    /**
     * 监听酒店数据的删除
     * @param id 数据的id
     */
    @RabbitListener(queues = MqConstants.HOTEL_DELETE_QUEUE)
    public void listenHotelDelete(Long id) {
        hotelService.deleteById(id);
    }

}
