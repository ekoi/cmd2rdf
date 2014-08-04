package nl.knaw.dans.clarin.playground;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
//import org.rabinfingerprint.fingerprint.RabinFingerprintLong;
//import org.rabinfingerprint.polynomial.Polynomial;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteStreams;
import com.twmacinta.util.MD5;

public class MD5BenchMarkt {
	private static final Logger log = LoggerFactory.getLogger(MD5BenchMarkt.class);
	//private static final Polynomial polynomial = Polynomial.createFromLong(1012200420051966L);
	//private static final Polynomial polynomial = Polynomial.createIrreducible(53);
	public static void main(String[] args) {
		long l = System.currentTimeMillis();
		log.debug("BEGIN OF RABINFINGERPRINT, FAST MD5, APACHE DIGEST, JDK MD5 PERFORMANCE TEST");
		String basefolder = "/Users/akmi/Dropbox/DANS/IN_PROGRESS/CMDI2RDF-Workspace/data/cmd-xml";
		//String basefolder = "/data/cmdi2rdf/resultsets/results/cmdi";
		Collection<File> listFiles = FileUtils.listFiles(new File(basefolder),
				new String[] { "xml" }, true);
		log.debug("Number of files: " + listFiles.size());
//		log.debug("Listing process duration: "
//				+ (System.currentTimeMillis() - l) / 1000 + " seconds.");
		long totalhashingtime = 0;
		int x=0;
		
		
//		for (File file : listFiles) {
//			x++;
//			long a = System.currentTimeMillis();
//			
//			
//			// Create a fingerprint object
//			RabinFingerprintLong rabin = new RabinFingerprintLong(polynomial);
//
//			// Push bytes from a file stream
//			try {
//				rabin.pushBytes(ByteStreams.toByteArray(new FileInputStream(file)));
//				// Get fingerprint value and output
//				long rf = rabin.getFingerprintLong();
//				System.out.println(rf);
//				//String hash = Long.toString(rabin.getFingerprintLong(), 16);
//				//System.out.println(Long.toString(rabin.getFingerprintLong(), 16));
//				totalhashingtime += (System.currentTimeMillis() - a);
//				if (x%10000==0)
//					log.debug("================== x=" + x + "\t duration: " + (totalhashingtime/1000) + " seconds."); 
//			} catch (FileNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//
//			
//		}
		log.debug("RABINFINGERPRINT TIME: " + (totalhashingtime ) + " milliseconds.");
//		log.debug("RABINFINGERPRINT TIME: " + (totalhashingtime / 1000) + " seconds.");
//		log.debug("RABINFINGERPRINT TIME: " + (totalhashingtime / 60000) + " minutes.");
//		log.debug("RABINFINGERPRINT TIME: " + (totalhashingtime / 3600000) + " hours.");
		
		
		totalhashingtime = 0;

		for (File file : listFiles) {
			x++;
			long a = System.currentTimeMillis();
			try {
				String hash = MD5.asHex(MD5.getHash(file));
				//log.debug(x + "\t" + hash + "\t" + ((System.currentTimeMillis() - a)));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			totalhashingtime += (System.currentTimeMillis() - a);
			if (x%1000==0)
				log.debug("================== x=" + x + "\t duration: " + (totalhashingtime/1000) + " seconds."); 
		}
		log.debug("FAST MD5 TIME: " + (totalhashingtime ) + " milliseconds.");
//		log.debug("FAST MD5 TIME: " + (totalhashingtime / 1000) + " seconds.");
//		log.debug("FAST MD5 TIME: " + (totalhashingtime / 60000) + " minutes.");
//		log.debug("FAST MD5 TIME: " + (totalhashingtime / 3600000) + " hours.");
		totalhashingtime = 0;
		
		
		x=0;
		for (File file : listFiles) {
			x++;
			long a = System.currentTimeMillis();
			InputStream is;
			try {
				
				is = new FileInputStream(file);
				String digest = DigestUtils.md5Hex(is);
				is.close();
				//log.debug(x + "\t" + digest + "\t" + ((System.currentTimeMillis() - a)));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			totalhashingtime += (System.currentTimeMillis()-a);
			if (x%1000==0)
				log.debug("================== x=" + x + "\t duration: " + (totalhashingtime/1000) + " seconds."); 
		}
		log.debug("APACHE MD5 TIME: " + (totalhashingtime) + " milliseconds.");
//		log.debug("APACHE MD5 TIME: " + (totalhashingtime / 1000) + " seconds.");
//		log.debug("APACHE MD5 TIME: " + (totalhashingtime / 60000) + " minutes.");
//		log.debug("APACHE MD5 TIME: " + (totalhashingtime / 3600000) + " hours.");
		totalhashingtime = 0;
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Change MD5 to SHA1 to get SHA checksum
		// MessageDigest md = MessageDigest.getInstance("SHA1");
		x=0;
		for (File file : listFiles) {
			x++;
			long a = System.currentTimeMillis();
			FileInputStream fis;
			try {
				fis = new FileInputStream(file);
				byte[] dataBytes = new byte[1024];

				int nread = 0;

				while ((nread = fis.read(dataBytes)) != -1) {
					md.update(dataBytes, 0, nread);
				}
				;

				byte[] mdbytes = md.digest();

				// convert the byte to hex format
				StringBuffer sb = new StringBuffer("");
				for (int i = 0; i < mdbytes.length; i++) {
					sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16)
							.substring(1));
				}
				fis.close();
				String hash = sb.toString();
				//log.debug(x + "\t" + sb + "\t" + ((System.currentTimeMillis() - a)));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			totalhashingtime += (System.currentTimeMillis() - a);
			if (x%1000==0)
				log.debug("================== x=" + x + "\t duration: " + (totalhashingtime/1000) + " seconds."); 
		}
		log.debug("JDK MD5 TIME: " + (totalhashingtime) + " milliseconds.");
//		log.debug("JDK MD5 TIME: " + (totalhashingtime / 1000) + " seconds.");
//		log.debug("JDK MD5 TIME: " + (totalhashingtime / 60000) + " minutes.");
//		log.debug("JDK MD5 TIME: " + (totalhashingtime / 3600000) + " hours.");
	}

}
