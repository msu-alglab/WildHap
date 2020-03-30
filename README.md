# Wildcard Haplotype Blocks

### Problem Definition

Given a file of _k_ sequences with _n_ SNP calls each
(can be 0, 1, or * if the value is unknown), find all
maximal wildcard haplotype blocks.

This implementation was built to test the algorithm described in "Maxmimal
Perfect Haplotype Blocks with Wildcards," to be published in a Special Issue of
iScience covering [RECOMB-Seq
2020](https://www.recomb2020.org/recomb-seq-satellites),
over an input data set with varying
proportions of SNP calls unknown. It can be built using
[Netbeans](https://netbeans.org/).

### Input

WildHap takes five arguments, two of which are optional.
* input filename
* probability of replacing a SNP call with a *
* minimum block area for reported blocks (width x height)
* maximum number of rows to process (optional)
* maximum number of SNPs (columns) to process (optional)

### Output

WildHap produces two output files: an info file with a summary of the blocks
found in the input, and a dist file that lists the unique block shapes (height
x width) of the blocks.

### Example

The file fig1.txt is provided. If run with the following arguments

```
fig1.txt 0.0 1
```

(input file fig1.txt, 0 probability of replacing a SNP call with a *,
and a minimum block area of 1), WildHap produces a file fig1.txt.info-0.0-1.txt
with the following contents.

```
# of row: 6
# of SNPs: 3
minblockarea: 1
# of dfs calls: 11
# of blocks: 22
avg |K|: 3.91
avg # of block SNPs: 2.09
```

Additionally, WildHap produces file fig1.txt.dist-0.0-1.txt.
