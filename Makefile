CLASS_PATH = ./:/Users/bhizzle/code/github/gatk/build/classes/main:/Users/bhizzle/code/github/htsjdk/build/classes/main

vpath %.class $(CLASS_PATH)

all : libHello.jnilib

# $@ matches the target, $< matches the first dependancy
# NOTE: may need to change suffix for different OS
# (ex. *.so for Linux, *.dll for Windows)
libHello.jnilib : HelloJNI.o
	gcc -shared -o $@ $<

HelloJNI.o : HelloJNI.c HelloJNI.h
	gcc -I/Library/Java/JavaVirtualMachines/jdk1.8.0_101.jdk/Contents/Home/include/darwin -I/Library/Java/JavaVirtualMachines/jdk1.8.0_101.jdk/Contents/Home/include -c $< -o $@

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
