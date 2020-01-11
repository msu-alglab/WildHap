package wildhap;

import java.util.BitSet;

public class WildHap {

    static char seq[][];
    static BitSet set[][];
    static int j;
    static int consCnt[];
    static long numblocks;
    static long numDFScalls;

    public static int DFS(int i, BitSet rows) {
        numDFScalls++;
        int branchs = 0;
        for (int b = 0; b <= 1; b++) {
            BitSet kp = (BitSet) set[i][b].clone();
            kp.and(rows);
            consCnt[i] = kp.cardinality();
            kp.or(set[i][2]); // 2 = *
            kp.and(rows);
            BitSet rm = (BitSet) set[i][1 - b].clone();
            rm.and(rows);
            boolean ok = (consCnt[i] > 0);
            if (ok) {
                branchs++;
            }
            for (int r = rm.nextSetBit(0); r >= 0; r = rm.nextSetBit(r + 1)) {
                // operate on index r here
                for (int c = i + 1; c <= j; c++) {
                    if (seq[r][c] != '*') {
                        consCnt[c]--;
                        if (consCnt[c] <= 0) {
                            ok = false;
                        }
                    }
                }
                if (r == Integer.MAX_VALUE) {
                    break; // or (r+1) would overflow
                }
            }

            if (ok && j < seq[0].length - 1) {
                int has0 = 0, has1 = 0;
                for (int r = kp.nextSetBit(0); r >= 0; r = kp.nextSetBit(r + 1)) {
                    // operate on index r here
                    if (seq[r][j + 1] == '0') {
                        has0 = 1;
                    }
                    if (seq[r][j + 1] == '1') {
                        has1 = 1;
                        if (has0 == 1) {
                            break;
                        }
                    }
                    if (r == Integer.MAX_VALUE) {
                        break; // or (r+1) would overflow
                    }
                }
                if (has0 + has1 == 1) {
                    ok = false; // not right maximal
                }
            }
            if (ok && kp.cardinality() > 1 && (i == 0 || DFS(i - 1, kp) != 1)) { // left maximal
                //System.out.println("block found: " + kp + " [" + i + "," + j + "]");
                numblocks++;
            }
            //consCnt[i] = 0;
            for (int r = rm.nextSetBit(0); r >= 0; r = rm.nextSetBit(r + 1)) {
                // operate on index r here
                for (int c = i + 1; c <= j; c++) {
                    if (seq[r][c] != '*') {
                        consCnt[c]++;
                    }
                }
                if (r == Integer.MAX_VALUE) {
                    break; // or (r+1) would overflow
                }
            }
        }
        return branchs;
    }

    public static void main(String[] args) {

        seq = new char[][]{
            {'*', '*', '0'},
            {'*', '*', '1'},
            {'*', '0', '*'},
            {'*', '1', '*'},
            {'0', '*', '*'},
            {'1', '*', '*'}};
        /*
        int n = 1000;
        int m = 10000;
        double w = 0.1;
        seq = new char[n][m];
        for (int r = 0; r < n; r++) {
            for (int j = 0; j < m; j++) {
                if (Math.random() < 0.5) {
                    seq[r][j] = '0';
                } else {
                    seq[r][j] = '1';
                }
                if (Math.random() < w) {
                    seq[r][j] = '*';
                }
            }
        }
         */

        consCnt = new int[seq[0].length];
        set = new BitSet[seq[0].length][3];
        for (int j = 0; j < seq[0].length; j++) {
            set[j][0] = new BitSet(seq.length);
            set[j][1] = new BitSet(seq.length);
            set[j][2] = new BitSet(seq.length);
            for (int r = 0; r < seq.length; r++) {
                if (seq[r][j] == '0') {
                    set[j][0].set(r);
                }
                if (seq[r][j] == '1') {
                    set[j][1].set(r);
                }
                if (seq[r][j] == '*') {
                    set[j][2].set(r);
                }
            }
        }
        BitSet allRows = new BitSet(seq.length);
        allRows.set(0, seq.length);
        for (j = 0; j < seq[0].length; j++) {
            DFS(j, allRows);
            System.out.println("finished col: " + j + ", # of dfs calls: " + numDFScalls + ", # of blocks: " + numblocks);
        }

    }
}
