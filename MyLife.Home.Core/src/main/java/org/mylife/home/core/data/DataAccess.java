package org.mylife.home.core.data;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mylife.home.core.Configuration;

/**
 * Accès aux données
 * 
 * @author pumbawoman
 * 
 */
public class DataAccess {

	/**
	 * Logger
	 */
	private final static Logger log = Logger.getLogger(DataAccess.class
			.getName());

	private final Connection con;

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
	public DataAccess() {
		Properties config = Configuration.getInstance().getProperties();
		String url = config.getProperty("data.url");
		String user = config.getProperty("data.user");
		String password = config.getProperty("data.password");
		try {
			con = DriverManager.getConnection(url, user, password);
			log.info("Connection opened");
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
			log.info("Connection closed");
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	private void safeClose(Statement st) {
		if (st == null)
			return;
		try {
			st.close();
		} catch (SQLException e1) {
			log.log(Level.SEVERE, "Error closing statement", e1);
		}
	}

	private void safeClose(ResultSet rs) {
		if (rs == null)
			return;
		try {
			rs.close();
		} catch (SQLException e1) {
			log.log(Level.SEVERE, "Error closing result set", e1);
		}
	}

	public DataConfiguration getConfigurationByKey(int id) {
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			pst = con
					.prepareStatement("select * from core_configuration where conf_id = ?");
			pst.setInt(1, id);
			rs = pst.executeQuery();

			if (!rs.next())
				return null;

			DataConfiguration item = new DataConfiguration();
			map(item, rs);
			return item;

		} catch (SQLException e) {
			throw new DataException(e);
		} finally {
			safeClose(rs);
			safeClose(pst);
		}
	}

	public Set<DataConfiguration> getConfigurationsActives() {
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			pst = con
					.prepareStatement("select * from core_configuration where conf_active = 1");
			rs = pst.executeQuery();

			Set<DataConfiguration> set = new HashSet<DataConfiguration>();
			while (rs.next()) {
				DataConfiguration item = new DataConfiguration();
				map(item, rs);
				set.add(item);
			}
			return set;

		} catch (SQLException e) {
			throw new DataException(e);
		} finally {
			safeClose(rs);
			safeClose(pst);
		}
	}

	public Set<DataConfiguration> getConfigurationsAll() {
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			pst = con.prepareStatement("select * from core_configuration");
			rs = pst.executeQuery();

			Set<DataConfiguration> set = new HashSet<DataConfiguration>();
			while (rs.next()) {
				DataConfiguration item = new DataConfiguration();
				map(item, rs);
				set.add(item);
			}
			return set;

		} catch (SQLException e) {
			throw new DataException(e);
		} finally {
			safeClose(rs);
			safeClose(pst);
		}
	}

	public void createConfiguration(DataConfiguration item) {
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			pst = con
					.prepareStatement(
							"insert into core_configuration "
									+ "(conf_type, conf_content, conf_active, conf_date, conf_comment) "
									+ "values (?, ?, ?, ?, ?)",
							Statement.RETURN_GENERATED_KEYS);
			Blob content = con.createBlob();
			content.setBytes(1, item.getContent());

			pst.setString(1, item.getType());
			pst.setBlob(2, content);
			pst.setBoolean(3, item.isActive());
			pst.setDate(4, new Date(item.getDate().getTime()));
			pst.setString(5, item.getComment());

			pst.executeUpdate();

			rs = pst.getGeneratedKeys();
			if (!rs.next())
				throw new DataException("Cannot read inserted id");
			item.setId(rs.getInt(1));

		} catch (SQLException e) {
			throw new DataException(e);
		} finally {
			safeClose(rs);
			safeClose(pst);
		}
	}

	public void updateConfiguration(DataConfiguration item) {
		PreparedStatement pst = null;
		try {
			pst = con.prepareStatement("update core_configuration "
					+ "set conf_active = ? " + ", conf_comment = ? "
					+ "where conf_id = ?");
			pst.setBoolean(1, item.isActive());
			pst.setString(2, item.getComment());
			pst.setInt(3, item.getId());
			pst.executeUpdate();

		} catch (SQLException e) {
			throw new DataException(e);
		} finally {
			safeClose(pst);
		}
	}

	public void deleteConfiguration(DataConfiguration item) {
		PreparedStatement pst = null;
		try {
			pst = con
					.prepareStatement("delete from core_configuration where conf_id = ?");
			
			pst.setInt(1, item.getId());
			pst.executeUpdate();

		} catch (SQLException e) {
			throw new DataException(e);
		} finally {
			safeClose(pst);
		}
	}

	private void map(DataConfiguration item, ResultSet rs) throws SQLException {

		item.setId(rs.getInt("conf_id"));
		item.setType(rs.getString("conf_type"));
		Blob content = rs.getBlob("conf_content");
		item.setContent(content.getBytes(1, (int) content.length()));
		item.setActive(rs.getBoolean("conf_active"));
		item.setDate(rs.getDate("conf_date"));
		item.setComment(rs.getString("conf_comment"));
	}
}
