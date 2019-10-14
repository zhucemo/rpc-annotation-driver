package com.waykichain.rpcdrive.parser;

import com.caucho.hessian.client.HessianProxyFactory;
import org.springframework.beans.factory.FactoryBean;

/**
 * @author Sugar
 * Created by 2019/10/12
 **/
public class ClientMaker<T> implements FactoryBean<T> {

    private String url;
    private Class<T> rpcClazz;

    @Override
    public T getObject() throws Exception {
        HessianProxyFactory factory = new HessianProxyFactory();
        factory.setOverloadEnabled(true);
        String beanName = Character.toLowerCase(rpcClazz.getSimpleName().charAt(0)) + rpcClazz.getSimpleName().substring(1);
        return (T) factory.create(rpcClazz, url + beanName);
    }

    @Override
    public Class<?> getObjectType() {
        return rpcClazz;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setRpcClazz(Class<T> rpcClazz) {
        this.rpcClazz = rpcClazz;
    }
}
