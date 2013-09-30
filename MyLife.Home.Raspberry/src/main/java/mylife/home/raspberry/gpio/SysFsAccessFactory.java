package mylife.home.raspberry.gpio;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Fabrique de SysFsAccess
 * @author pumbawoman
 *
 */
public abstract class SysFsAccessFactory {

	/**
	 * Obtention du chemin (ex : /sys/class/gpio/)
	 * @return
	 */
	protected abstract String getClassPath();
	
	/**
	 * Obtention du préfix (ex : gpio)
	 * @return
	 */
	protected abstract String getDevicePrefix();
	
	/**
	 * Obtention du nom du tool d'admin
	 * @return
	 */
	protected abstract String getAdminTool();
	
	/**
	 * Ouverture d'un accès
	 * @param pin
	 * @return
	 */
	public SysFsAccess openAccess(int pin) {
		export(pin);
		return createAccess(pin);
	}
	
	/**
	 * Création de la classe accès pour le pin spécifié et initialisation si nécessaire
	 * @param pin
	 * @return
	 */
	protected abstract SysFsAccess createAccess(int pin);
	
	/**
	 * Fermeture d'un accès
	 * @param access
	 */
	public void closeAccess(SysFsAccess access) {
		cleanupAccess(access);
		unexport(access.getPin());
	}
	
	/**
	 * Nettoyage de l'accès
	 * @param access
	 */
	protected void cleanupAccess(SysFsAccess access) {
	}
	
	/**
	 * Export
	 * @param pin
	 */
	protected void export(int pin) {
		runTool(getAdminTool(), true, pin);
	}
	
	/**
	 * Unexport
	 * @param pin
	 */
	protected void unexport(int pin) {
		runTool(getAdminTool(), false, pin);
	}
	
	private void runTool(String tool, boolean export, int pin) {
		Process process = null;
		
		// lancement du process
		String[] cmd = new String[] { tool, export ? "export" : "unexport", Integer.toString(pin) };
		try {
			process = Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			throw new AdminToolException(e);
		}
		
		// attente de la fin
		try {
			if(process.waitFor() == 0)
				return;
		} catch (InterruptedException e) {
			throw new AdminToolException(e);
		}
		
		// gestion d'erreur
		String error = null;
		try {
			error = readStream(process.getErrorStream());
		} catch(IOException e) {
			throw new AdminToolException(e);
		}
		
		throw new AdminToolException(error);
	}
	
	private String readStream(InputStream is) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    byte[] buffer = new byte[1024];
	    int length = 0;
	    while ((length = is.read(buffer)) != -1) {
	        baos.write(buffer, 0, length);
	    }
	    return new String(baos.toByteArray());
	}
}
