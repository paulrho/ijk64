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
  static String OP_OPEN_BRACKET="(";
  static String OP_CLOSE_BRACKET=")";
  static String OP_COMMA=",";

  // debugging/formatting etc
  String printprefix="";
  boolean verbose=true;

  Machine using_machine=null;

  evaluate(Machine machine)
  {
    using_machine=machine;
    //evaluate(); // this is repeated 
    if (false) { System.out.printf("Running evaluate\n"); }
    stknum=new double[100];
    stkop=new String[100];
    stkfunc=new String[100];
    // all setup ready
  }

  evaluate()
  {
    if (false) { System.out.printf("Running evaluate\n"); }
    stknum=new double[100];
    stkop=new String[100];
    stkfunc=new String[100];
    // all setup ready
  } // end func

  void show_state() {
    System.out.printf("%s |STATE:  upto=%d doing=%d\n",printprefix,upto,doing);
    // necessarily there it is the case when doing==D_OP that we DON'T know what stkop[upto-1] is - therefore, don't show it!
    for (int levelx=0; levelx<upto; levelx++) {
      System.out.printf("%s |upto=%d stknum[]=%f stkop[]=%s%s%s\n",
        printprefix,levelx,stknum[levelx],(levelx!=upto-1 || doing!=D_OP)?stkop[levelx]:"N/A",(stkfunc[levelx]!=null)?" stkfunc[]=":"",(stkfunc[levelx]!=null)?stkfunc[levelx]:"");
    }
    System.out.printf("%s\n",printprefix);
  }

  // just a bit of shorthard to improve readability
  void calc_and_pop() {
    if (verbose) { System.out.printf("%sPerforming a calculation on %f %s %f\n",printprefix,stknum[upto-2],stkop[upto-2],stknum[upto-1]); }
    stknum[upto-2]=calc(stknum[upto-2],stkop[upto-2],stknum[upto-1],stkfunc[upto-2]);
      // where ( - tried stknum[upto-2]=calc(stknum[upto-2],stkop[upto-2],stknum[upto-1]); //efficiency only - but no better - I think
    upto--;
    if (verbose) { show_state(); }
    return;
  }

  double calc(double left, String oper, double right, String function) {
    double answer=0.0;
    if (oper.equals(OP_OPEN_BRACKET) && function != null && !function.equals("")) { 
      answer=right; 
      if (verbose) { System.out.printf("%sfunction needs calculating : %s(%f) we have %d parameters\n",printprefix,function,right,parameters); }
      // if it is a function the parameters are stored at upto-1, upto, upto+1, upto+2, upto+3... etc.
      // for a single parameter it is stored simply in upto-1
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
        answer=0.0; // NI yet
      } else if (function.equals("int")) {
        answer=0.0; // NI yet
      } else if (function.equals("abs")) {
        answer=0.0; // NI yet
      } else if (function.equals("rnd")) {
        answer=0.0; // NI yet
      } else if (function.equals("exp")) {
        answer=Math.exp(right);
      } else if (function.equals("cos")) {
        answer=Math.cos(right);
      } else if (function.equals("tan")) {
        answer=Math.tan(right);
      } else if (function.equals("sqrt")) {
        answer=Math.sqrt(right);
      } else if (function.equals("sqt")) { // for basic
        answer=Math.sqrt(right);
      } else {
        // have to assume that it is an array
        //System.out.printf("?SYTAX ERROR 003f *** Unknown/unsupported function %s\n",function);
        System.out.printf("Assuming array %s\n",function);
      }
    
    } else answer=calc(left,oper,right);
    return answer; 
  }

  int prec(String oper) {
         if (oper.equals(",")) { return 1; } // allows me to use setOp without changing code!
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
  double calc(double left, String oper, double right) {
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

  double get_value(String variablename) {
    if (using_machine!=null) {
      // -
      return using_machine.getvariable(variablename.toUpperCase()); // convert back to uppercase
    }
    if (variablename.equals("pi")) {
      return Math.PI;
    } else if (variablename.equals("x")) {
      return 7.0;
    } else if (variablename.equals("a")) {
      return 1.0;
    }
    return 0.0;
  }

  void pushNum(double num) {
    stknum[upto]=num;
    upto++;
    doing=D_OP;
    // at this point we have filled the stknum[upto-1] element, but NOT the stkop[upto-1]
    // thus if doing==D_OP we have NOT filled stkop[upto-1], but we HAVE filled stnum[upto-1]
  }

  void pushOp(String op) {
    stknum[upto]=0.0; // we are skipping the num
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
            if (a.compareTo("0")>=0 && a.compareTo("9")<=0 || a.equals(".") || a.equals("e") 
              || last_e && (a.equals("-") || a.equals("+"))) {
              building=building+a;
              if (a.equals("e")) last_e=true; else last_e=false;
            } else { break; }
            ispnt++;
          }
          double value=Double.parseDouble(building);
          if (verbose) { System.out.printf("%sGot value %f\n",printprefix,value); }
    return value;
  }

  void readStringOpAlpha() {
          String building=a;
          while (ispnt<intstring.length()-1) {
            a=intstring.substring(ispnt+1,ispnt+2);
            if (a.compareTo("a")>=0 && a.compareTo("z")<=0) {
              building=building+a;
            } else { break; }
            ispnt++;
            // if we got or or and, then break out
            if (building.equals("or") || building.equals("and")) {
              break;
            }
          }
          if (verbose) { System.out.printf("%sgot a string %s\n",printprefix,building); }
          if (building.equals("or") || building.equals("and")) {
            //operatortoken=building;
            setOp(building);
            return;
          } else {
            System.out.printf("?SYNTAX ERROR *** not a valid operator %s\n",building);
            return;
          }
  }
  void readStringOp() {
          String building=a;
          while (ispnt<intstring.length()-1) {
            a=intstring.substring(ispnt+1,ispnt+2);
            if (a.equals("=") || a.equals("<") || a.equals(">")) {
              building=building+a;
            } else { break; }
            ispnt++;
          }
          if (verbose) { System.out.printf("%sgot a string %s\n",printprefix,building); }
          if (building.equals("=") || building.equals(">") || building.equals("<") 
              || building.equals(">=") || building.equals("<=")
              || building.equals("=>") || building.equals("=<")
              || building.equals("=") || building.equals("<>")
          ) {
            setOp(building);
            return;
          } else {
            System.out.printf("?SYNTAX ERROR *** not a valid operator %s\n",building);
            return;
          }
  }

  void readQuotedString() {
          String building=a;
          while (ispnt<intstring.length()-1) {
            a=intstring.substring(ispnt+1,ispnt+2);
            if (a.equals("\"")) {
              ispnt++;
              break;
            }
            ispnt++;
          }
    return;
  }

  void readString() {
          String building=a;
          while (ispnt<intstring.length()-1) {
            // expensive - think of a better way! - trying to make it a bit better - dont know if it helps
            a=intstring.substring(ispnt+1,ispnt+2);
            if (a.equals("o") && ispnt<intstring.length()-2 && intstring.substring(ispnt+1,ispnt+3).equals("or")) {
              //note, if we find an imbedded or or and, we must pop it off, and stop processing!
              break;
            }
            if (a.equals("a") && ispnt<intstring.length()-3 && intstring.substring(ispnt+1,ispnt+4).equals("and")) {
              //note, if we find an imbedded or or and, we must pop it off, and stop processing!
              break;
            }
            if (a.compareTo("a")>=0 && a.compareTo("z")<=0 || a.compareTo("0")>=0 && a.compareTo("9")<=0) {
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
          } else {
            if (verbose) { System.out.printf("%sgot a variable %s\n",printprefix,building); }
            // replace the variable with the value of the variable
            double value=get_value(building);
            if (verbose) { System.out.printf("%sGot value %f\n",printprefix,value); }
            // push this value on stack
            pushNum(value);
            doing=D_OP;
            //is_function=false; // probably no need to set this as it should normally sit on false!
          }
  }

  double interpret_string(String intstring_param) {
    return interpret_string(intstring_param, false, 0.0);
  }

  double interpret_string(String intstring_param, double expecting) {
    return interpret_string(intstring_param, true, expecting);
  }

  double interpret_string(String intstring_param, boolean testing, double expecting) {

    upto=0; // nothing is on the stack, upto points past the end of the current array, at the NEXT point
    doing=D_NUM;

    intstring=intstring_param; // this may be a dumb way, but it will make code more readable
    intstring=intstring.toLowerCase(); // opposite of statement.java!
    if (true || verbose) { System.out.printf("%s>>Interpreting %s\n",printprefix,intstring); }

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
        } else if (a.compareTo("a")>=0 && a.compareTo("z")<=0) {
          readString();
          if (!is_function) { doing=D_OP; }
        } else if (a.equals("\"")) {
          // quoted string
          readQuotedString();
          pushNum(0.0); // for now - just a 0
          doing=D_OP;
        } else {
          System.out.printf("?SYNTAX ERROR 001\n***Not correct syntax\n");
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
        } else if (a.compareTo("a")>=0 && a.compareTo("z")<=0) {
          readStringOpAlpha();
        } else if (a.equals("=") || a.equals("<") || a.equals(">")) {
          readStringOp();
        } else {
          System.out.printf("?SYNTAX ERROR 002\n***Not correct syntax\n");
        }
        if (verbose) { show_state(); }
      } // end D_OP
    } /* for */

    /* final calc */
    if (verbose) { System.out.printf("%sFINAL CALCS\n",printprefix); }
    while (upto>1) {
      // the only things that will need the function in it will be badly closed brackets, what is the overhead to do this?
      //add func for unclosed brackets
      // notice, if we find an opening bracket, we just calculate through it, like we are closing all the brackets
      // this is not really what we want normally in a program, but in a calculator we do
      if (stkop[upto-2].equals(OP_COMMA)) {
        break; // go no further that the ","
      }
      calc_and_pop();
    }

    /* print the answer */
    if (verbose) {
      show_state();
      System.out.printf("-------------------------------\n");
      System.out.printf("Evaluated %-20s  ",intstring);
      if (!testing) {
        System.out.printf("%sanswer=%22.20f\n",printprefix,stknum[0]);
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
      System.out.printf("Evaluated %-20s = %f\n",intstring,stknum[0]);
    }

    return stknum[0];
  }
}

/////////
// END //
/////////
