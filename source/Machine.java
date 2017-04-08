/////////////////////////////////////////////////////////////////////////////////
//
// $Id: Machine.java,v 1.39 2012/04/30 09:45:34 pgs Exp $
//
// $Log: Machine.java,v $
// Revision 1.39  2012/04/30 09:45:34  pgs
// resizing ability
// fixing (but not fixed yet) character tranlation mapping
//
// Revision 1.38  2012/04/18 06:07:53  pgs
// Adding graphics capability
//
// Revision 1.37  2011/07/03 23:00:20  pgs
// Add EVAL$ function
// Fix insertspace etc modes - they were buggy
//
// Revision 1.36  2011/06/27 23:33:55  pgs
// More changes for 325 cut/paste, insertchar mode, List params
//
// Revision 1.34  2007/04/22 22:37:01  pgs
// Add duty cycle
//
// Revision 1.33  2007/04/19 08:28:24  pgs
// Refactoring and simplifying/formatting code especially in Machine
//
// Revision 1.32  2007/04/18 09:37:19  pgs
// More refactoring with regards to creating program/immediate modes
//
// Revision 1.31  2007/04/17 21:46:14  pgs
// Modifications to get CONT to work properly
//
// Revision 1.30  2007/04/17 09:22:33  pgs
// Adding ability to restart with CONT
// moved all statments into statements/Machine engine
//
// Revision 1.29  2007/04/16 21:31:00  pgs
// Complete exception creation, ratify error messages, refactor code
// to use exceptions (makes code clearer)
//
// Revision 1.27  2007/04/13 09:32:43  pgs
// programText now in Machine - and used this way from C64
// in preparation for C64 online editting of program
// Added ability to enter line numbers and change program
//
// Revision 1.26  2007/04/11 17:45:38  pgs
// snapshot 20070411
//
// Revision 1.25  2007/03/28 21:25:59  pgs
// clear line cache and forloop/gosub stacks before running new program
//
// Revision 1.23  2006/02/19 22:32:02  pgs
// Fix for loop algorith, repeated variables clear the stack before and including that
// variable and the stack is squished up.
// this allows for un-nexted fori forj fork fori nexti nextk nextj to work properly
//
// Revision 1.22  2006/02/16 05:53:51  ctpgs
// Correctly check the loop finish test depending on whether +ve or -ve step value
//
// Revision 1.21  2006/02/15 22:49:09  pgs
// Allow peeking of 197 in a simplistic fashion
//
// Revision 1.20  2006/02/15 21:27:44  pgs
// Add a evaluate_partial which simply calls
// evaluate.interpret_string_partial
//
// Revision 1.19  2006/02/15 01:54:46  ctpgs
// Standard header
//
//
/////////////////////////////////////////////////////////////////////////////////

// package au.com.futex.jebi

// for reading a file
import java.io.*;
import java.util.Arrays; // for dir sorted
// for dir sorted
import java.util.Comparator;
import java.text.SimpleDateFormat; // for DIR

import java.net.*; // for reading from http

/** Machine modules :
<PRE>
    +-------------------------
    | VStore : Variable Store
    +-------------------------
    | FStack : FOR/NEXT Stack
    | GStack : Gosub Stack
    +-------------------------
    | PText  : Program/Direct text and exection/data points
    |   ProgramText                  : The program BASIC code itself
    |   DirectText                   : The line being executed in immediate (direct) mode [notionally exists]
    |   ---------------------
    |   executionpoint               : The point in the program we are actually parsing/executing
    |                                  not always kept up to date (pnt in statements is up to date)
    |   program_saved_executionpoint : Saved for use in CONT
    |   uptoDATA                     : The point in the program we are upto with DATA READ
    +-------------------------
    | MemoryIO                       : Pokes/peeks/sys
    +-------------------------
    | LCache : Line Cache
    | DCache : Data Cache
    +-------------------------
    | ScreenIO                       : Hooks to C64Screen (if exists)
    +-------------------------
</PRE>
  <BR>
<PRE>
   Object interdependance :

                  ,----+ C64Screen           (precreated)
                 / ,+--+ C64PopupMenu        (precreated)               {+-+links back to machine}
                / /
   C64-+ Machine 
                 -+ FStack                   {internal}
                 -+ GStack                   {internal}
                 -+ PText                    {internal, including execution/data/save points}
                 -+ LCache                   {internal}
                 -+ DCache                   {internal}
           -+ Variables                      (Variable Store (VStore))
           +-+ evaluate                      (evaluate engine)          [ could be renamed : ExpressionEvaluator ]
                 interpret_string ( string )                            {+-+links back to machine}
           +-+ statements ( string )         (parse the program/direct) [ could be renamed : CxxParser ]
               [instansiated each time]                                 {+-+links back to machine}
  
   Passing datatype : GenericType
   Exceptions       : BasicException and EvaluateException
</PRE>
  <BR>
  Notes:
  In immediate (direct) mode, we cannot push onto GStack, but we can push onto FStack
  Pulling off either (by doing RETURN or NEXT) doesnt really make sense, but will do
  wierd things (*need to re-architect to fix).

**/

public class Machine {

  /*************************
    Create the machine
   *************************/
  public Machine() {
    initialise_machine();
  }

  public Machine(C64Screen screen) {
    initialise_machine();
    attachScreen(screen);
  }

  public void initialise_machine() {
    if (verbose) { System.out.printf("Initialising machine\n"); }
    variables.verbose=verbose;
    executionpoint=0;
    evaluate_engine = new evaluate(this);  // create engine
    evaluate_engine.verbose=false;
    evaluate_engine.quiet=true;
    System.setProperty("java.net.useSystemProxies", "true");
  }
  //////////////////////////////////
  // Machine configuration
  //////////////////////////////////
  public boolean verbose=false;
  boolean enabledmovement=true;  // if this is false, we just parse from top to bottom, good for debugging!
  int partialDutyCycle=0;
  public boolean signal_exit=false;
  boolean hasSyntaxHighlighting=true;
  boolean crlfText=false;

  boolean basictimer; // passed directly to statements
  
  //////////////////////////////////
  // Machine State
  //////////////////////////////////
  int executionpoint; // ?? where we currently are at
  int save_executionpoint;
  int program_saved_executionpoint=(-1);
  String currentLineNo=""; // info only?

  /** clear the machine state
      Empty LCache
      Empty FStack
      Empty GStack
      Empty DCache
  **/
  void clearMachineState() {
    toplinecache=0; // optimisation only & goto/gosub lookup & findcurrentline
    toplabcache=0;
    topforloopstack=0;
    topgosubstack=0;
    // also clear data
    allDATA="";
    uptoDATA=0;
    CloseAllFiles();
    //topHandle=0; // clear open files list // should really close all open nicely!
  }

  /** CLeaRs all machine state variables
      Delete all variables
      Reset the ability to CONTinue the program
   **/
  void variables_clr() {
    // reset all variables, by creating new ones (are the old ones garbage collected properly?
    boolean verbosekeep=variables.verbose;
    variables=new Variables();
    variables.verbose=verbosekeep;
    program_saved_executionpoint=(-1); // cant continue any more
    //!!! should forloopstack be cleared too? // no -statements clears it
  }

  //////////////////////////////////
  // VStore : Variable (&fn) Store
  //////////////////////////////////
  boolean builtinvariables=true;

  Variables variables=new Variables();

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
    if (builtinvariables) {
      if (variable.startsWith("ti")) {
        if (variable.equals("ti$") || variable.equals("time$")) {
          java.text.SimpleDateFormat localDateFormat = new java.text.SimpleDateFormat("HHmmss");
          return new GenericType(
            localDateFormat.format( System.currentTimeMillis())
          );
        } else if (variable.equals("ti$") || variable.equals("time$")) {
          return new GenericType((double)(int)(System.currentTimeMillis()/1000.0));
        } else if (variable.equals("ti")) {
          return new GenericType((double)(int)((System.currentTimeMillis()/16.66666666)%1073741824));
        } else if (variable.equals("tisec")) 
          return new GenericType((double)(int)(System.currentTimeMillis()/1000.0));
      } else if (variable.equals("st")) {
        //return new GenericType(0.0); // start to use it now
        int st=fileio_ST; fileio_ST=0; // I think we clear on read
        return new GenericType(st); // start to use it now
 
      } else if (variable.equals("mathpi")) {
        return new GenericType(Math.PI);
      }
    }
    return variables.getvariable(variable);
  }

  //////////////////////////////////
  // FStack : for loop stack
  //////////////////////////////////
  //static final int MAXFORS=30; // make it break faster
  static final int MAXFORS=100; // really need more if combining gosub and for
  int topforloopstack=0;
  int forloopstack[]=new int[MAXFORS];
  String forloopstack_var[]=new String[MAXFORS];
  double forloopstack_to[]=new double[MAXFORS];
  double forloopstack_step[]=new double[MAXFORS];

  void forloopdumpstate() {
    System.out.printf("For loop stack:\n");
    for (int i=0; i<topforloopstack; ++i) {
      System.out.printf("%d) %s to %f step %f at position in code %d\n",
        i,forloopstack_var[i],forloopstack_to[i],forloopstack_step[i],forloopstack[i]);
    }
  }
  // continue...
  void createFORloop(int current, String variable, double forto, double forstep) throws BasicException
  {
    // I think that if we use an already used variable, we pop off the rest of the stack
    // e.g.  FOR I   FOR J   FOR K ......then FOR I again
    // find current variable
    //for (int i=0; i<topforloopstack; ++i) {
    // should this be a circular stack, where once something is reused, all older ones are purged?
    // i.e. for i, j, k, i (again)
    for (int i=0; i<topforloopstack; ++i) {
      if (forloopstack_var[i].equals(variable)) {
        // use this one and pop the rest off -- NO WRONG
        //topforloopstack=i;

        // no we should compress the stack at this point - supressing the old for, and adding
        // the new one to the end!
        if (topforloopstack-i>1) { // check this
          for (int j=i; j<topforloopstack-1; ++j) {
            forloopstack[j]=forloopstack[j+1];
            forloopstack_var[j]=forloopstack_var[j+1];
            forloopstack_to[j]=forloopstack_to[j+1];
            forloopstack_step[j]=forloopstack_step[j+1];
          }
        }
        topforloopstack--;
        break;
      }
    }
    if (verbose) { System.out.printf("processing FOR %s to %f step %f at current=%d at FORSTACK=%d\n",variable,forto,forstep,current,topforloopstack); }
    forloopstack[topforloopstack]=current;
    forloopstack_var[topforloopstack]=variable;
    forloopstack_to[topforloopstack]=forto;
    forloopstack_step[topforloopstack]=forstep;
    if (topforloopstack==MAXFORS-1) throw new BasicException("OUT OF MEMORY");
    topforloopstack++;
  }

  boolean processNEXT(int current, String var) throws BasicException {
    if (verbose) { System.out.printf("processing NEXT %s at current=%d\n",var,current); }
    // could be multiple steps?, no, this should be dealt with by the statements parser
    int fl=topforloopstack;
    executionpoint=current;
    while(fl>0) {
      fl--;
      if (forloopstack_var[fl].startsWith("_")) break; // matches c64 behavior 
      if (var.equals("") || forloopstack_var[fl].equals(var)) {
      //if (var.equals("") && !forloopstack_var[fl].startsWith("_") || forloopstack_var[fl].equals(var)) {
        // just pop the last one
        setvariable(forloopstack_var[fl].toLowerCase(),evaluate(forloopstack_var[fl].toLowerCase()+"+"+forloopstack_step[fl]));
        if (verbose) { System.out.printf("about to add to loop at stack location %d %f>%f\n",fl,getvariable(forloopstack_var[fl]).num(),forloopstack_to[fl]); }
        //if (verbose) { dumpstate(); }
        if (forloopstack_step[fl]<0 && getvariable(forloopstack_var[fl].toLowerCase()).num()<forloopstack_to[fl]  
            || forloopstack_step[fl]>0 && getvariable(forloopstack_var[fl].toLowerCase()).num()>forloopstack_to[fl]) {
          if (verbose) { System.out.printf("NEXT: At end of stack (%s)\n",forloopstack_var[fl].toLowerCase()); }
          // pop it off, but keep on going, we may be popping off many
          topforloopstack=fl; // at least one
          return false; // I think we need to exit now
        } else {
          if (verbose) { System.out.printf("NEXT: Looping back\n"); }
          // goto the end of that for loop
          executionpoint=forloopstack[fl];
 
          topforloopstack=fl+1; // new!!!

          return true;
        }
      }
    }
    if (verbose) { System.out.printf("No more for loops to match!\n"); }

    topforloopstack=fl; // new!!!

    throw new BasicLineNotFoundError("NEXT WITHOUT FOR ERROR");
  }


  // repurpose for loop for gosub too
  void push_fl_gosub(int current) throws BasicException
  {
    forloopstack[topforloopstack]=current;
    forloopstack_var[topforloopstack]="_GOSUB";
    //forloopstack_to[topforloopstack]=forto;
    //forloopstack_step[topforloopstack]=forstep;
    if (topforloopstack==MAXFORS-1) throw new BasicException("OUT OF MEMORY"); // FORMULA TO COMPLEX in c128
    topforloopstack++;
  }
  
  int pop_fl_gosub() throws BasicException
  {
    // search back for most recent gosub
    if (verbose) { System.out.printf("processing RETURN\n"); }
    int fl=topforloopstack;
    //executionpoint=current;
    while(fl>0) {
      fl--;
      if (forloopstack_var[fl].equals("_GOSUB")) {
          // pop it off, but keep on going, we may be popping off many
          topforloopstack=fl; // at least one
          return forloopstack[fl];
      }
    }
    topforloopstack=0; // new!!!
    throw new BasicException("RETURN WITHOUT GOSUB");
  }

  //////////////////////////////////
  // Evaluate engine
  //////////////////////////////////
  evaluate evaluate_engine;

  // from within here we execute the evaluate?
  GenericType evaluate(String expression) throws BasicException {
    try {
      return evaluate_engine.interpret_string(expression);
    } catch (EvaluateException ee) {
      throw new BasicException(ee.getMessage());
    }
  }
  GenericType evaluate_partial(String expression) throws BasicException {
    try {
      return evaluate_engine.interpret_string_partial(expression);
    } catch (EvaluateException ee) {
      throw new BasicException(ee.getMessage());
    }
  }
  void assignment(String expression) throws BasicException {
    try {
      if (evaluate_engine.interpret_string_with_assignment(expression)!=null)
        return;// true;
      else
        return;// false;
    } catch (EvaluateException ee) {
      throw new BasicException(ee.getMessage());
    }
  }
  void assignment_dt(String expression) throws BasicException {
    try {
      if (evaluate_engine.interpret_string_with_assignment_dt(expression)!=null)
        return;// true;
      else
        return;// false;
    } catch (EvaluateException ee) {
      throw new BasicException(ee.getMessage());
    }
  }

  //////////////////////////////////
  // GStack : Gosub stack
  //////////////////////////////////
  static final int MAXGOSUBS=300;
  int topgosubstack=0;
  int gosubstack[]=new int[MAXGOSUBS];

  void gotoLine(String lineNostr) throws BasicException {
    int value;
    try {
      value=Integer.parseInt(lineNostr);
    } catch(Exception e) {
      if (true) { // enable goto label
        gotoLabel(lineNostr);
        return;
      } else {
        throw new BasicException("NOT NUMERIC ERROR");
      }
    }
    gotoLine(value);
  }

  void gotoLine(int lineNo) throws BasicException {
    // put code in here
    // look up line in cache and set pnt to correct place and return
    for (int i=0; i<toplinecache; ++i) {
      if (linecacheline[i]==lineNo) {
        executionpoint=linecachepnt[i]; // set this, and then pnt must be set after return
        return;
      }
    }
    System.out.printf("?LINE %d NOT FOUND\n",lineNo);
    throw new BasicLineNotFoundError("LINE "+lineNo+" NOT FOUND");
    //return; // should fail
  }

  void gosubLine(String lineNostr, int pnt) throws BasicException {
    int value;
    try {
      value=Integer.parseInt(lineNostr);
    } catch(Exception e) {
      if (true) { // enable goto label
        gosubLabel(lineNostr,pnt);
        return;
      } else {
        throw new BasicException("NOT NUMERIC ERROR");
      }
    }
    gosubLine(value,pnt);
  }

  void gosubLine(int lineNo, int pnt) throws BasicException {
    // push on the stack where we are
    if (true) {
      push_fl_gosub(pnt);
    } else {
      gosubstack[topgosubstack]=pnt;
      topgosubstack++;
    }
    gotoLine(lineNo);
  }

  void popReturn() throws BasicException {
    if (true) {
      executionpoint=pop_fl_gosub();
    } else {
      if (topgosubstack>0) {
        topgosubstack--;
        executionpoint=gosubstack[topgosubstack];
      } else {
        System.out.printf("?RETURN WITHOUT GOSUB\n");
        // must throw an error here
        throw new BasicException("RETURN WITHOUT GOSUB");
      }
    }
  }


  //////////////////////////////////
  // LCache : Line cache
  //////////////////////////////////
  static final int MAXLINES=10000;
  int toplinecache=0;
  int linecacheline[]=new int[MAXLINES]; // when we get to them, we store the pointer into the code of each line // of course we have to read ahead one we get a GOTO or GOSUB
  int linecachepnt[]=new int[MAXLINES];

  void cacheLine(String lineNostr, int pnt) {
    int value=Integer.parseInt(lineNostr); // this could fail?
    if (verbose) { System.out.printf("Caching line %d pointer at %d\n",value,pnt); }
    linecacheline[toplinecache]=value;
    linecachepnt[toplinecache]=pnt;
    toplinecache++;
  }

  void setCurrentLine(int pointinprogram) {
    currentLineNo = getCurrentLine(pointinprogram);
  }

  String getCurrentLine(int pointinprogram) {
    // finds which line we are at by looking at the cache and comparing to the executionpoint
    // we assume we are on the first line (start from index 1)
    if (verbose) System.out.printf("At execution point %d\n",executionpoint);
    if (verbose) System.out.printf("At pointinprogram point %d\n",pointinprogram);
    for (int i=toplinecache-1; i>=0; --i) {
      if (pointinprogram>=linecachepnt[i]) {
        // assuming that the lines are cache in order, then we are here!
        return ""+linecacheline[i];
      }
    }
    return ""; // just return negative otherwise
  }

  //////////////////////////////////
  // LabCache : Label cache
  //////////////////////////////////
  static final int MAXLABELS=1000;
  int toplabcache=0;
  String labcachelabel[]=new String[MAXLABELS]; // when we get to them, we store the pointer into the code of each line // of course we have to read ahead one we get a GOTO or GOSUB
  int labcachepnt[]=new int[MAXLABELS];

  void cacheLabel(String label, int pnt) {
    if (verbose) { System.out.printf("Caching label %s pointer at %d\n",label,pnt); }
    labcachelabel[toplabcache]=label;
    labcachepnt[toplabcache]=pnt;
    toplabcache++;
  }

  void gotoLabel(String label) throws BasicException {
    for (int i=0; i<toplabcache; ++i) {
      if (labcachelabel[i].equals(label)) {
        executionpoint=labcachepnt[i]; // set this, and then pnt must be set after return
        return;
      }
    }
    if (verbose) System.out.printf("?LABEL %s NOT FOUND\n",label);
    throw new BasicLineNotFoundError("LABEL "+label+" NOT FOUND");
  }

  void gosubLabel(String label, int pnt) throws BasicException {
    // push on the stack where we are
    if (true) {
      push_fl_gosub(pnt);
    } else {
      gosubstack[topgosubstack]=pnt;
      topgosubstack++;
    }
    gotoLabel(label);
  }


  void dumpstate() {
    variables.dumpstate();
    forloopdumpstate();
    System.out.printf("Current Line No = %s\n",currentLineNo);
   // temp debugging
    //for (int i=0; i<256; ++i) { System.out.printf("%d =%d\n",i,(int)machinescreen.petconvert((char)i)); }
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
  
  GenericType metareaddatastreamNum() throws EvaluateException
  {
    String str=metareaddatastreamString().str();
    try {
      if (str.equals("")) return new GenericType(0.0);
      else return new GenericType(Double.parseDouble(str));
    } catch (NumberFormatException e) {
      throw new EvaluateException("NOT NUMERIC"); // TYPE MISMATCH ?
    }
  }
  
  GenericType metareaddatastreamString() throws EvaluateException
  {
    // from whatever we happen to be reading (say DATA) return a string
    // added possibility of continuation marks in DATA strings
    String building="";
    boolean quoted=false;
    boolean cont=false;
    boolean preamble=true; // chew all spaces until quote
    if (uptoDATA>=allDATA.length()) throw new EvaluateException("OUT OF DATA");
    for (; uptoDATA<allDATA.length(); ++uptoDATA) {
      String a=allDATA.substring(uptoDATA,uptoDATA+1);
      // this continuation code was a mistake, it was actually implemented in a basic program!
      if (cont) {
        // will read up quote cr quote
        if (a.equals("\n") || a.equals("\"")) {
          // not exactly, but it will do
          // ignore
        } else {
          building+=a;
          cont=false;
        }
      } else if (a.equals("\n")) {
        uptoDATA++;
        break;
      } else if (quoted && a.equals(""+(char)127)) {
        // it looks like a continuation character
        // really must be followed by close quote and end of line!
	preamble=false; //?
        cont=true;
      } else if (!quoted && a.equals(":")) {
        // chew up rest of line
        for (; uptoDATA<allDATA.length(); ++uptoDATA) {
          String b=allDATA.substring(uptoDATA,uptoDATA+1);
          if (b.equals("\n")) {
            uptoDATA++;
            break;
          }
        }
        break;
      } else if (!quoted && a.equals(",")) {
        uptoDATA++;
        break;
      } else if (preamble && a.equals(" ")) {
        // chew
      } else if (a.equals("\"")) {
        quoted=!quoted;
	preamble=false;
        //building+=a; - no, chew this up!
      } else { building+=a; preamble=false; }
    }
    if (verbose) { System.out.printf("Returning DATA >>>%s<<<\n",building); }
    return new GenericType(building);
  }
  

  //////////////////////////////////
  // Memory I/O
  //////////////////////////////////

  int peek(int val) {

    if (graphicsDevice!=null && (val==1024 || val>=1030 && val<=1033)) { 
        return graphicsDevice.command_PEEK(val);
    }
    if (val>=1024 && val<1024+1000) {
      int x=(val-1024)%40;
      int y=(val-1024)/40;
      return (int)machinescreen.screenchar[x][y];

    } else if (val>=55296 && val<55296+1000) {
      int x=(val-55296)%40;
      int y=(val-55296)/40;
      return (int)machinescreen.screencharColour[x][y];
    }
    if (val==648) return 4; // screen page
    if (val==197) {
      if (machinescreen.hasinput()) 
        return 22; // made up number just to make it NOT 64
      else
        return 64;
    } else
      return 0;
  }

  void performSYS(GenericType gt) throws BasicException {
    // here we should have a list of variables
    int memloc;
    if (gt.gttop==1) {
      memloc=(int)gt.num();
      if (verbose) { System.out.printf("Sysing to memory location %d\n",memloc); }
      performSYS(memloc);
    } else {
      System.out.printf("Wrong number of parameters\n");
      throw new BasicException("WRONG NUMBER OF PARAMETERS");
    }
  }

  void performSYS(int memloc) {
    return;
  }

  void performPOKE(GenericType gt) throws BasicException {
    // here we should have a list of variables
    int memloc;
    int memval;
    if (gt.gttop==2) {
      memloc=(int)gt.gtlist[0].num();
      memval=(int)gt.gtlist[1].num();
      if (verbose) { System.out.printf("Poking memory location %d with variable %d\n",memloc,memval); }
      performPOKE(memloc, memval);
    } else {
      System.out.printf("Wrong number of parameters\n");
      throw new BasicException("WRONG NUMBER OF PARAMETERS");
    }
  }

  void performPOKE(int memloc, int memval) {
    // the background and border may be round the wrong way, cant remember
    if (memloc==40000) {
      PlaySound a = new PlaySound("test.wav");
    } else if (memloc==198) {
      // clear the key buffer if you get this
    } else if (memloc==53281) {
      // background
      // if it is already set - dont do it again (expensive!)
      if (machinescreen.backgroundColour!=machinescreen.fullcolour[memval]) {
        machinescreen.backgroundColour = machinescreen.fullcolour[memval];
        machinescreen.reshapeScreen(); // just to see - this is a dodgy work around!!! when changing background or border colours, mu
      }
    } else if (memloc==53280) {
      // border
      if (machinescreen.borderColour!=machinescreen.fullcolour[memval]) {
        machinescreen.borderColour = machinescreen.fullcolour[memval];
        machinescreen.reshapeScreen(); // just to see - this is a dodgy work around!!! when changing background or border colours, mu
      }
    } else if (memloc>=1024 && memloc<1024+1000) {
      int x=(memloc-1024)%40;
      int y=(memloc-1024)/40;
      machinescreen.setChar(x,y,(char)memval);
    } else if (memloc>=55296 && memloc<55296+1000) {
      int x=(memloc-55296)%40;
      int y=(memloc-55296)/40;
      machinescreen.setCharColour(x,y,(char)memval);
    }
  }
  

  /*****/
  GraphicsDevice graphicsDevice=null;
  
  //////////////////////////////////
  // Screen
  //////////////////////////////////

  C64Screen machinescreen;
  String baseTitle="ijk64";
  
  public void attachScreen(C64Screen ascreen) {
    machinescreen=ascreen;
  }

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
/* for now -slow down a geta$ - will do a givemekey which has a sleep in it*/
  String getkey() {
    machinescreen.slowinput();
    if (machinescreen.hasinput()) {
      return ""+machinescreen.givemekey();
    } else {
      return "";
    }
  }

  boolean getconfirmation() {
    String ans=""+machinescreen.givemekey();
    if (ans.equals("y") ||ans.equals("Y")) return true;
    return false;
  }

  // checks whether the ControlC flag has been set 
  boolean hasControlC()
  {
    return machinescreen.hasControlC();
  }


  int asc(String str) {
	  // return the petascii of a string for this machine
//	  if (machinescreen!=null) return machinescreen.asc(str);
      if (machinescreen!=null) return (int)machinescreen.petconvert(str.charAt(0));
	  return 0;
  }

  String chrD(int asc) {
	  // return the petascii of a string for this machine
	  //if (machinescreen!=null) return machinescreen.chrD(str);
      if (machinescreen!=null) return ""+machinescreen.petunconvert((char)asc);
	  return "";
  }

  int asc1(int str) {
      if (machinescreen!=null) return (int)machinescreen.petconvert((char)str);
	  return 0;
  }

  int chrD1(int asc) {
      if (machinescreen!=null) return (int)machinescreen.petunconvert((char)asc);
	  return 0;
  }


  //////////////////////////////////
  // PText : Program Text & editting
  //////////////////////////////////

  String programText="";          //String code; // here is the code, stored as one big string // not used
  
  String program_name="";         // this is to keep a reference to what we save/loaded this program as
  boolean program_modified=false;
  boolean program_running=false; // true if NOT in direct mode PGS20150210
  
  boolean performExit(boolean checkprogram) throws BasicException {
    if (checkprogram) {
      if (program_modified) {
        print("program not saved, continue? ");
        if (!getconfirmation()) {
          printnewline();
          throw new BasicException("PROGRAM NOT SAVED");        
        }
        printnewline();
      }      
    }
    signal_exit=true;
    return true;
  }
  
  void newProgramText() throws BasicException
  {
    if (program_modified) {
      print("program not saved, continue? ");
      if (!getconfirmation()) {
        printnewline();
        throw new BasicException("PROGRAM NOT SAVED");        
      }
      printnewline();
    }
    programText=""; // NEW program
    program_modified=false;
    program_name=""; // clear this too
    crlfText=false;
    machinescreen.setTitle(baseTitle); // try this
    variables_clr();    
  };

 ///////////////////////////////
 // FILE I/O
 // vars
  class FileHandle {
    int fileno;
    String filename;
    int mode;
    int type;
    int dev;
    Writer output;
    BufferedReader input;
    FileHandle() { fileno=-1; }; //initialise
    void newFileHandle(int fh, int dev, String filename, char type, boolean overwrite) throws BasicException {
      if (verbose) System.out.printf("new fh = %d filename=%s dev=%d type=%c overwrite=%s\n",topHandle,filename,dev,type,overwrite?"true":"false");
      // look for free spot:
      int hand;
      for (hand=0; hand<topHandle; ++hand) {
        if (handleHash[hand].fileno<0) break;
      }
      if (verbose) System.out.printf("topHandle=%d hand=%d\n",topHandle,hand);
      handleHash[hand]=new FileHandle();
      handleHash[hand].fileno=fh;
      handleHash[hand].type=type;
      handleHash[hand].dev=dev;
      handleHash[hand].filename=filename;
      //if (!filename.equals("KB")) {
      if (dev>0) {
        try {
          if (type=='R') {
            if (verbose) { System.out.printf("Read mode\n"); }
            handleHash[hand].input = new BufferedReader(new FileReader( new File(filename)));
          } else if (type=='W') {
            if (verbose) { System.out.printf("Write mode\n"); }
            handleHash[hand].output = new BufferedWriter(new FileWriter(filename, !overwrite));
          }
        } catch (Exception e) { System.out.printf("open exception\n");
          throw new BasicException("COULD NOT OPEN");
        }
      }
      if (hand==topHandle) topHandle++;
      //handleHash[FileHandle.top].fileno=fh;
    }
    int findHandle(int fh) {
      for (int i=0; i<topHandle; ++i) if (handleHash[i].fileno==fh) return i;
      return -1;
    }
  }
  static int MAXHandle=20;
  int topHandle=0;
  FileHandle handleHash[] = new FileHandle[20];
  FileHandle dummyhandleHash = new FileHandle();
  int fileio_ST=0;
  
 // open file
// format [@][n:]filename[,format[,type]]    (type = 'A
  void OpenFile(int fh, int dev, String param) throws BasicException {
     String format="seq";
     char type='R';
     boolean overwrite=false;
     if (param.startsWith("@")) {
       overwrite=true;
       param=param.substring(1);
       if (verbose) { System.out.printf("overwrite\n"); }
     }
     if (param.charAt(1)==':') {
       if (verbose) { System.out.printf("got 0: format\n"); }
       String[] data = param.split(":"); // throw away the num for now
       param=data[1];
     }
     String[] data = param.split(",|\\.");
     if (data.length>1 && data[1].startsWith("s")) { format="seq"; }
     else if (data.length>1 && data[1].startsWith("u")) { format="usr"; }
     else if (data.length>1) { format=data[1]; }
     String filename=data[0]+"."+format;
     if (data.length>2 && data[2].equals("w")) { type='W'; }
     filename=filename.replace('/','_');
     dummyhandleHash.newFileHandle(fh,dev,filename,type,overwrite);
     fileio_ST=0;
  }

  void CloseAllFiles() {
    try {
      for (int i=0; i<topHandle; ++i) 
        if (handleHash[i].fileno>=0 && handleHash[i].dev>0) 
          if (handleHash[i].type=='W') 
            handleHash[i].output.close();
          else
            handleHash[i].input.close();
    } catch (Exception e) { System.out.printf("closeall exception\n"); }
    topHandle=0;
  }
 // close file
  void CloseFile(int fh) throws BasicException {
    foff=dummyhandleHash.findHandle(fh);
    try {
      if (handleHash[foff].dev>0) {
        if (handleHash[foff].type=='W') 
          handleHash[foff].output.close();
        else 
          handleHash[foff].input.close();
      }
    } catch (Exception e) { System.out.printf("close exception\n"); 
        throw new BasicException("FILE NOT OPEN");
    }
    handleHash[foff].fileno=-1;
    handleHash[foff].output=null;
  }
 // print to file
  int foff=-1;
  void SetFH(int fh) {
    foff=dummyhandleHash.findHandle(fh);
  }
  //void PrintFile(int fh, String raw) {
  void PrintFileFlush() {
    try {
    handleHash[foff].output.flush(); // do this for now - but maybe later only flush at end of program or after timed event
    } catch (Exception e) { System.out.printf("flush exception\n"); }
  }
  void PrintFile(String raw) {
    if (verbose) System.out.printf("use = %d raw test = %s\n",foff,raw);
    try {
    handleHash[foff].output.append(raw);
    } catch (Exception e) { System.out.printf("append exception\n"); }
  }
 // input from file
  String InputFile(int fh) {
    if (verbose) { System.out.printf("inputing fh=%d\n",foff); }
    try {
      String line;
      if (verbose) { System.out.printf("about to read line\n"); }
      line = handleHash[foff].input.readLine();
      if (line == null) { 
        fileio_ST=64; //?
      } else {
        //fileio_ST=0;
      }
      if (verbose) { System.out.printf("read %s\n",line); }
      return line;
    } catch (Exception e) { System.out.printf("input exception\n"); }
    return null;
  }


			//StringBuffer stringBuffer = new StringBuffer();
			//String line;
			//while ((line = bufferedReader.readLine()) != null) {
				//stringBuffer.append(line);
				//stringBuffer.append("\n");
			//}
			//fileReader.close();
			////System.out.println("Contents of file:");
			//System.out.println(stringBuffer.toString());
		//} catch (IOException e) {
			//e.printStackTrace();
		//}


 // get from file
 ///////////////////////////////
  // reads the program basic text file 
  // was static  why?
  String read_a_file(String filename) throws BasicException {
    String content="";

    try {
      FileInputStream fis = new FileInputStream(filename);
        if (true) { // want message
          print("loading");
        }
      int x= fis.available();
      byte b[]= new byte[x];
      fis.read(b);
      content = new String(b);
      //System.out.println(content);
    } catch (Exception e) {
      // try reading from the jar now
      try {
//        InputStream is = java.util.FileUtils.class.getResourceAsStream(filename);
//        InputStream is = getClass().getResourceAsStream(filename);
        InputStream is = getClass().getResourceAsStream("basic/"+filename);
        if (true) { // want message
          print("loading");
        }
        int x= is.available();
        byte b[]= new byte[x];
        is.read(b);
        content = new String(b);
      } catch (Exception e2) {
        throw new BasicException("FILE NOT FOUND");
      }
    }

    return content;
  }
  
//  static  // need to be static?
String read_http(String urlstring) throws BasicException {
    String content="";

    try {
      URL url = new URL(urlstring);
      BufferedReader in = new BufferedReader(
            new InputStreamReader(
            url.openStream()));

      String inputLine;

        if (true) { // want message
          print("loading");
        }

      while ((inputLine = in.readLine()) != null)
       //   System.out.println(inputLine);
       content+=inputLine+"\n";
      
      in.close();
        //}
    
    } catch (Exception e) { 
      System.out.println(e);
      throw new BasicException("FILE NOT FOUND 404");
      // return null; 
    }

    return content;
  }

  boolean save_a_file(String filename) throws BasicException {
	FileOutputStream out; // declare a file output object
	PrintStream p; // declare a print stream object
	try
	{
		// Create a new file output stream
		// connected to "myfile.txt"
		out = new FileOutputStream(filename);
		// Connect print stream to the output stream
		p = new PrintStream( out );
		if (crlfText) p.print (programText.replaceAll("\n","\r\n"));
		else p.print (programText);
		p.close();
	}
	catch (Exception e)
	{
		System.err.println ("Error writing to file");
                throw new BasicException("ERROR SAVING FILE");
                //return false;
	}
    return true;
  }

    boolean post_http(String filename) throws BasicException {

        String charset = "UTF-8"; 
        //String url = "http://localhost/test2.php";
        //String url = "http://www.futex.com.au/basic/uploader.php";
        String url = "http://test.futex.com.au/basic/uploader.php";
        try {
          String boundary = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.

          String CRLF = "\r\n"; // Line separator required by multipart/form-data.
          URLConnection connection = new URL(url).openConnection();
          connection.setDoOutput(true);
          connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
          
              OutputStream output = connection.getOutputStream();
              PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, charset), true);

              // Send text file.
              writer.append("--" + boundary).append(CRLF);
              // append .txt so we can just GET it back  - no, now done in php uploader
              writer.append("Content-Disposition: form-data; name=\"userfile\"; filename=\"" + filename + "\"").append(CRLF);
              writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF); // Text file itself must be saved in this charset!
              writer.append(CRLF).flush();
              writer.append(programText);
              //Files.copy(textFile.toPath(), output);
              //output.flush(); // Important before continuing with writer!
              writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.
          
              // End of multipart/form-data.
              writer.append("--" + boundary + "--").append(CRLF).flush();

              int responseCode = ((HttpURLConnection) connection).getResponseCode();
              if (responseCode == HttpURLConnection.HTTP_OK) {
                //System.out.printf("Response = %d\n",status);
	      } else {
                throw new BasicException("ERROR "+responseCode+" POSTING");
              }
	} catch (IOException e)
	{ 
	    e.printStackTrace();	// should do real exception handling
            //return false;
            throw new BasicException("SAVE ERROR");
	}
        return true;
    }
  ///
  /// new additions for keeping the program text in the machine
  ///
  String listProgram()
  {
    return programText;
  }

  void listProgram(int from, int to) throws BasicException
  {
    boolean printing;
    if (machinescreen.backgroundColour == 0xFFFFFFFF) {
      //11
      //d1="(blk)";
      //d2="(lblu)";
      //d8="(cyn)";
      d1="(blk)";
      d2="(blu)";
      d8="(lblu)";
      d5="(lred)";
    } else {
      d1="(wht)";
      d2="(cyn)";
      d8="(yel)";
      d5="(lgrn)";
    }
    short keepcolor=machinescreen.cursColour;
    /* need some inspection to split to lines now! */
    /* and to simplify - need to return it as a string! */
    if (verbose) System.out.printf("Listing from %d to %d\n",from,to);
    if (from==-1 && program_name!="") {
      print(syntaxHighlight("rem program = "+program_name.toLowerCase()+(program_modified?"*":"") /*+"\n"*/));
    }
    String lines[] = programText.split("\\r?\\n");
    printing=false; if (from==-1) printing=true;
    for (int i=0; i<lines.length; ++i) {
      int lineno=0;
      String thisline=machineReadLineNo(lines[i]);
      // allow showing of no numbered lines
      if (thisline==null) {
		  //continue;
		  lineno=-2;
	  } else {
		  try {
			lineno=Integer.parseInt(thisline.trim());
		  } catch (NumberFormatException e) {
  		    lineno=-2;
			//continue;
		  }
	  }
      if (lineno!=-2 && !printing && lineno>=from) printing=true;
      if (lineno!=-2 && printing && (lineno>to && to!=-1)) { printing=false; from=999999; }
      if (printing) { print(syntaxHighlight(lines[i] /*+"\n"*/)); }
      if (hasControlC()) {
        print("\n");
        currentLineNo=""; // For breaking a program, then running interactive
        throw new BasicException("BREAK"); 
      }
      //System.out.printf("this line %d\n",lineno);
    }
    print("\n");
    machinescreen.setcursColour(keepcolor);
    return;
  }


/****************************/
String t;
String out="";
int p;
int n;
int f;

/*
String colourname_alias[] = { "blk", "wht", "red", "cyn", "pur", "grn",
    "blu", "yel", "orng", "brn", "lred", "gry1",
    "gry2", "lgrn", "lblu", "gry3"
*/


String d1="(wht)"; //"^[[31m";  /* line# */
String d2="(cyn)"; //"^[[32m";  /* statement keyword */
//String d3="(red)"; //"^[[33m";  /* string (pink) */ // was pur
String d3="(orng)"; //"^[[33m";  /* string (pink) */ // was pur
String d4="(grn)"; //"^[[34m";  /* expression */
//String d6="(blu)"; //"^[[35m";  /* comment */
String d6="(gry2)"; //"^[[35m";  /* comment */
String d5="(lgrn)"; //"^[[36m"; /* assignment */
//String d7="(lred)"; //"^[[37m"; /* text */ unused
String d8="(yel)"; /* if/then */
String d9="(pur)"; //"^[[37m"; /* and/or/not */
String d0="(lblu)"; //"^[[0m";  /* normal */
//String sepchar=" ";
String sepchar="";
String linesep="";
//String sepchar="";
//String linesep=" ";
int hs;

     String[] token={
       "REM",
       "GOTO","GOSUB","THEN",
       "TO","STEP",
       "IF","FOR","NEXT","RETURN","PRINT#","PRINT","ENDFRAME","DIM",
       "AND","OR","NOT",
       "ON",
       "GET#5,",
       "POKE","OPEN","INPUT#,","CLOSE","DATA","RUN","READ","RESTORE","INPUT","LIST",
       "META-VERBOSE",
       "SYS","CLR",
       "META-SCALEY","META-ROWS",
       "FAST","GET",
       "META-CHARSET","LOAD","META-DUMPSTATE","META-COLS","META-BGTRANS","META-SCALE","META-TIMING",
       "NEW","SAVE","CONT","STOP",
       "EXIT",
       "LABEL"
       ,"SCREEN","GPRINT","BEGINFRAME","END","CLS","LINE","FSET","SLEEP","ALERT","RECT","FILES"
       ,"LSET"
       ,"IMAGELOAD","DRAWIMAGE","DESTROYIMAGE"
       ,"CIRCLE"
       ,"HELP"
    };
  String got;
  String syntaxHighlight(String line) {
    /* takes a line at a time, and highlights the syntax */
/* should try and get this from "statements" */

  char a;
  int i;
  int nest;
  t=line;

  if (!hasSyntaxHighlighting) { return line+"\n"; }
  p=0; /* pointer into text t */
  n=0; /* node that lexer is at */
  f=0;
  nest=0;
  t=t+'\000'; /* null */
  out="";

   outerloop:
   while (true) {
    /* chewloop */
    hs=0;
    while (t.charAt(p)==' ') { p++; out+=" "; hs++; }
    f=p;
    while(true) {
      a=t.charAt(p);

      if (a==0)                                 { if(n!=0) cs();                       break outerloop; }
      if (a==13 || a==10)                       {      cs(); p++; n=0;                 break; }
      if (n==5 && !Character.isDigit(a) && f==p )                 n=1;
      if (n==0 && !Character.isDigit(a))        {      cs();      n=1;                 break; }
      if (n==1 && a==' ')                       { p++; cs();      n=2;                 break; }
      if (n==1 && a=='=')                       { p++; cs();      n=2;                 break; }
      if (n==1 || n==4)            if (findtoken())             /*n=0,2,5*/            break;
      if (n!=3 && a==':')                       {      cs(); p++; n=1; out=out+a;      break; }
      if (n==3 && a=='"')                       { p++; cs();      n=4; nest=0;         break; }
      if (n==4 && (a==',' || a==';') && nest>0) {      cs(); p++; n=2; out=out+a;      break; }
      if (n==4 && a=='"')                       {      cs();      n=3; f=p; }
      else if ((n==2||n==1) && a=='"')          {                 n=3; } // n==1 special case DIR
      else if (n==2)                            {                 n=4; nest=0; continue; }
      else if (n==4 && a=='(')                  { nest++; }
      else if (n==4 && a==')')                  { nest--; }
      p++;
    }
  }

  if (false) {
    System.err.printf("STRING:\n");
    System.err.printf("%s\n",out);
    for (i=0; i<out.length(); ++i) {
      System.err.printf("%d,",(int)out.charAt(i));
    }
    System.err.printf("STRINGEND\n");
  }
  return out;
    
  }


boolean findtoken() {
  int i;
  int gotlen;
      for (i=0; i<token.length; ++i) { 
        got=token[i]; gotlen=got.length();
        if (n==1 && p==f && t.length()>=f+gotlen && t.substring(f,f+gotlen).equalsIgnoreCase(got) ||
            n==4 && (i<=5||i>=14&&i<=16) && t.length()>=p+gotlen && t.substring(p,p+gotlen).equalsIgnoreCase(got)) {
          if (i==0) { out+=d6; out+=sepchar; n=0; chewcr(); return true; }
          if (n==4) cs();
          p+=gotlen;
          //if (hs==0) out+=" ";
          if (i==3 || i==6 || i==17) { out+=sepchar+d8+got.toLowerCase()+d0+sepchar; }
          else if (i==14 || i==15 || i==16) { out+=sepchar+d9+got.toLowerCase()+d0+sepchar; }
          else { out+=sepchar+d2+got.toLowerCase()+d0+sepchar; }
          if (i<=3) n=5; else n=2;
          return true;
        }
      }
  return false;
}

void chewcr() {
  int i;
  while( t.charAt(p)!=13 && t.charAt(p)!=10 && t.charAt(p)!=0) p++;
  while( t.charAt(p)==13 || t.charAt(p)==10) p++;  /* jump new line */
  for (i=f; i<p; ++i) if(t.charAt(i)!=10) out+=t.charAt(i);
  out+=d0;
}
//unused//void chew() {
  //unused//while (t.charAt(p)==' ') { p++; out+=" "; }
//unused//}

  void cs() {
 int i;
  if (n==0) out+="\n"+d1;
  if (n==1) out+=sepchar+d5;
  if (n==3) out+=d3;
  if (n==4) out+=d4;
  if (n==5) out+=d1;
  for (i=f; i<p; ++i) out+=t.charAt(i);
  out+=d0;
  if (n==0) out+=linesep;

  }
  
  boolean loadProgram(String filename) //throws BasicException
  {

    try {
      // if it ends in au or wav play it
      if (filename.toLowerCase().endsWith(".au") || filename.toLowerCase().endsWith(".wav") || filename.toLowerCase().endsWith(".mp3")) {
        PlaySound sound = new PlaySound(filename);
        return true;
      } else
      if (filename.toLowerCase().endsWith(".png") || filename.toLowerCase().endsWith(".jpg") || filename.toLowerCase().endsWith(".bmp")) {
        machinescreen.load_bgimage(filename);
        return true;
      } else {
        if (program_modified) {
          print("program not saved, continue? ");
          if (!getconfirmation()) {
             print("\n?program not saved"); // took off "[CR]"
             return false;
             //printnewline()
            //throw new BasicException("PROGRAM NOT SAVED");        
          }
          printnewline();
        }
        if (filename.toLowerCase().contains("http:")) {
          //if (false) { // want message
            //print("\n");
            //print("searching for "+filename+"\n");
          //}
          programText=read_http(filename);
        } else {
          //if (false) { // want message
            //print("\n");
          //}
          programText=read_a_file(filename);
        }
        // fix CR LF issue, just remove CR LF and replace with (unix) LF
        if (programText.contains("\r\n")) {
          programText=programText.replaceAll("\r\n","\n");
          crlfText=true; // use this to convert back on save!
          if (verbose) System.out.printf("Flagging as crlf (and stripping)\n");
        } else crlfText=false;
      }
    } catch (BasicException basicerror) {
       System.out.printf("Basic Error: %s\n",basicerror.getMessage());
       print("\n?"+basicerror.getMessage().toLowerCase()); // took off "[CR]"
       return false;
    }
    program_saved_executionpoint=(-1);
    program_name=filename; // keep a copy of what we loaded
    machinescreen.setTitle(baseTitle+" - "+program_name); // try this

    program_modified=false;
    variables_clr();     // didnt do this before!
    return true;
  }

  boolean saveProgram(String filename)
  {
    boolean ret;
    try {
      if(filename.startsWith("%") || filename.startsWith("http://test.futex.com.au/cloud/c64x")) {
        filename=filename.replaceFirst("%","");
        filename=filename.replaceFirst("http://test.futex.com.au/cloud/c64x","");
        filename=filename.replaceFirst(".basic.txt",""); // trim trailing txt
        ret=post_http(filename);
        filename="%"+filename; // put it back! - but only the short version
      } else {
        ret=save_a_file(filename);
      }
    } catch (BasicException basicerror) {
       System.out.printf("Basic Error: %s\n",basicerror.getMessage());
       print("\n?"+basicerror.getMessage().toLowerCase()); // took off "[CR]"
       return false;
    }
    if (ret) {
      program_name=filename; // keep a copy of what we last saved
      machinescreen.setTitle(baseTitle+" - "+program_name); // try this
      program_modified=false;
    }      
    return ret;
  }
  
  void listFiles() {
    File dir = new File(".");
  
    String[] children = dir.list();
    if (children == null) {
        // Either dir does not exist or is not a directory
    } else {
        print("\n");
        for (int i=0; i<children.length; i++) {
            // Get filename of file or directory
            String filename = children[i];
            print(filename+"\n");
        }
    }
  }
  void doCHDIR(String newDir) {
    System.setProperty("user.dir", newDir);
  }
  void listDIR(boolean datesort) {
    File dir = new File(".");
  
    File[] files = dir.listFiles();
    if (datesort) {
       Arrays.sort(files, new Comparator<File>(){
       public int compare(File f1, File f2)
       {
           return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
       } });
    } else {
       Arrays.sort(files, new Comparator<File>(){
       public int compare(File f1, File f2)
       {
           return f1.getName().compareTo(f2.getName());
       } });
    }

    //if (children == null) {
        //// Either dir does not exist or is not a directory
    //} else {
        print("directory = "+dir.getAbsolutePath()+"\n");
        for (int i=0; i<files.length; i++) {
            // Get filename of file or directory
            String filename = files[i].getName();
            if (filename.contains(".basic")) {
              
              filename=filename.replaceFirst("\\.basic","");
              filename=("\""+filename+"\"                      ").substring(0,22);

   	      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH.mm");
              print(syntaxHighlight(filename+":"+sdf.format(files[i].lastModified())+"\n"));
            }
        }
    //}
  }

  boolean contProgram() throws BasicException // not used now (see below)
  {
    if (verbose) {
      System.out.printf("wanting to continue program : program_saved_executionpoint=%d\n",program_saved_executionpoint);
    }
    if (program_saved_executionpoint<0) {
      throw new BasicException("CAN'T CONTINUE ERROR");
    }
    // not the interpretted immediate line!
    program_running=true;
    new statements(programText, this, program_saved_executionpoint); // passing along the machine too
    program_saved_executionpoint=save_executionpoint; // only done on running a program
    program_running=false;
    return true;
  }

  boolean contProgram(String lineNo) throws BasicException
  {
    if (verbose) {
      System.out.printf("wanting to continue program : program_saved_executionpoint=%d\n",program_saved_executionpoint);
    }
    if (lineNo != null) {
      // not the interpretted immediate line!
      program_running=true;

      // assumes lines have been cached
      //gotoLine(lineNo);
      new statements(programText, this, executionpoint,lineNo);
      program_saved_executionpoint=save_executionpoint; // only done on running a program
      program_running=false;
    } else {
      if (program_saved_executionpoint<0) {
        throw new BasicException("CAN'T CONTINUE ERROR");
      }
      // not the interpretted immediate line!
      program_running=true;
      new statements(programText, this, program_saved_executionpoint); // passing along the machine too
      program_saved_executionpoint=save_executionpoint; // only done on running a program
      program_running=false;
    }
    return true;
  }

  // this function not used yet 
  boolean contProgram(int restartat) throws BasicException
  {
    if (verbose) {
      System.out.printf("wanting to continue program : restartat=%d\n",restartat);
    }
    if (restartat<0) {
      throw new BasicException("CAN'T CONTINUE ERROR");
    }
    // not the interpretted immediate line!
    program_running=true;
    new statements(programText, this, restartat); // passing along the machine too
    program_saved_executionpoint=save_executionpoint; // only done on running a program
    program_running=false;
    return true;
  }

  boolean runProgram()
  {
    program_running=true;
    new statements(programText, this); // passing along the machine too
    program_saved_executionpoint=save_executionpoint; // only done on running a program
    program_running=false;
    return true;
  }

  boolean runImmediate(String arg) //runDirect mode
  {
    // by giving 0, it will not clear the Machine state
    new statements(arg, this, 0); // tell the statements class who I am
    return true;
  }

  // inserts a line if new, or replaces a line of the same number (first one it finds)
  boolean insertLine(String line) // throws Exception
  {
    // do this until it is really implemented
    //throw ( new Exception("Not implemented\n") );
    // we are now in a position to enter a real line of the program
    // we will replace an existing line with the same number, or insert it
    // in the first "higher" location
    // with some of the test programs this will not work as they are either
    // missing numbers or repeated, or out of order numbers

    // read in this line number (can we use parts of statement??)
    String lineno=machineReadLineNo(line);
    if (lineno!=null) {
      program_saved_executionpoint=(-1);
      if (verbose) { System.out.printf("Inserting line number %s\n",lineno); }
    
    // find a line with the same number in the program text
      // if found, record its start, find the next line and record its start
      //   replace programText with:
      //     programText before start + lineToInsert + programText from start of next line
      // else
      //   find the next line and record its start
      //   replace program Text with:
      //     programText before start of next line + lineToInsert + programText from start of next line
      // quick and dirty testing only
      int startpos[] = { 0 };
      int endpos[] = { 0 };
      int ret=findLine(lineno,true,startpos);
      if (line.trim().length() == lineno.length()) {
        // we are deleting the line, so remove all trace!
        // issue here if the line begins with leading spaces
        line="";
      } else line=line+"\n";
      if (ret==0) {
        // replace it
        if (verbose) { System.out.printf("replace line -startpos=%d\n",startpos[0]); }
        ret=findLine(lineno,false,endpos);
        if (verbose) {System.out.printf("got %d from findLine (2nd time)\n",ret); }
        if (ret<0) { // last line of program
          programText = 
            programText.substring(0,startpos[0]) + line;
        } else {
          programText = 
            programText.substring(0,startpos[0]) + line + programText.substring(endpos[0]);
        }
      } else if (ret==1) {
        // insert it
        if (verbose) { System.out.printf("insert line\n"); }
        programText = 
          programText.substring(0,startpos[0]) + line + programText.substring(startpos[0]);
      } else {
        // add it
        if (verbose) { System.out.printf("add line\n"); }
        programText = 
          programText + line;
      }
      program_modified=true;
      machinescreen.setTitle(baseTitle+" - "+program_name+"*"); // try this
      return true;
    } else
      return false;
  }

  String machineReadLineNo(String line) {
    int at=0;
    boolean hasdigits=false;

    while (at<line.length()) {
      String a=line.substring(at,at+1);
      if (!hasdigits) {
        if (a.equals("\n") || a.equals(" ")) { // chew these up
        } else if (a.compareTo("0")>=0 && a.compareTo("9")<=0)
          hasdigits=true;
        else 
          // didnt have a line number!
          return null;
      } else {
        if (!(a.compareTo("0")>=0 && a.compareTo("9")<=0)) {
          break;
        }
      }
      at++;
    }
    // read at spaces (do we need to)
    if (hasdigits) {
      // we have got number
      if (verbose) { System.out.printf("Line number %s\n",line.substring(0,at)); }
      // we want to keep the line
      return line.substring(0,at);
    } else {
      return null;
    }
  }


  // if same==true
  // will return -1 for no lines found
  //             0 for line find exact
  //             1 for next higher line found
  // if same==false
  // will return -1 for no lines found
  //             1 for next higher line found
  //     
  int findLine(String lineno, boolean same, int pos[])
  {
    int at=0;
    boolean hasdigits=false;
    int start=0;
    int numstart=0;
  
    while (at<programText.length()) {
      hasdigits=false;
      while (at<programText.length()) {
        String a=programText.substring(at,at+1);
        if (!hasdigits) {
          if (a.equals("\n") || a.equals(" ")) { // chew these up
          } else if (a.compareTo("0")>=0 && a.compareTo("9")<=0) {
            numstart=at; // recording the actual start of the number
            hasdigits=true;
          } else {
            // didnt have a line number!
            hasdigits=false;
            break;
          }
        } else {
          if (!(a.compareTo("0")>=0 && a.compareTo("9")<=0)) {
            break;
          }
        }
        at++;
      } // end while finding line
      // read at spaces (do we need to)
      if (hasdigits) {
        // we have got number
        if (verbose) { System.out.printf(">> Line number %d to %d : %s\n",
          start,at,
          programText.substring(start,at)); }
        // we want to check the line #
        if (same && programText.substring(numstart,at).equals(lineno)) {
          if (verbose) System.out.printf("Exact match\n");
          pos[0]=start;
          return 0;

        // must be a numeric compare!! (or equivalent)
        } else if (programText.substring(numstart,at).length()>lineno.length() ||
                     programText.substring(numstart,at).length()==lineno.length() 
                   && programText.substring(numstart,at).compareTo(lineno)>0) {

          if (verbose) System.out.printf("Next higher match\n");
          pos[0]=start;
          return 1;
        }
      } 
   
      // read up until new line
      while (at<programText.length()) {
        String a=programText.substring(at,at+1);
        at++;
        if (a.equals("\n")) { start=at; break; }
      }
    }
    return -1;
  }

} // end of class Machine


//////////////////////////////////
// Exceptions, out of class
//////////////////////////////////

class BasicException extends Exception {
    BasicException() {
    }
    BasicException(String msg) {
        super(msg);
    }
}

class BasicLineNotFoundError extends BasicException {
    BasicLineNotFoundError() {
    }
    BasicLineNotFoundError(String msg) {
        super(msg);
    }
}

class BasicBREAK extends BasicException {
    BasicBREAK() {
    }
    BasicBREAK(String msg) {
        super(msg);
    }
}

class BasicRUNrestart extends BasicException {
    public String lineNo;
    BasicRUNrestart() {
    }
    BasicRUNrestart(String msg) {
        super(msg);
    }
    BasicRUNrestart(String msg, String lineNo) {
        super(msg);
        this.lineNo=lineNo;
    }
}

class BasicCONTrestart extends BasicException {
    public String lineNo;
    private String statementToken;
    BasicCONTrestart() {
    }
    BasicCONTrestart(String msg) {
        super(msg);
        this.lineNo=null;
    }
    BasicCONTrestart(String msg, String token, String lineNo) {
        super(msg);
        this.lineNo=lineNo;
    }
}

  /////////////////
 // end Machine //
/////////////////
