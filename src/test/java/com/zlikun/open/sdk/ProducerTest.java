package com.zlikun.open.sdk;

import com.aliyun.openservices.log.Client;
import com.aliyun.openservices.log.common.LogContent;
import com.aliyun.openservices.log.common.LogGroupData;
import com.aliyun.openservices.log.common.LogItem;
import com.aliyun.openservices.log.common.Logs;
import com.aliyun.openservices.log.exception.LogException;
import com.aliyun.openservices.log.request.ListLogStoresRequest;
import com.aliyun.openservices.log.response.BatchGetLogResponse;
import com.aliyun.openservices.log.response.GetCursorResponse;
import com.aliyun.openservices.log.response.ListLogStoresResponse;
import com.aliyun.openservices.log.response.PutLogsResponse;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 通过SDK输出日志到LogHub
 * @author zlikun <zlikun-dev@hotmail.com>
 * @date 2017/7/24 13:28
 */
public class ProducerTest {

    private final static String accessKeyId = "LTAI8CTKiKducjvf";
    private final static String accessKeySecret = "";
    private final static String endpoint = "cn-hangzhou.log.aliyuncs.com";

    private final static String project = "ceshhi";
    private final static String logStore = "test-sdk";

    // 构造一个日志客户端
    private Client client = new Client(endpoint, accessKeyId, accessKeySecret);

    /**
     * 列出当前 project 下的所有日志库名称
     */
    @Test
    public void ListLogStores() {

        // 构造一个请求实例
        ListLogStoresRequest req = new ListLogStoresRequest(project, 0, 10, null);

        try {
            // 执行请求，返回一个响应实例
            ListLogStoresResponse response = client.ListLogStores(req) ;

            // 获取LogStore列表
            ArrayList<String> logStores = response.GetLogStores();

            // ListLogs:[test-sdk, test-user]
            System.out.println("ListLogs:" + logStores.toString() + "\n");
        } catch (LogException e) {
            e.printStackTrace();
        }
    }

    /**
     * 输出日志测试
     */
    @Test
    public void PutLogs() {

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

    }

    /**
     * 批量读取日志
     */
    @Test
    public void BatchGetLog() throws LogException {

        int shardId = 0;  // 只读取0号shard的数据
        GetCursorResponse res = null;
        try {
            // 获取最近1个小时接收到的第一批日志的cursor位置
            long fromTime = (int)(System.currentTimeMillis()/1000.0 - 3600);
            // 获取游标(应该是为了优化查询速度)
            res = client.GetCursor(project, logStore, shardId, fromTime);
            // shard_id:0 Cursor:MTUwMDI3Mzg5OTMwOTAzNDIyNg==
            System.out.println("shard_id:" + shardId + " Cursor:" + res.GetCursor());
        } catch (LogException e) {
            e.printStackTrace();
            Assert.fail();
        }

        // 迭代查询日志信息
        String cursor = res.GetCursor();
        while(true) {
            BatchGetLogResponse logDataRes = client.BatchGetLog(project, logStore, shardId, 100, cursor);
            // 读取到的数据
            List<LogGroupData> logGroups = logDataRes.GetLogGroups();

            // 输出读到的日志
            if (logGroups != null && !logGroups.isEmpty()) {
                for (LogGroupData data : logGroups) {
                    // 获取日志组
                    Logs.LogGroup group = data.GetLogGroup() ;
                    if (group == null) continue;
                    // 查询日志组目录、日志条目
                    // category =  ,LogsCount = 2
                    System.out.println(String.format("category = %s ,LogsCount = %d" ,group.getCategory() ,group.getLogsCount()));
                    if (group.getLogsCount() > 0) {
                        for (Logs.Log log : group.getLogsList()) {
                            // 输出日志信息
                            System.out.println(log.toString());
                        }
                    }
                }
            }

            // 迭代，获取下一个游标
            String next_cursor = logDataRes.GetNextCursor();
            System.out.print("The Next cursor:" + next_cursor);
            if (cursor.equals(next_cursor)) {
                break;
            }
            cursor = next_cursor;
        }

    }

}
