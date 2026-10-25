package org.roniyaakobi.objects;

import org.roniyaakobi.utils.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class TreeBuilder {
    public static Tree buildTree(Path directory) throws IOException {
        Logger.log("Building tree: " + directory.toString());
        List<Tree.TreeEntry> treeEntries = new ArrayList<>();

        try (var paths = Files.walk(directory, 1)){
            for (Path path : paths.toList()) {
                if (path.compareTo(directory) == 0){
                    continue;
                }
                String name = path.getName(path.getNameCount() - 1).toString();
                if (Files.isDirectory(path)) {
                    Tree subTree = buildTree(path);
                    treeEntries.add(
                            new Tree.TreeEntry(
                                    name,
                                    subTree.getId(),
                                    RegitObjects.ObjectType.TREE
                            )
                    );
                } else {
                    Blob file = Blob.fromPath(path);
                    treeEntries.add(
                            new Tree.TreeEntry(
                                    name,
                                    file.getId(),
                                    RegitObjects.ObjectType.BLOB
                            )
                    );
                }
            }
        }

        Logger.log("Finished building tree: " + directory.toString());

        return new Tree(treeEntries);
    }
}
