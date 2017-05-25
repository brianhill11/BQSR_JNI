#!/bin/bash

bam_url="http://hgdownload.cse.ucsc.edu/goldenPath/hg19/encodeDCC/wgEncodeUwRepliSeq"
small_bam="wgEncodeUwRepliSeqBg02esG1bAlnRep1.bam"
medium_bam="wgEncodeUwRepliSeqImr90S3AlnRep1.bam"
large_bam="wgEncodeUwRepliSeqSknshS3AlnRep1.bam"

# 92 MB Bam
if [ ! -f "$small_bam" ]; then
    wget "${bam_url}/${small_bam}"
    wget "${bam_url}/${small_bam}.bai"
fi

# 500 MB Bam 
if [ ! -f "$medium_bam" ]; then
    wget "${bam_url}/${medium_bam}"
    wget "${bam_url}/${medium_bam}.bai"
fi

# 900 MB Bam
if [ ! -f "$large_bam" ]; then
    wget "${bam_url}/${large_bam}"
    wget "${bam_url}/${large_bam}.bai"
fi
