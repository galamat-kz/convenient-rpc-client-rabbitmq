package kz.galamat.rpc.convenient.client.rabbitmq.annotations;

import kz.galamat.rpc.convenient.client.rabbitmq.configurations.RpcClientMarkerConfiguration;
import org.springframework.context.annotation.Import;
import org.thepavel.icomponent.InterfaceComponentScan;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@InterfaceComponentScan
@Import(RpcClientMarkerConfiguration.class)
public @interface EnableRpcClient {

}
