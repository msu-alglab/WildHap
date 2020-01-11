package wildhap;

import java.util.*;
import java.io.*;

public class WildHap {

    static BitSet set[][];
    static int j, numRows;
    static int consCnt[];
    static long numblocks;
    static long numDFScalls;
    static TreeMap<Integer, TreeSet<Integer>> shape;

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
                    if (!set[c][2].get(r)) {
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
            if (ok && j < set.length - 1) {
                int has0 = 0, has1 = 0;
                for (int r = kp.nextSetBit(0); r >= 0; r = kp.nextSetBit(r + 1)) {
                    // operate on index r here
                    if (set[j + 1][0].get(r)) {
                        has0 = 1;
                    }
                    if (set[j + 1][1].get(r)) {
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
                //System.out.println(kp.cardinality() + "," + (j - i + 1));
                shape.putIfAbsent(kp.cardinality(), new TreeSet<Integer>());
                shape.get(kp.cardinality()).add(j - i + 1);
                numblocks++;
            }
            //consCnt[i] = 0;
            for (int r = rm.nextSetBit(0); r >= 0; r = rm.nextSetBit(r + 1)) {
                // operate on index r here
                for (int c = i + 1; c <= j; c++) {
                    if (!set[c][2].get(r)) {
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
        if (args.length != 2) {
            System.out.println("Usage: WildHap <file> <*prob>");
            System.exit(0);
        }
        String fileName = args[0];
        double prob = Double.parseDouble(args[1]);
        System.out.println("filename: " + fileName);
        System.out.println("* prob: " + prob);
        numRows = 0;
        File f = new File(fileName);
        try {
            FileInputStream inputStream = new FileInputStream(f);
            Scanner sc = new Scanner(inputStream, "UTF-8");

            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                if (set == null) {
                    set = new BitSet[line.length()][3];
                }
                for (int j = 0; j < line.length(); j++) {
                    char c = line.charAt(j);
                    if (Math.random() < prob) {
                        c = '*';
                    }
                    if (set[j][0] == null) {
                        set[j][0] = new BitSet();
                        set[j][1] = new BitSet();
                        set[j][2] = new BitSet();
                    }
                    if (c == '0') {
                        set[j][0].set(numRows);
                    }
                    if (c == '1') {
                        set[j][1].set(numRows);
                    }
                    if (c == '*') {
                        set[j][2].set(numRows);
                    }
                }
                numRows++;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        consCnt = new int[set.length];
        shape = new TreeMap<Integer, TreeSet<Integer>>();
        BitSet allRows = new BitSet(numRows);
        allRows.set(0, numRows);
        for (j = 0; j < set.length; j++) {
            DFS(j, allRows);
            if (j % 1000 == 0) {
                System.out.println("finished col: " + j + ", # of dfs calls: " + numDFScalls + ", # of blocks: " + numblocks);
            }
        }
        try {
            BufferedWriter infoOut = new BufferedWriter(new FileWriter(fileName + ".info-" + prob + ".txt"));
            infoOut.write("# of row: " + numRows + '\n');
            infoOut.write("# of SNPs: " + set.length + '\n');
            infoOut.write("# of dfs calls: " + numDFScalls + '\n');
            infoOut.write("# of blocks: " + numblocks + '\n');
            infoOut.close();
            BufferedWriter distOut = new BufferedWriter(new FileWriter(fileName + ".dist-" + prob + ".txt"));
            for (Integer Ksize : shape.keySet()) {
                for (Integer Length : shape.get(Ksize)) {
                    distOut.write(Ksize + "," + Length + '\n');
                }
            }
            distOut.close();
        } catch (Exception ex) {
        }
    }
}
