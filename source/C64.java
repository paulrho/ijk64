/////////////////////////////////////////////////////////////////////////////////
//
// $Id: C64.java,v 1.18 2006/02/21 06:05:18 pgs Exp pgs $
//
// $Log: C64.java,v $
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
// when ready // package au.com.futex.jebi;

import javax.swing.*;
import java.util.*;
import java.awt.event.*; // for key events

//import java.awt.*;
//import java.awt.Toolkit.*; // for image loading
//import java.awt.image.*;


class C64 {
  C64Screen screen;
  Machine machine;

  public C64(String args[]) {
    System.out.println("Running test harness for C64Screen...");
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

    // now add the popup
    new C64PopupMenu();

    // instansiate the machine
    machine=new Machine(); // we initialise it once here
    ///machine.verbose=verbose;
    machine.initialise_engines(); // silly, but there is a reason
    //machine.verbose=verbose;

    if (false) { screen.scale=3; }
    if (false) { screen.changeCharSet(1); }
    //if (true) { screen.setRows(50); }
    //screen.setCols(80);

    for (int i=0; i<args.length; ++i) {
      if (args[i].substring(0,1).equals("-")) {
        if (false) { System.out.printf("Got a switch\n"); }

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

    screen.setcursColour("LIGHT BLUE");
    screen.startupscreen();
    while (true) {
      screen.println("[CR]ready.");
      String result=screen.screenInput();
      if (result.length()>=4 && (result.substring(0,4)).equals("EXIT")) break;
      else if (result.length()>=4 && (result.substring(0,4)).equals("LIST")) {
        if (args.length>0) {
          // call the static method
          screen.print(statements.read_a_file(args[0]));
        }
        //screen.println("10 print\"mello word\"");
        //screen.println("20 a=5*5:print a");
        //screen.println("90 end");
        //continue;
      } else if (result.length()>=3 && (result.substring(0,3)).equals("SYS")) {
        screen.startupscreen();
        continue;
      } else if (result.length()>=3 && (result.substring(0,3)).equals("RUN")) {
        machine.statements(args); // we now execute the statements upon a machine
        continue;
      //} else if ((result.substring(0,4)).equals("LOAD")) {
        //screen.println("?very sorry - the interpreter is broken");
        //continue;
      //} else if ((result.substring(0,5)).equals("PRINT")) {
        //screen.println("?very sorry - the interpreter is broken");
        //continue;
      }
      System.out.println("Got string = "+result+"\n");
      if (result.length()>=40 && (result.substring(0,40)).equals("                                        ")) continue;
      if (result.equals("")) continue;
      machine.statements(result.trim()); // and again, upon a machine
      //screen.println("?syntax error");
    }
  } // end th_C64Screen

  public static void main(String args[]) {
    new C64(args);
  }
}

//--------------------------------------------------------------------------
/////////
// END //
/////////
