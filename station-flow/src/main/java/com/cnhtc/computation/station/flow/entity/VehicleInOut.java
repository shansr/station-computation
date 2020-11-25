package com.cnhtc.computation.station.flow.entity;

/**
 * @author shansr
 */
public class VehicleInOut {
    private long start = -1;
    private long end = -1;
    private double minDistance = 0;
    private String vin;
    private Long stationId;


    public VehicleInOut(String vin, Long stationId){
        this.vin = vin;
        this.stationId = stationId;
    }


    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public double getMinDistance() {
        return minDistance;
    }

    public void setMinDistance(double minDistance) {
        this.minDistance = minDistance;
    }

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public Long getStationId() {
        return stationId;
    }

    public void setStationId(Long stationId) {
        this.stationId = stationId;
    }

    @Override
    public String toString() {
        return "VehicleInOut{" +
                "start=" + start +
                ", end=" + end +
                ", minDistance=" + minDistance +
                ", vin='" + vin + '\'' +
                ", stationId='" + stationId + '\'' +
                '}';
    }
}
