/**
 * 
 */
package nl.knaw.dans.clarin.cmd2rdf.store;


/**
 * @author Eko Indarto
 *
 */
public abstract class RdfStore {
	private String serverURL;
	private int port;
	private String path;
	private String username;
	private String password;
	
	protected RdfStore(String serverURL, int port, String path, String username, String password){
		this.serverURL = serverURL;
		this.path = path;
		this.port = port;
		this.username = username;
		this.password = password;
	}
	
	protected RdfStore(String serverURL, String username, String password){
		this.serverURL = serverURL;
		this.username = username;
		this.password = password;
	}
	
	protected String getServerURL() {
		return serverURL;
	}
	protected void setServerURL(String serverURL) {
		this.serverURL = serverURL;
	}
	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	protected String getPath() {
		return path;
	}
	protected void setPath(String path) {
		this.path = path;
	}
	protected String getUsername() {
		return username;
	}
	protected void setUsername(String username) {
		this.username = username;
	}
	protected String getPassword() {
		return password;
	}
	protected void setPassword(String password) {
		this.password = password;
	}
	
}
