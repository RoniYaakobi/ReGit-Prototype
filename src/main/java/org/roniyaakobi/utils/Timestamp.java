package org.roniyaakobi.utils;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;

public record Timestamp (long timestamp, String timezone){

    /**
     * Serialize the timestamp object to a string
     * @return the serialized timestamp object
     */
    public String toSerializable(){
        return timestamp + ":" + timezone;
    }

    /**
     * Get the current time as a TimeStamp object
     * @return the current time as a TimeStamp object
     */
    public static Timestamp now(){
        return new Timestamp(
                LocalTime.now().toNanoOfDay(),
                ZoneId.systemDefault().getRules().getOffset(Instant.now()).toString()
        );
    }

    /**
     * Deserialized a string into a TimeStamp object
     * @param serialized the serialized TimeStamp
     * @return the deserialized TimeStamp object
     */
    public static Timestamp deserialize(String serialized){
        String[] time = serialized.split(":");

        return new Timestamp(Long.parseLong(time[0]), time[1]);

    }
}
