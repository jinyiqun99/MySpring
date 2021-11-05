package xyz.jinyiqun.service;

import spring.BeanPostProcessor;
import spring.Component;
import spring.Scope;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@Component("myBeanPostProcessor")
@Scope("singleton")
public class MyBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        System.out.println("初始化前");
        if("userService".equals(beanName)) {
            UserService userService = (UserService) bean;
            userService.setName("Set name in MyBeanPostProcessor...");
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        System.out.println("初始化后...");
        // 简单的AOP实现
        if("userService".equals(beanName)) {
            // JDK动态代理实现AOP
            return Proxy.newProxyInstance(MyBeanPostProcessor.class.getClassLoader(), new Class<?>[]{UserServiceInterface.class}, new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    System.out.println("代理逻辑");
                    return method.invoke(bean, args);
                }
            });
        }
        return bean;
    }
}
