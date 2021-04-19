## TCC-Transaction使用

1. 需要提供分布式事务支持的接口上添加@Compensable

2. 在对应的接口实现上添加@Compensable

3. 在接口实现上添加confirmMethod、cancelMethod、transactionContextEditor

4. 实现对应的confirmMethod、cancelMethod
	注意： confirm方法和cancel方法必须与try方法在同一个类中

5. 主事务的业务都已经实现的差不多的时候才调用子事务

> 注意：
> * 分布式事务里，不要轻易在业务层捕获所有异常	
> * 使用TCC-Transaction时，confirm和cancel的幂等性需要自己代码支持

幂等性：使用相同参数对同一资源重复调用某个接口的结果与调用一次的结果相同



## TCC-Transaction框架原理

![BLB3gs.png](https://s1.ax1x.com/2020/11/10/BLB3gs.png)

需要分布式事务的地方会被事务拦截器拦截到，事务拦截器经过一系列的处理后交给事务管理器，事务管理器将事务相关信息存在事务存储器中，并且后续事务状态的改变也会更新到事务存储器。最后会有一个事务处理的job，该job会针对事务存储器中的记录的事务做后续成功或失败的操作。

### 事务拦截器作用

![BLDez9.png](https://s1.ax1x.com/2020/11/10/BLDez9.png)

1. CompensableTransactionInterceptor

   ```
   1、将事务区分为Root事务和分支事务
   2、不断的修改数据库内的状态【初始化事务，修改事务状态】
   3、注册和清除事务管理器中队列内容
   ```

2. ResourceCoordinatorInterceptor

   ```
   1、主要处理try阶段的事情
   2、在try阶段，就将所有的"资源"（事务资源）封装完成并交给事务管理器
      包括事务的参与者：
       - Confirm上下文
       - Cancel上下文
       - 分支事务信息
   3、事务管理器修改数据库状态	
   ```

3. 调用目标对象 -> order red cap

## 小结

1. 事务的相关信息【全局事务编号，乐观锁版本等要持久化存储】
   
2. 资源

    TCC 【try-confirm-cancel】

    try核心点： 预留业务资源，把事务数据资源存入库中

3. 流程：

    注册和初始化事务 -> 组织事务参与者 -> 执行目标try方法 -> 执行confirm和cancel方法