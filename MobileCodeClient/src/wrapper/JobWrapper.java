package wrapper;

import java.io.Serializable;

public class JobWrapper implements Serializable {

	private static final long serialVersionUID = -6234953335026975826L;

	private String fileName;

	private String binaryClassName;

	/** The java-code, either source- or byte-code */
	private char[] code;

	private MethodWrapper[] methodCalls;
	
	private int billingUnits;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getBinaryClassName() {
		return binaryClassName;
	}

	public void setBinaryClassName(String binaryClassName) {
		this.binaryClassName = binaryClassName;
	}

	public MethodWrapper[] getMethodCalls() {
		return methodCalls;
	}

	public void setMethodCalls(MethodWrapper[] methodCalls) {
		this.methodCalls = methodCalls;
	}

	public char[] getCode() {
		return code;
	}

	public void setCode(char[] code) {
		this.code = code;
	}

	public int getBillingUnits() {
		return billingUnits;
	}

	public void setBillingUnits(int billingUnits) {
		this.billingUnits = billingUnits;
	}

}
