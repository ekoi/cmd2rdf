package nl.knaw.dans.clarin.cmd2rdf.mt;

import java.io.ByteArrayOutputStream;

public interface Converter {
	public ByteArrayOutputStream transform(Object object); 
}
