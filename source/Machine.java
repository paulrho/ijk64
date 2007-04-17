/////////////////////////////////////////////////////////////////////////////////
//
// $Id: Machine.java,v 1.30 2007/04/17 09:22:33 pgs Exp pgs $
//
// $Log: Machine.java,v $
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
// Revision 1.22  2006/02/16 05:53:51  pgs
// Correctly check the loop finish test depending on whether +ve or -ve step value
//
// Revision 1.21  2006/02/15 22:49:09  pgs
// Allow peeking of 197 in a simplistic fashion
//
// Revision 1.20  2006/02/15 21:27:44  pgs
// Add a evaluate_partial which simply calls
// evaluate.interpret_string_partial
//
// Revision 1.19  2006/02/15 01:54:46  pgs
// Standard header
//
//
/////////////////////////////////////////////////////////////////////////////////

// for reading a file
import java.io.*;
//import java.lang.Throwable;

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
  int save_executionpoint;
  int program_saved_executionpoint=(-1);

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
    initialise_engines(); // try this here
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

boolean builtinvariables=true;

  GenericType getvariable(String variable) {
    if (builtinvariables) {
      if (variable.equals("ti$")) {
        return new GenericType((double)(int)(System.currentTimeMillis()/1000.0));
      } else if (variable.equals("ti")) {
        return new GenericType((double)(int)((System.currentTimeMillis()/16.66666666)%1073741824));
      } else if (variable.equals("st")) {
        return new GenericType(0.0);
      } else if (variable.equals("mathpi")) {
        return new GenericType(Math.PI);
      }
    }
    return variables.getvariable(variable);
  }

  void forloopdumpstate() {
    System.out.printf("For loop stack:\n");
    for (int i=0; i<topforloopstack; ++i) {
      System.out.printf("%d) %s to %f step %f at position in code %d\n",
        i,forloopstack_var[i],forloopstack_to[i],forloopstack_step[i],forloopstack[i]);
    }
  }
  // continue...
  void createFORloop(int current, String variable, double forto, double forstep)
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
    topforloopstack++;
  }

  boolean processNEXT(int current, String var) throws BasicException {
    if (verbose) { System.out.printf("processing NEXT %s at current=%d\n",var,current); }
    // could be multiple steps?, no, this should be dealt with by the statements parser
    int fl=topforloopstack;
    executionpoint=current;
    while(true) {
      if (fl<=0) { 
        if (verbose) { System.out.printf("No more for loops to match!\n"); }

        topforloopstack=0; // new!!!

        throw new BasicLineNotFoundError("NEXT WITHOUT FOR ERROR");
        //return false; 
      }
      fl--;
      if (var.equals("") || forloopstack_var[fl].equals(var)) {
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
    //return false;
  }

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

  void initialise_engines() {
    evaluate_engine = new evaluate(this);  // create engine

    //evaluate_engine.verbose=true;
    evaluate_engine.verbose=verbose; // inherit!
    evaluate_engine.verbose=false;
    evaluate_engine.quiet=true;

    // only for C64Screen
    machinescreen=C64Screen.out;
  }

  void gotoLine(String lineNostr) throws BasicException {
    int value;
    try {
      value=Integer.parseInt(lineNostr);
    } catch(Exception e) {
      throw new BasicException("NOT NUMERIC ERROR");
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
      throw new BasicException("NOT NUMERIC ERROR");
    }
    gosubLine(value,pnt);
  }

  void gosubLine(int lineNo, int pnt) throws BasicException {
    // push on the stack where we are
    gosubstack[topgosubstack]=pnt;
    topgosubstack++;
    gotoLine(lineNo);
  }

  void popReturn() throws BasicException {
    if (topgosubstack>0) {
      topgosubstack--;
      executionpoint=gosubstack[topgosubstack];
    } else {
      System.out.printf("?RETURN WITHOUT GOSUB\n");
      // must throw an error here
      throw new BasicException("RETURN WITHOUT GOSUB");
    }
  }

  void clearMachineState() {
    toplinecache=0; // optimisation only
    topforloopstack=0;
    topgosubstack=0;
    // also clear data
    allDATA="";
    uptoDATA=0;
  }

  void cacheLine(String lineNostr, int pnt) {
    int value=Integer.parseInt(lineNostr); // this could fail?
    if (verbose) { System.out.printf("Caching line %d pointer at %d\n",value,pnt); }
    linecacheline[toplinecache]=value;
    linecachepnt[toplinecache]=pnt;
    toplinecache++;
  }

  void setCurrentLine(int pointinprogram) {
    //executionpoint=pnt; ??
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

  void dumpstate() {
    variables.dumpstate();
    forloopdumpstate();
    System.out.printf("Current Line No = %s\n",currentLineNo);
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
  // added possibility of continuation marks in DATA strings
  String building="";
  boolean quoted=false;
  boolean cont=false;
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
  int peek(int val) {
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
    if (memloc==198) {
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

/** was (still is really) in statement, it doesnt belong there - the machine
    reads the file 
**/
  static String read_a_file(String filename) throws BasicException {

    String content="";

    try {
      FileInputStream fis = new FileInputStream(filename);
      int x= fis.available();
      byte b[]= new byte[x];
      fis.read(b);
      content = new String(b);
      //System.out.println(content);
    } catch (Exception e) { 
      throw new BasicException("FILE NOT FOUND");
      // return null; 
    }

    return content;
  }

  boolean save_a_file(String filename) {
	FileOutputStream out; // declare a file output object
	PrintStream p; // declare a print stream object
	try
	{
		// Create a new file output stream
		// connected to "myfile.txt"
		out = new FileOutputStream(filename);
		// Connect print stream to the output stream
		p = new PrintStream( out );
		p.print (programText);
		p.close();
	}
	catch (Exception e)
	{
		System.err.println ("Error writing to file");
                return false;
	}
    return true;
  }



  boolean hasControlC()
  {
    return machinescreen.hasControlC();
  }
  
  void statements(String arg) {
    new statements(arg, this); // tell the statements class who I am
  }

  void variables_clr() {
    // reset all variables, by creating new ones (are the old ones garbage collected properly?
    boolean verbosekeep=variables.verbose;
    variables=new Variables();
    variables.verbose=verbosekeep;
    //variables.verbose=verbose;
    program_saved_executionpoint=(-1); // cant continue any more
  }

  String programText="";

  ///
  /// new additions for keeping the program text in the machine
  ///
  String listProgram()
  {
    return programText;
  }
  
  boolean loadProgram(String filename)
  {
    try {
      programText=read_a_file(filename);
    } catch (BasicException basicerror) {
       System.out.printf("Basic Error: %s\n",basicerror.getMessage());
       print("\n?"+basicerror.getMessage().toLowerCase()); // took off "[CR]"
       return false;
    }
    program_saved_executionpoint=(-1);
    return true;
  }

  boolean saveProgram(String filename)
  {
    return save_a_file(filename);
  }
  
  boolean contProgram() throws BasicException
  {
    if (verbose) {
      System.out.printf("wanting to continue program : program_saved_executionpoint=%d\n",program_saved_executionpoint);
    }
    if (program_saved_executionpoint<0) {
      throw new BasicException("CANT CONTINUE ERROR");
    }
    // not the interpretted immediate line!
    new statements(programText, this, program_saved_executionpoint); // passing along the machine too
    program_saved_executionpoint=save_executionpoint; // only done on running a program
    return true;
  }

  boolean runProgram()
  {
    new statements(programText, this); // passing along the machine too
    program_saved_executionpoint=save_executionpoint; // only done on running a program
    return true;
  }

  boolean runImmediate(String arg)
  {
    // by giving 0, it will not clear the Machine state
    new statements(arg, this, 0); // tell the statements class who I am
    return true;
  }

  /** inserts a line if new, or replaces a line of the same number (first one it finds)
  **/
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

void newProgramText()
{
  programText=""; // NEW program
};

} // end of class Machine


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
    BasicRUNrestart() {
    }
    BasicRUNrestart(String msg) {
        super(msg);
    }
}

class BasicCONTrestart extends BasicException {
    BasicCONTrestart() {
    }
    BasicCONTrestart(String msg) {
        super(msg);
    }
}

  /////////////////
 // end Machine //
/////////////////
