package mylife.home.hw.emulator.web.render;

import java.io.IOException;

/**
 * Flux d'écriture web
 * @author pumbawoman
 *
 */
public class WebStream {

	/**
	 * Appender final
	 */
	private final Appendable appender;
	
	/**
	 * Niveau d'indentation
	 */
	private int indentLevel;
	
	/**
	 * Constructeur avec appender
	 * @param appender
	 */
	public WebStream(Appendable appender) {
		this.appender = appender;
		indentLevel = 0;
	}
	
	/**
	 * Ecriture d'une ligne complète avec indentation texte et retour à la ligne
	 * @return
	 * @throws IOException 
	 */
	public WebStream writeln(String line) throws IOException {
		indentWrite();
		write(line);
		writeln();
		return this;
	}
	
	/**
	 * Ecriture d'un texte
	 * @param text
	 * @return
	 * @throws IOException 
	 */
	public WebStream write(String text) throws IOException {
		appender.append(text);
		return this;
	}
	
	/**
	 * Ecriture d'un retour à la ligne
	 * @return
	 * @throws IOException 
	 */
	public WebStream writeln() throws IOException {
		appender.append('\n');
		return this;
	}
	
	/**
	 * Ecriture de l'indentation
	 * @return
	 * @throws IOException 
	 */
	public WebStream indentWrite() throws IOException {
		for(int i=0; i<indentLevel; i++)
			appender.append('\t');
		return this;
	}
	
	/**
	 * Augmente l'indentation
	 * @return
	 */
	public WebStream indentInc() {
		++indentLevel;
		return this;
	}
	
	/**
	 * Diminue l'identation
	 * @return
	 */
	public WebStream indentDec() {
		if(indentLevel == 0)
			throw new UnsupportedOperationException();
		--indentLevel;
		return this;
	}
	
}
