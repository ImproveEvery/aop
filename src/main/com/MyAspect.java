package main.com;

/**
 * Created by Administrator on 2017/11/13 0013.
 */

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 切面类，含有多个通知
 */
@Aspect
@Component
public class MyAspect {
    //声明公共切入点
    @Pointcut("execution(* main.com.UserServiceImpl.*(..))")
    private void PointCut1(){}

    //声明指定包内的注解切入点
    @Pointcut("@annotation(main.com.LogAnno) && execution(* main.com.*.*(..)) ")
    private void PointCutofAnno(){}

    //拦截返回值
    @AfterReturning(value="PointCut1()" ,returning="ret")
    public void myAfterReturning(JoinPoint joinPoint, Object ret){
        System.out.println("后置通知 ： " + joinPoint.getSignature().getName() + " , -->" + ret);
    }

    //拦截@注解的方法
    @AfterReturning(value="PointCutofAnno()",returning = "ret")
    public void myAfterReturningofAnno (JoinPoint joinPoint,Object ret){
        //获取参数
        Object[] objs=joinPoint.getArgs();
        //获取返回值
        Object obj=objs[0];
        Map<String ,Object> inMap= getParameter(obj);
        Map<String ,Object> outMap= getParameter(ret);
        System.out.println(inMap);
        System.out.println(outMap);
    }

    @Before(value="PointCutofAnno()")
    public void myBeforeReturningofAnno (JoinPoint joinPoint){
        //获取参数
        Object[] objs=joinPoint.getArgs();
        //获取返回值
        Object obj=objs[0];
        Map<String ,Object> inMap= getParameter(obj);
        System.out.println(inMap);
    }

    //拓展日志的功能，对拦截的入参进行反射获取信息
    private Map<String, Object> getParameter(Object obj) {
        try {
            //反射对象中的属性
            Class clazz=obj.getClass();
            Field[] fields= clazz.getDeclaredFields();
            Map<String,Object> resultMap=new HashMap<String, Object>();
            //遍历并返回
            for(Field field:fields){
                String fieldName=field.getName();
                PropertyDescriptor pd=new PropertyDescriptor(fieldName,clazz);
                Method readMethod = pd.getReadMethod();
                Object resultObj= readMethod.invoke(obj);
                resultMap.put(fieldName,resultObj);
            }
            return resultMap;
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
