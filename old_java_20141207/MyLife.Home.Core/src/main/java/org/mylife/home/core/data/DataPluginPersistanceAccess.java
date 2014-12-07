package org.mylife.home.core.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.mylife.home.common.data.BaseDataAccess;
import org.mylife.home.common.data.DataException;

/**
 * Accès aux données
 * 
 * @author pumbawoman
 * 
 */
public class DataPluginPersistanceAccess extends BaseDataAccess {

	/**
	 * Logger
	 */
	private final static Logger log = Logger.getLogger(DataPluginPersistanceAccess.class
			.getName());

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
