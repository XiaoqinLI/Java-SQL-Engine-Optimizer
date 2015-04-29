import java.util.ArrayList;
import java.util.Map;


public class RATreeNode {
	private boolean isLeaf = false; 
    private boolean isRoot = false;
    
	private ArrayList<ExpressionWhereModel> selectListRA;//Default
	private ArrayList<String> inAttsList;//Default
	private ArrayList<Attribute> outAttsList;//For root node only
	private Map<String,String> exprsMap; //For root node only
	
//	private ArrayList<String> abbrList; //For non-leaf nodes only
//	private TableModel table;//For leaf node only





	public RATreeNode(boolean isLeaf, boolean isRoot) {
		super();
		this.isLeaf = isLeaf;
		this.isRoot = isRoot;
		this.selectListRA = new ArrayList<ExpressionWhereModel>();
	}

	public boolean isLeaf() {return isLeaf;}
	public void setLeaf(boolean isLeaf) {this.isLeaf = isLeaf;}

	public boolean isRoot() {return isRoot;}
	public void setRoot(boolean isRoot) {this.isRoot = isRoot;}

	public ArrayList<ExpressionWhereModel> getSelectListRA() {return selectListRA;}
	public void setSelectListRA(ArrayList<ExpressionWhereModel> selectListRA) {this.selectListRA = selectListRA;}
	
	public ArrayList<String> getInAttsList() {return inAttsList;}
	public void setInAttsList(ArrayList<String> inAttsList) {this.inAttsList = inAttsList;}

	public ArrayList<Attribute> getOutAttsList() {return outAttsList;}
	public void setOutAttsList(ArrayList<Attribute> outAttsList) {this.outAttsList = outAttsList;}
	
	public Map<String, String> getExprsMap() {return exprsMap;}
	public void setExprsMap(Map<String, String> exprsMap) {this.exprsMap = exprsMap;}
    
    
}
