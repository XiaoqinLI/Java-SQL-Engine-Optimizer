import java.util.ArrayList;

/**
 * A table model storing table information from the parsed 
 * catalog.
 * @author Xiaoqin LI
 */
public class TableModel implements Comparable<TableModel>{
	private String tableName;
	private ArrayList<String> aliasesList; // for original table, only one element, but for intermediate temp table after join, maybe more than one
	private ArrayList<Attribute> attributeList; // Attribute list, 
	private int tupleCount;
	
	public TableModel (String name) {
		this.tableName = name;
		this.aliasesList = new ArrayList<String>();
		this.attributeList = new ArrayList<Attribute>();
	}

	public String setOutputFileName(){
		return "_SelectTemp_" + this.aliasesList.toString();
	}
	
	public void clear(){
		this.tableName = "";
		this.aliasesList.clear();
		this.attributeList.clear();
	}
	
	public String getTableName() {return tableName;}
	public void setTableName(String tableName) {this.tableName = tableName;}

	public ArrayList<String> getAliasesList() {return aliasesList;}
	public void setAliasesList(ArrayList<String> aliasesList) {this.aliasesList = aliasesList;}

	public ArrayList<Attribute> getAttributeList() {return attributeList;}
	public void setAttributeList(ArrayList<Attribute> attributeList) {this.attributeList = attributeList;}

	public int getTupleCount() {return tupleCount;}
	public void setTupleCount(int tupleCount) {this.tupleCount = tupleCount;}

	@Override
	public int compareTo(TableModel otherTable) {
		// TODO Auto-generated method stub
		Integer tCount = new Integer(tupleCount);
		return tCount.compareTo(otherTable.tupleCount);

	}
	
	
	
}
