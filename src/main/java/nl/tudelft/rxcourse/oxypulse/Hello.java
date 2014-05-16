package nl.tudelft.rxcourse.oxypulse;

import rx.Observable;
import rx.functions.Action1;

public class Hello {
	
	public static void main(String args[]) {
		hello("I.", "R.", "Sital");
	}
	
    public static void hello(String... names) {
        Observable.from(names).subscribe(new Action1<String>() {

            @Override
            public void call(String s) {
                System.out.println("Hello " + s + "!");
            }

        });
    }
}
