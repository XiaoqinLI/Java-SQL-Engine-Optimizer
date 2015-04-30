import java.util.ArrayList;

/**
 * A table model storing table information from the parsed 
 * catalog.
 * @author Xiaoqin LI
 */
public class TableModel {
	private String tableName;
	private String alias; // Abbreviate list
	private ArrayList<Attribute> attributeList; // Attribute list
	
	public TableModel (String name, String alias) {
		this.tableName = name;
		this.alias = alias;
		this.attributeList = new ArrayList<Attribute>();
	}

	public String setOutputFileName(){
		return "_Temp_"+this.alias;
	}
	
	public void clear(){
		this.tableName = "";
		this.alias = "";
		this.attributeList.clear();
	}
	
	public String getTableName() {return tableName;}
	public void setTableName(String tableName) {this.tableName = tableName;}

	public String getAlias() {return alias;}
	public void setAlias(String alias) {this.alias = alias;}

	public ArrayList<Attribute> getAttributeList() {return attributeList;}
	public void setAttributeList(ArrayList<Attribute> attributeList) {this.attributeList = attributeList;}
	
	
	
}
