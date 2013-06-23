package wrapper;

import java.io.Serializable;

public class JobWrapper implements Serializable {

	private static final long serialVersionUID = -6234953335026975826L;

	private String fileName;

	/** The java-code, either source- or byte-code */
	private String code;

	private MethodWrapper[] methodCalls;
	
	private int billingUnits;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public MethodWrapper[] getMethodCalls() {
		return methodCalls;
	}

	public void setMethodCalls(MethodWrapper[] methodCalls) {
		this.methodCalls = methodCalls;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public int getBillingUnits() {
		return billingUnits;
	}

	public void setBillingUnits(int billingUnits) {
		this.billingUnits = billingUnits;
	}

}
