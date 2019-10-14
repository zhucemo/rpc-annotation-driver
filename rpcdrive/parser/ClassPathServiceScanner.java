package com.waykichain.rpcdrive.parser;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Sugar
 * Created by 2019/10/12
 **/
public class ClassPathServiceScanner extends ClassPathBeanDefinitionScanner {

    private Set<BeanDefinitionHolder> beanDefinitions = new LinkedHashSet<>();

    ClassPathServiceScanner(BeanDefinitionRegistry registry) {
        super(registry);
    }

    public ClassPathServiceScanner(BeanDefinitionRegistry registry, boolean useDefaultFilters) {
        super(registry, useDefaultFilters);
    }

    public ClassPathServiceScanner(BeanDefinitionRegistry registry, boolean useDefaultFilters, Environment environment) {
        super(registry, useDefaultFilters, environment);
    }

    public ClassPathServiceScanner(BeanDefinitionRegistry registry, boolean useDefaultFilters, Environment environment, ResourceLoader resourceLoader) {
        super(registry, useDefaultFilters, environment, resourceLoader);
    }

    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        return !beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata().getAnnotationTypes().contains("com.waykichain.rpcdrive.annotation.WaykichainRpcService");
    }

    @Override
    protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
        super.doScan(basePackages);
        return beanDefinitions;
    }

    @Override
    protected boolean checkCandidate(String beanName, BeanDefinition beanDefinition) {
        if (!super.getRegistry().containsBeanDefinition(beanName)) {
            return false;
        }
        BeanDefinition existingDef = super.getRegistry().getBeanDefinition(beanName);
        BeanDefinition originatingDef = existingDef.getOriginatingBeanDefinition();
        if (originatingDef != null) {
            existingDef = originatingDef;
        }
        if (isCompatible(beanDefinition, existingDef)) {
            BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(beanDefinition, beanName);
            beanDefinitions.add(definitionHolder);
            return false;
        }
        throw new RuntimeException("bean检查异常");
    }

    void registerFilters() {
        addIncludeFilter((MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) -> {
            Set<String> annotation = metadataReader.getAnnotationMetadata().getAnnotationTypes();
            for (String annotationName : annotation) {
                if (annotationName.equals("WaykichainRpcService")) return true;
            }
            return false;
        });
    }

}
