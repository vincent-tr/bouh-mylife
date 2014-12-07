package org.mylife.home.net.hub.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.mylife.home.common.data.BaseDataAccess;
import org.mylife.home.common.data.DataException;

public class DataLinkAccess extends BaseDataAccess {

	/**
	 * Logger
	 */
	private final static Logger log = Logger.getLogger(DataLinkAccess.class
			.getName());

	public DataLink getLinkByKey(int id) {
		log.log(level, "getLinkByKey(%d)", id);
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			pst = con
					.prepareStatement("select * from net_link where link_id = ?");
			pst.setInt(1, id);
			rs = pst.executeQuery();

			if (!rs.next())
				return null;

			DataLink item = new DataLink();
			map(item, rs);
			return item;

		} catch (SQLException e) {
			throw new DataException(e);
		} finally {
			safeClose(rs);
			safeClose(pst);
		}
	}

	public List<DataLink> getLinksAll() {
		log.log(level, "getLinksAll()");
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			pst = con
					.prepareStatement("select * from net_link order by link_name");
			rs = pst.executeQuery();

			List<DataLink> list = new ArrayList<DataLink>();
			while (rs.next()) {
				DataLink item = new DataLink();
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

	public void createLink(DataLink item) {
		log.log(level, "createLink()");
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			pst = con.prepareStatement("insert into net_link "
					+ "(link_name, link_address, link_port) "
					+ "values (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			pst.setString(1, item.getName());
			pst.setString(2, item.getAddress());
			pst.setInt(3, item.getPort());

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

	public void updateLink(DataLink item) {
		log.log(level, "updateLink(%d)", item.getId());
		PreparedStatement pst = null;
		try {
			pst = con.prepareStatement("update net_link "
					+ "set link_name = ? " + ", link_address = ? "
					+ ", link_port = ? " + "where link_id = ?");
			pst.setString(1, item.getName());
			pst.setString(2, item.getAddress());
			pst.setInt(3, item.getPort());
			pst.setInt(4, item.getId());
			pst.executeUpdate();

		} catch (SQLException e) {
			throw new DataException(e);
		} finally {
			safeClose(pst);
		}
	}

	public void deleteLink(DataLink item) {
		log.log(level, "deleteLink(%d)", item.getId());
		PreparedStatement pst = null;
		try {
			pst = con
					.prepareStatement("delete from net_link where link_id = ?");

			pst.setInt(1, item.getId());
			pst.executeUpdate();

		} catch (SQLException e) {
			throw new DataException(e);
		} finally {
			safeClose(pst);
		}
	}

	private void map(DataLink item, ResultSet rs) throws SQLException {

		item.setId(rs.getInt("link_id"));
		item.setName(rs.getString("link_name"));
		item.setAddress(rs.getString("link_address"));
		item.setPort(rs.getInt("link_port"));
	}

}
