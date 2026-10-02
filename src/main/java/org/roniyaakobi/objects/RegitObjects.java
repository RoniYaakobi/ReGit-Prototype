package org.roniyaakobi.objects;

import org.roniyaakobi.Constants;

import java.nio.file.Path;
import java.util.HexFormat;

public class RegitObjects {
    public enum ObjectType {
        BLOB(0),
        TREE(1);

        private int priority;

        ObjectType(int priority){
            this.priority = priority;
        }

        public int getPriority(){
            return priority;
        }
    }

    public static class ObjectId{
        private byte[] objectHash;

        public ObjectId(byte[] objectHash){
            this.objectHash = objectHash;
        }

        public Path getPath(){
            String hex = HexFormat.of().formatHex(objectHash);


            String prefix = hex.substring(0,2);
            Path blobFolder = Constants.objectStorePath.resolve(prefix);

            String suffix = hex.substring(2);
            return blobFolder.resolve(suffix + ".blob");
        }

        public byte[] getBytes(){
            return objectHash;
        }
    }
}
