package nl.knaw.dans.clarin.cmd2rdf.mt;

import java.util.List;
import java.util.Map;

import nl.knaw.dans.clarin.cmd2rdf.batch.Property;
import nl.knaw.dans.clarin.cmd2rdf.exception.ActionException;

public interface IAction {
	public void startUp(Map<String, String> vars) throws ActionException;
	public Object execute(String path,Object object) throws ActionException;
	public void shutDown() throws ActionException;
}
