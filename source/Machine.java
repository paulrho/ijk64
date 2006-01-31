class Machine {
  //
  String code; // here is the code, stored as one big string
  static final int MAXLINES=10000;
  int linecache[]; // when we get to them, we store the pointer into the code of each line
                           // of course we have to read ahead one we get a GOTO or GOSUB
  static final int MAXFORS=300;
  int forloopstack[];
  String forloopstack_var[];

  static final int MAXGOSUBS=300;
  int gosubstack[];

  int executionpoint; // ?? where we currently are at

  static final int MAXVARIABLES=300;
  static int V_DOUBLE=0;
  int topvariable=0;
  String variablename[];
  int variabletype[];
  double variablevalue[];
  String variablestring[]; // not coded for in evaluate yet
  int variableint[]; // not coded for in evaluate yet
  evaluate evaluate_engine;

  Machine() {
    System.out.printf("Initialising machine\n");
    linecache=new int[MAXLINES];
    forloopstack=new int[MAXFORS];
    forloopstack_var=new String[MAXFORS];
    gosubstack=new int[MAXGOSUBS];
    variablename=new String[MAXVARIABLES];
    variablevalue=new double[MAXVARIABLES];
    variablestring=new String[MAXVARIABLES];
    variableint=new int[MAXVARIABLES];
    executionpoint=0;
    evaluate_engine = new evaluate(this);  // create engine
       // if we pass a "Machine" class variable, then we have linked
       // the two together such that we can set and get variables
       // is this a good way? I don't know
  }

  private createvariable(String variable, double val) {
    variablename[topvariable]=variable;
    variablevalue[topvariable]=val;
    variabletype[topvariable]=V_DOUBLE;
    topvariable++;
    return;
  }

  void setvariable(String variable, double val) {
    // search for this variable, if it isn't there - then create it
    for (int i=0; i<topvariable; ++i) {
      if (variable.equals(variablename[i])) {
        // found it, so set it
        variablevalue[i]=val;
        return;
      }
    }
    createvariable(variable,val);
    return;
  }

  double getvariable(String variable) {
    // if we try to get a non existant variable, create it and set its value to 0.0
    for (int i=0; i<topvariable; ++i) {
      if (variable.equals(variablename[i])) {
        // found it, so set it
        return variablevalue[i];
      }
    }
    createvariable(variable,0.0);
    return 0.0;
  }

  // from within here we execute the evaluate?
  double evaluate(String expression) {
    return evaluate_engine.interpret_string(expression);
  }
}

  /////////////////
 // end Machine //
/////////////////
