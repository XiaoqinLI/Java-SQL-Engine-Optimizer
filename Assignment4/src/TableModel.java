import java.util.ArrayList;

/**
 * A table model storing table information from the parsed 
 * catalog.
 * @author Xiaoqin LI
 */
public class TableModel {
	private String tableName;
	private ArrayList<String> aliasesList;
	private ArrayList<Attribute> attributeList; // Attribute list
	
	public TableModel (String name) {
		this.tableName = name;
		this.aliasesList = new ArrayList<String>();
		this.attributeList = new ArrayList<Attribute>();
	}

	public String setOutputFileName(){
		return "_Temp_" + this.aliasesList.toString();
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
	
	
	
}
