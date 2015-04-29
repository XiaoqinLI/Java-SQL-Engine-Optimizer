import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This class run valid SQL queries
 * @author Xiaoqin LI
 *
 */
public class QueryOptimizationExecution {
	private static String outputFileName = "out.tbl";
	private static String compiler = "g++";
	private static String outputLocation = "cppDir/";
	
	private boolean ifGrouped = false; 
	
	Map<String, TableData> dataMap;
	ArrayList<Expression> selectClause;
	Map<String, String> fromClause;
	Expression whereClause;
	ArrayList<String> groupbyClause;
	
	ArrayList<TableModel> tableListFromClause;
	ArrayList<ExpressionWhereModel> expressionListWhereClause;
	ArrayList<Attribute> inAttsList;
    ArrayList<Attribute> outAttsList;
    Map<String,String> exprsMap;
    
	
	
	public QueryOptimizationExecution(Map<String, TableData> data,
			ArrayList<Expression> select, Map<String, String> from,
			Expression where, ArrayList<String> groupby) {
		super();
		this.dataMap = data;
		this.selectClause = select;
		this.fromClause = from;
		this.whereClause = where;
		this.groupbyClause = groupby;
	}
	
	public void execute() {
		// populate input and output attribute Lists and output expression Map based on selectClause
		inAttsList =  new ArrayList<Attribute>(); // new Attribute ("Int", "o_orderkey")
		outAttsList = new ArrayList<Attribute>(); // new Attribute ("Int", "att1")
		exprsMap = new HashMap<String,String>(); // exprs.put ("att1", "o_orderkey");
		for(int i = 0; i < this.selectClause.size(); i++){
			getInAttsList(inAttsList, this.selectClause.get(i));
			outAttsList.add(new Attribute(getExprTypeInSelection(this.selectClause.get(i)), "att" + String.valueOf(i + 1)));
			exprsMap.put("att" + String.valueOf(i + 1), convertExprToStr(this.selectClause.get(i)));
		}
		//For debugging		
//		for(Attribute attri : inAttsList){
//			System.out.println(attri.toString());
//		}
//		for(Attribute attri : outAttsList){
//			System.out.println(attri.toString());
//		}
//		for(Entry<String, String> entry: exprsMap.entrySet()){
//			System.out.println(entry.toString());
//		}
		
		// Populate tableList from fromClause
		tableListFromClause =  getTableListFromClause();
		
		// populate expressionList from whereClause
		expressionListWhereClause = new ArrayList<ExpressionWhereModel>();
		parseWhereClouse(expressionListWhereClause, whereClause); 
		//For debugging		
//		System.out.println();
//		for(ExpressionWhereModel expr: expressionListWhereClause){
//			System.out.println(expr.getExprString());
//		}
		
		// build RA Tree
		RATreeNode rootNode = new RATreeNode(true, false); // starting with a root node.
		rootNode.setSelectListRA(expressionListWhereClause);
		
	}
	
	/*########################################Helper Functions##############################################*/
	/**
	 * Get all selected attributes in SelectClause as input attributes
	 * @param inAtts
	 * @param expression
	 */
	private void getInAttsList(ArrayList<Attribute> inAtts, Expression expression){
    	if(!expression.getSubexpression().getType().equals("nonUnaryTypes"))
    		getInAttsList(inAtts, expression.getSubexpression());
    	
    	if(!expression.getSubexpression("left").getType().equals("nonBinaryTypes"))
    		getInAttsList(inAtts, expression.getLeftSubexpression());
    	
    	if(!expression.getSubexpression("right").getType().equals("nonBinaryTypes"))
    		getInAttsList(inAtts, expression.getRightSubexpression());	
    	
    	if(expression.getType().equals("identifier"))
			inAtts.add(new Attribute(this.getExprTypeInSelection(expression), expression.getValue()));
	}
	
	
	/**
	 * Get the type from attribute in select clause based on catalog information
	 * @param expression
	 * @return
	 */
	private String getExprTypeInSelection(Expression expression){	
		String expressionType = expression.getType();
		
		if(expressionType.equals("literal int")){
			return "Int";}
		if(expressionType.equals("literal float")){
			return "Float";}
		if(expressionType.equals("literal string")){
			return "Str";}
		
		//Identifier
		if (expressionType.equals("identifier")){
			String expressionValue = expression.getValue();
			String alias = expressionValue.substring(0, expressionValue.indexOf("."));	
			String attributeName = expressionValue.substring(expressionValue.indexOf(".") + 1);
			String expressionTypeInSelection = this.dataMap.get(this.fromClause.get(alias)).getAttInfo(attributeName).getDataType();
			return expressionTypeInSelection;
		}

		// "+ - * /"
		if (expressionType.equals("plus") || expressionType.equals("minus") || expressionType.equals("times") || expressionType.equals("divided by"))
			return this.getExprTypeInSelection(expression.getLeftSubexpression());

		//"sum, avg, unary minus", "not" shouldn't be in select clause
		if (expressionType.equals("sum") || expressionType.equals("avg") || expressionType.equals("unary minus")){
			if(expressionType.equals("sum") || expressionType.equals("avg")) {
				this.ifGrouped = true;
			}
			return this.getExprTypeInSelection(expression.getSubexpression());
		}

//		System.err.println("Optimizer.getSelectExpTypeRec: input expression: "+ expression +" is of invalid type! ");
		return "Unknown";
		
	}
	
	
	/**
	 * Convert a expression to a valid string
	 * @param expr
	 * @return
	 */
	private String convertExprToStr(Expression expression){
		String expressionType = expression.getType();
		
		if(expressionType.equals("identifier")){
			return expression.getValue();}
		
		if(expressionType.equals("literal string")){
			return "Str (" + expression.getValue() + ")";}
    	if(expressionType.equals("literal float")){  
    		return "Float (" + expression.getValue() + ")";}
    	if(expressionType.equals("literal int")){
    		return "Int (" + expression.getValue() + ")";}
    	
    	if(expressionType.equals("greater than")){
    		return "(" + convertExprToStr(expression.getLeftSubexpression()) + " > "
    				   + convertExprToStr(expression.getRightSubexpression()) + ")";}
    	if(expressionType.equals("less than")){
    		return "(" + convertExprToStr(expression.getLeftSubexpression()) + " < "
    				   + convertExprToStr(expression.getRightSubexpression()) + ")";} 	
    	if(expressionType.equals("or")){
    		return "(" + convertExprToStr(expression.getLeftSubexpression()) + " || "
    				   + convertExprToStr(expression.getRightSubexpression()) + ")";}	
    	if(expressionType.equals("equals")){
    		return "(" + convertExprToStr(expression.getLeftSubexpression()) + " == "
    				   + convertExprToStr(expression.getRightSubexpression()) + ")";}
//    	if(expressionType.equals("and")){
//    		return "(" + convertExprToStr(expression.getLeftSubexpression()) + " && "
//    				   + convertExprToStr(expression.getRightSubexpression()) + ")";}	
    	
    	if(expressionType.equals("plus")){
    		return "(" + convertExprToStr(expression.getLeftSubexpression()) + " + "
    				   + convertExprToStr(expression.getRightSubexpression()) + ")";}
    	if(expressionType.equals("minus")){
    		return "(" + convertExprToStr(expression.getLeftSubexpression()) + " - "
    				   + convertExprToStr(expression.getRightSubexpression()) + ")";}
    	if(expressionType.equals("times")){
    		return "(" + convertExprToStr(expression.getLeftSubexpression()) + " * "
    				   + convertExprToStr(expression.getRightSubexpression()) + ")";}
    	if(expressionType.equals("divided by")){
    		return "(" + convertExprToStr(expression.getLeftSubexpression()) + " / "
    				   + convertExprToStr(expression.getRightSubexpression()) + ")";}
    	
    	if (expressionType.equals("unary minus")){
    		return "-(" + convertExprToStr(expression.getSubexpression()) +")";}
    	if (expressionType.equals("not")){
    		return "!(" + convertExprToStr(expression.getSubexpression()) + ")";}
    	if (expressionType.equals("sum")){
    		return convertExprToStr(expression.getSubexpression());}
    	if (expressionType.equals("avg")){
    		return convertExprToStr(expression.getSubexpression());}
    	
    	return "Unknown";
	}
	
	
	/**
	 * populate table list based on fromClause.
	 * @return
	 */
	private ArrayList<TableModel> getTableListFromClause(){
		ArrayList<TableModel> tempTableList = new ArrayList<TableModel>();
		
		for(String currentAlias : fromClause.keySet()){
			// create a tablemodel to store current table's info.
			String currentTableName = fromClause.get(currentAlias);
			TableModel currentTable = new TableModel(currentTableName, currentAlias);
			// populate attribute list
			Map<String, AttInfo> allAttributesInfo = dataMap.get(currentTableName).getAttributes();
			for(int i = 0; i<allAttributesInfo.size(); i++){
				for(Entry<String, AttInfo> entry: allAttributesInfo.entrySet()){
					if(entry.getValue().getAttSequenceNumber() == i){
						currentTable.getAttributeList().add(new Attribute(entry.getValue().getDataType(), entry.getKey()));
						break;
					}
				}
			}
			tempTableList.add(currentTable);
		}

		return tempTableList;	
	}
	
	
	/**
	 * Parse whereClause, and populate constraintListWhereList.
	 * @param constraintListWhere
	 * @param where
	 */
	private void parseWhereClouse(ArrayList<ExpressionWhereModel> expressionListWhere, Expression where){
		String expressionType = where.getType();
		if(expressionType.equals("and")){
			parseWhereClouse(expressionListWhere,where.getSubexpression("left"));
			parseWhereClouse(expressionListWhere,where.getSubexpression("right"));
		}else{
			String expressionString = convertExprToStr(where);
			ExpressionWhereModel expressionWhere = new ExpressionWhereModel(expressionString,expressionType);
//			convertExp2CondRec(expressionWhere,where);
			expressionListWhere.add(expressionWhere);
		}	
	}
	
	

}
