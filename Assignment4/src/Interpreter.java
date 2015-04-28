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
				Expression myWhere = parser.getWHERE ();
				if (myWhere != null)
					System.out.println ("\t" + myWhere.print ());

				System.out.println ("GROUPING atts:");
				ArrayList<String> attributesGroupBy = parser.getGROUPBY();
				for (String att : attributesGroupBy) {
					System.out.println ("\t" + att);
				}

				// semantics checking starts here.
				semanticChecker = new SemanticCheck(res, mySelect, myFrom, attributesGroupBy, myWhere);

				System.out.println("\n##### Semantic checking started. #####");
				if(semanticChecker.checkingSQLQuery()){
					System.out.println("\n##### Semantic checking successfully ended. #####");	
					// Semantics checking ends here.
					
					// Query Execution and optimization starts here
					long queryStartTime = System.currentTimeMillis(); 
//					QueryOptimizationExecution(mySelect,myFrom, myWhere, attributesGroupBy, rvQuery.getSelTypes()).execution();
					long queryEndTime = System.currentTimeMillis();
				    System.out.println("Query execution completed.");
				    System.out.println("The total time run for this query is " + (queryEndTime - queryStartTime) + " milliseconds");
					// Query Execution and optimization ends here
					
				}else{
					System.out.println("\n##### Failed, the Error has been reported above. Semantic checking ended #####");
					// Semantics checking ends here.
					System.out.println("\n##### Passed the invalid query #####");
				}
				
				System.out.format ("\nSQL>");
			}
		}catch (Exception e) {
			System.out.println("Error! Exception: " + e); 
		} 
		
	} // end of Main function
	
	

}
