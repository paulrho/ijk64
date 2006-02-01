class Machine {
  //
  String code; // here is the code, stored as one big string
  static final int MAXLINES=10000;
  int toplinecache=0;
  int linecacheline[]; // when we get to them, we store the pointer into the code of each line
                           // of course we have to read ahead one we get a GOTO or GOSUB
  int linecachepnt[];

  static final int MAXFORS=300;
  int topforloopstack=0;
  int forloopstack[];
  String forloopstack_var[];

  static final int MAXGOSUBS=300;
  int topgosubstack=0;
  int gosubstack[];

  boolean verbose=false;
  //boolean verbose=true;

  int executionpoint; // ?? where we currently are at

  static final int MAXVARIABLES=300;
  static final int V_DOUBLE=0;
  static final int V_STRING=1;
  int topvariable=0;
  String variablename[];
  int variabletype[];
  double variablevalue[];
  String variablestring[]; // not coded for in evaluate yet
  int variableint[]; // not coded for in evaluate yet
  evaluate evaluate_engine;
  boolean enabledmovement=true;  // if this is false, we just parse from top to bottom, good for debugging!

  static final int ST_NUM=0;
  static final int ST_STRING=1;

  Machine() {
    if (verbose) { System.out.printf("Initialising machine\n"); }
    linecacheline=new int[MAXLINES];
    linecachepnt=new int[MAXLINES];
    forloopstack=new int[MAXFORS];
    forloopstack_var=new String[MAXFORS];
    gosubstack=new int[MAXGOSUBS];

    variablename=new String[MAXVARIABLES];
    variablevalue=new double[MAXVARIABLES];
    variablestring=new String[MAXVARIABLES];
    variableint=new int[MAXVARIABLES];
    variabletype=new int[MAXVARIABLES];
    topvariable=0;
    executionpoint=0;
    //evaluate_engine = new evaluate(this);  // create engine
       // if we pass a "Machine" class variable, then we have linked
       // the two together such that we can set and get variables
       // is this a good way? I don't know
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
    return;
  }

  // void setvariable(String variable, double val) {
    // // search for this variable, if it isn't there - then create it
    // for (int i=0; i<topvariable; ++i) {
      // if (variable.equals(variablename[i])) {
        // // found it, so set it
        // variablevalue[i]=val;
        // return;
      // }
    // }
    // createvariable(variable,val);
    // return;
  // }

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
    } else {
      createvariable(variable,new GenericType(0.0));
    }
    return new GenericType(0.0);
  }

  // from within here we execute the evaluate?
  GenericType evaluate(String expression) {
    return evaluate_engine.interpret_string(expression);
  }

  void initialise_engines() {
    evaluate_engine = new evaluate(this);  // create engine
    //evaluate_engine.verbose=true;
    //evaluate_engine.verbose=false;
    evaluate_engine.verbose=verbose; // inherit!
    evaluate_engine.quiet=true;
  }

  void gotoLine(String lineNostr) {
    int value=Integer.parseInt(lineNostr);
    gotoLine(value);
  }

  void gotoLine(int lineNo) {
    // put code in here
    // look up line in cache and set pnt to correct place and return
    for (int i=0; i<toplinecache; ++i) {
      if (linecacheline[i]==lineNo) {
        executionpoint=linecachepnt[i]; // set this, and then pnt must be set after return
        return;
      }
    }
    System.out.printf("?LINE NOT FOUND\n");
    return; // should fail
  }

  void gosubLine(String lineNostr, int pnt) {
    int value=Integer.parseInt(lineNostr);
    gosubLine(value,pnt);
  }

  void gosubLine(int lineNo, int pnt) {
    // push on the stack where we are
    gosubstack[topgosubstack]=pnt;
    topgosubstack++;
    gotoLine(lineNo);
  }

  void popReturn() {
    if (topgosubstack>0) {
      topgosubstack--;
      executionpoint=gosubstack[topgosubstack];
    } else {
      System.out.printf("?TOO MANY RETURNS\n");
    }
  }

  void cacheLine(String lineNostr, int pnt) {
    int value=Integer.parseInt(lineNostr);
    if (verbose) { System.out.printf("Caching line %d pointer at %d\n",value,pnt); }
    linecacheline[toplinecache]=value;
    linecachepnt[toplinecache]=pnt;
    toplinecache++;
  }

  void dumpstate() {
    for (int i=0; i<topvariable; ++i) {
      if (variabletype[i]==V_DOUBLE) {
        System.out.printf("  variable %s = %f\n",variablename[i],variablevalue[i]);
      } else {
        System.out.printf("  variable %s (string) = %s\n",variablename[i],variablestring[i]);
      }
    }
  }
}

  /////////////////
 // end Machine //
/////////////////
