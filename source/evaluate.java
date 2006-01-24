// why not - java will do

// make test harness of equations

// PGS20060124
// believe it or not, I programmed this without using any reference, and I don't actually fully understand how it works!!
// but it does!

class evaluate {

  String stkop[];
  double stknum[];
  int upto;
  String stkfunc[];
  String printprefix="";

  evaluate()
  {
    System.out.printf("Running evaluate\n");
    stknum=new double[100];
    stkop=new String[100];
    stkfunc=new String[100];

    interpret_string("(1+1)/(5+2)",2.0/7.0);
    interpret_string("(1+1)",2);
    interpret_string("+7*3*x^2-4*x^3-5*x^4",-12348);
    interpret_string("3+2*3^1+3",3+2*3+3);
    interpret_string("+1*2*3^2-2*3^1-3*3^3",2*9-2*3-3*27);
    interpret_string("+0.07*3*x^2-0.1*4*x^3-0.1*5*x^4",-1327.410000);
    interpret_string("7-4*1^1-5",+7-4*1-5);
    interpret_string("+0.7+0.1*2*x+0.07*3*x^2-0.1*4*x^3-0.1*5*x^4-0.09*6*x^5-0.02*x^6-0.001*8*x^7",0);
    interpret_string("1-4*3+2",1-4*3+2);
    interpret_string("1-4*x+2",1-4*7+2);
    interpret_string("1-4+2",1-4+2);
    interpret_string("+7+14+75-144*x-2",+7+14+75-144*7-2);
    // interpret_string("+0.743809523809524+0.1421825*2*x+0.07555555*3*x^2-0.1447222*4*x^3-2",-186.717828);
    // interpret_string("+0.743809523809524+0.1421825*2*x+0.07555555*3*x^2-0.1447222*4*x^3",0.0);
    // interpret_string("+0.743809523809524+0.1421825*2*x+0.07555555*3*x^2-0.1447222*4*x^3-0.19777*5*x^4",-2558.946678);
    // interpret_string("+0.743809523809524+0.1421825*2*x+0.07555555*3*x^2-0.1447222*4*x^3",0.0);
    // interpret_string("-0.19777*5*x^4",0.0);
    interpret_string("+0.743809523809524+0.1421825*2*x+0.07555555*3*x^2-0.1447222*4*x^3-0.19777*5*x^4-0.095555*6*x^5-0.02158*7*x^6-0.001904761*8*x^7",-42516.182634);
    interpret_string("6.6732e-11*1000000000000",6.6732e-11*1000000000000.0);
    interpret_string("5.98e24",5.98e24);
    interpret_string("(41.3-23.45)/(42.3+56.4)",(41.3-23.45)/(42.3+56.4));
    interpret_string("sqrt(4^2+3^2)+10",15);
    interpret_string("sqrt(4^2+3^2)+10",15);
    interpret_string("sqrt(4^2+3^2)",5);
    interpret_string("0.5*1.23*5^2*a",0.5*1.23*5*5);
    interpret_string("pi",Math.PI);
    interpret_string("2*pi",2*Math.PI);
    interpret_string("1-1.3",1-1.3);
    interpret_string("1--1",2);
    interpret_string("4.4+3.7",4.4+3.7);
    interpret_string("(1)+(2)+(3)",1+2+3);
    interpret_string("1+(5)^2",26);
    interpret_string("sin(1)+1",1.841471);
    interpret_string("sin(1)*2",2*0.841471);
    interpret_string("1+sin(1)",1.841471);
    interpret_string("2*sin(1)",2*0.841471);
    interpret_string("sin(1)",0.841471);
    interpret_string("sin(1)",0.841471);
    interpret_string("sin(1)+cos(2)+tan(3)",0.282777605186476321819);
    interpret_string("(1)+(2)",3);
    interpret_string("(1+4)+(2+9)",(1+4)+(2+9));
    interpret_string("17+12",17+12);
    interpret_string("4+5*2^2",4+5*4);
    interpret_string("(1+2+3+4+5+6)*4",4*(1+2+3+4+5+6));
    interpret_string("(1+2+3+4+5+6)*4",4*(1+2+3+4+5+6));
    interpret_string("(5)^2+1",26);
    interpret_string("1+(5)^2",26);
    interpret_string("(1)+(2*3)*4+(5)^2",1+2*3*4+25);
    interpret_string("1+(5)",6);
    interpret_string("(2*3)*4+(5)",(2*3)*4+5);
    interpret_string("(1)+(2*3)*4+(5)",1+2*3*4+5);
    interpret_string("(4+3*3)/2^3-7",-5.37500000000000000000);
    interpret_string("1+1",2);
    interpret_string("(1+1)",2);
    interpret_string("((1+1))",2);
    interpret_string("1+2*3^4",1+2*3*3*3*3);
    interpret_string("3^4*2+1",1+2*3*3*3*3);
    interpret_string("3^4+2^3",3*3*3*3+8);
    interpret_string("2*3",2*3);
    interpret_string("2^3",8);
    interpret_string("2*(3+4)",2*(3+4));
    interpret_string("(3+4)*2",2*(3+4));
    interpret_string("2+(3*4)",2+(3*4));
    interpret_string("(3*4)+2",2+(3*4));
    interpret_string("(3+4)/2",(3+4)/2.0);
    interpret_string("1+1+1",3);
    interpret_string("1+1+1+1",4);
    interpret_string("1+2*3*4",1+2*3*4);
    interpret_string("1+2*3*4+5",1+2*3*4+5);
    interpret_string("(1)+(2*3)*4+(5)",1+2*3*4+5);
    interpret_string("(1)+(2*3)*4+(5^2)",1+2*3*4+25);
    interpret_string("(1+2+3+4+5+6)",1+2+3+4+5+6);
    interpret_string("4*(1+2+3+4+5+6)",4*(1+2+3+4+5+6));
    interpret_string("(1+2+3+4+5+6)*4",4*(1+2+3+4+5+6));
    if (false) {
    }
  }

  void show_state() {
    System.out.printf("%sSTATE:  upto=%d\n",printprefix,upto);
    for (int levelx=0; levelx<upto; levelx++) {
      System.out.printf("%supto=%d stknum[]=%f stkop[]=%s\n",printprefix,levelx,stknum[levelx],stkop[levelx]);
    }
  }

  double calc(double left, String oper, double right, String function) {
    double answer=0.0;
    if (oper.equals("(") && !function.equals("")) { 
      answer=left; 
      System.out.printf("%sfunction needs calculating : %s(%f)\n",printprefix,function,left);
      if (function.equals("sin")) {
        answer=Math.sin(left);
      } else if (function.equals("cos")) {
        answer=Math.cos(left);
      } else if (function.equals("tan")) {
        answer=Math.tan(left);
      } else if (function.equals("sqrt")) {
        answer=Math.sqrt(left);
      } else if (function.equals("sqt")) { // for basic
        answer=Math.sqrt(left);
      } else {
        System.out.printf("%sUnknown/unsupported function %s\n",printprefix,function);
      }
    
    } else answer=calc(left,oper,right);
    return answer; 
  }

  int prec(String oper) {
    if (oper.equals("+")) { return 9; }
    else if (oper.equals("-")) { return 9; }
    else if (oper.equals("*")) { return 10; }
    else if (oper.equals("/")) { return 10; }
    else if (oper.equals("^")) { return 11; }
    else if (oper.equals("X")) { return 0; }
    else if (oper.equals("(")) { return 0; }
    else if (oper.equals("")) { return 0; }
    return 0;
  }
  double calc(double left, String oper, double right) {
    double answer=0.0;
    if (oper.equals("+")) { answer=left+right; }
    else if (oper.equals("-")) { answer=left-right; }
    else if (oper.equals("*")) { answer=left*right; }
    else if (oper.equals("/")) { answer=left/right; }
    else if (oper.equals("^")) { answer=Math.pow(left,right); }
    else if (oper.equals("X")) { answer=left; } // no calc
    else if (oper.equals("(")) { answer=left; }
    else if (oper.equals("")) { answer=left; }
    else {
      System.out.printf("?SYNTAX ERROR 003\n***Unsupported oper \"%s\"\n",oper);
      //answer=NaN;
      answer=0.0;
    }
    System.out.printf("%sCalculating %f %s %f, Answer = %f\n",printprefix,left,oper,right,answer);
    show_state();
    return answer;
  }

  double get_value(String variablename) {
    if (variablename.equals("pi")) {
      return Math.PI;
    } else if (variablename.equals("x")) {
      return 7.0;
    } else if (variablename.equals("a")) {
      return 1.0;
    }
    return 0.0;
  }

  String intstring;
  int ispnt; // pointer into intstring
  int doing;
  static int D_NUM=0;
  static int D_OP=1;
  String a; // temp string variable containing current pointed to char
  boolean is_function=false;

  void pushNum(double num) {
    stknum[upto]=num;
    upto++;
    doing=D_OP;
  }
  void pushOp(String op) {
    stknum[upto]=0.0; // we are skipping the num
    stkop[upto]=op;
    upto++;
    doing=D_NUM; // if you are going to push an op, it is ( and therefore next is num
  }
  void setOp(String op) {
    if (upto==0) {
      System.out.printf("?STACK ERROR  ** tried to set current operator stack when empty\n");
      return; // cant do this
    }
    // now come the tricks, as soon as this op has a lower or equal precedence,
    // pop and calc the two above!
    while (upto>1 && prec(op)<=prec(stkop[upto-2])) {
       
      System.out.printf("%sPerforming a calculation on %f %s %f\n",printprefix,stknum[upto-2],stkop[upto-2],stknum[upto-1]);

      if (stkop[upto-2].equals("(")) {
        break; // go no further that the brackets
      }
      double val=calc(stknum[upto-2],stkop[upto-2],stknum[upto-1]);
      stknum[upto-2]=val;
      upto--; // pop off stack one level
    }
    stkop[upto-1]=op;
    doing=D_NUM;
  }

  double readNum() {
          // look ahead?
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
          System.out.printf("%sGot value %f\n",printprefix,value);
    return value;
  }

  void readString() {
          String building=a;
          while (ispnt<intstring.length()-1) {
            a=intstring.substring(ispnt+1,ispnt+2);
            if (a.compareTo("a")>=0 && a.compareTo("z")<=0) {
              building=building+a;
            } else { break; }
            ispnt++;
          }
          System.out.printf("%sgot a string %s\n",printprefix,building);
          // if the next char is ( then it is a function, otherwise it is a variable
          if (ispnt<intstring.length()-1 && (intstring.substring(ispnt+1,ispnt+2)).equals("(")) {
            System.out.printf("%sgot a function %s\n",printprefix,building);
            //functionname=building;
            is_function=true;
          } else {
            System.out.printf("%sgot a variable %s\n",printprefix,building);
            // replace the variable with the value of the variable
            double value=get_value(building);
            System.out.printf("%sGot value %f\n",printprefix,value);
            // push this value on stack
            pushNum(value);
            doing=D_OP;
            is_function=false;
          }
  }

  void interpret_string(String intstring_param, double expecting) {
    String functionname="";

    upto=0; // nothing is on the stack, upto points past the end of the current array, at the NEXT point
    //stkop[level]=""; // not reqd
    //operfunction[level]=""; // not reqd
    doing=D_NUM;

    intstring=intstring_param; // this may be a dumb way, but it will make code more readable
    System.out.printf("%sInterpreting %s\n",printprefix,intstring);
    /* take it a character at a time */
    // i is now part of the object
    for (ispnt=0; ispnt<intstring.length() ;++ispnt) {
      System.out.printf("%sGot character %s (index %d) [upto=%d, doing=%d]\n",printprefix,intstring.substring(ispnt,ispnt+1),ispnt,upto,doing);
      a=intstring.substring(ispnt,ispnt+1);
      // it is all different now:
      
      if (doing==D_NUM /* looking for number */) {
        if (a.equals("(")) {
          pushOp(a); // (
        } else if (a.compareTo("0")>=0 && a.compareTo("9")<=0 || a.equals("-") || a.equals("+")) {
          double num=readNum();
          pushNum(num);
          doing=D_OP;
        } else if (a.compareTo("a")>=0 && a.compareTo("z")<=0) {
          readString();
          if (!is_function) { doing=D_OP; }
        } else {
          System.out.printf("?SYNTAX ERROR 001\n***Not correct syntax\n");
        }
        show_state();
      } else if (doing==D_OP) {
        if (a.equals("^")) {
          System.out.printf("%sGot ^ oper\n",printprefix);
          setOp(a);
        } else if (a.equals("*")||a.equals("/")) {
          System.out.printf("%sGot */ oper\n",printprefix);
          setOp(a);
        } else if (a.equals("+")||a.equals("-")) {
          System.out.printf("%sGot +- oper\n",printprefix);
          setOp(a);
        } else if (a.equals(")")) {
          System.out.printf("%sCLOSING BRACKET from doing=%d\n",printprefix,doing);
           // pop eveything off until we get to an op of ( and then pop that too
          while (upto>1) {
             
            System.out.printf("%sPerforming a calculation on %f %s %f\n",printprefix,stknum[upto-2],stkop[upto-2],stknum[upto-1]);
      
            if (stkop[upto-2].equals("(")) {
              // pop this too, but end
              stknum[upto-2]=stknum[upto-1];
              doing=D_OP; // efectively poping the op
              upto--;
              break;
            }
            double val=calc(stknum[upto-2],stkop[upto-2],stknum[upto-1]);
            stknum[upto-2]=val;
            upto--; // pop off stack one level
          }

          //setOp(a);
        }
        show_state();
      }
    } /* for */
    /* final calc */

    System.out.printf("%sFINAL CALCS\n",printprefix);
    while (upto>1) {
       
      System.out.printf("%sPerforming a calculation on %f %s %f\n",printprefix,stknum[upto-2],stkop[upto-2],stknum[upto-1]);

      double val=calc(stknum[upto-2],stkop[upto-2],stknum[upto-1]);
      stknum[upto-2]=val;
      upto--; // pop off stack one level
    }
    show_state();
    System.out.printf("-------------------------------\n");
    System.out.printf("Evaluated %-20s  ",intstring);
    System.out.printf("%sanswer=%12f  expecting=%12f  difference=%12f",printprefix,stknum[0],expecting,stknum[0]-expecting);
    //System.out.printf("%sanswer=%12f  expecting=%12f  difference=%12f",printprefix,left[level],expecting,left[level]-expecting);
    if (stknum[0]-expecting>0.0000001 || stknum[0]-expecting<-0.0000001) {
      System.out.printf(" !!**BAD**!!\n");
      System.out.printf("\n   ***************DISCREPENCY***************\n");
      System.out.printf("-------------------------------\n\n");
    } else {
      System.out.printf(" OKAY\n");
      System.out.printf("-------------------------------\n\n");
      //return 0;
    }

    return;
  }

  public static void main(String[] args) {
    System.out.printf("Mello Word\n");
    new evaluate();
  }
}

/////////
// END //
/////////
