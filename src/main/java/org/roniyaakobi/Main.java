package org.roniyaakobi;

import org.roniyaakobi.diff.DiffCalculator;
import org.roniyaakobi.objects.*;
import org.roniyaakobi.utils.BytesFormatter;
import org.roniyaakobi.utils.Timestamp;
import org.roniyaakobi.utils.User;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Scanner;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    private static final Scanner reader = new Scanner(System.in);

    private static User user;

    public static void main(String[] args) throws IOException {

        user = login();

        System.out.println("Enter the command you want to use: ");

        String command = reader.nextLine().strip();

        if (command.equals("commit")) {
            commit();
        } else if (command.equals("diff")){
            diff();
        } else {
            System.out.println("Invalid command!");
        }
    }

    private static User login(){
        System.out.println("Enter your username: ");
        String username = reader.nextLine();

        System.out.println("Enter your email: ");
        String email = reader.nextLine();

        return new User(username, email);
    }

    private static void commit() throws IOException {
        System.out.println("Enter the path for the tree: ");
        String path = reader.nextLine().strip();

        System.out.println("Enter the message for the commit: ");
        String message = reader.nextLine().strip();

        Tree tree = TreeBuilder.buildTree(Path.of(path));

        Commit commit = new Commit(
                tree.getId(),
                Optional.empty(),
                user,
                Timestamp.now(),
                message
        );
    }

    private static void diff() throws IOException {
        System.out.println("Enter the commit hash for the old commit: ");
        String oldHash = reader.nextLine();

        System.out.println("Enter the commit hash for the new commit: ");
        String newHash = reader.nextLine();

        System.out.println("Enter the path to the file to apply diff: ");
        String filePath = reader.nextLine();

        RegitObjects.ObjectId oldId = RegitObjects.ObjectId.fromPath(
                Path.of(oldHash.substring(0,2), oldHash.substring(2)),
                RegitObjects.ObjectType.COMMIT
        );

        RegitObjects.ObjectId newId = RegitObjects.ObjectId.fromPath(
                Path.of(newHash.substring(0,2), newHash.substring(2)),
                RegitObjects.ObjectType.COMMIT
        );

        DiffCalculator calculator = new DiffCalculator(
                (Commit) Compressable.deserialize(oldId),
                (Commit) Compressable.deserialize(newId)
        );

        DiffCalculator.FileDiff diff = calculator.calculate(Path.of(filePath));

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

            System.out.print(YELLOW + LCSString.substring(i,i+1) + RESET);
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