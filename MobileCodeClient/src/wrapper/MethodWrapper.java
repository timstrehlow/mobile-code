package wrapper;

import java.io.Serializable;

public class MethodWrapper implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8614259735196318269L;

	/** Name of the method to invoke */
	private String name;

	/** Object to call the method on, if method is not static (optional) */
	private Object instance;

	/** Arguments passed to the method (optional) */
	private Object[] args;

	/** Return value of the method (optional) */
	private Object result;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Object getInstance() {
		return instance;
	}

	public void setInstance(Object instance) {
		this.instance = instance;
	}

	public Object[] getArgs() {
		return args;
	}

	public void setArgs(Object[] args) {
		this.args = args;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}
}
