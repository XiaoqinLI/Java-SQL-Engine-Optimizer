import java.util.ArrayList;
import java.util.Map;

/**
 * TreeNode for RATree
 * @author Xiaoqin LI
 *
 */
public class RATreeNode {
	private RATreeNode parentNode = null; 
	private RATreeNode leftNode = null; 
	private RATreeNode rightNode = null; 
	
	private boolean isLeaf = false; 
    private boolean isRoot = false;
	private boolean isSingle = false; // if there is only one table in from clause, then only 2 nodes are needed
    private boolean isJoin = false; // whether the node is going to have a join or not
    
	private ArrayList<ExpressionWhereModel> selectListRA;//Default
	private ArrayList<String> inAttsList = new ArrayList<String>(); ;//Default, attributes involved in SQL selection
	private ArrayList<Attribute> outAttsList;//For output, root node only
	private Map<String,String> exprsMap; //For projection output, root node only
	private TableModel table; // For leaf node only, since leaf nodes are all tables
	private ArrayList<String> aliasesList; //For selection or join, non-leaf nodes only


	public RATreeNode(boolean isLeaf, boolean isRoot) {
		super();
		this.isLeaf = isLeaf;
		this.isRoot = isRoot;
		this.selectListRA = new ArrayList<ExpressionWhereModel>();
		if(!this.isLeaf){
			this.aliasesList = new ArrayList<String>();
		}
	}
	
	
	public RATreeNode getParentNode() {return parentNode;}
	public void setParentNode(RATreeNode parentNode) {this.parentNode = parentNode;}

	public RATreeNode getLeftNode() {return leftNode;}
	public void setLeftNode(RATreeNode leftNode) {this.leftNode = leftNode;}

	public RATreeNode getRightNode() {return rightNode;}
	public void setRightNode(RATreeNode rightNode) {this.rightNode = rightNode;}

	
	public boolean isLeaf() {return isLeaf;}
	public void setLeaf(boolean isLeaf) {this.isLeaf = isLeaf;}

	public boolean isRoot() {return isRoot;}
	public void setRoot(boolean isRoot) {this.isRoot = isRoot;}

	public boolean isSingle() {return isSingle;}
	public void setSingle(boolean isSingle) {this.isSingle = isSingle;}
	
	public boolean isJoin() {return isJoin;}
	public void setJoin(boolean isJoin) {this.isJoin = isJoin;}

	
	public ArrayList<ExpressionWhereModel> getSelectListRA() {return selectListRA;}
	public void setSelectListRA(ArrayList<ExpressionWhereModel> selectListRA) {this.selectListRA = selectListRA;}
	
	public ArrayList<String> getInAttsList() {return inAttsList;}
	public void setInAttsList(ArrayList<String> inAttsList) {this.inAttsList = inAttsList;}

	public ArrayList<Attribute> getOutAttsList() {return outAttsList;}
	public void setOutAttsList(ArrayList<Attribute> outAttsList) {this.outAttsList = outAttsList;}
	
	public Map<String, String> getExprsMap() {return exprsMap;}
	public void setExprsMap(Map<String, String> exprsMap) {this.exprsMap = exprsMap;}

	public TableModel getTable() {return table;}
	public void setTable(TableModel table) {this.table = table;}

	public ArrayList<String> getAliasesList() {return aliasesList;}
	public void setAliasesList(ArrayList<String> aliasesList) {this.aliasesList = aliasesList;}
	
}
