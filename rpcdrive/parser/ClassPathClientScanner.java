package com.waykichain.rpcdrive.parser;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;

import java.util.Set;

/**
 * @author Sugar
 * Created by 2019/10/11
 **/
public class ClassPathClientScanner extends ClassPathBeanDefinitionScanner {

    ClassPathClientScanner(BeanDefinitionRegistry registry) {
        super(registry);
    }

    public ClassPathClientScanner(BeanDefinitionRegistry registry, boolean useDefaultFilters) {
        super(registry, useDefaultFilters);
    }

    public ClassPathClientScanner(BeanDefinitionRegistry registry, boolean useDefaultFilters, Environment environment) {
        super(registry, useDefaultFilters, environment);
    }

    public ClassPathClientScanner(BeanDefinitionRegistry registry, boolean useDefaultFilters, Environment environment, ResourceLoader resourceLoader) {
        super(registry, useDefaultFilters, environment, resourceLoader);
    }

    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        return beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata().isIndependent();
    }

    void doScan(String url, String... basePackages) {
        Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);

        if (beanDefinitions.isEmpty()) {
            logger.warn("不存在可执行RPC服务");
        } else {
            processBeanDefinitions(url, beanDefinitions);
        }

    }

    private void processBeanDefinitions(String url, Set<BeanDefinitionHolder> beanDefinitions) {
        GenericBeanDefinition definition;

        for (BeanDefinitionHolder holder : beanDefinitions) {
            definition = (GenericBeanDefinition) holder.getBeanDefinition();

            logger.debug("Creating RpcClientBean with name '" + holder.getBeanName()
                    + "' and '" + definition.getBeanClassName() + "' rpcInterface");

            Class clazz;
            String beanClass = definition.getBeanClassName();
            try {
                clazz = Class.forName(beanClass);
                definition.getPropertyValues().add("rpcClazz", clazz);
                definition.getPropertyValues().add("url", url);
                definition.setBeanClass(ClientMaker.class);
                definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);

                /*//获取传递invocationHandler对象来创建bean
                factory.create(clazz, url + beanName).getClass().getSuperclass().getDeclaredField("h");
                constructorArgumentValues.addGenericArgumentValue();
                definition.setConstructorArgumentValues();
                definition.getConstructorArgumentValues().addGenericArgumentValue(definition.getBeanClassName());*/

            } catch (ClassNotFoundException e) {
                logger.error(holder.getBeanName() + " RPC类无法加载", e);
            }
        }
    }

    void registerFilters() {
        addIncludeFilter((MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) -> {
            return true;
        });
    }
}
