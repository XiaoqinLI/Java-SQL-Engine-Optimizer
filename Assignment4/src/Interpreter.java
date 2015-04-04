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
				for (Expression e : mySelect)
					System.out.println ("\t" + e.print ());

				System.out.println ("Tables in FROM:");
				Map <String, String> myFrom = parser.getFROM ();
				System.out.println ("\t" + myFrom);

				System.out.println ("WHERE clause:");
				Expression where = parser.getWHERE ();
				if (where != null)
					System.out.println ("\t" + where.print ());

				System.out.println ("GROUPING atts:");
				for (String att : parser.getGROUPBY ()) {
					System.out.println ("\t" + att);
				}
				
				// semantics checking starts here.
				semanticChecker = new SemanticCheck(res, myFrom);
				System.out.println("\n##### Semantic checking started. #####");
				if(semanticChecker.checkingQuery()){
					System.out.println("\n##### Semantic checking successfully ended. #####");				
				}else{
					System.out.println("\n##### The Error has been reported above. #####");
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
		Map<String, String> fromClause;

		SemanticCheck(Map <String, TableData> res, Map<String, String> FROM){		
			// TODO Auto-generated constructor stub
			this.dataMap = res;
			this.fromClause = FROM;
		}

		public boolean checkingQuery(){
			
			// checking the from clause of the query
			System.out.println("Checking the 'From' clause");
			if(!isValidFromClause(fromClause)){
				System.out.println("Invalid syntax Found in 'From' clause");
				return false;
			}else{
				System.out.println("'From' clause is validated");
			}

			return true;
		}

		private boolean isValidFromClause(Map<String, String> FROM){
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





	}
}
