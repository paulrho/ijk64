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
  //boolean verbose=true;

  String read_a_file() {
    return read_a_file("test.basic");
  }

  String read_a_file(String filename) {

    String content="";

    try {
      FileInputStream fis = new FileInputStream(filename);
      int x= fis.available();
      byte b[]= new byte[x];
      fis.read(b);
      content = new String(b);
      //System.out.println(content);
    } catch (Exception e) { }

    return content;
  }

  statements(String interpstring, Machine themachine)
  {
    machine=themachine;
    interpret_string(interpstring);
  }

  statements(String[] args, Machine themachine)
  {
    machine=themachine;
    boolean has_parameter=false;
    for (int i=0; i<args.length; ++i) {
      if (args[i].substring(0,1).equals("-")) {
        if (verbose) { System.out.printf("Got a switch\n"); }

        if (args[i].substring(0,2).equals("-v")) {
          //evaluate_engine.verbose=true;
        } else if (args[i].substring(0,2).equals("-q")) {
          //evaluate_engine.verbose=false;
        } else if (args[i].substring(0,2).equals("-t")) {
          //do_many=true; // timing test
        }
      } else {
        // parameter // do it immediately
        has_parameter=true;
        interpret_string( read_a_file(args[i]));
      }
    }
    // at the moment, spaces are passed onto the "evaluate interpreter" that's okay
    //interpret_string("FORI=1TO1000:PRINTI:NEXT");
    if (!has_parameter) { interpret_string( read_a_file() ); }

  } // end func

int MAXTOKENS=100;

String[] basicTokens={"FOR","TO","STEP","NEXT","IF","THEN","GOTO","GOSUB","RETURN","REM","PRINT","END","DIM","GET","POKE","OPEN","INPUT#1,","CLOSE","DATA","RUN","READ","RESTORE","INPUT","LIST","META-VERBOSE"};
static final int ST_FOR=0;
static final int ST_TO=1;
static final int ST_STEP=2;
static final int ST_NEXT=3;
static final int ST_IF=4;
static final int ST_THEN=5;
static final int ST_GOTO=6;
static final int ST_GOSUB=7;
static final int ST_RETURN=8;
static final int ST_REM=9;
static final int ST_PRINT=10;
static final int ST_END=11;
static final int ST_DIM=12;
static final int ST_GET=13;
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

String line;
int pnt;
int linelength;
int gotToken=0;

String keepVariable;
String keepExpression;
String keepLine;

Machine machine; // we now expect to be passed this from machine itself

void precache_all_lines()
{
  // read_all_lines
  // read the line# first
  pnt=0;
  while (true) {
    SkipSpaces();
    //System.out.printf("\n");
    int start=pnt;
    if (!ReadLineNo()) {
      if (verbose) { System.out.printf("No line # -finishing\n"); }
      break;
    }
    // got a line # cache it
    machine.cacheLine(keepLine,pnt); //was start
    // read out rest of line
    IgnoreRestofLine();

    if (pnt>=line.length()) {
      break;
    }
    SkipNewLines();
  }

}

//boolean newline; // this is a bit of a work around, when we change the execution point to a new line
//hmm, actually, changing mind, set the line cache to the first statement!

void interpret_string(String passed_line)
{
  line=passed_line;
  linelength=line.length();
  if (verbose) { System.out.printf("\n**************************************\n"); }
  //System.out.printf("Got line %s\n",line);

  // fix up code
  //try it without now // line=line.toUpperCase(); // convert to uppercase

  // nomore // // instansiate the machine
  // nomore // machine = new Machine();
  // nomore // ///machine.verbose=verbose;
  // nomore // machine.initialise_engines(); // silly, but there is a reason
  // nomore // //machine.verbose=verbose;

  // skip to the chase, and just read the line #s
  precache_all_lines();

  pnt=0;
  // read the line# first
  while (pnt<line.length()) {
    SkipSpaces();
    if (verbose) { System.out.printf("\n"); }
    if (!ReadLineNo()) {
      //if (verbose) { System.out.printf("No line # -finishing\n"); }
      //break;
      // what about this - execute that line IMMEDIATE mode
      if (verbose) { System.out.printf("No line # NOT finishing going direct\n"); }
    }
    // debugging
    if (false) {
      if (keepLine.equals("100")) {
        System.out.printf("Found line, switching on debugging\n");
        verbose=true;
        machine.verbose=true;
        machine.dumpstate();
      } else {
        //verbose=false;
      }
    }

    SkipSpaces();
    while(pnt<linelength) {
      SkipSpaces();
      if (!ReadStatement()) {
        System.out.printf("?SYNTAX ERROR 101: did not get statement\n");
        machine.print("?syntax error[CR]");
        // force it to exit
        pnt=linelength;
        break;
      }
      if (pnt>=line.length() || line.substring(pnt,pnt+1).equals("\n")) {
        break;
      }
    } // while everything
    if (pnt>=line.length()) {
      break;
    }
    SkipNewLines();
  }
  machine.dumpstate();

}

boolean ReadStatement() {
  // read statement should also read the colon at the end, ready for the next statement
  if (ReadStatementToken()) {
    switch(gotToken) {
      case ST_FOR: 
        if (ProcessFORstatement()) { return true; }
        break;
      case ST_NEXT: 
        if (ProcessNEXTstatement()) { return true; }
        break;
      case ST_IF: 
        if (ProcessIFstatement()) { return true; }
        break;
      case ST_GOTO: 
        if (ProcessGOTOstatement()) { return true; }
        break;
      case ST_GOSUB: 
        if (ProcessGOSUBstatement()) { return true; }
        break;
      case ST_RETURN: 
        if (ProcessRETURNstatement()) { return true; }
        break;
      case ST_PRINT: 
        if (ProcessPRINTstatement()) { return true; }
        break;
      case ST_REM: 
        if (ProcessREMstatement()) { return true; }
        break;
      case ST_RESTORE: 
        if (ProcessIGNOREstatement()) { return true; }
        break;
      case ST_READ: 
        if (ProcessIGNOREstatement()) { return true; }
        break;
      case ST_DATA: 
        if (ProcessIGNOREstatement()) { return true; }
        break;
      case ST_RUN: 
        if (ProcessREMstatement()) { return true; }
        break;
      case ST_DIM: 
        if (ProcessDIMstatement()) { return true; }
        break;
      case ST_GET:
        if (ProcessGETstatement()) { return true; }
        break;
      case ST_META_VERBOSE:
        verbose=true; machine.verbose=true;
        machine.evaluate_engine.verbose=true;
        return true;
      case ST_INPUT: case ST_INPUT1:
        if (ProcessINPUTstatement()) { return true; }
        break;
      case ST_POKE: case ST_OPEN: case ST_CLOSE:
        if (ProcessIGNOREstatement()) { return true; }
        break;
      case ST_END: 
        MachineEND();
        pnt=line.length();
        return true; // just END!
    }
    return false;
  } else {
    // could be a null statement
    if (ReadColon()) { return true; }

    // could be an assignment
    if (ReadAssignment()) {
      ReadExpression();
      if (verbose) { System.out.printf("MachineVariableSet(variable=%s with evaluate( %s ))\n",keepVariable,keepExpression); }
      // parse the keep variable in the machine to turn X(I+1) into X(42)
      machine.setvariable(machine.parse(keepVariable),machine.evaluate(keepExpression));
      ReadColon();
      return true;
    } else {
      System.out.printf("?SYNTAX ERROR 103: did not get token or assignment\n");
      return false;
    }
  }
}

boolean ReadStatementToken() {
  
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
      return true;
    }
  }
  // otherwise, search for an "=" sign, and take note of the string preceeding
  //ReadAssignment();

  return false;
}

boolean ProcessDIMstatement()
{
  if (verbose) { System.out.printf("Processing DIM statement\n"); }
  ReadExpression();
  if (verbose) { System.out.printf("Machine to DIM( %s )\n",keepExpression); }
  machine.evaluate(keepExpression); // throw away the result, we are just dimensioning the array
  ReadColon();
  return true;
}

boolean ProcessPRINTstatement()
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

boolean ProcessIFstatement()
{
  ReadExpression();
  
  if (verbose) { System.out.printf("MachineEvaluate( %s )\n",keepExpression); }
  if (verbose) { System.out.printf("  evaluates to %s\n",machine.evaluate(keepExpression).print()); }
  //ReadStatementToken(); // note MUST be THEN or GOTO
  // implicetly read already with the ReadExpression
  if (gotToken!=ST_GOTO && gotToken!=ST_THEN) {
    System.out.printf("?SYNTAX ERROR 104: did not get THEN or GOTOT token\n");
    return false;
  }
  // IF the evaluated expression is true (non zero), continue...
  // otherwise read out rest of line and skip to new line
  //if (machine.evaluate(keepExpression)==0.0) { // num only returns a num
  if (machine.evaluate(keepExpression).equals(0.0)) { // num only returns a num
    // read everthing to the end of line
    while (pnt<linelength && !line.substring(pnt,pnt+1).equals("\n")) {
      pnt++;
    }
    return true; // parsed okay
  }
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
  return true;
}

boolean ProcessGOTOstatement()
{
  SkipSpaces(); // added this in as there may be leading spaces (probably a better way to do this)
  ReadExpression(); // not really, should just be a numberic??? maybe an expression is good
  // lets go!
  // only with this line will we not do a front to back parse
  if (machine.enabledmovement) {
    machine.gotoLine(keepExpression);
    pnt=machine.executionpoint; // we should now have a different execution point
  }
  ReadColon();
  return true;
}

boolean ProcessGOSUBstatement()
{
  ReadExpression(); // not really, should just be a numberic??? maybe an expression is good
  if (machine.enabledmovement) {
    machine.gosubLine(keepExpression,pnt);
    pnt=machine.executionpoint; // we should now have a different execution point
  }
  ReadColon();
  return true;
}

boolean ProcessRETURNstatement()
{
  if (machine.enabledmovement) {
    machine.popReturn();
    pnt=machine.executionpoint; // we should now have a different execution point
  }
  ReadColon(); // because we may have been left with a colon
  //IgnoreRestofLine(); // no this is wrong, we are back were we came from here!
  return true;
}

boolean ProcessNEXTstatement()
{
  keepExpression=""; // in case it is nothing!
  ReadExpression(); // not really, should just be a variable!!
  // split up between commas
  int at=0; 
  int start=0;
  while (at<keepExpression.length()) {
    if (keepExpression.substring(at,at+1).equals(",")) {
      if (machine.processNEXT(pnt,keepExpression.substring(start,at))) {
        // we did loop
        pnt=machine.executionpoint;
        return true; // we skip the following ones
      }
      start=at+1;
    }
    at++;
  }
  if (machine.processNEXT(pnt,keepExpression.substring(start,keepExpression.length()))) {
    // we did loop
    pnt=machine.executionpoint;
    return true; // we skip the following ones
  }
  ReadColon();
  return true;
}

boolean ProcessFORstatement()
{
  String forto;
  String forstep="1";
  if (verbose) { System.out.printf("Processing FOR statement\n"); }
  ReadAssignment();
  ReadExpression();

  if (verbose) { System.out.printf("MachineVariableSet(variable=%s with evaluate( %s ))\n",keepVariable,keepExpression); }
  // parse the keep variable in the machine to turn X(I+1) into X(42)
  machine.setvariable(machine.parse(keepVariable),machine.evaluate(keepExpression));

  if (verbose) { System.out.printf("MachineVariableSet(variable=%s with evaluate( %s ))\n",keepVariable,keepExpression); }
  ReadStatementToken(); // note MUST be TO
  if (gotToken!=ST_TO) {
    System.out.printf("?SYNTAX ERROR 104: did not get TO token\n");
    return false;
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
      return true;
    } else {
      System.out.printf("?SYNTAX ERROR 105: did not get STEP token\n");
      return false;
    }
  } else {
    // no STEP keyword
  }
  // MachineProcessFOR...
  // should to be an expression or a evaluated number?
  //machine.processFOR(/*position*/pnt,/*variable*/..,/*to*/..,/*step*/..);
  ReadColon(); // check
  machine.createFORloop(pnt, keepVariable, machine.evaluate(forto).num(), machine.evaluate(forstep).num());
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

boolean ProcessREMstatement() 
{
  IgnoreRestofLine();
  return true;
}

boolean ProcessGETstatement()
{
  ReadExpression();
  if (verbose) { System.out.printf("getting to %s\n",keepExpression); }
  String got=machine.getkey();
  if (verbose) { System.out.printf("got string \"%s\"\n",got); }
  // it a string so keep it quoted
  machine.setvariable(machine.parse(keepExpression),machine.evaluate("\""+got.toLowerCase()+"\""));
  return true;
}
boolean ProcessINPUTstatement()
{
  ReadExpression();
  System.out.printf("inputting to %s\n",keepExpression);
  String got=machine.getline();
  System.out.printf("got string \"%s\"\n",got.trim());
  // it a string so keep it quoted
  machine.setvariable(machine.parse(keepExpression),machine.evaluate("\""+got.trim().toLowerCase()+"\""));
  if (verbose) { machine.dumpstate(); }
  return true;
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

boolean ReadExpression()
{
    int start=pnt;
    int previous;
    previous=pnt;
    boolean quoted=false;
    //keepExpression="";
    while (pnt<linelength) {
      String a=line.substring(pnt,pnt+1);
      previous=pnt;
      if (a.equals("\n")) { break; }
      if (!quoted && a.equals(":")) {
        break;
      }
      if (!quoted && a.equals(";")) {
        break;
      }
      if (!quoted && ReadStatementToken()) {
        break;
      }
      if (a.equals("\"")) {
        quoted=!quoted;
      }
      pnt++;
    }
  if (pnt==linelength) previous=pnt; // special case when we read to end of line
  if (verbose) { System.out.printf("Interpreting \"%s\" as an expression\n",line.substring(start,previous)); }
  keepExpression=line.substring(start,previous);
  return true;
}

boolean ReadAssignment()
{
    int at=0;
    // up here - should we get rid of spaces?
    int bracketcount=0;
    boolean isarray=false;
    int start=pnt;
    while (pnt+at<linelength) {
      
      String a=line.substring(pnt+at,pnt+at+1);
      if (a.equals("(")) { 
        if (!isarray && bracketcount==0) {
          // okay, we mark the start here
          start=pnt+at+1;
          isarray=true; 
        }
        bracketcount++; 
      } else if (a.equals(")")) { bracketcount--; }
        // okay, we have an array
      else if (a.equals("=")) {
        // should only allow variables
        // if (at==0) start allowed
        // else internal allowed
        break;
      }
      at++;
    }
    if (pnt+at<linelength) {
      // we have got variable =
      if (false) {
        if (isarray) {
          if (verbose) { System.out.printf("Found an ARRAY variable %s\n",line.substring(pnt,pnt+at)); }
          // really, should evaluate the bit in the middle
          // assuming (very big assumption that we end with )= when there is an array assignment)
          if (verbose) { System.out.printf("Found an ARRAY contents= %s\n",line.substring(start,pnt+at-1)); }
     
          keepVariable=line.substring(pnt,start)+machine.evaluate(line.substring(start,pnt+at-1)).print()+")"; // the contents of the array evaluated
          if (verbose) { System.out.printf("Found an ARRAY variable %s\n",keepVariable); }
        }
      }
      if (verbose) { System.out.printf("Found variable %s\n",line.substring(pnt,pnt+at)); }
      keepVariable=line.substring(pnt,pnt+at); // keep it
      pnt+=at+1; // read out the variable and the "=" sign
      return true;
    } else {
      return false;
    }
}

boolean ReadLineNo() {
    int at=0;
    //System.out.printf("Trying to read line number - looking at [%d] %s\n",pnt,line.substring(pnt+at,pnt+at+1));
    while (pnt+at<linelength) {
      String a=line.substring(pnt+at,pnt+at+1);
      if (a.equals("\n")) {
        // dont read this
        if (verbose) { System.out.printf("Line number %s\n",line.substring(pnt,pnt+at)); }
        keepLine=line.substring(pnt,pnt+at);
        pnt+=at;
        return true;
      }
      if (a.equals(" ")) {
        break; // hopefully got some part of a number
      }
      if (!(a.compareTo("0")>=0 && a.compareTo("9")<=0)) {
        return false; // not a number
      }
      at++;
    }
    //if (pnt+at<linelength && at>0) {  // no, not now, as we may be reading a ...THEN100 line
    if (at>0) {
      // we have got number
      if (verbose) { System.out.printf("Line number %s\n",line.substring(pnt,pnt+at)); }
      keepLine=line.substring(pnt,pnt+at);
      pnt+=at+1;
      return true;
    } else {
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

///////////////////////////////
boolean MachineEND() {
  return true;
}

///////////////////////////////

  public static void main(String[] args) {
    if (false) { System.out.printf("Mello Word\n"); }
    // no longer can run it like this - must run it from machine
    //new statements(args );
  }
}

/////////
// END //
/////////
