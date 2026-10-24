package org.roniyaakobi;

import org.roniyaakobi.diff.DiffCalculator;
import org.roniyaakobi.objects.*;
import org.roniyaakobi.utils.BytesFormatter;
import org.roniyaakobi.utils.Timestamp;
import org.roniyaakobi.utils.User;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length < 1){
            throw new RuntimeException("Regit needs a file!");
        }


        Tree tree = TreeBuilder.buildTree(Path.of(args[0]));

        Commit commit = new Commit(
                tree.getId(),
                Optional.empty(),
                new User("Roni", "RoniYaakobi@gmail.com"),
                Timestamp.now(),
                "Heh funny"
        );

        System.out.println(commit.getId().getPath());


//
//        RegitObjects.ObjectId oldId = RegitObjects.ObjectId.fromPath(
//                Path.of("54/cf0c97eb5550ee83d87550c04458b5000cafa0"),
//                RegitObjects.ObjectType.COMMIT
//        );
//
//        RegitObjects.ObjectId newId = RegitObjects.ObjectId.fromPath(
//                Path.of("08/cf0d8f449135b250ea19353e3d6dd22cf6d9c7"),
//                RegitObjects.ObjectType.COMMIT
//        );
//
//        DiffCalculator calculator = new DiffCalculator(
//                (Commit) Compressable.deserialize(oldId),
//                (Commit) Compressable.deserialize(newId)
//        );
        ;

//        DiffCalculator.FileDiff diff = calculator.calculate(Path.of("funny/bruh"));
//
//        String LCSString = BytesFormatter.bytesToString(diff.LCS());
//
//
//        String RESET = "\033[0m";
//        String RED = "\033[31m";
//        String GREEN = "\033[32m";
//        String YELLOW = "\033[33m";
//
//        int i;
//        for (i = 0; i < LCSString.length(); i++){
//            int finalI = i;
//            diff.deletionList().forEach(deletion -> {
//                if (deletion.byteOffset() == finalI){
//                    System.out.print(RED + "{-" + deletion.diff() + "}" + RESET);
//                }
//            });
//
//            diff.additionList().forEach(addition -> {
//                if (addition.byteOffset() == finalI){
//                    System.out.print(GREEN + "{+" + addition.diff() + "}" + RESET);
//                }
//            });
//
//            System.out.print(YELLOW + LCSString.substring(i,i+1) + RESET);
//        }
//
//        int finalI = i;
//        diff.deletionList().forEach(deletion -> {
//            if (deletion.byteOffset() == finalI){
//                System.out.print(RED + "{-" + deletion.diff() + "}" + RESET);
//            }
//        });
//
//        diff.additionList().forEach(addition -> {
//            if (addition.byteOffset() == finalI){
//                System.out.print(GREEN + "{+" + addition.diff() + "}" + RESET);
//            }
//        });


    }
}