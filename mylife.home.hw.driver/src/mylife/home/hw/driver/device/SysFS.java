package mylife.home.hw.driver.device;

import java.io.File;
import java.nio.charset.Charset;

import mylife.home.hw.driver.platform.PlatformConstants;
import mylife.home.hw.driver.platform.PlatformFile;

class SysFS {

	/**
	 * Numéro GPIO
	 */
	private final int gpioId;
	
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
	 * @param gpioId
	 * @param options
	 * @param baseDirectory
	 * @param exportFile
	 * @param unexportFile
	 * @param itemDirectoryName
	 */
	public SysFS(int gpioId,
			String baseDirectory, String exportFile, String unexportFile,
			String itemDirectoryName) {

		this.gpioId = gpioId;
		this.exportPath = baseDirectory + File.separator + exportFile;
		this.unexportPath = baseDirectory + File.separator + unexportFile;
		this.itemDirectoryPath = baseDirectory + File.separator + itemDirectoryName + gpioId;
	}

	private static final Charset ascii = Charset.forName("US-ASCII");

	/**
	 * Ecriture dans un fichier
	 * 
	 * @param file
	 * @param content
	 */
	protected void writeFile(String file, String content) {
		int flags = PlatformConstants.O_APPEND | PlatformConstants.O_WRONLY;
		PlatformFile fd = new PlatformFile(file, flags);
		try
		{
			fd.write(content.getBytes(ascii));
		} finally {
			fd.close();
		}
	}
	
	/**
	 * Ecriture d'une valeur dans un fichier de l'interface
	 * @param valueFile
	 * @param value
	 */
	public void writeValue(String valueFile, String value) {
		writeFile(itemDirectoryPath + File.separator + valueFile, value);
	}

	private boolean fileExists(String path) {
		File file = new File(path);
		return file.exists();
	}
	
	/**
	 * Ouverture du périphérique
	 */
	public void open() {
		if(isOpened())
			throw new java.lang.UnsupportedOperationException("The specified pin is already opened");
		writeFile(exportPath, "" + gpioId);
	}
	
	/**
	 * Indique si le périphérique est ouvert
	 * @return
	 */
	public boolean isOpened() {
		return fileExists(itemDirectoryPath);
	}
	
	/**
	 * Fermeture du périphérique
	 */
	public void close() {
		if(!isOpened())
			throw new java.lang.UnsupportedOperationException("The specified pin is not opened");
		writeFile(unexportPath, "" + gpioId);
	}

	/**
	 * Numéro GPIO
	 * @return
	 */
	protected int getGpioId() {
		return gpioId;
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
