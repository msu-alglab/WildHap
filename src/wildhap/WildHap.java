package wildhap;

import java.util.*;
import java.util.stream.IntStream;
import java.util.concurrent.atomic.*;
import java.util.concurrent.*;
import java.io.*;

public class WildHap {

    static BitSet set[][];
    static int numRows;
    static AtomicLong numblocks, numDFScalls, totalKsize, totalSNPsize;
    static ConcurrentSkipListMap<Integer, ConcurrentSkipListSet<Integer>> shape;
    static int minBlockArea;

    public static int DFS(int i, BitSet rows, int j, int[] consCnt) {
        boolean ok;
        numDFScalls.incrementAndGet();
        int branchs = 0;
        for (int b = 0; b <= 1; b++) {
            BitSet kp = (BitSet) set[i][b].clone();
            kp.and(rows);
            consCnt[i] = kp.cardinality();
            kp.or(set[i][2]); // 2 = *
            kp.and(rows);
            ok = (consCnt[i] > 0);
            if (ok) {
                branchs++;
            }
            ok = ok && kp.cardinality() > 1;
            ok = ok && ((long) j + 1) * (long) kp.cardinality() >= (long) minBlockArea;
            if (ok && j < set.length - 1) { // check right maximal
                int has0 = 0, has1 = 0;
                for (int r = kp.nextSetBit(0); r >= 0; r = kp.nextSetBit(r + 1)) {
                    // operate on index r here
                    if (set[j + 1][0].get(r)) {
                        has0 = 1;
                        if (has1 == 1) {
                            break;
                        }
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
            if (ok) {
                BitSet rm = (BitSet) set[i][1 - b].clone();
                rm.and(rows);
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
                if (ok && (i == 0 || DFS(i - 1, kp, j, consCnt) != 1)) { // left maximal
                    if ((kp.cardinality() * (j - i + 1)) >= minBlockArea) {
                        //System.out.println(kp.cardinality() + "," + (j - i + 1));
                        shape.putIfAbsent(kp.cardinality(), new ConcurrentSkipListSet<Integer>());
                        shape.get(kp.cardinality()).add(j - i + 1);
                        numblocks.incrementAndGet();
                        totalKsize.addAndGet(kp.cardinality());
                        totalSNPsize.addAndGet(j - i + 1);
                    }
                }
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
        }
        return branchs;
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: WildHap file *prob minblockarea <maxRows maxSNPs>");
            System.exit(0);
        }
        String fileName = args[0];
        double prob = Double.parseDouble(args[1]);
        minBlockArea = Integer.parseInt(args[2]);
        System.out.println("filename: " + fileName);
        System.out.println("wildcard prob: " + prob);
        System.out.println("mindblockarea: " + minBlockArea);
        int maxRows = Integer.MAX_VALUE;
        int maxSNPs = Integer.MAX_VALUE;
        if (args.length == 5) {
            maxRows = Integer.parseInt(args[3]);
            maxSNPs = Integer.parseInt(args[4]);
            System.out.println("maxRows: " + maxRows);
            System.out.println("maxSNPs: " + maxSNPs);
        }
        numRows = 0;
        File f = new File(fileName);
        try {
            FileInputStream inputStream = new FileInputStream(f);
            Scanner sc = new Scanner(inputStream, "UTF-8");

            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                if (set == null) {
                    set = new BitSet[Math.min(maxSNPs, line.length())][3];
                }
                for (int j = 0; j < Math.min(maxSNPs, line.length()); j++) {
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
                if (numRows == maxRows) {
                    break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("finished reading data");
        long startTime = System.nanoTime(); // start meansuring time after I/O
        numDFScalls = new AtomicLong(0);
        numblocks = new AtomicLong(0);
        totalKsize = new AtomicLong(0);
        totalSNPsize = new AtomicLong(0);
        shape = new ConcurrentSkipListMap<Integer, ConcurrentSkipListSet<Integer>>();

        BitSet allRows = new BitSet(numRows);
        allRows.set(0, numRows);
        AtomicInteger numCols = new AtomicInteger(0);
        IntStream.range(0, set.length).parallel().forEach((j) -> {
            DFS(j, allRows, j, new int[j + 1]);
            int x = numCols.incrementAndGet();
            if (x % 10000 == 0) {
                System.out.println("finished col: " + x + ", # of dfs calls: " + numDFScalls.get() + ", # of blocks: " + numblocks.get());
            }
        });

        long estimatedTime = System.nanoTime() - startTime;
        long seconds = estimatedTime / 1000000000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        double avgKsize = (double) totalKsize.get() / numblocks.get();
        double avgSNPsize = (double) totalSNPsize.get() / numblocks.get();
        String[] names = fileName.split("/");
        String shortFileName = names[names.length - 1];
        try {
            BufferedWriter infoOut = new BufferedWriter(new FileWriter(shortFileName + ".info-" + prob + "-" + minBlockArea + ".txt"));
            infoOut.write("elapsed time: " + String.format("%d min %d sec", minutes, seconds) + '\n');
            infoOut.write("# of row: " + numRows + '\n');
            infoOut.write("# of SNPs: " + set.length + '\n');
            infoOut.write("minblockarea: " + minBlockArea + '\n');
            infoOut.write("# of dfs calls: " + numDFScalls.get() + '\n');
            infoOut.write("# of blocks: " + numblocks.get() + '\n');
            infoOut.write("avg |K|: " + String.format("%.2f", avgKsize) + '\n');
            infoOut.write("avg # of block SNPs: " + String.format("%.2f", avgSNPsize) + '\n');
            infoOut.close();
            BufferedWriter distOut = new BufferedWriter(new FileWriter(shortFileName + ".dist-" + prob + "-" + minBlockArea + ".txt"));
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
