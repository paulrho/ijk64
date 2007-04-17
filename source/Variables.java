/////////////////////////////////////////////////////////////////////////////////
//
// $Id: Variables.java,v 1.8 2006/02/15 01:56:07 pgs Exp $
//
// $Log: Variables.java,v $
// Revision 1.8  2006/02/15 01:56:07  pgs
// Standard header
//
//
/////////////////////////////////////////////////////////////////////////////////

class Variables {
  static final int MAXVARIABLES=300;
  static final int V_DOUBLE=0;
  static final int V_STRING=1;
  static final int V_ARRAY_DOUBLE1=101;
  static final int V_ARRAY_DOUBLE2=102;
  static final int V_ARRAY_STRING1=111;
  static final int V_ARRAY_STRING2=112;
  int     topvariable=0;
  String  variablename[];
  int     variabletype[];
  double  variablevalue[];
  String  variablestring[]; // not coded for in evaluate yet
  int     variableint[]; // not coded for in evaluate yet
  double  variablearrayvalue1[][]; // single element array
  double  variablearrayvalue2[][][]; // single element array
  String  variablearraystring1[][]; // single element array
  String  variablearraystring2[][][]; // single element array // not quite implemented (for returning)

  boolean verbose=false;

  Variables() {
    variablename=new String[MAXVARIABLES];
    variablevalue=new double[MAXVARIABLES];
    variablestring=new String[MAXVARIABLES];
    variableint=new int[MAXVARIABLES];
    variabletype=new int[MAXVARIABLES];
    variablearrayvalue1=new double[MAXVARIABLES][];
    variablearrayvalue2=new double[MAXVARIABLES][][];
    variablearraystring1=new String[MAXVARIABLES][];
    variablearraystring2=new String[MAXVARIABLES][][];
    topvariable=0;
  }
  private void createvariable(String variable, GenericType contents) {
    variablename[topvariable]=variable;
    if (contents.isNum()) {
      variablevalue[topvariable]=contents.num();
      variabletype[topvariable]=V_DOUBLE;
    } else {
      variablestring[topvariable]=contents.str();
      variabletype[topvariable]=V_STRING;
    }
    topvariable++;
    if (verbose) { dumpstate(); }
    return;
  }

  private void createarray(String variable, int params, int p1, int p2, int p3, GenericType contents) {
    if (verbose) { System.out.printf("Dimensioning the array with params=%d [%d,%d,%d]\n",params,p1,p2,p3); }
    if (verbose) { System.out.printf("creating array with %d elements\n",p1+1); }
    if (params==1) {
      
      // special case again for C64 - minimum of 10 (11) slots for 1 dimensional array only
      // really should only do this if it is not a DIM statement (but here we dont know the difference)
      int pset=p1;
      if (p1<11) p1=11;
      if (contents.isNum()) {
        // num
        variablename[topvariable]=variable;
        variablearrayvalue1[topvariable]=new double[p1+1];
        variablearrayvalue1[topvariable][pset]=contents.num();
        variabletype[topvariable]=V_ARRAY_DOUBLE1;
      } else {
        // str
        variablename[topvariable]=variable;
        variablearraystring1[topvariable]=new String[p1+1];
        variablearraystring1[topvariable][pset]=contents.str();
        variabletype[topvariable]=V_ARRAY_STRING1;
      }
    } else if (params==2) {
      if (contents.isNum()) {
        // num
          variablename[topvariable]=variable;
        variablearrayvalue2[topvariable]=new double[p1+1][p2+1];
        variablearrayvalue2[topvariable][p1][p2]=contents.num();
        variabletype[topvariable]=V_ARRAY_DOUBLE2;
      } else {
        // str
        variablename[topvariable]=variable;
        variablearraystring2[topvariable]=new String[p1+1][p2+1];
        variablearraystring2[topvariable][p1][p2]=contents.str();
        variabletype[topvariable]=V_ARRAY_STRING2;
      }
    }
    topvariable++;
    if (verbose) { dumpstate(); }
    return;
  }

  // used only for evuated parsed assignment
  void setvariable(String variable, int params, int p1, int p2, int p3, GenericType contents) {
    if (verbose) { System.out.printf("This is a array parms=%d [%d][%d][%d] for array %s\n",params,p1,p2,p3,variable); }
    for (int i=0; i<topvariable; ++i) {
      if (variable.equals(variablename[i])) {
        // found it, so set it
        if (variabletype[i]==V_ARRAY_DOUBLE1) {
          variablearrayvalue1[i][p1]=contents.num();
          if (verbose) { System.out.printf("Setting variablearrayvalue[%d][%d] to value %f\n",i,p1,variablearrayvalue1[i][p1]); }
        } else if (variabletype[i]==V_ARRAY_DOUBLE2) {
          variablearrayvalue2[i][p1][p2]=contents.num();
          if (verbose) { System.out.printf("Setting variablearrayvalue[%d][%d][%d] to value %f\n",i,p1,p2,variablearrayvalue2[i][p1][p2]); }
        } else if (variabletype[i]==V_ARRAY_STRING1) {
          variablearraystring1[i][p1]=contents.str();
          if (verbose) { System.out.printf("Setting variablearrayvalue[%d][%d] to value %s\n",i,p1,variablearraystring1[i][p1]); }
        } else if (variabletype[i]==V_ARRAY_STRING2) {
          variablearraystring2[i][p1][p2]=contents.str();
          if (verbose) { System.out.printf("Setting variablearrayvalue[%d][%d][%d] to value %s\n",i,p1,p2,variablearraystring2[i][p1][p2]); }
        } else { // really, should check this
          variabletype[i]=V_STRING;
          variablearraystring1[i][p1]=contents.str();
        }
        return;
      }
    }
    if (verbose) { System.out.printf("Creating the array %s via the contents\n",variable); }
    createarray(variable,params,p1,0,0,contents);
  }

  void setvariable(String variable, GenericType contents) {
    // search for this variable, if it isn't there - then create it
    for (int i=0; i<topvariable; ++i) {
      if (variable.equals(variablename[i])) {
        // found it, so set it
        if (contents.isNum()) {
          variabletype[i]=V_DOUBLE;
          variablevalue[i]=contents.num();
        } else {
          variabletype[i]=V_STRING;
          variablestring[i]=contents.str();
        }
        return;
      }
    }
    createvariable(variable,contents);
    return;
  }

  GenericType getvariable(String variable, int param, int p1, int p2, int p3) {
    // array
    if (verbose) { System.out.printf("getvariable(%s,param=%d,%d,%d,%d\n",variable,param,p1,p2,p3); }
    for (int i=0; i<topvariable; ++i) {
      if (variable.equals(variablename[i])) {
        if (param==1) {
          if (verbose) { System.out.printf("Returning value of array variablearrayvalue1[%d][%d]\n",i,p1); }
          if (variabletype[i]==V_ARRAY_DOUBLE1) {
            if (verbose) { System.out.printf("Returning value of array variablearrayvalue1[%d][%d]=%f\n",i,p1,variablearrayvalue1[i][p1]); }
            return new GenericType(variablearrayvalue1[i][p1]);
          } else if (variabletype[i]==V_ARRAY_STRING1) {
            if (verbose) { System.out.printf("Returning value of array variablearraystring2[%d][%d]=%s\n",i,p1,variablearraystring1[i][p1]); }
            if (variablearraystring1[i][p1]==null) variablearraystring1[i][p1]=""; // default to empty string!
            return new GenericType(variablearraystring1[i][p1]);
          }
        } else if (param==2) {
          if (variabletype[i]==V_ARRAY_DOUBLE2) {
            if (verbose) { System.out.printf("Returning value of array variablearrayvalue2[%d][%d][%d]\n",i,p1,p2); }
            if (verbose) { System.out.printf("Returning value of array variablearrayvalue2[%d][%d][%d]=%f\n",i,p1,p2,variablearrayvalue2[i][p1][p2]); }
            return new GenericType(variablearrayvalue2[i][p1][p2]);
          } else if (variabletype[i]==V_ARRAY_STRING2) {
            if (verbose) { System.out.printf("Returning value of array variablestringvalue2[%d][%d][%d]=%s\n",i,p1,p2,variablearraystring2[i][p1][p2]); }
            return new GenericType(variablearraystring2[i][p1][p2]);
          }
        }
      }
    }
    // we did not find the array
    // we need to work out what type to create it as
    // base this on the variable name (did this is evaluate too)

    // work out the default type:
    if (variable.substring(variable.length()-2,variable.length()).equals("$(")) {
      if (verbose) { System.out.printf("Creating the array %s via the variable name (string)\n",variable); }
      createarray(variable,param,p1,p2,p3, new GenericType(""));
      return new GenericType("");
    } else {
      if (verbose) { System.out.printf("Creating the array %s via the variable name (num)\n",variable); }
      createarray(variable,param,p1,p2,p3, new GenericType(0.0));
      return new GenericType(0.0);
    }
  }

  //not used yet//  int determine_type(String variable) 
  //not used yet// {
  //not used yet//   int end=variable.length()-1;
  //not used yet//   String lastchar=variable.substring(end,end+1);
  //not used yet//   if (lastchar.equals("$")) {
  //not used yet//     return V_STRING;
  //not used yet//   else if (lastchar.equals("%")) 
  //not used yet//     return V_DOUBLE; // for now
  //not used yet//   else
  //not used yet//     return V_DOUBLE;
  //not used yet// }

  GenericType getvariable(String variable) {
    // if we try to get a non existant variable, create it and set its value to 0.0
    for (int i=0; i<topvariable; ++i) {
      if (variable.equals(variablename[i])) {
        // found it, so set it
        if (variabletype[i]==V_DOUBLE) {
          return new GenericType(variablevalue[i]);
        } else {
          return new GenericType(variablestring[i]);
        }
      }
    }
    // work out the default type:
    if (variable.substring(variable.length()-1,variable.length()).equals("$")) {
      createvariable(variable,new GenericType(""));
      return new GenericType("");
    } else {
      createvariable(variable,new GenericType(0.0));
      return new GenericType(0.0);
    }
  }

  void dumpstate() {
    for (int i=0; i<topvariable; ++i) {
      if (variabletype[i]==V_DOUBLE) {
        System.out.printf("  variable %s = %f\n",variablename[i],variablevalue[i]);
      } else if (variabletype[i]==V_STRING) {
        System.out.printf("  variable %s (string) = %s\n",variablename[i],variablestring[i]);
      } else if (variabletype[i]==V_ARRAY_DOUBLE1) {
        System.out.printf("  variable %s (array of doubles) size = %d\n",variablename[i],variablearrayvalue1[i].length);
      } else if (variabletype[i]==V_ARRAY_DOUBLE2) {
        System.out.printf("  variable %s (array of doubles 2 dimension) size = %d,%d\n",variablename[i],variablearrayvalue2[i].length,variablearrayvalue2[i][0].length);
      } else if (variabletype[i]==V_ARRAY_STRING1) {
        System.out.printf("  variable %s (array of strings) size = %d\n",variablename[i],
          variablearraystring1[i].length);
      } else if (variabletype[i]==V_ARRAY_STRING2) {
        System.out.printf("  variable %s (array of strings 2 dimension) size = %d,%d\n",variablename[i],
        variablearraystring2[i][0].length);
      } else {
        System.out.printf("  unknown type %s\n",variablename[i]);
      }
    }
  }
} // end Variables

/////////
// END //
/////////
