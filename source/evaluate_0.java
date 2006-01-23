// why not - java will do

// make test harness of equations

class evaluate {

  double left[];
  double right[];
  String oper[];
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
    interpret_string("(4+3*3)/2^3-7");
  }

  double calc(double left, String oper, double right) {
    double answer=0.0;
    if (oper.equals("+")) { answer=left+right; }
    else if (oper.equals("-")) { answer=left-right; }
    else if (oper.equals("*")) { answer=left*right; }
    else if (oper.equals("/")) { answer=left/right; }
    else if (oper.equals("^")) { answer=Math.pow(left,right); }
    else if (oper.equals("(")) { answer=left; }
    else {
      System.out.printf("?SYNTAX ERROR 003\n***Unsupported oper \"%s\"\n",oper);
      //answer=NaN;
      answer=0.0;
    }
    System.out.printf("%sCalculating %f %s %f, Answer = %f\n",printprefix,left,oper,right,answer);
    return answer;
  }

  double pop_level() {
    /* finished, come back up a level, after evaluating lower level */
    double answer=calc(left[level],oper[level],right[level]);
    level--;
    printprefix=printprefix.substring(0,printprefix.length()-2);
    System.out.printf("%sLevel %d, Popping answer=%f\n",printprefix,level,answer);
    return answer;
  }

  int interpret_string(String intstring) {
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
          oper[level]="(";
          level++;
          printprefix=printprefix+"  ";
        } else if (a.equals("+")) {
          /* modifier or actual operator? */
        } else if (a.equals("-")) {
        } else if (a.compareTo("0")>=0 && a.compareTo("9")<=0) {
          double value=Double.parseDouble(a);
          System.out.printf("%sGot value %f\n",printprefix,value);
          left[level]=value;
          doing=1 /*oper*/;
        } else {
          System.out.printf("?SYNTAX ERROR 001\n***Not correct syntax\n");
        }
      } else if (doing==2 /* RIGHT */) {
        if (a.equals("(")) {
          // here we need to push down two levels ?
          level++;
          oper[level]="(";
          level++;
        } else if (a.equals("+")) {
          // fixme
        } else if (a.equals("-")) {
          // fixme
        } else if (a.compareTo("0")>=0 && a.compareTo("9")<=0) {
          double value=Double.parseDouble(a);
          System.out.printf("%sGot value %f\n",printprefix,value);
          right[level]=value;
          //doing=1 /*oper*/;
          doing=3 /* oper for second time */;
        } else {
          System.out.printf("?SYNTAX ERROR 001\n***Not correct syntax\n");
        }

      } else if (doing==1 /* OPER */) {
        if (a.equals("^")) {
          System.out.printf("%sGot ^ oper\n",printprefix);
          oper[level]=a;
          doing=2 /*right*/;
          operprec[level]=11;
        } else if (a.equals("*")||a.equals("/")) {
          System.out.printf("%sGot */ oper\n",printprefix);
          oper[level]=a;
          doing=2 /*right*/;
          operprec[level]=10;
        } else if (a.equals("+")||a.equals("-")) {
          System.out.printf("%sGot +- oper\n",printprefix);
          oper[level]=a;
          doing=2 /*right*/;
          operprec[level]=9;
        } else if (a.equals(")")) {
          while(true) {
            left[level]=pop_level();
            if (oper[level].equals("(")) {
              left[level]=pop_level();
              break;
            }
          }
        } else { 
          System.out.printf("?SYNTAX ERROR 002\n***Not correct syntax - invalid operator \"%s\"\n",a);
        }

      } else if (doing==3 /* OPER-again */) {
        if (a.equals("^")) {
          System.out.printf("%sGot ^ oper\n",printprefix);
          if (operprec[level]>=11) {
            calc(left[level],oper[level],right[level]);
          } else {
            // push down a level
            //push_level();
            left[level+1]=right[level];
            level++;
            printprefix=printprefix+"  ";
            oper[level]=a;
            operprec[level]=11;
            doing=2 /* RIGHT */;
          }
        } else if (a.equals("*")||a.equals("/")) {
          System.out.printf("%sGot */ oper\n",printprefix);
          if (operprec[level]>=10) {
            // calculate and keep shift to left
            calc(left[level],oper[level],right[level]);
          } else {
            // push down a level
            //push_level();
            left[level+1]=right[level];
            level++;
            printprefix=printprefix+"  ";
            oper[level]=a;
            operprec[level]=10;
            doing=2 /* RIGHT */;
          }
        } else if (a.equals("+")||a.equals("-")) {
          System.out.printf("%sGot +- oper\n",printprefix);
          if (operprec[level]>=11) {
            left[level]=calc(left[level],oper[level],right[level]);
            oper[level]=a;
            doing=2 /* RIGHT */;
          } else {
            // push down a level
            //push_level();
            left[level+1]=right[level];
            level++;
            printprefix=printprefix+"  ";
            oper[level]=a;
            operprec[level]=9;
            doing=2 /* RIGHT */;
          }
        } else if (a.equals(")")) {
          while(level>=0) {
            System.out.printf("%sending bracket\n",printprefix);
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
          doing=1;
        } else { 
          System.out.printf("?SYNTAX ERROR 002\n***Not correct syntax - invalid operator \"%s\"\n",a);
        }
      
     
      } else { /* nothing */ 
          System.out.printf("?SYNTAX ERROR 005\n***\"doing\" status that is not valid\n");
        
      }

    } /* fop */
    /* final calc */
          while(level>=0) {
            System.out.printf("%sending bracket\n",printprefix);
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
              doing=1;
              break;
            }
          }



    return 0;
  }

  public static void main(String[] args) {
    System.out.printf("Mello Word\n");
    new evaluate();
  }
}

