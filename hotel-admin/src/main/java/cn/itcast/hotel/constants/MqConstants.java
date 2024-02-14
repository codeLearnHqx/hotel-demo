package cn.itcast.hotel.constants;

/**
 * @Description
 * @Create by hqx
 * @Date 2024/2/14 21:50
 */
public class MqConstants {
    /**
     * 交换机
     */
    public final static String HOTEL_EXCHANGE = "hotel.topic";
    /**
     * 监听新增和修改的队列
     */
    public final static String HOTEL_INSERT_QUEUE = "hotel.insert.queue";
    /**
     * 监听删除的队列
     */
    public final static String HOTEL_DELETE_QUEUE = "hotel.delete.queue";
    /**
     * 新增或者修改的 routingKey
     */
    public final static String HOTEL_INSERT_KEY = "hotel.inset";
    /**
     * 删除的 routingKey
     */
    public final static String HOTEL_DELETE_KEY = "hotel.delete";

}
