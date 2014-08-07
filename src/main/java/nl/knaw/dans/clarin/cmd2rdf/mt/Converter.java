package nl.knaw.dans.clarin.cmd2rdf.mt;

import java.io.ByteArrayOutputStream;

import nl.knaw.dans.clarin.cmd2rdf.exception.ConverterException;

public interface Converter {
	public void startUp() throws ConverterException;
	public ByteArrayOutputStream transform(Object object); 
}
