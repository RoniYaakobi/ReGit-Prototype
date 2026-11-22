package org.roniyaakobi.comments;

import org.roniyaakobi.Constants;
import org.roniyaakobi.utils.FileUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CommentLoader {

    /**
     * A node in the comment tree. Every node is a path segment and every leaf is a file.
     * Comments are expected only on leaves, but this is not enforced
     */
    public static class CommentNode {
        private final Map<String, CommentNode> children = new TreeMap<>();
        private final List<Comment> comments = new ArrayList<>();

        /**
         * Get a child node
         * @param name the name of the path segment
         * @return the child node, or null if it does not exist
         */
        public CommentNode getChild(String name){
            return children.get(name);
        }

        /**
         * Get a child node, creating it if it does not exist
         * @param name the name of the path segment
         * @return the child node
         */
        public CommentNode getOrCreateChild(String name){
            return children.computeIfAbsent(name, key -> new CommentNode());
        }

        /**
         * Get the child nodes
         * @return the children by path segment name
         */
        public Map<String, CommentNode> getChildren(){
            return children;
        }

        /**
         * Get the comments on this node
         * @return the live list of comments
         */
        public List<Comment> getComments(){
            return comments;
        }
    }

    public static final CommentNode COMMENTS = load();

    /**
     * Load the comment tree from the comments file. Each record is
     * "path\u001FstartLine\u001FendLine\u001Fcontent" and records are separated by "\u001E"
     * @return the root of the comment tree
     */
    static CommentNode load(){
        CommentNode root = new CommentNode();

        try {
            FileUtil.createFileIfNotExists(Constants.COMMENTS_PATH);

            String serialized = Files.readString(Constants.COMMENTS_PATH);

            for (String record : serialized.split(Comment.RECORD_DELIMITER)){
                if (record.isBlank()){
                    continue;
                }

                String[] fields = record.split(Comment.FIELD_DELIMITER, 4);

                Comment comment = new Comment(
                        fields[3],
                        Integer.parseInt(fields[1]),
                        Integer.parseInt(fields[2])
                );

                getOrCreateNode(root, Path.of(fields[0])).getComments().add(comment);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return root;
    }

    /**
     * Write every comment in the comment tree to the comments file
     */
    public static void store(){
        StringBuilder serialized = new StringBuilder();

        storeNode(COMMENTS, "", serialized);

        try {
            FileUtil.writeFile(Constants.COMMENTS_PATH, serialized.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Serialize the comments of a node and all of its descendants
     * @param node the node
     * @param path the path of the node, segments joined by "/"
     * @param serialized the builder to append the records to
     */
    private static void storeNode(CommentNode node, String path, StringBuilder serialized){
        for (Comment comment : node.getComments()){
            serialized.append(path).append(Comment.FIELD_DELIMITER)
                    .append(comment.getStartLine()).append(Comment.FIELD_DELIMITER)
                    .append(comment.getEndLine()).append(Comment.FIELD_DELIMITER)
                    .append(comment.getContent())
                    .append(Comment.RECORD_DELIMITER);
        }

        for (Map.Entry<String, CommentNode> child : node.getChildren().entrySet()){
            String childPath = path.isEmpty() ? child.getKey() : path + "/" + child.getKey();

            storeNode(child.getValue(), childPath, serialized);
        }
    }

    /**
     * Walk down from a node along a path, creating missing nodes
     * @param root the node to start from
     * @param path the relative path
     * @return the node at the end of the path
     */
    private static CommentNode getOrCreateNode(CommentNode root, Path path){
        CommentNode node = root;

        for (Path segment : path){
            if (segment.toString().isEmpty()){
                continue;
            }

            node = node.getOrCreateChild(segment.toString());
        }

        return node;
    }

    /**
     * Get the node of a path in the comment tree
     * @param path the path relative to the working directory root
     * @return the node, or null if no node exists for the path
     */
    public static CommentNode getNode(Path path){
        CommentNode node = COMMENTS;

        for (Path segment : path){
            if (segment.toString().isEmpty()){
                continue;
            }

            node = node.getChild(segment.toString());

            if (node == null){
                return null;
            }
        }

        return node;
    }

    /**
     * Get the comments on a file
     * @param path the path relative to the working directory root
     * @return the live list of comments, or an empty list if the file has none
     */
    public static List<Comment> getComments(Path path){
        CommentNode node = getNode(path);

        return node == null ? List.of() : node.getComments();
    }

    /**
     * Add a comment to a file
     * @param path the path relative to the working directory root
     * @param comment the comment
     */
    public static void addComment(Path path, Comment comment){
        getOrCreateNode(COMMENTS, path).getComments().add(comment);
    }

    /**
     * Remove a comment from a file
     * @param path the path relative to the working directory root
     * @param comment the comment
     * @return true if the comment was removed
     */
    public static boolean removeComment(Path path, Comment comment){
        CommentNode node = getNode(path);

        return node != null && node.getComments().remove(comment);
    }
}
