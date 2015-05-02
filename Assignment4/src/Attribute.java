
class Attribute {
 
  private String name;
  private String attType;
  

  
  public Attribute (String inType, String inName) {
	  name = inName;
	  attType = inType;
  }

  public String toString(){
	  return "(name: "+ this.name + ", type: " + this.attType + ")";
  } 

  public String getName () {
	  return name; 
  }

  public String getType () {
	  return attType;
  }
  
  public void setName(String name){
	  this.name = name;
  }

}