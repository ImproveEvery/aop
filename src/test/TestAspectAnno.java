import javafx.application.Application;
import main.com.User;
import main.com.UserService;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by Administrator on 2017/11/13 0013.
 */
public class TestAspectAnno {
    @Test
    public void demo01(){
        String xmlPath = "beans.xml";
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext(xmlPath);

        //获得目标类
        UserService userService = (UserService) applicationContext.getBean("userServiceId");
        userService.addUser();
        User user=new User();
        user.setName("gds");
        user.setSubject("ComputerScience");
        user.setScore(98);
        userService.updateUser(user);
    }

}
