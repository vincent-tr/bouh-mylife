# gcc -c -I"/usr/lib/jvm/java-7-openjdk-armhf/include" -I"/usr/lib/jvm/java-7-openjdk-armhf/include/linux" PlatformCalls.c
# gcc -I"/usr/lib/jvm/java-7-openjdk-armhf/include" -I"/usr/lib/jvm/java-7-openjdk-armhf/include/linux" -o PlatformCalls.so -shared PlatformCalls.o -lc


PlatformCalls.so : PlatformCalls.o
	gcc -I"/usr/lib/jvm/java-7-openjdk-armhf/include" -I"/usr/lib/jvm/java-7-openjdk-armhf/include/linux" -o PlatformCalls.so -shared PlatformCalls.o -lc
	
PlatformCalls.o : PlatformCalls.c
	gcc -c -std=c99 -I"/usr/lib/jvm/java-7-openjdk-armhf/include" -I"/usr/lib/jvm/java-7-openjdk-armhf/include/linux" PlatformCalls.c 
