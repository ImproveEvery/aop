package main.com;

import org.springframework.stereotype.Service;

/**
 * Created by Administrator on 2017/11/13 0013.
 */

@Service("userServiceId")
public class UserServiceImpl implements UserService {

    public void addUser() {
        System.out.println("d_aspect.b_anno addUser");
    }

    @LogAnno(operationType = Type.UPDATE,remark = "this is update operation")
    public User updateUser(User user){
        System.out.println(user);
        user.setName("update_"+user.getName());
        return user;
    }
}
