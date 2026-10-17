package org.roniyaakobi;

import org.roniyaakobi.diff.DiffCalculator;
import org.roniyaakobi.objects.*;
import org.roniyaakobi.utils.BytesFormatter;
import org.roniyaakobi.utils.Timestamp;
import org.roniyaakobi.utils.User;

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

        Blob blob = Blob.fromPath(Path.of(args[0]));

        blob.store();

        Tree tree = new Tree(
                List.of(new Tree.TreeEntry("blob", blob.getId(),
                        RegitObjects.ObjectType.BLOB))
        );

        tree.store();

        Commit commit = new Commit(
                tree.getId(),
                Optional.empty(),
                new User("Roni", "RoniYaakobi@gmail.com"),
                Timestamp.now(),
                "Heh funny"
        );

//        commit.store();



        RegitObjects.ObjectId oldId = RegitObjects.ObjectId.fromPath(
                Path.of("9a/54dfda49c3f43ba81cae6b3505fa4c010d9e2b"),
                RegitObjects.ObjectType.COMMIT
        );

        RegitObjects.ObjectId newId = RegitObjects.ObjectId.fromPath(
                Path.of("59/502b063334b4c343d19d2224fb261fb5f8d7e4"),
                RegitObjects.ObjectType.COMMIT
        );

        DiffCalculator calculator = new DiffCalculator(
                (Commit) Compressable.deserialize(oldId),
                (Commit) Compressable.deserialize(newId)
        );
        ;

        DiffCalculator.FileDiff diff = calculator.calculate(Path.of("blob"));

        String LCSString = BytesFormatter.bytesToString(diff.LCS());


        String RESET = "\033[0m";
        String RED = "\033[31m";
        String GREEN = "\033[32m";
        String YELLOW = "\033[33m";

        int i;
        for (i = 0; i < LCSString.length(); i++){
            int finalI = i;
            diff.deletionList().forEach(deletion -> {
                if (deletion.byteOffset() == finalI){
                    System.out.print(RED + "{-" + deletion.diff() + "}" + RESET);
                }
            });

            diff.additionList().forEach(addition -> {
                if (addition.byteOffset() == finalI){
                    System.out.print(GREEN + "{+" + addition.diff() + "}" + RESET);
                }
            });

            System.out.print(LCSString.substring(i,i+1));
        }

        int finalI = i;
        diff.deletionList().forEach(deletion -> {
            if (deletion.byteOffset() == finalI){
                System.out.print(RED + "{-" + deletion.diff() + "}" + RESET);
            }
        });

        diff.additionList().forEach(addition -> {
            if (addition.byteOffset() == finalI){
                System.out.print(GREEN + "{+" + addition.diff() + "}" + RESET);
            }
        });


    }
}