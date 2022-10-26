package kz.galamat.convenient.rpc.client.rabbitmq.configurations;

import kz.galamat.convenient.rpc.client.rabbitmq.handlers.RpcClientMethodHandler;
import kz.galamat.convenient.rpc.rabbitmq.settings.RpcProperties;
import kz.galamat.convenient.rpc.client.rabbitmq.services.RpcRequestService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by Yersin Mukay on 14.10.2022
 */
@Configuration
public class SpringAutoConfiguration {

    @Bean
    public RpcRequestService rpcRequestService(RabbitTemplate rabbitTemplate,
                                               RpcProperties rpcProperties) {
        return new RpcRequestService(rabbitTemplate, rpcProperties);
    }

    @Bean
    @ConditionalOnBean(RpcClientMarkerConfiguration.Marker.class)
    public RpcClientMethodHandler rpcClientMethodHandler(RpcRequestService rpcRequestService) {
        return new RpcClientMethodHandler(rpcRequestService);
    }

}
