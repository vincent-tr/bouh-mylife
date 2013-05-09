#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <errno.h>
#include <fcntl.h>
#include <poll.h>
#include "PlatformCalls.h"

#define RETURN_ERROR_CHECK(value) { int __val = (value); return __val == -1 ? -errno : __val; }

/*
 * Class:     mylife_home_hw_driver_platform_PlatformCalls
 * Method:    open
 * Signature: (Ljava/lang/String;II)I
 */
JNIEXPORT jint JNICALL Java_mylife_home_hw_driver_platform_PlatformCalls_open
	(JNIEnv *env, jclass declaringClass, jstring pathname, jint flags, jint mode)
{
	// http://linux.die.net/man/2/open

	const char * path = (*env)->GetStringUTFChars(env, pathname, NULL);
	int fd = open(path, flags, mode);
	(*env)->ReleaseStringUTFChars(env, pathname, path);
	RETURN_ERROR_CHECK(fd);
}

/*
 * Class:     mylife_home_hw_driver_platform_PlatformCalls
 * Method:    close
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_mylife_home_hw_driver_platform_PlatformCalls_close
	(JNIEnv *env, jclass declaringClass, jint fd)
{
	// http://linux.die.net/man/2/close

	RETURN_ERROR_CHECK(close(fd));
}

/*
 * Class:     mylife_home_hw_driver_platform_PlatformCalls
 * Method:    read
 * Signature: (I[B)I
 */
JNIEXPORT jint JNICALL Java_mylife_home_hw_driver_platform_PlatformCalls_read
	(JNIEnv *env, jclass declaringClass, jint fd, jbyteArray buf)
{
	// http://linux.die.net/man/2/read

	size_t len = (*env)->GetArrayLength(env, buf);
	void *localBuffer = (*env)->GetByteArrayElements(env, buf, NULL);
	int ret = read(fd, localBuffer, len);
	(*env)->ReleaseByteArrayElements(env, buf, localBuffer, 0);
	RETURN_ERROR_CHECK(ret);
}

/*
 * Class:     mylife_home_hw_driver_platform_PlatformCalls
 * Method:    write
 * Signature: (I[B)I
 */
JNIEXPORT jint JNICALL Java_mylife_home_hw_driver_platform_PlatformCalls_write
	(JNIEnv *env, jclass declaringClass, jint fd, jbyteArray buf)
{
	// http://linux.die.net/man/2/write

	size_t len = (*env)->GetArrayLength(env, buf);
	void *localBuffer = (*env)->GetByteArrayElements(env, buf, NULL);
	int ret = write(fd, localBuffer, len);
	(*env)->ReleaseByteArrayElements(env, buf, localBuffer, 0);
	RETURN_ERROR_CHECK(ret);
}

/*
 * Class:     mylife_home_hw_driver_platform_PlatformCalls
 * Method:    poll
 * Signature: ([Lmylife/home/hw/driver/platform/PlatformCalls/pollfd;I)I
 */
JNIEXPORT jint JNICALL Java_mylife_home_hw_driver_platform_PlatformCalls_poll
	(JNIEnv *env, jclass declaringClass, jobjectArray fds, jint timeout)
{
	// http://linux.die.net/man/2/poll

	size_t count = (*env)->GetArrayLength(env, fds);
	struct pollfd *cfds = malloc(sizeof(*cfds) * count);

	jclass pollfdClass = (*env)->FindClass(env, "mylife/home/hw/driver/platform/PlatformCalls$pollfd");
	jfieldID fdField = (*env)->GetFieldID(env, pollfdClass, "fd", "I");
	jfieldID eventsField = (*env)->GetFieldID(env, pollfdClass, "events", "S");
	jfieldID reventsField = (*env)->GetFieldID(env, pollfdClass, "revents", "S");

	// lecture des structures
	for(int index=0; index<count; ++index)
	{
		jobject jitem = (*env)->GetObjectArrayElement(env, fds, index);
		struct pollfd *citem = cfds + index;

		citem->fd = (*env)->GetIntField(env, jitem, fdField);
		citem->events = (*env)->GetShortField(env, jitem, eventsField);
		citem->revents = 0;
	}

	int ret = poll(cfds, count, timeout);

	// écriture des structures
	for(int index=0; index<count; ++index)
	{
		jobject jitem = (*env)->GetObjectArrayElement(env, fds, index);
		struct pollfd *citem = cfds + index;

		(*env)->SetShortField(env, jitem, reventsField, citem->revents);
	}

	free(cfds);

	RETURN_ERROR_CHECK(ret);
}

/*
 * Class:     mylife_home_hw_driver_platform_PlatformCalls
 * Method:    lseek
 * Signature: (III)I
 */
JNIEXPORT jint JNICALL Java_mylife_home_hw_driver_platform_PlatformCalls_lseek
  (JNIEnv *env, jclass declaringClass, jint fd, jint offset, jint whence)
{
	RETURN_ERROR_CHECK(lseek(fd, offset, whence));
}

/*
 * Class:     mylife_home_hw_driver_platform_PlatformCalls
 * Method:    strerror
 * Signature: (I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_mylife_home_hw_driver_platform_PlatformCalls_strerror
	(JNIEnv *env, jclass declaringClass, jint errnum)
{
	// http://linux.die.net/man/3/strerror

	const char *str = strerror(errnum);
	if(str == NULL)
		return NULL;
	return (*env)->NewStringUTF(env, str);
}


