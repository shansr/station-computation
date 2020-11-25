package com.cnhtc.computation.station.flow.schema;

import com.cnhtc.computation.station.flow.entity.Vehicle;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * @author shansr
 */
public class VehicleSchema implements DeserializationSchema<Vehicle> {

    @Override
    public Vehicle deserialize(byte[] message) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(message, Vehicle.class);
    }

    @Override
    public boolean isEndOfStream(Vehicle nextElement) {
        return false;
    }

    @Override
    public TypeInformation<Vehicle> getProducedType() {
        return TypeInformation.of(Vehicle.class);
    }
}
