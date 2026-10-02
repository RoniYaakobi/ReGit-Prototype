package org.roniyaakobi.objects;

import org.roniyaakobi.Compressable;
import org.roniyaakobi.Hashable;
import org.roniyaakobi.utils.BytesFormatter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.function.Consumer;
import java.util.zip.DeflaterOutputStream;

public final class Tree extends RegitObject {

    private final List<TreeEntry> entries;
    public Tree(List<TreeEntry> entries) {
        this.entries = entries.stream().sorted(Tree::isBigger).toList();
    }
    public record TreeEntry(
            String name,
            RegitObjects.ObjectId objectId,
            RegitObjects.ObjectType type
    ) {}

    private static int isBigger(TreeEntry a , TreeEntry b){
        if (a.type() != b.type()){
            return (int) Math.signum(a.type().getPriority() - b.type().getPriority());
        }

        return a.name().compareTo(b.name());
    }

    public byte[] buildHeaderBytes(){
        String header = "tree " + getTreeSize() + "\0";
        return header.getBytes();
    }

    public int getTreeSize(){
        int entriesBytesSize = entries.size() * formatEntry(
                        new TreeEntry("",
                        new RegitObjects.ObjectId(new byte[0], RegitObjects.ObjectType.BLOB),
                        RegitObjects.ObjectType.BLOB)).length();

        for(TreeEntry entry : entries){
            entriesBytesSize += entry.name().length();
        }

        return entriesBytesSize;
    }

    public void applyForEachEntry(Consumer<TreeEntry> streamDataConsumer){
        for (TreeEntry entry : entries){
            streamDataConsumer.accept(entry);
        }
    }

    public static String formatEntry(TreeEntry entry){
        return entry.type().name() + " " + entry.name() + "\0" +
                BytesFormatter.bytesToString(entry.objectId().getBytes());
    }

    public byte[] compress() {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream();
             DeflaterOutputStream deflater =
                     new DeflaterOutputStream(output)) {


            deflater.write(buildHeaderBytes());

            applyForEachEntry(
                    entry -> {
                        try {
                            deflater.write(
                                    Tree.formatEntry(entry).getBytes(), 0,
                                    Tree.formatEntry(entry).getBytes().length);
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

    public RegitObjects.ObjectId hash(){
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");

            byte[] headerBytes = buildHeaderBytes();

            digest.update(headerBytes);

            applyForEachEntry(
                    entry ->
                            digest.update(Tree.formatEntry(entry).getBytes(),
                                    0 ,Tree.formatEntry(entry).getBytes().length)
            );

            return new RegitObjects.ObjectId(digest.digest(), RegitObjects.ObjectType.TREE);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-1 is not available", e);
        }
    }
}
