/////////////////////////////////////////////////////////////////////////////////
//
// $Id: GenericType.java,v 1.9 2012/04/18 06:07:53 pgs Exp $
//
// $Log: GenericType.java,v $
// Revision 1.9  2012/04/18 06:07:53  pgs
// Adding graphics capability
//
// Revision 1.8  2011/07/03 23:00:20  pgs
// Add EVAL$ function
// Fix insertspace etc modes - they were buggy
//
// Revision 1.7  2006/02/15 01:54:04  ctpgs
// Standard header
//
//
/////////////////////////////////////////////////////////////////////////////////

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
    if (type==ST_STRING) astring=new String(base.astring);
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

  GenericType(boolean no) {
    type=-1;
    astring=""; // just in case
    gttop=0; // effectively a null list
  }

  GenericType(double val) {
    type=ST_NUM;
    aval=val;
  }

  // here we add to a list
  void add(double val, int hint) {
    //add a generic type
   if (gttop==1) {
     gtlist=new GenericType[hint]; // empty types // funny way to do this - watch that this doesnt get blown
                                 // 20170405 - yes it did get blown, for now - make it the same as what we want!
     gtlist[0]=new GenericType(this); // duplicate this generic type and put it on the list
   }
   // add to list
   gtlist[gttop]=new GenericType(val);
   gttop++;
  }

  void add(String thestring, int hint) {
    //add a generic type
   if (gttop==1) {
     gtlist=new GenericType[hint]; // empty types // dont forget this one too! // see above
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

  // interesting puzzle here -
  // this is GenericType and printing it could be any format so desired
  // but as I want this in a C64 format, we will use that
  // perhaps there should be multiple output formats available?
  String print() {
    if (isNum()) {
      // if it is an interger, return no trailing decimal
      //return new Double(aval).toString();
      if (aval-(int)aval==0) {
        if (aval>=0.0) {
          return " "+new Integer((int)aval).toString()+" ";
        } else {
          return new Integer((int)aval).toString()+" ";
        }

      } else {
        // just for now - put it in lower case (e)
        if (aval>=0.0) {
          return " "+new Double(aval).toString().toLowerCase()+" ";
        } else {
          return new Double(aval).toString().toLowerCase()+" ";
        }
      }
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

/////////
// END //
/////////
