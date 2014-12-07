package org.mylife.home.core.data;

import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.mylife.home.common.data.BaseDataAccess;
import org.mylife.home.common.data.DataException;

public class DataPluginAccess extends BaseDataAccess {

	/**
	 * Logger
	 */
	private final static Logger log = Logger.getLogger(DataPluginAccess.class
			.getName());

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
}
