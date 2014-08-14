package nl.knaw.dans.clarin.playground;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import nl.knaw.dans.clarin.cmd2rdf.util.ActionStatus;

import com.twmacinta.util.MD5;

public class CmdMD5Db {
	//private static final Logger log = LoggerFactory.getLogger(CmdMD5Db.class);
	public static final String DB_NAME = "db_cmd_md5";
	public static final String QUERY = "";
	public static final int COL_CHECKSUM_MAX_LENGTH = 1024;
	public static final int COL_PATH_MAX_LENGTH = 256;
	public static final int COL_ACTION_MAX_LENTH = 10;

	static int i;
	static int gnInsert;
	static int gnUpdate;
	static int gnSkip;
	Connection conn;
	PreparedStatement gpsInsert;
	PreparedStatement gpsUpdate;

	public CmdMD5Db() throws ClassNotFoundException, SQLException {
		new CmdMD5Db(DB_NAME);
	}

	public CmdMD5Db(String db_file_name_prefix) throws ClassNotFoundException,
			SQLException {

		Class.forName("org.hsqldb.jdbcDriver");

		conn = DriverManager.getConnection(
				"jdbc:hsqldb:" + db_file_name_prefix, "sa", "");
		update("CREATE TABLE IF NOT EXISTS CMD_MD5 "
				+ "( id INTEGER IDENTITY, path VARCHAR("
				+ COL_CHECKSUM_MAX_LENGTH + "), " + "md5 VARCHAR("
				+ COL_CHECKSUM_MAX_LENGTH + "), action VARCHAR("
				+ COL_ACTION_MAX_LENTH + "))");
		 conn.setAutoCommit(false);
		 gpsInsert = conn
					.prepareStatement("INSERT INTO CMD_MD5(path, md5, action) VALUES(?,?,?)");
		 gpsUpdate = conn
					.prepareStatement("UPDATE CMD_MD5  SET md5 = ?, action=? WHERE path = ?");
	}

	public CmdMD5Db(String db_file_name_prefix, boolean dropDb)
			throws ClassNotFoundException, SQLException {

		Class.forName("org.hsqldb.jdbcDriver");

		conn = DriverManager.getConnection(
				"jdbc:hsqldb:" + db_file_name_prefix, "sa", "");

		if (dropDb)
			update("DROP TABLE CMD_MD5 IF EXISTS");

		update("CREATE TABLE IF NOT EXISTS CMD_MD5 "
				+ "( id INTEGER IDENTITY, path VARCHAR("
				+ COL_CHECKSUM_MAX_LENGTH + "), " + "md5 VARCHAR("
				+ COL_CHECKSUM_MAX_LENGTH + "), action VARCHAR("
				+ COL_ACTION_MAX_LENTH + "))");
		 conn.setAutoCommit(false);
		 gpsInsert = conn
					.prepareStatement("INSERT INTO CMD_MD5(path, md5, action) VALUES(?,?,?)");
		 gpsUpdate = conn
					.prepareStatement("UPDATE CMD_MD5  SET md5 = ?, action=? WHERE path = ?");
	}

	public void walk(String basefolder) {
		try {
			File root = new File(basefolder);
			File[] list = root.listFiles();

			if (list == null)
				return;

			for (File file : list) {
				if (file.isDirectory()) {
					walk(file.getAbsolutePath());
				} else {
					String abspath = file.getAbsolutePath();
					if (abspath.endsWith(".xml")) {
						store(basefolder, file, abspath);
					
					}

				}
				
			}

		} catch (SQLException e) {
			System.out.println("ERROR WALK: " + e.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			// shutdown();
		}
	}

	private void store(String basefolder, File file, String abspath)
			throws IOException, SQLException {
		i++;
		String hash = MD5.asHex(MD5.getHash(file));
		String relativePath = abspath.replace(
				(basefolder + "/"), "");
		String checksum = getChecksum(relativePath);
		if (checksum == null) {
			gnInsert++;
			gpsInsert.setString(1, relativePath);
			gpsInsert.setString(2, hash);
			gpsInsert.setString(3, ActionStatus.NEW.name());
			gpsInsert.addBatch();
			if (gnInsert % 50000 == 0) {
				long ll1 = System.currentTimeMillis();
				System.out.println("Inserting 50.000 records");
				gpsInsert.executeBatch();
				conn.commit();
				System.out.println("Number of records: " + gnInsert + " Duration: " + (System.currentTimeMillis() - ll1)/ 1000 + " seconds.");
				gpsInsert = conn
						.prepareStatement("INSERT INTO CMD_MD5(path, md5, action) VALUES(?,?,?)");

			}
		} else {
			if (!checksum.equals(hash)) {
				gnUpdate++;
				gpsUpdate.setString(1, hash);
				gpsUpdate.setString(2, ActionStatus.UPDATE.name());
				gpsUpdate.setString(3, relativePath);
				gpsUpdate.addBatch();
				if (gnUpdate % 50000 == 0) {
					long ll1 = System.currentTimeMillis();
					System.out.println("Updating 50.000 records");
					gpsUpdate.executeBatch();
					conn.commit();
					System.out.println("Number of updated records: "
							+ gnUpdate + " Duration: " + (System.currentTimeMillis() - ll1)/ 1000 + " seconds.");
					gpsUpdate = conn
							.prepareStatement("UPDATE CMD_MD5  SET md5 = ?, action=? WHERE path = ?");
				}
			} else {
				gnSkip++;
				gpsUpdate.setString(1, hash);
				gpsUpdate.setString(2, ActionStatus.NONE.name());
				gpsUpdate.setString(3, relativePath);
				gpsUpdate.addBatch();
				if (gnSkip % 50000 == 0) {
					long ll1 = System.currentTimeMillis();
					System.out.println("Skipping 50.000 records");
					gpsUpdate.executeBatch();
					conn.commit();
					System.out.println("Number of skipped records: "
							+ gnUpdate + " Duration: " + (System.currentTimeMillis() - ll1)/ 1000 + " seconds.");
					gpsUpdate = conn
							.prepareStatement("UPDATE CMD_MD5  SET md5 = ?, action=? WHERE path = ?");

				}
			}
		}
	}
	
	public void commitRetainRecords() throws SQLException{
		 if (gnInsert%10000 != 0) {
	            int xx[] = gpsInsert.executeBatch();
	            conn.commit();
	            System.out.println("INSERT: " + xx.length);
         }
         if (gnUpdate%10000 != 0) {
	            int xx[] = gpsUpdate.executeBatch();
	            conn.commit();
	            System.out.println("UPDATE: " + xx.length);
         }
         
         if (gnSkip%10000 != 0) {
	            int xx[] = gpsUpdate.executeBatch();
	            conn.commit();
	            System.out.println("UPDATE: " + xx.length);
      }
	}

	public void shutdown() {

		Statement st;
		try {
			st = conn.createStatement();
			st.execute("SHUTDOWN");
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private synchronized void update(String expression) throws SQLException {

		Statement st = conn.createStatement();

		int i = st.executeUpdate(expression);

		if (i == -1) {
			System.out.println("db error : " + expression);
		}

		st.close();
	}

	private String getChecksum(String path) throws SQLException {
		String checksum = null;

		Statement st = conn.createStatement();
		ResultSet rs = st.executeQuery("SELECT md5 FROM CMD_MD5 WHERE path = '"
				+ path + "'");
		for (; rs.next();) {
			checksum = rs.getString("md5");
		}
		st.close();
		return checksum;
	}

	public int getTotalNumberOfRecords() throws SQLException {
		int total = 0;

		Statement st = conn.createStatement();
		ResultSet rs = st.executeQuery("SELECT count(*) AS total FROM CMD_MD5");
		for (; rs.next();) {
			total = rs.getInt("total");
		}
		st.close();
		return total;
	}

	public int getTotalNumberOfNewRecords() throws SQLException {
		int total = 0;

		Statement st = conn.createStatement();
		ResultSet rs = st
				.executeQuery("SELECT count(*) AS total FROM CMD_MD5 WHERE action='"
						+ ActionStatus.NEW.name() + "'");
		for (; rs.next();) {
			total = rs.getInt("total");
		}
		st.close();
		return total;
	}

	public int getTotalNumberOfUpdatedRecords() throws SQLException {
		int total = 0;

		Statement st = conn.createStatement();
		ResultSet rs = st
				.executeQuery("SELECT count(*) AS total FROM CMD_MD5 WHERE action='"
						+ ActionStatus.UPDATE.name() + "'");
		for (; rs.next();) {
			total = rs.getInt("total");
		}
		st.close();
		return total;
	}

	public int getTotalNumberOfDoneRecords() throws SQLException {
		int total = 0;
		Statement st = conn.createStatement();
		ResultSet rs = st
				.executeQuery("SELECT count(*) AS total FROM CMD_MD5 WHERE action='"
						+ ActionStatus.DONE.name() + "'");
		for (; rs.next();) {
			total = rs.getInt("total");
		}
		st.close();
		return total;
	}

	public int getTotalNumberOfNoneRecords() throws SQLException {
		int total = 0;
		Statement st = conn.createStatement();
		ResultSet rs = st
				.executeQuery("SELECT count(*) AS total FROM CMD_MD5 WHERE action='"
						+ ActionStatus.NONE.name() + "'");
		for (; rs.next();) {
			total = rs.getInt("total");
		}
		st.close();
		return total;
	}

	public void printAll() throws SQLException {
		Statement st = conn.createStatement();
		ResultSet rs = st.executeQuery("SELECT * FROM CMD_MD5");
		ResultSetMetaData meta = rs.getMetaData();
		int colmax = meta.getColumnCount();
		int i;
		Object o = null;
		st = conn.createStatement();

		for (; rs.next();) {
			for (i = 0; i < colmax; ++i) {
				o = rs.getObject(i + 1); // Is SQL the first column is indexed

				// with 1 not 0
				System.out.print(o.toString() + " ");
			}

			System.out.println(" ");
		}
		st.close();
	}

}
