# Wildcard Haplotype Blocks

### Problem Definition

Given a file of _k_ sequences with _n_ SNP calls each
(can be 0, 1, or * if the value is unknown), find all
maximal wildcard haplotype blocks.

### Input

WildHap takes five arguments, two of which are optional.
* input filename
* probability of replacing a SNP call with a *
* minimum block area for reported blocks (width x height)
* maximum rows to process (optional)
* maximum SNPs (columns) to process (optional)
