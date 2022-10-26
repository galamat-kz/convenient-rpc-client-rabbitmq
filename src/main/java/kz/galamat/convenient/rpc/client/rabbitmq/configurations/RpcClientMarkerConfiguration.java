package kz.galamat.convenient.rpc.client.rabbitmq.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by Yersin Mukay on 19.10.2022
 */
@Configuration(proxyBeanMethods = false)
public class RpcClientMarkerConfiguration {

    @Bean
    public Marker rpcClientMarkerBean() {
        return new Marker();
    }

    class Marker {

    }

}
