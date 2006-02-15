/////////////////////////////////////////////////////////////////////////////////
//
// $Id: evaluate.java,v 1.20 2006/02/15 01:52:47 pgs Exp pgs $
//
// $Log: evaluate.java,v $
// Revision 1.20  2006/02/15 01:52:47  pgs
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

  boolean is_function=false; // flag to indicate that the function name has been pre inserted in the array
  //String functionname="";  // temporarily used until the bracket is confirmed! - no longer required, prestore it!
  int parameters; // count of the number of parameters that a function is called with

  // parsing the string
  String intstring;
  int ispnt; // pointer into intstring
  String a; // temp string variable containing current pointed to char

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

  Machine using_machine=null;

  Random generator = new Random();

  evaluate(Machine machine)
  {
    using_machine=machine;
    //evaluate(); // this is repeated 
    if (false) { System.out.printf("Running evaluate\n"); }
    stknum=new double[100];
    stkop=new String[100];
    stkfunc=new String[100];

    // to allow typeing
    stktype=new int[100];
    stkstring=new String[100];

    // all setup ready
  }

  evaluate()
  {
    if (false) { System.out.printf("Running evaluate\n"); }
    stknum=new double[100];
    stkop=new String[100];
    stkfunc=new String[100];

    // to allow typeing
    stktype=new int[100];
    stkstring=new String[100];

    // all setup ready
  } // end func

  void show_state() {
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
  void calc_and_pop() {
    if (verbose) { System.out.printf("%sPerforming a calculation on %f %s %f\n",printprefix,stknum[upto-2],stkop[upto-2],stknum[upto-1]); }
    //stknum[upto-2]=calc(stknum[upto-2],stkop[upto-2],stknum[upto-1],stkfunc[upto-2]);

    //stknum[upto-2]=calc();
    //upto--;
    calc();
    upto--;

    // where ( - tried stknum[upto-2]=calc(stknum[upto-2],stkop[upto-2],stknum[upto-1]); //efficiency only - but no better - I think

    if (verbose) { show_state(); }
    return;
  }

  //double calc(double left, String oper, double right, String function) {
  // because we always call this with one and only one set (from calc_and_pop), we should make this only need the upto parameter
    //called by stknum[upto-2]=calc(stknum[upto-2],stkop[upto-2],stknum[upto-1],stkfunc[upto-2]);
    // upto is also implicit
  ////////////////////////////////////////////////////
  //
  ////////////////////////////////////////////////////
  // now does not return - perhaps should be a boolean
  void calc() {

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

      if (function.equals("sin")) {
        answer=Math.sin(right);
      } else if (function.equals("silly")) {
        if (parameters==3) {
          if (verbose) { System.out.printf("%susing params %f %f %f\n",printprefix,stknum[upto-1],stknum[upto],stknum[upto+1]); }
          answer=stknum[upto-1]+stknum[upto]*10.0+stknum[upto+1]*100.0;
        } else {
          System.out.printf("?SYNTAX ERROR 005 - wrong number of parameters - had %d params wanting %d\n",parameters,3);
          answer=0.0;
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
        answer=generator.nextDouble();
      } else if (function.equals("exp")) {
        answer=Math.exp(right);
      } else if (function.equals("cos")) {
        answer=Math.cos(right);
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
        } catch (Exception e) { stknum[upto-2]=0.0; }
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
        return;
      } else if (function.equals("spc")) { // they probably are different
        stktype[upto-2]=ST_STRING;
        String building="";
        for (int i=0; i<(int)stknum[upto-1]; ++i) {
          building+="(rght)";
        }
        stkstring[upto-2]=building;
        return;
      } else if (function.equals("pos")) {
        // tap into the machine - I think this gives the position on the screen?
        stktype[upto-2]=ST_NUM;
        stknum[upto-2]=0.0; // just for now - not implemented
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
        stktype[upto-2]=ST_NUM;
        stknum[upto-2]=(int)(stkstring[upto-1].charAt(0));
        return;
      } else if (function.equals("chr$")) {
        stktype[upto-2]=ST_STRING;
        stkstring[upto-2]=""+(char)stknum[upto-1];
        return;
      } else if (function.equals("metards")) { // meta read data stream
        // VERY special function to say, we dont read anything else and we also get our values from a machine function
        // dummy setting - but we also set a special flag to indicate this??
        stktype[upto-2]=ST_DATASTREAM;
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
      } else if (function.equals("mid$")) {
        if (parameters==3) {
          if (verbose) { System.out.printf("Calculating mid$\n"); }
          stktype[upto-2]=ST_STRING;
          stkstring[upto-2]=stkstring[upto-1].substring((int)stknum[upto]-1,(int)stknum[upto]-1+(int)stknum[upto+1]);
          return;
        } else if (parameters==2) {
          if (verbose) { System.out.printf("Calculating mid$\n"); }
          stktype[upto-2]=ST_STRING;
          stkstring[upto-2]=stkstring[upto-1].substring((int)stknum[upto]-1,stkstring[upto-1].length());
          return;
        } else {
          System.out.printf("?WRONG NUMBER PARAMETERS\n");
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
        }
      } else {
        // have to assume that it is an array
        //System.out.printf("?SYTAX ERROR 003f *** Unknown/unsupported function %s\n",function);
        if (verbose) { System.out.printf("Assuming array %s\n",function); }
        // for an array, add the distinguishing trailing (
        GenericType value=get_value(function+"(",parameters,stknum[upto-1],stknum[upto],stknum[upto+1]);
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

  int prec(String oper) {
    oper=oper.toLowerCase(); //20060204pgs
         if (oper.equals(",")) { return 1; } // allows me to use setOp without changing code!
    else if (oper.equals("===")) { return 2; } // or should this be lower than , ??
    else if (oper.equals("or")) { return 6; }
    else if (oper.equals("xor")) { return 6; }
    else if (oper.equals("and")) { return 7; }
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
    else if (oper.equals("^")) { return 11; }
    else if (oper.equals("-ve")) { return 12; }
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

  GenericType calc(String leftstr, String oper, String rightstr) {
    oper=oper.toLowerCase(); //20060204pgs
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
    return new GenericType(); // this is actually an error
  }

  double calc(double left, String oper, double right) {
    oper=oper.toLowerCase(); //20060204pgs
    double answer=0.0;
    // old boolean like way, lets do a c64 bit like way now
         //if (oper.equals("or")) { answer=(((left!=0.0)?true:false) || ((right!=0.0)?true:false))?-1.0:0.0; }
    //else if (oper.equals("and")) { answer=(((left!=0.0)?true:false) && ((right!=0.0)?true:false))?-1.0:0.0; }
         if (oper.equals("or")) { answer=(double)(((int)(left)) | ((int)(right))); }
    else if (oper.equals("xor")) { answer=(double)(((int)(left)) ^ ((int)(right))); }
    else if (oper.equals("and")) { answer=(double)(((int)(left)) & ((int)(right))); }
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

  void setOp(String op) {
    if (verbose) { System.out.printf("%sGot %s oper\n",printprefix,op); }
    if (upto==0) {
      System.out.printf("?STACK ERROR  ** tried to set current operator stack when empty\n");
      return; // cant do this
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
    stkop[upto-1]=op;
    doing=D_NUM;
  }

  double readNum() {
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
          double value=Double.parseDouble(building);
          if (verbose) { System.out.printf("%sGot value %f\n",printprefix,value); }
    return value;
  }

  boolean readStringOpAlpha() {
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
            //operatortoken=building;
            setOp(building);
            return true;
          } else {
            if (!partialmatching) { System.out.printf("?SYNTAX ERROR *** not a valid operator %s\n",building); }
            return false;
          }
  }
  boolean readStringOp() {
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
            if (!partialmatching) { System.out.printf("?SYNTAX ERROR *** not a valid operator %s\n",building); }
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
          String building=a;
          while (ispnt<intstring.length()-1) {
            // expensive - think of a better way! - trying to make it a bit better - dont know if it helps
            a=intstring.substring(ispnt+1,ispnt+2);
            if (a.equalsIgnoreCase("o") && ispnt<intstring.length()-2 && intstring.substring(ispnt+1,ispnt+3).equalsIgnoreCase("or")) {
              //note, if we find an imbedded or or and, we must pop it off, and stop processing!
              break;
            }
            if (a.equalsIgnoreCase("a") && ispnt<intstring.length()-3 && intstring.substring(ispnt+1,ispnt+4).equalsIgnoreCase("and")) {
              //note, if we find an imbedded or or and, we must pop it off, and stop processing!
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
            stkfunc[upto]=building; // note, we are writing passed the end of the reference at the moment
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
            // replace the variable with the value of the variable
              GenericType value=get_value(building);
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

  GenericType interpret_string_partial(String intstring_param) {
    partialmatching=true;
    return interpret_string(intstring_param, false, 0.0, false);
  }

  GenericType interpret_string_with_assignment(String intstring_param) {
    partialmatching=false;
    return interpret_string(intstring_param, false, 0.0, true);
  }

  GenericType interpret_string(String intstring_param) {
    partialmatching=false;
    return interpret_string(intstring_param, false, 0.0, false);
  }

  GenericType interpret_string(String intstring_param, double expecting) {
    partialmatching=true; //for now???
    return interpret_string(intstring_param, true, expecting, false);
  }


  // added the is_assignment, if this is true, then it will expect the first OUTER = sign to be the assignment operator
  // it also means that the FIRST variable be it array or singleton should NOT be evaluated!
  // note, we could have A(X+5,Y-B=3)=5
  boolean g_is_assignment;
  GenericType interpret_string(String intstring_param, boolean testing, double expecting, boolean is_assignment) {

    parse_restart=(-1); // means no restart required // really could be zero too!
    g_is_assignment=is_assignment;
    upto=0; // nothing is on the stack, upto points past the end of the current array, at the NEXT point
    doing=D_NUM;
    //stkop[0]=""; // this is a special case where we simple return a value, but have passed in is_assigment=true
    // i.e. 6  instead of X=6

    intstring=intstring_param; // this may be a dumb way, but it will make code more readable
    // try without // intstring=intstring.toLowerCase(); // opposite of statement.java!
    if (verbose) { System.out.printf("%s>>Interpreting %s\n",printprefix,intstring); }

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
          doing=D_OP;
        } else {
          if (!partialmatching) {
            System.out.printf("?SYNTAX ERROR 001\n***Not correct syntax\n");
            // new - stop parsing the expression
          }
          if (verbose) { System.out.printf("Parsed up to %d at char %s\n",ispnt,a); }
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
          }
          // allow also multi charactor operators OR and AND
        } else if (a.equals(OP_COMMA)) {
          // comma separators allowed within brackets, when you hit a comma, calculate everything back to the
          // brackets, note, I'm using setOp to do this and if prec of , is lower than all else it will do what I want
          setOp(OP_COMMA);
        // allow also multi charactor operators OR and AND
        } else if (a.compareToIgnoreCase("a")>=0 && a.compareToIgnoreCase("z")<=0) {
          int save_ispnt=ispnt;
          if (!readStringOpAlpha()) {
            // new - stop parsing the expression
            if (!partialmatching) {
              System.out.printf("?SYNTAX ERROR 010\n***Not correct syntax\n");
            }
            if (verbose) { System.out.printf("Parsed up to %d at char %s\n",save_ispnt,a); }
            parse_restart=save_ispnt; // rewinding back to the save point
            break;
          }
        } else if (a.equals("=") || a.equals("<") || a.equals(">")) {
          int save_ispnt=ispnt;
          if (!readStringOp()) {
            // new - stop parsing the expression
            if (!partialmatching) {
              System.out.printf("?SYNTAX ERROR 011\n***Not correct syntax\n");
            }
            if (verbose) { System.out.printf("Parsed up to %d at char %s\n",save_ispnt,a); }
            parse_restart=save_ispnt; // rewinding back to the save point
            break;
          }
        } else {
          if (!partialmatching) {
            System.out.printf("?SYNTAX ERROR 002\n***Not correct syntax\n");
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
        } else if (a.equals(OP_COMMA)) {
          // we can now list assignment targets
          setOp("===,"); // note, we now change this to be a series of ===s (to differentiate from index ,s
                         // yet another which means assignment, with a comma
        } else {
          // we have a problem
          System.out.printf("?ASSIGNMENT ERROR\n");
        }
        if (verbose) { show_state(); }
      }
    } /* for */

    /* final calc */
    boolean really_has_assignment=false;
    if (verbose) { System.out.printf("%sFINAL CALCS\n",printprefix); }
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
      if (stktype[0]==ST_NUM) {
        if (!quiet) { System.out.printf("Evaluated %-20s = %f\n",intstring,stknum[0]); }
      } else {
        // add the quotes in too
        if (!quiet) { System.out.printf("Evaluated %-20s = \"%s\"\n",intstring,stkstring[0]); }
      }
    }

    if (verbose) { System.out.printf("upto=%d\n",upto); }
    GenericType gt; //only allow list of 
    if (stktype[0]==ST_NUM) {
      gt=new GenericType(stknum[0]);
    } else {
      if (verbose) { System.out.printf("Returning a string type from evaluate\n"); }
      gt=new GenericType(stkstring[0]);
    }
    // if there are more, add to it
    for (int i=1; i<upto; ++i) {
      if (stktype[i]==ST_NUM) {
        gt.add(stknum[i]);
      } else {
        gt.add(stkstring[i]);
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
boolean reads_from_datastream;

GenericType ReadValue(int stypeindex, int index) {
  if (reads_from_datastream) {
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
      if (lastchar.equals("$")) {
        // demanding a GenericType from the machine (we dont care we it gets it from)
        return using_machine.metareaddatastreamString();
      } else { 
        return using_machine.metareaddatastreamNum();
      }
    }
  } else {
    // we return the type that it is in the stack
    // ignore stype passed,:
    int stype=stktype[index]; // i think this was an undected bug! - it was getting given rubbish types, but it seemed to work!
    // is this because it ignores it anyway and uses the type of the string?
    if (stype==ST_NUM) {
      if (verbose) { System.out.printf("%f\n",stknum[index]); }
      return new GenericType(stknum[index]);
    } else {
      if (verbose) { System.out.printf("%s\n",stkstring[index]); }
      return new GenericType(stkstring[index]);
    }
  }
}

void ProcessAssignment() { 
      // try and see if it can be made to go faster for the singleton case
      //verbose=true;
      reads_from_datastream=false;
      if (upto>0 && stktype[upto-1]==ST_DATASTREAM) {
        if (verbose) { System.out.printf("We need to read from a datastream\n"); }
        reads_from_datastream=true;
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
         return; 
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
            if (using_machine!=null) {
              if (verbose) { System.out.printf("Wanting to set %s = ",stkfunc[stackp]); }
              using_machine.setvariable(stkfunc[stackp].toLowerCase(),ReadValue(stackp,currentvalue++));
              //using_machine.setvariable(stkfunc[stackp].toLowerCase(),ReadValue(stktype[stackp+1],currentvalue++));
            } else {
              if (verbose) {
                System.out.printf("stackp=%d Would have set %s to ",stackp,stkfunc[stackp].toLowerCase(),ReadValue(stackp,currentvalue++));
              }
            }
          } else { 
            if (verbose) { System.out.printf("array %d\n",parameters); }
            if (verbose) { System.out.printf("%sFINAL ARRAY SETTING\n",printprefix); }
              if (verbose) { System.out.printf("Wanting to set array %s ( (params=%d.. %f,%f,%f) to value = %f\n",
                stkfunc[stackvar],parameters,stknum[stackvar+1],stknum[stackvar+2],stknum[stackvar+3],
                stknum[currentvalue]); }
              if (using_machine!=null) {
                using_machine.setvariable(stkfunc[stackvar].toLowerCase()+"(",parameters,(int)stknum[stackvar+1],(int)stknum[stackvar+2],(int)stknum[stackvar+3],ReadValue(stackvar,currentvalue++));
                //using_machine.setvariable(stkfunc[stackvar].toLowerCase()+"(",parameters,(int)stknum[stackvar+1],(int)stknum[stackvar+2],(int)stknum[stackvar+3],ReadValue(stktype[0],currentvalue++));
              }
          }
          parameters=0;
        } else if (stkop[stackp].equals(OP_OPEN_BRACKET)) {
          // we have an array
          parameters=1;
          stackvar=stackp;
        } else if (stkop[stackp].equals(OP_COMMA)) {
          parameters++;
        }
      }
    }

}
/////////
// END //
/////////
