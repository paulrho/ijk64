/////////////////////////////////////////////////////////////////////////////////
//
// $Id: ev.java,v 1.15 2007/04/16 21:31:00 pgs Exp pgs $
//
// $Log: ev.java,v $
// Revision 1.15  2007/04/16 21:31:00  pgs
// Complete exception creation, ratify error messages, refactor code
// to use exceptions (makes code clearer)
//
// Revision 1.14  2007/04/13 08:59:01  pgs
// fix verbose display for assign too
//
// Revision 1.13  2007/04/11 20:12:33  pgs
// Added the option of running a Machine attached (stored variables etc)
//
// Revision 1.12  2007/04/11 17:57:58  pgs
// clearing verbose stack display (to the same as comment in code)
//
// Revision 1.10  2007/04/06 23:34:20  pgs
// move comment
//
// Revision 1.9  2007/04/06 23:32:48  pgs
// comments
//
// Revision 1.8  2007/04/06 23:30:18  pgs
// help comment
//
// Revision 1.7  2007/04/06 23:29:31  pgs
// Fix iterations (it was actually doing it twice for single and 101 for 100)
//
// Revision 1.6  2007/04/06 23:25:07  pgs
// More options and help listing
//
// Revision 1.5  2006/02/15 01:53:25  pgs
// Standard header
//
//
/////////////////////////////////////////////////////////////////////////////////

/**
     evaluate test harness
       // 100 x  82 equations = about 40 seconds, is : 205 equations per second on yoink <BR>
       // 100 x  72 equations  non verbose = about 2.0 seconds, is : 3600 equations per second on sirius <BR>
       // 101 x  84 equations  non verbose = about 3.79 seconds, is : 2238 equations per second on sirius <BR>
<PRE>
       1000 x 84 on sirius 20070407
         real    0m29.499s
         user    0m17.571s
         sys     0m7.324s
       --------------
       2.255s (with no stdout about 9sec with std out!)
       84000 equations = 37,333 equations per second!
       ~ 25us per equations

</PRE>
 **/

class ev {

  ev(String[] args) throws EvaluateException
  {
    evaluate evaluate_engine;
    boolean use_machine=false;

    for (int i=0; i<args.length; ++i) {
      if (args[i].substring(0,1).equals("-")) {
        if (args[i].length()>=2 && args[i].substring(0,2).equals("-m")) {
          use_machine=true;
        }
      }
    }
    if (!use_machine) {
      evaluate_engine = new evaluate();  // create engine
    } else {
      Machine machine=new Machine();
      // evaluate_engine = new evaluate(machine);  // create engine
     evaluate_engine = machine.evaluate_engine; // done inside initialise_engines - which is done now on creation of the machine
      // machine.initialise_engines(); // silly, but there is a reason (what is it?) // no done on creation now
     
      // to make equivalent to stand alone
      evaluate_engine.interpret_string_with_assignment("x=7");
      evaluate_engine.interpret_string_with_assignment("a=1");
      evaluate_engine.interpret_string_with_assignment("pi=mathpi");
    }
    boolean has_parameter=false;
    boolean do_many=false;
    boolean is_assignment=false;
    int iterations=1;

    // switch or negative number???
    for (int i=0; i<args.length; ++i) {
      if (args[i].length()>=2 && args[i].substring(0,1).equals("-")) {
        if (args[i].substring(0,2).equals("-v")) {
          evaluate_engine.verbose=true;
        } else if (args[i].substring(0,2).equals("-q")) {
          evaluate_engine.verbose=false;
        } else if (args[i].substring(0,2).equals("-a")) {
          is_assignment=true;
        } else if (args[i].substring(0,2).equals("-e")) {
          is_assignment=false;
        } else if (args[i].length()>=3 && args[i].substring(0,3).equals("-tt")) {
          iterations=1000;
        } else if (args[i].substring(0,2).equals("-t")) {
          iterations=100;
        } else if (args[i].substring(0,2).equals("-h")) {
          System.out.printf("evaluate test harness : ev : version $Id: ev.java,v 1.15 2007/04/16 21:31:00 pgs Exp pgs $\n");
          System.out.printf("  -a : assignment\n");
          System.out.printf("  -e : evaluate only - no assignment (default)\n");
          System.out.printf("  -h : help\n");
          System.out.printf("  -m : machine attached (to store variables) - dont run test with this\n");
          System.out.printf("  -q : quiet\n");
          System.out.printf("  -t : timing tests (100 iterations)\n");
          System.out.printf("  -tt : timing test with 1000 iterations\n");
          System.out.printf("  -v : verbose (default)\n");
          System.exit(0);
        }
      } else {
        has_parameter=true;
        // parameter // do it immediately
        if (is_assignment) {
          evaluate_engine.interpret_string_with_assignment(args[i]);
        } else {
          evaluate_engine.interpret_string(args[i]);
        }
      }
    }

    if (!has_parameter) {

    for (int i=0; i<iterations; ++i) {
    evaluate_engine.interpret_string("a>mxorb<3",-1.0);
    evaluate_engine.interpret_string("-3/-4",0.75);
    evaluate_engine.interpret_string("-sin(x)",-0.656986598718);
    evaluate_engine.interpret_string("0 or 2 and 0",0.0);
    evaluate_engine.interpret_string("1 or 2 and 0",1.0);
    evaluate_engine.interpret_string("1 and 2 or 0",0.0);
    evaluate_engine.interpret_string("1 or 2",3.0);
    evaluate_engine.interpret_string("1or2",3.0);
    evaluate_engine.interpret_string("1and2",0.0);
    evaluate_engine.interpret_string("1and0",0.0);
    evaluate_engine.interpret_string("1or0",1.0);

    if (false) { // these now fail on purpose
      // errors on purpose
      evaluate_engine.interpret_string("1+2)*3+1",10.0); //syntax wrong, but calculator wise definable
      evaluate_engine.interpret_string("1+2*(3+1",9.0); //syntax wrong, but calculator wise definable
    } else {
      evaluate_engine.interpret_string("(1+2)*3+1",10.0); //syntax corrected
      evaluate_engine.interpret_string("1+2*(3+1)",9.0); //syntax corrected
    }

    // should work
    //interpret_string("exp(10)",0.0);
    evaluate_engine.interpret_string("100+(1+(2+(3+(4+(5+(6+(7+8)))))))",100+1+2+3+4+5+6+7+8);
    evaluate_engine.interpret_string("7+(2*3+1+4*4)",7+(2*3+1+4*4));
    evaluate_engine.interpret_string("sin(1)",0.841471);
    evaluate_engine.interpret_string("(1+1)/(5+2)",2.0/7.0);
    evaluate_engine.interpret_string("(1+1)",2);
    evaluate_engine.interpret_string("+7*3*x^2-4*x^3-5*x^4",-12348);
    evaluate_engine.interpret_string("3+2*3^1+3",3+2*3+3);
    evaluate_engine.interpret_string("+1*2*3^2-2*3^1-3*3^3",2*9-2*3-3*27);
    evaluate_engine.interpret_string("+0.07*3*x^2-0.1*4*x^3-0.1*5*x^4",-1327.410000);
    evaluate_engine.interpret_string("7-4*1^1-5",+7-4*1-5);
    // need to work out the answer to this and set the corresponding expectation
    //interpret_string("+0.7+0.1*2*x+0.07*3*x^2-0.1*4*x^3-0.1*5*x^4-0.09*6*x^5-0.02*x^6-0.001*8*x^7",0);
    evaluate_engine.interpret_string("1-4*3+2",1-4*3+2);
    evaluate_engine.interpret_string("1-4*x+2",1-4*7+2);
    evaluate_engine.interpret_string("1-4+2",1-4+2);
    evaluate_engine.interpret_string("+7+14+75-144*x-2",+7+14+75-144*7-2);
    // interpret_string("+0.743809523809524+0.1421825*2*x+0.07555555*3*x^2-0.1447222*4*x^3-2",-186.717828);
    // interpret_string("+0.743809523809524+0.1421825*2*x+0.07555555*3*x^2-0.1447222*4*x^3",0.0);
    // interpret_string("+0.743809523809524+0.1421825*2*x+0.07555555*3*x^2-0.1447222*4*x^3-0.19777*5*x^4",-2558.946678);
    // interpret_string("+0.743809523809524+0.1421825*2*x+0.07555555*3*x^2-0.1447222*4*x^3",0.0);
    // interpret_string("-0.19777*5*x^4",0.0);
    evaluate_engine.interpret_string("+0.743809523809524+0.1421825*2*x+0.07555555*3*x^2-0.1447222*4*x^3-0.19777*5*x^4-0.095555*6*x^5-0.02158*7*x^6-0.001904761*8*x^7",-42516.182634);
    evaluate_engine.interpret_string("6.6732e-11*1000000000000",6.6732e-11*1000000000000.0);
    evaluate_engine.interpret_string("5.98e24",5.98e24);
    evaluate_engine.interpret_string("(41.3-23.45)/(42.3+56.4)",(41.3-23.45)/(42.3+56.4));
    evaluate_engine.interpret_string("sqrt(4^2+3^2)+10",15);
    evaluate_engine.interpret_string("sqrt(4^2+3^2)+10",15);
    evaluate_engine.interpret_string("sqrt(4^2+3^2)",5);
    evaluate_engine.interpret_string("0.5*1.23*5^2*a",0.5*1.23*5*5);
    evaluate_engine.interpret_string("pi",Math.PI);
    evaluate_engine.interpret_string("2*pi",2*Math.PI);
    evaluate_engine.interpret_string("1-1.3",1-1.3);
    evaluate_engine.interpret_string("1--1",2);
    evaluate_engine.interpret_string("4.4+3.7",4.4+3.7);
    evaluate_engine.interpret_string("(1)+(2)+(3)",1+2+3);
    evaluate_engine.interpret_string("1+(5)^2",26);
    evaluate_engine.interpret_string("sin(1)+1",1.841471);
    evaluate_engine.interpret_string("sin(1)*2",2*0.841471);
    evaluate_engine.interpret_string("1+sin(1)",1.841471);
    evaluate_engine.interpret_string("2*sin(1)",2*0.841471);
    evaluate_engine.interpret_string("sin(1)",0.841471);
    evaluate_engine.interpret_string("sin(1)",0.841471);
    evaluate_engine.interpret_string("sin(1)+cos(2)+tan(3)",0.282777605186476321819);
    evaluate_engine.interpret_string("(1)+(2)",3);
    evaluate_engine.interpret_string("(1+4)+(2+9)",(1+4)+(2+9));
    evaluate_engine.interpret_string("17+12",17+12);
    evaluate_engine.interpret_string("4+5*2^2",4+5*4);
    evaluate_engine.interpret_string("(1+2+3+4+5+6)*4",4*(1+2+3+4+5+6));
    evaluate_engine.interpret_string("(1+2+3+4+5+6)*4",4*(1+2+3+4+5+6));
    evaluate_engine.interpret_string("(5)^2+1",26);
    evaluate_engine.interpret_string("1+(5)^2",26);
    evaluate_engine.interpret_string("(1)+(2*3)*4+(5)^2",1+2*3*4+25);
    evaluate_engine.interpret_string("1+(5)",6);
    evaluate_engine.interpret_string("(2*3)*4+(5)",(2*3)*4+5);
    evaluate_engine.interpret_string("(1)+(2*3)*4+(5)",1+2*3*4+5);
    evaluate_engine.interpret_string("(4+3*3)/2^3-7",-5.37500000000000000000);
    evaluate_engine.interpret_string("1+1",2);
    evaluate_engine.interpret_string("(1+1)",2);
    evaluate_engine.interpret_string("((1+1))",2);
    evaluate_engine.interpret_string("1+2*3^4",1+2*3*3*3*3);
    evaluate_engine.interpret_string("3^4*2+1",1+2*3*3*3*3);
    evaluate_engine.interpret_string("3^4+2^3",3*3*3*3+8);
    evaluate_engine.interpret_string("2*3",2*3);
    evaluate_engine.interpret_string("2^3",8);
    evaluate_engine.interpret_string("2*(3+4)",2*(3+4));
    evaluate_engine.interpret_string("(3+4)*2",2*(3+4));
    evaluate_engine.interpret_string("2+(3*4)",2+(3*4));
    evaluate_engine.interpret_string("(3*4)+2",2+(3*4));
    evaluate_engine.interpret_string("(3+4)/2",(3+4)/2.0);
    evaluate_engine.interpret_string("1+1+1",3);
    evaluate_engine.interpret_string("1+1+1+1",4);
    evaluate_engine.interpret_string("1+2*3*4",1+2*3*4);
    evaluate_engine.interpret_string("1+2*3*4+5",1+2*3*4+5);
    evaluate_engine.interpret_string("(1)+(2*3)*4+(5)",1+2*3*4+5);
    evaluate_engine.interpret_string("(1)+(2*3)*4+(5^2)",1+2*3*4+25);
    evaluate_engine.interpret_string("(1+2+3+4+5+6)",1+2+3+4+5+6);
    evaluate_engine.interpret_string("4*(1+2+3+4+5+6)",4*(1+2+3+4+5+6));
    evaluate_engine.interpret_string("(1+2+3+4+5+6)*4",4*(1+2+3+4+5+6));
    }
    }
  } // end func

  public static void main(String[] args) throws EvaluateException {
    if (false) { System.out.printf("Mello Word\n"); }
    new ev(args);
  }
}

/////////
// END //
/////////
