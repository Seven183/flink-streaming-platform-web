import com.flink.streaming.core.execute.ExecuteSql;
import org.apache.flink.api.common.JobID;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhuhuipei
 * @Description:
 * @date 2020-07-20
 * @time 21:36
 */
public class Test2 {

  private static List<String> sqlList = new ArrayList<>();

  static {
    sqlList.add("SET 'table.local-time-zone' = 'Asia/Shanghai' ");
    sqlList.add("CREATE TABLE spend_report (\n" +
            "  `id` INT primary key," +
            "  `login_time` Timestamp ," +
            "  `create_time` Timestamp  " +
            ") WITH (\n" +
            "    'connector' = 'mysql-cdc', " +
            "    'scan.startup.mode' = 'initial', " +
            "    'hostname' = '42.192.48.125', " +
            "    'port' = '3036', " +
            "    'username' = 'root', " +
            "    'password' = '123456', " +
            "    'database-name' = 'car', " +
            "    'table-name' = 'users_login_logs' " +
            ")"
    );
    sqlList.add(
            "CREATE TABLE output_kafka (" +
                    "  `id` BIGINT," +
                    "  `login_time` timestamp ," +
                    "  `create_time` timestamp, " +
                    "  PRIMARY KEY (`id`) NOT ENFORCED " +
                    ") WITH (" +
                    "  'connector' = 'upsert-kafka'," +
                    "  'topic' = 'output_kafka'," +
                    "  'properties.bootstrap.servers' = '42.192.48.125:9092'," +
                    "  'key.format' = 'json'," +
                    "  'value.format' = 'json'" +
                    ")"
    );
    sqlList.add("insert into output_kafka select * from spend_report");
  }

  public static void main(String[] args) throws IOException {

    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    TableEnvironment tEnv = StreamTableEnvironment.create(env, EnvironmentSettings.newInstance().inStreamingMode().build());

    JobID jobID = ExecuteSql.exeSql(sqlList, tEnv);
    System.out.println(jobID);

  }
}
