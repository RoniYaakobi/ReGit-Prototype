package org.roniyaakobi.branches;

import org.roniyaakobi.Constants;
import org.roniyaakobi.objects.Archivable;
import org.roniyaakobi.objects.Commit;
import org.roniyaakobi.objects.RegitObjects;
import org.roniyaakobi.utils.FileUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Branch {
    private final String name;
    private final RegitObjects.ObjectId lastCommitID;

    public Branch(String name, RegitObjects.ObjectId lastCommitID){
        this.name = name;
        this.lastCommitID = lastCommitID;
    }

    public static Branch loadMain() throws IOException {
        RegitObjects.ObjectId objectID =
                RegitObjects.ObjectId.fromHex(
                        Files.readString(Constants.HEAD),
                        RegitObjects.ObjectType.COMMIT
                );

        return new Branch("main", objectID);
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
}
