package org.roniyaakobi.objects;

import org.roniyaakobi.Constants;
import org.roniyaakobi.utils.Timestamp;
import org.roniyaakobi.utils.User;
import org.roniyaakobi.utils.BytesFormatter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;
import java.util.zip.DeflaterOutputStream;

public final class Commit extends RegitObject {

    public record SerialziedCommitMetadata(
            String treeIdSerialized, String parentIdSerialized,
            String authorSerialized, String timeStampSerialized,
            String message) {
        public static SerialziedCommitMetadata buildMetadata(byte[] serialized){

            String serializedString = BytesFormatter.bytesToString(serialized);

            String[] fields = serializedString.split(" ");


            String treeIdSerialized = fields[1];

            String parentIdSerialized = fields[3];
            String authorSerialized = fields[5];
            String timeStampSerialized = fields[6];


            String[] message = new String[fields.length - 7];

            for(int i = 7; i < fields.length; i++){
                message[i - 7] = fields[i];
            }

            String reconstructedMessage = String.join(" ", message);

            return new SerialziedCommitMetadata(
                    treeIdSerialized, parentIdSerialized,
                    authorSerialized, timeStampSerialized,
                    reconstructedMessage
            );
        }
    }
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
    ) throws IOException {
        this.treeId = treeId;
        this.parentId = parentId;
        this.author = author;
        this.timestamp = timestamp;
        this.message = message;

        store();

    }

    public byte[] buildHeaderBytes(){
        String header = "commit " + serialize().getBytes().length + "\0";
        return header.getBytes();
    }

    private String serialize() {
        return "tree " +  HexFormat.of().formatHex(treeId.getBytes()) +
                " parent " +
                (parentId.isPresent() ?
                        HexFormat.of().formatHex(parentId.get().getBytes()) :
                        Constants.NONE) +
                " author " + author + " " + timestamp.toSerializable() + " " +
                message;
    }

    public static Commit deserialize(byte[] serialized) throws IOException {
        SerialziedCommitMetadata metadata = SerialziedCommitMetadata.buildMetadata(serialized);

        RegitObjects.ObjectId treeId = new RegitObjects.ObjectId(
                BytesFormatter.hexToBytes(metadata.treeIdSerialized()),
                RegitObjects.ObjectType.TREE
        );

        Optional<RegitObjects.ObjectId> commitId =
                metadata.parentIdSerialized() == Constants.NONE ?

                Optional.of(
                        new RegitObjects.ObjectId(
                    BytesFormatter.hexToBytes(metadata.parentIdSerialized()),
                    RegitObjects.ObjectType.COMMIT)
                ) : Optional.empty();

        User author = User.deserialize(metadata.authorSerialized());

        Timestamp commitTime = Timestamp.deserialize(metadata.timeStampSerialized());

        return new Commit(treeId, commitId, author, commitTime, metadata.message());

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

    public Optional<RegitObjects.ObjectId> getParentId(){
        return parentId;
    }

    public RegitObjects.ObjectId getTreeId(){
        return treeId;
    }
}