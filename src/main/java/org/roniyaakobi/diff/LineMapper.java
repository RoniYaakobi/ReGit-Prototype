package org.roniyaakobi.diff;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public final class LineMapper {

    /**
     * A range of lines in a file
     * @param startLine the first line, 1-based
     * @param endLine the last line, 1-based and inclusive
     */
    public record LineRange(int startLine, int endLine) {}

    /**
     * Where every line of an old version of a file ended up in the new version
     * @param oldToNew the 0-based new index of every old line, or -1 if the line was deleted or changed
     * @param newLineCount the number of lines in the new version
     */
    public record LineMap(int[] oldToNew, int newLineCount) {

        /**
         * Map a range of lines in the old version to the new version.
         * If some of the lines survived, the range covers them. If none survived but other lines
         * replaced them, the range covers the replacement lines. If the lines were only removed,
         * there is no range
         * @param startLine the first line of the range in the old version, 1-based
         * @param endLine the last line of the range in the old version, 1-based and inclusive
         * @return the range in the new version, or empty if the lines were removed
         */
        public Optional<LineRange> mapRange(int startLine, int endLine) {
            int oldLineCount = oldToNew.length;
            int start = startLine - 1;
            int end = endLine - 1;

            int first = -1;
            int last = -1;

            for (int i = Math.max(start, 0); i <= Math.min(end, oldLineCount - 1); i++) {
                if (oldToNew[i] != -1) {
                    if (first == -1) {
                        first = i;
                    }
                    last = i;
                }
            }

            if (first != -1) {
                return Optional.of(new LineRange(oldToNew[first] + 1, oldToNew[last] + 1));
            }

            int previousNew = -1;
            for (int i = Math.min(start, oldLineCount) - 1; i >= 0; i--) {
                if (oldToNew[i] != -1) {
                    previousNew = oldToNew[i];
                    break;
                }
            }

            int nextNew = newLineCount;
            for (int i = Math.max(end + 1, 0); i < oldLineCount; i++) {
                if (oldToNew[i] != -1) {
                    nextNew = oldToNew[i];
                    break;
                }
            }

            if (nextNew - previousNew > 1) {
                return Optional.of(new LineRange(previousNew + 2, nextNew));
            }

            return Optional.empty();
        }
    }

    /**
     * Calculate where every line of the old buffer ended up in the new buffer, using the
     * longest common subsequence of lines
     * @param oldBytes the old buffer
     * @param newBytes the new buffer
     * @return the line map from the old buffer to the new buffer
     */
    public static LineMap map(byte[] oldBytes, byte[] newBytes) {
        List<String> oldLines = splitLines(oldBytes);
        List<String> newLines = splitLines(newBytes);

        int oldCount = oldLines.size();
        int newCount = newLines.size();

        int[] oldToNew = new int[oldCount];
        Arrays.fill(oldToNew, -1);

        int prefix = 0;
        while (prefix < oldCount && prefix < newCount &&
                oldLines.get(prefix).equals(newLines.get(prefix))) {
            oldToNew[prefix] = prefix;
            prefix++;
        }

        int suffix = 0;
        while (suffix < oldCount - prefix && suffix < newCount - prefix &&
                oldLines.get(oldCount - 1 - suffix).equals(newLines.get(newCount - 1 - suffix))) {
            oldToNew[oldCount - 1 - suffix] = newCount - 1 - suffix;
            suffix++;
        }

        int oldEnd = oldCount - suffix;
        int newEnd = newCount - suffix;

        int rows = oldEnd - prefix;
        int columns = newEnd - prefix;

        // lcs[i][j] is the LCS length of the old lines from prefix + i and the new lines from prefix + j
        int[][] lcs = new int[rows + 1][columns + 1];

        for (int i = rows - 1; i >= 0; i--) {
            for (int j = columns - 1; j >= 0; j--) {
                lcs[i][j] = oldLines.get(prefix + i).equals(newLines.get(prefix + j)) ?
                        lcs[i + 1][j + 1] + 1 :
                        Math.max(lcs[i + 1][j], lcs[i][j + 1]);
            }
        }

        int i = 0, j = 0;
        while (i < rows && j < columns) {
            if (oldLines.get(prefix + i).equals(newLines.get(prefix + j))) {
                oldToNew[prefix + i] = prefix + j;
                i++;
                j++;
            } else if (lcs[i + 1][j] >= lcs[i][j + 1]) {
                i++;
            } else {
                j++;
            }
        }

        return new LineMap(oldToNew, newCount);
    }

    /**
     * Split a buffer into lines, without the line endings
     * @param bytes the buffer
     * @return the lines, empty for an empty buffer
     */
    static List<String> splitLines(byte[] bytes) {
        String text = new String(bytes, StandardCharsets.ISO_8859_1);

        List<String> lines = new ArrayList<>();

        if (text.isEmpty()) {
            return lines;
        }

        for (String line : text.split("\n", -1)) {
            lines.add(line.endsWith("\r") ? line.substring(0, line.length() - 1) : line);
        }

        if (text.endsWith("\n")) {
            lines.remove(lines.size() - 1);
        }

        return lines;
    }
}
