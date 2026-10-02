package org.roniyaakobi;

import org.roniyaakobi.objects.Blob;
import org.roniyaakobi.objects.RegitObjects;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hasher {
    public static RegitObjects.ObjectId hash(Blob blob) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");

            byte[] headerBytes = blob.buildHeaderBytes();

            digest.update(headerBytes);

            blob.streamFileContents(streamData -> digest.update(streamData.bytes(), 0 ,streamData.length()));

            return new RegitObjects.ObjectId(digest.digest());

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-1 is not available", e);
        }
    }
}
