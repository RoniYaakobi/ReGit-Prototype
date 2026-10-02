package org.roniyaakobi;

import org.roniyaakobi.objects.Blob;
import org.roniyaakobi.objects.Commit;
import org.roniyaakobi.objects.RegitObjects;
import org.roniyaakobi.objects.Tree;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length < 1){
            throw new RuntimeException("Regit needs a file!");
        }

        Blob blob = Blob.from(Path.of(args[0]));

        blob.store();

        Tree tree = new Tree(
                List.of(new Tree.TreeEntry("blob", blob.getId(),
                        RegitObjects.ObjectType.TREE))
        );

        tree.store();

        Commit commit = new Commit(
                tree.getId(),
                Optional.empty(),
                new User("Roni", "RoniYaakobi@gmail.com"),
                Timestamp.now(),
                "Heh funny"
        );

        commit.store();
    }
}