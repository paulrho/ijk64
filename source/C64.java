/////////////////////////////////////////////////////////////////////////////////
//
// $Id: C64.java,v 1.38 2012/04/30 09:45:34 pgs Exp $
//
// $Log: C64.java,v $
// Revision 1.38  2012/04/30 09:45:34  pgs
// resizing ability
// fixing (but not fixed yet) character tranlation mapping
//
// Revision 1.37  2012/04/18 06:07:53  pgs
// Adding graphics capability
//
// Revision 1.36  2011/07/06 09:46:23  pgs
// Mouse pointing and scrolling
//
// Revision 1.35  2011/07/03 23:00:20  pgs
// Add EVAL$ function
// Fix insertspace etc modes - they were buggy
//
// Revision 1.34  2007/04/19 08:28:24  pgs
// Refactoring and simplifying/formatting code especially in Machine
//
// Revision 1.33  2007/04/18 22:11:16  pgs
// Fix to be default banner screen
//
// Revision 1.32  2007/04/18 09:37:19  pgs
// More refactoring with regards to creating program/immediate modes
//
// Revision 1.31  2007/04/17 21:46:14  pgs
// Modifications to get CONT to work properly
//
// Revision 1.30  2007/04/17 09:22:33  pgs
// Adding ability to restart with CONT
// moved all statments into statements/Machine engine
//
// Revision 1.29  2007/04/16 21:31:00  pgs
// Complete exception creation, ratify error messages, refactor code
// to use exceptions (makes code clearer)
//
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

// Revision 1.18  2006/02/21 06:05:18  ctpgs
// Use a print instead of a println in list
//
// Revision 1.17  2006/02/20 07:36:02  ctpgs
// Comment on background only
//
// Revision 1.16  2006/02/19 22:26:19  pgs
// Now longer force 80 columns (this is done with a META-COLS 80) directive in the basic program
//
// Revision 1.15  2006/02/17 14:05:51  pgs
// Quick change of transparency and frame
//
// Revision 1.14  2006/02/15 01:50:33  ctpgs
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
    //boolean speeder=false;
    boolean speeder=true;
    boolean specialGOTO=false;
    boolean exitImmediate=false;

    // transparent background
    // C64Screen.static_bgtrans=true; // true = picture background
    // C64Screen.static_handles=false; // false = has NO frame around it

    // standard background
    C64Screen.static_bgtrans=false; // default normal background
    C64Screen.static_handles=true;  // default frames

    //System.out.println("Running test harness for C64Screen...");
    System.out.println("JEBI/C64 version " + version.programVersion + " Running...\n");

    boolean blankscreen=false;
    String filename="";
    String specialcommand="";
    String cloudNet="";
    int screendeco=0;

    for (int i=0; i<args.length; ++i) {
      if (args[i].length()>=2 && args[i].substring(0,1).equals("-")) {
        if (args[i].substring(0,2).equals("-v")) {
          //System.out.println("version : " +
            //C64.class.getPackage().getImplementationVersion() );
          System.out.printf("Version = %s\n", version.programVersion);
        } else if (args[i].substring(0,2).equals("-r")) {
          runImmediate=true;
        } else if (args[i].length()>=3 && args[i].substring(0,2).startsWith("-a")) {
            specialGOTO=true;
            specialcommand=args[i].substring(2);
        } else if (args[i].substring(0,2).equals("-h")) {
          System.out.printf("program [options] [filename]\n");
          System.out.printf("  -b : blank screen - no banner\n");
          System.out.printf("  -c : centre screen\n");
          System.out.printf("  -h : help\n");
          System.out.printf("  -n : no frame\n");
          System.out.printf("  -r : run immediately\n");
          System.out.printf("  -a\"commands\" : autotest run:commands\n");
          System.out.printf("  -t : transparent background\n");
          System.out.printf("  -v : version\n");
          System.out.printf("  -x : exit immediately\n");
          System.out.printf("  -2 : double size\n");
          System.out.printf("  -3 : triple size\n");
          System.out.printf("  -z : petspeed [default]\n");  // make this the default
          System.out.printf("  --slow  : no petspeed\n");  // make this the default
          System.out.printf("  --https : use https for cloud\n");  // make this the default
          System.out.printf("  --black : white on black on black\n");  // make this the default
        } else if (args[i].substring(0,2).equals("-b")) {
          blankscreen=true;
        } else if (args[i].equals("--black")) {
          blankscreen=true; screendeco=1;
        } else if (args[i].substring(0,2).equals("-n")) {
          C64Screen.static_handles=false;
        } else if (args[i].substring(0,2).equals("-t")) {
          C64Screen.static_bgtrans=true;
        } else if (args[i].substring(0,2).equals("-x")) {
          exitImmediate=true;
        } else if (args[i].substring(0,2).equals("-z")) {
          speeder=true;
        } else if (args[i].equals("--slow")) {
          speeder=false;
        } else if (args[i].equals("--https")) {
          cloudNet="https://futex.com.au";
        } else if (args[i].substring(0,2).equals("-2")) {
          C64Screen.static_scale=2;
        } else if (args[i].substring(0,2).equals("-3")) {
          //screen.setScale(3);
          C64Screen.static_scale=3;
        } else if (args[i].substring(0,2).equals("-c")) {
          //screen.setLocationRelativeTo(null); 
          C64Screen.static_centre=true; // make this the default ?? FIX
        }
      } else {
        if (args[i].length()>=1 && !args[i].substring(0,1).equals("-")) {
          if (filename.equals("")) filename=args[i];
        }
      }
    }

//    machine=new Machine(screen=new C64Screen("C64")); // new way of attaching screen
    machine=new Machine(screen=new C64Screen("ijk64")); // new way of attaching screen
    machine.switchSpeeder(speeder);
    if (!cloudNet.equals("")) machine.cloudNet=cloudNet;
    C64Screen.static_centre=false; // is this right CHECK
    C64PopupMenu pop=new C64PopupMenu(machine,screen); // keep a reference to it for returning things

    // post initialise
    //for (int i=0; i<args.length; ++i) {
      //if (args[i].length()>=2 && args[i].substring(0,1).equals("-")) {
      //}
    //}

    // set the icon (doesnt work with ico but does with png
    //    java.net.URL url = ClassLoader.getSystemResource("images/c64.ico");
    java.net.URL url = C64Screen.class.getResource("images/c64.png");
    java.awt.Toolkit kit = java.awt.Toolkit.getDefaultToolkit();
    java.awt.Image img = kit.getImage(url);
    screen.setIconImage(img);
  
    //machine.runOS // gets a line and executes it (including running program)

    if (screendeco==1)
      screen.startupscreen(0,0,1,blankscreen);
    else {
      if (!blankscreen) screen.startupscreen(); else screen.startupscreen_blank();
    }
    for (int i=0; i<args.length; ++i) 
      if (args[i].length()>=1 && !args[i].substring(0,1).equals("-")) {
        filename=args[i];
        if (filename.length()>=1) machine.loadProgram(filename,true); // load it in
        if (runImmediate) {
          machine.runProgram(); // we now execute the statements upon a machine
        }
        if (specialGOTO) {
          // used for autotesting, will think of more eligant soln
          try {
          screen.println("[CR]ready.");
          //machine.contProgram("5000"); // we now execute the statements upon a machine
          // machine.runImmediate("goto5000"); // and again, upon a machine
          machine.runImmediate(specialcommand); // and again, upon a machine
          } catch(Exception e) { System.out.printf("GOTO5000 failed\n"); }
          //if (!blankscreen) screen.startupscreen(); else screen.startupscreen_blank();
          // really should reset machine
          String command="new:meta-charset0:meta-rows25:meta-cols40:meta-scale1:meta-scaley1:print\"(clr)\"";
          machine.runImmediate(command.trim()); // and again, upon a machine
        }
      } 
    // this has now become the IMMEDIATE interpreter
    boolean displayReady=true;
    while (!exitImmediate) {
      if (displayReady) {
        screen.println("[CR]ready.");
      } else displayReady=true;

      String result;
      do {
        result=screen.screenInput(true/*direct*/);
      } while (result.trim().equals("") && !pop.forcedcompletion && !screen.forcedcompletion);
      if (screen.forcedcompletion) {
        result=screen.forcedcommand;
        screen.forcedcompletion=false;
        screen.forcedcommand=""; // not really necessary
        /* read out the char that forced the line term (CR or BREAK) */
        if (screen.hasinput()) screen.givemekey();
      }
      if (pop.forcedcompletion) {
        pop.forcedcompletion=false;
        result=pop.command;
        /* read out the char that forced the line term (CR or BREAK) */
        if (screen.hasinput()) screen.givemekey();
      }
      if (result.trim().equals("")) continue; // just a blank line
      // execute immediate!
      if (machine.insertLine(result.trim())) {
        displayReady=false;
      } else {
	      // experiment - run this in another thread
	     
	//THREAD//final String resultthread=result.trim();
        //THREAD//Thread t = new Thread() {   // Create an instance of an anonymous inner class that extends Thread
          //THREAD//@Override
          //THREAD//public void run() {      // Override run() to specify the running behaviors

            machine.runImmediate(result.trim()); // and again, upon a machine
            //THREAD//machine.runImmediate(resultthread); // and again, upon a machine

          //THREAD//}
        //THREAD//};
        //THREAD//t.start();  // Start the thread. Call back run() in a new thread


        if (machine.signal_exit) break;
       }
    } // end while

    // actually exit
    System.exit(0);
  } // end C64

  public static void main(String args[]) {
    new C64(args);
  }
}

//--------------------------------------------------------------------------
/////////
// END //
/////////
