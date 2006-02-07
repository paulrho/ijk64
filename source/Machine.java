////////////////////////////////////////////////////////////////////
// 
// Machine
// 
////////////////////////////////////////////////////////////////////
class Machine {
  //
  String code; // here is the code, stored as one big string
  Variables variables=new Variables();

  // line cache
  static final int MAXLINES=10000;
  int toplinecache=0;
  int linecacheline[]; // when we get to them, we store the pointer into the code of each line // of course we have to read ahead one we get a GOTO or GOSUB
  int linecachepnt[];

  // for stack
  static final int MAXFORS=30; // make it break faster
  int topforloopstack=0;
  int forloopstack[];
  String forloopstack_var[];
  double forloopstack_to[];
  double forloopstack_step[];

  // gosub stack
  static final int MAXGOSUBS=300;
  int topgosubstack=0;
  int gosubstack[];

  evaluate evaluate_engine;
  int executionpoint; // ?? where we currently are at

  boolean verbose=false;
  //boolean verbose=true;
  boolean enabledmovement=true;  // if this is false, we just parse from top to bottom, good for debugging!
  String currentLineNo="";


  Machine() {
    if (verbose) { System.out.printf("Initialising machine\n"); }
    linecacheline=new int[MAXLINES];
    linecachepnt=new int[MAXLINES];
    forloopstack=new int[MAXFORS];
    forloopstack_var=new String[MAXFORS];
    forloopstack_to=new double[MAXFORS];
    forloopstack_step=new double[MAXFORS];
    gosubstack=new int[MAXGOSUBS];

    variables.verbose=verbose;
    executionpoint=0;
    //evaluate_engine = new evaluate(this);  // create engine
       // if we pass a "Machine" class variable, then we have linked
       // the two together such that we can set and get variables
       // is this a good way? I don't know
  }

  // access to variables etc
  // could we do this in a nice way with an "interface?" or something
  void setvariable(String variable, int params, int p1, int p2, int p3, GenericType contents) {
    variables.setvariable(variable,params,p1,p2,p3,contents);
  }
  //void setvariable(VariablePointer variable, GenericType contents) {
    //variables.setvariable(variable,contents);
  //}
  void setvariable(String variable, GenericType contents) {
    variables.setvariable(variable, contents);
  }
  GenericType getvariable(String variable, int param, int p1, int p2, int p3) {
    return variables.getvariable(variable,param,p1,p2,p3);
  }
  GenericType getvariable(String variable) {
    return variables.getvariable(variable);
  }

  // continue...
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
        setvariable(forloopstack_var[fl].toLowerCase(),evaluate(forloopstack_var[fl].toLowerCase()+"+"+forloopstack_step[fl]));
        if (verbose) { System.out.printf("about to add to loop at stack location %d %f>%f\n",fl,getvariable(forloopstack_var[fl]).num(),forloopstack_to[fl]); }
        if (verbose) { dumpstate(); }
        if (getvariable(forloopstack_var[fl].toLowerCase()).num()>forloopstack_to[fl]) {
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

  // from within here we execute the evaluate?
  GenericType evaluate(String expression) {
    return evaluate_engine.interpret_string(expression);
  }
  void assignment(String expression) {
    evaluate_engine.interpret_string_with_assignment(expression);
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
    variables.dumpstate();
  }

//////////////////////////////////
// DATA staement DATA STREAM
//////////////////////////////////
String allDATA="";
int uptoDATA=0;

void cacheDataAdd(String param) {
  allDATA+=param;
}

void cacheAllDATA()
{
  // for now, we just simple read in all the DATA statements
  // also, expecting it to be the first keyword on the line
  // and wack it into a big string
}

GenericType metareaddatastreamNum()
{
  String str=metareaddatastreamString().str();
  if (str.equals("")) return new GenericType(0.0);
  else return new GenericType(Double.parseDouble(str));
}

GenericType metareaddatastreamString()
{
  // from whatever we happen to be reading (say DATA) return a string
  String building="";
  boolean quoted=false;
  for (; uptoDATA<allDATA.length(); ++uptoDATA) {
    String a=allDATA.substring(uptoDATA,uptoDATA+1);
    if (a.equals("\n")) {
      uptoDATA++;
      break;
    } else if (!quoted && a.equals(",")) {
      uptoDATA++;
      break;
    } else if (a.equals("\"")) {
      quoted=!quoted;
      //building+=a; - no, chew this up!
    } else { building+=a; }
  }
  if (verbose) { System.out.printf("Returning DATA >>>%s<<<\n",building); }
  return new GenericType(building);
}

//////////////////////////////////
// Memory I/O
//////////////////////////////////
  void performPOKE(GenericType gt) {
    // here we should have a list of variables
    int memloc;
    int memval;
    if (gt.gttop==2) {
      memloc=(int)gt.gtlist[0].num();
      memval=(int)gt.gtlist[1].num();
      System.out.printf("Poking memory location %d with variable %d\n",memloc,memval);
      performPOKE(memloc, memval);
    } else {
      System.out.printf("Wrong number of parameters\n");
    }
  }

  void performPOKE(int memloc, int memval) {
    // the background and border may be round the wrong way, cant remember
    if (memloc==198) {
      // clear the key buffer if you get this
    } else if (memloc==53280) {
      // background
      machinescreen.backgroundColour = machinescreen.fullcolour[memval];
      machinescreen.reshapeScreen(); // just to see - this is a dodgy work around!!! when changing background or border colours, mu
    } else if (memloc==53281) {
      // border
      machinescreen.borderColour = machinescreen.fullcolour[memval];
      machinescreen.reshapeScreen(); // just to see - this is a dodgy work around!!! when changing background or border colours, mu
    }
  }
//////////////////////////////////
// Screen
//////////////////////////////////

  C64Screen machinescreen;
  
  // only for screen
  void print(String arg) {
    machinescreen.print(arg);
  }

  void printnewline() {
    machinescreen.println();
  }

  String getline() {
    return machinescreen.screenInput();
  }
  String getkey() {
    if (machinescreen.hasinput()) {
      return ""+machinescreen.givemekey();
    } else {
      return "";
    }
  }

  // statements no longer have any meaning unless we are operating on a machine
   // now for different instansiation order
  void statements(String[] args) {
    new statements(args, this); // tell the statements class who I am
  }
  
  void statements(String arg) {
    new statements(arg, this); // tell the statements class who I am
  }

}

  /////////////////
 // end Machine //
/////////////////
