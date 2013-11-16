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

public class DataConfigurationAccess extends BaseDataAccess {

	/**
	 * Logger
	 */
	private final static Logger log = Logger.getLogger(DataConfigurationAccess.class
			.getName());
	
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
}
