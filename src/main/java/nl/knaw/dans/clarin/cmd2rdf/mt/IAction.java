package nl.knaw.dans.clarin.cmd2rdf.mt;

import java.io.ByteArrayOutputStream;

import nl.knaw.dans.clarin.cmd2rdf.exception.ConverterException;

public interface IAction {
	public void startUp() throws ConverterException;
	public ByteArrayOutputStream execute(Object object);
	public void shutDown() throws ConverterException;
}
