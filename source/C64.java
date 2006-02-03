import javax.swing.*;
import java.util.*;
import java.awt.event.*; // for key events

//import java.awt.*;
//import java.awt.Toolkit.*; // for image loading
//import java.awt.image.*;


class C64 {
  C64Screen screen;

  public C64() {
    System.out.println("Running test harness for C64Screen...");
    C64Screen.static_bgtrans=false;
    C64Screen.static_handles=true;
    screen=new C64Screen("C64");

    screen.scale=2;

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
      if ((result.substring(0,4)).equals("EXIT")) break;
      else if ((result.substring(0,4)).equals("LIST")) {
        screen.println("10 print\"mello word\"");
        screen.println("20 a=5*5:print a");
        screen.println("90 end");
        continue;
      } else if ((result.substring(0,3)).equals("SYS")) {
        screen.startupscreen();
        continue;
      } else if ((result.substring(0,3)).equals("RUN")) {
        //screen.println("mello word");
        //screen.println("25");
        String dummy[];
        dummy=new String[1];
        dummy[0]="x";
        new statements(dummy);
        continue;
      } else if ((result.substring(0,4)).equals("LOAD")) {
        screen.println("?very sorry - the interpreter is broken");
        continue;
      } else if ((result.substring(0,5)).equals("PRINT")) {
        screen.println("?very sorry - the interpreter is broken");
        continue;
      }
      //screen.println("Got string = "+result);
      if ((result.substring(0,40)).equals("                                        ")) continue;
      screen.println("?syntax error");
    }
  } // end th_C64Screen

  public static void main(String args[]) {
    new C64();
  }
}

//--------------------------------------------------------------------------
