/*
 * this model record the type and each Expression element and whether it is valid in 
 * corresponding operation
 * expType:-1 - null or invalid attribute
 * 			0 - CNF(and/or)Expression type
 * 			1 - String type attribute
 * 			2 - int or float attribute
 * isValid: boolean
 */

public class ExpressionIsTypeValid {
	private int expType;
	private boolean isTypeValid;
	
	public ExpressionIsTypeValid(int type, boolean isValid){
		this.expType = type;
		this.isTypeValid = isValid;			
	}

	public int getExpType() {
		return expType;
	}
	
	public boolean isTypeValid() {
		return isTypeValid;
	}

	protected void setExpType(int expType) {
		this.expType = expType;
	}

	protected void setTypeValid(boolean isTypeValid) {
		this.isTypeValid = isTypeValid;
	}
	
}
