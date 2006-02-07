class GenericType {

  static final int ST_NUM=0;
  static final int ST_STRING=1;
  static final int ST_LIST=10; // these are comma separated (possible mixed) values

  int type;
  double aval;
  String astring;
  GenericType gtlist[]; // an array of generic types
  int gttop=1; // normally is initialised as a singleton

  GenericType(GenericType base)
  {
    type=base.type;
    aval=base.aval;
  }

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

  // here we add to a list
  void add(double val) {
    //add a generic type
   if (gttop==1) {
     gtlist=new GenericType[50]; // empty types // funny way to do this - watch that this doesnt get blown
     gtlist[0]=new GenericType(this); // duplicate this generic type and put it on the list
   }
   // add to list
   gtlist[gttop]=new GenericType(val);
   gttop++;
  }

  void add(String thestring) {
    //add a generic type
   if (gttop==1) {
     gtlist=new GenericType[50]; // empty types // dont forget this one too!
     gtlist[0]=new GenericType(this); // duplicate this generic type and put it on the list
   }
   // add to list
   gtlist[gttop]=new GenericType(thestring);
   gttop++;
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
    } else if (type==ST_STRING) {
      if (false) { System.out.printf("Returning a String\n"); }
      return astring;
    } else if (type==ST_LIST) {
      // in turn, separated by commas, display all:
      String building="";
      for (int i=0; i<gttop; ++i) {
        if (i!=0) { building+=","; }
        building+=gtlist[i].print();
      }
      return building;
    } else {
      return "";
    }
  }

  boolean equals(double val) {
    if (isNum()) { 
      return val==aval;
    } else { return false; }
  }
}

