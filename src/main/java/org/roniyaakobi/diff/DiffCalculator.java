package org.roniyaakobi.diff;

import org.roniyaakobi.objects.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class DiffCalculator {

    public enum PathState {
        ADDED,
        DELETED,
        PERSERVED,
        NONEXSISTANT
    }

    public record FileDiff(
            Path file,
            byte[] LCS,
            List<DiffSection> additionList,
            List<DiffSection> deletionList,
            PathState pathState){ }
    public record DiffSection(String diff , int byteOffset){ }
    private final Commit oldCommit;

    private final Commit newCommit;

    public DiffCalculator(Commit oldCommit, Commit newCommit){
        this.oldCommit = oldCommit;
        this.newCommit = newCommit;
    }

    public DiffCalculator(Commit commit) throws IOException {
        this.newCommit = commit;

        Optional<RegitObjects.ObjectId> parent = commit.getParentId();

        if (parent.isPresent()){
            this.oldCommit = (Commit)Compressable.deserialize(parent.get());
        } else {
            throw new IllegalArgumentException("Commit had no parent to Diff to.");
        }
    }

    public FileDiff calculate(Path file) throws IOException {
        RegitObjects.ObjectId oldTreeId = oldCommit.getTreeId();
        RegitObjects.ObjectId newTreeId = newCommit.getTreeId();

        Tree oldTree = (Tree)Compressable.deserialize(oldTreeId);
        Tree newTree = (Tree)Compressable.deserialize(newTreeId);

        return ofTrees(file, file, oldTree, newTree);


    }

    public static FileDiff ofTrees(Path fullPath, Path subTreePath, Tree oldTree, Tree newTree) throws IOException {
        List<Tree.TreeEntry> oldEntries = oldTree.getEntries();
        List<Tree.TreeEntry> newEntries = newTree.getEntries();

        if (oldEntries.size() == 0 &&
                newEntries.size() == 0) {
            return new FileDiff(fullPath, null, null,
                    null, PathState.PERSERVED);
        }


        Map<Path, Tree.TreeEntry> pathsToOldEntries = new HashMap<>();
        Map<Path, Tree.TreeEntry> pathsToNewEntries = new HashMap<>();

        for (Tree.TreeEntry entry : oldEntries) {
            Path entryPath = Path.of(entry.name());

            pathsToOldEntries.put(entryPath.getName(entryPath.getNameCount() - 1), entry);
        }

        for (Tree.TreeEntry entry : newEntries) {
            Path entryPath = Path.of(entry.name());

            pathsToNewEntries.put(entryPath.getName(entryPath.getNameCount() - 1), entry);
        }

        Path subtree = subTreePath.getName(0);

        PathState pathState = PathState.NONEXSISTANT;
        if (pathsToNewEntries.containsKey(subtree) && pathsToOldEntries.containsKey(subtree)) {
            pathState = PathState.PERSERVED;

        } else if (pathsToNewEntries.containsKey(subtree) ^ pathsToOldEntries.containsKey(subtree)) {
            pathState = pathsToNewEntries.containsKey(subtree) ? PathState.ADDED : PathState.DELETED;
        }


        if (pathState != PathState.PERSERVED) {
            return new FileDiff(fullPath, null, null,
                    null, pathState);
        }


        return switch (pathsToNewEntries.get(subtree).type()) {
            case BLOB -> FileDiffCalculator.calculate(

                    Compressable.loadObjectId(pathsToOldEntries.get(subtree).objectId()),

                    Compressable.loadObjectId(pathsToNewEntries.get(subtree).objectId()),

                    pathState,

                    fullPath);

            case TREE -> {
                Tree oldSubTree = (Tree) Compressable.deserialize(pathsToOldEntries.get(subtree).objectId());
                Tree newSubTree = (Tree) Compressable.deserialize(pathsToNewEntries.get(subtree).objectId());

                yield ofTrees(fullPath, subTreePath.subpath(1, subTreePath.getNameCount() - 1), oldSubTree, newSubTree);
            }

            default -> throw new IllegalStateException("Tree may only contain other trees or blobs.");
        };
    }

}
