package org.mylife.home.common.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mylife.home.common.Configuration;

/**
 * Accès aux données
 * 
 * @author pumbawoman
 * 
 */
public class BaseDataAccess {

	/**
	 * Logger
	 */
	private final static Logger log = Logger.getLogger(BaseDataAccess.class
			.getName());
	protected final static Level level = Level.INFO;

	protected final Connection con;

	static {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (Exception e) {
			throw new DataException(e);
		}
	}

	/**
	 * Constructeur et ouverture
	 */
	public BaseDataAccess() {
		Properties config = Configuration.getInstance().getProperties();
		String url = config.getProperty("data.url");
		String user = config.getProperty("data.user");
		String password = config.getProperty("data.password");
		try {
			con = DriverManager.getConnection(url, user, password);
			log.log(level, "Connection opened");
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	/**
	 * Fermeture
	 */
	public void close() {
		try {
			con.close();
			log.log(level, "Connection closed");
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	protected void safeClose(Statement st) {
		if (st == null)
			return;
		try {
			st.close();
		} catch (SQLException e1) {
			log.log(Level.SEVERE, "Error closing statement", e1);
		}
	}

	protected void safeClose(ResultSet rs) {
		if (rs == null)
			return;
		try {
			rs.close();
		} catch (SQLException e1) {
			log.log(Level.SEVERE, "Error closing result set", e1);
		}
	}
}
