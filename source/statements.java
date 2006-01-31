// here we go on a new venture - basic interpreting
//   

// to do - cache the line # to the pnt # so that in a goto we can go there (or at least run through lines to (caching at same time) to find where it is)

// reads an entire basic program now 20060131

// need to separate out the ND"sdgsdg" so it evaluates as ND and "sdgsdg"
// need to work out how to do the DIM statement (doesn't interpret the top level - it sets it up
// does this happen:
//   count the brackets and separate on the outer commas
//  or change evaluate to know about a DIM statement (it separates commas etc)


import java.io.*;

class statements {

  String read_a_file() {

    String content="";

    try {
      FileInputStream fis = new FileInputStream("test.basic");
      int x= fis.available();
      byte b[]= new byte[x];
      fis.read(b);
      content = new String(b);
      //System.out.println(content);
    } catch (Exception e) { }

    return content;
  }

  statements(String[] args)
  {
    //evaluate evaluate_engine = new evaluate();  // create engine

    for (int i=0; i<args.length; ++i) {
      if (args[i].substring(0,1).equals("-")) {
        if (true) { System.out.printf("Got a switch\n"); }

        if (args[i].substring(0,2).equals("-v")) {
          //evaluate_engine.verbose=true;
        } else if (args[i].substring(0,2).equals("-q")) {
          //evaluate_engine.verbose=false;
        } else if (args[i].substring(0,2).equals("-t")) {
          //do_many=true; // timing test
        }
      } else {
        // parameter // do it immediately
        //has_parameter=true;
        interpret_string(args[i]);
      }
    }
    // at the moment, spaces are passed onto the "evaluate interpreter" that's okay
    //interpret_string("FORI=1TO1000:PRINTI:NEXT");
    interpret_string( read_a_file() );
    if (false) {
    interpret_string(
      " 2 mx=300:nc=2:rn=int(rnd(ti)*1000):a=rnd(-rn)" + "\n" +
      "10 FORI=1TO1000+4:PRINTI;I*3:NEXTI" + "\n" +
      "20 IFI<3THENPRINT\"HELLO\":GOTO10" + "\n" +
      "30 IFI<3THENPRINT\"HELLO\";:GOTO10" + "\n" +
      "40 X=SIN(I)*0.2142/232.4+124124+9*5^3:X=X+2" + "\n" +
      "50 PRINT X" + "\n" +
      "60 REM IGNORE THIS" + "\n" +
      "70 FOR I = 1 TO 10 : NEXT I" + "\n" +
      "99 END" + "\n" +
      "110 dimlg%(mx,nc),rg%(mx,nc),t(50),d%(mx),le%(mx),al%(mx),pm%(mx),pf%(mx)" + "\n" +
      "112 print\"do you want to bypass the generating    proceedures? (y/n):\"" + "\n" +
      "114 geta$:ifa$=\"\"then14" + "\n" +
      "116 ifa$=\"y\"thenprint\"<?> for help\":goto100" + "\n" +
      "118 print\"o.k.generating creatures.  (seed\";rn;\")\"" + "\n" +
      "120 FORip=1TO20:FORk=0TOnc:FORj=0TO14" + "\n" +
      "122 IFk<>1orj<>11THENlg%(ip,k)=lg%(ip,k)or(2^j*(int(rnd(rn)*2)))" + "\n" +
      "124 rg%(ip,k)=rg%(ip,k)or(2^j*(int(rnd(rn)*2)))" + "\n" +
      "126 NEXTj,k" + "\n" 
     );
     }


  } // end func

int MAXTOKENS=100;

String[] basicTokens={"FOR","TO","STEP","NEXT","IF","THEN","GOTO","GOSUB","RETURN","REM","PRINT","END","DIM","GET","POKE","OPEN","INPUT","CLOSE","DATA","RUN","READ","RESTORE"};
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
static final int ST_INPUT=16;
static final int ST_CLOSE=17;
static final int ST_DATA=18;
static final int ST_RUN=19;
static final int ST_READ=20;
static final int ST_RESTORE=21;

String line;
int pnt;
int linelength;
int gotToken=0;

String keepVariable;
String keepExpression;

void interpret_string(String passed_line)
{
  line=passed_line;
  linelength=line.length();
  pnt=0;
  System.out.printf("\n**************************************\n");
  //System.out.printf("Got line %s\n",line);

  // fix up code
  line=line.toUpperCase(); // convert to uppercase

  // instansiate the machine
  Machine machine = new Machine();

  // read the line# first
  while (true) {
    SkipSpaces();
    System.out.printf("\n");
    if (!ReadLineNo()) {
      System.out.printf("No line # -finishing\n");
      break;
    }
    SkipSpaces();
    while(true) {
      SkipSpaces();
      if (!ReadStatement()) {
        System.out.printf("?SYNTAX ERROR 101: did not get statement\n");
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
        if (ProcessREMstatement()) { return true; }
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
      case ST_RUN: 
        if (ProcessREMstatement()) { return true; }
        break;
      case ST_DATA: 
        if (ProcessIGNOREstatement()) { return true; }
        break;
      case ST_DIM: 
        if (ProcessIGNOREstatement()) { return true; }
        break;
      case ST_GET: 
        if (ProcessIGNOREstatement()) { return true; }
        break;
      case ST_POKE: case ST_OPEN: case ST_INPUT: case ST_CLOSE:
        if (ProcessIGNOREstatement()) { return true; }
        break;
      case ST_END: 
        MachineEND();
        return true; // just END!
    }
    return false;
  } else {
    // could be a null statement
    if (ReadColon()) { return true; }

    // could be an assignment
    if (ReadAssignment()) {
      ReadExpression();
      System.out.printf("MachineVariableSet(variable=%s with evaluate( %s ))\n",keepVariable,keepExpression);
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
      if (!line.substring(pnt+at,pnt+at+1).equals(basicTokens[tok].substring(at,at+1))) {
        break;
      }
      at++;
    }
    if (at==basicTokens[tok].length()) {
      // we matched it completely
      pnt+=at;
      System.out.printf("Found match for %s\n",basicTokens[tok]);
      gotToken=tok;
      return true;
    }
  }
  // otherwise, search for an "=" sign, and take note of the string preceeding
  //ReadAssignment();

  return false;
}

boolean ProcessPRINTstatement()
{
  System.out.printf("Processing PRINT statement\n");
  ReadExpression();
  System.out.printf("MachinePrintEvaluate( %s )\n",keepExpression);
  // Machine evaluate this expression and PRINT it!
  //System.out.printf("Would evaluate the expression %s\n","not finished coding yet");
  // if we finished with ";", then loop again
  while (pnt<linelength && line.substring(pnt,pnt+1).equals(";")) {
    pnt++;
    ReadExpression();
    System.out.printf("MachinePrintEvaluate( %s )\n",keepExpression);
    //System.out.printf("  also would evaluate the expression %s\n","not finished coding yet");
  }
  ReadColon(); // check
  return true;
}

boolean ProcessIFstatement()
{
  ReadExpression();
  
  System.out.printf("MachineEvaluate( %s )\n",keepExpression);
  //ReadStatementToken(); // note MUST be THEN or GOTO
  // implicetly read already with the ReadExpression
  if (gotToken!=ST_GOTO && gotToken!=ST_THEN) {
    System.out.printf("?SYNTAX ERROR 104: did not get THEN or GOTOT token\n");
    return false;
  }
  // it might be just a line # - try and read it - if it isn't keep going
  if (ReadLineNo()) {
    // then read out till end of line
    //while (pnt<linelength && !line.substring(pnt,pnt+1).equals("\n")) {
      //pnt++;
    //}
  }
  return true;
}

boolean ProcessGOTOstatement()
{
  ReadExpression(); // not really, should just be a numberic??? maybe an expression is good
  ReadColon();
  return true;
}
boolean ProcessGOSUBstatement()
{
  ReadExpression(); // not really, should just be a numberic??? maybe an expression is good
  ReadColon();
  return true;
}

boolean ProcessNEXTstatement()
{
  ReadExpression(); // not really, should just be a variable!!
  ReadColon();
  return true;
}

boolean ProcessFORstatement()
{
  System.out.printf("Processing FOR statement\n");
  ReadAssignment();
  ReadExpression();
  System.out.printf("MachineVariableSet(variable=%s with evaluate( %s ))\n",keepVariable,keepExpression);
  ReadStatementToken(); // note MUST be TO
  if (gotToken!=ST_TO) {
    System.out.printf("?SYNTAX ERROR 104: did not get TO token\n");
    return false;
  }
  ReadExpression();
  if (!ReadColon() && pnt<linelength && !line.substring(pnt,pnt+1).equals("\n")) {
    ReadStatementToken(); // note CAN be STEP
    if (gotToken==ST_STEP) {
      ReadExpression();
      return true;
    } else {
      System.out.printf("?SYNTAX ERROR 105: did not get STEP token\n");
      return false;
    }
  } else {
    // no STEP keyword
  }
  // MachineProcessFOR...
  ReadColon(); // check
  return true;
}

boolean ProcessREMstatement() 
{
  // read everthing to the end of line
  while (pnt<linelength && !line.substring(pnt,pnt+1).equals("\n")) {
    pnt++;
  }
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
  System.out.printf("Interpreting \"%s\" as an expression\n",line.substring(start,previous));
  keepExpression=line.substring(start,previous);
  return true;
}

boolean ReadAssignment()
{
    int at=0;
    // up here - should we get rid of spaces?
    while (pnt+at<linelength) {
      
      if (line.substring(pnt+at,pnt+at+1).equals("=")) {
        // should only allow variables
        // if (at==0) start allowed
        // else internal allowed
        break;
      }
      at++;
    }
    if (pnt+at<linelength) {
      // we have got variable =
      System.out.printf("Found variable %s\n",line.substring(pnt,pnt+at));
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
        System.out.printf("Line number %s\n",line.substring(pnt,pnt+at));
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
      System.out.printf("Line number %s\n",line.substring(pnt,pnt+at));
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
    if (true) { System.out.printf("Mello Word\n"); }
    new statements(args);
  }
}

/////////
// END //
/////////
