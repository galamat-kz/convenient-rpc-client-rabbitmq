package kz.galamat.convenient.rpc.client.rabbitmq.annotations;

import kz.galamat.convenient.rpc.client.rabbitmq.configurations.RpcClientMarkerConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;
import org.thepavel.icomponent.InterfaceComponentScan;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@InterfaceComponentScan
@Import(RpcClientMarkerConfiguration.class)
public @interface EnableRpcClient {
    /**
     * Alias for {@link #basePackages()}.
     */
    @AliasFor("basePackages")
    String[] value() default {};

    /**
     * Packages to scan.
     */
    @AliasFor("value")
    String[] basePackages() default {};

    /**
     * The package of each class will be scanned.
     */
    Class<?>[] basePackageClasses() default {};

    /**
     * Interfaces marked with annotation of this type will be registered.
     */
    Class<? extends Annotation> annotation() default Component.class;

    /**
     * Name of the attribute under {@link #annotation()} specifying bean name.
     */
    String beanNameAnnotationAttribute() default "value";
}
