import java.util.ArrayList;
import java.util.Map;

/**
 * This class run valid SQL queries
 * @author Xiaoqin LI
 *
 */
public class QueryOptimizationExecution {
	static String outputFileName = "out.tbl";
	static String compiler = "g++";
	static String outputLocation = "cppDir/";
	
	Map<String, TableData> dataMap;
	ArrayList<Expression> selectClause;
	Map<String, String> fromClause;
	Expression whereClause;
	ArrayList<String> groupbyClause;
	ArrayList<ExpressionIsTypeValid> selectExpressionTypes;
	
	public QueryOptimizationExecution(Map<String, TableData> dataMap,
			ArrayList<Expression> selectClause, Map<String, String> fromClause,
			Expression whereClause, ArrayList<String> groupbyClause,
			ArrayList<ExpressionIsTypeValid> selectExpressionTypes) {
		super();
		this.dataMap = dataMap;
		this.selectClause = selectClause;
		this.fromClause = fromClause;
		this.whereClause = whereClause;
		this.groupbyClause = groupbyClause;
		this.selectExpressionTypes = selectExpressionTypes;
	}
	
	public void execute() {
		
	}
	
	

}
