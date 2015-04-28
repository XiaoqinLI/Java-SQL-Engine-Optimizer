import java.util.ArrayList;
import java.util.Map;
import java.util.Set;



public class SemanticCheck {
	/**
	 * 
	 * This class provides all semantic check functions
	 *
	 */

	Map<String, TableData> dataMap;
	ArrayList<Expression> selectClause;
	Map<String, String> fromClause;
	ArrayList<String> groupbyClause;
	Expression whereClause;

	SemanticCheck(Map <String, TableData> res, ArrayList<Expression> SELECT, Map<String, String> FROM, ArrayList<String> GROUPBY, Expression WHERE){		
		// TODO Auto-generated constructor stub
		this.dataMap = res;
		this.selectClause =  SELECT;
		this.fromClause = FROM;
		this.groupbyClause = GROUPBY;
		this.whereClause = WHERE;
	}

	//		public 

	public boolean checkingSQLQuery(){

		// checking the From clause of the query
		System.out.println("-----Checking the 'From' clause---------------------------------------------");
		if(!isValidFromClause()){
			System.out.println("Invalid syntax Found the in 'From' clause");
			return false;
		}else{
			System.out.println("The 'From' clause is validated");
		}
		System.out.println("----------------------------------------------------------------------------\n");

		// Checking Identifiers: Whether Alias matching From clause and 
		// Attribute in corresponding table in the SELECT Clause
		System.out.println("-----Checking the Alias and Attribute of Identifiers in 'Select' clause-----");
		if(!isValidIdentifierSelectClause()){
			System.out.println("Invalid syntax found in the 'Select' clause");
			return false;
		}else{
			System.out.println("Alias and Attribute in the 'Select' clause are all validated");
		}
		System.out.println("----------------------------------------------------------------------------\n");

		// Checking the Group Clause and corresponding Syntax in Select Clause
		System.out.println("-----Checking the Group Clause----------------------------------------------");
		if( (groupbyClause.size() > 0) && !isValidGroupByClause() ){
			System.out.println("Invalid syntax found in the 'Group By' clause or its corresponding 'Select' clause");
			return false;
		}
		else if(groupbyClause.size() == 0){
			System.out.println("No 'Group By' clause exists");
		}else{
			System.out.println("The 'Group By' clause is validated");
		}
		System.out.println("----------------------------------------------------------------------------\n");

		// Checking the Where Clause and corresponding types' mismatches.
		System.out.println("-----Checking the Where Clause----------------------------------------------");
		if( (whereClause != null) && ( !(isValidWhereClause(whereClause).isTypeValid()) )){
			System.out.println("Invalid syntax found in the 'Where' clause");
			return false;
		}
		else if (whereClause == null){
			System.out.println("No 'Where' clause exists");
		}else{
			System.out.println("The 'Where' clause is validated");
		}
		System.out.println("----------------------------------------------------------------------------\n");

		// Checking Operations in Select Clause
		System.out.println("-----Checking Operation in Select Clause------------------------------------");
		for (Expression selectExpression : selectClause){
			if( !( isValidSelectClause(selectExpression).isTypeValid() ) ){
				System.out.println("Invalid syntax found in the 'Select' clause");
				System.out.println("----------------------------------------------------------------------------\n");
				return false;
			}
		}
		System.out.println("The 'Select' clause is validated");
		System.out.println("----------------------------------------------------------------------------");

		return true;

	}

	private boolean isValidFromClause(){
		String currentTableName;
		Set<String> allTableNames = dataMap.keySet(); //
		Set<String> allAliases = fromClause.keySet();

		for(String eleAliase: allAliases){
			currentTableName = fromClause.get(eleAliase);
			if (!allTableNames.contains(currentTableName)){
				System.out.println("Error: "+ currentTableName +" TABLE does not exist");
				return false;
			}
		}
		return true;
	}

	private boolean isValidIdentifierSelectClause(){
		for (Expression selectEle : selectClause){

			if(selectEle.getType().equals("identifier")){	
				String attribute = selectEle.getValue();
				String alias = attribute.substring(0, attribute.indexOf("."));
				String attributeName = attribute.substring(attribute.indexOf(".") + 1);
				String currentTableName = fromClause.get(alias);

				// if the alias does not match to the one in From clause
				if(!fromClause.containsKey(alias)){
					System.out.println("Error: Alias '" + alias + "' does not stand for any Table in the FROM clause");
					return false;
				}

				Map<String, AttInfo> allAttributesInfo = dataMap.get(currentTableName).getAttributes();

				//if the table for this alias actually does not exist in the Catalog
				if(allAttributesInfo == null){
					System.out.println("Error: '" + currentTableName + "' table does not exist in the provided catalog");
					return false;
				}

				// if the attribute is wrong
				if(!allAttributesInfo.containsKey(attributeName)){
					System.out.println("Error: '" + attributeName + "' arrtibute does not exist in any Table in the FROM clause");
					return false;
				}
			}  	
		}
		return true;
	}

	private  boolean isValidGroupByClause() {
		for(String attributeEle : groupbyClause){
			String alias = attributeEle.substring(0, attributeEle.indexOf("."));	
			String attributeName = attributeEle.substring(attributeEle.indexOf(".") + 1);
			String currentTableName = fromClause.get(alias);

			// Similar to isValidIdentifierSelectClause: if the alias does not match to the one in From clause
			if(!fromClause.containsKey(alias)){
				System.out.println("Error: Alias '" + alias + "' does not stand for any Table in the FROM clause");
				return false;
			}

			Map<String, AttInfo> allAttributesInfo = dataMap.get(currentTableName).getAttributes();

			// Similar to isValidIdentifierSelectClause: if the table for this alias actually does not exist in the Catalog
			if(allAttributesInfo == null){
				System.out.println("Error: '" + currentTableName + "' table does not exist in the provided catalog");
				return false;
			}

			// Similar to isValidIdentifierSelectClause: if the attribute is wrong
			if(!allAttributesInfo.containsKey(attributeName)){
				System.out.println("Error: '" + attributeName + "' arrtibute does not exist in any Table in the FROM clause");
				return false;
			}
		}

		// checking if expression in Select Clause is valid to the Group By Clause
		for(Expression selectEle : selectClause){

			if(selectEle.getType().equals("sum") || selectEle.getType().equals("avg")){
				// these two are allowed, so doing nothing.
			}
			else if (!(selectEle.getType().equals("identifier") && groupbyClause.contains( selectEle.getValue()))){
				System.out.println("Error: Expression "+ selectEle.print() +" expression is not allowed in the select clause when GroupBy exists");
				return false;	
			}	
		}
		return true;
	}

	ExpressionIsTypeValid isValidWhereClause(Expression WHERE) {
		// first of all, check the CNF(and/or) recursively:
		if(WHERE.getType().equals("or") || WHERE.getType().equals("and")){
			ExpressionIsTypeValid leftSubExp = isValidWhereClause(WHERE.getLeftSubexpression());
			ExpressionIsTypeValid rightSubExp = isValidWhereClause(WHERE.getRightSubexpression());

			if(leftSubExp.isTypeValid() && rightSubExp.isTypeValid()){
				return new ExpressionIsTypeValid(0, true);
			}else{
				return new ExpressionIsTypeValid(0, false);
			}				  
		}

		// secondly, check sum, avg and other unary operation
		if(isUnaryOperation(WHERE.getType())){	
			ExpressionIsTypeValid leftSubExpression = isValidWhereClause(WHERE.getLeftSubexpression());
			if(!leftSubExpression.isTypeValid())
				System.out.println("Error: Incompatible unaryTypes found: " + WHERE.print());			

			// everything is fine in "not" based on the assumption made in assignment description
			if(WHERE.getType().equals("not"))
				return leftSubExpression;

			// string type can't be in sum, avg or unary minus no matter what.
			else if (leftSubExpression.getExpType() == 1 ){
				System.out.println("Error: Incompatible unaryTypes found: " + WHERE.print());
				return new ExpressionIsTypeValid(-1, false);
			}

			else
				return leftSubExpression;
		}

		// then check all other BinaryOperations
		String exppresionType = WHERE.getType();

		if(isBinaryOperation(exppresionType)){
			ExpressionIsTypeValid leftSubExpression = isValidWhereClause(WHERE.getLeftSubexpression());
			ExpressionIsTypeValid rightSubExpression = isValidWhereClause(WHERE.getRightSubexpression());

			if((leftSubExpression != null) && (rightSubExpression != null)){
				ExpressionIsTypeValid expressionValid = checkBinaryOperation(leftSubExpression, rightSubExpression, exppresionType);	
				if(!expressionValid.isTypeValid())
					System.out.println("Error: Incompatible bianry operation found in: " + WHERE.print());

				return expressionValid;		  
			}	
		}	  			

		//check identifiers
		String expType;
		if(WHERE.getType().equals("identifier")){
			String expressionString = WHERE.getValue();
			expType = getExpressionAtributeType(expressionString);

			if(expType == null){
				return (new ExpressionIsTypeValid(-1, false));
			}
			else if (expType.equals("Int") || expType.equals("Float"))
				return (new ExpressionIsTypeValid(2, true));	  
			else if(expType.equals("Str")){
				return (new ExpressionIsTypeValid(1, true));	
			}

		}

		// check other valueTypes
		if (WHERE.getType().equals("literal float") || WHERE.getType().equals("literal int"))
			return (new ExpressionIsTypeValid(2, true));
		else if(WHERE.getType().equals("literal string"))
			return (new ExpressionIsTypeValid(1, true));

		// unknown invalid types
		return null; 
	}

	ExpressionIsTypeValid isValidSelectClause(Expression SELECT) {

		// firstly, check sum, avg and other unary operation
		if(isUnaryOperation(SELECT.getType())){	
			ExpressionIsTypeValid leftSubExpression = isValidWhereClause(SELECT.getLeftSubexpression());
			if(!leftSubExpression.isTypeValid())
				System.out.println("Error: Incompatible unaryTypes found: " + SELECT.print());			

			if(SELECT.getType().equals("not"))
				return leftSubExpression;

			// string type can't be in unary operation
			else if (leftSubExpression.getExpType() == 1 ){
				System.out.println("Error: Incompatible unaryTypes found: " + SELECT.print());
				return new ExpressionIsTypeValid(-1, false);
			}

			else
				return leftSubExpression;
		}

		// secondly, check BinaryOperations
		String exppresionType = SELECT.getType();

		if(isBinaryOperation(exppresionType)){
			ExpressionIsTypeValid leftSubExpression = isValidSelectClause(SELECT.getLeftSubexpression());
			ExpressionIsTypeValid rightSubExpression = isValidSelectClause(SELECT.getRightSubexpression());

			if((leftSubExpression != null) && (rightSubExpression != null)){
				ExpressionIsTypeValid expressionValid = checkBinaryOperation(leftSubExpression, rightSubExpression, exppresionType);	
				if(!expressionValid.isTypeValid())
					System.out.println("Error: Incompatible bianry operation found in: " + SELECT.print());

				return expressionValid;		  
			}	
		}	  			

		//check identifiers
		String expType;
		if(SELECT.getType().equals("identifier")){
			String expressionString = SELECT.getValue();
			expType = getExpressionAtributeType(expressionString);

			if(expType == null){
				// System.out.println("Error: "+exp.getValue() +"  is not the valid attribute of the table");
				return (new ExpressionIsTypeValid(-1, false));
			}
			else if (expType.equals("Int") || expType.equals("Float"))
				return (new ExpressionIsTypeValid(2, true));	  
			else if(expType.equals("Str")){
				return (new ExpressionIsTypeValid(1, true));	
			}

		}

		// check other literal valueTypes
		if (SELECT.getType().equals("literal float") || SELECT.getType().equals("literal int"))
			return (new ExpressionIsTypeValid(2, true));
		else if(SELECT.getType().equals("literal string"))
			return (new ExpressionIsTypeValid(1, true));

		// unknown invalid types
		return null; 

	}


	// helper functions:
	private boolean isUnaryOperation(String exppressionType) {
		for (String UnaryTppe : Expression.unaryTypes) {
			if(UnaryTppe.equals(exppressionType))
				return true;
		}
		return false;
	}

	private boolean isBinaryOperation(String exppressionType) {
		for (String binaryTppe : Expression.binaryTypes) {
			if(binaryTppe.equals(exppressionType))
				return true;
		}
		return false;
	}

	private String getExpressionAtributeType(String expression){
		String attributeType;
		String alias = expression.substring(0, expression.indexOf("."));
		String attributeName = expression.substring(expression.indexOf(".")+1);
		String currentTableName = fromClause.get(alias);

		if(currentTableName == null){
			System.out.println("Error: Alias '"+ alias +"' does not exist in any table selected the FROM clause");
			return null;
		}

		Map<String, AttInfo> allAttributesInfo = dataMap.get(currentTableName).getAttributes();

		if (allAttributesInfo == null){
			System.out.println("Error: Table '"+ currentTableName +"' does not exist in the CATALOGUE");
			return null;
		}

		if(!(allAttributesInfo.containsKey(attributeName))){
			System.out.println("Error: Attribute '"+ attributeName +"' does not exist in the TABLE: "+ currentTableName);
			return null;
		}
		else
			attributeType = allAttributesInfo.get(attributeName).getDataType();

		return attributeType;

	}

	private ExpressionIsTypeValid checkBinaryOperation(ExpressionIsTypeValid left, ExpressionIsTypeValid right, String expressionType){
		if(left.isTypeValid() && right.isTypeValid()){			  
			// expression type is int or float
			if(left.getExpType() == 2){ 
				if(right.getExpType() == 1){ // str
					if (expressionType.equals("plus") ){
						return (new ExpressionIsTypeValid(1, true));
					}else{
						System.out.println("Invalid Binary operation between Number and String");
						return (new ExpressionIsTypeValid(-1, false));
					}

				}
				return (new ExpressionIsTypeValid(2, true));
			}

			// expression type is str	
			else if(left.getExpType() == 1){
				if (right.getExpType() == 1){ // str
					if (expressionType.equals("minus") || expressionType.equals("times") || expressionType.equals("divided by")){
						System.out.println("Invalid Binary operation between Strings.");
						return (new ExpressionIsTypeValid(-1, false));
					}else{
						return (new ExpressionIsTypeValid(1, true));
					}
				}
				else if (right.getExpType() == 2){
					if (expressionType.equals("plus") ){
						return (new ExpressionIsTypeValid(1, true));
					}else{
						System.out.println("Invalid Binary operation between String and Number");
						return (new ExpressionIsTypeValid(-1, false));
					}
				}
				else{ // other type
					return (new ExpressionIsTypeValid(1, true));
				}					
			}else{ // unknown type
				return (new ExpressionIsTypeValid(-1, false));	
			}				
		}
		else{ // invalid
			return (new ExpressionIsTypeValid(-1, false));		  
		}

	}

}// end of inner semanticCheck class

