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
	
	private boolean ifGrouped = false; 
	
	Map<String, TableData> dataMap;
	ArrayList<Expression> selectClause;
	Map<String, String> fromClause;
	Expression whereClause;
	ArrayList<String> groupbyClause;
	
	ArrayList<TableModel> tableListFromClause;
	ArrayList<ExpressionWhereModel> expressionListWhereClause;
	ArrayList<Attribute> selectedAttsList;
    ArrayList<Attribute> projectedAttsList;
    Map<String,String> exprsMap;
    
    RATreeNode rootNodeRA;
    
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
	
	
	/**
	 * The main function that initialize SQL engine
	 */
	public void execute() {
		// populate input and output attribute Lists and output expression Map based on SQL selectClause
		selectedAttsList =  new ArrayList<Attribute>(); // new Attribute ("Int", "o_orderkey")
		projectedAttsList = new ArrayList<Attribute>(); // new Attribute ("Int", "att1")
		exprsMap = new HashMap<String,String>(); // exprs.put ("att1", "o_orderkey");
		for(int i = 0; i < this.selectClause.size(); i++){
			getInAttsList(selectedAttsList, this.selectClause.get(i));
			projectedAttsList.add(new Attribute(getExprTypeInSelection(this.selectClause.get(i)), "att" + String.valueOf(i + 1)));
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
		
		// Populate tableList from SQL fromClause
		tableListFromClause =  getTableListFromClause();
		
		// populate expressionList from SQL whereClause
		expressionListWhereClause = new ArrayList<ExpressionWhereModel>();
		parseWhereClouse(expressionListWhereClause, whereClause); 
		//For debugging		
//		System.out.println();
//		for(ExpressionWhereModel expr: expressionListWhereClause){
//			System.out.println(expr.getExprString());
//		}
		
		// build the RA Tree
		rootNodeRA = createRATree();
	    aggregateAliasesToAllNonLeafNodes(rootNodeRA);
	    
	    // optimize the RA tree
	    // to be continue...
	    
	    // Execute Query
	    System.out.println();
//	    executeQuery(rootNodeRA);
	    
	}
	
	/******************************************Execution Functions************************************/
	/**
	 * main SQL query execute
	 */
	private void executeQuery(RATreeNode rootNode){
		
		if(rootNode.isSingle()){
//			if(this.ifGrouped)
//				rootNode.setTable(this.executeSelect(rootNode.getCond_list(),rootNode.getRequiredAtts(),rootNode.getlNode().getTable(),null));
//			else
			rootNode.setTable(executeSelect(rootNode.getSelectListRA(), rootNode.getInAttsList(),rootNode.getExprsMap(), rootNode.getLeftNode().getTable() ));
			return;
		}
		
	}
	
	private TableModel executeSelect(ArrayList<ExpressionWhereModel> selectRAList, ArrayList<String> requiredAtts, Map<String,String> expressionMap, TableModel nodeTable){
		ArrayList<Attribute> inputAttributes = nodeTable.getAttributeList(); 
		
		ExpressionWhereModel selectionRA = ConvertSelectRAListToOneSelectRA(selectRAList);
		String selectionRAString = selectionRA.getExprString();
		
		return nodeTable;
	}
	
	/******************************************Helper Functions****************************************/
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

		//"sum, avg, unary minus", ("not" shouldn't be in select clause)
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
	 * Convert a expression to a valid string in C++
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
				System.out.println("Optimizer.execute.t.attrlist: "+currentTable.getAttributeList());
			}
			tempTableList.add(currentTable);
		}
		System.out.println("Optimizer.tempTableList: "+ tempTableList);

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
//			convertExprToExprWhereModel(expressionWhere,where);
			//TODO 
			expressionListWhere.add(expressionWhere);
		}	
	}
	
	
	
	private void convertExprToExprWhereModel(ExpressionWhereModel expressionModel, Expression expression){
		String expressiontType = expression.getType();
		
		
	}
	
	
	/**
	 * Aggregate ArrayList from origin to target.
	 * No duplicates
	 * @param target
	 * @param origin
	 */
	private void aggregateArrayList(ArrayList<String> target, ArrayList<String> origin){
		if(target==null){
			target = new ArrayList<String>();
		}
		for (String entry : origin){
			if (!target.contains(entry)) {
				target.add(entry);
			}
		}
	}
	
	
	/**
	 * build a RA tree based on current SQL query
	 * @return
	 */
	private RATreeNode createRATree(){
		RATreeNode rootNode = new RATreeNode(false, true); // starting with a root node.
		rootNode.setSelectListRA(expressionListWhereClause); // By default, put all Selection in RA on root
		rootNode.setOutAttsList(projectedAttsList); // set all output attribute to Root
		rootNode.setExprsMap(exprsMap);// set all output projection to Root
		
		//Pull down to right node to construct the tree
		RATreeNode currentpointer = rootNode;
	    int i=0;
	    //Handle the upper tree when table number > 1. 
	    //If table size =2, root have two leaf nodes, each has a table.
	    //then this loop is is ignored
	    for(; i < tableListFromClause.size() - 2; i++){
	    	currentpointer.setLeftNode(new RATreeNode(true, false));//left node is set to leaf
	    	currentpointer.getLeftNode().setTable(tableListFromClause.get(i));
	    	currentpointer.getLeftNode().setParentNode(currentpointer);
	    	
	    	currentpointer.setRightNode(new RATreeNode(false,false));//right node is not leaf node yet
	    	currentpointer.getRightNode().setParentNode(currentpointer);
	    	
	    	currentpointer = currentpointer.getRightNode();//move cursor to right node
	    }

	    //Handle the last two leaf nodes with last two tables when table number > 1
	    if(tableListFromClause.size() > 1){
	    	currentpointer.setLeftNode(new RATreeNode(true, false));//left node is set to leaf
	    	currentpointer.getLeftNode().setTable(tableListFromClause.get(i)); // the second last table
	    	currentpointer.getLeftNode().setParentNode(currentpointer);
	    	
	    	currentpointer.setRightNode(new RATreeNode(true, false));//right node is set to leaf too
	    	currentpointer.getRightNode().setTable(tableListFromClause.get(i+1)); //  the last table
	    	currentpointer.getRightNode().setParentNode(currentpointer);
	    	currentpointer = currentpointer.getRightNode();//move cursor to right node
	    }
	    
	   //If only 1 table (2 nodes)
	    if(tableListFromClause.size()==1){
	    	rootNode.setSingle(true);
	    	rootNode.setLeftNode(new RATreeNode(true, false));//left node is set to leaf
	    	rootNode.getLeftNode().setTable(tableListFromClause.get(0));
	    	rootNode.getLeftNode().setParentNode(rootNode);
	    }
		return rootNode;
	}
	
	private void aggregateAliasesToAllNonLeafNodes(RATreeNode node){
		if(node.isLeaf()){ return;}
		
		if(node.getLeftNode() != null && node.getLeftNode().isLeaf() == true){
			String currentLeafAlias = node.getLeftNode().getTable().getAlias();
			if ( !node.getAliasesList().contains(currentLeafAlias)){
				node.getAliasesList().add(currentLeafAlias);
			}	
		}	
		else if(node.getLeftNode() != null && node.getLeftNode().isLeaf() == false){
			aggregateAliasesToAllNonLeafNodes(node.getLeftNode() );
			aggregateArrayList(node.getAliasesList(), node.getLeftNode().getAliasesList());
		}
		
		if(node.getRightNode() != null && node.getRightNode().isLeaf()==true){
			String currentLeafAlias = node.getRightNode().getTable().getAlias();
			if ( !node.getAliasesList().contains(currentLeafAlias)){
				node.getAliasesList().add(currentLeafAlias);
			}	
		}	
		else if(node.getRightNode() != null && node.getRightNode().isLeaf()==false){
			aggregateAliasesToAllNonLeafNodes(node.getRightNode() );
			aggregateArrayList(node.getAliasesList(), node.getRightNode().getAliasesList());
		}
			
	}
	
	/**
	 * Concatenate RA selections to one RA selection with "&&"
	 * @param selectList
	 * @return
	 */
	private ExpressionWhereModel ConvertSelectRAListToOneSelectRA(ArrayList<ExpressionWhereModel> selectList){
		if(selectList.size()==0) {
			return new ExpressionWhereModel("none","none"); // no where clause
		}

		String str = "";
		ExpressionWhereModel result = new ExpressionWhereModel(str,"and");//Type is "and"
		for(ExpressionWhereModel select :selectList){
			if(str.equals("")){
				str += select.getExprString();
			}
			else{
				str += " && " + select.getExprString();
			}
			aggregateArrayList(result.getAliasesList(), select.getAliasesList());
			aggregateArrayList(result.getAttributesList(), select.getAttributesList());
		}
		result.setExprString(str);
		return result;	 
	}
	
	
}
