//////////////////////////////////////////////////////////////////////////////////
//
// $Id: C64Screen.java,v 1.24.1.3.1.22 2012/09/04 11:22:38 pgs Exp $
//                                                               
// $Log: C64Screen.java,v $
// Revision 1.24.1.3.1.22  2012/09/04 11:22:38  pgs
// petscii mapping
// /
//
// Revision 1.24.1.3.1.20  2012/04/18 06:23:49  pgs
// explicitly identify GraphicsDevice as I used the same name as awt.java!
//
// Revision 1.24.1.3.1.19  2012/04/18 06:07:53  pgs
// Adding graphics capability
//
// Revision 1.24.1.3.1.18  2011/07/06 09:46:23  pgs
// Mouse pointing and scrolling
//
// Revision 1.24.1.3.1.17  2011/07/03 23:00:20  pgs
// Add EVAL$ function
// Fix insertspace etc modes - they were buggy
//
// Revision 1.24.1.3.1.16  2011/06/29 21:36:51  pgs
// more changes - no space required after line number
// shift return no-op
// using PETSCII
//
// Revision 1.24.1.3.1.15  2011/06/28 23:40:14  pgs
// Standardising keycodes - now use PETSCII
//
// Revision 1.24.1.3.1.14  2011/06/27 23:33:55  pgs
// More changes for 325 cut/paste, insertchar mode, List params
//
// Revision 1.24.1.3.1.12  2007/04/22 22:37:01  pgs
// Add duty cycle
//
// Revision 1.24.1.3.1.11  2007/04/17 09:22:33  pgs
// Adding ability to restart with CONT
// moved all statments into statements/Machine engine
//
// Revision 1.24.1.3.1.10  2007/04/16 21:31:00  pgs
// Complete exception creation, ratify error messages, refactor code
// to use exceptions (makes code clearer)
//
// Revision 1.24.1.3.1.9  2007/04/13 09:32:43  pgs
// programText now in Machine - and used this way from C64
// in preparation for C64 online editting of program
// Added ability to enter line numbers and change program
//
// Revision 1.24.1.3.1.7  2007/04/11 17:45:38  pgs
// snapshot 20070411
//
// Revision 1.24.1.3.1.6  2007/03/28 21:25:03  pgs
// changing a couple of keys square bracket etc
//
// Revision 1.24.1.3.1.4  2007/03/24 02:06:41  pgs
// Changes to add CBM codes
//
// Revision 1.24.1.3.1.2  2006/02/21 22:24:16  pgs
// More changes for the special test version
//
// Revision 1.24.1.3.1.1  2006/02/21 06:04:26  ctpgs
// New sub-sub-version experimenting with quoting
//
// Revision 1.24.1.3  2006/02/20 07:36:41  ctpgs
// Allow stetched (scaley) characters to simulate 80x25 mode to an extent
// Correctly rezero (new) the screen buffer when changing anything with transparent mode
// (the transparent attribute needs to be cleared)
//
// Revision 1.24.1.2  2006/02/15 04:58:06  ctpgs
// Remove the last screen CLEARed pscreenchar on Reshape screen - it was leaving things behind
// I think this was not correct to do this, it now appears to work okay.  More research probably required.
//
// Revision 1.24.1.1  2006/02/14 22:41:14  pgs
// This is the branched version, rebranched of the correctly formatted final version
// from java genes.
// This means a diff between 1.24 and the latest on 1.24.1 branch should show actually changes
// made form C64 JEBI
//
// Revision 1.23.1.2  2006/02/14 13:26:21  pgs
// Changing only the header to include information from the (non RCS versioned) changes made
// These where made with RCS but not within the ,v file
//
// Revision 1.23.1.1  2006/02/14 13:21:11  pgs
// Merged version of C64Screen version 1.20.1.8 and 1.23 (last version from java genes)
// But kept as another branch - because this has not been regression tested back on java genes
// but it has been forward tested on C64 JEBI
//
// ... note - in here - we should mention the other versions (of which some are NOT rcsed!)
// can we fix up later?? dont know, but I would like to!
//---
// Revision 1.23  2006/02/14 13:14:47  pgs
// >> Bringing back orginal genes version C64Screen - with ITS latest changes in C64Screen.java
// >> and NO changes from the branch
// >> will start another branch under 1.23 that will include the JEBI branch and changes
// >>
// Really comes from the date 2004/12/28 03:45:34 zulu - but RCS doesnt let that happen
//
// Revision 1.22  2004/12/28 03:45:34  pgs
// Checking in final change made - but not checked in:
// Enabling dynamic cols, new functions setColsRows
//
// Revision 1.21  2004/12/28 03:19:50  pgs
// Allow up to a characters columns
//
// Revision 1.20  2004/12/28 03:16:27  pgs
// modifications unspecified
//
// Revision 1.19  2004/12/06 10:35:04  pgs
// Allow startup variables for C64Screen instance (not the best way to do it)
//
// Revision 1.18  2004/12/06 10:34:05  pgs
// *** empty log message ***
//
// Revision 1.16  2004/11/30 20:50:55  pgs
// Enhancements for meta characters in println strings
//---
//
// Revision 1.20.1.7  2006/02/13 04:36:24  ctpgs
// Allow character set selection from with running program
// Flash time now a variable and set down to 250ms (from 500ms) which is similar to the real (emulated version)
// Add (home) keyword
// Make cursor do revese char (like the real) instead of just a block
//
// Revision 1.20.1.6  2006/02/09 01:23:46  ctpgs
// Enable scale=3 to allow screens three times wider than standard (using new _3 files too)
//
// Revision 1.20.1.5  2006/02/07 23:16:34  ctpgs
// Allow control C break and experiment with lower case graphics
//
// Revision 1.20.1.4  2006/02/06 06:01:20  ctpgs
// Changes to allow input a line work correctly
//
// Revision 1.20.1.3  2006/02/03 21:17:48  ctpgs
// More changes for detokenised file and making printing chars print the right ones
//
// Revision 1.20.1.2  2006/02/03 12:40:04  ctpgs
// More changes to allow reading of detokenised basic program from another source
//
// Revision 1.20.1.1  2006/02/03 06:09:07  ctpgs
// Branched version, to make changes for C64 (JEBI).
// Allows different escape character for special chars (keywords) uses round brackets
// instead of square  (as well) as to allow for the version of genesdt that was detokenised
// by another program in a different way
//
// Revision 1.20  2005/06/16 06:09:27  ctpgs
// Resubmitted version - appears to be that last version of C64Screen used with GenesJCPmA (but was never checked in)
// The is also the latest untouched version, before changes were made for C64 (JEBI)
//
// Revision 1.16  2004/11/30 20:50:55  pgs
// Enhancements for meta characters in println strings
//
/////////////////////////////////////////////////////////////////////////////////

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;        // for key events
import java.awt.Toolkit.*;      // for image loading
import java.awt.image.*;
import java.util.*;

// for funky imageio stuff
import java.io.*;
import javax.imageio.*;

// for jar loading URL
import java.net.*; // I think
import java.lang.Class; // I think

// things to do:
// . change the screen writing process from being done EVERY paint
//   to being done on change only, and painting the static image
//   -tried this 10's to 100's of times slower!!!
// . not display until all scrolls etc finished (otherwise blanking etc shows up)
//--------------------------------------------------------------------------
// alseo 200412-- in order to radically change the screen
// how about removing the object (and therefore frame!) and creating a new one

class C64Screen extends JFrame 
  implements KeyListener
             ,WindowFocusListener
{
  Graphics2D offGraphics;
  Graphics2D newoffGraphics;
  int cursX = 0;
  int cursY = 0;
  short cursColour = 2;
  boolean cursVisible = false;
  boolean cursEnabled = false;
  boolean verbose = false; // only recent
  boolean verbosePrint = false;
  boolean shiftbgimage = true; // now true
//int flashtimems=500;
  int flashtimems = 330; // was 250, C64 is 333
  int screenUpatems=165; // 200 worked well with 40x25 on win but not bigger, then 400, then 250
  // interaction between two means that flashtimems cant be set to exactly what I want
  // 250,250 ok - but fast flashing - like this for a long time
  // 330,165 now same as real c64 - but updates screen 50% more often (this might be a good thing)

  static public C64Screen out = null;   // a pointer to the last (really first) instansiated screen for 
// external use (like Screen.out, but C64Screen.screen)

  String colourname[] = { "BLACK", "WHITE", "RED", "CYAN", "PURPLE", "GREEN", "BLUE", "YELLOW", "ORANGE",
    "BROWN", "LIGHT RED", "GREY 1", "GREY 2", "LIGHT GREEN", "LIGHT BLUE", "GREY 3"
  };
  String colourname_alias[] = { "blk", "wht", "red", "cyn", "pur", "grn",
    "blu", "yel", "orng", "brn", "lred", "gry1",
    "gry2", "lgrn", "lblu", "gry3"
  };                            // just for testing

  int fullcolour[] = {
    0xFF000000,                 // black
    0xFFFFFFFF,                 // white
    0xFFE00000,                 // red //0xFFFF2020, // red
    0xFF20FFFF,                 // cyan
    0xFFD020FF,                 // purple        was magenta 0xFFFF20FF, 
    0xFF20E020,                 // green         was 0xFF20FF20, // green
    0xFF4040C0,                 // blue          was 0xFF2020A0, // dark blue //3030B0 seems good
    0xFFFFFF20,                 // yellow
    0xFFC87020,                 // orange        was 0xFF808020, // brown
    0xFF704020,                 // brown
    0xFFFF8080,                 // light red
    0xFF404040,                 // grey 1
    0xFF808080,                 // grey 2
    0xFF80FF80,                 // light green
    0xFFA0A0FF,                 // light blue
    0xFFA0A0A0                  // grey 3
  };
//modImage.setRGB(posx*8+i,posy*8+j,0xFFA0AAFF);
  int backgroundColour = fullcolour[0]; // for test 2
  int borderColour = fullcolour[0];     // for test 5

//int scale=2;
//int scale=1;
  int scale = 1;
  int scaley = 1; // allow scaling in the y direction only - now it gets interesting how to retrofit
//int maxX=40;
  int maxX = 40;
  static int maxY = 25;
  final static int MAXmaxX = 80;
  final static int MAXmaxY = 125;
  int topY = 12;
//String lines[]=new String[maxY];
  char[][] screenchar = new char[MAXmaxX][MAXmaxY];
  short[][] screencharColour = new short[MAXmaxX][MAXmaxY];

  short[] contmark = new short[MAXmaxY];
  boolean faint = false;
  boolean bgtrans = true;
  boolean bgtrans_ability = true; /*TEST_ONLY*/
  boolean bgshadow = true;
  boolean slowdown = false;

  static boolean static_bgtrans = true; // for a dodgy initialisation
  static boolean static_handles = false;        // for a dodgy initialisation
  static boolean static_centre = false;        // for a dodgy initialisation
  static int static_scale = 1;        // for a dodgy initialisation

  boolean sendToBack = false;

  int SLOW_INPUT_MS=1; // amout to slow down GETa$
  int GIVEMEKEY_MS=10; // same as always  
  
//key
  static int keybufmax = 1000;
  int keybuftop = 0;
  int keybufbot = 0;
  char keybuf[] = new char[keybufmax];

  public final static int BREAK_KEY = 256; // just anything really

  final static char PETSCII_WHITE        =   5;
  final static char PETSCII_DISABLE_SC   =   8;
  final static char PETSCII_ENABLE_SC    =   9;
  public final static char PETSCII_ENTER =  13;
  final static char PETSCII_SWITCH_LOWER =  14;
  final static char PETSCII_DOWN         =  17;
  final static char PETSCII_RVON         =  18;
  final static char PETSCII_HOME         =  19;
  final static char PETSCII_DEL          =  20; // backspace
  final static char PETSCII_RED          =  28;
  final static char PETSCII_RGHT         =  29;
  final static char PETSCII_GREEN        =  30;
  final static char PETSCII_BLUE         =  31;
  final static char PETSCII_SPACE        =  32;
  final static char PETSCII_SHIFT_STAR   =  96;  // screen -32
  final static char PETSCII_PI           = 126;
  final static char PETSCII_ORANGE       = 129;
  final static char PETSCII_F1           = 133;
  final static char PETSCII_F3           = 134;
  final static char PETSCII_F5           = 135;
  final static char PETSCII_F7           = 136;
  final static char PETSCII_F2           = 137;
  final static char PETSCII_F4           = 138;
  final static char PETSCII_F6           = 139;
  final static char PETSCII_F8           = 140;
  final static char PETSCII_SHIFTENTER   = 141;
  final static char PETSCII_SWITCH_UPPER = 142;
  final static char PETSCII_BLACK        = 144;
  final static char PETSCII_UP           = 145;
  final static char PETSCII_RVOF         = 146;
  final static char PETSCII_CLR          = 147;
  final static char PETSCII_INST         = 148;
  final static char PETSCII_BROWN        = 149;
  final static char PETSCII_LTRED        = 150;
  final static char PETSCII_GREY1        = 151;
  final static char PETSCII_GREY2        = 152;
  final static char PETSCII_LTGREEN      = 153;
  final static char PETSCII_LTBLUE       = 154;
  final static char PETSCII_GREY3        = 155;
  final static char PETSCII_PURPLE       = 156;
  final static char PETSCII_LEFT         = 157;
  final static char PETSCII_YELLOW       = 158;
  final static char PETSCII_CYAN         = 159;
  final static char PETSCII_SHIFT_SPACE  = 160;

  final static char EXTENDSCII_DELETE    = 128; //127; // probably 128 is better
  final static char EXTENDSCII_END       = 130; 
  final static char EXTENDSCII_PGDN      = 131; //250; // random - is okay?
  final static char EXTENDSCII_PGUP      = 132; //251; // random - is okay?
// CODES192-223 same as 96-127
// COES 224-254 same as 160-190
// CODE 255 same as 126

//end key

// charset
  Image charsetUpp;
  Image charsetUpp2;
  Image charsetUpp2x1;
  Image charsetUpp3;
  Image bgImage;                //here
// should we start a cursor flashing thread?
  BufferedImage modImage;
  BufferedImage modImage2;
  BufferedImage modImage2x1;
  BufferedImage modImage3;
  BufferedImage charBuffImage = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
// need to make it at least 3 big
//  if (false) {
//    BufferedImage screenBuffImage = new BufferedImage(8 * 40 * 3 + 200, 150 * 8 * 3 + 200, BufferedImage.TYPE_INT_ARGB);    //here
//    BufferedImage bgBuffImage =     new BufferedImage(8 * 40 * 3 + 200, 150 * 8 * 3 + 200, BufferedImage.TYPE_INT_ARGB);        //here
//  } else {
    // 1920 x 1080

    //BufferedImage screenBuffImage = new BufferedImage(2000, 2000, BufferedImage.TYPE_INT_ARGB);    //here
    /*TEST_ONLY*/
    //BufferedImage screenBuffImage = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);    //here
    //BufferedImage screenBuffImage = new BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB);    //here
    BufferedImage screenBuffImage = new BufferedImage(1280, 1100, BufferedImage.TYPE_INT_ARGB);    //here

    //BufferedImage bgBuffImage =     new BufferedImage(2000, 2000, BufferedImage.TYPE_INT_ARGB);        //here  // BUG 20150210 !
//  }
  Image newoffImage;

  public C64Screen(String title) {
    super(title);

  // create the screeen 

    //pgs force this back
    //static_handles=false;
    //static_bgtrans=true;

    bgtrans = static_bgtrans;
    bgshadow = static_bgtrans;

    scale = static_scale;
    scaley = static_scale;
  //bgtrans_ability=static_bgtrans;
  //reshapeScreen();
  //if (bgtrans_ability) { setUndecorated(true); }

    if (!static_handles) {
      setUndecorated(true);
    }
  //setVisible(true);

  // try this
  //this.setBackground(new Color(0x00FFFFB0,true));

  // test only
    //this.setBackground(new Color(0x00000000, true));
  //this.setBackground(new Color(0x00FFFFB0,true));

  //try this to get tab
    setFocusTraversalKeysEnabled(false);        // this allows you to capture tabs

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    addKeyListener(this);       // for listening to key strokes
  //create_screen_updater();
    Toolkit kit = Toolkit.getDefaultToolkit();
  // just for now
    if (true) {
      //normally getClass.getResource(...)
      URL url;
      // static context
      url = C64Screen.class.getResource("images/c64_upp.gif");
      charsetUpp = kit.getImage(url);
      url = C64Screen.class.getResource("images/c64_upp_2.gif");
      charsetUpp2 = kit.getImage(url);
      url = C64Screen.class.getResource("images/c64_upp_2x1.gif");
      charsetUpp2x1 = kit.getImage(url);
      url = C64Screen.class.getResource("images/c64_upp_3.gif");
      charsetUpp3 = kit.getImage(url);
    } else {
      charsetUpp = kit.getImage("images/c64_low.gif");
      charsetUpp2 = kit.getImage("images/c64_upp_2.gif");
      charsetUpp2x1 = kit.getImage("images/c64_low_2x1.gif");
      charsetUpp3 = kit.getImage("images/c64_low_3.gif");
    }
  //bgImage=kit.getImage("mer_banner.jpg"); // here
  //if (bgtrans_ability) { bgImage=kit.getImage("sea_bh_1.jpg"); } // here
    initcolour();
    drawchar_init();
  // initialise screen
    if (allow_pointing) {    
        MouseListener c64mouse = new C64Mouse();
	      // attach to screen
        addMouseListener(c64mouse);
        if (allow_scrolling) {    
          MouseWheelListener c64mousewheel = new C64MouseWheel();
          // attach to screen
          addMouseWheelListener(c64mousewheel);
          topScrollArray=new CircleStringArray(5000);
          botScrollArray=new CircleStringArray(5000);
        }    
        addWindowFocusListener(this); // this is so we dont move the cursor when only trying to get focus on the window
      }    

    //setWindowOpacity(this, 0.5f);
    //com.sun.awt.AWTUtilities.setWindowOpacity(this, 0.5f);
 
    clearscreen();
    if (bgtrans_ability) {
      URL url;
      url = C64Screen.class.getResource("images/background.jpg");
      bgImage = kit.getImage(url);
    }                           // here // jfn
  // this works
  //try {
  //if (bgtrans_ability) { bgImage=ImageIO.read(new File("bh91.jpg")); } 
  //} catch (Exception e) { };

  /// wait for it...
    if (true) {
      MediaTracker mt = new MediaTracker(this);
      mt.addImage(bgImage, 0);
    //mt.addImage(newoffImage,1);
      try {
        mt.waitForID(0);
      //mt.waitForID(1);
      }
      catch(InterruptedException e) {
      }
    }
  //this.repaint();

    reshapeScreen();

    if (static_centre) { setLocationRelativeTo(null); }
    setVisible(true);
    //setMaximizedBounds(new Rectangle(1280, 1100)); //PGS

    create_screen_updater();

    if (bgtrans_ability) {
      //newoffImage = createImage(8 * 40 * 2 + 200 + 1600, 150 * 8 * 2 + 200 + 1200);
      /*TEST_ONLY*/
      //newoffImage = createImage(2, 2);
      //newoffImage = createImage(500, 500);
      newoffImage = createImage(1280, 1100);
    }
    if (bgtrans_ability) {
      newoffGraphics = (Graphics2D) newoffImage.getGraphics();
    }
  /// wait for it...
    if (true) {
      MediaTracker mt = new MediaTracker(this);
    //mt.addImage(bgImage,0);
      mt.addImage(newoffImage, 1);
      try {
      //mt.waitForID(0);
        mt.waitForID(1);
      }
      catch(InterruptedException e) {
      }
    }
  //while ((newoffGraphics==null) || (modImage==null) || (modImage2==null) || (offGraphics==null)) {
  //try {
  //Thread.sleep(10); // in the extreme slowdown
  //} catch (InterruptedException e) {}
  //}

    if (true) {
    this.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                // This is only called when the user releases the mouse button.
                if (verbose) { System.out.println("componentResized"); }
                int x=(getSize().width-50*scale)/scale/8;
                int y=(getSize().height-topY-50*scaley)/scaley/8;

                //setSize(scale * maxX * 8 + 50 * scale, scaley * maxY * 8 + 50 * scaley + topY);
                if (maxX!=x && maxY!=y) { // resize
                   if (x>=maxX*3.0&&scale<2) setScale(scale+2);
                   else if (x>=maxX*1.5&&scale<3) setScale(scale+1);
                   else if (x<=maxX*0.34&&scale>2) setScale(scale-2);
                   else if (x<=maxX*0.75&&scale>1) setScale(scale-1);
                   else {
					  if (x<20) x=20; if (y<20) y=20;
					  if (x>MAXmaxX) x=MAXmaxX;
					  if (y>MAXmaxY) y=MAXmaxY;
					  
					  setColsRows(x,y);
				   }
                   //else setColsRows(maxX,maxY); // reset it back
			    } else  {
                  //print("resized "+x+" "+y);
                  if (x<20) x=20; if (y<20) y=20;
                  if (x>MAXmaxX) x=MAXmaxX;
                  if (y>MAXmaxY) y=MAXmaxY;
                  
                  setColsRows(x,y);
			    }
                
            }
        });

	}

    out = this;                 // for external references
    
    return;
  }

  boolean allow_pointing=true; // allow to move the cursor using the mouse
  boolean allow_scrolling=true;
  CircleStringArray topScrollArray;
  CircleStringArray botScrollArray;
  
  void bufferscrolldown(int lines) {
          for (int n=0; n<lines; ++n) {
            scrolldown(0);                        

            //print(topScrollArray.pull());
            String str;
            char ch[];//=new char[maxX];
            
            ch=topScrollArray.pull();
            for (int i=0; i<ch.length && i<maxX; ++i) screencharColour[i][0]=(short)(ch[i]);
//            for (int i=0; i<maxX; ++i) screencharColour[i][0]=(short)(1);
            ch=topScrollArray.pull();
            for (int i=0; i<ch.length-1 && i<maxX; ++i) screenchar[i][0]=ch[i];
            if (ch.length>1) contmark[0]=(short)ch[ch.length-1];
          }
  }
  
  void bufferscrollup(int lines) {
          for (int n=0; n<lines; ++n) {
            scrollscreen();

//            print(botScrollArray.pull());
            String str;
            char ch[];//=new char[maxX];
            
            ch=botScrollArray.pull();
//            System.out.printf("size o char = %d\n",ch.length);
            for (int i=0; i<ch.length && i<maxX; ++i) screencharColour[i][maxY-1]=(short)(ch[i]&0x0F);
//            for (int i=0; i<maxX; ++i) screencharColour[i][maxY-1]=(short)(1);
            ch=botScrollArray.pull();
            for (int i=0; i<ch.length-1 && i<maxX; ++i) screenchar[i][maxY-1]=ch[i];            
            if (ch.length>1) contmark[maxY-1]=(short)ch[ch.length-1];
          }
  }

  class C64MouseWheel implements MouseWheelListener {
   public void mouseWheelMoved(MouseWheelEvent e) {
       String message;
       String newline="\n";
       int notches = e.getWheelRotation();
       if (notches < 0) {
           message = "Mouse wheel moved UP "
                        + -notches + " notch(es)" + newline;
          bufferscrolldown(-notches*4);
       } else {
           message = "Mouse wheel moved DOWN "
                        + notches + " notch(es)" + newline;
          bufferscrollup(notches*4);
       }
       
       
////       if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
////           message += "    Scroll type: WHEEL_UNIT_SCROLL" + newline;
////           message += "    Scroll amount: " + e.getScrollAmount()
////                   + " unit increments per notch" + newline;
////           message += "    Units to scroll: " + e.getUnitsToScroll()
////                   + " unit increments" + newline;
////           message += "    Vertical unit increment: "
//////               + scrollPane.getVerticalScrollBar().getUnitIncrement(1)
////               + " pixels" + newline;
////       } else { //scroll type == MouseWheelEvent.WHEEL_BLOCK_SCROLL
////           message += "    Scroll type: WHEEL_BLOCK_SCROLL" + newline;
////           message += "    Vertical block increment: "
////  //             + scrollPane.getVerticalScrollBar().getBlockIncrement(1)
////               + " pixels" + newline;
////       }
       if (verbose) System.out.printf("%s\n",message);
    }
    
  }

  
  class C64Mouse extends MouseAdapter {
    public void mousePressed(MouseEvent e) {
      int modifiers = e.getModifiers();
      if ((modifiers & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK && cursEnabled && hasFocus) {
        if (verbose) System.out.println("Left button pressed.");       
        if (verbose) System.out.print("mousepressed ");
        if (verbose) System.out.printf("x=%d y=%d\n",e.getX(), e.getY());
        /* reverse this back to a character position */
        //                drawchar2x1(screenchar[i][j], 0 + 25 * scale + i * 8 * scale, j * 8 * scaley + 25 * scaley + topY, screencharColour[i][j]);
        int cx=(e.getX()-25*scale)/(8*scale);
        int cy=(e.getY()-25*scaley-topY)/(8*scaley);      
        if (cx>=0 && cx<maxX && cy>=0 && cy<maxY)
          gotoXY(cx,cy); 
          print_quotes_on=false; // if we move the cursor - just cancel this mode
          /* better - if nothing is on the line, then move to the start - not implemented yet */          
      }
    }
    
  }

  class CircleStringArray {
    /* create a circular string array */
    /* you can PUSH onto the end or PULL from the end */
    int maxsize=0;
    int head=0;
    int base=0;
    int size=0;
    char buffer[][];
    void push(char charray[]) {
      /* add this string to the array, wrapping and loosing oldest okay */      
      size++;
      if (verbose) System.out.printf("size = %d\n",size);
      head=(head+1)%maxsize;
      buffer[head]=charray;
      /* check if overlapped */      
      if (size>maxsize) { 
        size--; 
        base=(base+1)%maxsize;
      }
    }
    char[] pull() {
      int i;
      if (size==0) return new char[0];
      i=head;
      size--;
      head--; if (head<0) head=maxsize-1;
      return buffer[i];
    }
    void flush() {
      /* truncate it */
      head=maxsize-1;
      base=0;
      size=0;
    }
    CircleStringArray(int maxsize) {
      if (maxsize>0 && maxsize<100000) {
        this.maxsize=maxsize;
        buffer=new char[maxsize][];
        head=maxsize-1;
      }
    }
  }

///////////////////////////////////////////
// window listener events

boolean hasFocus=false;

    public void windowGainedFocus(WindowEvent e) {
        //displayMessage("WindowFocusListener method called: windowGainedFocus.");
        hasFocus=true;
    }

    public void windowLostFocus(WindowEvent e) {
        //displayMessage("WindowFocusListener method called: windowLostFocus.");
        hasFocus=false;
    }

///////////////////////////////////////////
  
  int currentcharset=0;
  public void changeCharSet(int charset) {
    Toolkit kit = Toolkit.getDefaultToolkit();
    if (charset == 0) {
      charsetUpp = kit.getImage(C64Screen.class.getResource("images/c64_upp.gif"));
      charsetUpp2 = kit.getImage(C64Screen.class.getResource("images/c64_upp_2.gif"));
      charsetUpp2x1 = kit.getImage(C64Screen.class.getResource("images/c64_upp_2x1.gif"));
      charsetUpp3 = kit.getImage(C64Screen.class.getResource("images/c64_upp_3.gif"));
      currentcharset=0;
    } else {
      charsetUpp = kit.getImage(C64Screen.class.getResource("images/c64_low.gif"));
      charsetUpp2 = kit.getImage(C64Screen.class.getResource("images/c64_low_2.gif"));
      charsetUpp2x1 = kit.getImage(C64Screen.class.getResource("images/c64_low_2x1.gif"));
      charsetUpp3 = kit.getImage(C64Screen.class.getResource("images/c64_low_3.gif"));
      currentcharset=1;
    }
    initcolour();
    drawchar_init();
    reshapeScreen();
    return;
  }


//------------------------------

  public void load_bgimage(String filename) throws BasicException {

    if (bgtrans_ability) {

      filename=filename.replace(".basic","");
      filename=filename.replace(".txt","");
      System.out.printf("About to read %s\n",filename);
      try {
        //bgImage = ImageIO.read(new File(filename));
	if (filename.startsWith("http")) {
	  URL url = new URL(filename);
          bgImage = ImageIO.read(url.openStream());
	} else {
          bgImage = ImageIO.read(new File(filename));
        }

      } catch (IOException e) {
        System.out.println(e);
        throw new BasicException("BGIMAGE FILE NOT FOUND");
      }
    
      // correct?
      drawchar_init();
      reshapeScreen();
    }
  }

//--------------------------------



  // try this too
  // no didnt work
  public // synchronized 
  void reshapeScreen() {

  ///pgs - force it black ///
  //backgroundColour = fullcolour[0]; // for test 2
  //borderColour = fullcolour[0];     // for test 5
  ///pgs - force it black ///

    //setSize(scale * maxX * 8 + 50 * scale, scaley * maxY * 8 + 50 * scaley + topY);
    /*TEST_ONLY*/
    //setSize(2,2);
    //setSize(500,500);
    // only do this if it actually changes size
    //if (getSize().width!=scale * maxX * 8 + 50 * scale || 
        //getSize().height!=scaley * maxY * 8 + 50 * scaley + topY)

     if (scale * maxX * 8 + 50 * scale>1280) { maxX=(1280-50*scale)/8/scale; setExtendedState(JFrame.NORMAL); }
     if (scaley * maxY * 8 + 50 * scaley + topY>1100) { maxY=(1100-topY-50*scaley)/8/scale; setExtendedState(JFrame.NORMAL); }

     setSize(scale * maxX * 8 + 50 * scale, scaley * maxY * 8 + 50 * scaley + topY);

    offGraphics = (Graphics2D) screenBuffImage.getGraphics();
    if (!bgtrans) {             // here
      offGraphics.setColor(new Color(borderColour, true));
      offGraphics.fillRect(0, 0, scale * maxX * 8 + 50 * scale, scaley * maxY * 8 + 50 * scaley + topY);
      offGraphics.setColor(new Color(backgroundColour, true));
      offGraphics.fillRect(25 * scale, 25 * scaley + topY, scale * maxX * 8, scaley * maxY * 8);
    } else {
    //offGraphics.setColor(new Color(borderColour&0x00FFFFFF,true));
      offGraphics.setColor(new Color(0x00FFFFFF, true));
      offGraphics.fillRect(0, 0, scale * maxX * 8 + 50 * scale, scaley * maxY * 8 + 50 * scaley + topY);
      offGraphics.fillRect(25 * scale, 25 * scaley + topY, scale * maxX * 8, scaley * maxY * 8);
    }

    if (false) { // try NOT doing this and see what happens
      for (int j = 0; j < MAXmaxY; ++j) { //last screen was CLEARed
        for (int i = 0; i < MAXmaxX; ++i) {
        //pscreenchar[i][j]='X'; should
          pscreenchar[i][j] = ' ';
          pscreencharColour[i][j] = 0;
        }
      }
    }
    // I think if you change the DONT size, it will NOT clear the screen
    // But if it changes it DOES clear the screen
    if (true) { // try invalidating the entire "printed" char map, meaning it repaints the LOT
      redrawScreen();
    }

    // now done in redrawScreen
    if (bgtrans) {
        screenBuffImage = new BufferedImage(1280,1100, BufferedImage.TYPE_INT_ARGB);
    }

    repaint();
  }

  // add sync to see if it fixed the blank on META-BGTRANS 1 twice in a row
  // no didnt work
  public // synchronized 
  void redrawScreen() {
    // here we assume that we have done something that means any character already
    // drawn (and cached) is now invalid, we must mark this as so so the next draw
    // will really draw everything
    for (int j = 0; j < MAXmaxY; ++j) { //last screen was CLEARed
      for (int i = 0; i < MAXmaxX; ++i) {
      //pscreenchar[i][j]='X'; should
        //pscreenchar[i][j] = (char)(screenchar[i][j]^1); // to make sure it is always different  !! wrong!!
        pscreenchar[i][j] = (char)(screenchar[i][j]^255); // to make sure it is always different
        pscreencharColour[i][j] = 0;
      }
    }
  }

  public boolean setCols(int cols) {
    return setColsRows(cols, C64Screen.maxY);
  }

  public boolean setRows(int rows) {
    return setColsRows(maxX, rows);
  }

  public boolean setColsRows(int cols, int rows) {
    if (rows >= 20 && rows <= MAXmaxY && cols >= 20 && cols <= MAXmaxX) {
    //// this will clear existing rows!
      if (C64Screen.maxY < rows) {
      // we are growing the screen
        for (int j = C64Screen.maxY; j < rows; ++j) {   //clear ALL the screen that was not previously written
          for (int i = 0; i < maxX; ++i) {
            screenchar[i][j] = ' ';
            screencharColour[i][j] = 0;
          }
        }
      } else {
      // we are shrinking the screen
      // this is only really good when we shrink a full screen, if it is partially empty
      // we will start to loose output
        for (int j = 0; j < rows; ++j) {        //move the last out to the new last output
          for (int i = 0; i < maxX; ++i) {
            screenchar[i][j] = screenchar[i][j + (C64Screen.maxY - rows)];
            screencharColour[i][j] = screencharColour[i][j + (C64Screen.maxY - rows)];
          }
        }
        cursY = cursY - (C64Screen.maxY - rows);        // not sure if we always want to do this
        if (cursY < 0)
          cursY = 0;
        if (cursY >= rows)
          cursY = rows - 1;
      }
      C64Screen.maxY = rows;
      maxX = cols;
      reshapeScreen();
      if (static_centre) setLocationRelativeTo(null); // command line option
      return true;
    }
    return false;
  }

  public boolean makeFaint() {
    for (int i = 0; i < 16; ++i) {
      fullcolour[i] = ((fullcolour[i] & 0x00FCFCFC) >> 2) | 0xFF000000;
    }
    return true;
  }

  public boolean setScale(int scale) {
    if (scale >= 1 && scale <= 3) {
      this.scale = scale;
      this.scaley = scale;
      //if (bgtrans) { setBackgroundTransparent(bgtrans); } // to get a new zerod background
      reshapeScreen();
      if (static_centre) setLocationRelativeTo(null); // command line option
      return true;
    }
    return false;
  }

  // only allow 1 or 2 when scale (x) = 1
  public boolean setScaleY(int scaley) {
    if (this.scale >= 1 && this.scale <= 1 & scaley >= 1 && scaley <= 2) {
      this.scaley = scaley;
      //if (bgtrans) { setBackgroundTransparent(bgtrans); } // to get a new zerod background
      reshapeScreen();
      if (static_centre) setLocationRelativeTo(null); // command line option
      return true;
    }
    return false;
  }

// this isn't working
  long cursflash = 0;
  public void create_screen_updater() {
    
    javax.swing.Timer timer1 = new javax.swing.Timer(screenUpatems, 
      new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          // blink cursor
          if (System.currentTimeMillis() - cursflash >= flashtimems) {
            cursflash = System.currentTimeMillis();
            cursVisible = !cursVisible;
            screenneedsupdate = true;
          }
          repaint_ifupdated();
        }
      }
    );
    timer1.start();
  }

// add synchronized to see if it stops cracked screen scrolls!
// this, together with a synchronized paint stops the "cracked" screen
// problem, but it is VERY SLOW!
  private synchronized void scrollscreen() {
  //private void scrollscreen() {
  // scroll them
    if (allow_scrolling) {
      //String buf="";
      //for (int i = 0; i < maxX; ++i)
      //  buf+=screenchar[i][maxY-1];      
      //topScrollArray.push(buf);
      char ar[]=new char[maxX+1]; // add the contmark too
      for (int i=0;i<maxX;++i) ar[i]=screenchar[i][0];
      ar[maxX]=(char)contmark[0];
      topScrollArray.push(ar);
      ar=new char[maxX];
      for (int i=0;i<maxX;++i) ar[i]=(char)screencharColour[i][0];
      topScrollArray.push(ar);
    }
    for (int j = 0; j < maxY - 1; ++j) {
      for (int i = 0; i < maxX; ++i) {
        screenchar[i][j] = screenchar[i][j + 1];
        screencharColour[i][j] = screencharColour[i][j + 1];
      }
    }
    for (int i = 0; i < maxX; ++i) {
      screenchar[i][maxY-1] = ' '; // this was cursY, change it now
    }
   
    /* continuation mark */
    for (int j = 0; j < maxY-1; ++j) {
      contmark[j]=contmark[j+1];
if (verbose) screencharColour[maxX-2][j] = (contmark[j]==1)?(short)7:0;
    }
    contmark[maxY-1]=0;
    /* continuation mark */

  //drawrelevent(0,maxX,0,maxY);
  }

  private synchronized void scrolldown(int y) {
    // if we are trying to push the bottom line - scroll up!
    //if (y==maxY) { scrollscreen(); return; }
  // scroll them

    if (y==0) {
      if (allow_scrolling) {
        //String buf="";
        //for (int i = 0; i < maxX; ++i)
          //buf+=screenchar[i][0];      
        //botScrollArray.push(buf);
        char ar[]=new char[maxX+1]; // for contmark
        for (int i=0;i<maxX;++i) ar[i]=screenchar[i][maxY-1];
        ar[maxX]=(char)contmark[maxY-1];
        botScrollArray.push(ar);
        ar=new char[maxX];
        for (int i=0;i<maxX;++i) ar[i]=(char)(screencharColour[i][maxY-1]);
        botScrollArray.push(ar);
      }
    }

    for (int j = maxY-1; j > y; --j) {
      for (int i = 0; i < maxX; ++i) {
        screenchar[i][j] = screenchar[i][j-1];
        screencharColour[i][j] = screencharColour[i][j-1];
      }
    }

    for (int i = 0; i < maxX; ++i) {
      screenchar[i][y] = ' ';
    }
   
    /* continuation mark */
    for (int j = maxY-1; j > y; --j) {
      contmark[j]=contmark[j-1];
    }
    contmark[y]=0;
    //contmark[j]=1; /* maybe?? */
    /* continuation mark */

  //drawrelevent(0,maxX,0,maxY);


  }

  private void scrolldelay() {
    if (true) {
    //just for effect, add a delay!
      try {
        Thread.sleep(10);       // in the extreme slowdown
      }
      catch(InterruptedException e) {
      }
    }
  }

// converts a keyboard (PC) character to the best "PETASCII" char
  public char petconvert(char ch) {
    int pos = 0;

    if (ch >= 'a' && ch <= 'z')
      pos = 1 + (ch - 'a');
    else if (ch >= 'A' && ch <= 'Z')
      pos = 1 + (ch - 'A') + 64;
    else if (ch >= 'a' + 128 && ch <= 'z' + 128)
      pos = 1 + (ch - 'a') - 32;
    else if (ch == ' ' + 128)
      //pos = PETSCII_SPACE + 128;           // reversed block  // no - this was wrong! it is still a space
      pos = 96;  // shift space
    else if (ch >= '0' && ch <= '9' || ch >= ' ' && ch <= '?')
      pos = (ch);

    else if (ch == '_')
      pos = 64;               // new translation
//      pos = 31;               // new translation
    else if (ch == '`')
      pos = 31;               // new translation
//      pos = 64;               // new translation

    
    else if (ch == '_')
      pos = (ch);               // special case -testing
    else if (ch >= 91 && ch <= 95)
      pos = (ch - 64);
    else if (ch == '`')
      pos = 31;
    else if (ch == '|')
      pos = 29 + 64;
      //pos = 30+64;
    else if (ch == '\\')
      pos = 30; // second place this exists
    else if (ch == '{') // make this horiz line?
//      pos = 28;
      pos = 115;
    else if (ch == '}')
//      pos = 28;
      pos = 107;
    else if (ch == '~')
      pos = 30 + 64;
// this was a bug - but will break many basic progs      
//    else if (ch == '@')
//      pos = 1 + ('C' - 'A') + 64;       // special case for horizontal line - testing
    else if (ch == '@')
      pos = 0;
      
// new stuff
    else if (ch >= 'A' + 128 && ch <= 'Z' + 128)
      pos = 1 + (ch - 'A' - 64);
    // try these to fill the zero gaps (20160326)
    else if (ch ==179) pos=91; //try this 20160326
    else if (ch ==171) pos=92; //try this 20160326
    else if (ch >=161 && ch <=191) pos=(ch-64);
    else if (ch ==192) pos=64;
    else if (ch ==127) pos=95;
    else if (ch >=219 && ch<=224) pos=ch-128;
    else if (ch >=251 && ch<=254) pos=ch-128;
    else if (ch ==255) pos=94;

    if (pos >= 256)
      pos = 0;
//System.err.printf("PET=%d\n",pos);
    return (char) pos;
  }

// converts a pet screen code back to a pc code this needs more work 

  public char petunconvert(char pos) {
    int ch = 0;
    // new turn 0 into 64
    if (pos==0) ch= 64;
    else if (pos > 0 && pos <= 26)
      //ch = 'A' + pos - 1;
      ch = 'a' + pos - 1; // to make equivalent ???
    else if (pos >= 32 && pos < 64)
      ch = pos;
    else if (pos > 64 && pos <= 26 + 64)
      //ch = 'a' + pos - 1 - 64; // switch this around
      ch = 'A' + pos - 1 - 64;
    else if (pos == 115) ch='{'; // new translations
    else if (pos == 107) ch='}'; // new translations

    else if (pos >= 27 && pos<=30) ch=pos+64; // technical - not normally used
    else if (pos == 31) ch='`'; // 96
    else if (pos == 64) ch='_'; // to match petconvert
    else if (pos == 93) ch='|'; // to match petconvert  124
    else if (pos == 94) ch=126; // to match petconvert  
    else if (pos == 95) ch=127; // to match petconvert  
    //else if (pos == 27) ch=91; // technical - not normally used
    //else if (pos == 28) ch=92; // technical - not normally used
    //else if (pos == 29) ch=93; // technical - not normally used
    //else if (pos == 30) ch='^'; // not pos==94
    //else if (pos == 31) ch='`'; // new translations

    return (char) ch;
  }

  public String petunconvert_encoded(char pos) {
    int ch = 0;
    // new turn 0 into 64
    if (pos==0) ch= 64;
    if (pos > 0 && pos <= 26)
      //ch = 'A' + pos - 1;
      ch = 'a' + pos - 1; // to make equivalent ???
    if (pos >= 32 && pos < 64)
      ch = pos;
    if (pos >= 64 && pos <= 26 + 64)
      //ch = 'a' + pos - 1 - 64; // switch this around
      ch = 'A' + pos - 1 - 64;
    if (pos == 30) ch='^'; // not pos==94
    else if (pos == 31) ch='`'; // new translations
    else if (pos == 115) ch='{'; // new translations
    else if (pos == 107) ch='}'; // new translations
    else if (pos == 91) return "(SHIFT-PLUS)"; // try override
    else if (pos == 91) ch=pos; // try
    //else if (pos == 93) ch=pos; // try
    else if (pos == 93) ch=124; // try
    else if (pos == 27 || pos == 29 || pos==28) ch=pos+64; // try [] add pound

    if (pos == PETSCII_BLACK  +64) return "(blk)";
    else if (pos == PETSCII_WHITE +128) return "(wht)";
    else if (pos == PETSCII_RED   +128) return "(red)";
    else if (pos == PETSCII_CYAN   +64) return "(cyn)";
    else if (pos == PETSCII_PURPLE +64) return "(pur)";
    else if (pos == PETSCII_GREEN +128) return "(grn)";
    else if (pos == PETSCII_BLUE  +128) return "(blu)";
    else if (pos == PETSCII_YELLOW +64) return "(yel)";
    else if (pos == PETSCII_ORANGE +64) return "(orng)";
    else if (pos == PETSCII_BROWN  +64) return "(brn)";
    else if (pos == PETSCII_LTRED  +64) return "(lred)";
    else if (pos == PETSCII_GREY1  +64) return "(gry1)";
    else if (pos == PETSCII_GREY2  +64) return "(gry2)";
    else if (pos == PETSCII_LTGREEN+64) return "(lgrn)";
    else if (pos == PETSCII_LTBLUE +64) return "(lblu)";
    else if (pos == PETSCII_GREY3  +64) return "(gry3)";

    else if (pos == PETSCII_LEFT +64) return "(left)";
    else if (pos == PETSCII_RGHT+128) return "(rght)";
    else if (pos == PETSCII_UP   +64) return "(up)";
    else if (pos == PETSCII_DOWN+128) return "(down)";
    else if (pos == PETSCII_HOME+128) return "(home)";
    else if (pos == PETSCII_CLR  +64) return "(clr)";
    else if (pos == PETSCII_RVON+128) return "(rvon)";
    else if (pos == PETSCII_RVOF +64) return "(rvof)";

    else if (pos == PETSCII_SHIFT_STAR-32) return "(-)";
    else if (pos == PETSCII_SHIFT_SPACE) return "(BLOCK)"; // not really BLOCK any more - just a shift space

    else if (pos == 94) return "(mathpi)";
    else if (pos == 96) return "(SHIFT-SPACE)";
    //if (pos == 97) return "(CBM-A)"; // try this first of the set...
    //if (pos >= 97 && pos <=97+26) return "(CBM-"+(char)('A'+pos-97)+")";
    else if (pos == 11+96) return "(CBM-Q)"; 
    else if (pos == 19+96) return "(CBM-W)"; 
    else if (pos == 17+96) return "(CBM-E)"; 
    else if (pos == 18+96) return "(CBM-R)"; 
    else if (pos == 3+96) return "(CBM-T)"; 
    else if (pos == 23+96) return "(CBM-Y)"; 
    else if (pos == 24+96) return "(CBM-U)"; 
    else if (pos == 2+96) return "(CBM-I)"; 
    else if (pos == 4+96) return "(CBM-@)"; 
    else if (pos == 16+96) return "(CBM-A)"; 
    else if (pos == 14+96) return "(CBM-S)"; 
    else if (pos == 27+96) return "(CBM-F)"; 
    else if (pos == 13+96) return "(CBM-Z)"; 
    else if (pos == 29+96) return "(CBM-X)"; 
    else if (pos == 28+96) return "(CBM-C)"; 
    else if (pos == 30+96) return "(CBM-V)"; 

    else if (pos == 31+96) return "(CBM-B)"; 
    else if (pos == 12+96) return "(CBM-D)"; 
    else if (pos ==  5+96) return "(CBM-G)"; 
    else if (pos == 20+96) return "(CBM-H)"; 
    else if (pos == 21+96) return "(CBM-J)"; 
    else if (pos ==  1+96) return "(CBM-K)"; 
    else if (pos == 22+96) return "(CBM-L)"; 
    else if (pos ==  7+96) return "(CBM-M)"; 
    else if (pos == 10+96) return "(CBM-N)"; 
    else if (pos == 25+96) return "(CBM-O)"; 
    else if (pos == 15+96) return "(CBM-P)"; 
    else if (pos ==    92) return "(LEFT-CHECK)";  // CBM -
    else if (pos ==  8+96) return "(BOT-CHECK)";   // CBM POUND
    else if (pos ==  9+96) return "(SHIFT-POUND)"; 
    else if (pos == 26+96) return "(SHIFT-@)"; 
    else if (pos == 95) return "(BACK-TRIANGLE)"; 
    else if (pos == 102) return "(CBM-PLUS)"; // try 

    else if (pos >= 128+1 && pos <=128+26) return ""+(char) (pos-128); // not encoded! but we need the CTRL code //try

    if (ch!=0) return ""+(char) ch;
    else return "@"; // not valid
  }

  boolean reverse = false;      // should really keep this value! // from previous print settings

  boolean print_quotes_on=false;

   /**
   * This method print a string to the machine screen
   * @param line pet-meta-encoded string
   */
  public void print(String line) { // this is where the strings with quotes around are taken to be the encoded value and printed as such i.e. "(down)" shows as " then a reversed Q then another "
    int i;
    char theChar;
    if (verbosePrint) {
      System.out.printf("%s",line);
    }
    for (i = 0; i < line.length(); ++i) {
    // allow "special" characters
    // backslash followed by [ is a real /
      theChar = line.charAt(i);
if (verbose) System.err.printf("[o:%d]",(int)theChar);
      if (theChar == '\\') {
        if (i + 1 < line.length() && (line.charAt(i + 1) == '(' || line.charAt(i + 1) == '[')) {        // just for now
          theChar = line.charAt(i + 1); // just for now
          i++;
        }
        theChar = petconvert(theChar);
      } else if (theChar == '{' && i<line.length()-2 && Character.isDigit(line.charAt(i+1))) {    // just for now //try
if (verbose) { System.out.printf("looking for {n}\n"); }
        boolean matched = false;
        String cS="";
        for (i++; i < line.length(); ++i) {
           if (line.charAt(i)=='}') {
             matched=true; 
             break;
           }
           if (!Character.isDigit(line.charAt(i))) {
             break;
           } else cS+=line.charAt(i);
        }
        int num=(-1);
        try { 
          num = Integer.parseInt(cS);
        } catch(Exception e) {
        }
        if (num>=0 && num<=255) {
          print(""+petconvert((char)num));
if (verbose) { System.out.printf("print char %d\n",num); }
          continue;
        } else {
          print(cS);
          continue;
        }
      } else if (theChar == '(' || theChar == '[') {    // just for now
      // now read until the next ']'
        String cS;
        cS = "";
        boolean matched = false;
        // if we hit a double quote or new line - finish reading (maybe even a space or another ( or [! // could this become an issue?
        for (i++; i < line.length(); ++i)
          if (line.charAt(i) == '(') { // nested!
             i--; // rewind so we are at the bracket again
             break; 
          } else
          if (line.charAt(i) != ')' && line.charAt(i) != ']' && line.charAt(i) != '\"' && line.charAt(i) != '\n' && line.charAt(i) != '\r') { // just for now
            cS = cS + line.charAt(i);
          } else { 
             if (line.charAt(i) == ')' || line.charAt(i) == ']') matched=true;
             else cS = cS + line.charAt(i); // 20110714 - I THINK - keep the quote or NL
             break; 
          }
        if (!matched) {
          // we never got a full match - just print it
          //print("\\(");          
          print("\\"+theChar); // should be the same char
          if (!cS.equals("")) { print(cS); }           // slight recursion
          continue;
        }
      // special code is now in cS
      //System.out.println("The string is "+cS+"\n\n");
        matched = false;
        for (int j = 0; j < 16; ++j) {
          if (cS.equals(colourname[j])) {
            if (print_quotes_on)
              theChar = (char) (j); //128 + 'O'-'A'+1);
            else
              setcursColour((short) j);
            matched = true;
            break;
          }
          if (cS.equals(colourname_alias[j])) {
            if (print_quotes_on)
              theChar = (char) (j); //128 + 'O'-'A'+1);
            else
              setcursColour((short) j);
            matched = true;
            break;
          }
        }
        if (matched && print_quotes_on) {
          switch ((int)theChar) {
            case 0:  theChar=(char)(PETSCII_BLACK  +64); break;
            case 1:  theChar=(char)(PETSCII_WHITE +128); break;
            case 2:  theChar=(char)(PETSCII_RED   +128); break;
            case 3:  theChar=(char)(PETSCII_CYAN   +64); break;
            case 4:  theChar=(char)(PETSCII_PURPLE +64); break;
            case 5:  theChar=(char)(PETSCII_GREEN +128); break;
            case 6:  theChar=(char)(PETSCII_BLUE  +128); break;
            case 7:  theChar=(char)(PETSCII_YELLOW +64); break;
            case 8:  theChar=(char)(PETSCII_ORANGE +64); break;
            case 9:  theChar=(char)(PETSCII_BROWN  +64); break;
            case 10: theChar=(char)(PETSCII_LTRED  +64); break;
            case 11: theChar=(char)(PETSCII_GREY1  +64); break;
            case 12: theChar=(char)(PETSCII_GREY2  +64); break;
            case 13: theChar=(char)(PETSCII_LTGREEN+64); break;
            case 14: theChar=(char)(PETSCII_LTBLUE +64); break;
            case 15: theChar=(char)(PETSCII_GREY3  +64); break;
          }
        }
      // new to make it break out if we got a colour match
        if (matched) {
          if (!print_quotes_on) continue;
        } else if (cS.equals("BLOCK")) { theChar = petconvert((char) (' ' + 128));
        } else if (cS.equals("BACK-TRIANGLE")) { theChar = (char) (95);
        } else if (cS.equals("BACK-TRIANGLE-REV")) { theChar = (char) (95 + 128);
        } else if (cS.equals("FORWARD-TRIANGLE")) { theChar = (char) ('i');
        } else if (cS.equals("FORWARD-TRIANGLE-REV")) { theChar = (char) ('i' + 128);
        } else if (cS.equals("LOW-HLINE")) { theChar = (char) ('c');
        } else if (cS.equals("UPP-LEFT-LINE")) { theChar = (char) ('p');
        } else if (cS.equals("UPP-RIGHT-LINE")) { theChar = (char) ('n');
        } else if (cS.equals("LOW-LEFT-LINE")) { theChar = (char) ('m');
        } else if (cS.equals("LOW-RIGHT-LINE")) { theChar = (char) (']' + 32);
        } else if (cS.equals("LEFT-CHECK")) { theChar = (char) (92);
        } else if (cS.equals("BOT-CHECK")) { theChar = (char) (104);
        } else if (cS.equals("SHIFT-SPACE")) { theChar = (char) (96);
        } else if (cS.equals("VLINE")) { theChar = (char) (']');
        } else if (cS.equals("HLINE")) { 
               //theChar = petconvert('C'); // was this wrong?
               theChar = (char) (64);

        } else if (cS.startsWith("CBM-")) { 
          if (cS.equals("CBM-Q")) { theChar = (char) (11 + 96);
          } else if (cS.equals("CBM-W")) { theChar = (char) (19 + 96);
          } else if (cS.equals("CBM-E")) { theChar = (char) (17 + 96);
          } else if (cS.equals("CBM-R")) { theChar = (char) (18 + 96);
          } else if (cS.equals("CBM-T")) { theChar = (char) (3 + 96);
          } else if (cS.equals("CBM-Y")) { theChar = (char) (23 + 96);
          } else if (cS.equals("CBM-U")) { theChar = (char) (24 + 96);
          } else if (cS.equals("CBM-I")) { theChar = (char) (2 + 96);
  
          } else if (cS.equals("CBM-@")) { theChar = (char) (4 + 96);
  
          } else if (cS.equals("CBM-A")) { theChar = (char) (16 + 96);
          } else if (cS.equals("CBM-S")) { theChar = (char) (14 + 96);
          } else if (cS.equals("CBM-F")) { theChar = (char) (27 + 96);
  
          } else if (cS.equals("CBM-Z")) { theChar = (char) (13 + 96);
          } else if (cS.equals("CBM-X")) { theChar = (char) (29 + 96);
          } else if (cS.equals("CBM-C")) { theChar = (char) (28 + 96);
          } else if (cS.equals("CBM-V")) { theChar = (char) (30 + 96);

          } else if (cS.equals("CBM-B")) { theChar = (char) (31 + 96);
          } else if (cS.equals("CBM-D")) { theChar = (char) (12 + 96);
          } else if (cS.equals("CBM-G")) { theChar = (char) (5 + 96);
          } else if (cS.equals("CBM-PLUS")) { theChar = (char) (6 + 96);
          } else if (cS.equals("CBM-POUND")) { theChar = (char) (8 + 96);
          } else if (cS.equals("CBM-H")) { theChar = (char) (20 + 96);
          } else if (cS.equals("CBM-J")) { theChar = (char) (21 + 96);
          } else if (cS.equals("CBM-K")) { theChar = (char) (1 + 96);
          } else if (cS.equals("CBM-L")) { theChar = (char) (22 + 96);
          } else if (cS.equals("CBM-M")) { theChar = (char) (7 + 96);
          } else if (cS.equals("CBM-N")) { theChar = (char) (10 + 96);
          } else if (cS.equals("CBM-O")) { theChar = (char) (25 + 96);
          } else if (cS.equals("CBM-P")) { theChar = (char) (15 + 96);
          } else { 
            // we are committed, just 
            print("\\(");           // this should be the [ or ( as original
            if (!cS.equals("")) { print(cS); }           // slight recursion
            print(")");           // and matching closing
            continue;
          }
// try this
        } else if (cS.equals("mathpi")) { theChar = (char) (94);

        } else if (cS.equals("SHIFT-POUND")) { theChar = (char) (9 + 96);
        } else if (cS.equals("SHIFT-@")) { theChar = (char) (26 + 96);
        } else if (cS.equals("SHIFT-PLUS")) { theChar = (char) (91);
        } else if (cS.equals("-")) { theChar = (char) (64); // "(-)"
        } else if (cS.equals("left")) { 
          if (print_quotes_on) theChar = (char) (PETSCII_LEFT + 64); 
          else {
            gotoXY(cursX - 1, cursY);
            continue;
          }
        } else if (cS.equals("rght")) { 
          if (print_quotes_on) theChar = (char) (PETSCII_RGHT + 128); // note
          else {
            gotoXY(cursX + 1, cursY);
            continue;
          }
        } else if (cS.equals("UP") || cS.equals("up")) { 
          if (print_quotes_on) theChar = (char) (128 + 'Q'-'A'+1+64); // PETSCII_UP + 64 I think
          else {
            gotoXY(cursX, cursY - 1);
            continue;
          }
        } else if (cS.equals("DOWN") || cS.equals("down")) { 
          if (print_quotes_on) theChar = (char) (128 + 'Q'-'A'+1);
          else {
            gotoXY(cursX, cursY + 1);
            continue;
          }
        } else if (cS.equals("home")) { 
          if (print_quotes_on) theChar = (char) (128 + 'S'-'A'+1);
          else {
            gotoXY(0, 0);
            continue;
          }
        } else if (cS.equals("clr")) { 
          if (print_quotes_on) theChar = (char) (128 + 'S'-'A'+1+64);
          else {
            clearscreen();
            continue;
          }
        } else if (cS.equals("rvon")) { 
          if (print_quotes_on) theChar = (char) (128 + 'R'-'A'+1);
          else {
            reverse = true;
            continue;
          }
        } else if (cS.equals("rvof")) { 
          if (print_quotes_on) theChar = (char) (128 + 'R'-'A'+1+64);
          else {
            reverse = false;
            continue;
          }
        } else if (cS.equals("CR")) {
        // same as println()
          reverse = false; // try - think this should be here 
          if (cursY < maxY - 1) {
            cursY++;
          } else {
          // we are on the last line of the screen so scroll it
            scrollscreen();
            if (slowdown)
              scrolldelay();
          }
          cursX = 0;
          print_quotes_on=false; //try - think i need to add it here too
          continue;
        } else {
        // we dont know what it is so print the whole thing
          //print("\\(");           // this should be the [ or ( as original
          print("\\"+theChar);
          if (!cS.equals("")) { print(cS); }           // slight recursion
          if (theChar=='(') {
            print(")");           // and matching closing
          } else {
            print("]");           // and matching closing
          }
          continue;
        }
      } else if (theChar == '\"') {
        print_quotes_on=!print_quotes_on; // toggle it
      } else if ((int) (theChar & 0xFF) == 10) { // && !print_quotes_on) {   // try ignoring if quotes on - breaks other things - do this later
        println();              // is it okay to put it in here?       //try, no
        print_quotes_on=false; // cancel it
        continue;
      } else if (theChar == PETSCII_ENTER || theChar == PETSCII_SHIFTENTER) {
        println();
        print_quotes_on=false; // cancel it
        continue;
      } else if (theChar == EXTENDSCII_END) {
        /* dont quote this */
        gotoendline();
        continue;
      } else if (theChar == EXTENDSCII_PGDN) {
        /* dont quote this */
        bufferscrollup(maxY-1); // not quite a whole screen
        continue;
      } else if (theChar == EXTENDSCII_PGUP) {
        /* dont quote this */
        bufferscrolldown(maxY-1); // not quite a whole screen
        continue;
      } else if (theChar == EXTENDSCII_DELETE) {
        /* dont quote this */
        backspace(0);
        continue;
      } else if (theChar == PETSCII_DEL) {        // backspace or move left 0x08 // 157 left move // 20 leftdel
        /* dont quote this */
        backspace(-1);
        continue;
      } else if (theChar == PETSCII_INST && !print_quotes_on) {        // backspace or move left 0x08 // 157 left move // 20 leftdel
        /* dont quote this */
        if(!insertchars) insertspace();  // if you are already inserting chars - dont do this
        continue;

      } else if (print_quotes_on && (int) (theChar) >=0 && (int) (theChar) <=27) { // try this
          theChar = (char) (128+theChar); // got a non-specified CTRL code
          if (verbose) System.out.printf("got a non-specified CTRL code\n");

      } else if (theChar == PETSCII_SWITCH_LOWER) {
        // case down?
        changeCharSet(1);
        continue;
      } else if (theChar == PETSCII_SWITCH_UPPER) {
        // case up?
        changeCharSet(0);
        continue;

      } else if (theChar == '\r') {
        println();              // is it okay to put it in here?     
        continue;
// same as metacodes worked out here - but it has been worked out elsewhere

// I dont think this is actually used except if you printchr$(xx)
      } else if (print_quotes_on && theChar >= 128 && theChar <= 159) {
        // technical hardly used
        theChar = (char) (theChar + 64);
      } else if (theChar == PETSCII_BLACK) {
        if (!print_quotes_on) { setcursColour((short) 0); continue; }
      } else if (theChar == PETSCII_WHITE) {
        if (print_quotes_on) theChar = (char) (128 + PETSCII_WHITE);
        else { setcursColour((short) 1); continue; }
      } else if (theChar == PETSCII_RED) {
        if (print_quotes_on) theChar = (char) (128 + PETSCII_RED);
        else { setcursColour((short) 2); continue; }
      } else if (theChar == PETSCII_CYAN) {
        if (print_quotes_on) theChar = (char) (PETSCII_CYAN); // technical hardly used
        else { setcursColour((short) 3); continue; }
      } else if (theChar == PETSCII_PURPLE) {
        if (print_quotes_on) theChar = (char) (PETSCII_PURPLE); //technical hardly used
        else { setcursColour((short) 4); continue; }
      } else if (theChar == PETSCII_GREEN) {
        if (print_quotes_on) theChar = (char) (128 + PETSCII_GREEN);
        else { setcursColour((short) 5); continue; }
      } else if (theChar == PETSCII_BLUE) {
        if (print_quotes_on) theChar = (char) (128 + PETSCII_BLUE);
        else { setcursColour((short) 6); continue; }
      } else if (theChar == PETSCII_YELLOW) {
        if (print_quotes_on) ; //theChar = (char) (PETSCII_YELLOW); //technical hardly used
        else { setcursColour((short) 7); continue; }
      } else if (theChar == PETSCII_ORANGE) {
        if (print_quotes_on) ; //theChar = (char) (PETSCII_ORANGE); //technical hardly used
        else { setcursColour((short) 8); continue; }
      } else if (theChar == PETSCII_BROWN) {
        if (print_quotes_on) ; // theChar = (char) (); //technical hardly used
        else { setcursColour((short) 9); continue; }
      } else if (theChar == PETSCII_LTRED) {
        if (print_quotes_on) ; // theChar = (char) (128 + 5); //technical hardly used
        else { setcursColour((short) 10); continue; }
      } else if (theChar == PETSCII_GREY1) {
        if (print_quotes_on) ; // theChar = (char) (128 + 5); //technical hardly used
        else { setcursColour((short) 11); continue; }
      } else if (theChar == PETSCII_GREY2) {
        if (print_quotes_on) ; // theChar = (char) (128 + 5); //technical hardly used
        else { setcursColour((short) 12); continue; }
      } else if (theChar == PETSCII_LTGREEN) {
        if (print_quotes_on) ; // theChar = (char) (128 + 5); //technical hardly used
        else { setcursColour((short) 13); continue; }
      } else if (theChar == PETSCII_LTBLUE) {
        if (print_quotes_on) ; // theChar = (char) (128 + 5); //technical hardly used
        else { setcursColour((short) 14); continue; }
      } else if (theChar == PETSCII_GREY3) {
        if (print_quotes_on) ; // theChar = (char) (128 + 5); //technical hardly used
        else { setcursColour((short) 15); continue; }
      } else if ((int) (theChar & 0xFF) == PETSCII_HOME) {
          if (print_quotes_on) theChar = (char) (128+PETSCII_HOME);
          else {
            gotoXY(0, 0);
            continue;
          }
      } else if ((int) (theChar & 0xFF) == PETSCII_UP) {
          if (print_quotes_on) theChar = (char) (64+PETSCII_UP);
          else {
            if (true && cursY==0) {
              bufferscrolldown(1);
            } else
              gotoXY(cursX, cursY - 1);
            continue;
          }
      } else if ((int) (theChar & 0xFF) == PETSCII_DOWN) {
          if (print_quotes_on) theChar = (char) (128+PETSCII_DOWN);
          else {
            if (true && cursY==maxY-1) {
              bufferscrollup(1);
            } else
              gotoXY(cursX, cursY + 1);
            continue;
          }
      } else if ((int) (theChar & 0xFF) == PETSCII_LEFT) {
          if (print_quotes_on) theChar = (char) (64+PETSCII_LEFT);
          else {
            gotoXY(cursX - 1, cursY);
            continue;
          }
      } else if ((int) (theChar & 0xFF) == PETSCII_RGHT) {
          if (print_quotes_on) theChar = (char) (128+PETSCII_RGHT);
          else {
            gotoXY(cursX + 1, cursY);
            continue;
          }
      } else if ((int) (theChar & 0xFF) == PETSCII_RVON) {
          if (print_quotes_on) theChar = (char) (128+PETSCII_RVON);
          else {
            reverse = true;
            continue;
          }
      } else if ((int) (theChar & 0xFF) == PETSCII_RVOF) {
          if (print_quotes_on) theChar = (char) (64+PETSCII_RVOF);
          else {
            reverse = false;
            continue;
          }
      } else if ((int) (theChar & 0xFF) == PETSCII_CLR) {
          if (print_quotes_on) theChar = (char) (64+PETSCII_CLR);
          else {
            clearscreen();
            continue;
          }

      } else if ((int) (theChar) >=0 && (int) (theChar) <=27) { // try this
          if (verbose) System.out.printf("got a non-specified CTRL code unquoted - ignoring\n");
          continue;

      } else {
        theChar = petconvert(theChar);
      }
    /* normal processing */
//System.err.printf("<sA:%d%s>",(int)theChar,reverse?"|128":"");
      screenchar[cursX][cursY] = (char) (theChar ^ (reverse ? 128 : 0));        // for reverse
      screencharColour[cursX][cursY] = cursColour;
      if (cursX == maxX - 1) {  // same as in println

        /* continuation mark */
        if (cursY==0 || contmark[cursY-1]==0||contmarks_infinite) {
          contmark[cursY]=1;
          //screenchar[cursX][cursY] |= 128;
          if (verbose) screencharColour[cursX][cursY] = 7;
          /* if there is any text on the next line (or a continuation mark), then scroll it down */
          /* this will be inefficient if we dont keep track of used lines */
          // NO if(insertchars) scrolldown(cursY+1); /* lines y+1 onward scroll down one line */
        }
        /* continuation mark */

        cursX = 0;

        if (cursY == maxY - 1) {
          scrollscreen();
          if (slowdown)
            scrolldelay();
        } else {
          cursY++;
        }
      } else {
        cursX++;
      }
    }
    repaint();                  // another one!
  }

  public void println(String line) {

    print(line);
    if (verbosePrint) { System.out.printf("\n"); }
  // same as println()
    if (cursY < maxY - 1) {
      cursY++;
    } else {
    // we are on the last line of the screen so scroll it
      scrollscreen();
      if (slowdown)
        scrolldelay();
    }
    cursX = 0;

  // now signal it to be redrawn
    print_quotes_on=false; // try
    repaint();
  }

  public void println() {
    if (verbosePrint) { System.out.printf("\n"); }
    reverse = false;            // don't know if this really is the case
  // here we define the line to be written on the screen
  // for now, implement this as an array of lines
  // should really stop it from repainting whilst inside this routine
    if (cursY < maxY - 1) {
      cursY++;
    } else {
      scrollscreen();
      if (slowdown)
        scrolldelay();
    }
    cursX = 0;

  // now signal it to be redrawn
    print_quotes_on=false; // try
    repaint();
  }

  public void setcursColour(short colour) {
    cursColour = colour;
    repaint();
  }

  public void setcursColour(String colour) {
    for (int i = 0; i < 16; ++i) {
      if (colour.equals(colourname[i])) {
        setcursColour((short) i);
      }
    }
    return;
  }

  public void writechar(char ch) {
  // here we define the line to be written on the screen
  // for now, implement this as an array of lines
  // should really stop it from repainting whilst inside this routine
  // note, doesnt wrap yet
    screenchar[cursX][cursY] = petconvert(ch);
    screencharColour[cursX][cursY] = cursColour;
    if (cursX == maxX - 1) {
      println();
    } else {
      cursX++;
    }
  // now signal it to be redrawn
    repaint();
  }

  public void writechar_raw(char ch) {
  // here we define the line to be written on the screen
  // for now, implement this as an array of lines
  // should really stop it from repainting whilst inside this routine
  // note, doesnt wrap yet
    screenchar[cursX][cursY] = ch;
    screencharColour[cursX][cursY] = cursColour;
    if (cursX == maxX - 1) {
      println();
    } else {
      cursX++;
    }
  // now signal it to be redrawn
    repaint();
  }

  public void gotoendline() {
    /* continuation mark */
    /* new backspace */
    /* do it to the end of the logical (contination) line */
    int x;
    int y;
    int nx,ny;
    int ox=cursX;
    int oy=cursY;
    if (verbose) System.out.printf("gotoendline\n\r");
    x=cursX;
    y=cursY;

    while (true) {
      nx=x+1; ny=y; if (nx==maxX) { if (contmark[y]==1) { nx=0; ny++; } else { ny=maxY; } }
      if (ny==maxY) { 
        screenchar[x][y] = ' ';
        // we are now at the end - work back to the last non space
        break;
      }
      x=nx; y=ny;
    }

    /* now work back from x,y to ox,oy */
    while (ox!=x || oy!=y) {
      nx=x-1; ny=y; if (nx<0) { nx=maxX-1; ny--; }
      if (screenchar[nx][ny]!=' ') break;
      x=nx; y=ny;
    }
    gotoXY(x,y);

    repaint();    
  }

  // make a new backspace that will pull all characters back on the current SOFT line
  // first draft now
  public void backspace(int xmove) {
    if (true) {

      /* continuation mark */
      /* new backspace */
      /* do it to the end of the logical (contination) line */
      if (cursX+xmove>=0 || cursY>0 && contmark[cursY-1]==1) {
        int x;
        int y;
        int nx,ny;
if (verbose) System.out.printf("backspacing (new)\n\r");
        cursX+=xmove;
        if (cursX<0) { cursX=maxX-1; cursY--; }
        x=cursX;
        y=cursY;

        //if (screenchar[x][y]=='"') print_quotes_on=!print_quotes_on;
        if (screenchar[x][y]=='"') print_quotes_on=false; // no just always clear it
        while (true) {
          nx=x+1; ny=y; if (nx==maxX) { if (contmark[y]==1) { nx=0; ny++; } else { ny=maxY; } }
          if (ny==maxY) { 
            screenchar[x][y] = ' ';
            break;
          }
          screenchar[x][y] = screenchar[nx][ny];
          screencharColour[x][y] = screencharColour[nx][ny];
          x=nx; y=ny;
        }
      }
      /* continuation mark */

    } else {
      if (cursX > 0) {
        screenchar[cursX - 1][cursY] = ' ';
        cursX--;
      }
    }
    repaint();
  }

  // make a new backspace that will pull all characters back on the current SOFT line
  // first draft now

  boolean contmarks_infinite=true;

  public void insertspace() {

      /* continuation mark */
      /* new backspace */
      /* do it to the end of the logical (contination) line */
        int ox=cursX;
        int oy=cursY;
        int x=cursX;
        int y=cursY;
        int nx,ny;
if (verbose) System.out.printf("insertspace (new)\n\r");

        while (true) {
          nx=x+1; ny=y; 
          if (nx==maxX) { 
            nx=0; ny++;
            if (contmark[y]==0) break;
          }
          if (ny==maxY) break;
          x=nx; y=ny;
        }
        /* x,y now contain the LAST character */
        /* should reall count the number of inserts like the real c64 */

        /* if we started at the end of the line -assume we will want to extend it */
        if (contmark[y]==0 && (screenchar[x][y]!=' ' || ox==x && oy==y)) {
           //if (y==oy && (oy==0 || contmark[oy-1]==0 || contmarks_infinite)) {
           if (true) {  // I think 
            //contmark[oy]=1; /* done by scroll down anyway ??*/
            contmark[y]=1; /* done by scroll down anyway ??*/
            /* scroll the lines below down! */
            if (y==maxY-1) { // we need to scroll up - and change all our variables
              scrollscreen();
              oy--; ny--; cursY--;
            } else 
              scrolldown(y+1); /* lines y+1 onward scroll down one line */
            /* I THINK we need to advance the marker */
            x=nx; y=ny;
          } else {
            if (verbose) System.out.printf("line full - the last char is %d\n",(int)screenchar[x][y]);
            return;
          }
        }
        

if (verbose) System.out.printf("%d,%d to %d,%d\n",ox,oy,x,y);

        /* now work back from x,y to ox,oy */
        while (ox!=x || oy!=y) {
          nx=x-1; ny=y; if (nx<0) { nx=maxX-1; ny--; }
          screenchar[x][y] = screenchar[nx][ny];
          screencharColour[x][y] = screencharColour[nx][ny];
          x=nx; y=ny;
        }
        screenchar[x][y]=' ';

    repaint();

      /* continuation mark */
  }

  public void gotoXY(int tocursX, int tocursY) {
  // if you try to move off the screen, it will try to fix that up
    if (tocursX < 0 && tocursY > 0) {
      tocursY--;
      tocursX = maxX - 1;
    }
    if (tocursY < 0) {
      tocursY = 0;
    }
    if (tocursX >= maxX) {
      tocursY++;
      tocursX = 0;
    }
    if (tocursY >= maxY) {
      tocursY = maxY - 1;
      scrollscreen();
    }
    if (tocursX >= 0 && tocursX < maxX && tocursY >= 0 && tocursY < maxY) {
      cursX = tocursX;
      cursY = tocursY;
      repaint();
    }
  }

  public void setChar(int x, int y, char memval) {
    screenchar[x][y] = memval;
    repaint();
  }

  public void setCharColour(int x, int y, char memval) {
    screencharColour[x][y] = (short) memval;
    repaint();
  }

// Timings
  long tstart;
  long ttime_paint;
  long ttime_paintl;
  long ttime_paintm;
  long tcount_paint = 0;

//         Graphics2D offGraphics;

//public void paint(Graphics g)
// try this
  public synchronized void paint(Graphics g) {
    Graphics2D g2d = (Graphics2D) g;

    if (true) {
      tstart = System.currentTimeMillis();
      tcount_paint++;
    }
  // sometimes this is not ready yet
    if (offGraphics != null) {
    // draw all the lines
      if (modImage != null && modImage2 != null && modImage2x1 !=null && modImage3 != null) { // hold off until ready
        for (int j = 0; j < maxY; ++j) {
          for (int i = 0; i < maxX; i++) {

            if (pscreenchar[i][j] == screenchar[i][j] && pscreencharColour[i][j] == screencharColour[i][j]) {
//System.out.printf("skipping [%d][%d] %d,%d ",i,j,(int)screenchar[i][j],(int)screencharColour[i][j]);
            } else {
              pscreenchar[i][j] = screenchar[i][j];
              pscreencharColour[i][j] = screencharColour[i][j];

              if (scale == 1 && scaley==2) { // new way
                drawchar2x1(screenchar[i][j], 0 + 25 * scale + i * 8 * scale, j * 8 * scaley + 25 * scaley + topY, screencharColour[i][j]);
              } else if (scale == 1) {
//System.err.printf("drawchar[%d]",(int)screenchar[i][j]);
                drawchar(screenchar[i][j], 0 + 25 * scale + i * 8 * scale, j * 8 * scaley + 25 * scaley + topY, screencharColour[i][j]);
              } else if (scale == 2) {
                drawchar2(screenchar[i][j], 0 + 25 * scale + i * 8 * scale, j * 8 * scaley + 25 * scaley + topY, screencharColour[i][j]);
              } else if (scale == 3) {
                drawchar3(screenchar[i][j], 0 + 25 * scale + i * 8 * scale, j * 8 * scaley + 25 * scaley + topY, screencharColour[i][j]);
              }
            }
          }
          if (true && j == 4) {
            ttime_paintl = System.currentTimeMillis() - tstart;
          }
        }
      // drawcursor - in the curColor
        if (cursEnabled && cursVisible) {
          if (pscreenchar[cursX][cursY] == (char) (' ' + 128) && pscreencharColour[cursX][cursY] == cursColour) {
          } else {
          //pscreenchar[cursX][cursY] = (char) (' ' + 128); // originally
            pscreenchar[cursX][cursY] = (char) (((int)
                                                 pscreenchar[cursX][cursY] + 128) % 256);
            pscreencharColour[cursX][cursY] = cursColour;

            if (scale == 1 && scaley==2) {
              drawchar2x1((char) (pscreenchar[cursX][cursY]), 0 + 25 * scale + cursX * 8 * scale, cursY * 8 * scaley + 25 * scaley + topY, cursColour);
            } else if (scale == 1) {
//System.err.printf("drawchar[p%d]",(int)pscreenchar[cursX][cursY]);
              drawchar((char) (pscreenchar[cursX][cursY]), 0 + 25 * scale + cursX * 8 * scale, cursY * 8 * scaley + 25 * scaley + topY, cursColour);
            } else if (scale == 2) {
              drawchar2((char) (pscreenchar[cursX][cursY]), 0 + 25 * scale + cursX * 8 * scale, cursY * 8 * scaley + 25 * scaley + topY, cursColour);
            } else if (scale == 3) {
              drawchar3((char) (pscreenchar[cursX][cursY]), 0 + 25 * scale + cursX * 8 * scale, cursY * 8 * scaley + 25 * scaley + topY, cursColour);
            }
          }
        }
      }

      if (true) {
        ttime_paintm = System.currentTimeMillis() - tstart;
      }

      if (bgtrans) {
        if (newoffGraphics != null) { 
          // get positon of window - work out how much slower this is
          if (shiftbgimage) {
          Point p=this.getLocation();
          
            // if (verbose) System.out.printf("%d,%d\n",p.x,p.y);
            newoffGraphics.drawImage(bgImage, 0, 0, 8 * scale * maxX + 50 * scale, 8 * scaley * maxY + 50 * scaley + topY, 
              p.x, p.y, p.x+8 * scale * maxX + 50 * scale, p.y+8 * scaley * maxY + 50 * scaley + topY, 
              this);
          } else {
            newoffGraphics.drawImage(bgImage, 0, 0, 8 * scale * maxX + 50 * scale, 8 * scaley * maxY + 50 * scaley + topY, 
              0, 0, 8 * scale * maxX + 50 * scale, 8 * scaley * maxY + 50 * scaley + topY,
              this);
          }
          newoffGraphics.drawImage(screenBuffImage, 0, 0, 8 * scale * maxX + 50 * scale, 8 * scaley * maxY + 50 * scaley + topY, 0, 0, 8 * scale * maxX + 50 * scale, 8 * scaley * maxY + 50 * scaley + topY, this);

          g2d.drawImage(newoffImage, 0, 0, this);
        }
      } else {
      // normally
        if (false) {
          g2d.drawImage(screenBuffImage, 0, 0, 8 * scale * maxX + 50 * scale, 8 * scaley * maxY + 50 * scaley + topY, 0, 0, 8 * scale * maxX + 50 * scale, 8 * scaley * maxY + 50 * scaley + topY, this);
        } else {
          if (newoffGraphics != null) { //here
          // this method seems twice as efficient, but needs proper testing - yes it is faster
            newoffGraphics.drawImage(screenBuffImage, 0, 0, 8 * scale * maxX + 50 * scale, 8 * scaley * maxY + 50 * scaley + topY, 0, 0, 8 * scale * maxX + 50 * scale, 8 * scaley * maxY + 50 * scaley + topY, this);
            g2d.drawImage(newoffImage, 0, 0, this);
          }
        }

      }

    }

    if (true) {
      ttime_paint = System.currentTimeMillis() - tstart;
    }
  }

  public void printstats() {
    System.out.println("Config: scale=" + scale + " rows=" + maxY + " cols=" + maxX);
    System.out.println("ttime-paintm          = " + ttime_paintm + " ms");
    System.out.println("ttime-paintl (5 lines)= " + ttime_paintl + " ms");
    System.out.println("ttime-paint           = " + ttime_paint + " ms");
    System.out.println("ttime-paint draw      = " + (ttime_paint - ttime_paintm) + " ms");
    System.out.println("tcount-paint = #" + tcount_paint);
    System.out.println("--------------------------");
  }

  boolean dots[][];
  boolean dots2[][];
  boolean dots2x1[][];
  boolean dots3[][];
  public void drawchar_init() {
    dots = new boolean[32 * 8][8 * 8];
  //System.out.println("Allocated space for dots");
    if (dots == null) {
      System.out.println("but it is null");
    }
    for (int posx = 0; posx < 32; ++posx) {
      for (int posy = 0; posy < 8; ++posy) {
        for (int i = 0; i < 8; ++i) {
          for (int j = 0; j < 8; ++j) {
            int rgb = modImage.getRGB(posx * 8 + i, posy * 8 + j);
            if ((rgb & 0xFFFFFF) == 0) {
              dots[posx * 8 + i][posy * 8 + j] = true;
            } else {
              dots[posx * 8 + i][posy * 8 + j] = false;
            }
          }
        }
      }
    }

    dots2 = new boolean[32 * 16][8 * 16];
  //System.out.println("Allocated space for dots");
    if (dots2 == null) {
      System.out.println("but it is null");
    }
    for (int posx = 0; posx < 32; ++posx) {
      for (int posy = 0; posy < 8; ++posy) {
        for (int i = 0; i < 16; ++i) {
          for (int j = 0; j < 16; ++j) {
            int rgb = modImage2.getRGB(posx * 16 + i, posy * 16 + j);
            if ((rgb & 0xFFFFFF) == 0) {
              dots2[posx * 16 + i][posy * 16 + j] = true;
            } else {
              dots2[posx * 16 + i][posy * 16 + j] = false;
            }
          }
        }
      }
    }

    dots2x1 = new boolean[32 * 8][8 * 16];
  //System.out.println("Allocated space for dots");
    if (dots2x1 == null) {
      System.out.println("but it is null");
    }
    for (int posx = 0; posx < 32; ++posx) {
      for (int posy = 0; posy < 8; ++posy) {
        for (int i = 0; i < 8; ++i) {
          for (int j = 0; j < 16; ++j) {
            int rgb = modImage2x1.getRGB(posx * 8 + i, posy * 16 + j);
            if ((rgb & 0xFFFFFF) == 0) {
              dots2x1[posx * 8 + i][posy * 16 + j] = true;
            } else {
              dots2x1[posx * 8 + i][posy * 16 + j] = false;
            }
          }
        }
      }
    }

    dots3 = new boolean[32 * 8 * 3][8 * 8 * 3];
  //System.out.println("Allocated space for dots");
    if (dots3 == null) {
      System.out.println("but it is null");
    }
    for (int posx = 0; posx < 32; ++posx) {
      for (int posy = 0; posy < 8; ++posy) {
        for (int i = 0; i < 8 * 3; ++i) {
          for (int j = 0; j < 8 * 3; ++j) {
            int rgb = modImage3.getRGB(posx * 8 * 3 + i, posy * 8 * 3 + j);
            if ((rgb & 0xFFFFFF) == 0) {
              dots3[posx * 8 * 3 + i][posy * 8 * 3 + j] = true;
            } else {
              dots3[posx * 8 * 3 + i][posy * 8 * 3 + j] = false;
            }
          }
        }
      }
    }
  }

  char[][] pscreenchar = new char[MAXmaxX][MAXmaxY];
  short[][] pscreencharColour = new short[MAXmaxX][MAXmaxY];

  public void drawchar(char ch, int x, int y, int colour) {
    int pos;
    int posx;
    int posy;
    int col;

    pos = ch;
//System.err.printf("<%d>(%d)",pos,(int)pscreenchar[cursX][cursY]);
    posx = pos % 32;
    posy = pos / 32;
    col = fullcolour[colour];
  // modify here the colours of the source to match what we want
  // then draw it onto the offGraphics place
    for (int i = 0; i < 8; ++i) {
      for (int j = 0; j < 8; ++j) {
      //int rgb=modImage.getRGB(posx*8+i,posy*8+j);
        int xx = i * scale + x;
        int yy = j * scaley + y; // note scale y as we only have 8 bits here!
        if (dots[posx * 8 + i][posy * 8 + j]) {
          if (faint)
            screenBuffImage.setRGB(xx, yy, col & 0x60FFFFFF);   // and here
          else
            screenBuffImage.setRGB(xx, yy, col);
          if (scale == 1 && scaley == 2) {
            if (faint)
              screenBuffImage.setRGB(xx, yy + 1, col & 0x60FFFFFF);
            else
              screenBuffImage.setRGB(xx, yy + 1, col);
          }
          if (scale == 2) {
            screenBuffImage.setRGB(xx + 1, yy, col);
            screenBuffImage.setRGB(xx, yy + 1, col);
            screenBuffImage.setRGB(xx + 1, yy + 1, col);
          }
        } else {
          if (bgtrans && bgshadow && (j > 0 && dots[posx * 8 + i][posy * 8 + j - 1] || (i > 0 && dots[posx * 8 + i - 1][posy * 8 + j]))) {
          //screenBuffImage.setRGB(xx,yy,backgroundColour & 0x50FFFFFF); //here // 60 worked well //40good, but sometimes too suttle
            screenBuffImage.setRGB(xx, yy, backgroundColour & 0x70FFFFFF);      //here // 60 worked well //40good, but sometimes too suttle
            if (scale == 1 && scaley == 2) {
              // in testing, set to yellow - got an interesting effect fullcolour[7]
              screenBuffImage.setRGB(xx, yy+1, backgroundColour & 0x70FFFFFF);      //here // 60 worked well //40good, but sometimes too suttle
            }
          } else if (bgtrans) {
            screenBuffImage.setRGB(xx, yy, backgroundColour & 0x00FFFFFF);      //here
            if (scale == 1 && scaley == 2)
              screenBuffImage.setRGB(xx, yy + 1, backgroundColour & 0x00FFFFFF);
          } else {
            screenBuffImage.setRGB(xx, yy, backgroundColour);
            if (scale == 1 && scaley == 2)
              screenBuffImage.setRGB(xx, yy + 1, backgroundColour);
          }
          if (scale == 2) {
            screenBuffImage.setRGB(xx + 1, yy, backgroundColour);
            screenBuffImage.setRGB(xx, yy + 1, backgroundColour);
            screenBuffImage.setRGB(xx + 1, yy + 1, backgroundColour);
          }
        }
      }
    }
  }

  public void drawchar2x1(char ch, int x, int y, int colour) {
    int pos;
    int posx;
    int posy;
    int col;

    pos = ch;
    posx = pos % 32;
    posy = pos / 32;
    col = fullcolour[colour];
  // modify here the colours of the source to match what we want
  // then draw it onto the offGraphics place
    for (int i = 0; i < 8; ++i) {
      for (int j = 0; j < 16; ++j) {
        int xx = i + x;
        int yy = j + y;
        if (dots2x1[posx * 8 + i][posy * 16 + j]) {
          if (faint)
            screenBuffImage.setRGB(xx, yy, col & 0x60FFFFFF);   // try this
          else
            screenBuffImage.setRGB(xx, yy, col);
        } else {
          if (bgtrans) {
            screenBuffImage.setRGB(xx, yy, backgroundColour & 0x00FFFFFF);
          } else {
            screenBuffImage.setRGB(xx, yy, backgroundColour);
          }
        }
      }
    }
  }

  public void drawchar2(char ch, int x, int y, int colour) {
    int pos;
    int posx;
    int posy;
    int col;

    pos = ch;
    posx = pos % 32;
    posy = pos / 32;
    col = fullcolour[colour];
  // modify here the colours of the source to match what we want
  // then draw it onto the offGraphics place
    for (int i = 0; i < 16; ++i) {
      for (int j = 0; j < 16; ++j) {
        int xx = i + x;
        int yy = j + y;
        if (dots2[posx * 16 + i][posy * 16 + j]) {
          screenBuffImage.setRGB(xx, yy, col);
        } else {
          if (bgtrans) {
            screenBuffImage.setRGB(xx, yy, backgroundColour & 0x00FFFFFF);
          } else {
            screenBuffImage.setRGB(xx, yy, backgroundColour);
          }
        }
      }
    }
  }
  public void drawchar3(char ch, int x, int y, int colour) {
    int pos;
    int posx;
    int posy;
    int col;

    pos = ch;
    posx = pos % 32;
    posy = pos / 32;
    col = fullcolour[colour];
  // modify here the colours of the source to match what we want
  // then draw it onto the offGraphics place
    for (int i = 0; i < 8 * 3; ++i) {
      for (int j = 0; j < 8 * 3; ++j) {
        int xx = i + x;
        int yy = j + y;
//System.out.printf("%d,%d ",xx,yy);
        if (dots3[posx * 8 * 3 + i][posy * 8 * 3 + j]) {
          screenBuffImage.setRGB(xx, yy, col);
        } else {
          if (bgtrans) {
            screenBuffImage.setRGB(xx, yy, backgroundColour & 0x00FFFFFF);
          } else {
            screenBuffImage.setRGB(xx, yy, backgroundColour);
          }

        }
      }
    }
  }

  public void clearscreen() {
    for (int j = 0; j < MAXmaxY; ++j) { //clear ALL the screen
      for (int i = 0; i < MAXmaxX; ++i) {
        screenchar[i][j] = ' ';
        screencharColour[i][j] = 0;
      }
    }
    /* contination mark */
    for (int i = 0; i < maxY; ++i) {
      contmark[i]=0;
    }
    /* contination mark */

    cursX = 0;
    cursY = 0;
    //forcedrepaint();                  // this is new - wasn't doing this before, 
                                // this will hopefully fix the chars left on screen problem
    if (allow_scrolling) { botScrollArray.flush(); } /* clear everything below in the scroll buffer */
    if (true && allow_scrolling) { topScrollArray.flush(); } /* clear everything above!! in the scroll buffer */
  }

  public String getline(int from_cursX, int upto_cursX) {
  // return the current line above the cursor  (we would have hit enter already)
    String rets = "";
  //int y = cursY - 1; // was -now no CR (yet)
    int y = cursY;
    if (y < 0) {
      y = 0;
    }
  //for (int i = from_cursX; i < maxX && i < upto_cursX; ++i) {
    for (int i = from_cursX; i < maxX; ++i) {
      rets = rets + petunconvert_encoded(screenchar[i][y]);
    }
    println();                  // do it the other way around!
    return rets;
  }

  public String getline(int from_cursX) {
  // return the current line above the cursor  (we would have hit enter already)
    String rets = "";
    int y = cursY;
    if (y < 0) y = 0;

    /* continuation mark */
    /* move up to start of input */
    while (y>0 && contmark[y-1]==1) y--;
    while (contmark[y]==1) {
if (verbose) System.out.printf("Got a contination!\n\r");
      for (int i = from_cursX; i < maxX; ++i) {
        rets = rets + petunconvert_encoded(screenchar[i][y]);
      }
      from_cursX=0; /* back to start of line */
      y++;
    }
    while (cursY!=maxY-1 && cursY<y+1-1) {
      println();
    }
    /* continuation mark */

    for (int i = from_cursX; i < maxX; ++i) {
      rets = rets + petunconvert_encoded(screenchar[i][y]);
    }
    println();                  // do it the other way around!

if (verbose) System.out.printf("About to return line %s\n",rets);
    return rets;
  }

  public void startupscreen(int fg, int bg, int cc, boolean starttext) {
    setcursColour((short)cc);
    backgroundColour = fullcolour[fg];
    borderColour = fullcolour[bg];
    clearscreen();
    reshapeScreen();            // just to see - this is a dodgy work around!!! when changing background or border colours, must reshape screen
    if (!starttext) {
      println("");
      println(" **** c=64 approximator basic v2ae ****");
      println("");
      println(" java ram system  many basic bytes free");
    }
  }
  public void startupscreen_blank() {
    setcursColour("LIGHT BLUE"); // now done here
    backgroundColour = fullcolour[6];
    borderColour = fullcolour[6 + 8];
    clearscreen();
    reshapeScreen();            // just to see - this is a dodgy work around!!! when changing background or border colours, must reshape screen
  }
  public void startupscreen() {
    setcursColour("LIGHT BLUE"); // now done here
    backgroundColour = fullcolour[6];
    borderColour = fullcolour[6 + 8];
    clearscreen();
    reshapeScreen();            // just to see - this is a dodgy work around!!! when changing background or border colours, must reshape screen
    if (false) {
      println("");
      println("    **** commodore 64 basic v2 ****");
      println("");
      println(" 64k ram system  38911 basic bytes free");
    } else {
      println("");
      println(" **** c=64 approximator basic v2ae ****");
      println("");
      println(" java ram system  many basic bytes free");
    }
  }

  public void initcolour() {
    modImage = toBufferedImage(charsetUpp);
    modImage2 = toBufferedImage(charsetUpp2);
    modImage2x1 = toBufferedImage(charsetUpp2x1);
    modImage3 = toBufferedImage(charsetUpp3);
  }
  public void update(Graphics g) {
    paint(g);
  }

/// key stuff
//  little extra key stuff - FAST REPEAT option
//

  // key stack - mini array of just held down keys only
  static int MAXKS=10; // only five keys at same time
  int ks_code[]=new int[MAXKS];
  char ks_key[]=new char[MAXKS];
  int ks_total=0;
  long ks_last=0;
  int ks_lastkey=0;
  //int ks_next=0;
  void keypush(int code, char key) {
    int i;
    if (verbose) System.out.printf("got keypush [%d,%d]\n",code,(int)key);
    for (i=0; i<MAXKS; ++i) if (ks_code[i]==code) { ks_key[i]=key; return; }
    for (i=0; i<MAXKS; ++i) if (ks_code[i]==0) { ks_total++; ks_code[i]=code; ks_key[i]=key; return; }
    ks_code[0]=code; ks_key[0]=key; return; // overflow - just replace the first one
    //ks_next++; if (ks_next==MAXKS) ks_next=0;
    //ks_code[ks_next]=code; ks_key[ks_next]=key; return;
  }

  void keyrelease(int code) {
    int i;
    if (verbose) System.out.printf("got keyrelease [%d] total=%d\n",code,ks_total);
    for (i=0; i<MAXKS; ++i) if (ks_code[i]==code) { ks_code[i]=0; ks_total--; return; }
  }

//
//
  boolean altdown = false;
  boolean ctrldown = false;
  boolean tabdown = false;

  public void addkey2buf(char key) {
        keybuf[keybuftop] = key;
        keybuftop++; if (keybuftop >= keybufmax) { keybuftop = 0; }

	//
	keypush(ks_lastkey,key);
  }
  
  public void keyPressed(KeyEvent e) {
  // first look at it to see if it is just a shift/control/alt event
    if (verbose) { System.out.printf("\n[keycode=%d keychar=%d] ",(int)e.getKeyCode(),(int)e.getKeyChar()); }
    //
    ks_lastkey=e.getKeyCode();

    if (e.getKeyCode() == KeyEvent.VK_ALT) {
      altdown = true;
    }
    if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
      ctrldown = true;
    }
    if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
      if (altdown && ctrldown) { changeCharSet(1-currentcharset); }
    }


    switch (e.getKeyCode()) {
      case KeyEvent.VK_UP:        addkey2buf(PETSCII_UP);        return;
      case KeyEvent.VK_DOWN:      addkey2buf(PETSCII_DOWN);      return;
      case KeyEvent.VK_LEFT:      addkey2buf(PETSCII_LEFT);      return;
      case KeyEvent.VK_RIGHT:     addkey2buf(PETSCII_RGHT);      return;
//    case KeyEvent.VK_INSERT:    addkey2buf(PETSCII_INST);      return;
      case KeyEvent.VK_INSERT:    insertchars=!insertchars;      return; // toggle the insert mode
      case KeyEvent.VK_DELETE:    addkey2buf(EXTENDSCII_DELETE); return;
      case KeyEvent.VK_END:       addkey2buf(EXTENDSCII_END);    return;
      case KeyEvent.VK_PAGE_DOWN: addkey2buf(EXTENDSCII_PGDN);    return;
      case KeyEvent.VK_PAGE_UP:   addkey2buf(EXTENDSCII_PGUP);    return;

      case KeyEvent.VK_BACK_SPACE:        
        if (e.isShiftDown()||e.isControlDown())
          addkey2buf(PETSCII_INST);
        else
          addkey2buf(PETSCII_DEL);
        return;

      case 10:
        if (e.isShiftDown()||e.isControlDown())
          addkey2buf(PETSCII_SHIFTENTER);
        else
          addkey2buf(PETSCII_ENTER);
        return;

      case 32:
        if (e.isShiftDown()) {
          addkey2buf(PETSCII_SHIFT_SPACE);
          return;
        }
    }
    
    if (e.getKeyCode() == 36) {
      if (e.isShiftDown()||e.isControlDown()) {
        addkey2buf(PETSCII_CLR);
        return;
      } else {
        addkey2buf(PETSCII_HOME);
        return;
      }
    }
    if (tabdown && e.getKeyChar()=='9') {
        addkey2buf(PETSCII_RVON);
        return;
    }
    if (tabdown && e.getKeyChar()=='0') {
        addkey2buf(PETSCII_RVOF);
        return;
    }

    if (e.getKeyCode() == KeyEvent.VK_TAB) {
      tabdown = true;
      return;
    }
    if (e.getKeyChar()==27) {
      if (e.isShiftDown()) { // force it to be shifted (i.e. shift-runstop)
        has_controlC=true; // but chew it up
        addkey2buf((char)BREAK_KEY); // just anything really - force givemekey to return
        return;
      } else {
        return; // chew it up anyway
      }
    }
    if (e.getKeyChar() != KeyEvent.CHAR_UNDEFINED) {

    // cutpaste
      if ((char) e.getKeyCode() == 'V' && e.isControlDown() && e.isShiftDown()) {
        // paste
        print(textTransfer.getClipboardContents());
        return;
      }                         // really needs more than this to trigger
    
      if ((char) e.getKeyCode() == 'C' && e.isControlDown() && e.isShiftDown()) {
        // cut - for now - the WHOLE screen!
        {
          String cut="";
          for (int j = 0 ; j < maxY; ++j) {
            for (int i = 0 ; i < maxX; ++i) {
              cut = cut + petunconvert_encoded(screenchar[i][j]);
            }
            if (contmark[j]==0) cut+="\n";
          }
          textTransfer.setClipboardContents(cut.trim()+"\n"); // because it is the whole screen, trimmed, add \n
        }
        return;
      }                         // really needs more than this to trigger
    
    // special codes first
      if ((char) e.getKeyCode() == 'H' && e.isAltDown()) {
        faint = !faint;
        redrawScreen();
        forcedrepaint();
        return;
      }                         // really needs more than this to trigger
      if ((char) e.getKeyCode() == 'B' && e.isAltDown()
          && bgtrans_ability) {
        bgtrans = !bgtrans;
        // cant do without deleting fram // setUndecorated(bgtrans);
        setBackgroundTransparent(bgtrans); //note - sort of double setting it!
        return;
      }                         // really needs more than this to trigger
      if ((char) e.getKeyCode() == 'D' && e.isAltDown()) {
        bgshadow = !bgshadow;
        redrawScreen();
        forcedrepaint();
        return;
      }                         // really needs more than this to trigger
      if ((char) e.getKeyCode() == 'S' && e.isAltDown()) {
        slowdown = !slowdown;
        forcedrepaint();
        return;
      }                         // really needs more than this to trigger

// this is plainly wrong! - no conversion to pet!
      // this will totally change all programs with input of get functions
      //{
		//int c=(int)e.getKeyChar()&0xFF;
		//if (c>=97&&c<=122) keybuf[keybuftop]=(char)(c-32);
		//else if (c>=65 && c<=90) keybuf[keybuftop]=(char)(c+128);
		//else
      //{
		//int c=(int)e.getKeyChar()&0xFF;
		//if (c>=65&&c<=90) keybuf[keybuftop]=(char)(c+128);
        //else

// swap 95 96 at keyboard stage
//         if ((char) e.getKeyChar() == '_') {
//           keybuf[keybuftop] = '`'; 
//		 } else if ((char) e.getKeyChar() == '`') {
//           keybuf[keybuftop] = '_'; 
//		 } else
           keybuf[keybuftop] = e.getKeyChar(); 
      //}
      if (verbose) { System.out.printf("got key %d\n",(int)e.getKeyChar()); }
      if (e.isAltDown()) {
        keybuf[keybuftop] += 128;
      } else if (e.isControlDown() && keybuf[keybuftop] >= '0' && keybuf[keybuftop] <= '9') {
        keybuf[keybuftop] += 128+64; // move these to another place now!
      } else if (e.isControlDown()) {
        //keybuf[keybuftop] += 'a' + 128 - 1;
        // try this -> map to correct
        switch(keybuf[keybuftop]) {
          // note to clear the 128+digit bit, we use the shifted (apart from B)
          case 1: keybuf[keybuftop] = 176+64*0; break;
          case 2: keybuf[keybuftop] = 191; break;
          case 3: keybuf[keybuftop] = 188+64*0; break;
          case 4: keybuf[keybuftop] = 172+64*0; break;
          case 5: keybuf[keybuftop] = 177+64*0; break;
          case 6: keybuf[keybuftop] = 187+64*0; break;
          case 7: keybuf[keybuftop] = 185+64*0; break;
          case 8: keybuf[keybuftop] = 180+64*0; break;
          case 9: keybuf[keybuftop] = 162+64*0; break;
          case 10: keybuf[keybuftop] = 181+64*0; break;
          case 11: keybuf[keybuftop] = 161+64*0; break;
          case 12: keybuf[keybuftop] = 182+64*0; break;
          case 13: keybuf[keybuftop] = 167+64*0; break;
          case 14: keybuf[keybuftop] = 170+64*0; break;
          case 15: keybuf[keybuftop] = 185+64*0; break;
          case 16: keybuf[keybuftop] = 175+64*0; break;
          case 17: keybuf[keybuftop] = 171+64*0; break;
          case 18: keybuf[keybuftop] = 178+64*0; break;
          case 19: keybuf[keybuftop] = 174+64*0; break;
          case 20: keybuf[keybuftop] = 163+64*0; break;
          case 21: keybuf[keybuftop] = 184+64*0; break;
          case 22: keybuf[keybuftop] = 190+64*0; break;
          case 23: keybuf[keybuftop] = 179+64*0; break;
          case 24: keybuf[keybuftop] = 189+64*0; break;
          case 25: keybuf[keybuftop] = 183+64*0; break;
          case 26: keybuf[keybuftop] = 173+64*0; break;
          default:
            keybuf[keybuftop] += 'a' + 128 - 1;
        }
      } else if (tabdown && keybuf[keybuftop] >= '0' && keybuf[keybuftop] <= '9') {
        keybuf[keybuftop] += 256;
      } else if (tabdown && keybuf[keybuftop] >= 'a' && keybuf[keybuftop] <= 'z') {
        keybuf[keybuftop] = (char)(keybuf[keybuftop]-'a'+1); // try this
      } else if (tabdown && keybuf[keybuftop] >= '@' && keybuf[keybuftop] <= '[') {
        keybuf[keybuftop] = (char)(keybuf[keybuftop]-'A'+1); // try this
      }

      //
      
      keypush(ks_lastkey,keybuf[keybuftop]);
      
      keybuftop++;
      if (keybuftop >= keybufmax) {
        keybuftop = 0;
      }
    } else {
    // do nothing
      if (verbose) { System.out.printf("[donothing:%d] ",(int)e.getKeyCode()); }
    }
  }

  // add sync - this fixes the blanked screen on two META-BGTRANS 1 in a row (intermittent)
  public synchronized void setBackgroundTransparent(boolean setbgtrans) {
    bgtrans=setbgtrans;
    if (true && bgtrans) {
      // create a NEW buffered image as we want all zeros again (in case it is going to be used transparently // this is a bit of a dogy way to do it
      //OLDif (false)
        //OLDscreenBuffImage = new BufferedImage(8 * 40 * 3 + 200, 150 * 8 * 3 + 200, BufferedImage.TYPE_INT_ARGB);
      //OLDelse
        //
        //screenBuffImage = new BufferedImage(2000,2000, BufferedImage.TYPE_INT_ARGB);
        /*TEST_ONLY*/
        //screenBuffImage = new BufferedImage(2,2, BufferedImage.TYPE_INT_ARGB);
        //screenBuffImage = new BufferedImage(500,500, BufferedImage.TYPE_INT_ARGB);
        screenBuffImage = new BufferedImage(1280,1100, BufferedImage.TYPE_INT_ARGB);
    }
    reshapeScreen();

    return;
  }

  TextTransfer textTransfer = new TextTransfer(); /* for clipboard */

  boolean insertchars=true;

  public String screenInput() {
  // save the cursor input, if we remain on the same line, we will return only after the original cursor
    int from_cursX = cursX;
    int from_cursY = cursY;
    while (true) {
      char ch;
      ch = givemekey();
      if (verbose) { System.out.printf("got %d ",(int)ch); }
      if (ch == BREAK_KEY) {
        // arbitrary break key
        return ""; // is this good?
      } else if (ch == PETSCII_SHIFTENTER) {
        print_quotes_on=false; // special - fix up the quotes
        getline(0);  
        from_cursX = cursX;
        from_cursY = cursY;
        // just continue
      } else if (ch == PETSCII_ENTER) { // was '\n') {
        int upto_cursX = cursX;
        int upto_cursY = cursY; // in case we have scrolled the screen!        
        print_quotes_on=false; // special - fix up the quotes

        /* contination mark */
        /* if we have a continued line - do the default input */
        if (upto_cursY == from_cursY && (cursY==0 || contmark[cursY-1]==0))        
          return getline(from_cursX); //, upto_cursX); // we stayed on the same line
        else
          return getline(0);        
        /* contination mark */

      } else if (ch >= 256 + '0' && ch <= 256 + '9') {
        if (insertchars && print_quotes_on) insertspace();
        print("("+colourname_alias[(short) ((ch-1) % 16)]+")");
      } else if (ch >= 128+64 + '0' && ch <= 128+64 + '9') {
        if (insertchars && print_quotes_on) insertspace();
        print("("+colourname_alias[(short) ((ch + 8 -1) % 16)]+")");
      } else if (ch==EXTENDSCII_DELETE || ch==PETSCII_DEL || ch==EXTENDSCII_END) {
        print(ch+"");
      } else if (ch==EXTENDSCII_PGDN || ch==EXTENDSCII_PGUP) {
        print(ch+"");
      } else if (
          //(ch>=1 && ch<=26) || //try this
           ch==PETSCII_UP 
        || ch==PETSCII_DOWN 
        || ch==PETSCII_LEFT 
        || ch==PETSCII_RGHT 
        || ch==PETSCII_RVON
        || ch==PETSCII_RVOF 
        || ch==PETSCII_HOME 
        || ch==PETSCII_CLR
          ) {
          // generisize : normally non-printing
          if (insertchars && print_quotes_on) insertspace();
          print(ch+"");

      } else {
        if (insertchars) insertspace();
        print(ch+""); // convert it to a string (of one char)
      }
    }
  }

  public void keyReleased(KeyEvent e) {
    if (e.getKeyCode() == KeyEvent.VK_ALT) {
      altdown = false;
    }
    if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
      ctrldown = false;
    }
    if (e.getKeyCode() == KeyEvent.VK_TAB) {
      tabdown = false;
    }
    // if enabled (currently will always be enabled, then send to back on key release
    if (sendToBack) {
      this.toBack(); // put it in the background
    }
    //
    keyrelease(e.getKeyCode());
  }

  public void keyTyped(KeyEvent e) {
  }

// add a new special one
// do this a different way!!
boolean has_controlC;

  public boolean hasControlC() {
    boolean ret;
    ret=has_controlC;
    has_controlC=false; // and clear it
    return ret;
  }

  public boolean hasControlC_oldway() {
  // 131 is ctrl alt c - for now
  // change to be the last typed char
    if (hasinput() && keybuf[(keybuftop!=0)?keybuftop-1:keybufmax-1] == (char) 131) {
      keybufbot=keybuftop; // chew up all chars
      return true;
    } else if (hasinput() && keybuf[(keybuftop!=0)?keybuftop-1:keybufmax-1] == (char) 27) {
      // escape too
      keybufbot=keybuftop; // chew up all chars
      // perhaps - better - throw an exception!!
      return true;
    } else
      return false;
  }

  public boolean hasinput() {
    if (keybuftop != keybufbot) {
      return true;
    };
    return false;
  }

  public char givemekey() {
    return givemekey(true);
  }

  public void slowinput() {
    /* does nothing but slows the input on no key - usually for loops of get */
    if (keybuftop == keybufbot && SLOW_INPUT_MS>=0) {
      try {
        Thread.sleep(SLOW_INPUT_MS); // was 10 now 1
      }
      catch(InterruptedException e) {
      }
    }
  }

  public char givemekey(boolean hasCursor) {
    char returnval;
  // is this the right spot?
    if (hasCursor) {
      cursEnabled = true;
      cursVisible = true;
      repaint();
    }
    forcedrepaint();            //dodgy work around
  // should block until there is a key!
    while (keybuftop == keybufbot && GIVEMEKEY_MS>=0) {
      try {
        Thread.sleep(GIVEMEKEY_MS); // was 10
      }
      catch(InterruptedException e) {
      }
    }
    returnval = keybuf[keybufbot];
    keybufbot++;
    if (keybufbot >= keybufmax) {
      keybufbot = 0;
    }
    if (hasCursor) {
      cursEnabled = false;
      cursVisible = false;
    }                           //repaint(); 
    return returnval;
  }

  // you will always get the original one - plus hold down repeats if empty and key still down
  public String givemefastkey() {
    char returnval;

    // use the mini array - when all empty in here - push entire set on
    if (keybuftop==keybufbot) 
      for (int i=0; i<MAXKS; ++i) if (ks_code[i]!=0) {
        keybuf[keybuftop]=ks_key[i];
        keybuftop++;
        if (keybuftop >= keybufmax) { keybuftop = 0; }
      }
    if (keybuftop==keybufbot) 
      return "";

    returnval = keybuf[keybufbot];
    keybufbot++;
    if (keybufbot >= keybufmax) { keybufbot = 0; }
    return ""+returnval;
  }

// TRY this
//int qval=0; // this works
  long qval = 0;

  public void forcedrepaint() {
    qval = 0;
    repaint();
  }

  boolean screenneedsupdate = false;
  public void repaint_ifupdated() {
    if (screenneedsupdate) {
      qval = 0;
      repaint();
      screenneedsupdate = false;
    }
  }

  public void repaint() {
  //if (qval%100==0) { // this works!
  // the value 100 needs tuning, 100 gives mostly good output on powerful, non-loaded machine
  // this is an extreme slowdown...
    if (true && slowdown && System.currentTimeMillis() - qval > 10) {   // was 200 // was 200 again
    // call the real one
      super.repaint();
      qval = System.currentTimeMillis();
    } else if ((false || !slowdown) && System.currentTimeMillis() - qval > 100) {       // was 200 // was 200 again // had it set to 50 for a while
    // call the real one
      super.repaint();
      qval = System.currentTimeMillis();
    } else {
    // set a timer, to get it to repaint finally, (after everything stops)
    // one shot, just to repaint, if it is already going, reset the timer
      screenneedsupdate = true;
    }
  //qval++; // this works!
  }

//public drawcursor() {
//// draw a little block where the cursor is
//}
//public undrawcursor() {
//}

// This method returns a buffered image with the contents of an image
  public static BufferedImage toBufferedImage(Image image) {
    if (image instanceof BufferedImage) {
      return (BufferedImage) image;
    }
  // This code ensures that all the pixels in the image are loaded
    image = new ImageIcon(image).getImage();

  // Determine if the image has transparent pixels; for this method's
  // implementation, see e661 Determining If an Image Has Transparent Pixels
  //boolean hasAlpha =  ComponentColorModel.hasAlpha(image);
    boolean hasAlpha = true;

  // Create a buffered image with a format that's compatible with the screen
    BufferedImage bimage = null;
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    try {
    // Determine the type of transparency of the new buffered image
      int transparency = Transparency.OPAQUE;
      if (hasAlpha) {
        transparency = Transparency.BITMASK;
      }
    // Create the buffered image
      java.awt.GraphicsDevice gs = ge.getDefaultScreenDevice();
      GraphicsConfiguration gc = gs.getDefaultConfiguration();
      bimage = gc.createCompatibleImage(image.getWidth(null), image.getHeight(null), transparency);
    }
    catch(HeadlessException e) {
    // The system does not have a screen
    }

    if (bimage == null) {
    // Create a buffered image using the default color model
      int type = BufferedImage.TYPE_INT_RGB;
      if (hasAlpha) {
        type = BufferedImage.TYPE_INT_ARGB;
      }
      bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
    }
  // Copy image to buffered image
    Graphics g = bimage.createGraphics();

  // Paint the image onto the buffered image
    g.drawImage(image, 0, 0, null);
    g.dispose();

    return bimage;
  }

}

///end
