package org.roniyaakobi.diff;

import org.roniyaakobi.utils.BytesFormatter;
import org.roniyaakobi.utils.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public final class FileDiffCalculator {


    private enum LcsArrow {
        NONE,
        UP,
        LEFT,
        EITHER,
        CORNER
    }

    private record LcsEntry(LcsArrow arrow, int currLength){}

    private record ComparisonEntry(String diff, int lcsIndex) {}


    public static DiffCalculator.FileDiff calculate(
            byte[] oldBytes,
            byte[] newBytes,
            DiffCalculator.PathState pathState,
            Path path
    ) throws IOException {
        LcsEntry[][] memo = new LcsEntry[oldBytes.length][newBytes.length];

        Logger.log("Build the LCS memo: \n");
        buildLcsMemo(memo, oldBytes, newBytes);

        Logger.log("Backtracking the LCS memo: \n");
        byte[] LCS = backtrackLcs(memo, oldBytes, newBytes);

        List<ComparisonEntry> additions = compare(LCS, newBytes);
        List<ComparisonEntry> deletions = compare(LCS, oldBytes);

        List<DiffCalculator.DiffSection> additionDiffs = new ArrayList<>();

        for (ComparisonEntry addition : additions){
            additionDiffs.add(new DiffCalculator.DiffSection(
                    addition.diff(), addition.lcsIndex()
                )
            );
        }

        List<DiffCalculator.DiffSection> deletionDiffs = new ArrayList<>();

        for (ComparisonEntry deletion : deletions){
            deletionDiffs.add(new DiffCalculator.DiffSection(
                    deletion.diff(), deletion.lcsIndex()
                )
            );
        }

        return new DiffCalculator.FileDiff(
                path,
                LCS,
                additionDiffs,
                deletionDiffs,
                pathState
        );


    }

    private static void buildLcsMemo(LcsEntry[][] memo, byte[] oldBytes, byte[] newBytes){
        for (int y = 0; y < oldBytes.length; y++){
            for (int x = 0; x < newBytes.length; x++){
                int upLength = y > 0 ? memo[y-1][x].currLength() : 0;
                int leftLength = x > 0 ? memo[y][x-1].currLength() : 0;

                int difference = upLength - leftLength;
                int maxLength = Math.max(upLength, leftLength);

                LcsArrow arrow = LcsArrow.NONE;

                if (difference == 0 && upLength != 0 && leftLength != 0){
                    arrow = (oldBytes[y] == newBytes[x]) ? LcsArrow.CORNER : LcsArrow.EITHER;
                } else if (difference > 0){
                    arrow = LcsArrow.UP;
                } else if (x != 0 || y != 0){
                    arrow = LcsArrow.LEFT;
                }

                int length = (oldBytes[y] == newBytes[x]) ? maxLength + 1 : maxLength;

                memo[y][x] = new LcsEntry(arrow, length);
            }
        }
    }

    private static byte[] backtrackLcs(LcsEntry[][] memo, byte[] oldBytes, byte[] newBytes) {
        int i = oldBytes.length - 1, j = newBytes.length - 1;

        Stack<String> reverse = new Stack<>();

        LcsArrow arrow = memo[i][j].arrow();

        while (arrow != LcsArrow.NONE) {
            switch (arrow) {
                case UP -> i--;
                case LEFT, EITHER -> j--;
                case CORNER -> {
                    reverse.push(BytesFormatter.bytesToString(new byte[]{oldBytes[i]}));
                    i--;
                    j--;
                }
            }

            arrow = memo[i][j].arrow();
        }

        if (oldBytes[0] == newBytes[0]) {
            reverse.push(BytesFormatter.bytesToString(new byte[]{oldBytes[i]}));
        }

        String LCS = "";

        while (!reverse.isEmpty()){
            LCS += reverse.pop();
        }

        return LCS.getBytes();
    }

    private static List<ComparisonEntry> compare(byte[] LCS, byte[] compare){
        int compareIndex = 0, lcsIndex = 0;

        List<ComparisonEntry> comparisonList = new ArrayList<>();

        String comparison = "";
        int startComparisonLCSIndex = -1;

        while (compareIndex < compare.length){
            if (lcsIndex < LCS.length && LCS[lcsIndex] == compare[compareIndex]){
                if (!comparison.isEmpty()){
                    comparisonList.add(new ComparisonEntry(comparison, startComparisonLCSIndex));
                    startComparisonLCSIndex = -1;
                    comparison = "";
                }

                lcsIndex++;

            } else {

                comparison += BytesFormatter.byteToAscii(compare[compareIndex]);


                if (startComparisonLCSIndex == -1){
                    startComparisonLCSIndex = lcsIndex;
                }
            }
            compareIndex++;
        }

        if (!comparison.isEmpty()){
            comparisonList.add(new ComparisonEntry(comparison, LCS.length));
        }

        return comparisonList;
    }
}
