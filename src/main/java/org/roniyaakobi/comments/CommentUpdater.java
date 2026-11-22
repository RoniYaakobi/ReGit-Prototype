package org.roniyaakobi.comments;

import org.roniyaakobi.diff.LineMapper;
import org.roniyaakobi.objects.Archivable;
import org.roniyaakobi.objects.RegitObjects;
import org.roniyaakobi.objects.Tree;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public class CommentUpdater {

    /**
     * Move the comments to match a new commit: comments on changed files follow their lines,
     * and comments on files that are no longer in the commit are removed
     * @param previous the tree of the previous commit, or null if there is none
     * @param current the tree of the new commit
     * @throws IOException
     */
    public static void update(Tree previous, Tree current) throws IOException {
        update(CommentLoader.COMMENTS, previous, current);
    }

    /**
     * Update the comments under a node of the comment tree
     * @param node the comment node of the directory
     * @param previous the directory's tree in the previous commit, or null if it was not there
     * @param current the directory's tree in the new commit
     * @throws IOException
     */
    private static void update(CommentLoader.CommentNode node, Tree previous, Tree current) throws IOException {
        node.getChildren().keySet().removeIf(name -> !current.getEntries().containsKey(name));

        for (Map.Entry<String, CommentLoader.CommentNode> child : node.getChildren().entrySet()) {
            Tree.TreeEntry now = current.getEntries().get(child.getKey());
            Tree.TreeEntry before = previous == null ? null : previous.getEntries().get(child.getKey());

            if (before != null && before.objectId().equals(now.objectId())) {
                continue;
            }

            if (now.type() == RegitObjects.ObjectType.TREE) {
                Tree previousSubTree = before != null && before.type() == RegitObjects.ObjectType.TREE ?
                        (Tree) Archivable.unarchive(before.objectId()) : null;

                update(child.getValue(), previousSubTree, (Tree) Archivable.unarchive(now.objectId()));

            } else if (before != null && before.type() == RegitObjects.ObjectType.BLOB) {
                LineMapper.LineMap lineMap = LineMapper.map(
                        Archivable.loadBytes(before.objectId()),
                        Archivable.loadBytes(now.objectId())
                );

                moveComments(child.getValue(), lineMap);
            }
        }
    }

    /**
     * Move the comments of a file to their lines in the new version, removing comments whose lines were removed
     * @param node the comment node of the file
     * @param lineMap the line map from the old version of the file to the new one
     */
    private static void moveComments(CommentLoader.CommentNode node, LineMapper.LineMap lineMap) {
        node.getComments().removeIf(comment -> {
            Optional<LineMapper.LineRange> range =
                    lineMap.mapRange(comment.getStartLine(), comment.getEndLine());

            range.ifPresent(newRange -> {
                comment.setStartLine(newRange.startLine());
                comment.setEndLine(newRange.endLine());
            });

            return range.isEmpty();
        });
    }
}
