package com.cnhtc.computation.station.flow;
import com.cnhtc.computation.station.flow.entity.Vehicle;
import com.cnhtc.computation.station.flow.function.StationDistanceComputeFunction;
import com.cnhtc.computation.station.flow.schema.VehicleSchema;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import java.util.Properties;

/**
 * @author shansr
 */
public class StationFlowComputeApplication {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        ParameterTool tool = ParameterTool.fromArgs(args);
        String topic = tool.get("topic");
        String brokers = tool.get("brokers");
        String groupId = tool.get("groupId");
        String stationDbUrl = tool.get("stationDbUrl");
        String stationDbUser = tool.get("stationDbUser");
        String stationDbPwd = tool.get("stationDbPwd");
        String flowDbUrl = tool.get("flowDbUrl");
        String flowDbUser = tool.get("flowDbUser");
        String flowDbPwd = tool.get("flowDbPwd");
        Properties properties = new Properties();
        properties.put("bootstrap.servers", brokers);
        properties.put("group.id", groupId);

        env.addSource(new FlinkKafkaConsumer<>(topic, new VehicleSchema(), properties))
                .keyBy((KeySelector<Vehicle, String>) Vehicle::getVin)
                .process(new StationDistanceComputeFunction(stationDbUrl, stationDbUser, stationDbPwd))
                .addSink(JdbcSink.sink(
                        "",
                        (p,s)->{
                        },
                        JdbcExecutionOptions
                                .builder()
                                .withBatchIntervalMs(1000)
                                .withBatchSize(5)
                                .build(),
                        new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                                .withUrl(flowDbUrl)
                                .withUsername(flowDbUser)
                                .withPassword(flowDbPwd)
                                .build()
                ));
        env.execute("station flow compute application");
    }
}
