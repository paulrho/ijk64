/////////////////////////////////////////////////////////////////////////////////
//
// $Id: C64.java,v 1.27 2007/04/13 09:32:43 pgs Exp pgs $
//
// $Log: C64.java,v $
// Revision 1.27  2007/04/13 09:32:43  pgs
// programText now in Machine - and used this way from C64
// in preparation for C64 online editting of program
// Added ability to enter line numbers and change program
//
// Revision 1.25  2007/04/11 17:45:38  pgs
// snapshot 20070411
//
// Revision 1.23  2007/03/28 21:23:23  pgs
// No READY prompt on empty CR line
//
// Revision 1.21  2007/03/24 02:11:08  pgs
// reinclude log entries
//
// Revision 1.20  2007/03/24 01:09:18  pgs
// more power to the popup
//
// revision 1.19
// date: 2007/03/24 01:07:51;  author: pgs;  state: Exp;  lines: +9 -1
// Modifications for popup menu

// Revision 1.18  2006/02/21 06:05:18  pgs
// Use a print instead of a println in list
//
// Revision 1.17  2006/02/20 07:36:02  pgs
// Comment on background only
//
// Revision 1.16  2006/02/19 22:26:19  pgs
// Now longer force 80 columns (this is done with a META-COLS 80) directive in the basic program
//
// Revision 1.15  2006/02/17 14:05:51  pgs
// Quick change of transparency and frame
//
// Revision 1.14  2006/02/15 01:50:33  pgs
// Standard header
//
//
/////////////////////////////////////////////////////////////////////////////////

import javax.swing.*;
import java.util.*;
import java.awt.event.*; // for key events

//import java.awt.*;
//import java.awt.Toolkit.*; // for image loading
//import java.awt.image.*;

/**
 * The main shell of C64
 * Does not do much in itself
 * <p>
 * Uses many classes to do its work.
 *
 * @see C64Screen
 */

class C64 {
  C64Screen screen;
  Machine machine;

  public C64(String args[]) {
    boolean runImmediate=false;
    boolean exitImmediate=false;

    //System.out.println("Running test harness for C64Screen...");
    System.out.println("JEBI/C64 version " + version.programVersion + " Running...\n");

    if (false) {
      // standard background
      C64Screen.static_bgtrans=false; // false = normal background
      C64Screen.static_handles=true; // true = has a frame around it
    } else {
      // transparent background
      C64Screen.static_bgtrans=true; // true = picture background
      C64Screen.static_handles=false; // false = has NO frame around it
    }
    screen=new C64Screen("C64");


    // instansiate the machine
    machine=new Machine(); // we initialise it once here
    ///machine.verbose=verbose;
    machine.initialise_engines(); // silly, but there is a reason
    //machine.verbose=verbose;

    // now add the popup
    C64PopupMenu pop=new C64PopupMenu(machine); // good idea???
    // keep a reference to it for returning things

    // when you close the window - stop the program
    // addWindowListener(new WindowAdapter() {
      // public void windowClosing(WindowEvent we) {
        // System.exit(0);
      // }
    // });


    if (false) { screen.scale=3; }
    if (false) { screen.changeCharSet(1); }
    //if (true) { screen.setRows(50); }
    //screen.setCols(80);

    for (int i=0; i<args.length; ++i) {
      if (args[i].substring(0,1).equals("-")) {
        if (false) { System.out.printf("Got a switch\n"); }

        if (args[i].substring(0,2).equals("-v")) {
          //evaluate_engine.verbose=true;
          //System.out.println("version : " +
            //C64.class.getPackage().getImplementationVersion() );
          System.out.printf("Version = %s\n", version.programVersion);
        } else if (args[i].substring(0,2).equals("-q")) {
          //evaluate_engine.verbose=false;
        } else if (args[i].substring(0,2).equals("-r")) {
          runImmediate=true;
        } else if (args[i].substring(0,2).equals("-x")) {
          exitImmediate=true;
        } else if (args[i].substring(0,2).equals("-t")) {
          //do_many=true; // timing test
        }
      } else {
        // parameter // do it immediately
        //has_parameter=true;
        //filename=args[i];
      }
    }



    if (false) {
      screen.println("This is a test");
      screen.println("Mellow word.");
      int i;
      for (i=0; i<1000; ++i) {
        screen.println("Printing line " +i);
      }
    }

    screen.startupscreen();
    if (args.length>=1) machine.loadProgram(args[0]); // load it in
    if (runImmediate) {
      //machine.statements(args); // we now execute the statements upon a machine
      machine.runProgram(); // we now execute the statements upon a machine // loaded program text
    }

    boolean displayReady=true;
    while (!exitImmediate) {
      if (displayReady) {
        screen.println("[CR]ready.");
      } else displayReady=true;

      String result;
      do {
        result=screen.screenInput();
        if (pop.forcedcompletion) break;
      } while (result.equals("")
               || result.length()>=40 
                  && (result.substring(0,40)).equals("                                        ")
               );
      if (pop.forcedcompletion) {
        pop.forcedcompletion=false;
        // this is true if a special command was called from the popup
        if (pop.command.equals("save")) {
          machine.saveProgram(pop.arg);
        } else if (pop.command.equals("fileopen")) {
          machine.loadProgram(pop.arg);
          machine.variables_clr();
        } else if (pop.command.equals("fileopenrun")) {
          // System.out.printf("got fileopen pop command\n");
          String [] str = new String[1];
          str[0]=pop.arg;
          // and keep it too
          args=new String[1];  // renew it
          args[0]=pop.arg; // will this work
          //machine.statements(str); // we now execute the statements upon a machine
          machine.loadProgram(str[0]);
          machine.variables_clr();
          machine.runProgram(); // we now execute the statements upon a machine
        } else if (pop.command.equals("run")) {
          // clear the variables first!
          // System.out.printf("got run pop command\n");
          //machine.statements(args); // we now execute the statements upon a machine
          machine.loadProgram(args[0]);
          machine.variables_clr();
          machine.runProgram(); // we now execute the statements upon a machine
          continue;
        } else if (pop.command.equals("new")) {
          screen.startupscreen();
          continue;
        } else if (pop.command.equals("exit")) {
          break;
        }
        System.out.printf("Forced completion triggered\n");
        continue;
      }
      if (result.length()>=4 && (result.substring(0,4)).equals("exit")) break;
      else if (result.length()>=4 && (result.substring(0,4)).equals("list")) {
        screen.print(machine.listProgram()); // just prints the program text
      } else if (result.length()>=3 && (result.substring(0,3)).equals("sys")) {
        screen.startupscreen();
        continue;
      } else if (result.length()>=6 && (result.substring(0,6)).equals("POPRUN")) {
        // no longer used
        // String [] str = new String[1];
        // str[0]=pop.arg;
        // machine.statements(str); // we now execute the statements upon a machine
        continue;
      } else if (result.length()>=3 && (result.substring(0,3)).equals("run")) {
        // clear the variables first!
        machine.variables_clr();
        //machine.statements(args); // we now execute the statements upon a machine
        machine.runProgram(); // we now execute the statements upon a machine
        continue;
      //} else if ((result.substring(0,4)).equals("LOAD")) {
        //screen.println("?very sorry - the interpreter is broken");
        //continue;
      //} else if ((result.substring(0,5)).equals("PRINT")) {
        //screen.println("?very sorry - the interpreter is broken");
        //continue;
      }
      if (false) System.out.println("Got string = "+result+"\n");
      if (result.length()>=40 && (result.substring(0,40)).equals("                                        ")) continue;
      if (result.equals("")) continue;
      // execute immediate!
      if (machine.insertLine(result.trim())) {
        displayReady=false;
      } else {
        machine.statements(result.trim()); // and again, upon a machine
       }
    }
    // actually exit
    System.exit(0);
  } // end th_C64Screen

  public static void main(String args[]) {
    new C64(args);
  }
}

//--------------------------------------------------------------------------
/////////
// END //
/////////
