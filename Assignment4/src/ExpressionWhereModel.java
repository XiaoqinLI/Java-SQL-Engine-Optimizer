import java.util.ArrayList;


/**
 * Expression Model for elemental clause separated by "and" in Where Clause
 * @author Xiaoqin LI
 *
 */
public class ExpressionWhereModel {
	private String exprString = "";
	private String exprType = "";
	
	private ArrayList<String> aliasesList; //The aliasesList of the elemental clause in WHERE
	private ArrayList<String> attributesList; //The attributesList of the elemental clause in WHERE
	
	public ExpressionWhereModel(String exprString, String exprType) {
		super();
		this.exprString = exprString;
		this.exprType = exprType;
		
		this.aliasesList =  new ArrayList<String>();
		this.attributesList =  new ArrayList<String>();
	}
	
	public String getExprString() {return exprString;}
	public void setExprString(String exprString) {this.exprString = exprString;}
	
	public String getExprType() {return exprType;}
	public void setExprType(String exprType) {this.exprType = exprType;}

	public ArrayList<String> getAliasesList() {return aliasesList;}
	public void setAliasesList(ArrayList<String> aliasesList) {this.aliasesList = aliasesList;}

	public ArrayList<String> getAttributesList() {return attributesList;}
	public void setAttributesList(ArrayList<String> attributesList) {this.attributesList = attributesList;}
	
}
