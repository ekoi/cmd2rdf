package nl.knaw.dans.clarin.cmd2rdf.store.db;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import nl.knaw.dans.clarin.cmd2rdf.util.ActionStatus;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.twmacinta.util.MD5;

public class ChecksumDb {
	private static final String TABLE_NAME = "CMD_MD5";
	private static final String UPDATE_PREPARED_STATEMENT = "UPDATE " + TABLE_NAME + " SET md5 = ?, action=? WHERE path = ?";
	private static final String INSERT_PREPARED_STATEMENT = "INSERT INTO " + TABLE_NAME + "(path, md5, action) VALUES(?,?,?)";
	private static final String NEW_RECORD_QUERY = "SELECT path FROM " + TABLE_NAME + " WHERE action='" + ActionStatus.NEW + "'";
	private static final String UPDATED_RECORD_QUERY = "SELECT path FROM " + TABLE_NAME + " WHERE action='" + ActionStatus.UPDATE + "'";
	private static final String NEW_OR_UPDATED_RECORD_QUERY = "SELECT path FROM " + TABLE_NAME + " WHERE action='" + ActionStatus.NEW + "' OR action ='" + ActionStatus.UPDATE + "'";
	
	private static boolean initialdata = false;
	private static long totalQueryDuration;
	private static long totalMD5GeneratedTime;
	private static long totalDbProcessingTime;
	private static final Logger log = LoggerFactory.getLogger(ChecksumDb.class);
	public static final String DB_NAME = "db_cmd_md5";
	public static final String QUERY = "";
	public static final int COL_CHECKSUM_MAX_LENGTH = 1024;
	public static final int COL_PATH_MAX_LENGTH = 256;
	public static final int COL_ACTION_MAX_LENTH = 10;
	
	
    Connection conn;
    
    public ChecksumDb(String db_file_name_prefix){ 
    	init(db_file_name_prefix);  
    }

	private void init(String db_file_name_prefix){
		try {
			Class.forName("org.hsqldb.jdbcDriver");
			conn = DriverManager.getConnection("jdbc:hsqldb:"
                    + db_file_name_prefix,    
                    "sa",                     
                    "");
			ResultSet rs = conn.getMetaData().getTables(null,null,TABLE_NAME,new String[]{"TABLE"});
			if (rs.next()) {
				initialdata = true;
			}
			if (!initialdata)
				createTable();
			else {
				log.debug("TABLE EXIST, table name: " + TABLE_NAME);
				log.debug("Total records of " + TABLE_NAME + " table: " + getTotalNumberOfRecords());
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}

        
	}
	
	public void process(String basefolder, Collection<File> files) throws IOException, SQLException {
		if (!initialdata)
			initRecords(files);
		else
			checkAndstore(files);
	}

	public void updateStatus(ActionStatus as) {
		try {
			update("UPDATE " + TABLE_NAME + " SET action=' " + as.name() + "' " 
					+ "WHERE action = '" + ActionStatus.NEW.name() + "' " 
							+ "OR action='" + ActionStatus.UPDATE.name() +"'");
			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
	}    
	
	private void createTable() throws SQLException {
		//update("DROP TABLE CMD_MD5 IF EXISTS");
		//conn.commit();
		log.debug("CREATE A NEW TABLE, table name: " + TABLE_NAME);
		update(
                "CREATE TABLE " + TABLE_NAME 
                + "( id INTEGER IDENTITY, path VARCHAR(" + COL_CHECKSUM_MAX_LENGTH + ") UNIQUE, "
                + "md5 VARCHAR(" + COL_CHECKSUM_MAX_LENGTH + "), action VARCHAR(" + COL_ACTION_MAX_LENTH + "))");
		update("CREATE INDEX path_idx ON " + TABLE_NAME + "(path)");
        update("CREATE INDEX md5_idx ON " + TABLE_NAME + "(md5)");
        conn.setAutoCommit(false);
	}

    public void shutdown(){
		try {
			Statement st = conn.createStatement();
			st.execute("SHUTDOWN");
	        conn.close();  
		} catch (SQLException e) {
			e.printStackTrace();
		}
    }

    private void update(String expression) throws SQLException {

        Statement st = conn.createStatement();   

        int i = st.executeUpdate(expression); 

        if (i == -1) {
            log.debug("db error : " + expression);
        }

        st.close();
    }    
    
    public List<String> getRecords(ActionStatus as) {
    	List<String> paths = new ArrayList<String>();
    	try {
    	
    	switch (as) {
	    		case NEW: paths=getNewRecords();
	    			break;
	    		case UPDATE: paths = getUpdatedRecords();
	    			break;
	    		case NEW_UPDATE: paths = getNewOrUpdatedRecords();
	    			break;
			default:
				paths = getNewOrUpdatedRecords();
				break;
	    	}
    	}catch (SQLException e) {
    		e.printStackTrace();
    	}
    	return paths;
    }
    
    public List<String> getNewRecords() throws SQLException {
    	List<String> paths = new ArrayList<String>();
    	long t = System.currentTimeMillis();
        Statement st = conn.createStatement();        
        ResultSet rs = st.executeQuery(NEW_RECORD_QUERY);    
        for (; rs.next(); ) {
        	String path = rs.getString("path"); 
        	paths.add(path);
        }
        st.close(); 
        long duration = (System.currentTimeMillis()-t);
        log.debug("Checksum query NEW_RECORD_QUERY needs " + duration + " milliseconds.");
    	return paths;
    }
    public List<String> getUpdatedRecords() throws SQLException {
    	List<String> paths = new ArrayList<String>();
    	long t = System.currentTimeMillis();
        Statement st = conn.createStatement();        
        ResultSet rs = st.executeQuery(UPDATED_RECORD_QUERY);    
        for (; rs.next(); ) {
        	String path = rs.getString("path"); 
        	paths.add(path);
        }
        st.close(); 
        long duration = (System.currentTimeMillis()-t);
        log.debug("Checksum query UPDATED_RECORD_QUERY needs " + duration + " milliseconds.");
    	return paths;
    }
    public List<String> getNewOrUpdatedRecords() throws SQLException {
    	List<String> paths = new ArrayList<String>();
    	long t = System.currentTimeMillis();
        Statement st = conn.createStatement();        
        ResultSet rs = st.executeQuery(NEW_OR_UPDATED_RECORD_QUERY);    
        for (; rs.next(); ) {
        	String path = rs.getString("path"); 
        	paths.add(path);
        }
        st.close(); 
        long duration = (System.currentTimeMillis()-t);
        log.debug("Checksum query NEW_OR_UPDATED_RECORD_QUERY needs " + duration + " milliseconds.");
    	return paths;
    }
    
    private String getChecksumRecord(String path) throws SQLException {
    	String checksum = null;
    	long t = System.currentTimeMillis();
        Statement st = conn.createStatement();        
        ResultSet rs = st.executeQuery("SELECT md5 FROM " + TABLE_NAME + " WHERE path = '" + path + "'");//TODO: EKO YOU MUST CHECK THIS QUERY!!!    
        for (; rs.next(); ) {
        	checksum = rs.getString("md5"); 
        }
        st.close(); 
        long duration = (System.currentTimeMillis()-t);
        totalQueryDuration += duration;
        //log.debug("Checksum query needs " + duration + " milliseconds.");
        return checksum;
    }
    
    public int getTotalNumberOfRecords() throws SQLException {
    	int total = 0;

        Statement st = conn.createStatement();        
        ResultSet rs = st.executeQuery("SELECT count(*) AS total FROM " + TABLE_NAME + "");    
        for (; rs.next(); ) {
        	total = rs.getInt("total"); 
        }
        st.close();   
        return total;
    }    
    
    public int getTotalNumberOfNewRecords() throws SQLException {
    	int total = 0;
    	
        Statement st = conn.createStatement();        
        ResultSet rs = st.executeQuery("SELECT count(*) AS total FROM " + TABLE_NAME + " WHERE action='" + ActionStatus.NEW.name() + "'");    
        for (; rs.next(); ) {
        	total = rs.getInt("total"); 
        }
        st.close();   
        return total;
    }    
    public int getTotalNumberOfUpdatedRecords() throws SQLException {
    	int total = 0;
    	
        Statement st = conn.createStatement();        
        ResultSet rs = st.executeQuery("SELECT count(*) AS total FROM " + TABLE_NAME + " WHERE action='" + ActionStatus.UPDATE.name() + "'");    
        for (; rs.next(); ) {
        	total = rs.getInt("total"); 
        }
        st.close();   
        return total;
    }     
    
    public int getTotalNumberOfDoneRecords() throws SQLException {
    	int total = 0;
        Statement st = conn.createStatement();        
        ResultSet rs = st.executeQuery("SELECT count(*) AS total FROM " + TABLE_NAME + " WHERE action='" + ActionStatus.DONE.name() + "'");    
        for (; rs.next(); ) {
        	total = rs.getInt("total"); 
        }
        st.close();   
        return total;
    }     
    
    public int getTotalNumberOfNoneRecords() throws SQLException {
    	int total = 0;
        Statement st = conn.createStatement();        
        ResultSet rs = st.executeQuery("SELECT count(*) AS total FROM " + TABLE_NAME + " WHERE action='" + ActionStatus.NONE.name() + "'");    
        for (; rs.next(); ) {
        	total = rs.getInt("total"); 
        }
        st.close();   
        return total;
    }  
    
    public void printAll() throws SQLException {
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM " + TABLE_NAME + "");    
        ResultSetMetaData meta   = rs.getMetaData();
        int               colmax = meta.getColumnCount();
        int               i;
        Object            o = null;
        st = conn.createStatement();        
        
        for (; rs.next(); ) {
            for (i = 0; i < colmax; ++i) {
                o = rs.getObject(i + 1);    // Is SQL the first column is indexed
                // with 1 not 0
                System.out.print(o.toString() + " ");
            }

            log.debug(" ");
        }
        st.close();  
    }
    
    private void initRecords(Collection<File> files) throws IOException{
    	log.debug("GENERATE MD5 for [generateApacheMD5Checksum(file)] " + files.size() + " files.");
        try {
        	long t = System.currentTimeMillis();
            log.debug("Generate MD5 Checksum of " + files.size() + " files.");
            PreparedStatement psInsert = conn.prepareStatement(INSERT_PREPARED_STATEMENT);
            int nRecords = 0;
            int nInsert = 0;
            long totalhashingtime = 0;
            long totaldatabaseprocessingtime = 0;
            for (File file : files) {
            	nRecords++;
            	long a = System.currentTimeMillis();
        		String hash = generateFastMD5Checksum(file);
        		//String hash = generateApacheMD5Checksum(file);
        		totalhashingtime += (System.currentTimeMillis()-a);
        		nInsert++;
        		setInsertedRecord(psInsert, hash,  file.getAbsolutePath());
            	if (nInsert%10000 ==0) {
                	 totaldatabaseprocessingtime += commitRecords(
							psInsert, nInsert, "Inserting");
                }
            	if (nRecords%10000 == 0) {
            		writeLog(t, nRecords, totalhashingtime,
							totaldatabaseprocessingtime);
            	}
                
            }
            
            if (nInsert%10000 != 0) {
            	totaldatabaseprocessingtime += commitRecords(
						psInsert, nInsert, "Inserting");
            }
            writeLog(t, nRecords, totalhashingtime,
					totaldatabaseprocessingtime);
            
        } catch (SQLException e) {
        	log.error("ERROR checkAndstore: " + e.getMessage());
        } 
    }
    
    private String generateApacheMD5Checksum(File file) throws IOException {
    	long t = System.currentTimeMillis();
    	InputStream is = new FileInputStream(file);
		String digest = DigestUtils.md5Hex(is);
		is.close();
		long duration = (System.currentTimeMillis()-t);
		totalMD5GeneratedTime+=duration;
		return digest;
    }
    
    private String generateFastMD5Checksum(File file) throws IOException{
    	long t = System.currentTimeMillis();
    	String hash = MD5.asHex(MD5.getHash(file));
		long duration = (System.currentTimeMillis()-t);
		totalMD5GeneratedTime+=duration;
		return hash;
    }
    
    private void checkAndstore(Collection<File> files) throws IOException {
    	log.debug("CHECK AND GENERATE MD5 [generateApacheMD5Checksum(file)] for " + files.size() + " files.");
        try {
        	long t = System.currentTimeMillis();
            log.debug("Generate MD5 Checksum of " + files.size() + " files.");
            PreparedStatement psInsert = conn.prepareStatement(INSERT_PREPARED_STATEMENT);
            PreparedStatement psUpdate = conn.prepareStatement(UPDATE_PREPARED_STATEMENT);
            
            int nRecords = 0;
            int nInsert = 0;
            int nUpdate = 0;
            int nSkip = 0;
            long totalhashingtime = 0;
            long totaldatabaseprocessingtime = 0;
            for (File file : files) {
            	nRecords++;
            	long a = System.currentTimeMillis();
            	String hash = MD5.asHex(MD5.getHash(file));
            	//String hash = generateApacheMD5Checksum(file);
        		totalhashingtime += (System.currentTimeMillis()-a);
        		String path = file.getAbsolutePath();
            	String checksum = getChecksumRecord(path);
            	if (checksum == null) {
            		nInsert++;
            		setInsertedRecord(psInsert, hash, path);
                	if (nInsert%10000 ==0) {
                    	 totaldatabaseprocessingtime += commitRecords(
								psInsert, nInsert, "Inserting 10.000");
                    	 
                    }
            	} else {
	            	if (!checksum.equals(hash)) {
	            			nUpdate++;
	            			setRecord(psUpdate, hash, path, ActionStatus.UPDATE);
	            			if (nUpdate%10000 ==0) {
		                    	 totaldatabaseprocessingtime += commitRecords(
										psUpdate, nUpdate,
										"Updating 10.000");
		                    }
	            		} else {
	            			nSkip++;
	            			//setRecord(psUpdate, hash, path, ActionStatus.NONE);
	            			if (nSkip%10000==0){
	            				 totaldatabaseprocessingtime += commitRecords(
										psUpdate, nUpdate,
										"Skipping 10.000");
	            				
	            			}
	            		}
            	}
            	if (nRecords%100000 == 0) {
            		writeLog(t, nRecords, totalhashingtime,
							totaldatabaseprocessingtime);
            	}
            	
            }
            
            if (nInsert%10000 != 0) {
            	totaldatabaseprocessingtime += commitRecords(
						psInsert, nInsert, "Inserting " + nInsert%10000);
            }
            if (nUpdate%10000 != 0) {
            	totaldatabaseprocessingtime += commitRecords(
						psUpdate, nUpdate, "Updating " + nUpdate%10000);
            }
            
            if (nSkip%10000 != 0) {
            	totaldatabaseprocessingtime += commitRecords(
            			psUpdate, nSkip, "Skipping " + nSkip%10000);
            }
            
//            update("DELETE FROM " + TABLE_NAME + " WHERE action = '" + ActionStatus.DONE.name() + "'");
//            conn.commit();
            writeLog(t, nRecords, totalhashingtime,
					totaldatabaseprocessingtime);
        } catch (SQLException e) {
        	log.error("ERROR checkAndstore: " + e.getMessage());
        } 
    }

	private long commitRecords(PreparedStatement ps, int nRecs, String msg) throws SQLException {
		long t = System.currentTimeMillis();
		 ps.executeBatch();
		 conn.commit();
		 long dbprocessingtime = (System.currentTimeMillis() - t);
		 totalDbProcessingTime+=dbprocessingtime;
		 log.debug(msg + " is done in " + dbprocessingtime + " milliseconds.");
		 log.debug("Total number of committed records: " + nRecs);
		return dbprocessingtime;
	}

	
	private void writeLog(long t, int nRecords, long totalhashingtime,
			long totaldatabaseprocessingtime) {
		log.debug("Total number of records: " + nRecords);
		log.debug("Total duration of md5 process is  " + (totalhashingtime/1000)  + " seconds and " + totalhashingtime%1000 + " milliseconds");
		log.debug("Total duration of database process is  " + (totaldatabaseprocessingtime/1000) + " seconds and " + totaldatabaseprocessingtime%1000 + " milliseconds");
		log.debug("Total process time is  " + ((System.currentTimeMillis() - t)/1000)+ " seconds.");
	}

	private void setInsertedRecord(PreparedStatement psInsert, String hash,
			String path) throws SQLException {
		psInsert.setString(1, path);
		psInsert.setString(2, hash);
		psInsert.setString(3, ActionStatus.NEW.name());
		psInsert.addBatch();
	}
	
	private void setRecord(PreparedStatement ps, String hash,
			String path, ActionStatus action) throws SQLException {
		ps.setString(1, hash);
		ps.setString(2, action.name());
		ps.setString(3, path);
		ps.addBatch();
	}
	
	public static long getTotalQueryDuration() {
		return totalQueryDuration;
	}

	public static long getTotalMD5GeneratedTime() {
		return totalMD5GeneratedTime;
	}

	public static long getTotalDbProcessingTime() {
		return totalDbProcessingTime;
	}
    
}   
