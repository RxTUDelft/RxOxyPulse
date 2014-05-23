package nl.tudelft.rxcourse.oxypulse;

import rx.Observable;

public class Hello {
	
	public static void main(String args[]) {
		hello("I.", "R.", "Sital");
	}
	
    public static void hello(String... names) {
        Observable.from(names).subscribe(
        	(String name) -> System.out.println("Hello " + name + "!")
        );
    }
}