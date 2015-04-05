import java.io.*;
import org.antlr.runtime.*;
import java.util.*;

class Interpreter {
	
	static SemanticCheck semanticChecker;

	public static void main (String [] args) throws Exception {

		try {

			CatalogReader foo = new CatalogReader ("Catalog.xml");
			Map <String, TableData> res = foo.getCatalog ();
			System.out.println (foo.printCatalog (res));

			InputStreamReader converter = new InputStreamReader(System.in);
			BufferedReader in = new BufferedReader(converter);

			System.out.format ("\nSQL>");
			String soFar = in.readLine () + "\n";

			// loop forever, or until someone asks to quit
			while (true) {

				// keep on reading from standard in until we hit a ";"
				while (soFar.indexOf (';') == -1) {
					soFar += (in.readLine () + "\n");
				}

				// split the string
				String toParse = soFar.substring (0, soFar.indexOf (';') + 1);
				soFar = soFar.substring (soFar.indexOf (';') + 1, soFar.length ());
				toParse = toParse.toLowerCase ();

				// parse it
				ANTLRStringStream parserIn = new ANTLRStringStream (toParse);
				SQLLexer lexer = new SQLLexer (parserIn);
				CommonTokenStream tokens = new CommonTokenStream(lexer);
				SQLParser parser = new SQLParser (tokens);

				// if we got a quit
				if (parser.parse () == false) {
					break; 
				}

				// print the results
				System.out.println ("RESULT OF PARSING");
				System.out.println ("Expressions in SELECT:");
				ArrayList <Expression> mySelect = parser.getSELECT ();
				for (Expression e : mySelect){
					System.out.println ("\t" + e.print () + "\t" + e.getType());
				}

				System.out.println ("Tables in FROM:");
				Map <String, String> myFrom = parser.getFROM ();
				System.out.println ("\t" + myFrom);

				System.out.println ("WHERE clause:");
				Expression where = parser.getWHERE ();
				if (where != null)
					System.out.println ("\t" + where.print ());

				System.out.println ("GROUPING atts:");
				ArrayList<String> attributesGroupBy = parser.getGROUPBY();
				for (String att : attributesGroupBy) {
					System.out.println ("\t" + att);
				}
				
				// semantics checking starts here.
				semanticChecker = new SemanticCheck(res, mySelect, myFrom, attributesGroupBy);
				System.out.println("\n##### Semantic checking started. #####");
				if(semanticChecker.checkingSQLQuery()){
					System.out.println("\n##### Semantic checking successfully ended. #####");				
				}else{
					System.out.println("\n##### Failed, the Error has been reported above. #####");
				}
				// semantics checking ends here.
				
				System.out.format ("\nSQL>");
			}
		}catch (Exception e) {
			System.out.println("Error! Exception: " + e); 
		} 
	}

	private static class SemanticCheck{
		Map<String, TableData> dataMap;
		ArrayList<Expression> selectClause;
		Map<String, String> fromClause;
		ArrayList<String> groupClause;
		
		SemanticCheck(Map <String, TableData> res, ArrayList<Expression> SELECT, Map<String, String> FROM, ArrayList<String> GROUPBY){		
			// TODO Auto-generated constructor stub
			this.dataMap = res;
			this.selectClause =  SELECT;
			this.fromClause = FROM;
			this.groupClause = GROUPBY;
		}

		public boolean checkingSQLQuery(){
			
			// checking the From clause of the query
			System.out.println("-----Checking the 'From' clause---------------------------------------------");
			if(!isValidFromClause()){
				System.out.println("Invalid syntax Found in 'From' clause");
				return false;
			}else{
				System.out.println("'From' clause is validated");
			}
			System.out.println("----------------------------------------------------------------------------\n");
			
			// Checking Identifiers: Whether Alias matching From clause and 
			// Attribute in corresponding table in the SELECT Clause
			System.out.println("-----Checking the Alias and Attribute of Identifiers in 'Select' clause-----");
			if(!isValidIdentifierSelectClause()){
        		System.out.println("Invalid syntax found in 'SELECT' clause");
        		return false;
        	}else{
        		System.out.println("Alias and Attribute in 'Select' clause are all validated");
        	}
	        System.out.println("----------------------------------------------------------------------------\n");
	        
	        // Checking Group Clause and corresponding Syntax in Select Clause
	        System.out.println("-----Checking the Group Clause----------------------------------------------");
	        if((groupClause.size() > 0) && !isValidGroupByClause()){
	        	System.out.println("Invalid syntax found in 'Group By' or its corresponding 'Select' clause");
	        	return false;
	        }
	        else if(groupClause.size() == 0){
	        	System.out.println("No 'Group By' clause exists");
	        }else{
	        	System.out.println("'Group By' clause is validated");
	        }
	        System.out.println("----------------------------------------------------------------------------\n");
	       
			return true;
		}

		private boolean isValidFromClause(){
			String currentTableName;
			Set<String> allTableNames = dataMap.keySet(); // change it too hashset later on
			Set<String> allAliases = fromClause.keySet();
			Iterator<String> allAliasesIterator = allAliases.iterator();
			while(allAliasesIterator.hasNext()){
				currentTableName = fromClause.get(allAliasesIterator.next().toString());
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
			for(String attributeEle : groupClause){
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

//			
//			if(!(attributesInfo.containsKey(attName))){
//				System.out.println("Error: Attribute "+ attName +" attribute absent in the table" + tableName +" database used in GroupBy");
//				return false;
//			}
//			for(Expression exp : mySelect){
//				if(isBinaryOperation(exp.getType())){
//					System.out.println("Error: Expression "+ exp.print() +" expression is not allowed in the select clause when GroupBy");
//					return false;
//				}
//				else if(isUnaryOperation(exp.getType())){
//				}				
//				else if (!(exp.getType().equals("identifier")&& exp.getValue().equals(att))){
//					System.out.println("Error: Expression "+ exp.print() +" expression is not allowed in the select clause when GroupBy");
//					return false;	
//				}
//				else{}
//			}
			return true;
		}
		
		
		
		
		




	}
}
