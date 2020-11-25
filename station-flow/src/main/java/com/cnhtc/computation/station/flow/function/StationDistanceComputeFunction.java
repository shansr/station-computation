package com.cnhtc.computation.station.flow.function;

import com.cnhtc.computation.station.flow.entity.Station;
import com.cnhtc.computation.station.flow.entity.Vehicle;
import com.cnhtc.computation.station.flow.entity.VehicleInOut;
import com.cnhtc.computation.station.flow.util.EarthMath;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * @author shansr
 */
public class StationDistanceComputeFunction extends KeyedProcessFunction<String, Vehicle, VehicleInOut> {
    private MapState<String, VehicleInOut> mapState;
    private Connection connection;
    private Cache<String, List<Station>> stationCache;
    private final String stationDbUrl;
    private final String stationDbUser;
    private final String stationDbPwd;

    public StationDistanceComputeFunction(String stationDbUrl, String stationDbUser, String stationDbPwd){
        this.stationDbUrl = stationDbUrl;
        this.stationDbUser = stationDbUser;
        this.stationDbPwd = stationDbPwd;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        mapState = getRuntimeContext().getMapState(new MapStateDescriptor<>("stationFlowState", String.class, VehicleInOut.class));
        Class.forName("com.mysql.cj.jdbc.Driver");
        connection = DriverManager.getConnection(this.stationDbUrl, this.stationDbUser, this.stationDbPwd);
        stationCache = Caffeine.
                newBuilder()
                .expireAfterAccess(Duration.ofMinutes(2))
                .build();
    }

    @Override
    public void processElement(Vehicle value, Context ctx, Collector<VehicleInOut> out) throws Exception {

        List<Station> stations = stationCache.get(value.getVin(), s -> {
            try {
                List<Station> stationsInDb = new ArrayList<>();
                PreparedStatement preparedStatement = connection.prepareStatement("select * from service_station");
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    Station station = new Station();
                    station.setId(resultSet.getLong("id"));
                    station.setName(resultSet.getString("name"));
                    station.setLatitude(resultSet.getFloat("latitude"));
                    station.setLongitude(resultSet.getFloat("longitude"));
                    station.setRadius(resultSet.getFloat("service_radius"));
                    stationsInDb.add(station);
                }
                preparedStatement.close();
                return stationsInDb;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
        if(null != stations && stations.size() != 0){
            for (Station station : stations) {
                String key = value.getVin() + ":" + station.getId();
                double distance = EarthMath.distance(value.getLongitude(), value.getLatitude(), station.getLongitude(), station.getLatitude());
                VehicleInOut vehicleInOutInMap = mapState.get(key);
                if(null == vehicleInOutInMap) {
                    mapState.put(key, new VehicleInOut(value.getVin(), station.getId()));
                    vehicleInOutInMap = mapState.get(key);
                }
                vehicleInOutInMap.setMinDistance(Math.min(distance, vehicleInOutInMap.getMinDistance()));
                if(distance < station.getRadius() * 1000){
                    if(vehicleInOutInMap.getStart() < 0){
                        vehicleInOutInMap.setStart(value.getTimestamp());
                        vehicleInOutInMap.setMinDistance(distance);
                    }
                } else {
                    if((vehicleInOutInMap.getEnd()) < 0 && (vehicleInOutInMap.getStart() > 0)){
                        vehicleInOutInMap.setEnd(value.getTimestamp());
                        out.collect(vehicleInOutInMap);
                    }
                }
            }
        }
    }

    @Override
    public void close() throws Exception {
        super.close();
        stationCache.cleanUp();
        connection.close();
        mapState.clear();
    }
}
