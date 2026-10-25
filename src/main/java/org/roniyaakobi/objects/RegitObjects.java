package org.roniyaakobi.objects;

import org.roniyaakobi.Constants;
import org.roniyaakobi.utils.BytesFormatter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HexFormat;
import java.util.function.Function;

public class RegitObjects {
    public enum ObjectType {
        BLOB(0, null),
        TREE(1, bytes -> {
            try {
                return Tree.deserialize(bytes);
            } catch (IOException e) {
                throw new RuntimeException("Could not deserialize bytes");}
            }
        ),
        COMMIT(2, bytes -> {
            try {
                return Commit.deserialize(bytes);
            } catch (IOException e) {
                throw new RuntimeException("Could not deserialize bytes");}
            }
        );

        private int priority;
        private Function<byte[] ,? extends RegitObject> deserializer;

        ObjectType(int priority, Function<byte[] ,? extends RegitObject> deserializer){
            this.priority = priority;
            this.deserializer = deserializer;
        }

        /**
         * Get the priority of the ObjectType
         * @return the priority
         */
        public int getPriority(){
            return priority;
        }

        /**
         * Deserialize based on an objectType
         * @param data the byte array
         * @return the deserialized object
         */
        public RegitObject deserialize(byte[] data) {
            return deserializer.apply(data);
        }
    }

    public static class ObjectId{
        private final byte[] objectHash;
        private final ObjectType objectType;

        public ObjectId(byte[] objectHash, ObjectType objectType){
            this.objectHash = objectHash;
            this.objectType = objectType;
        }

        /**
         * Create an objectId from a hash path
         * @param path the path
         * @param objectType the type of the object
         * @return The id of the object
         */
        public static ObjectId fromPath(Path path, ObjectType objectType){
            String hex = path.getName(0).toString() + path.getName(1).toString();

            byte[] objectHash = BytesFormatter.hexToBytes(hex);

            return new ObjectId(objectHash, objectType);
        }

        /**
         * Get the path of the object file
         * @return the path of the object file
         */
        public Path getPath(){
            String hex = HexFormat.of().formatHex(objectHash);

            String prefix = hex.substring(0,2);
            Path folder = Constants.objectStorePath.resolve(prefix);

            String suffix = hex.substring(2);
            return folder.resolve(suffix + "." + objectType.name().toLowerCase());
        }

        /**
         * Get the bytes of the objectHash
         * @return the objectHash
         */
        public byte[] getBytes(){
            return objectHash;
        }
    }
}
