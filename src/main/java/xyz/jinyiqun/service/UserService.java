package xyz.jinyiqun.service;

import spring.*;

@Component("userService")
@Scope("singleton")
public class UserService implements BeanNameAware, InitializingBean, UserServiceInterface {
    @Autowired
    private OrderService orderService;

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void test() {
        System.out.println(orderService);
    }

    @Override
    public void setBeanName(String name) {
        // Spring调用这个方法，这里的逻辑由程序员控制
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // Spring调用此方法，这里的逻辑由程序员控制
    }
}
