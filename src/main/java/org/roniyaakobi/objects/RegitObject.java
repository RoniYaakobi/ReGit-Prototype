package org.roniyaakobi.objects;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public abstract class RegitObject implements Hashable, Archivable {

    private RegitObjects.ObjectId objectId = null;

    private byte[] compressedBody = null;

    /**
     * Get the id of the object
     * @return The objectID
     */
    @Override
    public RegitObjects.ObjectId getObjectId(){
        if (objectId == null){
            objectId = hash();
        }

        return objectId;
    }

    /**
     * Get the compressed body, compressing it only once
     * @return the compressed body
     */
    protected byte[] getCompressedBody(){
        if (compressedBody == null){
            compressedBody = compressBody();
        }

        return compressedBody;
    }

    /**
     * Build the stored record using the cached compressed body
     * @return The record bytes
     */
    @Override
    public byte[] compress(){
        return Compressible.buildRecord(getType(), getCompressedBody());
    }

    /**
     * Hash the compressed body (not the type or length header)
     * @return The objectID
     */
    @Override
    public RegitObjects.ObjectId hash(){
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");

            return new RegitObjects.ObjectId(digest.digest(getCompressedBody()), getType());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-1 is not available", e);
        }
    }

    /**
     * Store the object
     * @throws IOException
     */
    public void store() throws IOException {
        archive();
    }

}
