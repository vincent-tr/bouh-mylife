package mylife.home.hw.driver.platform;

import java.util.Hashtable;
import java.util.Map;

public enum PlatformError {

	EPERM(1, "EPERM", "Operation not permitted"),
	ENOENT(2, "ENOENT", "No such file or directory"),
	ESRCH(3, "ESRCH", "No such process"),
	EINTR(4, "EINTR", "Interrupted system call"),
	EIO(5, "EIO", "I/O error"),
	ENXIO(6, "ENXIO", "No such device or address"),
	E2BIG(7, "E2BIG", "Argument list too long"),
	ENOEXEC(8, "ENOEXEC", "Exec format error"),
	ECHILD(10, "ECHILD", "No child processes"),
	EAGAIN(11, "EAGAIN", "Try again"),
	ENOMEM(12, "ENOMEM", "Out of memory"),
	EACCES(13, "EACCES", "Permission denied"),
	EFAULT(14, "EFAULT", "Bad address"),
	ENOTBLK(15, "ENOTBLK", "Block device required"),
	EBUSY(16, "EBUSY", "Device or resource busy"),
	EEXIST(17, "EEXIST", "File exists"),
	EXDEV(18, "EXDEV", "Cross-device link"),
	ENODEV(19, "ENODEV", "No such device"),
	ENOTDIR(20, "ENOTDIR", "Not a directory"),
	EISDIR(21, "EISDIR", "Is a directory"),
	EINVAL(22, "EINVAL", "Invalid argument"),
	ENFILE(23, "ENFILE", "File table overflow"),
	EMFILE(24, "EMFILE", "Too many open files"),
	ENOTTY(25, "ENOTTY", "Not a typewriter"),
	ETXTBSY(26, "ETXTBSY", "Text file busy"),
	EFBIG(27, "EFBIG", "File too large"),
	ENOSPC(28, "ENOSPC", "No space left on device"),
	ESPIPE(29, "ESPIPE", "Illegal seek"),
	EROFS(30, "EROFS", "Read-only file system"),
	EMLINK(31, "EMLINK", "Too many links"),
	EPIPE(32, "EPIPE", "Broken pipe"),
	EDOM(33, "EDOM", "Math argument out of domain of func"),
	ERANGE(34, "ERANGE", "Math result not representable"),
	EDEADLK(35, "EDEADLK", "Resource deadlock would occur"),
	ENAMETOOLONG(36, "ENAMETOOLONG", "File name too long"),
	ENOLCK(37, "ENOLCK", "No record locks available"),
	ENOSYS(38, "ENOSYS", "Function not implemented"),
	ENOTEMPTY(39, "ENOTEMPTY", "Directory not empty"),
	ELOOP(40, "ELOOP", "Too many symbolic links encountered"),
	 // EWOULDBLOCK = EAGAIN
	ENOMSG(42, "ENOMSG", "No message of desired type"),
	EIDRM(43, "EIDRM", "Identifier removed"),
	ECHRNG(44, "ECHRNG", "Channel number out of range"),
	EL2NSYNC(45, "EL2NSYNC", "Level 2 not synchronized"),
	EL3HLT(46, "EL3HLT", "Level 3 halted"),
	EL3RST(47, "EL3RST", "Level 3 reset"),
	ELNRNG(48, "ELNRNG", "Link number out of range"),
	EUNATCH(49, "EUNATCH", "Protocol driver not attached"),
	ENOCSI(50, "ENOCSI", "No CSI structure available"),
	EL2HLT(51, "EL2HLT", "Level 2 halted"),
	EBADE(52, "EBADE", "Invalid exchange"),
	EBADR(53, "EBADR", "Invalid request descriptor"),
	EXFULL(54, "EXFULL", "Exchange full"),
	ENOANO(55, "ENOANO", "No anode"),
	EBADRQC(56, "EBADRQC", "Invalid request code"),
	EBADSLT(57, "EBADSLT", "Invalid slot"),
	 // EDEADLOCK = EDEADLK
	EBFONT(59, "EBFONT", "Bad font file format"),
	ENOSTR(60, "ENOSTR", "Device not a stream"),
	ENODATA(61, "ENODATA", "No data available"),
	ETIME(62, "ETIME", "Timer expired"),
	ENOSR(63, "ENOSR", "Out of streams resources"),
	ENONET(64, "ENONET", "Machine is not on the network"),
	ENOPKG(65, "ENOPKG", "Package not installed"),
	EREMOTE(66, "EREMOTE", "Object is remote"),
	ENOLINK(67, "ENOLINK", "Link has been severed"),
	EADV(68, "EADV", "Advertise error"),
	ESRMNT(69, "ESRMNT", "Srmount error"),
	ECOMM(70, "ECOMM", "Communication error on send"),
	EPROTO(71, "EPROTO", "Protocol error"),
	EMULTIHOP(72, "EMULTIHOP", "Multihop attempted"),
	EDOTDOT(73, "EDOTDOT", "RFS specific error"),
	EBADMSG(74, "EBADMSG", "Not a data message"),
	EOVERFLOW(75, "EOVERFLOW", "Value too large for defined data type"),
	ENOTUNIQ(76, "ENOTUNIQ", "Name not unique on network"),
	EBADFD(77, "EBADFD", "File descriptor in bad state"),
	EREMCHG(78, "EREMCHG", "Remote address changed"),
	ELIBACC(79, "ELIBACC", "Can not access a needed shared library"),
	ELIBBAD(80, "ELIBBAD", "Accessing a corrupted shared library"),
	ELIBSCN(81, "ELIBSCN", ".lib section in a.out corrupted"),
	ELIBMAX(82, "ELIBMAX", "Attempting to link in too many shared libraries"),
	ELIBEXEC(83, "ELIBEXEC", "Cannot exec a shared library directly"),
	EILSEQ(84, "EILSEQ", "Illegal byte sequence"),
	ERESTART(85, "ERESTART", "Interrupted system call should be restarted"),
	ESTRPIPE(86, "ESTRPIPE", "Streams pipe error"),
	EUSERS(87, "EUSERS", "Too many users"),
	ENOTSOCK(88, "ENOTSOCK", "Socket operation on non-socket"),
	EDESTADDRREQ(89, "EDESTADDRREQ", "Destination address required"),
	EMSGSIZE(90, "EMSGSIZE", "Message too long"),
	EPROTOTYPE(91, "EPROTOTYPE", "Protocol wrong type for socket"),
	ENOPROTOOPT(92, "ENOPROTOOPT", "Protocol not available"),
	EPROTONOSUPPORT(93, "EPROTONOSUPPORT", "Protocol not supported"),
	ESOCKTNOSUPPORT(94, "ESOCKTNOSUPPORT", "Socket type not supported"),
	EOPNOTSUPP(95, "EOPNOTSUPP", "Operation not supported on transport endpoint"),
	EPFNOSUPPORT(96, "EPFNOSUPPORT", "Protocol family not supported"),
	EAFNOSUPPORT(97, "EAFNOSUPPORT", "Address family not supported by protocol"),
	EADDRINUSE(98, "EADDRINUSE", "Address already in use"),
	EADDRNOTAVAIL(99, "EADDRNOTAVAIL", "Cannot assign requested address"),
	ENETDOWN(100, "ENETDOWN", "Network is down"),
	ENETUNREACH(101, "ENETUNREACH", "Network is unreachable"),
	ENETRESET(102, "ENETRESET", "Network dropped connection because of reset"),
	ECONNABORTED(103, "ECONNABORTED", "Software caused connection abort"),
	ECONNRESET(104, "ECONNRESET", "Connection reset by peer"),
	ENOBUFS(105, "ENOBUFS", "No buffer space available"),
	EISCONN(106, "EISCONN", "Transport endpoint is already connected"),
	ENOTCONN(107, "ENOTCONN", "Transport endpoint is not connected"),
	ESHUTDOWN(108, "ESHUTDOWN", "Cannot send after transport endpoint shutdown"),
	ETOOMANYREFS(109, "ETOOMANYREFS", "Too many references: cannot splice"),
	ETIMEDOUT(110, "ETIMEDOUT", "Connection timed out"),
	ECONNREFUSED(111, "ECONNREFUSED", "Connection refused"),
	EHOSTDOWN(112, "EHOSTDOWN", "Host is down"),
	EHOSTUNREACH(113, "EHOSTUNREACH", "No route to host"),
	EALREADY(114, "EALREADY", "Operation already in progress"),
	EINPROGRESS(115, "EINPROGRESS", "Operation now in progress"),
	ESTALE(116, "ESTALE", "Stale NFS file handle"),
	EUCLEAN(117, "EUCLEAN", "Structure needs cleaning"),
	ENOTNAM(118, "ENOTNAM", "Not a XENIX named type file"),
	ENAVAIL(119, "ENAVAIL", "No XENIX semaphores available"),
	EISNAM(120, "EISNAM", "Is a named type file"),
	EREMOTEIO(121, "EREMOTEIO", "Remote I/O error"),
	EDQUOT(122, "EDQUOT", "Quota exceeded"),
	ENOMEDIUM(123, "ENOMEDIUM", "No medium found"),
	EMEDIUMTYPE(124, "EMEDIUMTYPE", "Wrong medium type"),
	ECANCELED(125, "ECANCELED", "Operation Canceled"),
	ENOKEY(126, "ENOKEY", "Required key not available"),
	EKEYEXPIRED(127, "EKEYEXPIRED", "Key has expired"),
	EKEYREVOKED(128, "EKEYREVOKED", "Key has been revoked"),
	EKEYREJECTED(129, "EKEYREJECTED", "Key was rejected by service"),
	/* for robust mutexes */
	EOWNERDEAD(130, "EOWNERDEAD", "Owner died"),
	ENOTRECOVERABLE(131, "ENOTRECOVERABLE", "State not recoverable"),
	ERFKILL(132, "ERFKILL", "Operation not possible due to RF-kill"),
	EHWPOISON(133, "EHWPOISON", "Memory page has hardware error");
	
	private final int num;
	private final String name;
	private final String desc;

	/**
	 * Construction d'un objet
	 * @param num
	 * @param name
	 * @param desc
	 */
	PlatformError(int num, String name, String desc) {
		this.num = num;
		this.name = name;
		this.desc = desc;
	}
	
	/**
	 * Obtention du numéro d'erreur
	 * 
	 * @return
	 */
	public int getNumber() {
		return num;
	}

	/**
	 * Obtention du nom
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * Obtention de la description
	 * 
	 * @return
	 */
	public String getDescription() {
		return desc;
	}

	private static final Map<Integer, PlatformError> errorsByNumber = initErrorsByNumber();
	
	private static Map<Integer, PlatformError> initErrorsByNumber() {
		Map<Integer, PlatformError> map = new Hashtable<Integer, PlatformError>();
		for(PlatformError e : PlatformError.values()) {
			map.put(new Integer(e.getNumber()), e);
		}
		return map;
	}

	/**
	 * Obtention d'une erreur par son numéro
	 * @param number
	 * @return
	 */
	public static PlatformError getByNumber(int number) {
		return errorsByNumber.get(new Integer(number));
	}
}
