package org.roniyaakobi;

import org.roniyaakobi.branches.Branch;
import org.roniyaakobi.comments.Comment;
import org.roniyaakobi.comments.CommentLoader;
import org.roniyaakobi.diff.DiffCalculator;
import org.roniyaakobi.objects.*;
import org.roniyaakobi.utils.BytesFormatter;
import org.roniyaakobi.utils.User;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class Main {
    private static final Scanner reader = new Scanner(System.in);

    private static final String RESET = "\033[0m";
    private static final String RED = "\033[31m";
    private static final String GREEN = "\033[32m";
    private static final String YELLOW = "\033[33m";

    private static User user;

    public static void main(String[] args) throws IOException {
        user = login();

        printHelp();

        while (true) {
            System.out.println("Enter the command you want to use: ");

            if (!reader.hasNextLine()) {
                exitRegit();
                return;
            }

            String command = reader.nextLine().strip();

            try {
                switch (command) {
                    case "commit" -> commit();
                    case "comment" -> comment();
                    case "diff" -> diff();
                    case "help" -> printHelp();
                    case "exit" -> {
                        exitRegit();
                        return;
                    }
                    case "" -> {}
                    default -> System.out.println("Invalid command! Type help to see the commands.");
                }
            } catch (IOException | RuntimeException e) {
                System.out.println("Command failed: " + e);
            }
        }
    }

    /**
     * Print the commands of the cli
     */
    private static void printHelp(){
        System.out.println("Commands:");
        System.out.println("  commit   commit every file and directory under a source directory");
        System.out.println("  comment  add a comment on lines of a file");
        System.out.println("  diff     show the diff of a file between two commits");
        System.out.println("  help     show this list");
        System.out.println("  exit     save and quit");
    }

    /**
     * Get the user data from the user
     * @return A User object based on what the user entered
     */
    private static User login(){
        String username = readField("Enter your username: ");
        String email = readField("Enter your email: ");

        return new User(username, email);
    }

    /**
     * Read a user field, which must not be empty or contain spaces, commas or '=' since commits store it that way
     * @param prompt the prompt to show
     * @return the field
     */
    private static String readField(String prompt){
        while (true) {
            System.out.println(prompt);
            String field = reader.nextLine().strip();

            if (!field.isEmpty() && !field.matches(".*[\\s,=].*")) {
                return field;
            }

            System.out.println("It must not be empty or contain spaces, commas or '='.");
        }
    }

    /**
     * Commit every file and directory under a source directory on top of the last commit
     * @throws IOException
     */
    private static void commit() throws IOException {
        System.out.println("Enter the source directory to commit: ");
        Path directory = Path.of(reader.nextLine().strip());

        if (!Files.isDirectory(directory)) {
            System.out.println("Not a directory: " + directory);
            return;
        }

        System.out.println("Enter the message for the commit: ");
        String message = reader.nextLine().strip();

        Branch main = Branch.loadMain();
        main.commit(directory, message);
        Branch.saveHead(main);

        saveState();

        System.out.println("Committed " + main.getLastCommitId().getHex());
    }

    /**
     * Add a comment on a range of lines in a file
     */
    private static void comment() {
        System.out.println("Enter the path of the file, relative to the source directory: ");
        Path path = Path.of(reader.nextLine().strip()).normalize();

        if (path.isAbsolute() || path.toString().isEmpty() || path.startsWith("..")) {
            System.out.println("The path must be relative to the source directory, like others/second");
            return;
        }

        int startLine = readLineNumber("Enter the start line: ");
        int endLine = readLineNumber("Enter the end line: ");

        if (endLine < startLine) {
            System.out.println("The end line must not be before the start line.");
            return;
        }

        System.out.println("Enter the comment: ");
        String content = reader.nextLine();

        CommentLoader.addComment(path, new Comment(content, startLine, endLine));
        CommentLoader.store();

        System.out.println("Added a comment on " + path + " lines " + startLine + "-" + endLine);
    }

    /**
     * Read a line number, which must be at least 1
     * @param prompt the prompt to show
     * @return the line number
     */
    private static int readLineNumber(String prompt){
        while (true) {
            System.out.println(prompt);
            String input = reader.nextLine().strip();

            try {
                int line = Integer.parseInt(input);

                if (line >= 1) {
                    return line;
                }
            } catch (NumberFormatException ignored) {}

            System.out.println("Enter a whole number, starting from 1.");
        }
    }

    /**
     * Read a commit hash and load the commit from the archive
     * @param prompt the prompt to show
     * @return the commit
     * @throws IOException
     */
    private static Commit readCommit(String prompt) throws IOException {
        System.out.println(prompt);
        String hex = reader.nextLine().strip().toLowerCase();

        if (!hex.matches("[0-9a-f]{40}")) {
            throw new IllegalArgumentException("A commit hash is 40 hex characters: " + hex);
        }

        RegitObjects.ObjectId id = RegitObjects.ObjectId.fromHex(hex, RegitObjects.ObjectType.COMMIT);

        if (Archivable.loadRecord(id).type() != RegitObjects.ObjectType.COMMIT) {
            throw new IllegalArgumentException("Object " + hex + " is not a commit");
        }

        return (Commit) Archivable.unarchive(id);
    }

    /**
     * Calculate a diff between two commits and files in the commits
     * @throws IOException
     */
    private static void diff() throws IOException {
        Commit oldCommit = readCommit("Enter the commit hash for the old commit: ");
        Commit newCommit = readCommit("Enter the commit hash for the new commit: ");

        System.out.println("Enter the path of the file, relative to the source directory: ");
        Path path = Path.of(reader.nextLine().strip()).normalize();

        DiffCalculator.FileDiff diff = new DiffCalculator(oldCommit, newCommit).calculate(path);

        switch (diff.pathState()) {
            case ADDED -> System.out.println(path + " is not in the old commit, it was added in the new one.");
            case DELETED -> System.out.println(path + " was deleted in the new commit.");
            case NONEXSISTANT -> System.out.println(path + " is not a file in either commit.");
            case PERSERVED -> {
                if (diff.LCS() == null) {
                    System.out.println(path + " is not a file in either commit.");
                } else if (diff.additionList().isEmpty() && diff.deletionList().isEmpty()) {
                    System.out.println("No changes in " + path);
                } else {
                    printDiff(diff);
                }
            }
        }
    }

    /**
     * Print a diff: the common text in yellow, deletions in red and additions in green
     * @param diff the diff of a file
     */
    private static void printDiff(DiffCalculator.FileDiff diff){
        String LCSString = BytesFormatter.bytesToString(diff.LCS());

        for (int i = 0; i <= LCSString.length(); i++){
            int offset = i;

            diff.deletionList().forEach(deletion -> {
                if (deletion.byteOffset() == offset){
                    System.out.print(RED + "{-" + deletion.diff() + "}" + RESET);
                }
            });

            diff.additionList().forEach(addition -> {
                if (addition.byteOffset() == offset){
                    System.out.print(GREEN + "{+" + addition.diff() + "}" + RESET);
                }
            });

            if (i < LCSString.length()) {
                System.out.print(YELLOW + LCSString.charAt(i) + RESET);
            }
        }

        System.out.println();
    }

    /**
     * Save the object map and the comments
     */
    private static void saveState(){
        Archivable.storeMap();
        CommentLoader.store();
    }

    private static void exitRegit(){
        saveState();
    }
}
