
public class Expression {
  
  // this is an exhaustive list of expression types
  static public final String [] validTypes = {"plus", "minus", "times", 
    "divided by",  "or", "and", "not", "literal string", "literal float",
    "literal int", "identifier", "unary minus",
    "sum", "avg", "equals", "greater than", "less than"};
  
  // this is an exhaustive list of the unary expression types
  static public final String [] unaryTypes = {"not", "unary minus", "sum", "avg"};
  
  // this is an exhaustive list of the binary expression types
  static public final String [] binaryTypes = {"plus", "minus", "times",
    "divided by", "or", "and", "equals", "greater than", "less than"};
  
  // this is an exhaustive list of the value types
  public final String [] valueTypes = {"literal string", "literal float",
    "literal int", "identifier"};
  
  // this is the type of the expression
  private String myType;
  
  // this is the literal value contained in the expression; only non-null
  // if myType is "literal" or "identifier"
  private String myValue;
  
  // these are the two subexpressions
  private Expression leftSubexpression;
  private Expression rightSubexpression;
  
  // prints the expression
  public String print () {
    
    String toMe;
    
    // see if it is a literal type
    for (int i = 0; i < valueTypes.length; i++) {
      if (myType.equals (valueTypes[i])) {
        toMe = myValue;
        return toMe;
      } 
    }
    
    // see if it is a unary type
    for (int i = 0; i < unaryTypes.length; i++) {
      if (myType.equals (unaryTypes[i])) {
        toMe = "(" + myType + " " + leftSubexpression.print () + ")";
        return toMe;
      }
    }
    
    // lastly, do a binary type
    for (int i = 0; i < binaryTypes.length; i++) {
      if (myType.equals (binaryTypes[i])) {
        toMe = "(" + leftSubexpression.print () + " " + myType + " " + rightSubexpression.print () + ")";
        return toMe;
      }
    }
    throw new RuntimeException ("got a bad type in the expression when printing");
  }
  
  // create a new expression of type specified type
  public Expression (String expressionType) {
    
    // verfiy it is a valid expression type
    for (int i = 0; i < validTypes.length; i++) {
      if (expressionType.equals (validTypes[i])) {
        myType = expressionType;
        return;
      }
    }
    
    // it is not valid, so throw an exception
    throw new RuntimeException ("you tried to create an invalid expr type");
  }
  
  public String getType () {
    return myType;
  }
  
  // this returns the value of the expression, if it is a literal (in which
  // case the literal values encoded as a string is returned), or it is an
  // identifier (in which case the name if the identifier is returned)
  public String getValue () {
    for (int i = 0; i < valueTypes.length; i++) {
      if (myType.equals (valueTypes[i])) {
        return myValue;
      }
    } 
    throw new RuntimeException ("you can't get a value for that expr type!");
  }
  
  // this sets the value of the expression, if it is a literal or an 
  // identifier
  public void setValue (String toMe) {
    for (int i = 0; i < valueTypes.length; i++) {
      if (myType.equals (valueTypes[i])) {
        myValue = toMe;
        return;
      }
    } 
    throw new RuntimeException ("you can't set a value for that expr type!");
  }
  
  // this gets the subexpression, which is only possible if this is a 
  // unary operation (such as "unary minus" or "not")
  public Expression getSubexpression () {
    
    // verfiy it is a valid expression type
    for (int i = 0; i < unaryTypes.length; i++) {
      if (myType.equals (unaryTypes[i])) {
        return leftSubexpression;
      }
    }
    
    // it is not valid, so throw an exception
    throw new RuntimeException ("you can't get the subexpression of an " +
                                "expression that is not unary!");
  }
  
  // this sets the subexpression, which is only possible if this is a 
  // unary operation (such as "unary minus" or "not")
  public void setSubexpression (Expression newChild) {
    
    // verfiy it is a valid expression type
    for (int i = 0; i < unaryTypes.length; i++) {
      if (myType.equals (unaryTypes[i])) {
        leftSubexpression = newChild;
        return;
      }
    }
    
    // it is not valid, so throw an exception
    throw new RuntimeException ("you can't set the subexpression of an " +
                                "expression that is not unary!");
  }
  
  // this gets either the left or the right subexpression, which is only 
  // possible if this is a binary operation... whichOne should either be
  // the string "left" or the string "right"
  public Expression getSubexpression (String whichOne) {
    
    // verfiy it is a valid expression type
    for (int i = 0; i < binaryTypes.length; i++) {
      if (myType.equals (binaryTypes[i])) {
        if (whichOne.equals ("left"))
          return leftSubexpression;
        else if (whichOne.equals ("right"))
          return rightSubexpression;
        else
          throw new RuntimeException ("whichOne must be left or right");
      }
    }
    
    // it is not valid, so throw an exception
    throw new RuntimeException ("you can't get the l/r subexpression of " +
                                "an expression that is not binry!");
  }
  
  // this sets the left and the right subexpression
  public void setSubexpression (Expression left, Expression right) {
    
    // verfiy it is a valid expression type
    for (int i = 0; i < binaryTypes.length; i++) {
      if (myType.equals (binaryTypes[i])) {
        leftSubexpression = left;
        rightSubexpression = right;
        return;
      }
    }
    
    // it is not valid, so throw an exception
    throw new RuntimeException ("you can't set the l/r subexpression of " +
                                "an expression that is not binry!");
  }
}


