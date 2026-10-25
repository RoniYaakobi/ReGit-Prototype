package org.roniyaakobi.objects;

import org.roniyaakobi.Constants;
import org.roniyaakobi.utils.BytesFormatter;
import org.roniyaakobi.utils.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.zip.DeflaterOutputStream;

public final class Blob extends RegitObject {
    public record StreamData(byte[] bytes, int length) {}

    public record Comment(String comment, int startLine, int endLine) {
        public static final String commentDelimeter = "\u001E";
        public static final String commentFieldDelimeter = "\u001F";

    }

    private final List<Comment> comments = new ArrayList<>();

    private final Path path;

    private RegitObjects.ObjectId objectId = null;
    private final long size;

    private Blob(Path path, long size) throws IOException {
        Logger.log("Created blob object for: " + path.toString() + "\n");
        this.path = path;
        this.size = size;

        this.objectId = hash();

        store();
    }


    /**
     * Add a comment to the blob object
     * @param comment The new comment
     */
    public void addComment(Comment comment){
        comments.add(comment);
    }

    /**
     * Get the size of the blob file
     * @return the size of the blob file
     */
    public long getSize(){
        return size;
    }

    /**
     * Create a blob from a given path
     * @param path The path of the blob source
     * @return A blob object
     * @throws IOException
     */
    public static Blob fromPath(Path path) throws IOException {
        return new Blob(path, Files.size(path));
    }

    /**
     * Build a blob from bytes
     * @param path The source directory of the blob
     * @param header The header bytes
     * @return A blob object
     * @throws IOException
     */
    public static Blob fromBytes(Path path, byte[] header) throws IOException {
        String headerString = BytesFormatter.bytesToString(header);

        String[] headerFields = headerString.split(" ");

        String blobString = headerFields[0], commentsStrings = headerFields[1], sizeString = headerFields[2];

        long size = Long.getLong(sizeString);

        Blob blob = new Blob(path, size);

        if (commentsStrings == Constants.NONE){
            return blob;
        }

        String[] comments = commentsStrings.split(Comment.commentDelimeter);

        for (String comment : comments){
            String[] fields = comment.split(Comment.commentFieldDelimeter);

            int startLine = Integer.getInteger(fields[0]);
            int endLine = Integer.getInteger(fields[1]);

            String commentString = fields[2];

            blob.addComment(new Comment(commentString, startLine, endLine));
        }

        return blob;
    }

    /**
     * Build the header bytes
     * @return the header bytes
     */
    public byte[] buildHeaderBytes(){
        String header = "blob " + getComments() + getSize() + "\0";
        return header.getBytes();
    }

    /**
     * Stream 8kb sections into a consumer
     * @param streamDataConsumer the consumer for the bytes of the file
     */
    public void streamFileContents(Consumer<StreamData> streamDataConsumer){
        try (InputStream in = Files.newInputStream(path)) {
            byte[] buffer = new byte[8192];

            for (int n = in.read(buffer); n != -1; n = in.read(buffer)) {
                streamDataConsumer.accept(new StreamData(buffer, n));
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to read bytes from file " + path, e);
        }
    }

    @Override
    public byte[] compress(){
        try (ByteArrayOutputStream output = new ByteArrayOutputStream();
             DeflaterOutputStream deflater =
                     new DeflaterOutputStream(output)) {


            deflater.write(buildHeaderBytes());

            streamFileContents(
                    streamData -> {
                        try {
                            deflater.write(streamData.bytes(), 0, streamData.length());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
            );

            deflater.finish();

            return output.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public RegitObjects.ObjectId hash() {
        if (objectId != null){
            return objectId;
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");

            byte[] headerBytes = buildHeaderBytes();

            digest.update(headerBytes);

            streamFileContents(
                    streamData ->
                            digest.update(streamData.bytes(), 0 ,streamData.length()));

            return new RegitObjects.ObjectId(digest.digest(), RegitObjects.ObjectType.BLOB);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-1 is not available", e);
        }
    }

    /**
     * Format all the comments into a string
     * @return The formatted comments for saving in the header
     */
    public String getComments() {
        String commentsString = "";

        for (Comment comment : comments){
            commentsString +=
                    comment.startLine() + Comment.commentFieldDelimeter +
                    comment.endLine() + Comment.commentFieldDelimeter +
                    comment.comment() + Comment.commentDelimeter;
        }

        if (commentsString.length() >= 2){
            commentsString = commentsString.substring(0, commentsString.length() - 2) + " ";
        } else {
            commentsString = Constants.NONE;
        }

        return commentsString;
    }
}
