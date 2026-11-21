package org.roniyaakobi.objects;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public interface Compressible {
    /**
     * Compress only the body of the object (no header)
     * @return The compressed body in bytes
     */
    byte[] compressBody();

    /**
     * Get the type of the object
     * @return the object type
     */
    RegitObjects.ObjectType getType();

    /**
     * Build the full stored record: "&lt;type&gt; &lt;compressedLength&gt;\0" followed by the compressed body
     * @return The record bytes
     */
    default byte[] compress(){
        return buildRecord(getType(), compressBody());
    }

    /**
     * Prefix a compressed body with its "&lt;type&gt; &lt;compressedLength&gt;\0" header
     * @param type the object type
     * @param body the compressed body
     * @return The record bytes
     */
    static byte[] buildRecord(RegitObjects.ObjectType type, byte[] body){
        byte[] header = (type.name().toLowerCase() + " " + body.length + "\0").getBytes();

        byte[] record = new byte[header.length + body.length];
        System.arraycopy(header, 0, record, 0, header.length);
        System.arraycopy(body, 0, record, header.length, body.length);

        return record;
    }

    /**
     * Parse an inflated body into an object of the given type
     * @param type the type of the object
     * @param body the inflated body
     * @return the deserialized object
     */
    static RegitObject parse(RegitObjects.ObjectType type, byte[] body){
        if (type == RegitObjects.ObjectType.BLOB){
            throw new IllegalArgumentException("Cannot deserialize blob object");
        }

        return type.deserialize(body);
    }

    /**
     * Inflate compressed bytes
     * @param compressed the compressed bytes
     * @return the decompressed bytes
     */
    static byte[] inflate(byte[] compressed){
        Inflater inflater = new Inflater();
        inflater.setInput(compressed);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(compressed.length);
        byte[] buffer = new byte[1024];

        try {
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                if (count == 0 && (inflater.needsInput() || inflater.needsDictionary())) {
                    throw new IllegalStateException("Compressed data is truncated");
                }
                outputStream.write(buffer, 0, count);
            }
        } catch (DataFormatException e) {
            throw new IllegalStateException("Corrupted compressed data", e);
        } finally {
            inflater.end();
        }

        return outputStream.toByteArray();
    }

    /**
     * Load the inflated body of an archived object
     * @param id the object id
     * @return the inflated body
     * @throws IOException
     */
    static byte[] getBytes(RegitObjects.ObjectId id) throws IOException {
        return Archivable.loadBytes(id);
    }
}
