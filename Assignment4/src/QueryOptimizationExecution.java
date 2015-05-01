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
	static GetKRecords result;
	
	// set a flag to check if the query has aggregations in Select and/or Group Clause
	private boolean isAggregationOrGroupBy = false; 
	
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
		// Populate input and output attribute Lists as well as output expression Map based on SQL selectClause
		selectedAttsList =  new ArrayList<Attribute>(); // new Attribute ("Int", "o_orderkey")
		projectedAttsList = new ArrayList<Attribute>(); // new Attribute ("Int", "att1")
		exprsMap = new HashMap<String,String>(); // exprs.put ("att1", "o_orderkey");
		
		for(int i = 0; i < this.selectClause.size(); i++){
			getSelectedAttsList(selectedAttsList, this.selectClause.get(i));
			String currentExpressionType = getExprTypeInSelection(this.selectClause.get(i));
			projectedAttsList.add(new Attribute(currentExpressionType, "att" + String.valueOf(i + 1)));
			String currentExpressionValue = convertExpressionToString(this.selectClause.get(i));
			exprsMap.put("att" + String.valueOf(i + 1), currentExpressionValue);
		}
		
		// Populate tableList from SQL fromClause
		tableListFromClause =  getTableListFromClause();
		
		// Populate expressionList from SQL whereClause
		expressionListWhereClause = new ArrayList<ExpressionWhereModel>();
		parseWhereClouse(expressionListWhereClause, whereClause); 
		
		// Build the RA Tree
		rootNodeRA = createRATree();
	    aggregateAliasesToAllNonLeafNodes(rootNodeRA);
	    
	    // Optimize the RA tree
	    if(rootNodeRA.isSingle()){
	    	pushDownselectedAttributes(rootNodeRA,selectedAttsList);
	    }else{
	    	pushDownRASelection(rootNodeRA);
	    	pushDownselectedAttributes(rootNodeRA, selectedAttsList);
	    }
//	    this.reorderAbbrListRec(root);
	    
	    // Execute Query
	    System.out.println();
	    executeQuery(rootNodeRA);
	    if(this.isAggregationOrGroupBy == true){
	    	executeAggregationOrGroupBy(rootNodeRA,projectedAttsList);
	    }
	    
	    // Print out at most 30 entries of output based on GetKRecords
	    if (isAggregationOrGroupBy){
	    	result = new GetKRecords ("GroupBy_Output.tbl", 30);
	    }else{
	    	result = new GetKRecords ("Selection_Output.tbl", 30);
	    }
	    result.print ();
	    
	}
	
	
	/******************************************Execution Functions************************************/
	/**
	 * main SQL query execute
	 */
	private void executeQuery(RATreeNode node){
		// If only one table from fromClouse
		if(node.isSingle()){
			if(this.isAggregationOrGroupBy){
				node.setTable(executeSelect(node.getSelectListRA(), node.getInAttsList(), null, node.getLeftNode().getTable()));
			}
			else{
				node.setTable(executeSelect(node.getSelectListRA(), node.getInAttsList(), node.getExprsMap(), node.getLeftNode().getTable() ));
			}
			return;
		}
		
		// If current node is a leaf node in a RA tree that has at least 2 tables from fromClause, then set it joinFlag to true.
		// Since every original table in this tree has to have at least one join.
		if(node.isLeaf()){
			node.setTable(executeSelect(node.getSelectListRA(), node.getInAttsList(), null, node.getTable()));
			node.setJoin(true);
			return;
		}
		
		if(node.getLeftNode().isJoin() == false){
			executeQuery(node.getLeftNode());
		}
		if(node.getRightNode().isJoin() == false){
			executeQuery(node.getRightNode());
		}
		
		// if both children are either a table or a intermediate temp table, 
		// than this node becomes a join node.
		node.setJoin(true);
		
		

	}
	
	
	/**
	 * execute selection
	 * @param selectRAList
	 * @param requiredAtts
	 * @param expressionMap
	 * @param nodeTable
	 * @return
	 */
	private TableModel executeSelect(ArrayList<ExpressionWhereModel> selectRAList, ArrayList<String> requiredAtts, Map<String,String> expressionMap, TableModel nodeTable){
		
		// Prepare selection string for SELECTION
		ExpressionWhereModel selectionRA = ConvertSelectRAListToOneSelectRA(selectRAList);
		String selectionRAString = selectionRA.getExprString();
		
		// Remove alias in selectionRAString
		for (String  currentAlias: nodeTable.getAliasesList()) {
			selectionRAString = selectionRAString.replaceAll(currentAlias + "\\.", "");
			for(int i = 0;i < requiredAtts.size();i ++){
				requiredAtts.set(i, requiredAtts.get(i).replaceAll(currentAlias + "\\.", ""));
			}
		}
		
		// Prepare inAtts for SELECTION
				ArrayList<Attribute> inputAttributes = nodeTable.getAttributeList(); 
		
		// Initialize exprsMap
		Map<String,String> exprsMap;
		if(expressionMap != null) {
			exprsMap = expressionMap;
		}else{
			exprsMap = new HashMap<String, String>();
		}
		
		// Prepare outAtts for SELECTION, create nextAttributes for next operation
		ArrayList<Attribute> outputAttributes = new ArrayList<Attribute>();
		ArrayList<Attribute> nextAttributes = new ArrayList<Attribute>();
		
		for(int i = 0, j = 0; i < inputAttributes.size(); i++){
			Attribute currentAttribute = inputAttributes.get(i);
			if(requiredAtts.contains(currentAttribute.getName())){
				nextAttributes.add(currentAttribute);
				outputAttributes.add(new Attribute(currentAttribute.getType(), "att" + String.valueOf(j + 1))); 
				if(expressionMap == null){
					exprsMap.put("att"+String.valueOf(j + 1), currentAttribute.getName());
				}
				j++;
			}
		}
		
		// Prepare exprs map for SELECTION
		for (Entry<String,String> entry : exprsMap.entrySet()){
			for (String currentAlias : nodeTable.getAliasesList()) {
				entry.setValue(entry.getValue().replaceAll(currentAlias + "\\.", ""));
			}
		}

		// Prepare inFile, outFile
		String inFileName = nodeTable.getTableName();
		String outFileName;
		if (expressionMap != null && this.isAggregationOrGroupBy==false) {
			outFileName = "Selection_Output";
		}
		else {
			outFileName = nodeTable.setOutputFileName();
		}
		
		// call Selection API
		try {
			new Selection (inputAttributes, outputAttributes, selectionRAString, exprsMap, inFileName + ".tbl", outFileName + ".tbl", "g++", "cppDir/"); 
		} catch (Exception e) {
			throw new RuntimeException (e);
		}
		
		// Prepare output table and clean memory
	    TableModel nextTable = new TableModel(outFileName);
	    nextTable.getAliasesList().addAll(nodeTable.getAliasesList());
	    nextTable.setAttributeList(nextAttributes);
	    
		System.out.println("Query Optimizer: executeSelect on table "+ nodeTable.getTableName() +", output file is " + outFileName + ".tbl");
		nodeTable.clear();
		return nextTable;
	}
	
	
	/**
	 * execute Groupby or Aggregation
	 * @param node
	 * @param outAtts
	 */
	private void executeAggregationOrGroupBy(RATreeNode node, ArrayList<Attribute> outAtts){
		TableModel nodeTable = node.getTable();
		
		//Prepare inAtts 
		ArrayList<Attribute> inAtts = nodeTable.getAttributeList();
		
		//Prepare groupingAtts
		ArrayList<String> groupingAtts = new ArrayList<String>();
		if(!(this.groupbyClause.size() == 0)){
			for (String attributeEle: groupbyClause){
				groupingAtts.add(attributeEle.substring(attributeEle.indexOf(".") + 1));
			}
		}
		
		//Prepare myAggs
		Map<String, AggFunc> myAggs = new HashMap<String, AggFunc>();
		
		for(int i = 0; i < this.selectClause.size(); i++){
			Expression currentExpr = this.selectClause.get(i);
			String aggregationType = getExprTypeInGroupBy(currentExpr);
			String selection = convertExpressionToString(currentExpr);
			for(String alias : nodeTable.getAliasesList()) {
				selection = selection.replaceAll(alias + "\\.", "");
			}
			if(!aggregationType.equals("sum") && !aggregationType.equals("avg")) {
				aggregationType = "none";
			}
			myAggs.put("att" + String.valueOf(i + 1), new AggFunc (aggregationType, selection));
		}
		
		//Prepare inFile, outFile
		String inFileName = nodeTable.getTableName() + ".tbl";
		String outFileName = "GroupBy_Output.tbl";
		
		try{
			new Grouping(inAtts, outAtts, groupingAtts, myAggs, inFileName, outFileName, "g++", "cppDir/");
		}catch(Exception e){
			throw new RuntimeException (e);
		}
		
		System.out.println("Query Optimizer: executeAggregationOrGroupBy on table "+ nodeTable.getTableName() +", output file is " + outFileName + ".tbl");

	}
	
	
	/******************************************Helper Functions****************************************/
	/**
	 * Get all selected attributes in SelectClause as input attributes
	 * only identifiers needed.
	 * @param inAtts
	 * @param expression
	 */
	private void getSelectedAttsList(ArrayList<Attribute> inAtts, Expression expression){
    	if(!expression.getSubexpression().getType().equals("nonUnaryTypes"))
    		getSelectedAttsList(inAtts, expression.getSubexpression());
    	
    	if(!expression.getSubexpression("left").getType().equals("nonBinaryTypes"))
    		getSelectedAttsList(inAtts, expression.getLeftSubexpression());
    	
    	if(!expression.getSubexpression("right").getType().equals("nonBinaryTypes"))
    		getSelectedAttsList(inAtts, expression.getRightSubexpression());	
    	
    	if(expression.getType().equals("identifier")){
    		String expressionValue = expression.getValue();
			String alias = expressionValue.substring(0, expressionValue.indexOf("."));	
			String attributeName = expressionValue.substring(expressionValue.indexOf(".") + 1);
			String expressionType = this.dataMap.get(this.fromClause.get(alias)).getAttInfo(attributeName).getDataType();
			inAtts.add(new Attribute(expressionType, expressionValue));
    	}
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
				this.isAggregationOrGroupBy = true;
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
	private String convertExpressionToString(Expression expression){
		String expressionType = expression.getType();
		// for identifier
		if(expressionType.equals("identifier")){
			return expression.getValue();}
		// for literal type
		if(expressionType.equals("literal string")){
			return "Str (" + expression.getValue() + ")";}
    	if(expressionType.equals("literal float")){  
    		return "Float (" + expression.getValue() + ")";}
    	if(expressionType.equals("literal int")){
    		return "Int (" + expression.getValue() + ")";}
    	// for binary operation
    	if(expressionType.equals("plus")){
    		return "(" + convertExpressionToString(expression.getLeftSubexpression()) + " + "
    				   + convertExpressionToString(expression.getRightSubexpression()) + ")";}
    	if(expressionType.equals("minus")){
    		return "(" + convertExpressionToString(expression.getLeftSubexpression()) + " - "
    				   + convertExpressionToString(expression.getRightSubexpression()) + ")";}
    	if(expressionType.equals("times")){
    		return "(" + convertExpressionToString(expression.getLeftSubexpression()) + " * "
    				   + convertExpressionToString(expression.getRightSubexpression()) + ")";}
    	if(expressionType.equals("divided by")){
    		return "(" + convertExpressionToString(expression.getLeftSubexpression()) + " / "
    				   + convertExpressionToString(expression.getRightSubexpression()) + ")";}
    	if(expressionType.equals("or")){
    		return "(" + convertExpressionToString(expression.getLeftSubexpression()) + " || "
    				   + convertExpressionToString(expression.getRightSubexpression()) + ")";}	
    	if(expressionType.equals("equals")){
    		return "(" + convertExpressionToString(expression.getLeftSubexpression()) + " == "
    				   + convertExpressionToString(expression.getRightSubexpression()) + ")";}
    	if(expressionType.equals("greater than")){
    		return "(" + convertExpressionToString(expression.getLeftSubexpression()) + " > "
    				   + convertExpressionToString(expression.getRightSubexpression()) + ")";}
    	if(expressionType.equals("less than")){
    		return "(" + convertExpressionToString(expression.getLeftSubexpression()) + " < "
    				   + convertExpressionToString(expression.getRightSubexpression()) + ")";} 	
    	// for unary operation
    	if (expressionType.equals("unary minus")){
    		return "-(" + convertExpressionToString(expression.getSubexpression()) +")";}
    	if (expressionType.equals("not")){
    		return "!(" + convertExpressionToString(expression.getSubexpression()) + ")";}
    	if (expressionType.equals("sum")){
    		return convertExpressionToString(expression.getSubexpression());}
    	if (expressionType.equals("avg")){
    		return convertExpressionToString(expression.getSubexpression());}
    	
    	return "Unknown"; // just finish up the function, nothing will be unknown based on all given test queries
	}
	
	
	/**
	 * populate table list based on fromClause.
	 * @return
	 */
	private ArrayList<TableModel> getTableListFromClause(){
		ArrayList<TableModel> tempTableList = new ArrayList<TableModel>();
		for(String currentAlias : fromClause.keySet()){
			// create a table model to store current table's info.
			String currentTableName = fromClause.get(currentAlias);
			TableModel currentTable = new TableModel(currentTableName);
			currentTable.getAliasesList().add(currentAlias);
			// populate attribute list, attributes are in order based on attrNum in catalog
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
			parseWhereClouse(expressionListWhere, where.getLeftSubexpression());
			parseWhereClouse(expressionListWhere, where.getRightSubexpression());
		}else{
			String expressionString = convertExpressionToString(where);
			ExpressionWhereModel expressionWhere = new ExpressionWhereModel(expressionString, expressionType);
//			populateExprWhereModel(expressionWhere, where);
			expressionListWhere.add(expressionWhere);
		}	
	}
	
	
	/**
	 * populate ExpressionWhereModel based on Expression type
	 * @param expressionModel
	 * @param expression
	 */
	private void populateExprWhereModel(ExpressionWhereModel expressionModel, Expression expression){
		String expressiontType = expression.getType();
		//TODO
		
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
	 *  build a RA tree based on current SQL query, based on RA tree covered in class.
	 *  The tree originally built in this way: 
	    all original tables are all on leaf nodes of this tree.
	    The tree grows on its right side child node, meaning that it always choose left side to have a table
	    the last table should be set on right side child node.
	    The tree structure is like:
	               N
	              / \
	             T   N
	                / \
	               T   N
	                  / \
	                 T   T(the last original Table)
	 * 
	 * @return
	 */
	private RATreeNode createRATree(){
		RATreeNode rootNode = new RATreeNode(false, true); // starting with a root node.
		rootNode.setSelectListRA(expressionListWhereClause); // By default, put all Selections in RA on rootNode
		rootNode.setOutAttsList(projectedAttsList); // set all output attributes to rootNode
		rootNode.setExprsMap(exprsMap);// set all output projections to rootNode
	    rootNode.setAliasesList(new ArrayList<String>());//For all non-leaf nodes

		RATreeNode currentpointer = rootNode;
	    int i = 0;
	    
	    //Build the tree when table number > 1. 
	    //If table size =2, root have two leaf nodes, each has a table.
	    //then this loop is is ignored
	    for(; i < tableListFromClause.size() - 2; i++){
	    	currentpointer.setLeftNode(new RATreeNode(true, false));//left node is always set to leaf.
	    	currentpointer.getLeftNode().setTable(tableListFromClause.get(i)); // left child is always a table
	    	currentpointer.getLeftNode().setParentNode(currentpointer); 
	    	
	    	currentpointer.setRightNode(new RATreeNode(false,false));//right node is not leaf node yet
	    	currentpointer.getRightNode().setParentNode(currentpointer);
	    	
	    	currentpointer = currentpointer.getRightNode();//move cursor to right node
	    }

	    //populate the last two leaf nodes with last two tables when table number > 1
	    if(tableListFromClause.size() > 1){
	    	currentpointer.setLeftNode(new RATreeNode(true, false));//left node is set to leaf
	    	currentpointer.getLeftNode().setTable(tableListFromClause.get(i)); // the second last table
	    	currentpointer.getLeftNode().setParentNode(currentpointer);
	    	
	    	currentpointer.setRightNode(new RATreeNode(true, false));//right node is set to leaf too
	    	currentpointer.getRightNode().setTable(tableListFromClause.get(i+1)); //  the last table
	    	currentpointer.getRightNode().setParentNode(currentpointer);
//	    	currentpointer = currentpointer.getRightNode();//move cursor to right node
	    }
	    
	   //If only 1 table (2 nodes, one is root, the other is the left child table node)
	    if(tableListFromClause.size()==1){
	    	rootNode.setSingle(true);
	    	rootNode.setLeftNode(new RATreeNode(true, false));//left node is set to leaf
	    	rootNode.getLeftNode().setTable(tableListFromClause.get(0));
	    	rootNode.getLeftNode().setParentNode(rootNode);
	    }
		return rootNode;
	}
	
	
	/**
	 * aggregate aliases from bottom to top
	 * @param node
	 */
	private void aggregateAliasesToAllNonLeafNodes(RATreeNode node){
		if(node.isLeaf()){ return;}
		
		if(node.getLeftNode() != null && node.getLeftNode().isLeaf() == true){
			aggregateArrayList(node.getAliasesList(), node.getLeftNode().getTable().getAliasesList());
		}	
		else if(node.getLeftNode() != null && node.getLeftNode().isLeaf() == false){
			aggregateAliasesToAllNonLeafNodes(node.getLeftNode());
			aggregateArrayList(node.getAliasesList(), node.getLeftNode().getAliasesList());
		}
		
		if(node.getRightNode() != null && node.getRightNode().isLeaf()==true){
			aggregateArrayList(node.getAliasesList(), node.getRightNode().getTable().getAliasesList());
		}	
		else if(node.getRightNode() != null && node.getRightNode().isLeaf()==false){
			aggregateAliasesToAllNonLeafNodes(node.getRightNode());
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
	
	
	/**
	 * get aggregation type in select expression
	 * @param expression
	 * @return
	 */
	private String getExprTypeInGroupBy(Expression expression){
		String expressionType =  expression.getType();
				
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

		// "+, -, *, /"
		if (expressionType.equals("plus") || expressionType.equals("minus") || expressionType.equals("times") || expressionType.equals("divided by"))
			return this.getExprTypeInSelection(expression.getLeftSubexpression());

		//"sum, avg, unary minus", ("not" shouldn't be in select clause)
		if (expressionType.equals("unary minus")){
			return this.getExprTypeInSelection(expression.getSubexpression());
		}
		
		//Aggregation 
    	if(expressionType.equals("sum")){
    		return "sum";}
    	if(expressionType.equals("avg")){
    		return "avg";}
    	
		return "Unknown";
	}
	
	
	/**
	 * push all needed attributes in select clause down to all children nodes
	 * @param node
	 * @param selectedAttributesList
	 */
	private void pushDownselectedAttributes(RATreeNode node, ArrayList<Attribute> selectedAttributesList){
		if(node.getParentNode()==null){
			if(selectedAttributesList !=null){
				for(Attribute attribute : selectedAttributesList){
					node.getInAttsList().add(attribute.getName());
				}
			}   
    	}
    	else{
    		aggregateArrayList(node.getInAttsList(), node.getParentNode().getInAttsList());
    		
    		//Also add attributes in parent's ExpressionWhereModel list to node's InAttsList
    		for(ExpressionWhereModel exprModelEle : node.getParentNode().getSelectListRA()){
    			aggregateArrayList(node.getInAttsList(), exprModelEle.getAttributesList());
    		}
    	}
    	if(node.getLeftNode() != null) {
    		pushDownselectedAttributes(node.getLeftNode(), null);
    	}
    	if(node.getRightNode() != null) {
    		pushDownselectedAttributes(node.getRightNode(), null);
    	}
	}
	

	/**
	 * push all RA selections based on where clause down to intermediate nodes as deep as we can.
	 * @param node
	 */
	private void pushDownRASelection(RATreeNode node){
		if(node.isLeaf()){ // It 's an original table. no need to push any more
			return; }
		ArrayList<ExpressionWhereModel> TempSelectRAList = new ArrayList<ExpressionWhereModel>();
		for(ExpressionWhereModel exprModelEle : node.getSelectListRA()){
			if(node.getLeftNode() != null){
				if(node.getLeftNode().isLeaf()){
					if(node.getLeftNode().getTable().getAliasesList().containsAll(exprModelEle.getAliasesList())){
						TempSelectRAList.add(exprModelEle);
						node.getLeftNode().getSelectListRA().add(exprModelEle);
						continue;// Pushed this exprModelEle down already, no need to find another place.
					}
				}
				else{
					if(node.getLeftNode().getAliasesList().containsAll(exprModelEle.getAliasesList())){
						TempSelectRAList.add(exprModelEle);
						node.getLeftNode().getSelectListRA().add(exprModelEle);
						continue;
					}
				}
			}
			
			if(node.getRightNode() != null){
				if(node.getRightNode().isLeaf()){
					if(node.getRightNode().getTable().getAliasesList().containsAll(exprModelEle.getAliasesList())){
						TempSelectRAList.add(exprModelEle);
						node.getRightNode().getSelectListRA().add(exprModelEle);
						continue;// Pushed this exprModelEle down already, no need to find another place.
					}
				}
				else{
					if(node.getRightNode().getAliasesList().containsAll(exprModelEle.getAliasesList())){
						TempSelectRAList.add(exprModelEle);
						node.getRightNode().getSelectListRA().add(exprModelEle);
						continue;
					}
				}
			}
		}
		
		node.getSelectListRA().removeAll(TempSelectRAList);
		if(node.getLeftNode()!=null){
			pushDownRASelection(node.getLeftNode());
		}
		if(node.getRightNode()!=null){
			pushDownRASelection(node.getRightNode());	
		}
	}
	
}
