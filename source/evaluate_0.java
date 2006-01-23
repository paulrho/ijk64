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
  }

  void show_state() {
    System.out.printf("STATE:\n",printprefix);
    for (int levelx=0; levelx<=level; levelx++) {
      System.out.printf("%slevel=%d left[level]=%f oper[level]=%s right[level]=%f\n",printprefix,levelx,left[levelx],oper[levelx],right[levelx]);
    }
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
    show_state();
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

  int interpret_string(String intstring, double expecting) {
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
          level++; printprefix=printprefix+"  ";
          oper[level]="(";
          show_state();
          level++; printprefix=printprefix+"  ";
          show_state();
          doing=0;
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
          System.out.printf("%sCLOSING BRACKET from doing=%d (confirm, doing=1)\n",printprefix,doing);
          // this one does not have an equation to calc
          show_state();
          oper[level]="("; // just to trick it into a no calc
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
          System.out.printf("%sFINISHED CLOSING BRACKET from doing=%d (confirm, doing=1)\n",printprefix,doing);
          doing=3;
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
            level++;
            printprefix=printprefix+"  ";
            oper[level]=a; operprec[level]=10;
            doing=2 /* RIGHT */;
          }
        } else if (a.equals("+")||a.equals("-")) {
          System.out.printf("%sGot +- oper in (operagain)\n",printprefix);
          if (level>0 && operprec[level-1]>9) {
            // the operator above is a higher precedence, pop!
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
            level++;
            printprefix=printprefix+"  ";
            oper[level]=a;
            operprec[level]=9;
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
          doing=1;
        } else { 
          System.out.printf("?SYNTAX ERROR 002\n***Not correct syntax - invalid operator \"%s\"\n",a);
        }
      
     
      } else { /* nothing */ 
          System.out.printf("?SYNTAX ERROR 005\n***\"doing\" status that is not valid\n");
        
      }

    } /* fop */
    /* final calc */
    show_state();
    while(level>0) {
      System.out.printf("%sfinal calc\n",printprefix);
      double x=pop_level();
      System.out.printf("%sx=%f\n",printprefix,x);
      right[level]=x;
      System.out.printf("%sright[level]=%f\n",printprefix,right[level]);
    }
    left[level]=calc(left[level],oper[level],right[level]);
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

