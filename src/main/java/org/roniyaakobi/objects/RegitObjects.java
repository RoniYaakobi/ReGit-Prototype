package org.roniyaakobi.objects;

import org.roniyaakobi.Constants;

import java.nio.file.Path;
import java.util.HexFormat;

public class RegitObjects {
    public enum ObjectType {
        BLOB(0),
        TREE(1),
        COMMIT(2);

        private int priority;

        ObjectType(int priority){
            this.priority = priority;
        }

        public int getPriority(){
            return priority;
        }
    }

    public static class ObjectId{
        private final byte[] objectHash;
        private final ObjectType objectType;

        public ObjectId(byte[] objectHash, ObjectType objectType){
            this.objectHash = objectHash;
            this.objectType = objectType;
        }

        public Path getPath(){
            String hex = HexFormat.of().formatHex(objectHash);

            String prefix = hex.substring(0,2);
            Path folder = Constants.objectStorePath.resolve(prefix);

            String suffix = hex.substring(2);
            return folder.resolve(suffix + "." + objectType.name().toLowerCase());
        }

        public byte[] getBytes(){
            return objectHash;
        }
    }
}
