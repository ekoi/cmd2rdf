package nl.knaw.dans.clarin.playground;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class Repeated {
	public enum Color {
		   WHITE, BLACK, RED, YELLOW, BLUE
		 }
	public static void main( String  [] args ) {
		System.out.println(Color.WHITE.name());
        Collection<String> listOne = Arrays.asList("milan","iga",
                                                  "dingo","iga",
                                                  "elpha","iga",
                                                  "hafil","iga",
                                                  "meat","iga", 
                                                  "neeta.peeta","iga");

        Collection<String> listTwo = Arrays.asList("hafil",
                                                   "iga",
                                                   "binga", 
                                                   "mike", 
                                                   "dingo","dingo","dingo");

        Collection<String> similar = new HashSet<String>( listOne );
        Collection<String> different = new HashSet<String>();
        different.addAll( listOne );
        different.addAll( listTwo );

        similar.retainAll( listTwo );
        different.removeAll( similar );

        System.out.printf("One:%s%nTwo:%s%nSimilar:%s%nDifferent:%s%n", listOne, listTwo, similar, different);
    }
}