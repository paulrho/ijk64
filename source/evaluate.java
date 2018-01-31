/////////////////////////////////////////////////////////////////////////////////
//
// $Id: evaluate.java,v 1.36 2012/09/04 11:22:38 pgs Exp $
//
// $Log: evaluate.java,v $
// Revision 1.36  2012/09/04 11:22:38  pgs
// petscii mapping
// /
//
// Revision 1.34  2011/07/03 23:00:20  pgs
// Add EVAL$ function
// Fix insertspace etc modes - they were buggy
//
// Revision 1.33  2011/06/29 21:36:51  pgs
// more changes - no space required after line number
// shift return no-op
// using PETSCII
//
// Revision 1.32  2011/06/28 23:40:14  pgs
// Standardising keycodes - now use PETSCII
//
// Revision 1.31  2007/04/19 08:28:24  pgs
// Refactoring and simplifying/formatting code especially in Machine
//
// Revision 1.30  2007/04/18 09:37:19  pgs
// More refactoring with regards to creating program/immediate modes
//
// Revision 1.29  2007/04/17 09:22:33  pgs
// Adding ability to restart with CONT
// moved all statments into statements/Machine engine
//
// Revision 1.28  2007/04/16 21:31:00  pgs
// Complete exception creation, ratify error messages, refactor code
// to use exceptions (makes code clearer)
//
// Revision 1.26  2007/04/13 08:59:01  pgs
// fix verbose display for assign too
//
// Revision 1.25  2007/04/11 17:57:58  pgs
// clearing verbose stack display (to the same as comment in code)
//
// Revision 1.23  2006/02/19 22:27:29  pgs
// Add atn() as a the function atan.
// Add the string trying to interpret to the syntax error 001 & 002 lines
//
// Revision 1.22  2006/02/17 07:03:10  ctpgs
// Add the ability to define functions and use defined functions
//
// Revision 1.21  2006/02/15 21:19:34  pgs
// Fix TAB( to do the correct thing:
// It will create enough spaces to move the cursor to that column
// Note, this will only work as the start of an expression,
// but then this may be the same as the real thing???
// Also, allow partial matches of expressions when flagged
// and stop parsing at that point (setting the position of the next bit in an accessible variable)
//
// Revision 1.20  2006/02/15 01:52:47  ctpgs
// Standard header
//
//
/////////////////////////////////////////////////////////////////////////////////

// why not - java will do

// make test harness of equations

// PGS20060124
// believe it or not, I programmed this without using any reference, and I don't actually fully understand how it works!!
// but it does!

// PGS20060124-2
// revisited, totally new engine, again, rethought the solution and come up with a simple elegant algorithm
// infact, based on the last equation that didn't work in the former, and made me realise I'd gone way off track
// works!

// PGS20060125-1
// added and and or
// added chew up space

// thoughts - what about strings and ints, run special modes perhaps??  - match in with c64

// PGS20060127-1
// adding "," to allow multiple parameters
// what about functions with TWO or more arguments!! yes done
// binary and and or
// added > < <= >= <> = (etc)
//
// a few changes for the intergration 20060131
// fixed a few things
//
//
// todo:
//   should allow NO parameters, easy to do, just need to do it
//   separate out the test harness from the actual program!
//   types too (double int boolean string)
//   maybe add "[" and "]" bracketing too? (for arrays)
//
// note at least four possible grammars:
//     c64
//     c (java) like
//     calculator like (brackets dont need closing)
//     simple calculator like (function operates on current in display)
//     hybrid, some of c64 and some of c
//   
// to do:
//   make a "special" mode that does not evaluate the outer array or function
//   this will be used in the case of DIM or assignments: i.e.
//   DIM A(MAX+1) in this case A(MAX+1) is evaluated to A(100) and used to dimension the array
//   A(I+2)=5124   in this case A(I+2) is evaluated to A(99) only and used to assign 
//   will allow ,s too

//package au.com.futex.jebi;

import java.util.*; // for Random

class evaluate {

  // state of the machine
  // a stack looking like the following: (midway through a parse)
  //  e.g. 5.3+1.6/sin(0.3 ...
  //
  //              D_NUM   D_OP
  //            +-------+------+-------+
  //            |  num  |  op  |  func |
  //            +-------+------+-------+
  //           0|  5.3  |  +   |  N/A  |
  //           1|  1.6  |  /   |  N/A  |
  //           2|  N/A  |  (   |  sin  |
  // upto=4 -> 3|  0.3  | N/A  |  N/A  |
  //            +-------+------+-------+
  //              doing=D_OP^
  //
  String stkop[];
  double stknum[];
  int upto;
  int doing;
  String stkfunc[]; 
        // ^maybe could also use this for the string

  // to allow typeing
  String stkstring[];
  int stktype[];
  static final int ST_NUM=0; // double the default
  static final int ST_STRING=1; // new type
  static final int ST_INT=2; // not implemented
  static final int ST_DATASTREAM=5;
  static final int ST_INPUTSTREAM=6;
  static final int ST_PARAM=10; // the current variables name is in func - there is no actual value
                                // need to use ST_PARAM and ST_DATASTREAM in debugging output

  boolean is_function=false; // flag to indicate that the function name has been pre inserted in the array
  //String functionname="";  // temporarily used until the bracket is confirmed! - no longer required, prestore it!
  int parameters; // count of the number of parameters that a function is called with

  // parsing the string
  String intstring;
  int ispnt; // pointer into intstring
  String a; // temp string variable containing current pointed to char
  boolean is_defining_function;

  boolean speeder_compile=false;
  String compiled_obj="";
  String compiled_asm="";

  // constants and enums
  static int D_NUM=0;
  static int D_OP=1;
  static int D_ASSIGN=2;
  static String OP_OPEN_BRACKET="(";
  static String OP_CLOSE_BRACKET=")";
  static String OP_COMMA=",";

  // debugging/formatting etc
  String printprefix="";
  boolean verbose=true;
  boolean quiet=false;
  int parse_restart=0;
  boolean partialmatching=false;
  boolean stayonline=false;

  Machine using_machine=null;

  Random generator = new Random();

  static final int STACKLIMIT = 100;
  evaluate(Machine machine)
  {
    using_machine=machine;
    stknum=new double[STACKLIMIT];
    stkop=new String[STACKLIMIT];
    stkfunc=new String[STACKLIMIT];

    // to allow typeing
    stktype=new int[STACKLIMIT];
    stkstring=new String[STACKLIMIT];

    // all setup ready
  }

  evaluate()
  {
    if (false) { System.out.printf("Running evaluate\n"); }
    stknum=new double[STACKLIMIT];
    stkop=new String[STACKLIMIT];
    stkfunc=new String[STACKLIMIT];

    // to allow typeing
    stktype=new int[STACKLIMIT];
    stkstring=new String[STACKLIMIT];

    // all setup ready
  } // end func

    // show_state displays the state of the machine stack.
    // It is only printed in verbose mode
    // <PRE>
    // 
    //              D_NUM   D_OP
    //            +-------+------+-------+
    //            |  num  |  op  |  func |
    //            +-------+------+-------+
    //           0|  5.3  |  +   |  N/A  |
    //           1|  1.6  |  /   |  N/A  |
    //           2|  N/A  |  (   |  sin  |
    // upto=4 -> 3|  0.3  | N/A  |  N/A  |
    //            +-------+------+-------+
    //              doing=D_OP^
    //  state of machine is defined by
    //    upto
    //    doing
    //    is_function
    //    [stack] num op func
    // 
    // </PRE>
    //
  void show_state() {
    System.out.printf("%s                D_NUM                 D_OP\n",printprefix);
    System.out.printf("%s              +---------------+------+------+------------------------------+\n",printprefix);
    System.out.printf("%s              | num           | type | op   | func                         |\n",printprefix);
    System.out.printf("%s              +---------------+------+------+------------------------------+\n",printprefix);
    //System.out.printf("%s |STATE:  upto=%d doing=%d\n",printprefix,upto,doing);
    // necessarily there it is the case when doing==D_OP that we DON'T know what stkop[upto-1] is - therefore, don't show it!
    for (int levelx=0; levelx<upto+((doing==D_NUM)?1:0); levelx++) {
      System.out.printf("%s ",printprefix);
      if (levelx==upto-1+((doing==D_NUM)?1:0)) {
        System.out.printf("upto=%2d -> ",upto);
      } else {
        System.out.printf("           ");
      }
      System.out.printf("%2d|",levelx);
      // print num column
      String haveop=(levelx<upto-1 || levelx==upto-1 && doing==D_NUM)?stkop[levelx]:""; // it is blank as we are going to write to it
      if (stktype[levelx]==ST_STRING) {
        System.out.printf(" %-13s |", stkstring[levelx]);
      } else {
        if (levelx==upto || haveop.equals("(") || haveop.equals("===") || haveop.equals("===,") || haveop.equals("-ve") || levelx==upto-1 && doing==D_ASSIGN) {
          System.out.printf(" %-13s |", "");
        } else {
          System.out.printf(" %13f |", stknum[levelx]);
        }
      }

      // print type column
      String havetype="";
      if (levelx==upto || haveop.equals("(") || haveop.equals("===") || haveop.equals("===,")) havetype="";
      else if (stktype[levelx]==ST_NUM) havetype=" num";
      else if (stktype[levelx]==ST_STRING) havetype="string";
      else if (stktype[levelx]==ST_PARAM) havetype="param";
      else if (stktype[levelx]==ST_INT) havetype=" int";
      else if (stktype[levelx]==ST_DATASTREAM) havetype="datast";
      else if (stktype[levelx]==ST_INPUTSTREAM) havetype="inputs";
      System.out.printf("%-6s|",havetype);

      // print op column
      System.out.printf(" %-4s |", haveop);
          //(levelx!=upto-1 || doing!=D_OP)?stkop[levelx]:"N/A");
      System.out.printf(" %-28s |\n", 
        (stkfunc[levelx]!=null && !stkfunc[levelx].equals("")
          && ((haveop.equals("(") || haveop.equals("===") || haveop.equals("===,")
               || levelx==upto && is_function || levelx==upto-1 && doing==D_ASSIGN))
        )?stkfunc[levelx]+
             ((is_function && levelx==upto)?" (preset:is_function)":
              (doing==D_ASSIGN)?" (assignment)":""
             )
          :"");

        // (stkfunc[levelx]!=null && !stkfunc[levelx].equals("")
          // && (haveop.equals("")||haveop.equals("("))
        // )?stkfunc[levelx]+(haveop.equals("(")?"":" (possible)"):"");

        //(stkfunc[levelx]!=null)?" stkfunc[]="+stkfunc[levelx]:"");
        // I think it is only valid if it is D_OP??

      //System.out.printf("%s |upto=%d stknum[]=%f stkop[]=%s%s%s type=%d %s\n",
        //printprefix,levelx,stknum[levelx],(levelx!=upto-1 || doing!=D_OP)?stkop[levelx]:"N/A",(stkfunc[levelx]!=null)?" stkfunc[]=":"",(stkfunc[levelx]!=null)?stkfunc[levelx]:"",
        //stktype[levelx],stktype[levelx]==ST_STRING?stkstring[levelx]:"");
    }
    System.out.printf("%s              +---------------+------+------+------------------------------+\n",printprefix);
    System.out.printf("%s   %s  doing=%s\n",printprefix,
      (doing==D_OP)?"                        ":"",(doing==D_OP)?"D_OP^":((doing==D_NUM)?"D_NUM^":"D_ASSIGN"));
    System.out.printf("%s\n",printprefix);
  }

  void show_state_oldway() {
    System.out.printf("%s |STATE:  upto=%d doing=%d\n",printprefix,upto,doing);
    // necessarily there it is the case when doing==D_OP that we DON'T know what stkop[upto-1] is - therefore, don't show it!
    for (int levelx=0; levelx<upto; levelx++) {
      System.out.printf("%s |upto=%d stknum[]=%f stkop[]=%s%s%s type=%d %s\n",
        printprefix,levelx,stknum[levelx],(levelx!=upto-1 || doing!=D_OP)?stkop[levelx]:"N/A",(stkfunc[levelx]!=null)?" stkfunc[]=":"",(stkfunc[levelx]!=null)?stkfunc[levelx]:"",
        stktype[levelx],stktype[levelx]==ST_STRING?stkstring[levelx]:"");
    }
    System.out.printf("%s\n",printprefix);
  }

  //int return_type; // the type that calc returns
  //double return_num; // if it is a double, the return value
  //String return_string; // if it is a string the return string
  ////////////////////////////////////////////////////
  //
  ////////////////////////////////////////////////////
  // just a bit of shorthard to improve readability
  void calc_and_pop() throws EvaluateException {
    if (verbose) { System.out.printf("%sPerforming a calculation on %f %s %f\n",printprefix,stknum[upto-2],stkop[upto-2],stknum[upto-1]); }
    calc();
    upto--;
    if (verbose) { show_state(); }
    return;
  }

  ////////////////////////////////////////////////////
  //
  ////////////////////////////////////////////////////
  void calc() throws EvaluateException {

    double left=stknum[upto-2];
    String oper=stkop[upto-2];
    double right=stknum[upto-1];
    String function=stkfunc[upto-2];
    int lefttype=stktype[upto-2]; // the type of variable we are working with
    int righttype=stktype[upto-1]; // the type of variable we are working with

    // for all these convert the function (or array name) to lowercase
    if (function != null) { function=function.toLowerCase(); } //20060204pgs

    if (oper.equals("===")) {
      // we have an assignment
      // just do it (how)
      //setvariable
      if (verbose) { System.out.printf("I want to assign the variable, but dont know how yet\n"); }
    }
    double answer=0.0;
    if (oper.equals(OP_OPEN_BRACKET) && function != null && !function.equals("")) { 
      // if it is bracketed and there is a function
      //answer=right;  why do this?
      if (verbose) { System.out.printf("%sfunction needs calculating : %s(%f) we have %d parameters\n",printprefix,function,right,parameters); }
      // if it is a function the parameters are stored at upto-1, upto, upto+1, upto+2, upto+3... etc.
      // for a single parameter it is stored simply in upto-1

      // these functions take 1 numeric parameter:
      //if (righttype!=ST_NUM) { System.out.printf("?INCORRECT TYPE\n"); }
      String save_compiled_asm="";
      if (speeder_compile) { save_compiled_asm=compiled_asm; compiled_obj=compiled_obj+","+function; // we will unwind this if it is a variable array
	      compiled_asm+="  FNC "+function+"\n";
	      //System.out.printf(",%s",function);
	      int ft=Petspeed.ftoken(function);
                if (verbose && ft<0) System.out.printf("A-COMPILER could not find %s (okay if array)\n",function);
	      using_machine.petspeed.addInstr(Petspeed.I_FNC | ft);
      }

      if (function.length()>=2 && function.substring(0,2).equals("fn")) {
        if (verbose) { System.out.printf("Wanting to use predefined function %s with parameter %f\n",function,right); }
        if (using_machine!=null) {
          // do we need toLower?
          String param=using_machine.getvariable("fn_"+function.toLowerCase()+"_param").str();
          String form=using_machine.getvariable("fn_"+function.toLowerCase()+"_function").str();
          if (verbose) { System.out.printf("About to set %s to %f\n",param.toLowerCase(),right); }
          using_machine.setvariable(param.toLowerCase(),new GenericType(right));
          if (verbose) { System.out.printf("form:%s\n",form); }
          evaluate evaluate_engine = new evaluate(using_machine); //probably very cpu expensive
          // it is very important that we use the same random generator!!!
          evaluate_engine.generator=generator;
          evaluate_engine.verbose=verbose;
          evaluate_engine.quiet=true;
          answer=evaluate_engine.interpret_string(form).num();
          if (verbose) { System.out.printf("Returned from evaluate\n"); }
          if (verbose) { show_state(); }
        } else {
          // this is just a made up value as we dont have a machine
          answer=100.0; // evaulate it (recurse)
        }
      } else 
      if (function.equals("sin")) {
        answer=Math.sin(right);
      } else if (function.equals("silly")) {
        if (parameters==3) {
          if (verbose) { System.out.printf("%susing params %f %f %f\n",printprefix,stknum[upto-1],stknum[upto],stknum[upto+1]); }
          answer=stknum[upto-1]+stknum[upto]*10.0+stknum[upto+1]*100.0;
        } else {
          System.out.printf("?SYNTAX ERROR 005 - wrong number of parameters - had %d params wanting %d\n",parameters,3);
          answer=0.0;
          throw new EvaluateException("WRONG NUMBER PARAMETERS IN SILLY : "+parameters+" WANTED : "+3);
        }
      } else if (function.equals("sgn")) {
        if (right<0.0) { answer=-1.0; }
        else if (right==0.0) { answer=0.0; }
        else answer=1.0;
      } else if (function.equals("int")) {
        answer=(double)((int)(right));
      } else if (function.equals("abs")) {
        answer=Math.abs(right);
      } else if (function.equals("rnd")) {
        if (right<0) { generator = new Random((long)-right); } // added this in for seeds
        answer=generator.nextDouble();
      } else if (function.equals("exp")) {
        answer=Math.exp(right);
      } else if (function.equals("cos")) {
        answer=Math.cos(right);
      } else if (function.equals("acos")) { // extension
        answer=Math.acos(right);
      } else if (function.equals("asin")) { // extension
        answer=Math.asin(right);
      } else if (function.equals("log")) { // didnt have this before
        answer=Math.log(right);
      } else if (function.equals("atn")) {
        answer=Math.atan(right);
      } else if (function.equals("tan")) {
        answer=Math.tan(right);
      } else if (function.equals("sqrt")) {
        answer=Math.sqrt(right);
      } else if (function.equals("sqr")) { // for basic (had sqt)
        answer=Math.sqrt(right);

      // string functions
      } else if (function.equals("val")) {
        // takes a string but returns a double
        stktype[upto-2]=ST_NUM;
        try {
          stknum[upto-2]=Double.parseDouble(stkstring[upto-1]); // this was WRONG! it cost 3 hours!! -finally fixed 2am monday
        } catch (Exception e) { 
          // maybe just a "." should be coverted to 0 or a ""???
          // throw new EvaluateException("NON NUMERIC STRING"); // look at what the C64 really does
          stknum[upto-2]=0.0; 
        }
        return;
      } else if (function.equals("tab")) { // they ARE different
        // we need to talk to the machine to get the current cursor setting
        int cursx=0;
        if (using_machine!=null) {
          cursx=using_machine.machinescreen.cursX;
        }
        stktype[upto-2]=ST_STRING;
        String building="";
        int moveto=(int)stknum[upto-1]-cursx;
        if (moveto<0) moveto=0;
        for (int i=0; i<moveto; ++i) {
          building+="(rght)";
        }
        stkstring[upto-2]=building;
        stayonline=true; 
        return;
      } else if (function.equals("spc")) { // they probably are different
        stktype[upto-2]=ST_STRING;
        String building="";
        for (int i=0; i<(int)stknum[upto-1]; ++i) {
          building+="(rght)";
        }
        stkstring[upto-2]=building;
        stayonline=true; 
        return;

      } else if (function.equals("eval$")) { // recursive!! - will this work?
        evaluate evaluate_engine = new evaluate(using_machine); //probably very cpu expensive


          // it is very important that we use the same random generator!!!
          evaluate_engine.generator=generator;
          evaluate_engine.verbose=verbose;
          evaluate_engine.quiet=true;

          stktype[upto-2]=ST_STRING;        
          // stkstring[upto-2]=machine.evaluate(stknum[upto-1]).print();
          //answer=evaluate_engine.interpret_string(stkstring[upto-1]).print();

          using_machine.setvariable("everr$",new GenericType(""));
          try {

            // if we have an xxx= this is an assigment...                      
            if (stkstring[upto-1].matches("[ ]*[a-zA-Z]+[ ]*=.*")) {
              System.out.printf("assigment\n");
              stkstring[upto-2]="";
              // I think we throw away answer
              evaluate_engine.interpret_string_with_assignment(stkstring[upto-1]);
            } else {
              stkstring[upto-2]=evaluate_engine.interpret_string(stkstring[upto-1]).print();
            }

          } catch (EvaluateException evalerror) {
             System.out.printf("Caught Evaluate Error: %s\n",evalerror.getMessage());
             using_machine.setvariable("everr$",new GenericType(evalerror.getMessage().toLowerCase()));
             // put this into error message
             //throw new EvaluateException(evalerror.getMessage());
          }

        return;
        
      } else if (function.equals("pos")) {
        // tap into the machine - I think this gives the position on the screen?
        // we need to talk to the machine to get the current cursor setting
        int cursx=0;
        if (using_machine!=null) {
          cursx=using_machine.machinescreen.cursX;
        }
        stktype[upto-2]=ST_NUM;
        stknum[upto-2]=cursx;
        return;
      } else if (function.equals("fre")) {
          int param=(int)stknum[upto-1];
          /* Returns the maximum amount of memory available to 
             the Java Virtual Machine set by the '-mx' or '-Xmx' flags. */
          long maxMemory = Runtime.getRuntime().maxMemory();

          /* Returns the total memory allocated from the system 
             (which can at most reach the maximum memory value 
             returned by the previous function). */
          long totalMemory = Runtime.getRuntime().totalMemory();

          /* Returns the free memory *within* the total memory 
             returned by the previous function. */
          long freeMemory = Runtime.getRuntime().freeMemory();
          long allocatedMemory = (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory());
          long presumableFreeMemory = Runtime.getRuntime().maxMemory() - allocatedMemory;
        stktype[upto-2]=ST_NUM;
        switch(param) {
          case 1:
            stknum[upto-2]=freeMemory;
            break;
          case 2:
            stknum[upto-2]=allocatedMemory;
            break;
          case 3:
            stknum[upto-2]=totalMemory;
            break;
          case 4:
            stknum[upto-2]=maxMemory;
            break;
          //case 0:
          default:
            stknum[upto-2]=presumableFreeMemory;
        }
        //if (using_machine!=null) {
        //} else {
          //stktype[upto-2]=ST_NUM;
          //stknum[upto-2]=0.0;
        //}
        return;
      } else if (function.equals("peek")) {
        if (using_machine!=null) {
          stktype[upto-2]=ST_NUM;
          stknum[upto-2]=using_machine.peek((int)stknum[upto-1]);
        } else {
          stktype[upto-2]=ST_NUM;
          stknum[upto-2]=0.0;
        }
        return;
      } else if (function.equals("len")) {
        stktype[upto-2]=ST_NUM;
        stknum[upto-2]=stkstring[upto-1].length();
        return;
      } else if (function.equals("asc")) {
		if (using_machine!=null) {
          //stktype[upto-2]=ST_NUM;
          //stknum[upto-2]=using_machine.asc(stkstring[upto-1]);
            // need to convert from PET!
          stktype[upto-2]=ST_NUM;
          if (stkstring[upto-1]=="") throw new EvaluateException("ILLEGAL QUANTITY");
          int v=(int)((stkstring[upto-1].charAt(0))&0xFF);
          if (v>=65&&v<=90) stknum[upto-2]=v+128; 
          else if (v>=65&&v<=90) stknum[upto-2]=v+32;  //try 2
          else if (v>=97&&v<=122) stknum[upto-2]=v-32; 
          else if (v>=193&&v<=218) stknum[upto-2]=v-96; 
          else if (v==95) stknum[upto-2]=96; 
          else if (v==96) stknum[upto-2]=95; 
          else if (v==123) stknum[upto-2]=179; else if (v==179) stknum[upto-2]=123;  //try this
          else if (v==124) stknum[upto-2]=125; 
          else if (v==125) stknum[upto-2]=171; 
          else if (v==171) stknum[upto-2]=124;  
          else          
            stknum[upto-2]=v;
          
		} else {		  
          stktype[upto-2]=ST_NUM;
          stknum[upto-2]=(int)((stkstring[upto-1].charAt(0)+1-'A')&0xFF);  // need to convert from PET!
	    }
        if (verbose) { System.out.printf("calculating the ascii of %s to be %f\n",stkstring[upto-1],stknum[upto-2]); }
        return;
      } else if (function.equals("chr$")) {
		if (using_machine!=null) {
          stktype[upto-2]=ST_STRING;
          //stkstring[upto-2]=using_machine.chrD((int)stknum[upto-1]);
          int v=(int)stknum[upto-1];
          if (v>=193&&v<=218) v=v-128; 
          else if (v>=65&&v<=90) v=v+32; 

          //else if (v>=97&&v<=122) v=v-32;   // quick test try

          else if (v>=97&&v<=122) v=v+96; 
          else if (v==95) v=96; 
          else if (v==96) v=95; 
          else if (v==123) v=179; else if (v==179) v=123;  //try this
          else if (v==124) v=171; 
          else if (v==125) v=124; 
          else if (v==171) v=125;  
          stkstring[upto-2]=""+(char)v;
            
	    } else {
          stktype[upto-2]=ST_STRING;
          stkstring[upto-2]=""+(char)stknum[upto-1];
	    }
        return;
      } else if (function.equals("pcasc")) {
		{
          stktype[upto-2]=ST_NUM;
          if (stkstring[upto-1]=="") throw new EvaluateException("ILLEGAL QUANTITY");
          stknum[upto-2]=(int)((stkstring[upto-1].charAt(0))&0xFF);  // need to convert from PET!
		}
        if (verbose) { System.out.printf("calculating the ascii of %s to be %f\n",stkstring[upto-1],stknum[upto-2]); }
        return;
      } else if (function.equals("pcchr$")) {
	    {
          stktype[upto-2]=ST_STRING;
          stkstring[upto-2]=""+(char)stknum[upto-1];
	    }
        return;
      } else if (function.equals("petconvert") || function.equals("petcnv") ) {
		if (using_machine!=null) {
          stktype[upto-2]=ST_NUM;
          stknum[upto-2]=using_machine.asc1((int)stknum[upto-1]);
		} else {		  
          stktype[upto-2]=ST_NUM;
          stknum[upto-2]=0;
	    }
        if (verbose) { System.out.printf("calculating the ascii of %s to be %f\n",stkstring[upto-1],stknum[upto-2]); }
        return;
      } else if (function.equals("petunconvert") || function.equals("petuncnv")) {
		if (using_machine!=null) {
          stktype[upto-2]=ST_NUM;
          stknum[upto-2]=using_machine.chrD1((int)stknum[upto-1]);
	    } else {
          stktype[upto-2]=ST_NUM;
          stknum[upto-2]=0;
	    }
        return;
      } else if (function.equals("metaread")) { // meta read data stream
        // VERY special function to say, we dont read anything else and we also get our values from a machine function
        // dummy setting - but we also set a special flag to indicate this??
        stktype[upto-2]=ST_DATASTREAM;
        return;
      } else if (function.equals("metainput")) { // meta read data stream
        // VERY special function to say, we dont read anything else and we also get our values from a machine function
        // dummy setting - but we also set a special flag to indicate this??
        stktype[upto-2]=ST_INPUTSTREAM;
        return;
      } else if (function.equals("str$")) {
        if (parameters==1) {
          stktype[upto-2]=ST_STRING;
          if (stknum[upto-1]-(int)stknum[upto-1]==0.0) {
            stkstring[upto-2]=" "+(int)(stknum[upto-1]);
          } else {
            stkstring[upto-2]=" "+(new Double(stknum[upto-1]).toString());
          }
          return;
        }
      } else if (function.equals("frm")) {
        if (parameters==2) {
          stktype[upto-2]=ST_STRING;
          try {          
          stkstring[upto-2]=String.format(stkstring[upto-1],stknum[upto]);
          } catch(Exception e) {
            throw new EvaluateException("BAD FORMAT");              
          }
          return;
        }
      } else if (function.equals("mid$")) {
        if (parameters==3) {
          if (verbose) { System.out.printf("Calculating mid$\n"); }
          stktype[upto-2]=ST_STRING;
          try {          
            stkstring[upto-2]=stkstring[upto-1].substring((int)stknum[upto]-1,(int)stknum[upto]-1+(int)stknum[upto+1]);
          } catch(Exception e) {
            throw new EvaluateException("BAD SUBSTRING INDEX");              
          }
          return;
        } else if (parameters==2) {
          if (verbose) { System.out.printf("Calculating mid$\n"); }
          stktype[upto-2]=ST_STRING;
          try {          
            stkstring[upto-2]=stkstring[upto-1].substring((int)stknum[upto]-1,stkstring[upto-1].length());
          } catch(Exception e) {
            throw new EvaluateException("BAD SUBSTRING INDEX");              
          }
          return;
        } else {
          System.out.printf("?WRONG NUMBER PARAMETERS\n");
          throw new EvaluateException("WRONG NUMBER PARAMETERS");
        }
      } else if (function.equals("instr")) {
        if (parameters==2) {
          if (verbose) { System.out.printf("Calculating instr(\"%s\",\"%s\")\n",stkstring[upto-1],stkstring[upto]); }
          stktype[upto-2]=ST_NUM;
          stknum[upto-2]=0;
          for (int i=0; i<=stkstring[upto-1].length()-stkstring[upto].length(); ++i) {
            if (stkstring[upto-1].substring(i,i+stkstring[upto].length()).equals(stkstring[upto])) {
              stknum[upto-2]=i+1;
              break;
            }
          }
          return;
        } else {
          System.out.printf("?WRONG NUMBER PARAMETERS\n");
          throw new EvaluateException("WRONG NUMBER PARAMETERS");
        }
      } else if (function.equals("left$")) {
        if (parameters==2) {
          if (verbose) { System.out.printf("Calculating left$(\"%s\",%d)\n",stkstring[upto-1],(int)stknum[upto]); }
          stktype[upto-2]=ST_STRING;
          if ((int)stknum[upto]<=0) { stkstring[upto-2]=""; }
          else if ((int)stknum[upto]>stkstring[upto-1].length()||(int)stknum[upto]<0) { stkstring[upto-2]=stkstring[upto-1]; }
          else { stkstring[upto-2]=stkstring[upto-1].substring(0,(int)stknum[upto]); }
          return;
        } else {
          System.out.printf("?WRONG NUMBER PARAMETERS\n");
          throw new EvaluateException("WRONG NUMBER PARAMETERS");
        }
      } else if (function.equals("right$")) {
        if (parameters==2) {
          if (verbose) { System.out.printf("Calculating right$(\"%s\",%d)\n",stkstring[upto-1],(int)stknum[upto]); }
          stktype[upto-2]=ST_STRING;
          if ((int)stknum[upto]<=0) { stkstring[upto-2]=""; }
          else if ((int)stknum[upto]>stkstring[upto-1].length()||(int)stknum[upto]<0) { stkstring[upto-2]=stkstring[upto-1]; }
          else { stkstring[upto-2]=stkstring[upto-1].substring(stkstring[upto-1].length()-(int)stknum[upto],stkstring[upto-1].length()); }
          return;
        } else {
          System.out.printf("?WRONG NUMBER PARAMETERS\n");
          throw new EvaluateException("WRONG NUMBER PARAMETERS");
        }
      } else {
        // have to assume that it is an array
        //System.out.printf("?SYTAX ERROR 003f *** Unknown/unsupported function %s\n",function);
        if (verbose) { System.out.printf("Assuming array %s\n",function); }
        // for an array, add the distinguishing trailing (
        GenericType value=get_value(function+"(",parameters,stknum[upto-1],stknum[upto],stknum[upto+1]);
        if (speeder_compile) { for (int i=0; i<parameters; ++i) compiled_obj=compiled_obj+"[]";
				       int v = using_machine.getvarindex(function+"(");
	  compiled_asm=save_compiled_asm+"  PSH mem: "+function;
          for (int i=0; i<parameters; ++i) compiled_asm+="[]";
	  compiled_asm+="="+String.valueOf(v)+"\n";
	      using_machine.petspeed.rewind();
	      using_machine.petspeed.addInstr(Petspeed.I_PSH | ((value.isNum())?Petspeed.T_Dbl:Petspeed.T_Str) | 
	        ((parameters<=1)?Petspeed.M_MEMARR1:Petspeed.M_MEMARR2)
		, v);
	}
        if (value.isNum()) {
          if (verbose) { System.out.printf("%sGot value %s\n",printprefix,value.print()); }
          // push this value on stack
          //pushNum(value.num());
          stktype[upto-2]=ST_NUM;
          stknum[upto-2]=value.num();
          return;
        } else {
          if (verbose) { System.out.printf("%sGot value %s\n",printprefix,value.print()); }
          //pushString(value.str());
          stktype[upto-2]=ST_STRING;
          stkstring[upto-2]=value.str();
          return;
        }
      }
    
    } else {
      if (lefttype==ST_NUM && righttype==ST_NUM) {
        answer=calc(left,oper,right);
      } else {
        String answerstr="";
        // it will return a string
        if (lefttype==ST_STRING && righttype==ST_NUM) {
          answerstr=calc(stkstring[upto-2],oper,right);
        } else if (lefttype==ST_NUM && righttype==ST_STRING) {
          answerstr=calc(left,oper,stkstring[upto-1]);
        } else if (lefttype==ST_STRING && righttype==ST_STRING) {
          GenericType ganswer=calc(stkstring[upto-2],oper,stkstring[upto-1]);
          if (ganswer.isNum()) {
            stktype[upto-2]=ST_NUM;
            stknum[upto-2]=ganswer.num();
            return;
          } else {
            stktype[upto-2]=ST_STRING;
            stkstring[upto-2]=ganswer.str();
            return;
          }
        } else { /* there is none! */ }
        stktype[upto-2]=ST_STRING;
        stkstring[upto-2]=answerstr;
        return;
      }
    }
    // now done inside here!
    stktype[upto-2]=ST_NUM;
    stknum[upto-2]=answer;
    return;         //return answer; 
  }

// precedence order of operators 

  int prec(String oper) {
    oper=oper.toLowerCase(); //20060204pgs
         if (oper.equals(",")) { return 1; } // allows me to use setOp without changing code!
    else if (oper.equals("===")) { return 2; } // or should this be lower than , ??
    else if (oper.equals("or")) { return 4; }
    else if (oper.equals("xor")) { return 5; }
    else if (oper.equals("and")) { return 6; }
    else if (oper.equals("not")) { return 7; }
    else if (oper.equals(">")) { return 8; }
    else if (oper.equals("<")) { return 8; }
    else if (oper.equals("<=")) { return 8; }
    else if (oper.equals("=<")) { return 8; }
    else if (oper.equals(">=")) { return 8; }
    else if (oper.equals("=>")) { return 8; }
    else if (oper.equals("!=")) { return 8; }
    else if (oper.equals("=")) { return 8; }
    else if (oper.equals("==")) { return 8; }
    else if (oper.equals("<>")) { return 8; }
    else if (oper.equals("+")) { return 9; }
    else if (oper.equals("-")) { return 9; }
    else if (oper.equals("*")) { return 10; }
    else if (oper.equals("/")) { return 10; }
    else if (oper.equals("-ve")) { return 11; } // note -reversing the order!! this might break things
    else if (oper.equals("^")) { return 12; }
    else if (oper.equals("X")) { return 0; }
    else if (oper.equals("(")) { return 0; }
    else if (oper.equals("")) { return 0; }
    return 0;
  }

  String calc(String leftstr, String oper, double right) {
    return leftstr;
  }

  String calc(double left, String oper, String rightstr) {
    return rightstr;
  }

  GenericType calc(String leftstr, String oper, String rightstr) throws EvaluateException {
    oper=oper.toLowerCase(); //20060204pgs
    if (speeder_compile) { if (!oper.equals("(")) compiled_obj+=",$"+oper;
      if (!oper.equals("(")) {
	    compiled_asm+="  PRF Str "+oper+"\n";
	    // if (!oper.equals("(")) System.out.printf(",%s",oper);
	      int ft=Petspeed.ftoken(oper);
                if (verbose && ft<0) System.out.printf("A-COMPILER could not find %s (okay if array)\n",oper);
	    using_machine.petspeed.addInstr(Petspeed.I_PRF | Petspeed.T_Str | ft);
      }
    }
    // returns a number
         if (oper.equals(">")) { return new GenericType((leftstr.compareTo(rightstr)>0)?-1.0:0.0); }
    else if (oper.equals("<")) { return new GenericType((rightstr.compareTo(leftstr)>0)?-1.0:0.0); }
    //else if (oper.equals("<=")) { answer=(left<=right)?-1.0:0.0; }
    //else if (oper.equals("=<")) { answer=(left<=right)?-1.0:0.0; } // for c64
    //else if (oper.equals(">=")) { answer=(left>=right)?-1.0:0.0; }
    //else if (oper.equals("=>")) { answer=(left>=right)?-1.0:0.0; } // for c64
    else if (oper.equals("=")) { return new GenericType(leftstr.equals(rightstr)?-1.0:0.0); }
    else if (oper.equals("<>")) { return new GenericType(leftstr.equals(rightstr)?0.0:-1.0); }

    // returns string
    else if (oper.equals("+")) { return new GenericType(leftstr+rightstr); }
    //else if (oper.equals("-")) { answer=left-right; }
    //else if (oper.equals("*")) { answer=left*right; }
    //else if (oper.equals("/")) { answer=left/right; }
    //else if (oper.equals("^")) { answer=Math.pow(left,right); }
    //else if (oper.equals("-ve")) { answer=(-right); }

    //else if (oper.equals("X")) { answer=left; } // no calc
    //else if (oper.equals("(")) { answer=right; }
    //else if (oper.equals("")) { answer=left; }
    System.out.printf("?ILLEGAL OPERATION : %s %s %s\n",leftstr,oper,rightstr);
    throw new EvaluateException("ILLEGAL OPERATION : "+leftstr+" "+oper+" "+rightstr);
    //return new GenericType(); // this is actually an error
  }

  double calc(double left, String oper, double right) throws EvaluateException {
    oper=oper.toLowerCase(); //20060204pgs
    if (speeder_compile) { if (!oper.equals("(")) compiled_obj+=","+oper;
      if (!oper.equals("(")) {
	    compiled_asm+="  PRF Dbl "+oper+"\n";
	    // if (!oper.equals("(")) System.out.printf(",%s",oper);
	      int ft=Petspeed.ftoken(oper);
                if (verbose && ft<0) System.out.printf("A-COMPILER could not find %s (okay if array)\n",oper);
	    using_machine.petspeed.addInstr(Petspeed.I_PRF | Petspeed.T_Dbl | ft);
      }
    }
    double answer=0.0;
    // old boolean like way, lets do a c64 bit like way now
         //if (oper.equals("or")) { answer=(((left!=0.0)?true:false) || ((right!=0.0)?true:false))?-1.0:0.0; }
    //else if (oper.equals("and")) { answer=(((left!=0.0)?true:false) && ((right!=0.0)?true:false))?-1.0:0.0; }
         if (oper.equals("or")) { answer=(double)(((int)(left)) | ((int)(right))); }
    else if (oper.equals("xor")) { answer=(double)(((int)(left)) ^ ((int)(right))); }
    else if (oper.equals("and")) { answer=(double)(((int)(left)) & ((int)(right))); }
    else if (oper.equals("not")) { answer=(double)(~(int)(right)); }
    else if (oper.equals(">")) { answer=(left>right)?-1.0:0.0; }
    else if (oper.equals("<")) { answer=(left<right)?-1.0:0.0; }
    else if (oper.equals("<=")) { answer=(left<=right)?-1.0:0.0; }
    else if (oper.equals("=<")) { answer=(left<=right)?-1.0:0.0; } // for c64
    else if (oper.equals(">=")) { answer=(left>=right)?-1.0:0.0; }
    else if (oper.equals("=>")) { answer=(left>=right)?-1.0:0.0; } // for c64
    else if (oper.equals("=")) { answer=(left==right)?-1.0:0.0; } // for c64
    else if (oper.equals("<>")) { answer=(left!=right)?-1.0:0.0; } // for c64
    else if (oper.equals("+")) { answer=left+right; }
    else if (oper.equals("-")) { answer=left-right; }
    else if (oper.equals("*")) { answer=left*right; }
    else if (oper.equals("/")) { answer=left/right; }
    else if (oper.equals("^")) { answer=Math.pow(left,right); }
    else if (oper.equals("-ve")) { answer=(-right); }
    else if (oper.equals("X")) { answer=left; } // no calc
    else if (oper.equals("(")) { answer=right; }
    else if (oper.equals("")) { answer=left; }
    else {
      System.out.printf("?SYNTAX ERROR 003o\n***Unsupported oper \"%s\"\n",oper);
      //answer=NaN;
      answer=0.0;
      throw new EvaluateException("SYNTAX ERROR : UNSUPPORTED OPERATOR : \""+oper+"\"");
    }
    if (verbose) { System.out.printf("%sCalculating %f %s %f, Answer = %f\n",printprefix,left,oper,right,answer); }
    if (verbose) { show_state(); }
    return answer;
  }

  GenericType get_value(String variablename, int params, double p1, double p2, double p3) {
    if (using_machine!=null) {
      return using_machine.getvariable(variablename.toLowerCase(),params,(int)p1,(int)p2,(int)p3); // convert back to uppercase
    }
    return new GenericType(0.0);
  }

  GenericType get_value(String variablename) {
    if (using_machine!=null) {
      // -
      //return using_machine.getvariable(variablename.toUpperCase()); // convert back to uppercase
      // added this here as we get issues when using a and A
      return using_machine.getvariable(variablename.toLowerCase()); // convert back to uppercase
    }
    variablename=variablename.toLowerCase(); //20060204pgs
    if (variablename.equals("pi")) {
      return new GenericType(Math.PI);
    } else if (variablename.equals("x")) {
      return new GenericType(7.0);
    } else if (variablename.equals("a")) {
      return new GenericType(1.0);
    }
    return new GenericType(0.0);
  }

  void pushString(String thestring) {
    stktype[upto]=ST_STRING; // new stack for typeing
    stkstring[upto]=thestring; // new stack just for strings
    upto++;
    doing=D_OP;
    // at this point we have filled the stknum[upto-1] element, but NOT the stkop[upto-1]
    // thus if doing==D_OP we have NOT filled stkop[upto-1], but we HAVE filled stnum[upto-1]
  }

  void pushNum(double num) {
    stknum[upto]=num;  stktype[upto]=ST_NUM; // seems to be the only place we do it
    upto++;
    doing=D_OP;
    // at this point we have filled the stknum[upto-1] element, but NOT the stkop[upto-1]
    // thus if doing==D_OP we have NOT filled stkop[upto-1], but we HAVE filled stnum[upto-1]
  }

  void pushOp(String op) {
    stknum[upto]=0.0; // we are skipping the num
    stktype[upto]=ST_NUM; // needed to make -ve work, (it was keeping the string setting)
                          // this function is only used by -ve and bracket (which doesnt look at this variable)
    stkop[upto]=op;
    upto++;
    doing=D_NUM; // if you are going to push an op, it is ( and therefore next is num
  }

  void setOp(String op) throws EvaluateException {
    if (verbose) { System.out.printf("%sGot %s oper\n",printprefix,op); }
    if (upto==0) {
      System.out.printf("?STACK ERROR  ** tried to set current operator stack when empty\n");
      // would this ever happen?
      throw new EvaluateException("STACK ERROR");
      //return; // cant do this
    }
    // now come the tricks, as soon as this op has a lower or equal precedence,
    // pop and calc the two above!
    while (upto>1 && prec(op)<=prec(stkop[upto-2])) {
      if (stkop[upto-2].equals("===") || stkop[upto-2].equals("===,") ) {
        break; // go no further that the equals as well, well do it after this!
      }
      if (stkop[upto-2].equals(OP_OPEN_BRACKET) || stkop[upto-2].equals(OP_COMMA)) {
        break; // go no further that the brackets or the ","
      }
      calc_and_pop(); //overhead of func passed too
    }
    // try this
    if (partialmatching && op==OP_COMMA && (upto==1 || upto==2 && stkop[upto-2].equals("==="))) {
      if (verbose) { System.out.printf("I think we should bomb out here\n"); }
      if (!partialmatching) {
        /* ... */
        throw new EvaluateException("TOP LEVEL COMMA ERROR");
      } else {
        parse_restart=ispnt; // because we are currently on the next invalid char
        return;
      }
    }
    stkop[upto-1]=op;
    doing=D_NUM;
  }

  double readNum() throws EvaluateException {
          // look ahead, this will look ahead one more character to see if it is NOT part of the number
          String building=a;
          boolean last_e=false;
          while (ispnt<intstring.length()-1) {

            a=intstring.substring(ispnt+1,ispnt+2);
            if (a.compareTo("0")>=0 && a.compareTo("9")<=0 || a.equals(".") || a.equalsIgnoreCase("e") 
              || last_e && (a.equals("-") || a.equals("+"))) {
              building=building+a;
              if (a.equalsIgnoreCase("e")) last_e=true; else last_e=false;
            } else { break; }
            ispnt++;
          }
          double value;
          if (building.equals(".")) { // allow just a dot for a number (C64 like)
            value=0.0;
          } else {
            /* for example, double decimal point might break it */
            try {          
              value=Double.parseDouble(building);
            } catch(Exception e) {
              throw new EvaluateException("BAD FORMAT FLOAT");              
            }
          
          }
          if (verbose) { System.out.printf("%sGot value %f\n",printprefix,value); }
    return value;
  }

// before returning true - if we are at the end - we should error out (as it should not end in an operator
// for the next to functions

  boolean readStringOpAlpha() throws EvaluateException {
          String building=a;
          while (ispnt<intstring.length()-1) {
            a=intstring.substring(ispnt+1,ispnt+2);
            if (a.compareToIgnoreCase("a")>=0 && a.compareToIgnoreCase("z")<=0) {
              building=building+a;
            } else { break; }
            ispnt++;
            // if we got or or and, then break out
            if (building.equalsIgnoreCase("or") || building.equalsIgnoreCase("and")) {
              break;
            }
          }
          if (verbose) { System.out.printf("%sgot a string %s\n",printprefix,building); }
          if (building.equalsIgnoreCase("or") || building.equalsIgnoreCase("and")) {
            setOp(building);
            return true;
          } else {
            if (!partialmatching) { 
              System.out.printf("?SYNTAX ERROR *** not a valid operator %s\n",building);
              throw new EvaluateException("SYNTAX ERROR : NOT A VALID OPERATOR : "+building);
            }
            return false;
          }
  }
  boolean readStringOp() throws EvaluateException {
          String building=a;
          while (ispnt<intstring.length()-1) {
            a=intstring.substring(ispnt+1,ispnt+2);
            if (a.equals("=") || a.equals("<") || a.equals(">")) {
              building=building+a;
            } else { break; }
            ispnt++;
          }
          if (verbose) { System.out.printf("%sgot a string %s\n",printprefix,building); }
          if (building.equals("=") && doing==D_ASSIGN || building.equals("===")) { // allows special === when not really expecting assignment! special case for me only!
            // we should have already known this was coming
            if (verbose) { System.out.printf("%sfound an assignment operator\n",printprefix); }
            setOp("==="); // special setting for an assignment // doesnt change anything
            return true;
          } else if (building.equals("=") || building.equals(">") || building.equals("<") 
              || building.equals(">=") || building.equals("<=")
              || building.equals("=>") || building.equals("=<")
              || building.equals("=") || building.equals("<>")
          ) {
            setOp(building);
            return true;
          } else {
            if (!partialmatching) { 
              System.out.printf("?SYNTAX ERROR *** not a valid operator %s\n",building); 
              throw new EvaluateException("SYNTAX ERROR : NOT A VALID STRING OPERATOR : "+building);
            }
            return false;
          }
  }

  String readQuotedString() {
          String building=""; // dont keep __a__ the quote
          while (ispnt<intstring.length()-1) {
            a=intstring.substring(ispnt+1,ispnt+2);
            if (a.equals("\"")) {
              ispnt++;
              break;
            }
            building+=a;
            ispnt++;
          }
    if (verbose) { System.out.printf("Got quoted string %s\n",building); }
    return building;
  }

  void readString() {
          boolean using_defined_function=false;
          String building=a;
            //System.out.printf("About to test for NOT, a=\"%s\" intstring=\"%s\" ispnt=%d\n",a,intstring,ispnt,(ispnt<intstring.length()-3)?"true":"false");
            if (a.equalsIgnoreCase("n") && ispnt<intstring.length()-2 && intstring.substring(ispnt,ispnt+3).equalsIgnoreCase("not")) {
              //note, if we find an imbedded or or and, we must pop it off, and stop processing!
              if(doing==D_OP) {
                // this is wrong - error out, not sure if this is right spot!
                //throw new EvaluateException("SYNTAX ERROR : INCORRECT NOT PLACEMENT");
                if (verbose) { System.out.printf("NOT in wrong spot\n"); }
                return;
              }
              pushOp("not");
              building="";
              if (verbose) { System.out.printf("Got the **not** operator\n"); }
              ispnt+=2; // is this right?
              // and do we continue into the while?
              return;
            }
          while (ispnt<intstring.length()-1) {
            // expensive - think of a better way! - trying to make it a bit better - dont know if it helps
            a=intstring.substring(ispnt+1,ispnt+2);

            // here we must ALSO look for the "fn" keyword
            // if we get this keyword, flag it, chew up the spaces and keep going!
            // if this is a define (and assignment) we must NOT expand the parameters
            // e.g. 'DEF' FN F(X)=2*X^2+5*X
            //            ^^   ^
            // should try and make this better, this would exclude fn from appearing in a variable
            // although we wont use it for defining, we will however for :
            //  A=FNF(77.0)+2
            //
            if (a.equalsIgnoreCase("n") && ispnt<intstring.length()-1 && intstring.substring(ispnt,ispnt+2).equalsIgnoreCase("fn")) {
              if (verbose) { System.out.printf("Got function use\n"); }
              ispnt++; // skip n
              // chew spaces ( we are in look ahead by 1  mode here)
              while (ispnt<intstring.length()-1) {
                a=intstring.substring(ispnt+1,ispnt+2);
                if (!a.equals(" ")) {
                  break;
                }
                ispnt++;
              }
              building=""; // clear this
              //if (verbose) { System.out.printf("Next character = %s\n",a); }
              using_defined_function=true;

	      if (speeder_compile) // until we implement it!
	          using_machine.petspeed.reject();
	      
            }

            if (a.equalsIgnoreCase("o") && ispnt<intstring.length()-2 && intstring.substring(ispnt+1,ispnt+3).equalsIgnoreCase("or")) {
              //note, if we find an imbedded or or and, we must pop it off, and stop processing!
              break;
            }
            if (a.equalsIgnoreCase("a") && ispnt<intstring.length()-3 && intstring.substring(ispnt+1,ispnt+4).equalsIgnoreCase("and")) {
              //note, if we find an imbedded or or and, we must pop it off, and stop processing!
              break;
            }
            if (a.equalsIgnoreCase("n") && ispnt<intstring.length()-3 && intstring.substring(ispnt+1,ispnt+4).equalsIgnoreCase("not")) {
              //note, if we find an imbedded or or and, we must pop it off, and stop processing!
              // this is an error!!!!
              break;
            }
            // not sure if this ignore case is right
            if (a.compareToIgnoreCase("a")>=0 && a.compareToIgnoreCase("z")<=0 || a.compareTo("0")>=0 && a.compareTo("9")<=0) {
              building=building+a;
            } else if (a.equals("$") || a.equals("%")) {
              building=building+a;
              ispnt++;
              break;
            } else { break; }
            ispnt++;
          }
          if (verbose) { System.out.printf("%sgot a string %s\n",printprefix,building); }
          // if the next char is ( then it is a function, otherwise it is a variable
          if (ispnt<intstring.length()-1 && (intstring.substring(ispnt+1,ispnt+2)).equals(OP_OPEN_BRACKET)) {
            if (verbose) { System.out.printf("%sgot a function %s\n",printprefix,building); }
            //functionname=building; // we KNOW that the bracket is coming, so we can be tricky and place it there already
            if (using_defined_function) {
              if (verbose) { System.out.printf("It is a defined function (prefixing fn)\n"); }
              stkfunc[upto]="fn"+building; // note, we are writing passed the end of the reference at the moment
              is_defining_function=true;
            } else {
              stkfunc[upto]=building; // note, we are writing passed the end of the reference at the moment
            }
            if (verbose) { System.out.printf("%sPre-Setting functionname to %s at stknum[(upto=)%d]\n",printprefix,stkfunc[upto],upto); }
            is_function=true;
            // now, this could be a function, or an array!
            return; // because we DONT want to set doing=D_OP
          } else {
            if (verbose) { System.out.printf("%sgot a variable %s\n",printprefix,building); }
              // now, special case, if we are on upto=0 and we have an assignment then DONT calculate
              // but keep this (somewhere) and get ready to set it

            // we want to be sure that we are at the base level:
            // that means either upto=0 or EVERY one up is a comma
            //for (int p=0; p<upto; p++) {
              //if (!stkop[p].equals(OP_COMMA) && !stkop[p].equals("===")) { is_baselevel=false; }
            //}
            boolean is_baselevel=false;
            if (upto==0 || stkop[upto-1].equals("===,")) { is_baselevel=true; }
            //if (g_is_assignment && upto==0) {
            if (g_is_assignment && is_baselevel) {
               // if it is an assignment AND dont have anything here already, we keep the variable
               // say, keep it in the stack num=n/a op="===" string=variable name
               if (verbose) { System.out.printf("Not resolving the variable, keeping it\n"); }
               stkop[upto]="===";
               stkfunc[upto]=building; // keep the variable
               upto++;
               doing=D_ASSIGN;
               return;

            } 
              else if (g_is_assignment && upto==1 && stkop[upto-1].equals(OP_OPEN_BRACKET) && is_defining_function) { // we are in a defining function
    
              // special case where we find that we are defining a function AND we are within the brackets               if (verbose) { System.out.printf("Not resolving the variable, keeping it BECAUSE we are going to use it as a parameter in a function definition\n"); }
               stkop[upto]="=f=";  // what should this be?
               stkfunc[upto]=building; // keep the variable
               stktype[upto]=ST_PARAM; // because we leave this as OP, we mark it as a special param
               upto++;
               doing=D_OP;
               // as soon as we close bracket (and get =) - the rest of the string is kept verbatim
               // as a special string which is usable as a function resolution
               // variable strings
               // fnFFF_param="A"
               // fnFFF_function="2*A+1"
               return;
            }
            is_defining_function=false; // turn it off

            // replace the variable with the value of the variable
              GenericType value=get_value(building);
	      if (speeder_compile) { compiled_obj+=","+building;
				       int v = using_machine.getvarindex(building.toLowerCase());
		compiled_asm+="  PSH "+ (value.isNum()?"Dbl":"Str") + " MEM: "+building+"="+String.valueOf(v)+"\n";
                // System.out.printf(",%s",building);
		if (v<0) {
		  if (verbose) System.out.printf("A-COMPILER could not find variable %s\n",building);
	          int ft=Petspeed.ftoken(building);
		    if (ft<0) System.out.printf("A-COMPILER could not find variable %s or special function variable\n",building);
		 // assuming this is a special variable, we will treat it like a function with no params
	          using_machine.petspeed.addInstr(Petspeed.I_FNC | ft);
		} else
	          using_machine.petspeed.addInstr(Petspeed.I_PSH | ((value.isNum())?Petspeed.T_Dbl:Petspeed.T_Str) | Petspeed.M_MEM,v);
	      }
              if (value.isNum()) {
                if (verbose) { System.out.printf("%sGot value %s\n",printprefix,value.print()); }
                // push this value on stack
                pushNum(value.num());
              } else {
                pushString(value.str());
              }
              doing=D_OP;
            //is_function=false; // probably no need to set this as it should normally sit on false!
          }
          doing=D_OP;  // moved this to here // !is_function
  }

  GenericType interpret_string_partial(String intstring_param) throws EvaluateException {
    partialmatching=true;
    stayonline=false;
    return interpret_string(intstring_param, false, 0.0, 0);
  }

  GenericType interpret_string_with_assignment(String intstring_param) throws EvaluateException {
    partialmatching=false;
    return interpret_string(intstring_param, false, 0.0, 1);
  }

  GenericType interpret_string_with_assignment_dt(String intstring_param) throws EvaluateException {
    partialmatching=false;
    return interpret_string(intstring_param, false, 0.0, 2);
  }

  GenericType interpret_string(String intstring_param) throws EvaluateException {
    partialmatching=false;
    return interpret_string(intstring_param, false, 0.0, 0);
  }

  GenericType interpret_string(String intstring_param, double expecting) throws EvaluateException {
    partialmatching=true; //for now???
    return interpret_string(intstring_param, true, expecting, 0);
  }


  // added the is_assignment, if this is true, then it will expect the first OUTER = sign to be the assignment operator
  // it also means that the FIRST variable be it array or singleton should NOT be evaluated!
  // note, we could have A(X+5,Y-B=3)=5
  boolean g_is_assignment;
  GenericType interpret_string(String intstring_param, boolean testing, double expecting, int is_assignment) 
    throws EvaluateException
  {

    if (speeder_compile) { compiled_obj=""; compiled_asm=""; }
    is_defining_function=false;
    parse_restart=(-1); // means no restart required // really could be zero too!
    g_is_assignment=is_assignment>0;
    inspect_datatype=is_assignment==2;   
    upto=0; // nothing is on the stack, upto points past the end of the current array, at the NEXT point
    doing=D_NUM;
    //stkop[0]=""; // this is a special case where we simple return a value, but have passed in is_assigment=true
    // i.e. 6  instead of X=6

    intstring=intstring_param; // this may be a dumb way, but it will make code more readable
    // try without // intstring=intstring.toLowerCase(); // opposite of statement.java!

    if (verbose && using_machine!=null) { System.out.printf("Running evaluate with a machine attached\n"); }

    if (verbose) { System.out.printf("%s>>Interpreting %s\n",printprefix,intstring); }

  try {
    /* take it a character at a time */
    for (ispnt=0; ispnt<intstring.length() ;++ispnt) {
      if (verbose) { 
        System.out.printf("%sGot character %s (index %d) [upto=%d, doing=%d]\n",
          printprefix,intstring.substring(ispnt,ispnt+1),ispnt,upto,doing);
      }
      a=intstring.substring(ispnt,ispnt+1);
      if (a.equals(" ")) { continue; }
      else if (doing==D_NUM /* looking for number */) {
        if (a.equals(OP_OPEN_BRACKET)) {
          pushOp(a);
          if (is_function) { // by seeing this flag, we know we have stored the function already in the stack
            is_function=false;
          } else { stkfunc[upto-1]=""; } // must clear it as there is no function

        // to implement
        //  else if CLOSING BRACKET // we either have no parameters or a  syntax error like: "1+)"

        } else if (a.equals("+")) {
          // totally ignore the + sign
          //pushOp("+ve");
        } else if (a.equals("-")) {
          // push the sign on
          pushOp("-ve");
        } else if (a.compareTo("0")>=0 && a.compareTo("9")<=0 || a.equals(".")) { // allow leading . for numbers
          double num=readNum();
	  if (speeder_compile) { compiled_obj+=","+String.valueOf(num);
		  compiled_asm+="  PSH Dbl IMM "+String.valueOf(num)+"\n";
	    //System.out.printf(",%.9f ",num);
	          using_machine.petspeed.addInstr(Petspeed.I_PSH | Petspeed.T_Dbl | Petspeed.M_IMM, num);
          }
          pushNum(num); 
          doing=D_OP;
        // not sure if this ignore case is right
        } else if (a.compareToIgnoreCase("a")>=0 && a.compareToIgnoreCase("z")<=0) {
          readString();
          //if (!is_function) { doing=D_OP; } // done in readString now
        } else if (a.equals("\"")) {
          // quoted string
          String qs=readQuotedString();
          pushString(qs); // we push, not a number, but a string
          if (speeder_compile) { compiled_obj+=",\""+qs+"\"";
		  compiled_asm+="  PSH Str IMM "+"\""+qs+"\"\n";
	          //System.out.printf(",\"%s\"",qs);
		  //System.out.printf("DEBUG: %d | %d | %d = %d\n",Petspeed.I_PSH, Petspeed.T_Str , Petspeed.M_IMM,
	            //Petspeed.I_PSH | Petspeed.T_Str | Petspeed.M_IMM);
	          using_machine.petspeed.addInstr(Petspeed.I_PSH | Petspeed.T_Str | Petspeed.M_IMM,qs);
          }
          doing=D_OP;
        } else {
          if (!partialmatching) {
            System.out.printf("?SYNTAX ERROR 001\n***Not correct syntax interpreting:%s\n",intstring);
            // new - stop parsing the expression
            throw new EvaluateException("SYNTAX ERROR AT "+intstring);
          }
          if (verbose) { System.out.printf("Parsed up to %d at char %s\n",ispnt,a); }
          if (ispnt==0) {
            // I believe we have a special case of a non-evaluatable expression right
            // from the start - lets error out!
            throw new EvaluateException("SYNTAX ERROR AT "+intstring);
          }
          parse_restart=ispnt; // because we are currently on the next invalid char
          break;
        }
        if (verbose) { show_state(); }
      } else if (doing==D_OP) {
        if (a.equals("^") || a.equals("*") || a.equals("/") || a.equals("+") || a.equals("-")) {
          setOp(a);
        } else if (a.equals(OP_CLOSE_BRACKET)) {
          parameters=1;
          if (verbose) { System.out.printf("%sCLOSING BRACKET from doing=%d\n",printprefix,doing); }
           // pop eveything off until we get to an op of ( and then pop that too, but stop there
          while (upto>1) {
            if (stkop[upto-2].equals(OP_COMMA)) {
              // if we get a comma, then we have a parameter to use in a function
              // keep a note of it
              if (verbose) { System.out.printf("%sFOUND COMMA\n",printprefix); }
              // this is a wierd one, keep going back, when we do the calc we use all of the other numbers
              upto--;
              parameters++;
              continue;
            }
            if (stkop[upto-2].equals(OP_OPEN_BRACKET)) {
              // special case - we are assigning, how do we know? there are no other brackets
              //for (int p=0; p<upto-2; p++) {
                //if (!stkop[p].equals(OP_COMMA) && !stkop[p].equals("===")) { is_baselevel=false; }
              //}
              boolean is_baselevel=false;
              if (upto==2 || stkop[upto-3].equals("===,")) { is_baselevel=true; }
              //if (g_is_assignment && upto-2==0) { // maybe a better way?
              if (g_is_assignment && is_baselevel) { // maybe a better way?
                if (verbose) { System.out.printf("I believe this is an array assignment\n"); }
                // you cannot calculate it, in fact, you must move the stack BACK past all the
                // parameters / not done yet
		upto+=parameters-1;
                // read the operator it SHOULD be an =
                doing=D_ASSIGN; //special case, the next one SHOULD be an assignment "=" sign
                //setOp("===");
		//doing=D_OP; // actually - really looking for the special operator of =
                break;
              }
              // pop this too, but end
              calc_and_pop();
              doing=D_OP; // efectively poping the op
              break;
            }
            calc_and_pop(); //overhead of func passed too

boolean dontallowextraclosingbrackets=true; // here for now
            if (dontallowextraclosingbrackets && upto<=1) { // then we are about to exit - but never got a brack
              // may not deal with the commas properly
              System.out.printf("?INCORRECT NUMBER OF BRACKETS - could not find openning bracket - extra close\n");
              throw new EvaluateException("INCORRECT NUMBER OF BRACKETS - EXTRA CLOSE");
            }

          } // end while
          // should check that we really finished on an open bracket and didnt have an extra one

          // allow also multi charactor operators OR and AND
        } else if (a.equals(OP_COMMA)) {
          // comma separators allowed within brackets, when you hit a comma, calculate everything back to the
          // brackets, note, I'm using setOp to do this and if prec of , is lower than all else it will do what I want
          parse_restart=(-1);
          setOp(OP_COMMA);
           /** COMMA **/
          if (parse_restart>=0) {
            /* can only get here if setOp flagged it as bad */
            break; /* ? */
          }
          
        // allow also multi charactor operators OR and AND
        } else if (a.compareToIgnoreCase("a")>=0 && a.compareToIgnoreCase("z")<=0) {
          int save_ispnt=ispnt;
          if (!readStringOpAlpha()) {
            // new - stop parsing the expression
            if (!partialmatching) { // this may never get here now because an error is thrown
              System.out.printf("?SYNTAX ERROR 010\n***Not correct syntax\n");
              throw new EvaluateException("SYNTAX ERROR 010 *** Not correct syntax");
            }
            if (verbose) { System.out.printf("Parsed up to %d at char %s\n",save_ispnt,a); }
            parse_restart=save_ispnt; // rewinding back to the save point
            break;
          }
        } else if (a.equals("=") || a.equals("<") || a.equals(">")) {
          int save_ispnt=ispnt;
          if (!readStringOp()) {
            // new - stop parsing the expression
            if (!partialmatching) { // this may never get here now because an error is thrown
              System.out.printf("?SYNTAX ERROR 011\n***Not correct syntax\n");
              throw new EvaluateException("SYNTAX ERROR 011 *** Not correct syntax");
            }
            if (verbose) { System.out.printf("Parsed up to %d at char %s\n",save_ispnt,a); }
            parse_restart=save_ispnt; // rewinding back to the save point
            break;
          }
        } else {
          if (!partialmatching) {
            System.out.printf("?SYNTAX ERROR 002\n***Not correct syntax interpreting:%s\n",intstring);
            throw new EvaluateException("SYNTAX ERROR : INVALID CHARACTER");
          }
          // new - stop parsing the expression
          if (verbose) { System.out.printf("Parsed up to %d at char %s\n",ispnt,a); }
          parse_restart=ispnt;
          break;
        }
        if (verbose) { show_state(); }
        // end D_OP
      } else if (doing==D_ASSIGN) {
        if (a.equals("=")) {
          readStringOp();
          /* special case */
          // if we are defining a function - the rest of this goes into a storage location in the machine and we finish up - not processing anything else
          if (is_defining_function) {
            if (verbose) { System.out.printf("We are going to ignore the rest of the string and finish\n"); }
            if (upto==2) { // in time, well add all parameters and join them together like this A,B,C,D
              if (verbose) { System.out.printf("Rest of string:%s\n", intstring.substring(ispnt+1,intstring.length())); }
              if (verbose) { System.out.printf("Should set fn_%s_param=%s\n",stkfunc[0],stkfunc[1]); }
              if (verbose) { System.out.printf("Should set fn_%s_function=%s\n",stkfunc[0],intstring.substring(ispnt+1,intstring.length())); }
              if (using_machine!=null) {
                using_machine.setvariable("fn_"+stkfunc[0].toLowerCase()+"_param",new GenericType(stkfunc[1]));
                using_machine.setvariable("fn_"+stkfunc[0].toLowerCase()+"_function",new GenericType(intstring.substring(ispnt+1,intstring.length())));
              }
            } else {
              // dont know how to trigger this error!
              System.out.printf("?SYNTAX ERROR in defined function - stack has incorrect number of params\n");
              throw new EvaluateException("INCORRECT PARAMETERS IN DEFINED FUNCTION");
            }
            return new GenericType();
          }
        } else if (a.equals(OP_COMMA)) {
          // we can now list assignment targets
          setOp("===,"); // note, we now change this to be a series of ===s (to differentiate from index ,s
                         // yet another which means assignment, with a comma
           /** COMMA **/
        } else {
          // we have a problem
          System.out.printf("?ASSIGNMENT ERROR\n");
          throw new EvaluateException("ASSIGNMENT ERROR");
        }
        if (verbose) { show_state(); }
      }
    } /* for */

    /* final calc */
    if (verbose) { System.out.printf("%sFINAL CALCS\n",printprefix); }

    // I *think* if we get here and we are on D_NUM with upto>0 then we have ended on an operator (or function) - and are therefore
    // in error???
    if (doing==D_NUM && upto>0) {
      System.out.printf("?SYNTAX ERROR : ENDS IN OPERATOR\n");
      throw new EvaluateException("SYNTAX ERROR : ENDS IN OPERATOR");
    }

    boolean really_has_assignment=false;
    while (upto>1) {
      // the only things that will need the function in it will be badly closed brackets, what is the overhead to do this?
      //add func for unclosed brackets
      // notice, if we find an opening bracket, we just calculate through it, like we are closing all the brackets
      // this is not really what we want normally in a program, but in a calculator we do
      if (stkop[upto-2].equals("===") || stkop[upto-2].equals("===,")) {
        break; // go no further that the equals as well, well do it after this!
      }
      if (stkop[upto-2].equals(OP_COMMA)) {
        break; // go no further that the ","
      }
boolean dontallowunbalancedopeningbracket=true; // here for now
      if (dontallowunbalancedopeningbracket && stkop[upto-2].equals(OP_OPEN_BRACKET)) {
        System.out.printf("?UNBALANCED BRACKETS - too many open brackets - not closed\n");
        throw new EvaluateException("UNBLANANCED BRACKETS - NOT CLOSED");
      }
      calc_and_pop();
    }
    if (g_is_assignment) {
      // note there is an issue with singleton returns that this would fail without g_is_assignment
      // add the special little bit to stop looking at a string that is not initialised
      //for (int p=0; p<upto-((doing==D_OP)?1:0); ++p) {
      // really, we never need to look at the top one unless we have done: A=  without a result at the end
      for (int p=0; p<upto-1; ++p) {
        if (stkop[p].equals("===") || stkop[p].equals("===,")) {
          really_has_assignment=true;
          break;
        }
      }
      if (really_has_assignment) { ProcessAssignment(); }
      else throw new EvaluateException("SYNTAX ERROR"); // no assignment to anything
    }

  } catch (EvaluateException evalerror) {
     System.out.printf("Caught Evaluate Error: %s\n",evalerror.getMessage());
     throw new EvaluateException(evalerror.getMessage());
  }

    if (upto==0) {
      // return empty string
      return new GenericType("");
    }
    /* print the answer */
    if (verbose) {
      show_state();
      System.out.printf("-------------------------------\n");
      System.out.printf("Evaluated %-20s  ",intstring);
      if (!testing) {
        if (stktype[0]==ST_NUM) {
          System.out.printf("%sanswer=%22.20f\n",printprefix,stknum[0]);
        } else {
          // add the quotes in too
          System.out.printf("%sanswer=\"%s\"\n",printprefix,stkstring[0]);
        }
      } else {
        System.out.printf("%sanswer=%22.20f  expecting=%12f  difference=%12f",printprefix,stknum[0],expecting,stknum[0]-expecting);
        if (stknum[0]-expecting>0.00001 || stknum[0]-expecting<-0.00001) {
          System.out.printf(" !!**BAD**!!\n");
          System.out.printf("\n   ***************DISCREPENCY***************\n");
          System.out.printf("-------------------------------\n\n");
        } else {
          System.out.printf(" OKAY\n");
          System.out.printf("-------------------------------\n\n");
        }
      }
    } else {
      if (!testing) {
        if (stktype[0]==ST_NUM) {
          if (!quiet) { System.out.printf("Evaluated %-20s = %f\n",intstring,stknum[0]); }
        } else {
          // add the quotes in too
          if (!quiet) { System.out.printf("Evaluated %-20s = \"%s\"\n",intstring,stkstring[0]); }
        }
      } else {
        if (stktype[0]==ST_NUM) {
          if (stknum[0]-expecting>0.00001 || stknum[0]-expecting<-0.00001) {
            //System.out.printf("Evaluated %-20s = \"%s\"",intstring,stkstring[0]);
            System.out.printf("%sanswer=%22.20f  expecting=%12f  difference=%12f",printprefix,stknum[0],expecting,stknum[0]-expecting);
            System.out.printf(" !!**BAD**!!\n");
            System.out.printf("   ***************DISCREPENCY***************\n");
          } else {
            System.out.printf(" OKAY\n"); // keep this
          }
        } else {
          // add the quotes in too
          if (!quiet) { System.out.printf("Evaluated %-20s = \"%s\"\n",intstring,stkstring[0]); }
        }
      }
    }

    // to keep the simple numeric expression simple - (like IF xxxx ) then just don't push anything
    if (verbose) { System.out.printf("upto=%d\n",upto); }
    GenericType gt; //only allow list of 
    if (stktype[0]==ST_NUM) {
      gt=new GenericType(stknum[0]);
      if (upto>1) // if it is a singleton - don't bother recording anthing!
        if (0==upto-1 || !stkop[0].equals("===") && !stkop[0].equals("===,")) 
          using_machine.petspeed.addInstr(Petspeed.I_PRF | Petspeed.T_Dbl); // flag a double
    } else {
      if (verbose) { System.out.printf("Returning a string type from evaluate\n"); }
      gt=new GenericType(stkstring[0]);
      if (upto>1) // if it is a singleton - don't bother recording anthing!
        if (0==upto-1 || !stkop[0].equals("===") && !stkop[0].equals("===,")) 
          using_machine.petspeed.addInstr(Petspeed.I_PRF | Petspeed.T_Str); // flag a string
    }
    // if there are more, add to it
    for (int i=1; i<upto; ++i) {
      if (stktype[i]==ST_NUM) {
        gt.add(stknum[i],upto);
        if (upto>1) // if it is a singleton - don't bother recording anthing!
          if (i==upto-1 || !stkop[i].equals("===") && !stkop[i].equals("===,")) 
            using_machine.petspeed.addInstr(Petspeed.I_PRF | Petspeed.T_Dbl); // flag a double
      } else {
        gt.add(stkstring[i],upto);
        if (upto>1) // if it is a singleton - don't bother recording anthing!
          if (i==upto-1 || !stkop[i].equals("===") && !stkop[i].equals("===,")) 
            using_machine.petspeed.addInstr(Petspeed.I_PRF | Petspeed.T_Str); // flag a string
      }
    }
    return gt;
    //return stknum[0]; // we cant do this now, we must return maybe a string (even if it is a number)
  }

// there are two ways to get data  from the evaluate stack - or the special case where
// we get the data from the machine itself
// this could be from an input string or reading data statements etc
// one way to do this would be to have a special thing at the top of the stack (or function or whatever)
// which will then go and get the values from else where (ie. the machine)
// READ A$,B,C%,D
// DATAxxx,999,999,999
// an example would be  A$,B,C%,D=METAREADDATASTREAM()
// the parser would also have to leave this function on the stack - and not compute it
// to get the data..another way would be to have the entire data in one string (with CR to separate lines)
// and a offset to the current reading part
///   // but only parse the string as required 
///   int prefill(int index) {
///     // this is a tricky thing that will read the next variable JUST IN TIME into the stack
///     // how do we know whether to treat it as a string or a number???
///     return index;
///   }
int reads_from_stream;
boolean inspect_datatype;

GenericType ReadValue(int stypeindex, int index) throws EvaluateException {
if (verbose) { System.out.printf("in 000\n"); }
  if (reads_from_stream>0) {
if (verbose) { System.out.printf("in 001\n"); }
    // we should NOT return the type in the stack because
    // we didnt set it! actually we set it to a special type
    // if ST_NUM, read it as a num, else read it as a string
    // to continue with the wierd theme below, we could (should?) 
    // return the value with both a string and num version
    // and based on the assignment, it will take what it needs
    // // dodgy work around GET the variable first, then set it
    // GenericType gt=getvariable(
    // no - another dodgy work around, preinspect the variable name to determine the type to be returned
    // we now get returned a pointer to the variable name - so lets look at it
    int end=stkfunc[stypeindex].length()-1;
    String lastchar=stkfunc[stypeindex].substring(end,end+1);
    if (using_machine==null) {
      if (lastchar.equals("$")) {
        return new GenericType("TESTONLY");
      } else { 
        return new GenericType(10.5); // test only
      }
    } else {
if (verbose) { System.out.printf("in 002\n"); }
      if (lastchar.equals("$")) {
if (verbose) { System.out.printf("in 003\n"); }
        // demanding a GenericType from the machine (we dont care we it gets it from)
        if (reads_from_stream==ST_DATASTREAM)
          return using_machine.metareaddatastreamString();
        else
          return using_machine.metainputstreamString();
      } else { 
        if (reads_from_stream==ST_DATASTREAM)
          return using_machine.metareaddatastreamNum();
        else
          return using_machine.metainputstreamNum();
      }
    }
  } else {
    boolean dotypecast_tonum=false;
    if (inspect_datatype) {
      if (verbose) { System.out.printf("Inspecting datatype\n"); }
      int end=stkfunc[stypeindex].length()-1;
      String lastchar=stkfunc[stypeindex].substring(end,end+1);
      if (!lastchar.equals("$")) {
        if (verbose) { System.out.printf("  not a string\n"); }
        dotypecast_tonum=true;
      }
    }
    // we return the type that it is in the stack
    // ignore stype passed,:
    int stype=stktype[index]; // i think this was an undected bug! - it was getting given rubbish types, but it seemed to work!
    // is this because it ignores it anyway and uses the type of the string?
    if (stype==ST_NUM) {
      if (verbose) { System.out.printf("%f\n",stknum[index]); }
      return new GenericType(stknum[index]);
    } else {
      if (verbose) { System.out.printf("%s\n",stkstring[index]); }
      if (dotypecast_tonum) {
        try {
          return new GenericType(Double.parseDouble(stkstring[index]));
        } catch (Exception e) {
          //return new GenericType(0.0); // should throw an error
          throw new EvaluateException("not numeric");
        }
      } else
        return new GenericType(stkstring[index]);
    }
  }
}

void ProcessAssignment() throws EvaluateException { 
      // try and see if it can be made to go faster for the singleton case
      //verbose=true;
      reads_from_stream=0;
      if (upto>0 && (stktype[upto-1]==ST_DATASTREAM || stktype[upto-1]==ST_INPUTSTREAM)) {
        if (verbose) { System.out.printf("We need to read from a datastream\n"); }
        reads_from_stream=stktype[upto-1];
      }
      if (verbose) { System.out.printf("Yes, we really have an assignment!\n"); }
      // lets SET the variable, we expect the top of the stack is the num or string, the rest is what we set!
      //if (stkop[upto-2].equals("===")   // we expect this
      // we now have quite a complicated stack of things to assign,

      // go down the stack to find the last assignment operator, and take a note
      int firstvalue=upto;
      int p=upto-1-1;
      while (p>=0) {
        // it would be good to count the available values but we wont
        if (stkop[p].equals("===")) {
          firstvalue=p+1;
          break;
        }
        p--;
      }
      if (firstvalue==upto) { 
         if (verbose) { System.out.printf("?ASSIGNMENTFINALERROR - cant find the last assignment\n"); }
         throw new EvaluateException("ASSIGNMENT FINAL ERROR");
      }

      if (verbose) { 
        System.out.printf("upto=%d and firstvalue=%d\n",upto,firstvalue);
      }
      // best to go UP the stack and inspect
      int currentvalue=firstvalue;
      int parameters=0; // singleton
      int stackvar=0;
      for (int stackp=0; stackp<firstvalue; ++stackp) {
        if (verbose) { System.out.printf("stackp=%d\n",stackp); }
        if (stkop[stackp].equals("===") || stkop[stackp].equals("===,")) {
          // we have therefore a variable to assign
          if (verbose) { System.out.printf("parameters %d\n",parameters); }
          if (parameters==0) {
            // simple setting
            GenericType rv=ReadValue(stackp,currentvalue++);
            if (using_machine!=null) {
              if (verbose) { 
                System.out.printf("Wanting to set %s = %s\n",stkfunc[stackp],rv.print());
                  //ReadValue(stackp,currentvalue).print()
                //);
              }
              using_machine.setvariable(stkfunc[stackp].toLowerCase(),rv);
              //using_machine.setvariable(stkfunc[stackp].toLowerCase(),ReadValue(stktype[stackp+1],currentvalue++));
	      if (speeder_compile) { compiled_obj+="."+stkfunc[stackp].toLowerCase();
				       int v = using_machine.getvarindex(stkfunc[stackp].toLowerCase());
		      compiled_asm+="  STO "+ (rv.isNum()?"Dbl":"Str") +" MEM: "+stkfunc[stackp].toLowerCase()+"="+String.valueOf(v)+"\n";
		      // System.out.printf(".%s\n",stkfunc[stackp].toLowerCase());
		      // System.out.printf("RPNALG: STO -> %s\n",stkfunc[stackp].toLowerCase());
	          using_machine.petspeed.addInstr(Petspeed.I_STO | ((rv.isNum())?Petspeed.T_Dbl:Petspeed.T_Str) | Petspeed.M_MEM,v);
	      }
            } else {
              if (verbose) {
                System.out.printf("stackp=%d Would have set %s to %s\n",stackp,stkfunc[stackp].toLowerCase(),rv.print());
              }
            }
          } else { 
            if (verbose) { System.out.printf("array %d\n",parameters); }
            if (verbose) { System.out.printf("%sFINAL ARRAY SETTING\n",printprefix); }
              if (verbose) { System.out.printf("Wanting to set array %s ( (params=%d.. %f,%f,%f) to value = %f\n",
                stkfunc[stackvar],parameters,stknum[stackvar+1],stknum[stackvar+2],stknum[stackvar+3],
                stknum[currentvalue]); }
              if (using_machine!=null) {
                GenericType rv=ReadValue(stackvar,currentvalue++);
                using_machine.setvariable(stkfunc[stackvar].toLowerCase()+"(",parameters,(int)stknum[stackvar+1],(int)stknum[stackvar+2],(int)stknum[stackvar+3],rv); //ReadValue(stackvar,currentvalue++));
                //using_machine.setvariable(stkfunc[stackvar].toLowerCase()+"(",parameters,(int)stknum[stackvar+1],(int)stknum[stackvar+2],(int)stknum[stackvar+3],ReadValue(stktype[0],currentvalue++));
	      if (speeder_compile) { compiled_obj+="."+stkfunc[stackvar].toLowerCase();
		                       for (int i=0; i<parameters; ++i) 
		                         compiled_obj+="[]";
				       int v = using_machine.getvarindex(stkfunc[stackvar].toLowerCase()+"(");
		      compiled_asm+="  STO MEM: "+stkfunc[stackvar].toLowerCase();
		                       for (int i=0; i<parameters; ++i) 
		                         compiled_asm+="[]";
		                       compiled_asm+="="+String.valueOf(v)+"\n";
		                         //compiled_obj+="["+String.valueOf((int)stknum[stackvar+i+1])+"]"; // no this doesn't make sense - we are a compiler!
		      // System.out.printf(".%s\n",stkfunc[stackp].toLowerCase());
		      // System.out.printf("RPNALG: STO -> %s\n",stkfunc[stackp].toLowerCase());
	          using_machine.petspeed.addInstr(Petspeed.I_STO | ((rv.isNum())?Petspeed.T_Dbl:Petspeed.T_Str) | ((parameters<=1)?Petspeed.M_MEMARR1:Petspeed.M_MEMARR2)
				  , v);
              }
	      }
          }
          parameters=0;
        } else if (stkop[stackp].equals(OP_OPEN_BRACKET)) {
          // we have an array
          parameters=1;
          stackvar=stackp;
        } else if (stkop[stackp].equals(OP_COMMA)) {
          /** COMMA **/
          /* should this be checked for the open bracket, e.g. if paraeters is 0 now, then this is wrong ?? */
          parameters++;
        }
      }
    }

}
  class EvaluateException extends Exception {
    EvaluateException() {
    }
    EvaluateException(String msg) {
        super(msg);
    }
  }

/////////
// END //
/////////
