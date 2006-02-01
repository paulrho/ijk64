class GenericType {

  static final int ST_NUM=0;
  static final int ST_STRING=1;

  int type;
  double aval;
  String astring;

  GenericType()
  { //default
    type=ST_NUM;
    aval=0.0;
    //astring="";
  }

  GenericType(String thestring) {
    type=ST_STRING;
    astring=thestring;
  }

  GenericType(double val) {
    type=ST_NUM;
    aval=val;
  }

  boolean isNum() {
    if (type==ST_NUM) { return true; } else { return false; }
  }

  double num() {
    return aval;
  }

  String str() {
    return astring;
  }

  String print() {
    if (isNum()) {
      return new Double(aval).toString();
    } else {
      if (false) { System.out.printf("Returning a String\n"); }
      return astring;
    }
  }

  boolean equals(double val) {
    if (isNum()) { 
      return val==aval;
    } else { return false; }
  }
}

