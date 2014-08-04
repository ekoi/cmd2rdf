package nl.knaw.dans.clarin.playground;

import java.io.FileInputStream;
import java.security.MessageDigest;
 
public class CheckSum {
 
  public static void main(String args[]) throws Exception {
 
    String datafile = "CheckSum.class";
 
    MessageDigest md = MessageDigest.getInstance("MD5");
    // Change MD5 to SHA1 to get SHA checksum
    // MessageDigest md = MessageDigest.getInstance("SHA1");
 
    FileInputStream fis = new FileInputStream(datafile);
    byte[] dataBytes = new byte[1024];
 
    int nread = 0; 
 
    while ((nread = fis.read(dataBytes)) != -1) {
      md.update(dataBytes, 0, nread);
    };
 
    byte[] mdbytes = md.digest();
 
    //convert the byte to hex format
    StringBuffer sb = new StringBuffer("");
    for (int i = 0; i < mdbytes.length; i++) {
    	sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
    }
 
    System.out.println(sb.toString() + " " + datafile);
  }
}
 