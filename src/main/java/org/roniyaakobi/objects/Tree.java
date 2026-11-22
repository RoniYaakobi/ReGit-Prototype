package org.roniyaakobi.objects;

import org.roniyaakobi.utils.BytesFormatter;
import org.roniyaakobi.utils.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.DeflaterOutputStream;

public final class Tree extends RegitObject {
    private final Map<String, TreeEntry> entries;
    public Tree(List<TreeEntry> entries) throws IOException {
        this.entries = entries.stream()
                .collect(Collectors.toMap(
                        TreeEntry::name,
                        Function.identity()
                ));;
        Logger.log("Tree: ");


        getEntryIterable().forEach(entry ->
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

    private Stream<TreeEntry> getEntryIterable(){
        return entries.values().stream().sorted(Tree::isBigger);
    }

    /**
     * An entry in a tree
     * @param name the file or directory name
     * @param objectId the id of the object the entry points to
     * @param type the type of the object
     */
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
     * Apply a tree entry consumer on every tree entry
     * @param treeEntryConsumer the entry consumer
     */
    public void applyForEachEntry(Consumer<TreeEntry> treeEntryConsumer){
        for (TreeEntry entry : getEntryIterable().toList()){
            treeEntryConsumer.accept(entry);
        }
    }

    /**
     * Turn an entry to string formatted for saving: "TYPE name\0hex\n"
     * @param entry the TreeEntry to format
     * @return the String of the formatted tree entry
     */
    public static String formatEntry(TreeEntry entry){
        return entry.type().name() + " " + entry.name() + "\0" +
                HexFormat.of().formatHex(entry.objectId().getBytes()) + "\n";
    }

    @Override
    public byte[] compressBody() {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream();
             DeflaterOutputStream deflater =
                     new DeflaterOutputStream(output)) {

            applyForEachEntry(
                    entry -> {
                        try {
                            deflater.write(Tree.formatEntry(entry).getBytes());
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
    public RegitObjects.ObjectType getType() {
        return RegitObjects.ObjectType.TREE;
    }

    /**
     * Deserialize a tree object from its inflated body
     * @param serialized serialized data
     * @return Tree object deserialized from bytes
     * @throws IOException
     */
    public static Tree deserialize(byte[] serialized) throws IOException {
        String serializedString = BytesFormatter.bytesToString(serialized);

        String[] entries = serializedString.split("\n");

        List<TreeEntry> treeEntryList = new ArrayList<TreeEntry>();

        for (String entry : entries){
            if (entry.isEmpty()){
                continue;
            }

            int nulIndex = entry.indexOf('\0');

            String metaData = entry.substring(0, nulIndex);
            String hex = entry.substring(nulIndex + 1);

            String[] fields = metaData.split(" ", 2);

            RegitObjects.ObjectType type = RegitObjects.ObjectType.valueOf(fields[0]);

            String name = fields[1];

            RegitObjects.ObjectId entryId = new RegitObjects.ObjectId(
                    BytesFormatter.hexToBytes(hex),
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
    public Map<String, TreeEntry> getEntries(){
        return entries;
    }

    public void loadTree(Path path){
        applyForEachEntry(entry -> Tree.loadEntry(path, entry));
    }
    public static void loadEntry(Path path, TreeEntry entry){
        path = path.resolve(entry.name());
        if (entry.type() == RegitObjects.ObjectType.BLOB){
            try {
                Blob.load(path, entry.objectId());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (entry.type() == RegitObjects.ObjectType.TREE) {
            try {
                Tree subtree = (Tree) Archivable.unarchive(entry.objectId());
                subtree.loadTree(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
