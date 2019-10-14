package com.waykichain.rpcdrive.annotation;

import com.waykichain.rpcdrive.parser.RpcServiceRegistrar;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * @author Sugar
 * Created by 2019/10/12
 **/
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(RpcServiceRegistrar.class)
public @interface EnableRpcServer {
    @AliasFor("value")
    String[] basePackages() default {};

    @AliasFor("basePackages")
    String[] value() default {};

    Class<?>[] basePackageClasses() default {};

    String uri() default "";
}
