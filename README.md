# log-alarm-springboot-starter
这个组件主要是解决开发者错误日志及时发现和追踪问题，是基于springboot封装错误日志告警功能，以及全局日志traceId跟踪，同时支持微服务和单体架构。


## 特性
- 支持钉钉,企业微信不同方式告警
- 支持traceId全局日志跟踪,包括web请求,Async任务,消息队列
- 支持错误异常类或关键字忽略告警
- 支持不同接口超时时间告警
## 使用

1. 增加依赖
- springboot2.x版本
   ```bash
   <dependency>
      <groupId>io.github.simonlu9</groupId>
      <artifactId>log-alarm-spring-boot-starter</artifactId>
      <version>1.1.1-SB2</version>
   </dependency>
   ```
- springboot3.x版本
 ```bash
    <dependency>
      <groupId>io.github.simonlu9</groupId>
      <artifactId>log-alarm-spring-boot-starter</artifactId>
      <version>1.1.1-SB3</version>
   </dependency>
  ```
## 使用


```bash
# 添加配置
log-alarm:
# 日志内容里的应用名字段，可自定义；不配置时默认读取 spring.application.name
  name: test
  #当开启时候,会自动在MDC注入traceID属性
  enableTraceId: true
  #mode可选wordWechat|dingding
  timeout:
    settings:
      - urlPattern: "^/.*$" # 匹配以 /test 开头的所有路径
        threshold: 1000 # 超时时间（毫秒）
  mode: workWechat
  #机器人人配置
  webhook: xxx
  exclude:
    #忽略异常类,多个逗号隔开
    throwable: java.lang.RuntimeException
    #忽略关键字配置
    keyword:

# logback配置
<configuration>
    <springProperty scope="context" name="logFile" source="logging.file.name" />
    #引用LogAlarmAppender才能告警
    <appender name="LOG_ALARM" class="com.ljw.logalarm.core.appender.LogAlarmAppender">
        #指定过滤器进行exclude相关过滤
        <filter class="com.ljw.logalarm.core.filter.AlarmFilter"></filter>
    </appender>

    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <!-- 输出格式 -->
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level [%X{traceId}] [%thread] %logger{50} - %msg%n</pattern>
        </encoder>
    </appender>
    <root level="INFO">
        <appender-ref ref="LOG_ALARM"/>
        <appender-ref ref="STDOUT"/>

    </root>
</configuration>
```
## 效果
```
链路追踪: fb2aa22424
应用名: insight
线程名称: http-nio-9057-exec-7
用户编号: 
请求信息: GET /sys/sync/orders
请求参数: {"hours":"1"}
请求body: null
异常来源: com.ljw.logalarm.core.filter.ExecutionTimeFilter
日志内容: Request to [/sys/sync/orders] exceeded timeout. Duration: 67457 ms, Threshold: 60000 ms
异常时间: 2025-01-23 14:01:08
异常描述: 
详细信息:

```

## `springboot @Async` 自定义线程池
```
    @Bean
    @Primary
    public ThreadPoolTaskExecutor customTaskExecutor() {
        TraceIdThreadPoolTaskExecutor executor =  new TraceIdThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("trace-id-task-executor-");
        executor.setCorePoolSize(4);
        return executor;
    }
```
## `springboot @Schedule` 自定义线程池
```
    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new TraceIdThreadPoolScheduleTaskExecutor();
        scheduler.setPoolSize(5); // 配置线程池大小
        scheduler.setThreadNamePrefix("Scheduler-");
        return scheduler;
    }
```
## 贡献

欢迎任何形式的贡献！请遵循以下步骤：

1. Fork 这个仓库
2. 创建你的特性分支 (`git checkout -b feature/你的特性`)
3. 提交你的更改 (`git commit -m '添加了一些特性'`)
4. 推送到分支 (`git push origin feature/你的特性`)
5. 创建一个 Pull Request

