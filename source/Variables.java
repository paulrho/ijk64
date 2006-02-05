////////////////////////////////////////////////////////////////////
// 
// Variables
// 
////////////////////////////////////////////////////////////////////
class Variables {
  static final int MAXVARIABLES=300;
  static final int V_DOUBLE=0;
  static final int V_STRING=1;
  static final int V_ARRAY_DOUBLE1=101;
  static final int V_ARRAY_DOUBLE2=102;
  static final int V_ARRAY_STRING1=111;
  int     topvariable=0;
  String  variablename[];
  int     variabletype[];
  double  variablevalue[];
  String  variablestring[]; // not coded for in evaluate yet
  int     variableint[]; // not coded for in evaluate yet
  double  variablearrayvalue1[][]; // single element array
  double  variablearrayvalue2[][][]; // single element array
  String  variablearraystring1[][]; // single element array

  boolean verbose=false;

  Variables() {
    variablename=new String[MAXVARIABLES];
    variablevalue=new double[MAXVARIABLES];
    variablestring=new String[MAXVARIABLES];
    variableint=new int[MAXVARIABLES];
    variabletype=new int[MAXVARIABLES];
    variablearrayvalue1=new double[MAXVARIABLES][];
    variablearrayvalue2=new double[MAXVARIABLES][][];
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
      // num
      variablename[topvariable]=variable;
      variablearrayvalue1[topvariable]=new double[p1+1];
      variablearrayvalue1[topvariable][p1]=contents.num();
      variabletype[topvariable]=V_ARRAY_DOUBLE1;
      // str
      // ...
    } else if (params==2) {
      // num
      variablename[topvariable]=variable;
      variablearrayvalue2[topvariable]=new double[p1+1][p2+1];
      variablearrayvalue2[topvariable][p1][p2]=contents.num();
      variabletype[topvariable]=V_ARRAY_DOUBLE2;
      // str
      // ...
    }
    topvariable++;
    dumpstate();
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
        } else {
          variabletype[i]=V_STRING;
          variablearraystring1[i][p1]=contents.str();
        }
        return;
      }
    }
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
          if (variabletype[i]==V_ARRAY_DOUBLE1) {
            if (verbose) { System.out.printf("Returning value of array variablearrayvalue1[%d][%d]\n",i,p1); }
            if (verbose) { System.out.printf("Returning value of array variablearrayvalue1[%d][%d]=%f\n",i,p1,variablearrayvalue1[i][p1]); }
            return new GenericType(variablearrayvalue1[i][p1]);
          }
        } else if (param==2) {
          if (variabletype[i]==V_ARRAY_DOUBLE2) {
            if (verbose) { System.out.printf("Returning value of array variablearrayvalue2[%d][%d][%d]\n",i,p1,p2); }
            if (verbose) { System.out.printf("Returning value of array variablearrayvalue2[%d][%d][%d]=%f\n",i,p1,p2,variablearrayvalue2[i][p1][p2]); }
            return new GenericType(variablearrayvalue2[i][p1][p2]);
          }
        }
      }
    }
    // we did not find the array
    createarray(variable,param,p1,p2,p3, new GenericType());
    return new GenericType();
  }

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
        System.out.printf("  variable %s (array of doubles)\n",variablename[i]);
      } else {
        System.out.printf("  unkown type %s\n",variablename[i]);
      }
    }
  }
} // end Variables