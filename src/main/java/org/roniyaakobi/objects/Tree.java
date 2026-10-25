package org.roniyaakobi.objects;

import org.roniyaakobi.utils.BytesFormatter;
import org.roniyaakobi.utils.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.function.Consumer;
import java.util.zip.DeflaterOutputStream;

public final class Tree extends RegitObject {
    private final List<TreeEntry> entries;
    public Tree(List<TreeEntry> entries) throws IOException {
        this.entries = entries.stream().sorted(Tree::isBigger).toList();
        Logger.log("Tree: ");
        entries.forEach(entry ->
                {
                    try {
                        Logger.log(
                                "Name: " + entry.name() + "\n" +
                                "ObjectId: " + entry.objectId().getPath() + "\n" +
                                "Type: " + entry.type().name()
                        );
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
        Logger.log("\n");

        store();
    }
    public record TreeEntry(
            String name,
            RegitObjects.ObjectId objectId,
            RegitObjects.ObjectType type
    ) {}

    /**
     * Determine which of the two entries come first
     * @param a the first tree entry
     * @param b the second tree entry
     * @return 0 if they are equivalent, 1 if a shoudl be first and -1 if b should be first
     */
    private static int isBigger(TreeEntry a , TreeEntry b){
        if (a.type() != b.type()){
            return (int) Math.signum(a.type().getPriority() - b.type().getPriority());
        }

        return a.name().compareTo(b.name());
    }

    /**
     * Build the header bytes for the Tree object
     * @return a byte array of the header
     */
    public byte[] buildHeaderBytes(){
        String header = "tree " + getTreeSize() + "\0";
        return header.getBytes();
    }

    /**
     * Get the size of the tree
     * @return find the size of the tree object
     */
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

    /**
     * Apply a tree entry consumer on every tree entry
     * @param treeEntryConsumer the entry consumer
     */
    public void applyForEachEntry(Consumer<TreeEntry> treeEntryConsumer){
        for (TreeEntry entry : entries){
            treeEntryConsumer.accept(entry);
        }
    }

    /**
     * Turn an entry to string formatted for saving
     * @param entry the TreeEntry to format
     * @return the String of the formatted tree entry
     */
    public static String formatEntry(TreeEntry entry){
        return entry.type().name() + " " + entry.name() + "\0" +
        HexFormat.of().formatHex(entry.objectId().getBytes()) + "\n";
    }

    @Override
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

    @Override
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

    /**
     * Deserialize a tree object from byte array
     * @param serialized serialized data
     * @return Tree object deserialized from bytes
     * @throws IOException
     */
    public static Tree deserialize(byte[] serialized) throws IOException {
        String serializedString = BytesFormatter.bytesToString(serialized);

        String[] entries = serializedString.split("\n");

        List<TreeEntry> treeEntryList = new ArrayList<TreeEntry>();

        for (String entry : entries){

            byte[] entryBytes = entry.getBytes();


            byte[][] metaDataAndId = BytesFormatter.splitAtOffset(
                    entryBytes,
                    BytesFormatter.getBytesToNulTerminator(entryBytes)
            );


            byte[] metaData = metaDataAndId[0], idBytes = metaDataAndId[1];

            String[] fields = BytesFormatter.bytesToString(metaData).split(" ");



            String typeName = fields[0];

            RegitObjects.ObjectType type = RegitObjects.ObjectType.valueOf(typeName);

            String name = fields[1];

            RegitObjects.ObjectId entryId = new RegitObjects.ObjectId(
                    BytesFormatter.hexToBytes(BytesFormatter.bytesToString(idBytes)),
                    type
            );


            treeEntryList.add(new TreeEntry(name, entryId, type));
        }

        return new Tree(treeEntryList);
    }

    /**
     * Get the tree entries
     * @return the entries
     */
    public List<TreeEntry> getEntries(){
        return entries;
    }

}
