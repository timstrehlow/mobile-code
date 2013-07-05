import de.tu_berlin.kbs.reflect.InvokeThis;

public class HelloWorld {
	
	public String doSomething() {
		return "doSomething";
	}
	
	public String doNothing() {
		return "unauthorized method call";
	}
	
	@InvokeThis
	public static String doSomethingStatic() {
		return "doSomething static";
	}
	
	public static String doNothingStatic() {
		return "unauthorized static method call";
	}
}