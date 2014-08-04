package nl.knaw.dans.clarin.cmd2rdf.akka;

import java.io.Serializable;

public class HelloMessage implements Serializable
{
    private String message;

    public HelloMessage(String message)
    {
        this.message = message;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }
}