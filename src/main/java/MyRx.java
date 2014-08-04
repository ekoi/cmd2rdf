import rx.Observable;
import rx.functions.Action1;


public class MyRx {
	
	public static void main(String[] args) {
		Observable<String> observable = Observable.from(new String[]{"Hello","world"});
		observable.subscribe(new Action1<String>() {
            public void call(String s) {
                String actual = s + "!";
                System.out.println(actual);
            }
	});

}}
