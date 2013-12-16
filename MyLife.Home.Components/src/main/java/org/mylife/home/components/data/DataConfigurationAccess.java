package org.mylife.home.components.data;

import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.mylife.home.common.data.BaseDataAccess;
import org.mylife.home.common.data.DataException;

public class DataConfigurationAccess extends BaseDataAccess {

	/**
	 * Logger
	 */
	private final static Logger log = Logger
			.getLogger(DataConfigurationAccess.class.getName());

	public DataConfiguration getConfigurationByKey(int id) {
		log.log(level, "getConfigurationByKey(%d)", id);
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			pst = con
					.prepareStatement("select * from comp_configuration where conf_id = ?");
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
					.prepareStatement("select * from comp_configuration where conf_active = 1");
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
			pst = con.prepareStatement("select * from comp_configuration");
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
			pst = con.prepareStatement("insert into comp_configuration "
					+ "(conf_compid, conf_type, conf_active, conf_parameters) "
					+ "values (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			Blob parameters = con.createBlob();
			parameters.setBytes(1, item.getParameters());

			pst.setString(1, item.getComponentId());
			pst.setString(2, item.getType());
			pst.setBoolean(3, item.isActive());
			pst.setBlob(4, parameters);

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
		log.log(level, "updateConfiguration(%d)", item.getId());
		PreparedStatement pst = null;
		try {
			pst = con.prepareStatement("update comp_configuration "
					+ "set conf_compid = ? " + ", conf_type = ? "
					+ "set conf_active = ? " + ", conf_parameters = ? "
					+ "where conf_id = ?");

			Blob parameters = con.createBlob();
			parameters.setBytes(1, item.getParameters());

			pst.setString(1, item.getComponentId());
			pst.setString(2, item.getType());
			pst.setBoolean(3, item.isActive());
			pst.setBlob(4, parameters);
			pst.setInt(5, item.getId());
			pst.executeUpdate();

		} catch (SQLException e) {
			throw new DataException(e);
		} finally {
			safeClose(pst);
		}
	}

	public void deleteConfiguration(DataConfiguration item) {
		log.log(level, "deleteConfiguration(%d)", item.getId());
		PreparedStatement pst = null;
		try {
			pst = con
					.prepareStatement("delete from comp_configuration where conf_id = ?");

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
		item.setComponentId(rs.getString("conf_compid"));
		item.setType(rs.getString("conf_type"));
		item.setActive(rs.getBoolean("conf_active"));
		Blob parameters = rs.getBlob("conf_parameters");
		item.setParameters(parameters.getBytes(1, (int) parameters.length()));
	}

}
