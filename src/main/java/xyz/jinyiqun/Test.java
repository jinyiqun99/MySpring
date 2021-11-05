package xyz.jinyiqun;

import spring.MyApplicationContext;
import xyz.jinyiqun.service.UserService;
import xyz.jinyiqun.service.UserServiceInterface;

public class Test {
    public static void main(String[] args) throws ClassNotFoundException {
        MyApplicationContext applicationContext = new MyApplicationContext(AppConfig.class);
        UserServiceInterface userService = (UserServiceInterface) applicationContext.getBean("userService");
        userService.test();
    }
}
