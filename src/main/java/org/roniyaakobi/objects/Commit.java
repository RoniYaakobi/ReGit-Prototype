package org.roniyaakobi.objects;

import org.roniyaakobi.Timestamp;
import org.roniyaakobi.User;
import org.roniyaakobi.utils.BytesFormatter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.zip.DeflaterOutputStream;

public class Commit extends RegitObject {

    private final RegitObjects.ObjectId treeId;
    private final Optional<RegitObjects.ObjectId> parentId;
    private final User author;
    private final Timestamp timestamp;
    private final String message;
    public Commit(
        RegitObjects.ObjectId treeId,
        Optional<RegitObjects.ObjectId> parentId,
        User author,
        Timestamp timestamp,
        String message
    ) {
        this.treeId = treeId;
        this.parentId = parentId;
        this.author = author;
        this.timestamp = timestamp;
        this.message = message;
    }

    public byte[] buildHeaderBytes(){
        String header = "tree " + serialize().length() + "\0";
        return header.getBytes();
    }

    private String serialize() {
        return  "tree " + BytesFormatter.bytesToString(treeId.getBytes()) +
                "parent " +
                (parentId.isPresent() ?
                    BytesFormatter.bytesToString(parentId.get().getBytes()) :
                    "" ) +
                "author " + author + " " + timestamp +
                "committer " + author + " " + timestamp +
                message;

    }

    @Override
    public byte[] compress() {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream();
             DeflaterOutputStream deflater =
                     new DeflaterOutputStream(output)) {


            deflater.write(buildHeaderBytes());

            deflater.write(serialize().getBytes(), 0 , serialize().getBytes().length);

            deflater.finish();

            return output.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public RegitObjects.ObjectId hash() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");

            byte[] headerBytes = buildHeaderBytes();

            digest.update(headerBytes);

            digest.update(serialize().getBytes());

            return new RegitObjects.ObjectId(digest.digest(), RegitObjects.ObjectType.COMMIT);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-1 is not available", e);
        }
    }
}