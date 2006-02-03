class VariablePointer {
  int variableindex;
  static final int VP_SIMPLE=0;
  static final int VP_ARRAY=1;
  int type;
  GenericType indices;
  String variablename;
  int params;

  VariablePointer(String variablenameparm) {
    type=VP_SIMPLE;
    variablename=variablenameparm;
    params=0;
  }
  VariablePointer(String variablenameparm, GenericType gt) {
    type=VP_ARRAY;
    variablename=variablenameparm;
    indices=gt;
    params=gt.gttop; //??
  }
}

class Machine {
  //
  String code; // here is the code, stored as one big string
  static final int MAXLINES=10000;
  int toplinecache=0;
  int linecacheline[]; // when we get to them, we store the pointer into the code of each line
                           // of course we have to read ahead one we get a GOTO or GOSUB
  int linecachepnt[];

  static final int MAXFORS=30; // make it break faster
  int topforloopstack=0;
  int forloopstack[];
  String forloopstack_var[];
  double forloopstack_to[];
  double forloopstack_step[];

  static final int MAXGOSUBS=300;
  int topgosubstack=0;
  int gosubstack[];

  boolean verbose=false;
  //boolean verbose=true;

  int executionpoint; // ?? where we currently are at

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
  evaluate evaluate_engine;
  boolean enabledmovement=true;  // if this is false, we just parse from top to bottom, good for debugging!
  double  variablearrayvalue1[][]; // single element array
  double  variablearrayvalue2[][][]; // single element array
  String  variablearraystring1[][]; // single element array

  static final int ST_NUM=0;
  static final int ST_STRING=1;

  Machine() {
    if (verbose) { System.out.printf("Initialising machine\n"); }
    linecacheline=new int[MAXLINES];
    linecachepnt=new int[MAXLINES];
    forloopstack=new int[MAXFORS];
    forloopstack_var=new String[MAXFORS];
    forloopstack_to=new double[MAXFORS];
    forloopstack_step=new double[MAXFORS];
    gosubstack=new int[MAXGOSUBS];

    variablename=new String[MAXVARIABLES];
    variablevalue=new double[MAXVARIABLES];
    variablestring=new String[MAXVARIABLES];
    variableint=new int[MAXVARIABLES];
    variabletype=new int[MAXVARIABLES];
    variablearrayvalue1=new double[MAXVARIABLES][];
    variablearrayvalue2=new double[MAXVARIABLES][][];
    topvariable=0;
    executionpoint=0;
    //evaluate_engine = new evaluate(this);  // create engine
       // if we pass a "Machine" class variable, then we have linked
       // the two together such that we can set and get variables
       // is this a good way? I don't know
  }

  void createFORloop(int current, String variable, double forto, double forstep)
  {
    // I think that if we use an already used variable, we pop off the rest of the stack
    // e.g.  FOR I   FOR J   FOR K ......then FOR I again
    if (verbose) { System.out.printf("processing FOR %s to %f step %f at current=%d\n",variable,forto,forstep,current); }
    // find current variable
    for (int i=0; i<topforloopstack; ++i) {
      if (forloopstack_var[i].equals(variable)) {
        // use this one and pop the rest off
        topforloopstack=i;
        break;
      }
    }
    forloopstack[topforloopstack]=current;
    forloopstack_var[topforloopstack]=variable;
    forloopstack_to[topforloopstack]=forto;
    forloopstack_step[topforloopstack]=forstep;
    topforloopstack++;
  }

  boolean processNEXT(int current, String var) {
    if (verbose) { System.out.printf("processing NEXT %s at current=%d\n",var,current); }
    // could be multiple steps?, no, this should be dealt with by the statements parser
    int fl=topforloopstack;
    executionpoint=current;
    while(true) {
      if (fl<=0) { return false; }
      fl--;
      if (var.equals("") || forloopstack_var[fl].equals(var)) {
        // just pop the last one
        setvariable(forloopstack_var[fl],evaluate(forloopstack_var[fl]+"+"+forloopstack_step[fl]));
        if (verbose) { System.out.printf("about to add to loop at stack location %d %f>%f\n",fl,getvariable(forloopstack_var[fl]).num(),forloopstack_to[fl]); }
        if (verbose) { dumpstate(); }
        if (getvariable(forloopstack_var[fl]).num()>forloopstack_to[fl]) {
          if (verbose) { System.out.printf("NEXT: At end of stack\n"); }
          // pop it off, but keep on going, we may be popping off many
          topforloopstack=fl; // at least one
          return false; // I think we need to exit now
        } else {
          if (verbose) { System.out.printf("NEXT: Looping back\n"); }
          // goto the end of that for loop
          executionpoint=forloopstack[fl];
          return true;
        }
      }
    }
    //return false;
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
      //variablearrayvalue2[topvariable]=new double[p1+1];
      //for (int i=0; i<p1+1; ++i) {
        //variablearrayvalue2[topvariable][i]=new double[p2+1];
      //}
      variablearrayvalue2[topvariable][p1][p2]=contents.num();
      variabletype[topvariable]=V_ARRAY_DOUBLE2;
      // str
      // ...
    }
    topvariable++;
    dumpstate();
    return;
  }

  void setvariable(VariablePointer var, GenericType contents) {
    // search for this variable, if it isn't there - then create it
    String variable=var.variablename;
    if (var.type == VariablePointer.VP_SIMPLE) {
      setvariable(variable, contents);
      return;
    }
    int params=var.params;
    int p1=(int)(var.indices.num());
    int p2=(params>=2)?((int)(var.indices.gtlist[1].num())):0;
    int p3=(params>=3)?((int)(var.indices.gtlist[2].num())):0;
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
    createarray(variable,var.params,p1,0,0,contents);
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

  VariablePointer parse(String thestring) {
    if (verbose) { System.out.printf("parsing %s\n",thestring); }
    int at=0;
    // up here - should we get rid of spaces?
    int bracketcount=0;
    boolean isarray=false;
    int start=0;
    while (at<thestring.length()) {

      String a=thestring.substring(at,at+1);
      if (a.equals("(")) {
        // okay, we have an array
        if (!isarray && bracketcount==0) {
          // okay, we mark the start here
          start=at+1;
          isarray=true;
        }
        bracketcount++;
      } else if (a.equals(")")) { bracketcount--; }

      at++;
    }

    if (isarray) {
      if (verbose) { System.out.printf("MACHINE Found an ARRAY variable %s\n",thestring.substring(0,start)); }
      // really, should evaluate the bit in the middle
      // assuming (very big assumption that we end with )= when there is an array assignment)
      if (verbose) { System.out.printf("MACHINE Found an ARRAY contents= %s\n",thestring.substring(start,at-1)); }
      // thestring.substring(0,start) contains the variable
      // machine.evaluate(thestring.substring(start,at-1)) contains the indices
      return new VariablePointer(thestring.substring(0,start), evaluate(thestring.substring(start,at-1)));
    } else {
      // simply thestring contains the variable
      return new VariablePointer(thestring);
    }
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
    evaluate_engine.verbose=verbose; // inherit!
    evaluate_engine.verbose=false;
    evaluate_engine.quiet=true;

    // only for C64Screen
    machinescreen=C64Screen.out;
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
      } else if (variabletype[i]==V_STRING) {
        System.out.printf("  variable %s (string) = %s\n",variablename[i],variablestring[i]);
      } else if (variabletype[i]==V_ARRAY_DOUBLE1) {
        System.out.printf("  variable %s (array of doubles)\n",variablename[i]);
      } else {
        System.out.printf("  unkown type %s\n",variablename[i]);
      }
    }
  }

  C64Screen machinescreen;
  
  // only for screen
  void print(String arg) {
    machinescreen.print(arg);
  }

  void printnewline() {
    machinescreen.println();
  }

}

  /////////////////
 // end Machine //
/////////////////
