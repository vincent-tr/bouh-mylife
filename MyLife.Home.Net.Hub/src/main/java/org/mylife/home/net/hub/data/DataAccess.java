package org.mylife.home.net.hub.data;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

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
	private final static Level level = Level.INFO;

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
		log.log(level, "getConfigurationByKey(%d)", id);
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

	public List<DataConfiguration> getConfigurationsActives() {
		log.log(level, "getConfigurationsActives()");
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			pst = con
					.prepareStatement("select * from core_configuration where conf_active = 1 order by conf_type, conf_date");
			rs = pst.executeQuery();

			List<DataConfiguration> list = new ArrayList<DataConfiguration>();
			while (rs.next()) {
				DataConfiguration item = new DataConfiguration();
				map(item, rs);
				list.add(item);
			}
			return list;

		} catch (SQLException e) {
			throw new DataException(e);
		} finally {
			safeClose(rs);
			safeClose(pst);
		}
	}

	public List<DataConfiguration> getConfigurationsAll() {
		log.log(level, "getConfigurationsAll()");
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			pst = con
					.prepareStatement("select * from core_configuration order by conf_type, conf_date");
			rs = pst.executeQuery();

			List<DataConfiguration> list = new ArrayList<DataConfiguration>();
			while (rs.next()) {
				DataConfiguration item = new DataConfiguration();
				map(item, rs);
				list.add(item);
			}
			return list;

		} catch (SQLException e) {
			throw new DataException(e);
		} finally {
			safeClose(rs);
			safeClose(pst);
		}
	}

	public void createConfiguration(DataConfiguration item) {
		log.log(level, "createConfiguration()");
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
			pst.setTimestamp(4, new Timestamp(item.getDate().getTime()));
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
		log.log(level, "createConfiguration(%d)", item.getId());
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
		log.log(level, "createConfiguration(%d)", item.getId());
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
		item.setDate(rs.getTimestamp("conf_date"));
		item.setComment(rs.getString("conf_comment"));
	}

	public DataPlugin getPluginByKey(int id) {
		log.log(level, "getPluginByKey(%d)", id);
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			pst = con
					.prepareStatement("select * from core_plugin where plugin_id = ?");
			pst.setInt(1, id);
			rs = pst.executeQuery();

			if (!rs.next())
				return null;

			DataPlugin item = new DataPlugin();
			map(item, rs);
			return item;

		} catch (SQLException e) {
			throw new DataException(e);
		} finally {
			safeClose(rs);
			safeClose(pst);
		}
	}

	public List<DataPlugin> getPluginsActives() {
		log.log(level, "getPluginsActives()");
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			pst = con
					.prepareStatement("select * from core_plugin where plugin_active = 1 order by plugin_date");
			rs = pst.executeQuery();

			List<DataPlugin> list = new ArrayList<DataPlugin>();
			while (rs.next()) {
				DataPlugin item = new DataPlugin();
				map(item, rs);
				list.add(item);
			}
			return list;

		} catch (SQLException e) {
			throw new DataException(e);
		} finally {
			safeClose(rs);
			safeClose(pst);
		}
	}

	public List<DataPlugin> getPluginsAll() {
		log.log(level, "getPluginsAll()");
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			pst = con
					.prepareStatement("select * from core_plugin order by plugin_date");
			rs = pst.executeQuery();

			List<DataPlugin> list = new ArrayList<DataPlugin>();
			while (rs.next()) {
				DataPlugin item = new DataPlugin();
				map(item, rs);
				list.add(item);
			}
			return list;

		} catch (SQLException e) {
			throw new DataException(e);
		} finally {
			safeClose(rs);
			safeClose(pst);
		}
	}

	public void createPlugin(DataPlugin item) {
		log.log(level, "createPlugin()");
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			pst = con
					.prepareStatement(
							"insert into core_plugin "
									+ "(plugin_name, plugin_content, plugin_active, plugin_date, plugin_comment) "
									+ "values (?, ?, ?, ?, ?)",
							Statement.RETURN_GENERATED_KEYS);
			Blob content = con.createBlob();
			content.setBytes(1, item.getContent());

			pst.setString(1, item.getName());
			pst.setBlob(2, content);
			pst.setBoolean(3, item.isActive());
			pst.setTimestamp(4, new Timestamp(item.getDate().getTime()));
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

	public void updatePlugin(DataPlugin item) {
		log.log(level, "updatePlugin(%d)", item.getId());
		PreparedStatement pst = null;
		try {
			pst = con.prepareStatement("update core_plugin "
					+ "set plugin_active = ? " + ", plugin_comment = ? "
					+ "where plugin_id = ?");
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

	public void deletePlugin(DataPlugin item) {
		log.log(level, "deletePlugin(%d)", item.getId());
		PreparedStatement pst = null;
		try {
			pst = con
					.prepareStatement("delete from core_plugin where plugin_id = ?");

			pst.setInt(1, item.getId());
			pst.executeUpdate();

		} catch (SQLException e) {
			throw new DataException(e);
		} finally {
			safeClose(pst);
		}
	}

	private void map(DataPlugin item, ResultSet rs) throws SQLException {
		item.setId(rs.getInt("plugin_id"));
		item.setName(rs.getString("plugin_name"));
		Blob content = rs.getBlob("plugin_content");
		item.setContent(content.getBytes(1, (int) content.length()));
		item.setActive(rs.getBoolean("plugin_active"));
		item.setDate(rs.getTimestamp("plugin_date"));
		item.setComment(rs.getString("plugin_comment"));
	}

	public List<DataPluginPersistance> getPluginPersistanceByComponentId(
			String id) {
		log.log(level, "getPluginPersistanceByComponentId(%s)", id);
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			pst = con
					.prepareStatement("select * from core_plugin_persistance where pers_component_id = ?");
			pst.setString(1, id);
			rs = pst.executeQuery();

			List<DataPluginPersistance> list = new ArrayList<DataPluginPersistance>();
			while (rs.next()) {
				DataPluginPersistance item = new DataPluginPersistance();
				map(item, rs);
				list.add(item);
			}
			return list;

		} catch (SQLException e) {
			throw new DataException(e);
		} finally {
			safeClose(rs);
			safeClose(pst);
		}
	}

	public void deletePluginPersistanceByComponentId(String id) {
		log.log(level, "deletePluginPersistanceByComponentId(%s)", id);
		PreparedStatement pst = null;
		try {
			pst = con
					.prepareStatement("delete from core_plugin_persistance where pers_component_id = ?");
			pst.setString(1, id);
			pst.executeUpdate();

		} catch (SQLException e) {
			throw new DataException(e);
		} finally {
			safeClose(pst);
		}
	}

	public void createPluginPersistance(DataPluginPersistance item) {
		log.log(level, "createPluginPersistance()");
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			pst = con.prepareStatement("insert into core_plugin_persistance "
					+ "(ppers_component_id, ppers_key, ppers_value) "
					+ "values (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			pst.setString(1, item.getComponentId());
			pst.setString(2, item.getKey());
			pst.setString(3, item.getValue());

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

	private void map(DataPluginPersistance item, ResultSet rs)
			throws SQLException {
		item.setId(rs.getInt("ppers_id"));
		item.setComponentId(rs.getString("ppers_component_id"));
		item.setKey(rs.getString("ppers_key"));
		item.setValue(rs.getString("ppers_value"));
	}
}
