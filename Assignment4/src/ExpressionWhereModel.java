
public class ExpressionWhereModel {
	private String exprString;
	private String exprType;
	public ExpressionWhereModel(String exprString, String exprType) {
		super();
		this.exprString = exprString;
		this.exprType = exprType;
	}
	public String getExprString() {
		return exprString;
	}
	private void setExprString(String exprString) {
		this.exprString = exprString;
	}
	public String getExprType() {
		return exprType;
	}
	private void setExprType(String exprType) {
		this.exprType = exprType;
	}
	
}
