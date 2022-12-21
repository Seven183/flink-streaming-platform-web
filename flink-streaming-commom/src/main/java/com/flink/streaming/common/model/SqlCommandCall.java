package com.flink.streaming.common.model;


import com.flink.streaming.common.enums.SqlCommand;
import lombok.Data;

@Data
public class SqlCommandCall {

    private SqlCommand sqlCommand;

    private String[] operands;

    public SqlCommandCall(SqlCommand sqlCommand, String[] operands) {
        this.sqlCommand = sqlCommand;
        this.operands = operands;
    }

    public SqlCommandCall(String[] operands) {
        this.operands = operands;
    }

    public SqlCommand getSqlCommand() {
        return sqlCommand;
    }

    public String[] getOperands() {
        return operands;
    }
}
