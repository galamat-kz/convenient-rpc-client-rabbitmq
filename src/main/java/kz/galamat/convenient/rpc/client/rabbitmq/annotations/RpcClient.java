package kz.galamat.convenient.rpc.client.rabbitmq.annotations;

import org.springframework.stereotype.Service;
import org.thepavel.icomponent.Handler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Yersin Mukay on 18.10.2022
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Service
@Handler("rpcClientMethodHandler")
public @interface RpcClient {

    /**
     * The name of the bean.
     */
    String value() default "";

    /**
     * The name of the target queue.
     */
    String queue() default "";

}
