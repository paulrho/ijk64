/////////////////////////////////////////////////////////////////////////////////
//
// $Id$
//
// $Log$
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
  //boolean verbose=true;

  String read_a_file() {
    return read_a_file("test.basic");
  }

  static String read_a_file(String filename) {

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
    verbose=machine.verbose; // inherit this now
    interpret_string(interpstring);
  }

  statements(String[] args, Machine themachine)
  {
    machine=themachine;
    verbose=machine.verbose; // inherit this now
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

String[] basicTokens={"FOR","TO","STEP","NEXT","IF","THEN","GOTO","GOSUB","RETURN","PRINT#","PRINT","END","DIM","GET#5,","POKE","OPEN","INPUT#1,","CLOSE","DATA","RUN","READ","RESTORE","INPUT","LIST","META-VERBOSE","SYS","CLR","META-SCALE","META-ROWS","FAST","GET","REM","META-CHARSET","LOAD","META-DUMPSTATE"};
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
static final int ST_END=11;
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
static final int ST_META_SCALE=27;
static final int ST_META_ROWS=28;
static final int ST_FAST=29;
static final int ST_GET=30;
static final int ST_REM=31;
static final int ST_META_CHARSET=32;
static final int ST_LOAD=33;
static final int ST_META_DUMPSTATE=34;

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

void precache_all_data()
{
  // read_all_lines
  // read the line# first
  pnt=0;
  if (verbose) { System.out.printf("Looking for DATA\n"); }
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
  precache_all_data();
  if (verbose) { System.out.printf("DATA is:\n%s\n",machine.allDATA); }

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
    if (verbose) { System.out.printf("On Line %s\n",keepLine); }
    machine.currentLineNo=keepLine; // only added for error tracking - might slow things down
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

      if (verbose) { System.out.printf("At position %d\n",pnt); }
      if (machine.hasControlC()) {
        System.out.printf("?BREAK%s%s\n",machine.currentLineNo.equals("")?"":" ON LINE ",machine.currentLineNo);
        machine.print("?break"+(machine.currentLineNo.equals("")?"":" on line ")+machine.currentLineNo+"[CR]");
        return;
      }
      try {
        if (!ReadStatement()) {
          System.out.printf("?SYNTAX ERROR%s%s\n  101:Did not get statement\n",machine.currentLineNo.equals("")?"":" ON LINE ",machine.currentLineNo);
          machine.print("?syntax error"+(machine.currentLineNo.equals("")?"":" on line ")+machine.currentLineNo+"[CR]");
          // force it to exit
          pnt=linelength;
          break;
        }
      } catch (ArrayIndexOutOfBoundsException e) { 
          System.out.printf("?ILLEGAL QUANTITY%s%s\n  201:Got ArrayIndexOutOfBoundsException\n",
            machine.currentLineNo.equals("")?"":" ON LINE ",machine.currentLineNo);
          machine.print("?illegal quantity"+(machine.currentLineNo.equals("")?"":" on line ")+machine.currentLineNo+"[CR]");
          return; 
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
  if (verbose) { machine.dumpstate(); }

}

boolean ReadStatement() {
  // read statement should also read the colon at the end, ready for the next statement
  if (verbose) { System.out.printf("In ReadStatement\n"); }
  if (ReadStatementToken()) {
    if (verbose) { System.out.printf("Got %d as token\n",gotToken); }
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
      case ST_PRINThash: // because it would have gone elsewhere
        if (ProcessIGNOREstatement()) { return true; }
        break;
      case ST_FAST:  // I wish
        if (ProcessIGNOREstatement()) { return true; }
        break;
      case ST_RESTORE: 
        machine.uptoDATA=0; // revert back again
        if (ProcessIGNOREstatement()) { return true; }
        return true;
      case ST_READ: 
        if (ProcessREADstatement()) { return true; }
        // this is tricky, even though I added (and great effor) the
        // ability to assign to "lists" of variables i.e. A,B=6,7
        // this doesnt reall work for READ and DATA, as it is not a
        // fix string of DATA, whereis we have a stream.
        break;
      case ST_CLR: 
        // should really do something
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
      case ST_GEThash:
        if (ProcessGEThashstatement()) { return true; }
        break;
      case ST_META_DUMPSTATE:
        machine.dumpstate();
        return true;
      case ST_META_VERBOSE:
        verbose=true; machine.verbose=true;
        machine.evaluate_engine.verbose=true;
        return true;
      case ST_META_CHARSET:
        if (ProcessMETACHARSETstatement()) { return true; }
        break;
      case ST_META_SCALE:
        if (ProcessMETASCALEstatement()) { return true; }
        break;
      case ST_LOAD:
        if (ProcessLOADstatement()) { return true; }
        break;
      case ST_META_ROWS:
        if (ProcessMETAROWSstatement()) { return true; }
        break;
      case ST_INPUT: case ST_INPUT1:
        if (ProcessINPUTstatement()) { return true; }
        break;
      case ST_POKE:
        if (ProcessPOKEstatement()) { return true; }
        break;
      case ST_OPEN: case ST_CLOSE:
        if (ProcessIGNOREstatement()) { return true; }
        break;
      case ST_LIST:
        if (ProcessLISTstatement()) { return true; }
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

      machine.assignment(keepVariable+"="+keepExpression);

      //machine.setvariable(machine.parse(keepVariable),machine.evaluate(keepExpression));
      // just change this one for the moment - try new way

      ReadColon();
      return true;
    } else {
      System.out.printf("?SYNTAX ERROR%s%s\n  103:Did not get token or assignment\n",machine.currentLineNo.equals("")?"":" ON LINE ",machine.currentLineNo);
      // doubled errors// machine.print("?syntax error"+(machine.currentLineNo.equals("")?"":" on line ")+machine.currentLineNo+"[CR]");
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
    if (verbose) { System.out.printf("goto, moving to executionpoint %d\n",machine.executionpoint); }
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
    if (verbose) { System.out.printf("gosub, moving to executionpoint %d\n",machine.executionpoint); }
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
      if (machine.processNEXT(pnt,keepExpression.substring(start,at).toLowerCase())) {
        // we did loop
        pnt=machine.executionpoint;
        return true; // we skip the following ones
      }
      start=at+1;
    }
    at++;
  }
  if (machine.processNEXT(pnt,keepExpression.substring(start,keepExpression.length()).toLowerCase())) {
    // we did loop
    pnt=machine.executionpoint;
    return true; // we skip the following ones
  }
  if (verbose) { System.out.printf("Finished the next loops\n"); }
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
  machine.assignment(keepVariable.toLowerCase()+"="+keepExpression);
  //machine.setvariable(machine.parse(keepVariable),machine.evaluate(keepExpression));

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

boolean ProcessREMstatement() 
{
  IgnoreRestofLine();
  return true;
}

boolean ProcessLISTstatement()
{
  //machine.print(machine.code);
  // not implemented yet!
  return true;
}

boolean ProcessGETstatement()
{
  ReadExpression();
  if (verbose) { System.out.printf("getting to %s\n",keepExpression); }
  String got=machine.getkey();
  if (verbose) { System.out.printf("got string \"%s\"\n",got); }
  // it a string so keep it quoted
  machine.assignment(keepExpression.toLowerCase()+"="+"\""+got.toLowerCase()+"\"");
  //machine.setvariable(machine.parse(keepExpression),machine.evaluate("\""+got.toLowerCase()+"\""));
  return true;
}
boolean ProcessGEThashstatement()
{
  ReadExpression();
  if (verbose) { System.out.printf("getting to %s\n",keepExpression); }
  String got=machine.getkey();
  if (verbose) { System.out.printf("got string \"%s\"\n",got); }
  // it a string so keep it quoted
  machine.assignment(keepExpression.toLowerCase()+"="+"\""+got.toLowerCase()+"\"");
  //machine.setvariable(machine.parse(keepExpression),machine.evaluate("\""+got.toLowerCase()+"\""));
  return true;
}

boolean ProcessREADstatement()
{
  // here we use the special feature of evaluate
  ReadExpression();
  if (verbose) { System.out.printf("inputting to %s\n",keepExpression); }
  machine.assignment(keepExpression.toLowerCase()+"=metards(1)");
  if (verbose) { machine.dumpstate(); }
  return true;
}

// maybe allow assignments like this
// A$,B$,C$="X","Y","Z"
// can be used for INPUT and READ
boolean ProcessINPUTstatement()
{
  ReadExpression();
  if (verbose) { System.out.printf("inputting to %s\n",keepExpression); }
  String got=machine.getline();
  if (verbose) { System.out.printf("got string \"%s\"\n",got.trim()); }
  // it a string so keep it quoted
  // still need to separate out the answer to multiple strings!
  // divide up by commas and separate out like this:
  // TEST,SECOND
  // "TEST","SECOND"
  // change all commas to \",\"
  String processedString = stringQuoteStuff(got.trim().toLowerCase());
  machine.assignment(keepExpression.toLowerCase()+"="+processedString);
  if (verbose) { machine.dumpstate(); }
  return true;
}

String stringQuoteStuff(String arg)
{
  //take a string and return it as is appart from , surrounded by \",\" and one single \" before and after the string
  String building="";
  for (int p=0; p<arg.length(); ++p) { 
    String a=arg.substring(p,p+1);
    if (a.equals(",")) {
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

boolean ProcessLOADstatement() 
{
  ReadExpression();
  if (verbose) { machine.dumpstate(); }
  machine.print("loading filename "+machine.evaluate(keepExpression).str().toLowerCase());
  //machine....(machine.evaluate(keepExpression).str());
  return true;
}
boolean ProcessMETACHARSETstatement() 
{
  ReadExpression();
  if (verbose) { machine.dumpstate(); }
  machine.machinescreen.changeCharSet((int)machine.evaluate(keepExpression).num());
  return true;
}

boolean ProcessMETASCALEstatement() 
{
  ReadExpression();
  if (verbose) { machine.dumpstate(); }
  machine.machinescreen.setScale((int)machine.evaluate(keepExpression).num());
  return true;
}

boolean ProcessMETAROWSstatement() 
{
  ReadExpression();
  if (verbose) { machine.dumpstate(); }
  machine.machinescreen.setRows((int)machine.evaluate(keepExpression).num());
  return true;
}

boolean ProcessPOKEstatement() 
{
  ReadExpression();
  if (verbose) { System.out.printf("inputting to %s\n",keepExpression); }
  //verboseFull();
  // we should have num,num evaluate into a list
  if (verbose) { System.out.printf("inputting to %s\n",machine.evaluate(keepExpression).print()); }
  if (verbose) { machine.dumpstate(); }
  machine.performPOKE(machine.evaluate(keepExpression)); // we assume it is a list of two numbers
  //verboseOff();
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
        keepLine="";
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
