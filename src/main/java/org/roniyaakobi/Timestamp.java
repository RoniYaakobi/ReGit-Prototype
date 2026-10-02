package org.roniyaakobi;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;

public record Timestamp (long timestamp, String timezone){
    public String toSerializable(){
        return timestamp + " " + timezone;
    }

    public static Timestamp now(){
        return new Timestamp(
                LocalTime.now().toNanoOfDay(),
                ZoneId.systemDefault().getRules().getOffset(Instant.now()).toString()
        );
    }
}
