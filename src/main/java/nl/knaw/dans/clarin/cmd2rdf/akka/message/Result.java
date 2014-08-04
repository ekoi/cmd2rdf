package nl.knaw.dans.clarin.cmd2rdf.akka.message;

import java.util.ArrayList;
import java.util.List;

public class Result
{
    private List<Long> results = new ArrayList<Long>();

    public Result()
    {
    }

    public List<Long> getResults()
    {
        return results;
    }
}
