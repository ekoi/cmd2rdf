package nl.knaw.dans.clarin.cmd2rdf.akka;

import nl.knaw.dans.clarin.cmd2rdf.akka.message.NumberRangeMessage;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;

public class AkkaConverter
{
    public void execute( long startNumber, long endNumber )
    {
        // Create our ActorSystem, which owns and configures the classes
        ActorSystem actorSystem = ActorSystem.create( "akkaConverter" );

        // Create our listener
        final ActorRef primeListener = actorSystem.actorOf( new Props( ConverterListener.class ), "converterListener" );

        // Create the PrimeMaster: we need to define an UntypedActorFactory so that we can control
        // how PrimeMaster instances are created (pass in the number of workers and listener reference
        ActorRef primeMaster = actorSystem.actorOf( new Props( new UntypedActorFactory() {
            public UntypedActor create() {
                return new ConverterMaster( 20, primeListener );
            }
        }), "converterMaster" );

        // Start the calculation
        primeMaster.tell( new NumberRangeMessage( startNumber, endNumber ) );
    }

    public static void main( String[] args )
    {
	//        if( args.length < 2 )
	//        {
	//            System.out.println( "Usage: java com.geekcap.akka.prime.PrimeCalculator <start-number> <end-number>" );
	//            System.exit( 0 );
	//        }

        long startNumber = 1;//Long.parseLong( args[ 0 ] );
        long endNumber = 10;//Long.parseLong( args[ 1 ] );

        AkkaConverter akkaConverter = new AkkaConverter();
        akkaConverter.execute( startNumber, endNumber );
    }
}
