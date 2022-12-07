import com.flink.streaming.common.sql.SqlFileParser;
import com.flink.streaming.core.execute.ExecuteSql;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Demo {


  private static String test_sql_file = "C:\\Users\\Seven.si\\Desktop\\test\\1.sql";


  public static void main(String[] args) throws Exception {

    EnvironmentSettings settings = null;

    TableEnvironment tEnv = null;

    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

    settings = EnvironmentSettings.newInstance()
        .useBlinkPlanner()
        .inStreamingMode()
        .build();
    tEnv = StreamTableEnvironment.create(env, settings);

    List<String> fileList = Files.readAllLines(Paths.get(test_sql_file));

//    List<SqlCommand>  res=SqlFileParser.fileToSqlCommand(fileList,tEnv);
//    System.out.println(res);

    List<String> sqlList = SqlFileParser.parserSql(fileList);
    System.out.println(sqlList);

    ExecuteSql.exeSql(sqlList,tEnv);



  }
}
