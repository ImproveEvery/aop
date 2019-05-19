在软件业，AOP为Aspect Oriented Programming的缩写，意为：[面向切面编程](https://baike.baidu.com/item/%E9%9D%A2%E5%90%91%E5%88%87%E9%9D%A2%E7%BC%96%E7%A8%8B)，通过[预编译](https://baike.baidu.com/item/%E9%A2%84%E7%BC%96%E8%AF%91)方式和运行期动态代理实现程序功能的统一维护的一种技术。AOP是[OOP](https://baike.baidu.com/item/OOP)的延续，是软件开发中的一个热点，也是[Spring](https://baike.baidu.com/item/Spring)框架中的一个重要内容，是[函数式编程](https://baike.baidu.com/item/%E5%87%BD%E6%95%B0%E5%BC%8F%E7%BC%96%E7%A8%8B)的一种衍生范型。利用AOP可以对业务逻辑的各个部分进行隔离，从而使得业务逻辑各部分之间的[耦合度](https://baike.baidu.com/item/%E8%80%A6%E5%90%88%E5%BA%A6)降低，提高程序的可重用性，同时提高了开发的效率。下图就是AOP的核心概念和学习路线图，掌握此图是关键：

![AOP架构图](http://upload-images.jianshu.io/upload_images/8926909-30fb6d79de88db72.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
#### 一、方案举例
想象一下下，写了一个功能代码（比如SayHello()），想要在函数前后都做点什么，最简单的就是去写一段硬编码：
### 1.写死代码
这是功能的接口
```java
public interface Greeting {
    void sayHello(String name);
}
```
在实现类里边去增加前置方法和后置方法
```java
public class GreetingImpl implements Greeting { 
    @Override
    public void sayHello(String name) {
        before();
        System.out.println("Hello! " + name);
        after();
    }
    private void before() {
        System.out.println("Before");
    }
    private void after() {
        System.out.println("After");
    }
}
```
比如我们要统计每个方法的执行时间，以对性能作出评估，那是不是要在每个方法的一头一尾都做点手脚呢？这样写死的方法会累死码农们，于是来一个加强版。
#### 2.静态代理
单独为 GreetingImpl 这个类写一个代理类，接口还是未变，实现类抽取出来放一边，这样就进行了解耦，后置和前置功能的实现放到这个静态代理类中去绑定结合：
```java
//Greeting接口的实现类
public class GreetingImpl implements Greeting { 
    @Override
    public void sayHello(String name) {
        System.out.println("Hello! " + name);
    }
}

//绑定前置和后置方法的静态代理类
public class GreetingProxy implements Greeting {
    private GreetingImpl greetingImpl;
    public GreetingProxy(GreetingImpl greetingImpl) {
        this.greetingImpl = greetingImpl;
    }
    @Override
    public void sayHello(String name) {
        before();
        greetingImpl.sayHello(name);
        after();
    }
    private void before() {
        System.out.println("Before");
    }
    private void after() {
        System.out.println("After");
    }
}
```
用这个 GreetingProxy 去代理 GreetingImpl，下面看看客户端如何来调用：
```java
public class Client {
    public static void main(String[] args) {
        Greeting greetingProxy = new GreetingProxy(new GreetingImpl());
        greetingProxy.sayHello("Jack");
    }
}
```
这样写没错，但是有个问题，每增强一个功能接口的实现类都要去实现一遍这个实现类的Proxy代理方法，最后的结果就会导致XxxProxy 这样的类会越来越多，如何才能将这些代理类尽可能减少呢？最好只有一个代理类。这时我们就需要使用 JDK 提供的动态代理了。

#### 3.JDK代理
所有的代理类都合并到动态代理类中了，该代理类设置为泛型，可以接收各种实现类，经过代理类生成后可以绑定结合前置、后置方法，进行功能增强。
```
public class JDKDynamicProxy implements InvocationHandler {
 
    private Object target;
 
    public JDKDynamicProxy(Object target) {
        this.target = target;
    }
 
    @SuppressWarnings("unchecked")
    public <T> T getProxy() {
        return (T) Proxy.newProxyInstance(
            target.getClass().getClassLoader(),
            target.getClass().getInterfaces(),
            this
        );
    }
 
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        before();
        Object result = method.invoke(target, args);
        after();
        return result;
    }
 
    private void before() {
        System.out.println("Before");
    }
 
    private void after() {
        System.out.println("After");
    }
}
```
客户端的调用
```java
public class Client {
    public static void main(String[] args) {
        Greeting greeting = new JDKDynamicProxy(new GreetingImpl()).getProxy();
        greeting.sayHello("Jack");
    }
}
```
所有的代理类都合并到动态代理类中了，但这样做仍然存在一个问题：JDK 给我们提供的动态代理只能代理接口，而不能代理没有接口的类。

#### 4.cglib动态代理
我们使用开源的 CGLib 类库可以代理没有接口的类，这样就弥补了 JDK 的不足。
```java
public class CGLibDynamicProxy implements MethodInterceptor {
 
    private static CGLibDynamicProxy instance = new CGLibDynamicProxy();
 
    private CGLibDynamicProxy() {
    }
 
    public static CGLibDynamicProxy getInstance() {
        return instance;
    }
 
    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> cls) {
        return (T) Enhancer.create(cls, this);
    }
 
    @Override
    public Object intercept(Object target, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        before();
        Object result = proxy.invokeSuper(target, args);
        after();
        return result;
    }
 
    private void before() {
        System.out.println("Before");
    }
 
    private void after() {
        System.out.println("After");
    }
}
```
客户端调用也更加轻松了
```java
public class Client {
 
    public static void main(String[] args) {
        Greeting greeting = CGLibDynamicProxy.getInstance().getProxy(GreetingImpl.class);
        greeting.sayHello("Jack");
    }
}
```
### 二、AOP的概念
**切面（Aspect）**：其实就是共有功能的实现。如日志切面、权限切面、事务切面等。在实际应用中通常是一个存放共有功能实现的普通Java类，之所以能被AOP容器识别成切面，是在配置中指定的。

**增强（Advice）**：是切面的具体实现。以目标方法为参照点，根据放置的地方不同，可分为前置通知（Before）、后置通知（AfterReturning）、异常通知（AfterThrowing）、最终通知（After）与环绕通知（Around）5种。在实际应用中通常是切面类中的一个方法，具体属于哪类通知，同样是在配置中指定的。

**连接点（Joinpoint）**：就是程序在运行过程中能够插入切面的地点。例如，方法调用、异常抛出或字段修改等，但[spring](http://lib.csdn.net/base/17 "Java EE知识库")只支持方法级的连接点。

**切入点（Pointcut）**：用于定义通知应该切入到哪些连接点上。不同的通知通常需要切入到不同的连接点上，这种精准的匹配是由切入点的正则表达式来定义的。

**目标对象（Target）**：就是那些即将切入切面的对象，也就是那些被通知的对象。这些对象中已经只剩下干干净净的核心业务逻辑代码了，所有的共有功能代码等待AOP容器的切入。

**代理对象（Proxy）**：将通知应用到目标对象之后被动态创建的对象。可以简单地理解为，代理对象的功能等于目标对象的核心业务逻辑功能加上共有功能。代理对象对于使用者而言是透明的，是程序运行过程中的产物。

**织入（Weaving）**：将切面应用到目标对象从而创建一个新的代理对象的过程。这个过程可以发生在编译期、类装载期及运行期，当然不同的发生点有着不同的前提条件。譬如发生在编译期的话，就要求有一个支持这种AOP实现的特殊编译器；发生在类装载期，就要求有一个支持AOP实现的特殊类装载器；只有发生在运行期，则可直接通过Java语言的反射机制与动态代理机制来动态实现。还有引入（Introduction），用以区分类和方法的拦截。

#### 1.项目aop的实现
项目中aop的实现基于两种模式或者综合，Spring AspectJ+（execution拦截表达式、基于@Annotation的注解拦截）
##### (1).Spring + AspectJ（基于注解：通过 AspectJ execution 表达式拦截方法）
通过表达式去匹配各个符合条件的切入点，如下就实现了aop.demo.GreetingImpl类下的任意方法拦截匹配，那么匹配后的切入点都会织入代理中。
```java
@Aspect
@Component
public class GreetingAspect {
 
    @Around("execution(* aop.demo.GreetingImpl.*(..))")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        before();
        Object result = pjp.proceed();
        after();
        return result;
    }
 
    private void before() {
        System.out.println("Before");
    }
 
    private void after() {
        System.out.println("After");
    }
}
```
类上面标注的 @Aspect 注解，这表明该类是一个 Aspect（其实就是 Advisor）。该类无需实现任何的接口，只需定义一个方法（方法叫什么名字都无所谓），只需在方法上标注 @Around 注解，在注解中使用了 AspectJ 切点表达式。方法的参数中包括一个 ProceedingJoinPoint 对象，它在 AOP 中称为 Joinpoint（连接点），可以通过该对象获取方法的任何信息，例如：方法名、参数等。

虽然有了execution拦截表达式，但是有时候我们只想满足精确的某些方法，靠表达式难免会有缺漏或者是带入了一些不想带入的切入点进来，这可是很考验写拦截表达式的人，我们可以采用点对点的更精确的基于注解的方法。

##### (2).Spring + AspectJ（基于注解：通过 AspectJ @annotation 表达式拦截方法）
为了拦截指定的注解的方法，我们首先需要来自定义一个注解：
```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Tag {
}
```
以上定义了一个 @Tag 注解，此注解可标注在方法上，在运行时生效。

只需将前面的 Aspect 类的切点表达式稍作改动：
```java
@Aspect
@Component
public class GreetingAspect {
 
    @Around("@annotation(aop.demo.Tag)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        ...
    }
    ...
}
```
这次使用了 @annotation() 表达式，只需在括号内定义需要拦截的注解名称即可。

直接将 @Tag 注解定义在您想要拦截的方法上，就这么简单：
```java
@Component
public class GreetingImpl implements Greeting {
 
    @Tag
    @Override
    public void sayHello(String name) {
        System.out.println("Hello! " + name);
    }
}
```
#### 2.项目AOP实现
项目中可能就会同时采用Spring AspectJ的拦截表达式和注解拦截方法，假设要做一个操作日志的功能，需要对某些关键操作进行记录，将要记录操作日志的方法保存为枚举类。比如常见的增删改操作都要记录其操作：
```java
public enum Type {
    DEFAULT("",""),
    ADD("1","新增"),
    UPDATE("2","修改"),
    DELETE("3","删除")
    ;
    private String id;
    private String operationType;
     Type(String id,String operationType){
        this.id=id;
        this.operationType=operationType;
    }

    public String getId() {return id;}

    public void setId(String id) {this.id = id;}

    public String getOperationType() {return operationType;}

    public void setOperationType(String operationType) {
         this.operationType = operationType;}
}
```
写一个注解类，该注解联合枚举就可以作为一个标记，每一个需要记录加一个对应注解，相应的操作就会留下操作类型信息，方便切面拦截下来调用处理。
```java
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE,ElementType.METHOD})
public @interface LogAnno {
    //操作类型
    public Type operationType() default Type.DEFAULT;
    //操作备注
    public String remark() default "";
}
```
切面类的编写，在这里我们采用了Spring AspectJ +两种拦截表达式，拦截特定类（`execution(* main.com.*.*(..)`）下的加了特定注解（`@annotation(main.com.LogAnno)`）的操作，可以进行后置加强方法操作和后置返回方法操作，一个是切下了切入点的输入信息，一个是切下了切入点返回值的信息，具体按照实际情景来各取所需吧。在拦下了输入参数信息的同时，我们可以采用反射来获取输入参数的属性和值，操作日志就相当于留下了现场的快照，我们只需要在写一个操作日志的写入数据库方法就可以保存下这些信息了。
```java
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
            Map<String,Object> resultMap=new java.util.HashMap<>();
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
```
