package server;

import java.io.Serializable;

public class JobWrapper implements Serializable {

	private static final long serialVersionUID = -6234953335026975826L;

	private String code;
	private String methodToExecute;
	private Object instance;
	private Object[] args;
	private Object[] results;
	private int billingUnits;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMethodToExecute() {
		return methodToExecute;
	}

	public void setMethodToExecute(String methodToExecute) {
		this.methodToExecute = methodToExecute;
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

	public Object[] getResults() {
		return results;
	}

	public void setResults(Object[] results) {
		this.results = results;
	}

	public int getBillingUnits() {
		return billingUnits;
	}

	public void setBillingUnits(int billingUnits) {
		this.billingUnits = billingUnits;
	}

}
