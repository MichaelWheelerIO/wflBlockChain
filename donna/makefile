# Define a variable for classpath
CLASS_PATH = ../bin

# Define a virtual path for .class in the bin directory
vpath %.class $(CLASS_PATH)

all : libed25519-donna.dylib

# $@ matches the target, $< matches the first dependancy
libed25519-donna.dylib : Ed25519DonnaJNI.o ed25519.o
	gcc -m64 -Wl, -shared -o $@ Ed25519DonnaJNI.o ed25519.o -L /usr/local/opt/openssl/lib -lcrypto
	
ed25519.o : ed25519.c
	gcc ed25519.c -m64 -O3 -c -D_REENTRANT -DED25519_TEST -I/usr/local/opt/openssl/include	

Ed25519DonnaJNI.o : Ed25519DonnaJNI.c com_setl_ed25519_Ed25519DonnaJNI.h
	gcc $< -m64 -O3 -c -D_REENTRANT -DED25519_TEST -I/usr/local/opt/openssl/include -I /library/Java/JavaVirtualMachines/jdk1.8.0_101.jdk/contents/home/include -I /library/Java/JavaVirtualMachines/jdk1.8.0_101.jdk/contents/home/include/darwin

clean :
	rm *.o *.dylib

# $* matches the target filename without the extension
com_setl_ed25519_Ed25519DonnaJNI.h : com/setl/ed25519/Ed25519DonnaJNI.class
	javah -classpath $(CLASS_PATH)  com.setl.ed25519.Ed25519DonnaJNI

# $@ matches the target, $< matches the first dependancy
#Ed25519DonnaJNI.o : test_donna.c Ed25519DonnaJNI.h
#	gcc -m64 -I /library/Java/JavaVirtualMachines/jdk1.8.0_101.jdk/contents/home/include -I /library/Java/JavaVirtualMachines/jdk1.8.0_101.jdk/contents/home/include/darwin -c $< -o $@
	