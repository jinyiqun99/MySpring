package spring;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MyApplicationContext {
    private final Class<?> configClass;
    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>();
    private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    private final List<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();

    public MyApplicationContext(Class<?> configClass) throws ClassNotFoundException {
        this.configClass = configClass;
        scan(configClass);
        // 对于单例Bean，容器启动时就创建好
        for(String beanName : beanDefinitionMap.keySet()) {
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if(beanDefinition.getScope().equals("singleton")) {
                Object bean = createBean(beanName, beanDefinition);
                singletonObjects.put(beanName, bean);
            }
        }
    }

    private Object createBean(String beanName, BeanDefinition beanDefinition) {
        Class<?> clazz = beanDefinition.getClazz();
        try {
            Object instance = clazz.getDeclaredConstructor().newInstance();
            // 依赖注入
            for(Field field :  clazz.getDeclaredFields()) {
                 if(field.isAnnotationPresent(Autowired.class)) {
                     field.setAccessible(true);
                     Object prop = getBean(field.getName());
                     field.set(instance, prop);
                 }
            }
            // BeanNameWare回调
            if(instance instanceof BeanNameAware) {
                ((BeanNameAware)instance).setBeanName(beanName);
            }
            // 初始化前
            for(BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                instance = beanPostProcessor.postProcessBeforeInitialization(instance, beanName);
            }
            // 初始化
            if(instance instanceof InitializingBean) {
                ((InitializingBean)instance).afterPropertiesSet();
            }
            // 初始化后
            for(BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                instance = beanPostProcessor.postProcessAfterInitialization(instance, beanName);
            }
            return instance;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void scan(Class<?> configClass) throws ClassNotFoundException {
        // 解析配置
        ComponentScan componentScan = configClass.getAnnotation(ComponentScan.class);
        String path = componentScan.value();
        // 扫描加了@Component注解的类
        ClassLoader classLoader = MyApplicationContext.class.getClassLoader();
        URL resource = classLoader.getResource("xyz/jinyiqun/service");
        File serviceDir = new File(resource.getFile());
        if (serviceDir.isDirectory()) {
            File[] files = serviceDir.listFiles();
            for (File file : files) {
                String filename = file.getAbsolutePath();
                String classname = filename.substring(filename.indexOf("xyz"), filename.indexOf(".class")).replaceAll("/", ".");
                Class<?> clazz = classLoader.loadClass(classname);
                if (clazz.isAnnotationPresent(Component.class)) {
                    // BeanPostProcessor
                    if(BeanPostProcessor.class.isAssignableFrom(clazz)) {
                        // clazz是否实现了BeanPostProcessor，注意这里是判断类
                        try {
                            BeanPostProcessor instance = (BeanPostProcessor) clazz.getDeclaredConstructor().newInstance();
                            beanPostProcessorList.add(instance);
                        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                            e.printStackTrace();
                        }
                    }

                    // 当前类是一个Bean
                    // 判断单例还是原型，解析类，生成BeanDefinition对象
                    // BeanDefinition
                    Component component = clazz.getAnnotation(Component.class);
                    String beanName = component.value();
                    Scope scope = clazz.getDeclaredAnnotation(Scope.class);
                    String scopeType = scope.value();

                    BeanDefinition definition = new BeanDefinition();
                    definition.setClazz(clazz);
                    definition.setScope(scopeType);
                    beanDefinitionMap.put(beanName, definition);
                }
            }
        }
    }

    public Object getBean(String name) {
        if (beanDefinitionMap.containsKey(name)) {
            BeanDefinition beanDefinition = beanDefinitionMap.get(name);
            if (beanDefinition.getScope().equals("singleton")) {
                // 单例Bean
                return singletonObjects.get(name);
            } else {
                // 原型Bean
                return createBean(name, beanDefinition);
            }
        } else {
            throw new NullPointerException();
        }
    }
}
