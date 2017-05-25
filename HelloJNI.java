import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMRecord;
import org.broadinstitute.hellbender.utils.read.GATKRead;
import org.broadinstitute.hellbender.engine.ReadsDataSource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class HelloJNI {
    static {
        System.loadLibrary("Hello"); // hello.dll (Windows) or libhello.so (Unixes)
    }
        
          // Declare native method
    private native int sayHello(TestInterface t);
    private native int passInt(int i);

    private native int processRead(int length, char[] contig, int start,
                                   int end, boolean isReverseStrand,
                                   boolean isPaired, boolean isSecondInPair,
                                   int baseQualityCount);
    private native int processRawRead(GATKRead read);
    private native int processReadBuffer();
    //private native int processRawRead(SAMRecord read);
          //     
          //        // Test Driver
          //           
    public static void main(String[] args) {
        String bam_file = "";
        if (args.length > 0) {
            bam_file = args[0];
        } else {
            System.err.println("Need BAM file parameter");
            System.exit(1);
        }

        final int num_iterations = 10;

        HelloJNI hello = new HelloJNI();

        TestClass tc = new TestClass();

        // 96MB BAM
        //Path bamPath = Paths.get("wgEncodeUwRepliSeqBg02esG1bAlnRep1.bam");
        // 500MB BAM
        Path bamPath = Paths.get(bam_file);
        ReadsDataSource ds = new ReadsDataSource(bamPath);

        /*
            Extract member variables from GATKRead object, pass to native method
         */
        long test1_sum = 0;
        for (int i = 0; i < num_iterations; i++) {
            Iterator<GATKRead> iter = ds.iterator();
            final long startTime1 = System.currentTimeMillis();
            while (iter.hasNext()) {
                GATKRead read = iter.next();

                final int length = read.getLength();
                final char[] contig = read.getContig().toCharArray();
                final int start = read.getStart();
                final int end = read.getEnd();
                //System.out.println("RG:" + read.getReadGroup());
                //final char[] readGroup = read.getReadGroup().toCharArray();
                final boolean isReverseStrand = read.isReverseStrand();
                final boolean isPaired = read.isPaired();
                final boolean isSecondInPair = read.isSecondOfPair();
                final int baseQualityCount = read.getBaseQualityCount();
                hello.processRead(length, contig, start, end, isReverseStrand,
                        isPaired, isSecondInPair, baseQualityCount);
                //System.out.println(read);
            }

            final long endTime1 = System.currentTimeMillis();
            System.out.println("Test 1 - iteration " + i + " execution time: " + (endTime1 - startTime1) + " ms");
            test1_sum = test1_sum + (endTime1 - startTime1);
        }
        System.out.println("Average time Test 1: " + test1_sum / num_iterations + " ms");
        //hello.sayHello(tc);  // invoke the native method


        /*
            Pass GATKRead object to C using native method
         */
        long test2_sum = 0;
        for (int i = 0; i < num_iterations; i++) {
            Iterator<GATKRead> iter2 = ds.iterator();
            final long startTime2 = System.currentTimeMillis();
            while (iter2.hasNext()) {
                GATKRead read = iter2.next();
                //SAMRecord sr = iter2.next().convertToSAMRecord(bam_header);
                //System.out.println(sr);
                //hello.processRawRead(sr);
                hello.processRawRead(read);
            }

            //hello.passInt(test_int);  // invoke the native method
            final long endTime2 = System.currentTimeMillis();
            System.out.println("Test 2 - iteration " + i + " execution time: " + (endTime2 - startTime2) + " ms");
            test2_sum = test2_sum + (endTime2 - startTime2);
        }
        System.out.println("Average time Test 2: " + test2_sum / num_iterations + " ms");
        /*
            Use memory-mapped file to share data between Java/C
         */
        final String shared_mem_file = "/tmp/shared_mem.file";
        final long buff_size = 4096 * 25600 *10;

        File mem_mapped_file = new File(shared_mem_file);

        long test3_sum = 0;
        for (int i = 0; i < num_iterations; i++) {
            try {
                FileChannel mem_mapped_file_channel = new RandomAccessFile(mem_mapped_file, "rw").getChannel();
                try {
                    MappedByteBuffer shared_buff = mem_mapped_file_channel.map(FileChannel.MapMode.READ_WRITE, 0, buff_size);

                    int read_count = 0;
                    shared_buff.putInt(read_count);
                    Iterator<GATKRead> iter = ds.iterator();
                    final long startTime3 = System.currentTimeMillis();
                    while (iter.hasNext()) {
                        GATKRead read = iter.next();

                        // reset buffer postion back to zero
                        //shared_buff.position(0);

                        //System.out.println("read length: " + read.getLength());
                        //System.out.println("contig:" + read.getContig());

                        // NOTE: this order must be perserved when reading in C code
                        shared_buff.putInt(read.getLength());
                        shared_buff.putInt(read.getStart());
                        shared_buff.putInt(read.getEnd());
                        shared_buff.putInt(read.getBaseQualityCount());
                        // These should get written as single bytes?
                        shared_buff.putInt(read.isReverseStrand() ? 1 : 0);
                        shared_buff.putInt(read.isPaired() ? 1 : 0);
                        shared_buff.putInt(read.isSecondOfPair() ? 1 : 0);
                        // writing string as byte string
                        // do we need to add length value before this?

                        shared_buff.putInt(read.getContig().getBytes().length);
                        shared_buff.put(read.getContig().getBytes());

                        //System.out.println("RG:" + read.getReadGroup());
                        //final char[] readGroup = read.getReadGroup().toCharArray();

                        //hello.processReadhexBuffer();
                        //System.out.println(read);
                        read_count = read_count + 1;
                    }
                    shared_buff.putInt(0, read_count);
                    hello.processReadBuffer();
                    final long endTime3 = System.currentTimeMillis();
                    System.out.println("Test 3 - iteration " + i + " execution time: " + (endTime3 - startTime3) + " ms");
                    test3_sum = test3_sum + (endTime3 - startTime3);
                } catch (IOException ex) {
                System.err.println(ex);
                }
            } catch (FileNotFoundException ex) {
                System.err.println(ex);
            }
        }
        System.out.println("Average time Test 3: " + test3_sum / num_iterations + " ms");

    }
}
