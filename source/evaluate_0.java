// why not - java will do

// make test harness of equations

class evaluate {

  double left[];
  double right[];
  int prevdoing[];
  String oper[];
  String operfunction[];
  int operprec[];
  int level;
  int doing; // 0=left 1=oper 2=right
  String printprefix="";

  evaluate()
  {
    System.out.printf("Running evaluate\n");
    left=new double[100];
    right=new double[100];
    oper=new String[100];
    operprec=new int[100];
    operfunction=new String[100];
    prevdoing=new int[100];

    interpret_string("(1)+(2)+(3)",1+2+3);
    interpret_string("1+(5)^2",26);
    interpret_string("sin(1)",0.1);
    interpret_string("sin(1)",0.1);
    interpret_string("sin(1)+cos(2)+tan(3)",0.1);
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
  }

  void show_state() {
    System.out.printf("%sSTATE:  doing=%d\n",printprefix,doing);
    for (int levelx=0; levelx<=level; levelx++) {
      System.out.printf("%slevel=%d left[level]=%f oper[level]=%s right[level]=%f %s\n",printprefix,levelx,left[levelx],oper[levelx],right[levelx],(oper[levelx].equals("("))?operfunction[levelx]:"");
    }
  }

  double calc(double left, String oper, double right, String function) {
    double answer=0.0;
    if (oper.equals("(") && !function.equals("")) { 
      answer=left; 
      System.out.printf("%sfunction needs calculating : %s(%f)\n",printprefix,function,left);
    } else answer=calc(left,oper,right);
    return answer; 
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
    else {
      System.out.printf("?SYNTAX ERROR 003\n***Unsupported oper \"%s\"\n",oper);
      //answer=NaN;
      answer=0.0;
    }
    System.out.printf("%sCalculating %f %s %f, Answer = %f\n",printprefix,left,oper,right,answer);
    show_state();
    return answer;
  }

  double pop_level() {
    /* finished, come back up a level, after evaluating lower level */
    double answer=calc(left[level],oper[level],right[level],operfunction[level]);
    if (oper[level].equals("(") && !operfunction[level].equals("")) {
      System.out.printf("%sLevel %d, Calc involves a function %s\n",printprefix,level,operfunction[level]);
    }
    level--;
    //new:
    doing=prevdoing[level];
    printprefix=printprefix.substring(0,printprefix.length()-2);
    System.out.printf("%sLevel %d, Popping answer=%f\n",printprefix,level,answer);
    return answer;
  }

  int interpret_string(String intstring, double expecting) {
    String functionname="";

    System.out.printf("%sInterpreting %s\n",printprefix,intstring);

    level=0;
    doing=0;
    /* take it a character at a time */
    for (int i=0; i<intstring.length() ;++i) {
      System.out.printf("%sGot character %s (index %d) [doing=%d]\n",printprefix,intstring.substring(i,i+1),i,doing);
      String a=intstring.substring(i,i+1);
      if (doing==0 /* LEFT */) {
        if (a.equals("(")) {
          // left is not known yet, descend a level
          oper[level]="("; operprec[level]=0;
          operfunction[level]=functionname; // if the oper is ( and the operfunction is not "" then it will do it
          prevdoing[level]=doing; // now push state as well
          level++; oper[level]="";
          printprefix=printprefix+"  ";
          functionname=""; // disable it as a function
        } else if (a.equals("+")) {
          /* modifier or actual operator? */
        } else if (a.equals("-")) {
        } else if (a.compareTo("0")>=0 && a.compareTo("9")<=0) {
          // look ahead?
          String building=a;
          while (i<intstring.length()-1) {

            a=intstring.substring(i+1,i+2);
            if (a.compareTo("0")>=0 && a.compareTo("9")<=0) {
              building=building+a;
            } else { break; }
            i++;
          }
          double value=Double.parseDouble(building);
          System.out.printf("%sGot value %f\n",printprefix,value);
          left[level]=value;
          doing=1 /*oper*/;
        } else if (a.compareTo("a")>=0 && a.compareTo("z")<=0) {
          String building=a;
          while (i<intstring.length()-1) {
            a=intstring.substring(i+1,i+2);
            if (a.compareTo("a")>=0 && a.compareTo("z")<=0) {
              building=building+a;
            } else { break; }
            i++;
          }
          System.out.printf("%sgot a string %s\n",printprefix,building);
          // if the next char is ( then it is a function, otherwise it is a variable
          if (i<intstring.length()-1 && (intstring.substring(i+1,i+2)).equals("(")) {
            System.out.printf("%sgot a function %s\n",printprefix,building);
            functionname=building;
          } else {
            System.out.printf("%sgot a variable %s\n",printprefix,building);
          }
          
        } else {
          System.out.printf("?SYNTAX ERROR 001\n***Not correct syntax\n");
        }
      } else if (doing==2 /* RIGHT */) {
        if (a.equals("(")) {
          // here we need to push down two levels ?
          prevdoing[level]=doing; // now push state as well
          level++; printprefix=printprefix+"  ";
          oper[level]="("; operprec[level]=0;
          operfunction[level]=functionname; // if the oper is ( and the operfunction is not "" then it will do it
          show_state();
          prevdoing[level]=doing; // now push state as well
          level++; printprefix=printprefix+"  "; oper[level]="";
          show_state();
          doing=0;
          functionname=""; // disable it as a function
        } else if (a.equals("+")) {
          // fixme
        } else if (a.equals("-")) {
          // fixme
        } else if (a.compareTo("0")>=0 && a.compareTo("9")<=0) {
          // look ahead?
          String building=a;
          while (i<intstring.length()-1) {
            a=intstring.substring(i+1,i+2);
            if (a.compareTo("0")>=0 && a.compareTo("9")<=0) {
              building=building+a;
            } else { break; }
            i++;
          }
          
          double value=Double.parseDouble(building); //a
          System.out.printf("%sGot value %f\n",printprefix,value);
          right[level]=value;
          //doing=1 /*oper*/;
          doing=3 /* oper for second time */;

        // } else ... { // read variable name or function name - store up
        } else if (a.compareTo("a")>=0 && a.compareTo("z")<=0) {
          String building=a;
          while (i<intstring.length()-1) {
            a=intstring.substring(i+1,i+2);
            if (a.compareTo("a")>=0 && a.compareTo("z")<=0) {
              building=building+a;
            } else { break; }
            i++;
          }
          System.out.printf("%sgot a string %s\n",printprefix,building);
          // if the next char is ( then it is a function, otherwise it is a variable
          if (i<intstring.length()-1 && (intstring.substring(i+1,i+2)).equals("(")) {
            System.out.printf("%sgot a function %s\n",printprefix,building);
            functionname=building;
          } else {
            System.out.printf("%sgot a variable %s\n",printprefix,building);
          }
          
        } else {
          System.out.printf("?SYNTAX ERROR 001\n***Not correct syntax\n");
        }

      } else if (doing==1 /* OPER */) {
        if (a.equals("^")) {
          System.out.printf("%sGot ^ oper\n",printprefix);
          oper[level]=a; operprec[level]=11;
          doing=2 /*right*/;
        } else if (a.equals("*")||a.equals("/")) {
          System.out.printf("%sGot */ oper\n",printprefix);
          oper[level]=a; operprec[level]=10;
          doing=2 /*right*/;
        } else if (a.equals("+")||a.equals("-")) {
          System.out.printf("%sGot +- oper\n",printprefix);
          oper[level]=a; operprec[level]=9;
          doing=2 /*right*/;
        } else if (a.equals(")")) {
          System.out.printf("%sCLOSING BRACKET from doing=%d (confirm, doing=1)\n",printprefix,doing);
          // this one does not have an equation to calc
          show_state();
          oper[level]="("; operprec[level]=0; // just to trick it into a no calc
          operfunction[level]="";//?
          while(level>0) {
            double x=pop_level();
            left[level]=x;
            if (oper[level].equals("(")) {
              if (level>0) { 
                double xx=pop_level();
                right[level]=xx;
              }
              break;
            }
          }
          // need to do final calc, as it may be a function

          System.out.printf("%sFINISHED CLOSING BRACKET from doing=%d (confirm, doing=1)\n",printprefix,doing);
          System.out.printf("%swould have been doing=%d\n",printprefix,doing);
          doing++;  // either 1 or 3 now based on what pops off stack
          //if (level>=0 && doing==1) { left[level]=calc(left[level],oper[level],right[level],operfunction[level]); }
          //if (level>=0 && doing==3) { right[level]=calc(left[level],oper[level],right[level],operfunction[level]); }
          //doing=3;
          show_state();
        } else { 
          System.out.printf("?SYNTAX ERROR 002\n***Not correct syntax - invalid operator \"%s\"\n",a);
        }

      } else if (doing==3 /* OPER-again */) {
        if (a.equals("^")) {
          System.out.printf("%sGot ^ oper\n",printprefix);
          if (operprec[level]>=11) {
            left[level]=calc(left[level],oper[level],right[level]);
            oper[level]=a; operprec[level]=11;
            doing=2 /* RIGHT */;
          } else {
            // push down a level
            //push_level();
            System.out.printf("%sDescending a level\n",printprefix);
            left[level+1]=right[level];
            prevdoing[level]=doing; // now push state as well
            level++;
            printprefix=printprefix+"  ";
            oper[level]=a; operprec[level]=11;
            doing=2 /* RIGHT */;
          }
        } else if (a.equals("*")||a.equals("/")) {
          System.out.printf("%sGot */ oper\n",printprefix);
          if (operprec[level]>=10) {
            // calculate and keep shift to left
            left[level]=calc(left[level],oper[level],right[level]);
            oper[level]=a; operprec[level]=10;
            doing=2 /* RIGHT */;
          } else {
            // push down a level
            //push_level();
            left[level+1]=right[level];
            prevdoing[level]=doing; // now push state as well
            level++;
            printprefix=printprefix+"  ";
            oper[level]=a; operprec[level]=10;
            doing=2 /* RIGHT */;
          }
        } else if (a.equals("+")||a.equals("-")) {
          System.out.printf("%sGot +- oper in (operagain)\n",printprefix);
          if (level>0 && operprec[level-1]>9) {
            // the operator above is a higher precedence, pop!
            System.out.printf("%slevel>0 && operprec[level-1]>9\n",printprefix);
            show_state();
            double x=pop_level();
            System.out.printf("%sx=%f\n",printprefix,x);
            right[level]=x;
            left[level]=calc(left[level],oper[level],right[level]);
            oper[level]=a; operprec[level]=9;
            doing=2; /* RIGHT */
            show_state();
          } else if (operprec[level]>=9) {
            left[level]=calc(left[level],oper[level],right[level]);
            oper[level]=a; operprec[level]=9;
            doing=2 /* RIGHT */;
          } else {
            // push down a level
            //push_level();
            left[level+1]=right[level];
            prevdoing[level]=doing; // now push state as well
            level++;
            printprefix=printprefix+"  ";
            oper[level]=a; operprec[level]=9;
            doing=2 /* RIGHT */;
          }
        } else if (a.equals(")")) {
          System.out.printf("%sCLOSING BRACKET from doing=%d\n",printprefix,doing);
          while(level>0) {
            System.out.printf("%sending bracket at level %d\n",printprefix,level);
            double x=pop_level();
            System.out.printf("%sx=%f\n",printprefix,x);
            //right[level]=pop_level();
            right[level]=x;
            System.out.printf("%sright[level]=%f\n",printprefix,right[level]);
            //left[level]=calc(left[level],oper[level],right[level]);
            if (oper[level].equals("(")) {
              System.out.printf("%sending bracket - found start\n",printprefix);
              //left[level]=pop_level();
              //double answer=left[level];
              //level--;
              //left[level]=answer;
              left[level]=right[level];
              doing=1; /* first oper */
              break;
            }
          }
          System.out.printf("%sdoing=%d\n",printprefix,doing);
          //doing=1;
        } else { 
          System.out.printf("?SYNTAX ERROR 002\n***Not correct syntax - invalid operator \"%s\"\n",a);
        }
      
     
      } else { /* nothing */ 
          System.out.printf("?SYNTAX ERROR 005\n***\"doing\" status that is not valid\n");
        
      }

    } /* fop */
    /* final calc */

    System.out.printf("%sFINAL CALCS\n",printprefix);
    show_state();
    while(level>0) {
      System.out.printf("%sfinal calc\n",printprefix);
      double x=pop_level();
      System.out.printf("%sx=%f\n",printprefix,x);
      right[level]=x;
      System.out.printf("%sright[level]=%f\n",printprefix,right[level]);
    }
    left[level]=calc(left[level],oper[level],right[level],operfunction[level]);
    System.out.printf("-------------------------------\n");
    System.out.printf("Evaluated %-20s  ",intstring);
    System.out.printf("%sanswer=%12f  expecting=%12f  difference=%12f",printprefix,left[level],expecting,left[level]-expecting);
    if (left[level]-expecting>0.0000001 || left[level]-expecting<-0.0000001) {
      System.out.printf(" !!**BAD**!!\n");
      System.out.printf("\n   ***************DISCREPENCY***************\n");
      System.out.printf("-------------------------------\n\n");
      return 1;
    } else {
      System.out.printf(" OKAY\n");
      System.out.printf("-------------------------------\n\n");
      return 0;
    }

  }

  public static void main(String[] args) {
    System.out.printf("Mello Word\n");
    new evaluate();
  }
}

