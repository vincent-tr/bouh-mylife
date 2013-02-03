package mylife.home.hw.driver.device;

import java.io.File;
import java.nio.charset.Charset;
import java.util.EnumSet;

import mylife.home.hw.api.Options;
import mylife.home.hw.driver.platform.PlatformConstants;
import mylife.home.hw.driver.platform.PlatformFile;

/**
 * Implémentation d'un device avec sysfs
 * 
 * @author pumbawoman
 * 
 */
public abstract class SysFSDeviceImpl extends DeviceImpl {

	/**
	 * Fichier d'export '/sys/class/gpio/export'
	 */
	private final String exportPath;

	/**
	 * Fichier de fin d'export '/sys/class/gpio/unexport'
	 */
	private final String unexportPath;

	/**
	 * Sous-dossier représentant un item '/sys/class/gpio/gpio0'
	 */
	private final String itemDirectoryPath;

	/**
	 * Constructeur avec données
	 * 
	 * @param pinId
	 * @param options
	 * @param baseDirectory
	 * @param exportFile
	 * @param unexportFile
	 * @param itemDirectoryName
	 */
	protected SysFSDeviceImpl(int pinId, EnumSet<Options> options,
			String baseDirectory, String exportFile, String unexportFile,
			String itemDirectoryName) {

		super(pinId, options);

		this.exportPath = baseDirectory + File.separator + exportFile;
		this.unexportPath = baseDirectory + File.separator + unexportFile;
		this.itemDirectoryPath = baseDirectory + File.separator + itemDirectoryName + getGpioId();

		open();
	}

	private static final Charset ascii = Charset.forName("US-ASCII");

	/**
	 * Ecriture dans un fichier
	 * 
	 * @param file
	 * @param content
	 */
	protected void write(String file, String content) {
		int flags = PlatformConstants.O_APPEND | PlatformConstants.O_WRONLY;
		PlatformFile fd = new PlatformFile(file, flags);
		try
		{
			fd.write(content.getBytes(ascii));
		} finally {
			fd.close();
		}
	}

	private boolean fileExists(String path) {
		File file = new File(path);
		return file.exists();
	}
	
	/**
	 * Ouverture du périphérique
	 */
	protected void open() {
		if(fileExists(itemDirectoryPath))
			throw new java.lang.UnsupportedOperationException("The specified pin is already opened");
		write(exportPath, "" + getGpioId());
	}

	/**
	 * Fin d'utilisation du périphérique
	 */
	protected void reset() {
		write(unexportPath, "" + getGpioId());
	}

	/**
	 * Fichier d'export '/sys/class/gpio/export'
	 * @return
	 */
	protected String getExportPath() {
		return exportPath;
	}

	/**
	 * Fichier de fin d'export '/sys/class/gpio/unexport'
	 * @return
	 */
	protected String getUnexportPath() {
		return unexportPath;
	}

	/**
	 * Sous-dossier représentant un item '/sys/class/gpio/gpio0'
	 * @return
	 */
	protected String getItemDirectoryPath() {
		return itemDirectoryPath;
	}

	/**
	 * Ecriture ascii
	 * @return
	 */
	protected static Charset getAscii() {
		return ascii;
	}

}
