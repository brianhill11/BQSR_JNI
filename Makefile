CLASS_PATH = ./:$(HOME)/code/github/gatk/build/classes/main:$(HOME)/code/github/htsjdk/build/classes/main
CXXFLAGS += " -fPIC -std=c99"

vpath %.class $(CLASS_PATH)

all : libHello

# $@ matches the target, $< matches the first dependancy
# NOTE: may need to change suffix for different OS
# (ex. *.so for Linux, *.dll for Windows)
UNAME := $(shell uname)
ifeq ($(UNAME), Linux)
libHello : HelloJNI.o
	gcc -shared -o $@.so $<
endif 
ifeq ($(UNAME), Darwin)
libHello : HelloJNI.o
	gcc -shared -o $@.jnilib $<
endif

HelloJNI.o : HelloJNI.c HelloJNI.h
	gcc -fPIC -I/Library/Java/JavaVirtualMachines/jdk1.8.0_101.jdk/Contents/Home/include/darwin -I/Library/Java/JavaVirtualMachines/jdk1.8.0_101.jdk/Contents/Home/include -I/usr/lib/jvm/java-1.8.0-openjdk/include/ -I/usr/lib/jvm/java-1.8.0-openjdk/include/linux -c $< -o $@

HelloJNI.h : HelloJNI.class
	javah -classpath $(CLASS_PATH) $*

HelloJNI.class : HelloJNI.java TestClass.class
	javac -cp $(CLASS_PATH) HelloJNI.java

TestClass.class : TestClass.java TestInterface.class
	javac -cp $(CLASS_PATH) TestClass.java

TestInterface.class : TestInterface.java
	javac TestInterface.java

clean :
	rm -f HelloJNI.h *.o libHello.jnilib *.class *.log
