# zlikun-open-aliyun-loghub
阿里云日志服务SDK使用指南

#### 引入依赖
```
<!-- 官方SDK依赖 -->
<dependency>
    <groupId>com.aliyun.openservices</groupId>
    <artifactId>aliyun-log</artifactId>
    <version>0.6.6</version>
</dependency>

<!-- aliyun-log 内部依赖了 json-lib，而 json-lib 又需要使用到 ezmorph 组件 -->
<dependency>
    <groupId>net.sf.ezmorph</groupId>
    <artifactId>ezmorph</artifactId>
    <version>1.0.6</version>
</dependency>
```

#### 代码示例
```
// 构造一个日志客户端
Client client = new Client(endpoint, accessKeyId, accessKeySecret);

List<LogItem> logItems = new ArrayList<LogItem>();
// LogItem 参数是日志的生成时间(时间戳格式，精确到秒)
LogItem logItem = new LogItem((int) (System.currentTimeMillis() / 1000));
logItem.PushBack("level", "INFO");
logItem.PushBack("method", "get");
logItem.PushBack("message", "这是一条测试日志!");
logItem.PushBack("userId" ,"11211");
logItems.add(logItem);
// 另一种日志条目构建方法
logItems.add(new LogItem((int) (System.currentTimeMillis() / 1000), new ArrayList(Arrays.asList(
        new LogContent("level" ,"DEBUG") ,
        new LogContent("method" ,"save") ,
        new LogContent("message" ,"这是一条测试日志-2!")
)))) ;

try {
    PutLogsResponse response = client.PutLogs(project, logStore, "Topic", logItems, "LocalTest");
    System.out.println("日志发送完成!");
    Map<String ,String> headers = response.GetAllHeaders() ;
    for (Map.Entry<String ,String> entry : headers.entrySet()) {
        System.out.println(String.format("%s: %s" ,entry.getKey() ,entry.getValue()));
    }
} catch (LogException e) {
    e.printStackTrace();
}
```

#### 参考文档
- <https://help.aliyun.com/document_detail/29063.html>
- <https://help.aliyun.com/document_detail/29068.html>
- <https://github.com/aliyun/aliyun-log-java-sdk>

