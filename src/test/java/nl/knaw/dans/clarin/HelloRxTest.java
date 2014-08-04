package nl.knaw.dans.clarin;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import rx.Observable;
import rx.Observer;
import rx.util.functions.Action1;
import rx.util.functions.Func2;
 
/**
 * @author jbetancourt
 */
public class HelloRxTest {
     
    private Observable<String> observable;
    private Exception thrown;
     
    @Before
    public void before() {
        observable = Observable.from(new String[]{"Hello","world"});
    }
 
    @Test
    public final void test1() {
         
        observable.reduce( new Func2<String, String, String>() {            
            public String call(String t1, String t2) {
                return t1 + " " + t2;
            }
        }).subscribe(new Action1<String>() {
            public void call(String s) {
                String actual = s + "!";
                Assert.assertEquals("Hello world!", actual);
                System.out.println(actual);
            }
        });
    }    
     
    /**
     * Should fail.
     * @throws Exception 
     * 
     */
    @Test(expected=RuntimeException.class)
    public final void test2() throws Exception {
        observable.subscribe(new Observer<String>() {
            StringBuilder buf = new StringBuilder();
     
            public void onCompleted() {
                try {
                    String actual = buf.append("!").toString();
                    Assert.assertEquals("Hello world!", actual);
                } catch (Throwable e) {
                    onError(e);
                }
            }
 
            public void onError(Throwable e){
                thrown =new RuntimeException(e.getMessage(),e);
            }
 
            public void onNext(String args) {
                buf.append(args).append(" ");                
            }
        });
         
        if(null != thrown){
            throw thrown;
        }        
    }
}