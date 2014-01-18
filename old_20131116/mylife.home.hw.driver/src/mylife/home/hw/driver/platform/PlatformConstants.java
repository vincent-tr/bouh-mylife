package mylife.home.hw.driver.platform;

public final class PlatformConstants {

	/*
	 * Event types that can be polled for. These bits may be set in `events' to
	 * indicate the interesting event types; they will appear in `revents' to
	 * indicate the status of the file descriptor.
	 */
	public static final short POLLIN = 0x001; /* There is data to read. */
	public static final short POLLPRI = 0x002; /* There is urgent data to read. */
	public static final short POLLOUT = 0x004; /* Writing now will not block. */

	/*
	 * Event types always implicitly polled for. These bits need not be set in
	 * `events', but they will appear in `revents' to indicate the status of the
	 * file descriptor.
	 */
	public static final short POLLERR = 0x008; /* Error condition. */
	public static final short POLLHUP = 0x010; /* Hung up. */
	public static final short POLLNVAL = 0x020; /* Invalid polling request. */

	private static final int __O_SYNC = 04000000;

	public static final int O_ACCMODE = 00000003;
	public static final int O_RDONLY = 00000000;
	public static final int O_WRONLY = 00000001;
	public static final int O_RDWR = 00000002;
	public static final int O_CREAT = 00000100; /* not fcntl */
	public static final int O_EXCL = 00000200; /* not fcntl */
	public static final int O_NOCTTY = 00000400; /* not fcntl */
	public static final int O_TRUNC = 00001000; /* not fcntl */
	public static final int O_APPEND = 00002000;
	public static final int O_NONBLOCK = 00004000;
	public static final int O_DSYNC = 00010000; /* used to be O_SYNC, see below */
	public static final int O_DIRECT = 00040000; /* direct disk access hint */
	public static final int O_LARGEFILE = 00100000;
	public static final int O_DIRECTORY = 00200000; /* must be a directory */
	public static final int O_NOFOLLOW = 00400000; /* don't follow links */
	public static final int O_NOATIME = 01000000;
	public static final int O_CLOEXEC = 02000000; /* set close_on_exec */
	public static final int O_SYNC = (__O_SYNC | O_DSYNC);
	public static final int O_PATH = 010000000;
	public static final int O_NDELAY = O_NONBLOCK;

	public static final int S_IRWXU = 00700;
	public static final int S_IRUSR = 00400;
	public static final int S_IWUSR = 00200;
	public static final int S_IXUSR = 00100;
	public static final int S_IRWXG = 00070;
	public static final int S_IRGRP = 00040;
	public static final int S_IWGRP = 00020;
	public static final int S_IXGRP = 00010;
	public static final int S_IRWXO = 00007;
	public static final int S_IROTH = 00004;
	public static final int S_IWOTH = 00002;
	public static final int S_IXOTH = 00001;

	public static final int SEEK_SET       = 0;       /* Seek from beginning of file.  */
	public static final int SEEK_CUR       = 1;       /* Seek from current position.  */
	public static final int SEEK_END       = 2;       /* Seek from end of file.  */
}
