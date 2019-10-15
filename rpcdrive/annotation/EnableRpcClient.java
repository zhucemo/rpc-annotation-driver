package com.waykichain.rpcdrive.annotation;

import com.waykichain.rpcdrive.parser.RpcClientRegistrar;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * @author Sugar
 * Created by 2019/10/11
 **/
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(RpcClientRegistrar.class)
public @interface EnableRpcClient {

    @AliasFor("value")
    String[] basePackages() default {};

    @AliasFor("basePackages")
    String[] value() default {};

    Class<?>[] basePackageClasses() default {};

    String[] urls() default "";
}
