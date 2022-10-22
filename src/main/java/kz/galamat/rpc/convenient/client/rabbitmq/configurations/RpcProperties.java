package kz.galamat.rpc.convenient.client.rabbitmq.configurations;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.rabbit.rpc")
@Data
public class RpcProperties {

    private String exchange;

}
