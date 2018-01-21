/////////////////////////////////////////////////////////////////////////////////
//
// $Id: statements.java,v 1.22.1.21 2012/09/04 11:22:38 pgs Exp $
//
// $Log: statements.java,v $
// Revision 1.22.1.21  2012/09/04 11:22:38  pgs
// petscii mapping
//
// Revision 1.22.1.19  2012/04/18 06:07:53  pgs
// Adding graphics capability
//
// Revision 1.22.1.18  2011/07/03 23:00:20  pgs
// Add EVAL$ function
// Fix insertspace etc modes - they were buggy
//
// Revision 1.22.1.17  2011/06/29 21:36:51  pgs
// more changes - no space required after line number
// shift return no-op
// using PETSCII
//
// Revision 1.22.1.16  2011/06/28 23:40:14  pgs
// Standardising keycodes - now use PETSCII
//
// Revision 1.22.1.15  2011/06/27 23:33:55  pgs
// More changes for 325 cut/paste, insertchar mode, List params
//
// Revision 1.22.1.13  2007/04/22 22:37:01  pgs
// Add duty cycle
//
// Revision 1.22.1.12  2007/04/19 08:28:24  pgs
// Refactoring and simplifying/formatting code especially in Machine
//
// Revision 1.22.1.11  2007/04/18 09:37:19  pgs
// More refactoring with regards to creating program/immediate modes
//
// Revision 1.22.1.10  2007/04/17 09:22:33  pgs
// Adding ability to restart with CONT
// moved all statments into statements/Machine engine
//
// Revision 1.22.1.9  2007/04/16 21:31:00  pgs
// Complete exception creation, ratify error messages, refactor code
// to use exceptions (makes code clearer)
//
// Revision 1.22.1.7  2007/04/13 09:32:43  pgs
// programText now in Machine - and used this way from C64
// in preparation for C64 online editting of program
// Added ability to enter line numbers and change program
//
// Revision 1.22.1.6  2007/04/11 17:57:58  pgs
// clearing verbose stack display (to the same as comment in code)
//
// Revision 1.22.1.3  2007/03/28 21:33:10  pgs
// Fixup end of file - exit nicely
// fixup syntax error reporting
//
// Revision 1.22.1.1  2006/02/24 04:43:53  ctpgs
// Testing an alternative reading method
//
// Revision 1.22  2006/02/21 22:24:38  pgs
// Changes related to pre-tokenising text within strings.
// This has not been made into a subversion, as it could be switched on and off easily with a boolean (not yet, but could be).
//
// Revision 1.21  2006/02/20 07:38:31  ctpgs
// Add META-SCALEY and META-BGTRANS keywords
//
// Revision 1.20  2006/02/19 22:33:35  pgs
// Add META-COLS statements
//
// Revision 1.19  2006/02/15 21:25:13  pgs
// Fixed for loop, chew up spaces before variable.
// New print method that allows partial evaluation of expressions.
//
// Revision 1.18  2006/02/15 01:55:18  ctpgs
// Standard header
//
//
/////////////////////////////////////////////////////////////////////////////////

// here we go on a new venture - basic interpreting
//   

// to do - cache the line # to the pnt # so that in a goto we can go there (or at least run through lines to (caching at same time) to find where it is)

// reads an entire basic program now 20060131

// need to separate out the ND"sdgsdg" so it evaluates as ND and "sdgsdg"
// need to work out how to do the DIM statement (doesn't interpret the top level - it sets it up
// does this happen:
//   count the brackets and separate on the outer commas
//  or change evaluate to know about a DIM statement (it separates commas etc)

// other notes, 20060203
// in c64 it appears that print expressions allow adjacency where there is an embedded token
// such that  print a$tab(16)b$  (or even atab(16)b)  is okay but not  print a$b$
// also "xxx"t$ is okay too
// one way to deal with this is to perhaps ...

import java.io.*;

class statements {

  boolean verbose=false;
  long startTime; // doesnt make a difference being in here // oh yes it does - non simultaneous measurs!
  long endTime;
  boolean speeder=true;

  statements(String interpstring, Machine themachine)
  {
    machine=themachine;
    verbose=machine.verbose; // inherit this now
    // except if we are CONTinuing
    machine.clearMachineState(); // clears the line cache and the forloops etc
    basictimer=machine.basictimer;
    interpret_string(interpstring, 0, "");
  }

  statements(String interpstring, Machine themachine, int startat)
  {
    machine=themachine;
    verbose=machine.verbose; // inherit this now
     // dont clearmachinestate - as we are continueing
    basictimer=machine.basictimer;
    interpret_string(interpstring, startat, "");
  }

  statements(String interpstring, Machine themachine, int startat, String lineNo)
  {
    machine=themachine;
    verbose=machine.verbose; // inherit this now
     // dont clearmachinestate - as we are continueing
    basictimer=machine.basictimer;
    interpret_string(interpstring, startat, lineNo);
  }

int MAXTOKENS=100;

String[] basicTokens={
  "FOR","TO","STEP","NEXT","IF","THEN","GOTO","GOSUB","RETURN","PRINT#","PRINT","ENDFRAME","DIM",
  "GET#5,",
  "POKE","OPEN","INPUT#","CLOSE","DATA","RUN","READ","RESTORE","INPUT","LIST",
  "META-VERBOSE",
  "SYS","CLR",
  "META-SCALEY","META-ROWS",
  "FAST","GET","REM",
  "META-CHARSET","LOAD","META-DUMPSTATE","META-COLS","META-BGTRANS","META-SCALE","META-TIMING",
  "NEW","SAVE","CONT","STOP",
  "EXIT",
  "LABEL"
  ,"SCREEN","GPRINT","BEGINFRAME","END","CLS","LINE","FSET","SLEEP","ALERT","RECT","FILES"
  ,"LSET"
  ,"IMAGELOAD","DRAWIMAGE","DESTROYIMAGE","CIRCLE","ANTIALIAS","PSET"
  ,"IMAGESAVE"
  ,"DIR","PWD","CHDIR","MKDIR"
  ,"ON"
  ,"DEF","LET"
  ,"HELP"
};
static final int ST_FOR=0;
static final int ST_TO=1;
static final int ST_STEP=2;
static final int ST_NEXT=3;
static final int ST_IF=4;
static final int ST_THEN=5;
static final int ST_GOTO=6;
static final int ST_GOSUB=7;
static final int ST_RETURN=8;
static final int ST_PRINThash=9;
static final int ST_PRINT=10;
static final int ST_ENDFRAME=11;
static final int ST_DIM=12;
static final int ST_GEThash=13; // this is just a work around for the moment
static final int ST_POKE=14;
static final int ST_OPEN=15;
static final int ST_INPUT1=16;
static final int ST_CLOSE=17;
static final int ST_DATA=18;
static final int ST_RUN=19;
static final int ST_READ=20;
static final int ST_RESTORE=21;
static final int ST_INPUT=22;
static final int ST_LIST=23;
static final int ST_META_VERBOSE=24;
static final int ST_SYS=25;
static final int ST_CLR=26;
static final int ST_META_SCALEY=27;
static final int ST_META_ROWS=28;
static final int ST_FAST=29;
static final int ST_GET=30;
static final int ST_REM=31;
static final int ST_META_CHARSET=32;
static final int ST_LOAD=33;
static final int ST_META_DUMPSTATE=34;
static final int ST_META_COLS=35;
static final int ST_META_BGTRANS=36;
static final int ST_META_SCALE=37;
static final int ST_META_TIMING=38;
static final int ST_NEW=39;
static final int ST_SAVE=40;
static final int ST_CONT=41;
static final int ST_STOP=42;
static final int ST_EXIT=43;
static final int ST_LABEL=44;

static final int ST_SCREEN=45;
static final int ST_GPRINT=46;
static final int ST_BEGINFRAME=47;
static final int ST_END=48;
static final int ST_CLS=49;
static final int ST_LINE=50;
static final int ST_FSET=51;
static final int ST_SLEEP=52;
static final int ST_ALERT=53;
static final int ST_RECT=54;
static final int ST_FILES=55;
static final int ST_LSET=56;

static final int ST_IMAGELOAD=57;
static final int ST_DRAWIMAGE=58;
static final int ST_DESTROYIMAGE=59;

static final int ST_CIRCLE=60;
static final int ST_ANTIALIAS=61;
static final int ST_PSET=62;
static final int ST_IMAGESAVE=63;
static final int ST_DIR=64;
static final int ST_PWD=65;
static final int ST_CHDIR=66;
static final int ST_MKDIR=67;

static final int ST_ON=68;

static final int ST_DEF=69;
static final int ST_LET=70;

static final int ST_HELP=71;


String line;
int pnt;
int linelength;
int gotToken=0;

String keepVariable;
String keepExpression;
String keepLine;

Machine machine; // we now expect to be passed this from machine itself

static final int PT_NEWLINE=0;
static final int PT_HASLINE=1;
static final int PT_TOKEN=2;
static final int PT_ASSIGN=3;
static final int PT_EOF=4; // new - end of file
int partType=PT_NEWLINE; // we start as though we are on a newline

long lastTime = 0;

static final int TIME_ReadPart=0;
static final int TIME_ReadStatementToken=1;
static final int TIME_ReadStatement=2;
static final int TIME_ReadAssign=3;
static final int TIME_RAReadExpression=4;
static final int TIME_RAReadAssignment=5;
static final int TIME_RAAssignment=6;
static final int TIME_interp_while=7;
static final int TIME_ReadPartReadStatementToken=8;
static final int TIME_ReadExpression=9;
static final int TIME_m_evaluate=10;
static final int TIME_massign=11;
static final int TIME_ReadLineNo=12;
long startTimeb[]={0,0,0,0,0,0,0,0,0,0,0,0,0};
long counter_func[]={0,0,0,0,0,0,0,0,0,0,0,0,0};
long timer_func[]={0,0,0,0,0,0,0,0,0,0,0,0,0};
String timername_func[]={"ReadPart","ReadStatementToken","ReadStatement","ReadAssign","ReadAssign->ReadExpression","ReadAssign->ReadAssignment","ReadAssign->machine.assignment","interp_whileloop","ReadPart->ReadStatementToken","ReadExpression","m_evaluate","m_assign1","ReadLineNo"};

void start_timing(int func)
{
    // not re-entrant!
    startTimeb[func]=System.currentTimeMillis(); 
}

void end_timing(int func)
{
    // not re-entrant!
    endTime=System.currentTimeMillis(); 
    counter_func[func]++;
    timer_func[func]+=endTime-startTimeb[func];
    //if (counter_func[func]%10000==0) {
    if (counter_func[func]%10000==0) {
      System.out.printf("%s c=%d t/c=%f t=%d\n",
        timername_func[func],
        //counter_func[func],timer_func[func]/(double)counter_func[func]);
        counter_func[func],timer_func[func]/(double)counter_func[func],timer_func[func]);
    }
}

void print_timing(int func)
{
      System.out.printf("%s c=%d t/c=%f t=%d (final)\n",
        timername_func[func],
        counter_func[func],timer_func[func]/(double)counter_func[func],timer_func[func]);
}

boolean ReadPart() throws BasicException
{
  // all new improved, it will return a couple of "global" codes
  // indicating:
  //   parttype
  // we read from _pnt_ on the string _line_

  // when changing String a= to chars, it took the same amount of time (in timingjar)

          if (basictimer) { // here!!!!! save this
            basictimer_thispnt=pnt;
          }
    while (pnt<linelength) {
      String a=line.substring(pnt,pnt+1);
      //if (partType==PT_NEWLINE && a.equals("\r")) continue; // try this to allow CRLF
      //else 
      if (partType==PT_NEWLINE && a.compareTo("0")>=0 && a.compareTo("9")<=0) {
        // just read it here like the token thing
if (true) { start_timing(TIME_ReadLineNo); }
        if (ReadLineNo()) {
if (true) { end_timing(TIME_ReadLineNo); }
          partType=PT_HASLINE; // we are up to statements
          // pnt+=at+1; already done (+1 is good?)
          // keepLine set to line # (string)
          // but wait, lets keep going...
          if (verbose) { System.out.printf("On Line %s\n",keepLine); }
          // not for now /// machine.currentLineNo=keepLine; // only added for error tracking - might slow things down
          if (basictimer) { // here!!!!! save this
            basictimer_thispnt=pnt;
          }

        } else {
          // we have a problem
          System.out.printf("?SYNTAX ERROR parsing lineno\n");
          throw new BasicException("SYNTAX ERROR : PARSING LINENO : NEEDS A SPACE");
          // return false;
        }
      } else if (a.compareTo("?")==0) {
        // special case print token
        pnt++;
        partType=PT_TOKEN;
        gotToken=ST_PRINT;
        return true;
      } else if (a.compareToIgnoreCase("a")>=0 && a.compareTo("z")<=0) {
        // one of two things here - a token, or an assignment!
if (true) { start_timing(TIME_ReadStatementToken); }
        if (ReadStatementToken()) {
if (true) { end_timing(TIME_ReadStatementToken); }
          partType=PT_TOKEN;
          // gotToken already set
          // pnt+=at; already done
          if (gotToken==ST_DEF || gotToken==ST_LET) { // this adds a bit EVERY statement...
            if (ReadStatementToken()) {
              partType=PT_TOKEN;
              return true;
            } else {
              partType=PT_ASSIGN;
              return true;
            }
          }
          return true;
        } else {
if (true) { end_timing(TIME_ReadStatementToken); }
          // have to assume this is an assignment
          partType=PT_ASSIGN;
          // we dont move pnt at all, we start from the start!
          return true;
        }
      } else if (a.equals("\n")) {
        partType=PT_NEWLINE;
        // keep going!
        pnt++;
      } else if (a.equals(":")) {
        pnt++; 
      } else if (a.equals(" ")) { // or TAB
        pnt++; // no, chew it up
      } else {
        // we have found something that finished the sequence
        // we have NO idea what is this!
        System.out.printf("?SYNTAX ERROR parsing got character \"%s\"\n",a);
        return false;
      }
    }
    // is this really an error? (it is the end!)
    // System.out.printf("?SYNTAX ERROR parsing - we fell off the end\n");
    // return false;
    partType=PT_EOF;
    return true;
} // ReadPart

void precache_all_lines()
{
  // read_all_lines
  // read the line# first
  // should clear all cached lines first
  machine.toplinecache=0;
  pnt=0;
  while (true) {
    SkipSpaces();
    //System.out.printf("\n");
    int start=pnt;
    if (!ReadLineNo()) {
      if (verbose) { System.out.printf("No line # -skipping\n"); }
      //break;
    } else {
      // got a line # cache it
      if (verbose) { System.out.printf("keepLine=%s pnt=%d\n",keepLine,pnt); }
      machine.cacheLine(keepLine,pnt); //was start
    }
    
    // read out rest of line
    IgnoreRestofLine();

    if (pnt>=line.length()) {
      break;
    }
    SkipNewLines();
  }

}

void precache_all_data()
{
  int keeppnt;
  // read_all_lines
  // read the line# first
  pnt=0;
  if (verbose) { System.out.printf("Looking for DATA\n"); }
  while (true) {
    SkipSpaces();
    //System.out.printf("\n");
    int start=pnt;
    if (!ReadLineNo()) {
      if (verbose) { System.out.printf("No line # -skipping\n"); }
      //break;
    } else {
		// got a line # cache it
		// shoud this be done here too?
		machine.cacheLine(keepLine,pnt); //was start
		keeppnt=pnt;
		SkipSpaces();
		if (ReadStatementToken()) {
		  if (gotToken==ST_DATA) {
			if (verbose) { System.out.printf("Found a data line\n"); }
			// from here to the end of the line is to be cached
			// read out rest of line
			String building="";
			String a;
			while (pnt<linelength && !(a=line.substring(pnt,pnt+1)).equals("\n")) {
			  building+=a;
			  pnt++;
			}
			machine.cacheDataAdd(building+"\n");
			if (verbose) { System.out.printf("Adding %s\n",building); }
		  }
		  if (gotToken==ST_LABEL) {
			SkipSpaces();
			if (verbose) { System.out.printf("Found a label\n"); }
			// from here to the end of the line is to be cached
			// read out rest of line
			String building="";
			String a;
			while (pnt<linelength && !(a=line.substring(pnt,pnt+1)).equals("\n") && !(a=line.substring(pnt,pnt+1)).equals(":")) {
			  building+=a;
			  pnt++;
			}
			// trim off ending spaces (but allow middle spaces)
			machine.cacheLabel(building.trim(),pnt); //cache the label - not the line
			if (verbose) { System.out.printf("Label %s\n",building); }
		  }
		}
    }
    IgnoreRestofLine();

    if (pnt>=line.length()) {
      break;
    }
    SkipNewLines();
  }


}


//boolean newline; // this is a bit of a work around, when we change the execution point to a new line
//hmm, actually, changing mind, set the line cache to the first statement!

static int MAXBASICTIMER=30000;
boolean basictimer=false;
int basictimer_lastpnt=(-1);
int basictimer_thispnt;
long basictimer_lasttime;
int basictimer_times[]=new int[MAXBASICTIMER];
int basictimer_count[]=new int[MAXBASICTIMER];
int basictimer_c=0;

//void clearBasicTimer() {
  //int i;
  //for (i=0; i<MAXBASICTIMER; ++i) {
    //basictimer_times[i]=0;
    //basictimer_count[i]=0;
  //}
  //basictimer_lastpnt=(-1);
//}

void doBasicTimer(boolean finalprint) {
    if (basictimer) {
      long timenow=System.currentTimeMillis(); 
      if (basictimer_lastpnt>=0) {
        basictimer_times[basictimer_lastpnt]+=(int)(timenow-basictimer_lasttime);
        basictimer_count[basictimer_lastpnt]++;
        //System.out.printf("BASICTIMER: pnt=%d td=%d line=%s\n",basictimer_lastpnt,timenow-basictimer_lasttime,machine.getCurrentLine(basictimer_lastpnt));
        basictimer_c++;
        if (basictimer_c>50000 || finalprint) { // need to make this configurable
          basictimer_c=0;
          int i;
          System.out.printf("BASICTIMER SUMMARY----at %d--for %s\n",timenow,machine.program_name);
          if (false) ; else
              System.out.printf("  %5s %8s %9s %10s  %5s %s\n","pnt=","t=","c=","tp=","line=","stm=");
          for (i=0; i<MAXBASICTIMER; ++i) if (basictimer_count[i]>0) {
            if (false)
              System.out.printf("  pnt= %5d t= %8d c= %9d tp= %10.3f line= %5s %s\n",
                i,basictimer_times[i],basictimer_count[i],
                (double)basictimer_times[i]/(double)basictimer_count[i],
                 machine.getCurrentLine(i),
                 (line.substring(i,(i+15>line.length()-1?line.length()-1:i+15))+"...").replaceAll("^[ ]*:","").replaceAll("\\r.*|\\n.*|:.*", "")
              );
            else
              System.out.printf("  %5d %8d %9d %10.3f  %5s %s\n",
                i,basictimer_times[i],basictimer_count[i],
                (double)basictimer_times[i]/(double)basictimer_count[i],
                 machine.getCurrentLine(i),
                 (line.substring(i,(i+15>line.length()-1?line.length()-1:i+15))+"...").replaceAll("^[ ]*:","").replaceAll("\\r.*|\\n.*|:.*", "")
              );
          }
          System.out.printf("BASICTIMER -----------\n");
          timenow=System.currentTimeMillis();  // hide the profiling print time!
        }
      }
      int pnt_tmp=basictimer_thispnt;
      while (pnt_tmp<linelength && line.substring(pnt_tmp,pnt_tmp+1).equals(" ")) { pnt_tmp++; }
      basictimer_lastpnt=pnt_tmp;
      basictimer_lasttime=timenow;
    }
}


void interpret_string(String passed_line, int startat, String lineNo)
{
  line=passed_line;
  linelength=line.length();
  if (verbose) { System.out.printf("\n**************************************\n"); }

  if (machine.hasControlC()) { } // do nothing - just clear it initially!

  // skip to the chase, and just read the line #s
  precache_all_lines();
  precache_all_data();
  if (verbose) { System.out.printf("DATA is:\n%s\n",machine.allDATA); }

  if (!lineNo.equals("")) {
    try {
      machine.gotoLine(lineNo);
    } catch (BasicException basicerror) {
      System.out.printf("Basic Error: %s\n",basicerror.getMessage());
      machine.print("\n"+"?"+basicerror.getMessage().toLowerCase());
      return;
    } 
    pnt=machine.executionpoint;
  } else
    pnt=startat; // 0 usually
  // the newline might play havok with CONT
  // read the line# first
  partType=PT_NEWLINE; // we start as though we are on a newline
  // no need any more // machine.currentLineNo=""; // do this to stop the keeping of previously used line #s

  machine.save_executionpoint=(-1); // effectively flagging not restartable

  long partcount=0; // will this slow it down

  try {
  try {

  long startTime2=0;
  System.currentTimeMillis(); 
  long startTime1=System.currentTimeMillis(); 
  long cnt2=0;
  while (pnt<linelength) {
if (true) { start_timing(TIME_interp_while); }
  startTime2+=System.currentTimeMillis(); 
    // for each part, check control C status - is this inefficient?
    if (machine.hasControlC()) {
      machine.save_executionpoint=pnt; // it is restartable
      if (verbose) {
        System.out.printf("setting save_executionpoint to %d\n",machine.save_executionpoint);
      }
      throw new BasicBREAK("BREAK");
    }
    if (machine.partialDutyCycle>0 && ((partcount++)%20)==0) {
      try {
        Thread.sleep(machine.partialDutyCycle);
      }
      catch(InterruptedException e) {
      }
      //machine.dutyCycle(); 
    }

    // change order to see if CONT will work now
                                                          if (true) { start_timing(TIME_ReadPart); }
    if (!ReadPart()) { 
      throw new BasicException("SYNTAX ERROR");
    }
                                                          if (true) { end_timing(TIME_ReadPart); }
    if (basictimer) { doBasicTimer(false); }


    try {
      if (partType==PT_EOF) {
        break; // better to put this at the end of the if condition (for speed)
      } else if (partType==PT_TOKEN) {
                                                          if (true) { start_timing(TIME_ReadStatement); }
        if (!ReadStatement()) {
          throw new BasicException("SYNTAX ERROR : BAD TOKEN");
        }
                                                          if (true) { end_timing(TIME_ReadStatement); }
      } else if (partType==PT_ASSIGN) {
                                                          if (true) { start_timing(TIME_ReadAssign); }
        if (!ReadAssign()) {
          //throw new BasicException("SYNTAX ERROR : NO TOKEN OR ASSIGNMENT");
          // simplify this now, we didnt get a token OR an assignment - just a plain old syntax error
          throw new BasicException("SYNTAX ERROR");          
        }
                                                          if (true) { end_timing(TIME_ReadAssign); }
      } else {
        throw new BasicException("SYNTAX ERROR : DID NOT GET STATEMENT");
      }
    } catch (ArrayIndexOutOfBoundsException e) { 
      e.printStackTrace();
      throw new BasicException("ILLEGAL QUANTITY");
    }
                                                          if (true) { end_timing(TIME_interp_while); }
                                                            startTime2-=System.currentTimeMillis();  cnt2++;
  } // while everything
                                                          if (true) { print_timing(TIME_interp_while); }
                                                          if (true) { print_timing(TIME_ReadPart); }
                                                          if (true) { print_timing(TIME_ReadStatement); }
                                                          if (true) { print_timing(TIME_ReadAssign); }
                                                          if (true) { print_timing(TIME_massign); }
                                                          if (true) { print_timing(TIME_ReadExpression); }
                                                          if (true) { print_timing(TIME_ReadStatementToken); }
                                                          if (true) { print_timing(TIME_ReadLineNo); }
                                                          if (true) { print_timing(TIME_RAReadAssignment); }
                                                          if (true) { System.out.printf("diff = %d\n", System.currentTimeMillis()-startTime1); }
                                                          if (true) { System.out.printf("2    = %d over %d\n", startTime2,cnt2); }

  } catch (BasicCONTrestart contrestart) {
     // this is a special case where we want to start/restart the program 
     // just like runProgram
     // this way is a bit dodgy - it nests it!
     machine.contProgram(contrestart.lineNo);
     // goes from direct mode to program mode
     // either by NEXT(into prog) RETURN GOTO GOSUB or CONT
  } catch (BasicRUNrestart runrestart) {
     // this is a special case where we want to start/restart the program 
     // just like runProgram
     // this way is a bit dodgy - it nests it!
     machine.variables_clr();
     if (runrestart.lineNo.equals("")) 
       machine.runProgram();
     else
       machine.contProgram(runrestart.lineNo);
  } // nested

  } catch (BasicException basicerror) {
     machine.setCurrentLine(pnt);
     System.out.printf("Basic Error: %s%s\n",basicerror.getMessage(),
       (machine.program_running && !machine.currentLineNo.equals("")?" on line "+machine.currentLineNo:"")
     );
     machine.print("\n"+"?"+basicerror.getMessage().toLowerCase()+
       (machine.program_running && !machine.currentLineNo.equals("")?" on line "+machine.currentLineNo:"")
     );
  } 

  if (verbose) { machine.dumpstate(); }
  machine.executionpoint=pnt;
  if (basictimer) { doBasicTimer(true); }
}

boolean ReadStatement() throws BasicException
{
  // read statement should also read the colon at the end, ready for the next statement
  // none of these return false, they throw an exception
  if (verbose) { System.out.printf("In ReadStatement\n"); }
  if (verbose) { System.out.printf("Got %d as token\n",gotToken); }
    switch(gotToken) {
      case ST_FOR: if (ProcessFORstatement()) { return true; } break;
      case ST_NEXT: if (ProcessNEXTstatement()) { return true; } break;
      case ST_IF: if (ProcessIFstatement()) { return true; } break;
      case ST_ON: if (ProcessONstatement()) { return true; } break;
      case ST_GOTO: if (ProcessGOTOstatement()) { return true; } break;
      case ST_GOSUB: if (ProcessGOSUBstatement()) { return true; } break;
      case ST_RETURN: if (ProcessRETURNstatement()) { return true; } break;
      case ST_PRINT: if (ProcessPRINTstatement()) { return true; } break;
      case ST_REM: if (ProcessREMstatement()) { return true; } break;
      case ST_PRINThash: // because it would have gone elsewhere
        if (ProcessPRINThashstatement()) { return true; } break;
      case ST_FAST:  // I wish
        if (ProcessIGNOREstatement()) { return true; } break;
      case ST_RESTORE: 
        machine.uptoDATA=0; // revert back again
        if (ProcessIGNOREstatement()) { return true; } return true;
      case ST_READ: 
        if (ProcessREADstatement()) { return true; }
        // this is tricky, even though I added (and great effor) the
        // ability to assign to "lists" of variables i.e. A,B=6,7
        // this doesnt reall work for READ and DATA, as it is not a
        // fix string of DATA, whereis we have a stream.
        break;
      case ST_CLR: 
        machine.variables_clr();
        if (ProcessIGNOREstatement()) { return true; } break;
      case ST_DATA: 
        if (ProcessIGNOREstatement()) { return true; } break;
      case ST_CONT: 
        if (ProcessCONTstatement()) { return true; } break;
      case ST_RUN: 
        if (ProcessRUNstatement()) { return true; } break;
      case ST_DIM: 
        if (ProcessDIMstatement()) { return true; } break;
      case ST_GET:
        if (ProcessGETstatement()) { return true; } break;
      case ST_GEThash:
        if (ProcessGEThashstatement()) { return true; } break;
      case ST_META_DUMPSTATE:
        machine.dumpstate();
        return true;
      case ST_META_VERBOSE:
        ReadExpression();
        if (!keepExpression.equals("")) {
          int level=(int)machine.evaluate(keepExpression).num();
          switch(level) {
            case 1:
              verbose=true; machine.verbose=true;
              machine.evaluate_engine.verbose=true;
              break;
            case 2:
              machine.machinescreen.verbosePrint=true;
              break;
            case 0:
            default:
              verbose=false; 
              machine.verbose=false;
              machine.evaluate_engine.verbose=false;
              machine.machinescreen.verbosePrint=false;
          }
        } else {
          verbose=true; machine.verbose=true;
          machine.evaluate_engine.verbose=true;
        }
        return true;
      case ST_META_CHARSET:
        if (ProcessMETACHARSETstatement()) { return true; } break;
      case ST_META_SCALE:
        if (ProcessMETASCALEstatement()) { return true; } break;
      case ST_META_SCALEY:
        if (ProcessMETASCALEYstatement()) { return true; } break;
      case ST_NEW:
        machine.newProgramText();
        return true;
      case ST_META_TIMING:
        long markTime = System.currentTimeMillis();
        // the parameter is an integer indicating HOW MANY things happened
        ReadExpression();
        int howmany=(int)machine.evaluate(keepExpression).num();
        // ... the code being measured ...
        // long estimatedTime = System.nanoTime() - startTime;
        //System.out.printf("System time = %d nanoseconds\n",startTime);
        if (lastTime==0) {
          System.out.printf("System time = %d milliseconds\n",markTime);
        }  else {
          System.out.printf("Delta time = %d milliseconds\n",markTime-lastTime);
          if (howmany>0) {
            System.out.printf("Unit time  = %.6f milliseconds (%d iterations)\n",(markTime-lastTime)/(double)howmany,howmany);
            System.out.printf("Rate       = %.6f iterations per second\n",howmany/(double)(markTime-lastTime)*1000.0);
          }
        }
        lastTime=markTime;
        return true;
      case ST_SAVE:
        if (ProcessSAVEstatement()) { return true; } break;
      case ST_LOAD:
        if (ProcessLOADstatement()) { return true; } break;
      case ST_META_ROWS:
        if (ProcessMETAROWSstatement()) { return true; } break;
      case ST_META_COLS:
        if (ProcessMETACOLSstatement()) { return true; } break;
      case ST_META_BGTRANS:
        if (ProcessMETABGTRANSstatement()) { return true; } break;
      case ST_INPUT:
        if (ProcessINPUTstatement(false)) { return true; } break;
      case ST_INPUT1:
        if (ProcessINPUTstatement(true)) { return true; } break;
      case ST_SYS:
        if (ProcessSYSstatement()) { return true; } break;
      case ST_POKE:
        if (ProcessPOKEstatement()) { return true; } break;
      case ST_OPEN:
        if (ProcessOPENstatement()) { return true; } break;
      case ST_CLOSE:
        if (ProcessCLOSEstatement()) { return true; }
        break;
      case ST_LIST:
        if (ProcessLISTstatement()) { return true; }
        break;
      case ST_HELP:
//        machine.print("?help! help! try this: load\"$\",8 [enter]");
//        machine.print("?help! try this: load\"$\",8 [enter]");
        machine.print("tokens: version "+version.programVersion+"\n");
        { String collect=""; int x=0;
          for (int tok=0; tok<basicTokens.length; ++tok) {
            x+=basicTokens[tok].length()+1;
            if (x>=machine.machinescreen.maxX-2) {  x=basicTokens[tok].length()+1; collect+="\n"; }
            collect+=basicTokens[tok].toLowerCase()+":";
            if (basicTokens[tok].toLowerCase().equals("rem")) { collect+="\n"; x=0; }
          }
          machine.print(machine.syntaxHighlight(collect));
        }
        machine.print("\n");
        machine.print("?help: try this (and press [");
        machine.print("enter])\n");
        machine.print("load\"$\",8   :rem loads internal dir\n");
        machine.print("load\"%\",8  :rem loads cloud directory\n");
        machine.print("list\n");
        return true;
        //break;
      case ST_STOP: 
        machine.save_executionpoint=pnt; // it is restartable
        if (verbose) {
          System.out.printf("setting save_executionpoint to %d\n",machine.save_executionpoint);
        }
        throw new BasicBREAK("BREAK ON STOP");
        //break;
      case ST_END: 
        machine.save_executionpoint=pnt; // it is restartable // yes, END is restartable!
        if (verbose) {
          System.out.printf("setting save_executionpoint to %d\n",machine.save_executionpoint);
        }
        MachineEND();
        return true; // just END!
      case ST_EXIT: 
        if (machine.performExit(true)) {
          MachineEND(); // not sure if I also need this
          return true;
        }
        break;
      case ST_LABEL:
        // just ignore it
        ReadExpression();
        return true;

      case ST_ALERT:
        ReadExpression();
        // ignored for now
       
        PlaySound sound = new PlaySound("alert"+keepExpression+".wav");
        return true;

      case ST_SCREEN:
        { 
          ReadExpression();
          GenericType gt=machine.evaluate(keepExpression);
          if (gt.gttop==3) {
            if (machine.graphicsDevice==null) {
              machine.graphicsDevice = new GraphicsDevice(
                (int)gt.gtlist[1].num(),(int)gt.gtlist[2].num());
	      machine.graphicsDevice.redirectkeys(machine.machinescreen); // should be done elsewhere
            } else {
              // just to make sure it is all reset
              machine.graphicsDevice.resetDevice(
                (int)gt.gtlist[1].num(),(int)gt.gtlist[2].num());
              // and make sure it is visible again (in case you closed it)
              machine.graphicsDevice.setVisible(true);
              //if (true) machine.machinescreen.setVisible(true);
            }
          } else {
            if (machine.graphicsDevice==null) {
              machine.graphicsDevice = new GraphicsDevice();
	      machine.graphicsDevice.redirectkeys(machine.machinescreen); // should be done elsewhere
            } else {
              // just to make sure it is all reset
              machine.graphicsDevice.resetDevice();
              // and make sure it is visible again (in case you closed it)
              machine.graphicsDevice.setVisible(true);
              //if (true) machine.machinescreen.setVisible(true);
            }
          }
          if (true) machine.machinescreen.setVisible(true); // refocus on main
          return true;
        }

      case ST_SLEEP:
        ReadExpression();
        //if (machine.graphicsDevice!=null) machine.graphicsDevice.command_SLEEP((int)machine.evaluate(keepExpression).num());
        // now call static version always
        GraphicsDevice.command_SLEEP((int)machine.evaluate(keepExpression).num());
        return true;

      case ST_CHDIR:
	{
          ReadExpression();
          GenericType gt=machine.evaluate(keepExpression);
          if (gt.gttop==1 && !gt.isNum() && !gt.str().equals("")) {
            machine.doCHDIR(gt.str());
	  } else {
            //throw new BasicException("ILLEGAL PARAMETERS ERROR"); // wrong number params
	    // should print the current dir
            machine.print(machine.cloudNet+"\n");
	  }
          return true;
	}
      case ST_DIR:
	{
          ReadExpression();
          GenericType gt=machine.evaluate(keepExpression);
          machine.listDIR(gt.str(),keepExpression.startsWith("-"));
          return true;
	}
      case ST_FILES:
        machine.listFiles();
        return true;
      case ST_CLS:
        if (machine.graphicsDevice!=null) machine.graphicsDevice.command_CLS();
        return true;
      case ST_BEGINFRAME:
        if (machine.graphicsDevice!=null) machine.graphicsDevice.command_BEGINFRAME();
        return true;
      case ST_ENDFRAME:
        if (machine.graphicsDevice!=null) machine.graphicsDevice.command_ENDFRAME();
        return true;
  //,"SCREEN","GPRINT","BEGINFRAME","ENDFRAME","CLS","LINE","FSET"

      case ST_LINE:
        if (machine.graphicsDevice!=null) {
          ReadExpression();
          GenericType gt=machine.evaluate(keepExpression);
          if (gt.gttop==5) {
              if (verbose) System.out.printf("about to draw line\n");
              machine.graphicsDevice.command_LINE(
                (int)gt.gtlist[0].num(),
                (int)gt.gtlist[1].num(),
                (int)gt.gtlist[2].num(),
                (int)gt.gtlist[3].num(),
                (int)gt.gtlist[4].num()
               );
              return true;
          } else
            throw new BasicException("ILLEGAL PARAMETERS ERROR"); // wrong number params

        } else throw new BasicException("GRAPHICS NOT ACTIVE");
        //break;

      case ST_LSET:
        if (machine.graphicsDevice!=null) {
          ReadExpression();
          GenericType gt=machine.evaluate(keepExpression);
          if (gt.gttop==2) {
              if (verbose) System.out.printf("about to set line size\n");
              machine.graphicsDevice.command_LSET(
                (int)gt.gtlist[0].num(),
                (int)gt.gtlist[1].num()
               );
              return true;
          } else
            throw new BasicException("ILLEGAL PARAMETERS ERROR"); // wrong number params

        } else throw new BasicException("GRAPHICS NOT ACTIVE");
        //break;
	//
      case ST_PSET:
        if (machine.graphicsDevice!=null) {
          ReadExpression();
          GenericType gt=machine.evaluate(keepExpression);
          if (gt.gttop==3) {
              if (verbose) System.out.printf("about to set line size\n");
              machine.graphicsDevice.command_PSET(
                (int)gt.gtlist[0].num(),
                (int)gt.gtlist[1].num(),
                (int)gt.gtlist[2].num()
               );
              return true;
          } else
            throw new BasicException("ILLEGAL PARAMETERS ERROR"); // wrong number params

        } else throw new BasicException("GRAPHICS NOT ACTIVE");
        //break;

      case ST_ANTIALIAS:
        if (machine.graphicsDevice!=null) {
          ReadExpression();
          GenericType gt=machine.evaluate(keepExpression);
          if (gt.gttop==1) {
              if (verbose) System.out.printf("about to set antialias\n");
              machine.graphicsDevice.command_ANTIALIAS( (int)gt.num());
              return true;
          } else
            throw new BasicException("ILLEGAL PARAMETERS ERROR"); // wrong number params
        } else throw new BasicException("GRAPHICS NOT ACTIVE");
        //break;

      case ST_CIRCLE:
        if (machine.graphicsDevice!=null) {
          ReadExpression();
          GenericType gt=machine.evaluate(keepExpression);
          if (gt.gttop==1) {
            // special - command the offset type
	    
            machine.graphicsDevice.command_CIRCLE_CONTROL((int)gt.num());
              return true;
	  } else if (gt.gttop==5) {
              if (verbose) System.out.printf("about to draw circle\n");
              machine.graphicsDevice.command_CIRCLE(
                (int)gt.gtlist[0].num(),
                (int)gt.gtlist[1].num(),
                (int)gt.gtlist[2].num(),
                (int)gt.gtlist[3].num(),
                (int)gt.gtlist[4].num()
               );
              return true;
          } else
            throw new BasicException("ILLEGAL PARAMETERS ERROR"); // wrong number params

        } else throw new BasicException("GRAPHICS NOT ACTIVE");
        //break;

      case ST_RECT:
        if (machine.graphicsDevice!=null) {
          ReadExpression();
          GenericType gt=machine.evaluate(keepExpression);
          if (gt.gttop==5) {
              if (verbose) System.out.printf("about to draw rect\n");
              machine.graphicsDevice.command_RECT(
                (int)gt.gtlist[0].num(),
                (int)gt.gtlist[1].num(),
                (int)gt.gtlist[2].num(),
                (int)gt.gtlist[3].num(),
                (int)gt.gtlist[4].num()
               );
              return true;
          } else
          if (gt.gttop==9) {
              if (verbose) System.out.printf("about to filled quad poly \n");
              machine.graphicsDevice.command_FILL(
                (int)gt.gtlist[0].num(),
                (int)gt.gtlist[1].num(),
                (int)gt.gtlist[2].num(),
                (int)gt.gtlist[3].num(),
                (int)gt.gtlist[4].num(),
                (int)gt.gtlist[5].num(),
                (int)gt.gtlist[6].num(),
                (int)gt.gtlist[7].num(),
                (int)gt.gtlist[8].num()
               );
              return true;
          } else
            throw new BasicException("ILLEGAL PARAMETERS ERROR"); // wrong number params

        } else throw new BasicException("GRAPHICS NOT ACTIVE");
        //break;

      case ST_GPRINT:
        if (machine.graphicsDevice!=null) {
          ReadExpression();
          GenericType gt=machine.evaluate(keepExpression);
          if (gt.gttop==4) {
              if (verbose) System.out.printf("about to draw string \"%s\"\n",gt.gtlist[0].str());
              //for (int i=0; i<gt.gttop; ++i) System.out.printf("isNum[%d]=%s\n",i,gt.gtlist[i].isNum()?"yes":"no");
              machine.graphicsDevice.command_GPRINT(
                gt.gtlist[0].str(),
                (int)gt.gtlist[1].num(),
                (int)gt.gtlist[2].num(),
                (int)gt.gtlist[3].num()
               );
              return true;
          } else
            throw new BasicException("ILLEGAL PARAMETERS ERROR"); // wrong number params

        } else throw new BasicException("GRAPHICS NOT ACTIVE");
        //break;

      case ST_FSET:
        if (machine.graphicsDevice!=null) {
          ReadExpression();
          GenericType gt=machine.evaluate(keepExpression);
          if (gt.gttop==2) {
              machine.graphicsDevice.command_FSET(
                gt.gtlist[0].str(),
                (int)gt.gtlist[1].num()
               );
              return true;
          } else
            throw new BasicException("ILLEGAL PARAMETERS ERROR"); // wrong number params

        } else throw new BasicException("GRAPHICS NOT ACTIVE");
        //break;

      case ST_IMAGELOAD:
        if (machine.graphicsDevice!=null) {
          ReadExpression();
          GenericType gt=machine.evaluate(keepExpression);
          if (gt.gttop==1) {
              if (verbose) System.out.printf("load the image to reference\n");
              int imgno=machine.graphicsDevice.command_LOADIMAGE(
                machine.fileUnalias(gt.str())
               );
               // special case here! so if it works
              machine.assignment("imgno="+imgno);
              return true;
          } else //throw new BasicException("INCORRECT PARAMETERS");
            throw new BasicException("ILLEGAL PARAMETERS ERROR"); // wrong number params

        } else throw new BasicException("GRAPHICS NOT ACTIVE");
        //break;

      case ST_IMAGESAVE:
        if (machine.graphicsDevice!=null) {
          ReadExpression();
          GenericType gt=machine.evaluate(keepExpression);
          if (gt.gttop==1) {
              try { 
                machine.graphicsDevice.command_SAVEIMAGE(
                  gt.str()
                 );
              } catch (Exception e) {
                if (verbose) { e.printStackTrace(); }
                throw new BasicException("IMAGE SAVE ERROR");
              }
              return true;
          } else //throw new BasicException("INCORRECT PARAMETERS");
            throw new BasicException("ILLEGAL PARAMETERS ERROR"); // wrong number params

        } else throw new BasicException("GRAPHICS NOT ACTIVE");
        //break;

      case ST_DRAWIMAGE:
        if (machine.graphicsDevice!=null) {
          ReadExpression();
          GenericType gt=machine.evaluate(keepExpression);
          if (gt.gttop==5) {
              if (verbose) System.out.printf("draw the image\n");
              machine.graphicsDevice.command_DRAWIMAGE(
                (int)gt.gtlist[0].num(),
                (int)gt.gtlist[1].num(),
                (int)gt.gtlist[2].num(),
                (double)gt.gtlist[3].num(),
                (double)gt.gtlist[4].num()
               );
              return true;
          } else //throw new BasicException("INCORRECT PARAMETERS");
            throw new BasicException("ILLEGAL PARAMETERS ERROR"); // wrong number params

        } else throw new BasicException("GRAPHICS NOT ACTIVE");
        //break;
    }
    return false;
}

boolean ReadAssign() throws BasicException {
    // could be a null statement
    //if (ReadColon()) { return true; }

    // could be an assignment
                                                 if (true) { start_timing(TIME_RAReadAssignment); }
    if (true || ReadAssignment()) {        // just ignore this - seem what happens!
                                                 if (true) { end_timing(TIME_RAReadAssignment); }
                                                 if (true) { start_timing(TIME_ReadExpression); }
//speeder
      int sp_start=pnt;
      ReadExpression();
                                                 if (true) { end_timing(TIME_ReadExpression); }

                                                 if (verbose) { System.out.printf("MachineVariableSet(variable=%s with evaluate( %s ))\n",keepVariable,keepExpression); }
      // parse the keep variable in the machine to turn X(I+1) into X(42)

                                                 if (true) { start_timing(TIME_massign); }
      if (!true) { machine.assignment(keepVariable+"="+keepExpression); }

      int sp_end=pnt;
      if (speeder) { System.out.printf("ASSIGNMENT:(%d,%d,%s) %s\n",sp_start,sp_end,machine.getCurrentLine(sp_start),keepExpression); }

      machine.assignment(keepExpression);
                                                 if (true) { end_timing(TIME_massign); }
      if (speeder) { System.out.printf("Compiled obj: %s\n",machine.evaluate_engine.compiled_obj); }
      if (speeder) { System.out.printf("Compiled asm:\n%s",machine.evaluate_engine.compiled_asm); }

      //machine.setvariable(machine.parse(keepVariable),machine.evaluate(keepExpression));
      // just change this one for the moment - try new way

      ReadColon();
      return true;
    } else {
if (true) { end_timing(TIME_RAReadAssignment); }
      // Did not get token or assignment (was 103:)
      return false;
    }
}

//---------------------------------------------------------------------//
long counter_ReadStatementToken=0;
long timer_ReadStatementToken=0;
char firstchar[]=new char[100];
boolean RSTinitted=false;
int catop[]=new int[30];
int chainarray[][]=new int[30][10];

    // ReadStatementToken will identify a token from the current "pnt" in the basic text.
    // It is very inefficient and should be re-written.
    // A significant portion of the time gets spent in here.
    
boolean ReadStatementToken() {
  if (!RSTinitted) {
    for (int i=0; i<30; ++i) { catop[i]=0; }
    for (int tok=0; tok<basicTokens.length; ++tok) {
      firstchar[tok]=basicTokens[tok].charAt(0);
      //System.out.printf("char is %c\n",firstchar[tok]);
      int index=basicTokens[tok].charAt(0)-'A';
      //System.out.printf("array %d,%d = %d\n",index,catop[index],tok);
      chainarray[index][catop[index]++]=tok;
    }
    RSTinitted=true;
  }

//if (true) { start_timing(TIME_ReadStatementToken); }

  // just test the first char!
  if (pnt>=linelength) return false; // guards against failing off end
  char ch=line.charAt(pnt);
  if (ch>='a'&&ch<='z') {ch-='a'-'A'; }
  int index=ch-'A';
  if (index>=26 || index<0) { return false; }
  //for (int tok=0; tok<basicTokens.length; ++tok) {

  for (int i=0; i<catop[index]; ++i) {
    int at=0;
    int tok=chainarray[index][i];
    //System.out.printf("inside array char is %c tok=ch[%d][%d] tok=%d\n",ch,index,i,tok);
    if (ch!=firstchar[tok]) continue;
    //System.out.printf("matching char %c for token %s\n",ch,basicTokens[tok]);

    while (pnt+at<linelength && at<basicTokens[tok].length()) {
      char cchar=line.charAt(pnt+at);
      if (cchar>='a' && cchar<='z') {cchar-='a'-'A'; }
      if (cchar!=basicTokens[tok].charAt(at)) {
        break;
      }
      at++;
    }
    if (at==basicTokens[tok].length()) {
      // we matched it completely
      pnt+=at;
      if (verbose) { System.out.printf("Found match for %s\n",basicTokens[tok]); }
      gotToken=tok;

//if (true) { end_timing(TIME_ReadStatementToken); }

      return true;
    }
  }
  // otherwise, search for an "=" sign, and take note of the string preceeding
  //ReadAssignment();

//if (true) { end_timing(TIME_ReadStatementToken); }

  return false;
}

boolean ReadStatementToken_old() {
  startTime=System.currentTimeMillis(); 
  for (int tok=0; tok<basicTokens.length; ++tok) {
    int at=0;
    //System.out.printf("Comparing %s\n",basicTokens[tok]);
    while (pnt+at<linelength && at<basicTokens[tok].length()) {
      if (!line.substring(pnt+at,pnt+at+1).equalsIgnoreCase(basicTokens[tok].substring(at,at+1))) {
        break;
      }
      at++;
    }
    if (at==basicTokens[tok].length()) {
      // we matched it completely
      pnt+=at;
      if (verbose) { System.out.printf("Found match for %s\n",basicTokens[tok]); }
      gotToken=tok;

      endTime=System.currentTimeMillis(); 
      counter_ReadStatementToken++;
      timer_ReadStatementToken+=endTime-startTime;

      return true;
    }
  }
  // otherwise, search for an "=" sign, and take note of the string preceeding
  //ReadAssignment();

  endTime=System.currentTimeMillis(); 
  counter_ReadStatementToken++;
  timer_ReadStatementToken+=endTime-startTime;
  if (false && counter_ReadStatementToken%10000==0) {
    System.out.printf("ReadStatementToken c=%d t/c=%f\n",counter_ReadStatementToken,timer_ReadStatementToken/(double)counter_ReadStatementToken);
  }

  return false;
}

boolean ProcessDIMstatement() throws BasicException
{
  if (verbose) { System.out.printf("Processing DIM statement\n"); }
  ReadExpression();
  if (verbose) { System.out.printf("Machine to DIM( %s )\n",keepExpression); }
  machine.evaluate(keepExpression); // throw away the result, we are just dimensioning the array
  ReadColon();
  return true;
}

boolean ProcessPRINTstatement_original() throws BasicException
{
  if (verbose) { System.out.printf("Processing PRINT statement\n"); }
  ReadExpression();
  if (verbose) { System.out.printf("MachinePrintEvaluate( %s )\n",keepExpression); }
  if (verbose  ) { System.out.printf("%s",machine.evaluate(keepExpression).print()); }
  machine.print(machine.evaluate(keepExpression).print());
  // Machine evaluate this expression and PRINT it!
  //System.out.printf("Would evaluate the expression %s\n","not finished coding yet");
  // if we finished with ";", then loop again
  keepExpression="dummy"; // this is here to allow for a null print PRINT this means a new line will get issued
  while (pnt<linelength && line.substring(pnt,pnt+1).equals(";")) {
    pnt++;
    keepExpression=""; // to prepare for Nothing read
    ReadExpression();
    if (verbose) { System.out.printf("MachinePrintEvaluate( %s )\n",keepExpression); }
    if (verbose  ) { System.out.printf("%s",machine.evaluate(keepExpression).print()); }
    machine.print(machine.evaluate(keepExpression).print());
    //System.out.printf("  also would evaluate the expression %s\n","not finished coding yet");
  }
  if (!keepExpression.equals("")) { 
    if (verbose  ) { System.out.printf("\n"); }
    machine.printnewline();
  }
  ReadColon(); // check
  return true;
}

boolean ProcessPRINTstatement() throws BasicException
{
  if (verbose) { System.out.printf("Processing PRINT statement\n"); }
  ReadExpression();
  if (verbose) { System.out.printf("MachinePrintEvaluate( %s )\n",keepExpression); }
  /// if (verbose  ) { System.out.printf("%s",machine.evaluate_partial(keepExpression).print()); }
  /// machine.print(machine.evaluate_partial(keepExpression).print());
  // this should probably be nicer
  int x=0;
  String separator="";
  /// while ((x=machine.evaluate_engine.parse_restart)>0) { 
  do {
    if (verbose) { System.out.printf("More to evaluate!!!! - should resubmit with the remaining string after %d...\n",machine.evaluate_engine.parse_restart); }
    // there is more to evaluate
    String temp=new String(keepExpression);  // do I need to do this?
    keepExpression=temp.substring(x,temp.length());

    x=0; // now we are at the start of it again
    while (x<keepExpression.length() && 
      (keepExpression.substring(x,x+1).equals(";") || keepExpression.substring(x,x+1).equals(",") || keepExpression.substring(x,x+1).equals(" ")))
      { 
        if (keepExpression.substring(x,x+1).equals(",")) {
          if (verbose) { System.out.printf("Should space out to next position....(Not impl yet)\n"); }
          int cursx=machine.machinescreen.cursX;
          //int tab=cursx-(int)(cursx/10)*10; if (tab>0) tab=10-tab;
          int tab=cursx-(int)(cursx/10)*10; if (tab>=0) tab=10-tab;
          if (cursx+tab>=machine.machinescreen.maxX) {
             machine.print("\n");
          } else {
             machine.machinescreen.cursX+=tab;
             if (verbose) { System.out.printf("adding %d tab\n",tab); }
          }
          separator=","; 
        } else if (keepExpression.substring(x,x+1).equals(";")) {
          separator=";"; 
        }
        x++;
      } // chew them up // need to include blanks and , too!
    /// String
    temp=new String(keepExpression);  // do I need to do this?
    keepExpression=temp.substring(x,temp.length());
    if (keepExpression.equals("")) { break; }

    if (verbose) { System.out.printf("should resubmit with the remaining string after %d will do so with:%s\n",machine.evaluate_engine.parse_restart,keepExpression); }
    machine.print(machine.evaluate_partial(keepExpression).print());
    separator="";
  /// }
  } while ((x=machine.evaluate_engine.parse_restart)>0);
  if (machine.evaluate_engine.stayonline && separator.equals("")) separator=";";
  if (!separator.equals(";") && !separator.equals(",")) {
    if (verbose  ) { System.out.printf("\n"); }
    machine.printnewline();
  }
  ReadColon(); // check
  return true;
}
boolean ProcessPRINThashstatement() throws BasicException
{
  boolean firstexp=true;
  if (verbose) { System.out.printf("Processing PRINT statement\n"); }
  ReadExpression();
  if (verbose) { System.out.printf("MachinePrintEvaluate( %s )\n",keepExpression); }
  /// if (verbose  ) { System.out.printf("%s",machine.evaluate_partial(keepExpression).print()); }
  /// machine.print(machine.evaluate_partial(keepExpression).print());
  // this should probably be nicer
  int x=0;
  String separator="";
  /// while ((x=machine.evaluate_engine.parse_restart)>0) { 
  do {
    if (verbose) { System.out.printf("More to evaluate!!!! - should resubmit with the remaining string after %d...\n",machine.evaluate_engine.parse_restart); }
    // there is more to evaluate
    String temp=new String(keepExpression);  // do I need to do this?
    keepExpression=temp.substring(x,temp.length());

    x=0; // now we are at the start of it again
    while (x<keepExpression.length() && 
      (keepExpression.substring(x,x+1).equals(";") || keepExpression.substring(x,x+1).equals(",") || keepExpression.substring(x,x+1).equals(" ")))
      { 
        if (keepExpression.substring(x,x+1).equals(",")) {
          //if (verbose) { System.out.printf("Should space out to next position....(Not impl yet)\n"); }
          separator=","; 
          if (!firstexp) machine.PrintFile(" "); // for now just a single space
          else firstexp=false;
        } else if (keepExpression.substring(x,x+1).equals(";")) {
          separator=";"; 
        }
        x++;
      } // chew them up // need to include blanks and , too!
    /// String
    temp=new String(keepExpression);  // do I need to do this?
    keepExpression=temp.substring(x,temp.length());
    if (keepExpression.equals("")) { break; }

    if (verbose) { System.out.printf("should resubmit with the remaining string after %d will do so with:%s\n",machine.evaluate_engine.parse_restart,keepExpression); }
    if (firstexp) {
          // if this is the first time here -> we will have the fh first
     
      GenericType gt=machine.evaluate_partial(keepExpression);
       //   if (gt.gttop==1) {
      if (verbose) System.out.printf("Hash value = %d\n",//(int)gt.num());
                (int)gt.num());
      machine.SetFH((int)gt.num());
      //firstexp=false;
    } else {
      machine.PrintFile(machine.evaluate_partial(keepExpression).print());
    }
    separator="";
  } while ((x=machine.evaluate_engine.parse_restart)>0);
  if (machine.evaluate_engine.stayonline && separator.equals("")) separator=";";
  if (!separator.equals(";") && !separator.equals(",")) {
    if (verbose  ) { System.out.printf("\n"); }
    machine.PrintFile("\n");
    machine.PrintFileFlush();
  }
  ReadColon(); // check
  return true;
}

boolean ProcessIFstatement() throws BasicException
{
  ReadExpression();
  
  if (verbose) { System.out.printf("MachineEvaluate( %s )\n",keepExpression); }
  //ReadStatementToken(); // note MUST be THEN or GOTO
  // implicetly read already with the ReadExpression
  if (gotToken!=ST_GOTO && gotToken!=ST_THEN) {
    System.out.printf("?SYNTAX ERROR 104: did not get THEN or GOTO token\n");
    throw new BasicException("SYNTAX ERROR : NO THEN OR GOTO");
    //return false;
  }
  // IF the evaluated expression is true (non zero), continue...
  // otherwise read out rest of line and skip to new line
  //if (machine.evaluate(keepExpression)==0.0) { // num only returns a num
  GenericType gt=machine.evaluate(keepExpression); // so that verbose works
  if (verbose) { System.out.printf("  evaluates to %s\n",gt.print()); }
  if (gt.equals(0.0)) { // num only returns a num
    // read everthing to the end of line
    while (pnt<linelength && !line.substring(pnt,pnt+1).equals("\n")) {
      pnt++;
    }
    return true; // parsed okay
  }
  SkipSpaces(); // really - spaces arent good for anything
  // it might be just a line # - try and read it - if it isn't keep going  
  if (ReadLineNo()) {
    if (machine.enabledmovement) {
      machine.gotoLine(keepLine);
      pnt=machine.executionpoint; // we should now have a different execution point
    }
    // then read out till end of line
    //while (pnt<linelength && !line.substring(pnt,pnt+1).equals("\n")) {
      //pnt++;
    //}
  }
  // if it is a GOTO - just goto it anyway - it must be a label
  else if (gotToken==ST_GOTO) {
    //SkipSpaces();
    ReadExpression();
    machine.gotoLine(keepExpression);
    pnt=machine.executionpoint; // we should now have a different execution point
  }
  return true;
}

boolean ProcessONstatement() throws BasicException
{
  ReadExpression();
  
  if (verbose) { System.out.printf("MachineEvaluate( %s )\n",keepExpression); }
  if (verbose) { System.out.printf("  evaluates to %s\n",machine.evaluate(keepExpression).print()); }
  if (gotToken!=ST_GOTO && gotToken!=ST_GOSUB) {
    System.out.printf("?SYNTAX ERROR ON WITHOUT GOTO OR GOSUB\n");
    throw new BasicException("SYNTAX ERROR ON WITHOUT GOTO OR GOSUB");
  }
  int offset=(int)machine.evaluate(keepExpression).num();
  SkipSpaces(); // really - spaces arent good for anything
  // should contain comma sep array
  int c=0;
  while (ReadLineNo()) {
    c=c+1;
    if (verbose) { System.out.printf("Got line=%s\n",keepLine); }
    if (c==offset) { // this is the one
       // do the gosub or goto
      if (verbose) { System.out.printf("We need to jump to %d %s\n",c,keepLine); }
      if (gotToken==ST_GOTO) {
        machine.gotoLine(keepLine);
        pnt=machine.executionpoint; // we should now have a different execution point
        return true;
      } else {
        // if we gosub - move to end of line
        IgnoreRestofStatement();
        machine.gosubLine(keepLine,pnt);
        if (verbose) { System.out.printf("gosub, moving to executionpoint %d\n",machine.executionpoint); }
        pnt=machine.executionpoint; // we should now have a different execution point
        return true;
      }
    }
    SkipSpaces(); // really - spaces arent good for anything
    // read out comma
    if (pnt<linelength && line.substring(pnt,pnt+1).equals(",")) pnt++;
    SkipSpaces(); // really - spaces arent good for anything
  }
  // evaluate it...

    //if (machine.enabledmovement) {
      //machine.gotoLine(keepLine);
      //pnt=machine.executionpoint; // we should now have a different execution point
    //}

  // just ignore and keep going
  return true;
}

boolean ProcessGOTOstatement() throws BasicException
{
  SkipSpaces(); // added this in as there may be leading spaces (probably a better way to do this)
  ReadExpression(); // not really, should just be a numberic??? maybe an expression is good
  // lets go!
  // only with this line will we not do a front to back parse
  if (machine.enabledmovement) {
    if (machine.program_running) {
      machine.gotoLine(keepExpression);
      if (verbose) { System.out.printf("goto, moving to executionpoint %d\n",machine.executionpoint); }
      pnt=machine.executionpoint; // we should now have a different execution point
    } else {
      //BasicCONTrestart
      if (verbose) System.out.printf("Got a GOTO in direct mode\n");
      throw new BasicCONTrestart("Just restart the program and continue it","GOTO",keepExpression);
    }
  }
  ReadColon();
  return true;
}

boolean ProcessGOSUBstatement() throws BasicException
{
  SkipSpaces(); // added this in as there may be leading spaces (probably a better way to do this)
  ReadExpression(); // not really, should just be a numberic??? maybe an expression is good
  if (machine.enabledmovement) {
    if (machine.program_running) {
      machine.gosubLine(keepExpression,pnt);
      if (verbose) { System.out.printf("gosub, moving to executionpoint %d\n",machine.executionpoint); }
      pnt=machine.executionpoint; // we should now have a different execution point
    } else {
      throw new BasicException("GOSUB NOT IMPLEMENTED IN DIRECT MODE");
      //nodoesntwork//lets try this
      //nodoesntwork//machine.gosubLine(keepExpression,pnt);
      //nodoesntworkint savepnt=pnt;
      //nodoesntworkmachine.gotoLine(keepExpression);
      //nodoesntworkmachine.contProgram(keepExpression);
      //nodoesntworkpnt=savepnt;
    }
  }
  ReadColon();
  return true;
}

boolean ProcessRETURNstatement() throws BasicException
{
  if (machine.enabledmovement) {
    machine.popReturn();
    pnt=machine.executionpoint; // we should now have a different execution point
  }
  ReadColon(); // because we may have been left with a colon
  //IgnoreRestofLine(); // no this is wrong, we are back were we came from here!
  return true;
}

boolean ProcessNEXTstatement() throws BasicException
{
  keepExpression=""; // in case it is nothing!
  ReadExpression(); // not really, should just be a variable!!
  // split up between commas
  int at=0; 
  int start=0;
  while (at<keepExpression.length()) {
    if (keepExpression.substring(at,at+1).equals(",")) {
      // skip spaces with trim!
      if (machine.processNEXT(pnt,keepExpression.substring(start,at).toLowerCase().trim())) {
        // we did loop
        pnt=machine.executionpoint;
        return true; // we skip the following ones
      }
      start=at+1;
    }
    at++;
  }
  // skip spaces with trim!
  if (machine.processNEXT(pnt,keepExpression.substring(start,keepExpression.length()).toLowerCase().trim())) {
    // we did loop
    pnt=machine.executionpoint;
    return true; // we skip the following ones
  }
  if (verbose) { System.out.printf("Finished the next loops\n"); }
  ReadColon();
  return true;
}

boolean ProcessFORstatement() throws BasicException
{
  String forto;
  String forstep="1";
  if (verbose) { System.out.printf("Processing FOR statement\n"); }
  ReadAssignment();
  ReadExpression();

  if (keepVariable==null || keepVariable.equals("")) {
    System.out.printf("?SYNTAX ERROR : NO VARIABLE\n");
    throw new BasicException("SYNTAX ERROR : NO VARIABLE");
  }
  // trim spaces
  keepVariable=keepVariable.trim();
  if (verbose) { System.out.printf("MachineVariableSet(variable=%s with evaluate( %s ))\n",keepVariable,keepExpression); }
  // parse the keep variable in the machine to turn X(I+1) into X(42)
  machine.assignment(keepVariable.toLowerCase()+"="+keepExpression);
  //machine.setvariable(machine.parse(keepVariable),machine.evaluate(keepExpression));

  if (verbose) { System.out.printf("MachineVariableSet(variable=%s with evaluate( %s ))\n",keepVariable,keepExpression); }
  ReadStatementToken(); // note MUST be TO
  if (gotToken!=ST_TO) {
    System.out.printf("?SYNTAX ERROR 104: did not get TO token\n");
    throw new BasicException("SYNTAX ERROR : NO TO");
    //return false;
  }
  ReadExpression();
  forto=keepExpression;
  if (verbose) { System.out.printf("NEXT: to string = %s\n",forto); }
  if (!ReadColon() && pnt<linelength && !line.substring(pnt,pnt+1).equals("\n")) {
    ReadStatementToken(); // note CAN be STEP
    if (gotToken==ST_STEP) {
      ReadExpression();
      forstep=keepExpression;
      // do we need to read the colon?
      /// - no dont return -- all is okay !! - return true;
    } else {
      System.out.printf("?SYNTAX ERROR 105: did not get STEP token\n");
      throw new BasicException("SYNTAX ERROR : NOT STEP");
      //return false;
    }
  } else {
    // no STEP keyword
  }
  // MachineProcessFOR...
  // should to be an expression or a evaluated number?
  //machine.processFOR(/*position*/pnt,/*variable*/..,/*to*/..,/*step*/..);
  ReadColon(); // check
  machine.createFORloop(pnt, keepVariable.toLowerCase(), machine.evaluate(forto).num(), machine.evaluate(forstep).num());
  return true;
}

void IgnoreRestofLine()
{
  // read everthing to the end of line
  while (pnt<linelength && !line.substring(pnt,pnt+1).equals("\n")) {
    pnt++;
  }
  return;
}
void IgnoreRestofStatement()
{
  // read everthing to the end of line
  while (pnt<linelength && !line.substring(pnt,pnt+1).equals("\n") && !line.substring(pnt,pnt+1).equals(":")) {
    pnt++;
  }
  return;
}

boolean ProcessCONTstatement() throws BasicException
{
  // again CONT is wierd because it transfers from immediate mode
  // to run (continue running an old program
  throw new BasicCONTrestart("Just restart the program and continue it");
}

boolean ProcessRUNstatement() throws BasicException
{
  // now see if there is a line#
  SkipSpaces(); // added this in as there may be leading spaces (probably a better way to do this)
  ReadExpression(); // not really, should just be a numberic??? maybe an expression is good
  // IgnoreRestofLine();

  if (verbose) System.out.printf("got a line RUN \"%s\"\n",keepExpression);
  // this is a funny one - we effectively restart the processing, but with the program!
    //machine.variables_clr();
    //machine.runProgram(); // we now execute the statements upon a machine
  throw new BasicRUNrestart("Just restart the program and rerun it",keepExpression);
  //return true;
}

boolean ProcessREMstatement() 
{
  IgnoreRestofLine();
  return true;
}

int getlineparse(String lineno) {
  int ret;
  try {
    ret=Integer.parseInt(lineno);
  } catch (NumberFormatException e) {
     ret = -1;
  }
  return ret;
}

boolean ProcessLISTstatement() throws BasicException
{
  int from,to;
  from=-1; to=-1;
  ReadExpression();
  if (verbose) { System.out.printf("wanting to list lines: %s\n",keepExpression); }
  if (keepExpression.startsWith("\"")) {
    // allow to list "xx" a file
    GenericType gt=machine.evaluate(keepExpression);
    machine.listDIR(gt.str(),keepExpression.startsWith("-"));
    return true;
  }
  /* split to from to */
  /* nothing:   -1 -1 */
  /* from-:     99 -1 */
  /* -to:       -1 99 */
  /* num        99 99 */
  String lineno[] = (" "+keepExpression+" ").split("-");
if (verbose)  for (int i=0; i<lineno.length; ++i)
    System.out.printf("LIST VAR %d = \"%s\"\n",i,lineno[i]);
  if (lineno.length>=1) from=getlineparse(lineno[0].trim());
  if (lineno.length>=2) to=getlineparse(lineno[1].trim());
  if (lineno.length==1 && from==-1) { to=-1; }
  else if (lineno.length==1) { to=from; }
  
  //machine.print(machine.listProgram(from,to)); // just prints the program text
  machine.listProgram(from,to); // just prints the program text
  return true;
}

boolean ProcessGETstatement() throws BasicException
{
  ReadExpression();
  if (verbose) { System.out.printf("getting to %s\n",keepExpression); }
  String got=machine.getkey();
  if (verbose) { System.out.printf("got string \"%s\"\n",got); }
  // it a string so keep it quoted
  //machine.assignment(keepExpression.toLowerCase()+"="+"\""+got.toLowerCase()+"\"");
//  else machine.assignment(keepExpression.toLowerCase()+"="+"chr$("+(int)got.charAt(0)+")");
  //machine.setvariable(machine.parse(keepExpression),machine.evaluate("\""+got.toLowerCase()+"\""));
  if (got=="" || got==null) machine.assignment(keepExpression.toLowerCase()+"="+"\""+"\"");
  else machine.assignment(keepExpression.toLowerCase()+"="+"\""+got+"\"");  // try this now
  return true;
}

boolean ProcessGEThashstatement() throws BasicException
{
  ReadExpression();
  if (verbose) { System.out.printf("getting to %s\n",keepExpression); }
  String got=machine.getkey();
  if (verbose) { System.out.printf("got string \"%s\"\n",got); }
  // it a string so keep it quoted
  //machine.assignment(keepExpression.toLowerCase()+"="+"\""+got.toLowerCase()+"\"");
  //machine.setvariable(machine.parse(keepExpression),machine.evaluate("\""+got.toLowerCase()+"\""));
  if (got=="" || got==null) machine.assignment(keepExpression.toLowerCase()+"="+"\""+"\"");
  else machine.assignment(keepExpression.toLowerCase()+"="+"\""+got+"\"");  // try this now
  return true;
}

boolean ProcessREADstatement() throws BasicException
{
  // here we use the special feature of evaluate
  ReadExpression();
  if (verbose) { System.out.printf("inputting to %s\n",keepExpression); }
  machine.assignment(keepExpression.toLowerCase()+"=metaread(1)");
  if (verbose) { machine.dumpstate(); }
  return true;
}

// maybe allow assignments like this
// A$,B$,C$="X","Y","Z"
// can be used for INPUT and READ
boolean ProcessINPUTstatement(boolean stayonsameline) throws BasicException
{
  int fh=-1;
  ReadExpression();
  // simplistic addition to allow string part of INPUT ["string";] x$, y$, z$...
  // search for semicolon, trim off and print
  //contains
  // read partial, see if ends in semicolon???
  if (stayonsameline) {
    // it must be a input#
    // read out fh
    GenericType gt=machine.evaluate_partial(keepExpression);
    int x=machine.evaluate_engine.parse_restart;
    if(x>=0 && x<keepExpression.length()-1 && keepExpression.substring(x,x+1).equals(",")) {
      // all good
      fh=(int)gt.num();
      if (verbose) { System.out.printf("fh=%d\n",fh); }
      String temp=keepExpression;
      keepExpression=temp.substring(x+1,temp.length());
    } else {
      throw new BasicException("SYNTAX ERROR NEEDS FH & COMMA");
    }
  }
  if (keepExpression.contains(";")) {
        // check if string AND semicolon is next
    String prompt=machine.evaluate_partial(keepExpression).print();
    int x=machine.evaluate_engine.parse_restart;
      // trim spaces not required
    if(x>=0 && x<keepExpression.length()-1 && keepExpression.substring(x,x+1).equals(";") &&
      keepExpression.substring(0,x-1).contains("\"")) {
        // print 
        if (verbose) { System.out.printf("Got a input with prompt string\n"); }
        machine.print(prompt);
        x=x+1;
        String temp=new String(keepExpression);  // do I need to do this?
        keepExpression=temp.substring(x,temp.length());
    }  else 
      /// / or anything else for that mattter, is a syntax error
      throw new BasicException("SYNTAX ERROR NEEDS LITERAL");
  } else if (keepExpression.contains("\"")) {
    throw new BasicException("SYNTAX ERROR NEEDS ;");
  }

  if (verbose) { System.out.printf("inputting to %s\n",keepExpression); }
  if (!stayonsameline) machine.print("? ");
  String got;
  if (fh!=1 && fh>=0) { // just for now - say fh=1 is keyboad! later we read this from device
    if (verbose) { System.out.printf("we are reading a fh=%d\n",fh); }
    machine.SetFH(fh); // this now becomes more critical, as the stream reader depends on this being set
    /* old way got=machine.InputFile(fh); */
    machine.assignment(keepExpression.toLowerCase()+"=metainput(1)");
    return true;
    //if (got==null) { got=""; }
  } else {
    got=machine.getline();
    if (stayonsameline) {
      machine.print("(up)");
    }
  }
  if (verbose) { System.out.printf("got string \"%s\"\n",got.trim()); }
  // it a string so keep it quoted
  // still need to separate out the answer to multiple strings!
  // divide up by commas and separate out like this:
  // TEST,SECOND
  // "TEST","SECOND"
  // change all commas to \",\"
  if (got != null) {
    String processedString = stringQuoteStuff(got.trim().toLowerCase());
    if (verbose) { System.out.printf("assigning to %s\n",keepExpression.toLowerCase()); }
    machine.assignment_dt(keepExpression.toLowerCase()+"="+processedString);
    if (verbose) { machine.dumpstate(); }
  }
  return true;
}

// fred,jim,sam  ->  "fred","jim","sam"
// but
// "fred,jim",sam must give "fred,jim","sam" 
// also eat up any existing quotes
String stringQuoteStuff(String arg)
{
  //take a string and return it as is appart from , surrounded by \",\" and one single \" before and after the string
  String building="";
  boolean quoted=false;
  for (int p=0; p<arg.length(); ++p) { 
    String a=arg.substring(p,p+1);
    if (a.equals("\"")) {
      quoted=!quoted;
      // just chew up the quote!
    } else if (!quoted && a.equals(",")) { // technically should include ":" and "," and even CR13
      building+="\",\"";
    } else { building+=a; }
  }
  return "\""+building+"\"";
}

void verboseFull()
{
  verbose=true;
  machine.verbose=verbose;
  machine.evaluate_engine.verbose=verbose;
}
void verboseOff()
{
  verbose=false;
  machine.verbose=verbose;
  machine.evaluate_engine.verbose=verbose;
}

boolean ProcessSAVEstatement() throws BasicException
{
  ReadExpression();
  if (verbose) { machine.dumpstate(); }
  String filename;
  if (keepExpression==null || keepExpression.equals("") || machine.evaluate(keepExpression).str()==null) {
    /* save the existing program previously mentioned */
    if (machine.program_name=="" || machine.evaluate(keepExpression).str()==null) {
      throw new BasicException("MISSING FILENAME");
    } else {
      filename=machine.program_name;
    }
  } else {
    filename=machine.evaluate(keepExpression).str().toLowerCase();
  }

  if (verbose) { machine.print("saving "+filename.toLowerCase()+"..."); }
  if (!filename.matches(".*\\.basic")) {
    filename=filename.toLowerCase()+".basic";
  } else {
  }
  
  machine.saveProgram(filename);
  return true;
}
//boolean ProcessPRINThashstatementTest() throws BasicException
//{
  //ReadExpression();
  //if (verbose) { machine.dumpstate(); }
  //GenericType gt=machine.evaluate(keepExpression);
  //if (gt.gttop==2) {
     //machine.PrintFile(
       //(int)gt.gtlist[0].num(),
       //gt.gtlist[1].str()
     //);
     //return true;
  //} else {
     //return false;
  //}
//}
boolean ProcessOPENstatement() throws BasicException
{
  ReadExpression();
  if (verbose) { machine.dumpstate(); }
  GenericType gt=machine.evaluate(keepExpression);
  if (gt.gttop==4) {
     machine.OpenFile(
       (int)gt.gtlist[0].num(),
       (int)gt.gtlist[1].num(),
       gt.gtlist[3].str()
     );
     return true;
  } else if (gt.gttop==2) {
     machine.OpenFile(
       (int)gt.gtlist[0].num(),
       (int)gt.gtlist[1].num(),
       "KB"
     );
     return true;
  } else {
     return false;
  }
}
boolean ProcessCLOSEstatement() throws BasicException
{
  ReadExpression();
  GenericType gt=machine.evaluate(keepExpression);
  machine.CloseFile( (int)gt.num());
  return true;
}

boolean ProcessLOADstatement() throws BasicException
{
  // this will be a bit wierd if actually run from a program itself!
  ReadExpression();
  if (verbose) { machine.dumpstate(); }
  if (keepExpression==null || keepExpression.equals("") || machine.evaluate(keepExpression).str()==null) {
    throw new BasicException("MISSING FILENAME");
  }
  String filename=machine.evaluate(keepExpression).str();
  // only add basic if it doesnt have it already
  // consider using fileUnalias
  String newfile=machine.fileUnalias(filename);
  if (!newfile.equals(filename)) {
    machine.print("\n");
    machine.print("searching for "+filename.toLowerCase()+"\n");
    filename=newfile;

//  if (filename.equals("%")) {
//    machine.print("\n");
//    machine.print("searching for "+filename.toLowerCase()+"\n");
//    filename=filename.replaceFirst("%",machine.cloudNet+"/basic/dir.php");
//  } else if (filename.equals("*")) {
//    machine.print("\n");
//    machine.print("searching for "+filename.toLowerCase()+"\n");
//    filename=filename.replaceFirst("\\*",machine.cloudNet+"/basic/dir.php");
//  } else if (filename.startsWith("%")) {
//    machine.print("\n");
//    machine.print("searching for "+filename.toLowerCase()+"\n");
//    filename=filename.replaceFirst("%",machine.cloudNet+"/cloud/c64x");
//    filename=filename+".basic.txt";
  } else
  if (filename.matches(".*\\.au")
    || filename.matches(".*\\.wav")
    || filename.matches(".*\\....") // bmp jpg txt etc
    || filename.matches(".*\\.....") // jpeg
    ) {
    //filename=filename.toLowerCase();
  } else {
    if (filename.startsWith("$$")) {
      filename=filename.replaceFirst("\\$",":");
    }
    machine.print("\n");
    machine.print("searching for "+filename.toLowerCase()+"\n");
    //machine.print("loading "+filename.toLowerCase()+"...");
    if (filename.startsWith(":")) {
      filename=filename.replaceFirst(":",machine.cloudNet+"/basic/");
    }
    if (filename.contains("http")) {
      //filename=filename.toLowerCase()+".txt";
      filename=filename+".txt";
    } else if (!filename.matches(".*\\.basic")) {
      //filename=filename.toLowerCase()+".basic";
      filename=filename+".basic";
    }
  }
  machine.loadProgram(filename,true);
  return true;
}
boolean ProcessMETACHARSETstatement() throws BasicException
{
  ReadExpression();
  if (verbose) { machine.dumpstate(); }
  machine.machinescreen.changeCharSet((int)machine.evaluate(keepExpression).num());
  return true;
}

boolean ProcessMETASCALEstatement() throws BasicException
{
  ReadExpression();
  if (verbose) { machine.dumpstate(); }
  machine.machinescreen.setScale((int)machine.evaluate(keepExpression).num());
  return true;
}

boolean ProcessMETASCALEYstatement() throws BasicException
{
  ReadExpression();
  if (verbose) { machine.dumpstate(); }
  machine.machinescreen.setScaleY((int)machine.evaluate(keepExpression).num());
  return true;
}

boolean ProcessMETAROWSstatement() throws BasicException
{
  ReadExpression();
  if (verbose) { machine.dumpstate(); }
  machine.machinescreen.setRows((int)machine.evaluate(keepExpression).num());
  return true;
}

boolean ProcessMETACOLSstatement() throws BasicException
{
  ReadExpression();
  if (verbose) { machine.dumpstate(); }
  machine.machinescreen.setCols((int)machine.evaluate(keepExpression).num());
  return true;
}

boolean ProcessMETABGTRANSstatement() throws BasicException
{
  int p;
  ReadExpression();
  if (verbose) { machine.dumpstate(); }
  p=(int)(machine.evaluate(keepExpression).num());
  if (p==2) machine.machinescreen.shiftbgimage=false;
  machine.machinescreen.setBackgroundTransparent(
    (p>0) ?true:false);
  return true;
}

boolean ProcessPOKEstatement() throws BasicException
{
  ReadExpression();
  if (verbose) { System.out.printf("inputting to %s\n",keepExpression); }
  //verboseFull();
  // we should have num,num evaluate into a list
  if (verbose) { System.out.printf("inputting to %s\n",machine.evaluate(keepExpression).print()); }
  if (verbose) { machine.dumpstate(); }

  if (machine.graphicsDevice!=null) {
    GenericType gt=machine.evaluate(keepExpression);
    int memloc;
    int memval;
    if (gt.gttop==2) {
      memloc=(int)gt.gtlist[0].num();
      memval=(int)gt.gtlist[1].num();
      if (memloc==1024) {
        machine.graphicsDevice.command_POKE(memloc,memval);
        return true;
      }
    }
  }

  machine.performPOKE(machine.evaluate(keepExpression)); // we assume it is a list of two numbers
  //verboseOff();
  return true;
}

boolean ProcessSYSstatement() throws BasicException
{
  ReadExpression();
  if (verbose) { System.out.printf("inputting to %s\n",keepExpression); }
  if (verbose) { System.out.printf("inputting to %s\n",machine.evaluate(keepExpression).print()); }
  if (verbose) { machine.dumpstate(); }
  if (!keepExpression.equals("")) {
    machine.performSYS(machine.evaluate(keepExpression));
    machine.machinescreen.startupscreen();
    return true;
  } else {
    throw new BasicException("TOO FEW PARAMETERS");
  }
}

boolean ProcessIGNOREstatement() 
{
  // read everthing to the colon (should ignore embedded : in a string -b ut doesn;t yet
  boolean quote=false;
  while (pnt<linelength && !line.substring(pnt,pnt+1).equals("\n")
    && !(line.substring(pnt,pnt+1).equals(":")&&!quote)) {
    if (line.substring(pnt,pnt+1).equals("\"")) {
      quote=!quote;
    }
    pnt++;
  }
  return true;
}

char metaCode=' ';

boolean checkMetaCode()
{
  // we assume that we have a pointer at pnt, which we wont move
  // and we have just read a bracket
  int at=1; // we already know the first one is a ( or [
  while (pnt+at<linelength) {
    String a=line.substring(pnt+at,pnt+at+1);
    if (a.equals("\n") || a.equals("\"")) return false; // quoted string ends or new line
    if (a.equals(")") || a.equals("]")) {
      // we have the seemingly end of a code
      // from pnt to (before)pnt+at
      String metatest=line.substring(pnt+1,pnt+at); // skip the first one
      if (verbose) { System.out.printf("Testing for meta code %s\n",metatest); }
   
      if (metatest.equals("home")) metaCode=(char)(19);
      else if (metatest.equals("up")) metaCode=(char)(145);
      else if (metatest.equals("down")) metaCode=(char)(17);
      else if (metatest.equals("left")) metaCode=(char)(157);
      else if (metatest.equals("rght")) metaCode=(char)(29);
      else if (metatest.equals("clr")) metaCode=(char)(147);
      else if (metatest.equals("rvon")) metaCode=(char)(18);
      else if (metatest.equals("rvof")) metaCode=(char)(146);
      else if (metatest.equals("SHIFT-SPACE")) metaCode=(char)(160);

      else if (metatest.equals("blk")) metaCode=(char)(144);
      else if (metatest.equals("wht")) metaCode=(char)(5);
      else if (metatest.equals("red")) metaCode=(char)(28);
      else if (metatest.equals("cyn")) metaCode=(char)(159);
      else if (metatest.equals("pur")) metaCode=(char)(156);
      else if (metatest.equals("grn")) metaCode=(char)(30);
      else if (metatest.equals("blu")) metaCode=(char)(31);
      else if (metatest.equals("yel")) metaCode=(char)(158);
      else if (metatest.equals("orng")) metaCode=(char)(129);
      else if (metatest.equals("brn")) metaCode=(char)(149);
      else if (metatest.equals("lred")) metaCode=(char)(150);
      else if (metatest.equals("gry1")) metaCode=(char)(151);
      else if (metatest.equals("gry2")) metaCode=(char)(152);
      else if (metatest.equals("lgrn")) metaCode=(char)(153);
      else if (metatest.equals("lblu")) metaCode=(char)(154);
      else if (metatest.equals("gry3")) metaCode=(char)(155);
      else return false;
      pnt+=at+1;
      return true; // if any matched
    }
    at++;
  }
  return false;
}

boolean ReadExpression()
{
    int start=pnt;
    int previous;
    previous=pnt;
    boolean quoted=false;
    keepExpression=""; // because we are going to build it up
//if (true) { start_timing(TIME_ReadExpression); }
    while (pnt<linelength) {
      String a=line.substring(pnt,pnt+1);
      previous=pnt;
      if (a.equals("\n")) { break; }
      if (!quoted && a.equals(":")) { break; }
      // add a bit to to change the meta codes e.g. (home) to 1 char
      if (quoted && (a.equals("(") || a.equals("["))) {
        // we may have a meta code, lets check it out
        if (checkMetaCode()) {
          // the code is set in a global metaCode
          if (verbose) { System.out.printf("Got a metacode match\n"); }
          keepExpression=keepExpression+line.substring(start,previous)+metaCode;
          start=pnt; // resetting it
          continue;
        }
      }
      if (!quoted // no difference && a.compareToIgnoreCase("a")>=0 && a.compareTo("z")<=0
        && ReadStatementToken()) { break; }
      if (a.equals("\"")) { quoted=!quoted; }
      pnt++;
    }
  if (pnt==linelength) previous=pnt; // special case when we read to end of line
  if (verbose) { System.out.printf("Interpreting \"%s\" as an expression\n",line.substring(start,previous)); }
  //keepExpression=line.substring(start,previous);
  keepExpression=keepExpression+line.substring(start,previous);
// if (true) { end_timing(TIME_ReadExpression); }
  return true;
}

boolean ReadAssignment()
{
    int at=0;

    // up here - should we get rid of spaces?
    // yes - chew up spaces - to make for loops keep correct variable
    while (pnt+at<linelength) {
      String a=line.substring(pnt+at,pnt+at+1);
      if (!a.equals(" ")) { break; }
      pnt++; //! note - moving pnt along!
    }

    int bracketcount=0;
    boolean isarray=false;
    int start=pnt;
    boolean quoted=false;
    while (pnt+at<linelength) {
      
      String a=line.substring(pnt+at,pnt+at+1);
      if (a.equals("(")) { 
        if (!isarray && bracketcount==0) {
          // okay, we mark the start here
          start=pnt+at+1;
          isarray=true; 
        }
        bracketcount++; 
      } else if (a.equals(")")) { 
        bracketcount--; 
        // okay, we have an array
      } else if (a.equals("\"")) {
        quoted=!quoted;
      } else if (!quoted && a.equals(":")) { // end of statement
        // bring the colon too?
        // but we didn't find it so return false
        //at++;
        //break;
        keepVariable="";
        return false;
      } else if (a.equals("=")) {
        // should only allow variables
        // if (at==0) start allowed
        // else internal allowed
        break;
      } else if (a.equals("\n")) {
        // we got to the end of the line - i think this will fix the syntax error miss??
        keepVariable="";
        return false;
      }
      at++;
    }
    if (pnt+at<linelength) {
      // we have got variable =
      if (verbose) { System.out.printf("Found variable %s\n",line.substring(pnt,pnt+at)); }
      keepVariable=line.substring(pnt,pnt+at); // keep it
      pnt+=at+1; // read out the variable and the "=" sign
      return true;
    } else {
      return false;
    }
}

    // for now will read a line number that has a space after it - 
    // will change this to allow no space in the future

boolean ReadLineNo() {
    int at=0;
    int chewspace=0;
    //System.out.printf("Trying to read line number - looking at [%d] %s\n",pnt,line.substring(pnt+at,pnt+at+1));
    while (pnt+at<linelength) {
      String a=line.substring(pnt+at,pnt+at+1);
      if (a.equals("\n")) {
		// bug fix
		if (at==0) {
            if (verbose) { System.out.printf("Empty line\n"); }
			return false;
		}
        // dont read this
        if (verbose) { System.out.printf("Line number %s\n",line.substring(pnt,pnt+at)); }
        keepLine=line.substring(pnt,pnt+at);
        pnt+=at;
        return true;
      }
      if (a.equals(" ")) {
        chewspace=1;
        break; // hopefully got some part of a number
      }

      /* for now - break when not a space! */
      if (!(a.compareTo("0")>=0 && a.compareTo("9")<=0)) {
        break;
      }      
      //if (!(a.compareTo("0")>=0 && a.compareTo("9")<=0)) {
      //  keepLine="";
      //  return false; // not a number
      //}
      at++;
    }
    //if (pnt+at<linelength && at>0) {  // no, not now, as we may be reading a ...THEN100 line
    if (at>0) {
      // we have got number
      if (verbose) { System.out.printf("Line number %s\n",line.substring(pnt,pnt+at)); }
      keepLine=line.substring(pnt,pnt+at);
      pnt+=at+chewspace;
      // add one if we ended in space
      return true;
    } else {
      keepLine="";
      return false;
    }

}

boolean SkipNewLines() {
  while (pnt<linelength && line.substring(pnt,pnt+1).equals("\n")) {
    pnt++;
  }
  return true;
}
boolean SkipSpaces() {
  while (pnt<linelength && line.substring(pnt,pnt+1).equals(" ")) {
    pnt++;
  }
  return true;
}
boolean ReadColon() {
  if (pnt<linelength && line.substring(pnt,pnt+1).equals(":")) {
    pnt++;
    return true;
  } else {
    return false;
  }
}

boolean MachineEND() {
  pnt=line.length();
  return true;
}


}

/////////
// END //
/////////
