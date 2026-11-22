package org.roniyaakobi.branches;

import org.roniyaakobi.Constants;
import org.roniyaakobi.comments.CommentUpdater;
import org.roniyaakobi.objects.*;
import org.roniyaakobi.utils.FileUtil;
import org.roniyaakobi.utils.Timestamp;
import org.roniyaakobi.utils.User;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class Branch {
    private final String name;
    private RegitObjects.ObjectId lastCommitID;

    public Branch(String name, RegitObjects.ObjectId lastCommitID){
        this.name = name;
        this.lastCommitID = lastCommitID;
    }

    /**
     * Load the main branch from HEAD
     * @return the main branch, with no last commit if nothing was committed yet
     * @throws IOException
     */
    public static Branch loadMain() throws IOException {
        if (!Files.exists(Constants.HEAD)){
            return new Branch("main", null);
        }

        RegitObjects.ObjectId objectID =
                RegitObjects.ObjectId.fromHex(
                        Files.readString(Constants.HEAD).strip(),
                        RegitObjects.ObjectType.COMMIT
                );

        return new Branch("main", objectID);
    }

    /**
     * Get the id of the last commit on the branch
     * @return the id of the last commit, or null if nothing was committed yet
     */
    public RegitObjects.ObjectId getLastCommitId(){
        return lastCommitID;
    }

    public void saveBranch(Path file) throws IOException {
        FileUtil.writeFile(file, lastCommitID.getHex().getBytes());
    }

    public static void saveHead(Branch branch) throws IOException {
        branch.saveBranch(Constants.HEAD);
    }

    public void loadCurrentCommit(Path directory) throws IOException {
        Commit commit = (Commit) Archivable.unarchive(lastCommitID);
        commit.load(directory);
        FileUtil.writeFile(Constants.CURRENT, commit.getObjectId().getHex().getBytes());
    }

    /**
     * Commit a directory on top of the branch's last commit, moving the comments to match the new file contents.
     * If the branch has no commits yet, this makes the first commit
     * @param directory the working directory root
     * @param message the commit message
     * @throws IOException
     */
    public void commit(Path directory, String message) throws IOException {
        Tree previousTree = null;

        if (lastCommitID != null){
            Commit previousCommit = (Commit) Archivable.unarchive(lastCommitID);
            previousTree = (Tree) Archivable.unarchive(previousCommit.getTreeId());
        }

        Tree newTree = TreeBuilder.buildTree(directory);
        CommentUpdater.update(previousTree, newTree);

        Commit newCommit = new Commit(
                newTree.getObjectId(),
                Optional.ofNullable(lastCommitID),
                User.instance,
                Timestamp.now(),
                message
        );

        this.lastCommitID = newCommit.getObjectId();
    }
}
