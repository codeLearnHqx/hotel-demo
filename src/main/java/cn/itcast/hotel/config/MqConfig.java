package cn.itcast.hotel.config;

import cn.itcast.hotel.constants.MqConstants;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description mq 配置类
 * @Create by hqx
 * @Date 2024/2/14 21:55
 */
@Configuration
public class MqConfig {


    /**
     * 定义主题交换机
     */
    @Bean
    public TopicExchange topicExchange() {
        return ExchangeBuilder
                .topicExchange(MqConstants.HOTEL_EXCHANGE) // 交换机名称
                .durable(true) // 持久化
                .build();
    }

    /**
     * 监新增和删除数据的队列
     */
    @Bean
    public Queue insertQueue() {
        return QueueBuilder
                .durable(MqConstants.HOTEL_INSERT_QUEUE)
                .build();
    }

    /**
     * 监听删除数据的队列
     */
    @Bean
    public Queue deleteQueue() {
        return QueueBuilder
                .durable(MqConstants.HOTEL_DELETE_QUEUE)
                .build();
    }

    /**
     * 绑定inset队列和主题交换机
     */
    @Bean
    public Binding insertQueueBinding(TopicExchange topicExchange, Queue insertQueue) {
        return BindingBuilder.bind(insertQueue).to(topicExchange).with(MqConstants.HOTEL_INSERT_KEY);
    }

    /**
     * 绑定delete队列和主题交换机
     */
    @Bean
    public Binding deleteQueueBinding(TopicExchange topicExchange, Queue deleteQueue) {
        return BindingBuilder.bind(deleteQueue).to(topicExchange).with(MqConstants.HOTEL_DELETE_KEY);
    }

}
