package com.waykichain.rpcdrive.parser;

import com.waykichain.rpcdrive.annotation.EnableRpcServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.*;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.remoting.caucho.HessianServiceExporter;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Sugar
 * Created by 2019/10/12
 **/
public class RpcServiceRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {

    private ResourceLoader resourceLoader;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes annoAttrs = AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(EnableRpcServer.class.getName()));
        ClassPathServiceScanner scanner = new ClassPathServiceScanner(registry);

        if (resourceLoader != null) {
            scanner.setResourceLoader(resourceLoader);
        }

        List<String> basePackages = new ArrayList<String>();

        String uri = annoAttrs.getString("uri");
        for (String pkg : annoAttrs.getStringArray("basePackages")) {
            if (StringUtils.hasText(pkg)) {
                basePackages.add(pkg.replaceAll("\\.", "/"));
            }
        }
        for (Class<?> clazz : annoAttrs.getClassArray("basePackageClasses")) {
            basePackages.add(ClassUtils.getPackageName(clazz));
        }
        scanner.registerFilters();
        Set<BeanDefinitionHolder> beanDefinitionHolders = scanner.doScan(StringUtils.toStringArray(basePackages));
        if (beanDefinitionHolders.isEmpty()) {
            logger.warn("不存在可执行RPC_SERVICE服务");
        } else {
            processBeanDefinitions(uri, beanDefinitionHolders, registry);
        }
    }

    private void processBeanDefinitions(String uri, Set<BeanDefinitionHolder> beanDefinitions, BeanDefinitionRegistry registry) {
        GenericBeanDefinition definition;

        for (BeanDefinitionHolder holder : beanDefinitions) {
            definition = (GenericBeanDefinition) holder.getBeanDefinition();

            logger.debug("Creating RpcServiceBean with name '" + holder.getBeanName()
                    + "' and '" + definition.getBeanClassName() + "' rpcInterface");

            String[] beanDefinitionInterfaceNames = ((ScannedGenericBeanDefinition) definition).getMetadata().getInterfaceNames();
            if (beanDefinitionInterfaceNames.length == 0) {
                logger.warn("rpcService:" + holder.getBeanName() + ",没有实现接口");
                return;
            }
            if (beanDefinitionInterfaceNames.length > 1) {
                logger.info("rpcService:" + holder.getBeanName() + ",实现多个接口,默认使用第一个接口提供rpc服务");
            }
            String interfaceName =  beanDefinitionInterfaceNames[0];
            Class clazz;
            try {
                clazz = Class.forName(interfaceName);
                String serviceName = "/" + uri + "/" + Character.toLowerCase(clazz.getSimpleName().charAt(0)) + clazz.getSimpleName().substring(1);
                BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(HessianServiceExporter.class);
                beanDefinitionBuilder.addPropertyValue("serviceInterface", clazz);
                beanDefinitionBuilder.addDependsOn(holder.getBeanName());
                beanDefinitionBuilder.addPropertyReference("service", holder.getBeanName());
                beanDefinitionBuilder.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_NAME);
                BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(beanDefinitionBuilder.getBeanDefinition(), serviceName);
                BeanDefinitionReaderUtils.registerBeanDefinition(definitionHolder, registry);
            } catch (ClassNotFoundException e) {
                logger.warn("rpcService:" + interfaceName + "-->无法获取类信息");
            }
        }
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

}
