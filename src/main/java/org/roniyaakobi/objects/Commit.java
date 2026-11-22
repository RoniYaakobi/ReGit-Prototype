package org.roniyaakobi.objects;

import org.roniyaakobi.Constants;
import org.roniyaakobi.utils.Logger;
import org.roniyaakobi.utils.Timestamp;
import org.roniyaakobi.utils.User;
import org.roniyaakobi.utils.BytesFormatter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
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

        Logger.log("Added commit: \n" + serialize() + "\n\n");

        store();
    }

    /**
     * Serialize the commit
     * @return A string of the serialize commit
     */
    private String serialize() {
        return "tree " +  HexFormat.of().formatHex(treeId.getBytes()) +
                " parent " +
                (parentId.isPresent() ?
                        HexFormat.of().formatHex(parentId.get().getBytes()) :
                        Constants.NONE) +
                " author " + author + " " + timestamp.toSerializable() + " " +
                message;
    }

    /**
     * Deserialize the byte array to a commit object
     * @param serialized byte array
     * @return a deserialized Commit object
     * @throws IOException
     */
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
    public byte[] compressBody() {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream();
             DeflaterOutputStream deflater =
                     new DeflaterOutputStream(output)) {

            deflater.write(serialize().getBytes());

            deflater.finish();

            return output.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public RegitObjects.ObjectType getType() {
        return RegitObjects.ObjectType.COMMIT;
    }

    /**
     * Get the parentId of the commit
     * @return the parent's id
     */
    public Optional<RegitObjects.ObjectId> getParentId(){
        return parentId;
    }

    /**
     * Get the treeId of the commit
     * @return the objectId of the tree in the commit
     */
    public RegitObjects.ObjectId getTreeId(){
        return treeId;
    }

    public void load(Path path) throws IOException {
        Tree tree = (Tree) Archivable.unarchive(treeId);
        tree.loadTree(path);
    }
}