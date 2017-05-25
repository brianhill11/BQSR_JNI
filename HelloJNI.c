#include <jni.h>
#include <stdio.h>
#include <stdbool.h>
#include "HelloJNI.h"

#include <unistd.h>
#include <fcntl.h>
#include <sys/types.h>
#include <sys/mman.h>
#include <errno.h>
#include <sys/stat.h>
#include <string.h>

//#define DEBUG1
//#define DEBUG2
//#define DEBUG3

#define BS32(x) __builtin_bswap32(x)
#define BS64(x) __builtin_bswap64(x)

JNIEXPORT jint JNICALL Java_HelloJNI_sayHello(JNIEnv *env, jobject thisObj, jobject test) {
    jclass testInterfaceClass = (*env)->GetObjectClass(env, test);
    jmethodID getStart = (*env)->GetMethodID(env, testInterfaceClass, "getStart", "()I");
    int start = (*env)->CallIntMethod(env, test, getStart);
    //printf("Int value: %d\n", start);
    return start;
}

JNIEXPORT jint JNICALL Java_HelloJNI_passInt(JNIEnv *env, jobject thisObj, jint i) {
    int native_int = i;
    printf("native int value: %d\n", i);
    return native_int;
}

/*
 * Class:     HelloJNI
 * Method:    processRead
 * Signature: (I[CII[CZZZI)I
 */
JNIEXPORT jint JNICALL Java_HelloJNI_processRead
        (JNIEnv *env, jobject thisObj, jint length, jcharArray contig,
         jint start, jint end, jboolean isReverseStrand,
         jboolean isPaired, jboolean isSecondInPair, jint baseQualityCount) {
    int i;

    // get size of char array
    jsize contig_len = (*env)->GetArrayLength(env, contig);
    // get pointer to char array
    jchar *contig_arr = (*env)->GetCharArrayElements(env, contig, 0);

    for (i = 0; i < contig_len; i++) {
#ifdef DEBUG1
        printf("%c", contig_arr[i]);
#endif
    }
#ifdef DEBUG1
    printf("\n");
    printf("read length: %d\n", length);
    printf("start: %d\tend: %d\n", start, end);
#endif
    if (!isReverseStrand) {
        //printf("read not reverse strand\n");
    }
    // clean up
    (*env)->ReleaseCharArrayElements(env, contig, contig_arr, 0);
    return 0;
}


/*
 * Class:     HelloJNI
 * Method:    processRawRead
 * Signature: (Lorg/broadinstitute/hellbender/utils/read/GATKRead;)I
 */
JNIEXPORT jint JNICALL Java_HelloJNI_processRawRead
        (JNIEnv *env, jobject thisObj, jobject read) {
    jclass readClass = (*env)->GetObjectClass(env, read);
    jmethodID getLength = (*env)->GetMethodID(env, readClass, "getLength", "()I");
    int length = (*env)->CallIntMethod(env, read, getLength);

    jmethodID getStart = (*env)->GetMethodID(env, readClass, "getStart", "()I");
    int start = (*env)->CallIntMethod(env, read, getStart);
    jmethodID getEnd = (*env)->GetMethodID(env, readClass, "getEnd", "()I");
    int end = (*env)->CallIntMethod(env, read, getEnd);

    jmethodID isReverseStrand = (*env)->GetMethodID(env, readClass, "isReverseStrand", "()Z");
    bool reverseStrand = (*env)->CallBooleanMethod(env, read, isReverseStrand);


    //jmethodID getAssignedContig = (*env)->GetMethodID(env, readClass, "getAssignedContig", "()Ljava/lang/String");
    //jstring contig = (*env)->CallObjectMethod(env, read, getAssignedContig);
    //const char *contig_string = (*env)->GetStringUTFChars(env, contig, 0);


#ifdef DEBUG2
   //printf("%s", contig_string);
    printf("\n");
    printf("read length: %d\n", length);
    printf("start: %d\tend: %d\n", start, end);
#endif
    if (!reverseStrand) {
        //printf("read not reverse strand\n");
    }
    return 0;
}

/*
 * Class:     HelloJNI
 * Method:    processReadBuffer
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_HelloJNI_processReadBuffer
        (JNIEnv *env, jobject thisObj) {

    int file_desc;
    int i;
    int page_size;
    char *shared_buff;
    struct stat sbuf;
    // get size of page
    page_size = getpagesize();

    // open shared file in READ_ONLY state
    if ((file_desc = open("/tmp/shared_mem.file", O_RDONLY)) == -1) {
        perror("open");
        return 1;
    }

    if (stat("/tmp/shared_mem.file", &sbuf) == -1) {
        perror("stat");
        return 3;
    }
#ifdef DEBUG3
    printf("Page size: %d\n", page_size);
    printf("file size: %lld\n", sbuf.st_size);
#endif
    // use mmap system call to get address of shared memory
    // parameters are: mmap(addr, len, protection, flags, filedes, offset)
    // using (caddr_t)0 for address lets OS choose for us
    shared_buff = mmap((caddr_t)0, sbuf.st_size, PROT_READ, MAP_SHARED, file_desc, 0);

    // make sure mmap returned correctly
    if (shared_buff == (void*)(-1)) {
        perror("mmap");
        return 2;
    }

    int32_t *int_buff = (int32_t*)shared_buff;
#ifdef DEBUG3
    printf("RL: %d\n\n", BS32( *int_buff) );
    printf("## %x\t%x\t%x\t%x\t%x\n", shared_buff[0], shared_buff[1], shared_buff[2], shared_buff[3], shared_buff[4]);
#endif
    struct GATKRead {
        int length;
        int start;
        int end;
        int base_quality_count;

        int is_reverse_strand;
        int is_paired;
        int is_second_of_pair;
        int contig_length;
        char contig;
    };

    // position 0 in file holds # of reads as int
    int num_reads = BS32(int_buff[0]);

#ifdef DEBUG3
    printf("size of GATKRead: %lu\n", sizeof(struct GATKRead));
    printf("num reads: %d\n", num_reads);
#endif
    shared_buff = shared_buff + sizeof(int);
    for (i = 0; i < num_reads; i++) {
        // get int pointer which points to same address as shared_buff
        struct GATKRead *read = (struct GATKRead *) shared_buff;
#ifdef DEBUG3
        printf("read length: %d\nread_start: %d\nread_end: %d\n", BS32(read->length), BS32(read->start), BS32(read->end));
        printf("read BQ count: %d\nis reverse strand? %d\nis paired? %d\n", BS32(read->base_quality_count), BS32(read->is_reverse_strand), BS32(read->is_paired));
        printf("contig length: %d\n", BS32(read->contig_length));

        char contig[BS32(read->contig_length)];
        memcpy(contig, &read->contig, BS32(read->contig_length));
        printf("%s\n", contig);
#endif
        shared_buff = shared_buff + sizeof(struct GATKRead) + sizeof(char)*(BS32(read->contig_length) -4);
    }
    // clean up mmapped memory
    munmap(shared_buff, page_size);
    // close file
    close(file_desc);
    return 0;
}
