package cn.cnhtc.component.connector;

import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.TableEnvironment;

/**
 * @author shansr
 */
public class KafkaToHbaseApplication {
    public static void main(String[] args) {
        EnvironmentSettings settings = EnvironmentSettings.newInstance().build();
        TableEnvironment tabEnv = TableEnvironment.create(settings);
        String kafkaSchemaSink = "CREATE TABLE KafkaTable2 (\n" +
                "  `c0` STRING,\n" +
                "  `c1` STRING,\n" +
                "  `c2` STRING,\n" +
                "  `c3` STRING,\n" +
                "  `c4` STRING,\n" +
                "  `c5` STRING,\n" +
                "  `c6` STRING,\n" +
                "  `c7` STRING,\n" +
                "  `c8` STRING,\n" +
                "  `c9` STRING,\n" +
                "  `c10` STRING,\n" +
                "  `c11` STRING,\n" +
                "  `c12` STRING,\n" +
                "  `c13` STRING,\n" +
                "  `c14` STRING,\n" +
                "  `c15` STRING,\n" +
                "  `c16` STRING,\n" +
                "  `c17` STRING,\n" +
                "  `c18` STRING,\n" +
                "  `c19` STRING,\n" +
                "  `c20` STRING,\n" +
                "  `c21` STRING\n" +
                ") WITH (\n" +
                "  'connector' = 'kafka',\n" +
                "  'topic' = 'tp-sinotruck-data-em-6-2',\n" +
                "  'properties.bootstrap.servers' = 'centos-node3:9092,centos-node4:9092,centos-node5:9092',\n" +
                "  'properties.group.id' = 'testGroup',\n" +
                "  'scan.startup.mode' = 'earliest-offset',\n" +
                "  'format' = 'json'\n" +
                ")";
        String hbaseSinkSchema = "CREATE TABLE hTable (\n" +
                " rowkey STRING,\n" +
                " f1 ROW<c0 STRING, c1 STRING, c2 STRING, c3 STRING,c4 STRING,c5 STRING,c6 STRING,c7 STRING,c8 STRING,c9 STRING,c10 STRING,c11 STRING,c12 STRING,c13 STRING,c14 STRING,c15 STRING,c16 STRING,c17 STRING,c18 STRING,c19 STRING,c20 STRING,c21 STRING>,\n" +
                " PRIMARY KEY (rowkey) NOT ENFORCED\n" +
                ") WITH (\n" +
                " 'connector' = 'hbase-2.2',\n" +
                " 'table-name' = 'ns_test:test2',\n" +
                " 'zookeeper.quorum' = 'centos-node3:2181'\n" +
                ")";

        String kafkaSchema = "CREATE TABLE KafkaTable (\n" +
                "  `c0` STRING,\n" +
                "  `c1` STRING,\n" +
                "  `c2` STRING,\n" +
                "  `c3` STRING,\n" +
                "  `c4` STRING,\n" +
                "  `c5` STRING,\n" +
                "  `c6` STRING,\n" +
                "  `c7` STRING,\n" +
                "  `c8` STRING,\n" +
                "  `c9` STRING,\n" +
                "  `c10` STRING,\n" +
                "  `c11` STRING,\n" +
                "  `c12` STRING,\n" +
                "  `c13` STRING,\n" +
                "  `c14` STRING,\n" +
                "  `c15` STRING,\n" +
                "  `c16` STRING,\n" +
                "  `c17` STRING,\n" +
                "  `c18` STRING,\n" +
                "  `c19` STRING,\n" +
                "  `c20` STRING,\n" +
                "  `c21` STRING\n" +
                ") WITH (\n" +
                "  'connector' = 'kafka',\n" +
                "  'topic' = 'tp-sinotruck-data-em-6',\n" +
                "  'properties.bootstrap.servers' = 'centos-node3:9092,centos-node4:9092,centos-node5:9092',\n" +
                "  'properties.group.id' = 'testGroup',\n" +
                "  'scan.startup.mode' = 'earliest-offset',\n" +
                "  'format' = 'json'\n" +
                ")";
        tabEnv.executeSql(kafkaSchemaSink);
        tabEnv.executeSql(kafkaSchema);

        tabEnv.executeSql(hbaseSinkSchema);
        // kafka to kafka
        //tabEnv.executeSql("insert into KafkaTable2 select * from KafkaTable");

        //Table kafkaTable = tabEnv.from("KafkaTable");

        // kafka to hbase
        //tabEnv.executeSql("insert into hTable select c1,ROW(c0,c1,c2,c3,c4,c5,c6,c7,c8,c9,c10,c11,c12,c13,c14,c15,c16,c17,c18,c19,c20,c21) as f1 from KafkaTable ");
        // mapped kafka to hbase
        tabEnv.executeSql("insert into hTable select SUBSTRING(MD5(c1),0,6) || '-' || c1 || '-' || CAST(CAST(TO_TIMESTAMP(c0) AS BIGINT) AS STRING),ROW(c0,c1,c2,c3,c4,c5,c6,c7,c8,c9,c10,c11,c12,c13,c14,c15,c16,c17,c18,c19,c20,c21) as f1 from KafkaTable");
    }
}
