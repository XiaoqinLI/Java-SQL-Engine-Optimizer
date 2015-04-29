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
	
	public String getAlias(){
		return this.alias;
	}
	
	public ArrayList<Attribute> getAttributeList(){
		return this.attributeList;
	}
}
