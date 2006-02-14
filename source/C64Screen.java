//////////////////////////////////////////////////////////////////////////////////
//
// $Id: C64Screen.java,v 1.22 2004/12/28 03:45:34 pgs Exp $
//                                                               
// $Log: C64Screen.java,v $
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
//
//////////////////////////////////////////////////////////////////////////////////

import javax.swing.*;
import java.awt.*;
import java.awt.event.*; // for key events
import java.awt.Toolkit.*; // for image loading
import java.awt.image.*;
import java.util.*;

// for funky imageio stuff
import java.io.*;
import javax.imageio.*;

// things to do:
// . change the screen writing process from being done EVERY paint
//   to being done on change only, and painting the static image
//   -tried this 10's to 100's of times slower!!!
// . not display until all scrolls etc finished (otherwise blanking etc shows up)
//--------------------------------------------------------------------------
// alseo 200412-- in order to radically change the screen
// how about removing the object (and therefore frame!) and creating a new one

class C64Screen extends JFrame implements KeyListener {
  Graphics2D offGraphics;
  Graphics2D newoffGraphics;
  int cursX=0;
  int cursY=0;
  short cursColour=2;
  boolean cursVisible=false;
  boolean cursEnabled=false;

  static public C64Screen out=null; // a pointer to the last (really first) instansiated screen for 
    // external use (like Screen.out, but C64Screen.screen)

  String colourname[]={"BLACK","WHITE","RED","CYAN","PURPLE","GREEN","BLUE","YELLOW","ORANGE",
                        "BROWN","LIGHT RED","GREY 1","GREY 2","LIGHT GREEN","LIGHT BLUE","GREY 3"};
  int fullcolour[]={
        0xFF000000, // black
        0xFFFFFFFF, // white
        0xFFE00000, // red //0xFFFF2020, // red
        0xFF20FFFF, // cyan
        0xFFD020FF, // purple        was magenta 0xFFFF20FF, 
        0xFF20E020, // green         was 0xFF20FF20, // green
        0xFF4040C0, // blue          was 0xFF2020A0, // dark blue //3030B0 seems good
        0xFFFFFF20, // yellow
        0xFFC87020, // orange        was 0xFF808020, // brown
        0xFF704020, // brown
        0xFFFF8080, // light red
        0xFF404040, // grey 1
        0xFF808080, // grey 2
        0xFF80FF80, // light green
        0xFFA0A0FF, // light blue
        0xFFA0A0A0  // grey 3
      };
          //modImage.setRGB(posx*8+i,posy*8+j,0xFFA0AAFF);
  int backgroundColour=fullcolour[0]; // for test 2
  int borderColour=fullcolour[0]; // for test 5
  
  //int scale=2;
  //int scale=1;
  int scale=1;
  //int maxX=40;
  int maxX=40;
  static int maxY=25;
  final static int MAXmaxX=80;
  final static int MAXmaxY=125;
  int topY=12;
  //String lines[]=new String[maxY];
  char[][] screenchar=new char[MAXmaxX][MAXmaxY];
  short[][] screencharColour=new short[MAXmaxX][MAXmaxY];
  boolean faint=false;
  boolean bgtrans=true;
  boolean bgtrans_ability=true;
  boolean bgshadow=true;
  boolean slowdown=false;

  static boolean static_bgtrans=true;  // for a dodgy initialisation
  static boolean static_handles=false;  // for a dodgy initialisation

  //key
  static int keybufmax=1000;
  int keybuftop=0;
  int keybufbot=0;
  char keybuf[] = new char[keybufmax];
  //end key

  // charset
  Image charsetUpp;
  Image charsetUpp2;
  Image bgImage; //here
  // should we start a cursor flashing thread?
  BufferedImage modImage;
  BufferedImage modImage2;
  BufferedImage charBuffImage=new BufferedImage(64,64,BufferedImage.TYPE_INT_ARGB);
  BufferedImage screenBuffImage=new BufferedImage(8*40*2+200,150*8*2+200,BufferedImage.TYPE_INT_ARGB); //here
  BufferedImage bgBuffImage=new BufferedImage(8*40*2+200,150*8*2+200,BufferedImage.TYPE_INT_ARGB); //here
  Image newoffImage;

  public C64Screen(String title) {
    super(title);
    // create the screeen 
    bgtrans=static_bgtrans;
    bgshadow=static_bgtrans;
    //bgtrans_ability=static_bgtrans;
    //reshapeScreen();
    //if (bgtrans_ability) { setUndecorated(true); }
    if (!static_handles) { setUndecorated(true); }
    //setVisible(true);

    // try this
    //this.setBackground(new Color(0x00FFFFB0,true));
    this.setBackground(new Color(0x00000000,true));

    //try this to get tab
    setFocusTraversalKeysEnabled(false); // this allows you to capture tabs

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    addKeyListener(this); // for listening to key strokes
    //create_screen_updater();
    Toolkit kit = Toolkit.getDefaultToolkit();
    charsetUpp=kit.getImage("c64_upp.gif");
    charsetUpp2=kit.getImage("c64_upp_2.gif");
    //bgImage=kit.getImage("mer_banner.jpg"); // here
    //if (bgtrans_ability) { bgImage=kit.getImage("sea_bh_1.jpg"); } // here
    initcolour(); 
    drawchar_init();
    // initialise screen
    clearscreen();
    if (bgtrans_ability) { bgImage=kit.getImage("bh91.jpg"); } // here // jfn
    // this works
    //try {
    //if (bgtrans_ability) { bgImage=ImageIO.read(new File("bh91.jpg")); } 
    //} catch (Exception e) { };

    /// wait for it...
    if (true) {
      MediaTracker mt = new MediaTracker(this);
      mt.addImage(bgImage,0);
      //mt.addImage(newoffImage,1);
      try {
        mt.waitForID(0);
        //mt.waitForID(1);
      } catch (InterruptedException e) { }
    }

    //this.repaint();

    reshapeScreen();

    setVisible(true);

    create_screen_updater();

    if (bgtrans_ability) { 
      newoffImage=createImage(8*40*2+200+1600,150*8*2+200+1200);
    }
    if (bgtrans_ability) { 
      newoffGraphics=(Graphics2D) newoffImage.getGraphics(); 
    }
    /// wait for it...
    if (true) {
      MediaTracker mt = new MediaTracker(this);
      //mt.addImage(bgImage,0);
      mt.addImage(newoffImage,1);
      try {
        //mt.waitForID(0);
        mt.waitForID(1);
      } catch (InterruptedException e) { }
    }

   //while ((newoffGraphics==null) || (modImage==null) || (modImage2==null) || (offGraphics==null)) {
      //try {
        //Thread.sleep(10); // in the extreme slowdown
      //} catch (InterruptedException e) {}
   //}

    out=this; // for external references
    return;
  }

  public void reshapeScreen() {

      setSize(scale*maxX*8+50*scale,scale*maxY*8+50*scale+topY);
      offGraphics=(Graphics2D) screenBuffImage.getGraphics();
      if (!bgtrans) { // here
        offGraphics.setColor(new Color(borderColour,true));
        offGraphics.fillRect(0,0,scale*maxX*8+50*scale,scale*maxY*8+50*scale+topY);
        offGraphics.setColor(new Color(backgroundColour,true));
        offGraphics.fillRect(25*scale,25*scale+topY,scale*maxX*8,scale*maxY*8);
      } else {
        //offGraphics.setColor(new Color(borderColour&0x00FFFFFF,true));
        offGraphics.setColor(new Color(0x00FFFFFF,true));
        offGraphics.fillRect(0,0,scale*maxX*8+50*scale,scale*maxY*8+50*scale+topY);
        offGraphics.fillRect(25*scale,25*scale+topY,scale*maxX*8,scale*maxY*8);
      }

      for (int j=0; j<MAXmaxY; ++j) {  //last screen was CLEARed
        for (int i=0; i<MAXmaxX; ++i) {
          //pscreenchar[i][j]='X'; should
          pscreenchar[i][j]=' ';
          pscreencharColour[i][j]=0;
        }
      }
      repaint();
  }

  public boolean setCols(int cols) {
    return setColsRows(cols,C64Screen.maxY);
  }

  public boolean setRows(int rows) {
    return setColsRows(maxX,rows);
  }

  public boolean setColsRows(int cols, int rows) {
    if (rows>=25 && rows<=MAXmaxY && cols>=40 && cols<=MAXmaxX) {
      //// this will clear existing rows!
      if (C64Screen.maxY<rows) {
        // we are growing the screen
        for (int j=C64Screen.maxY; j<rows; ++j) {  //clear ALL the screen that was not previously written
          for (int i=0; i<maxX; ++i) {
            screenchar[i][j]=' ';
            screencharColour[i][j]=0;
          }
        }
      } else {
        // we are shrinking the screen
        // this is only really good when we shrink a full screen, if it is partially empty
        // we will start to loose output
        for (int j=0; j<rows; ++j) {  //move the last out to the new last output
          for (int i=0; i<maxX; ++i) {
            screenchar[i][j]=screenchar[i][j+(C64Screen.maxY-rows)];
            screencharColour[i][j]=screencharColour[i][j+(C64Screen.maxY-rows)];
          }
        }
        cursY=cursY-(C64Screen.maxY-rows); // not sure if we always want to do this
        if (cursY<0) cursY=0;
        if (cursY>=rows) cursY=rows-1;
      }
      C64Screen.maxY=rows;
      maxX=cols;
      reshapeScreen();
      return true;
    }
    return false;
  }

  public boolean makeFaint() {
    for (int i=0; i<16; ++i) {
        fullcolour[i]=((fullcolour[i]&0x00FCFCFC)>>2)|0xFF000000; 
    }
    return true;
  }

  public boolean setScale(int scale) {
    if (scale>=1 && scale<=2) { 
      this.scale=scale;
      reshapeScreen();
      return true;
    }
    return false;
  }

  // this isn't working
  long cursflash=0;
  public void create_screen_updater() {
    //javax.swing.Timer timer1 = new javax.swing.Timer(200, new  // this worked well with 40x25 on win but not bigger
    //javax.swing.Timer timer1 = new javax.swing.Timer(400, new 
    javax.swing.Timer timer1 = new javax.swing.Timer(250, new 
      ActionListener() {
        public void actionPerformed(ActionEvent event) {
          //scrollscreen();
          // blink cursor
          if (System.currentTimeMillis()-cursflash>=500) { //
            cursflash=System.currentTimeMillis();
            cursVisible=!cursVisible;
            screenneedsupdate=true;
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
      for (int j=0; j<maxY-1; ++j) {
        for (int i=0; i<maxX; ++i) {
          screenchar[i][j]=screenchar[i][j+1];
          screencharColour[i][j]=screencharColour[i][j+1];
        }
      }
      for (int i=0; i<maxX; ++i) {
        screenchar[i][cursY]=' ';
      }
    //drawrelevent(0,maxX,0,maxY);
  }

  private void scrolldelay() {
    if (true) {
      //just for effect, add a delay!
      try {
        Thread.sleep(10); // in the extreme slowdown
      } catch (InterruptedException e) {}
    }
  }

  // converts a keyboard (PC) character to the best "PETASCII" char
  public char petconvert(char ch) {
    int pos=0;
    if (ch>='a' && ch<='z') pos=1+(ch-'a');
    else if (ch>='A' && ch<='Z') pos=1+(ch-'A')+64;
    else if (ch>='a'+128 && ch<='z'+128) pos=1+(ch-'a')-32;
    else if (ch==' '+128) pos=32+128;  // reversed block
    else if (ch>='0' && ch<='9' || ch>=' ' && ch<='?') pos=(ch);
    else if (ch>=91 && ch<=95) pos=(ch-64);
    else if (ch=='`') pos=31;
    else if (ch=='|') pos=29+64;
    else if (ch=='~') pos=30+64;
    if (pos>=256) pos=0;
    return (char)pos;
  }

  public char petunconvert(char pos) {
    int ch=0;
    if (pos>=0 && pos<=26) ch='A'+pos-1;
    if (pos>=32 && pos<64) ch=pos;
    if (pos>=64 && pos<=26+64) ch='a'+pos-1-64;
    return (char)ch;
  }

  public void print(String line) {
    int i;
    char theChar;
    for (i=0; i<line.length(); ++i) {
      // allow "special" characters
      // backslash followed by [ is a real /
      theChar=line.charAt(i);
      if (theChar=='\\') {
        if (i+1<line.length() && line.charAt(i+1)=='[') {
          theChar='[';
        }
        theChar=petconvert(theChar);
      } else if (theChar=='[') {
        // now read until the next ']'
        String cS;
        cS="";
        for (i++; i<line.length(); ++i) if (line.charAt(i)!=']') {
          cS=cS+line.charAt(i);
        } else break;
        // special code is now in cS
        //System.out.println("The string is "+cS+"\n\n");
        for (int j=0; j<16; ++j) {
          if (cS.equals(colourname[j])) { setcursColour((short)j); }
          continue;
        } 
        if (cS.equals("testing")) { 
          cursColour=3;
          continue;
        } else if (cS.equals("BLOCK")) {
          theChar=petconvert((char)(' '+128));
        } else if (cS.equals("BACK-TRIANGLE")) {
          theChar=(char)(95);
        } else if (cS.equals("BACK-TRIANGLE-REV")) {
          theChar=(char)(95+128);
        } else if (cS.equals("FORWARD-TRIANGLE")) {
          theChar=(char)('i');
        } else if (cS.equals("FORWARD-TRIANGLE-REV")) {
          theChar=(char)('i'+128);
        } else if (cS.equals("LOW-HLINE")) {
          theChar=(char)('c');
        } else if (cS.equals("UPP-LEFT-LINE")) {
          theChar=(char)('p');
        } else if (cS.equals("UPP-RIGHT-LINE")) {
          theChar=(char)('n');
        } else if (cS.equals("LOW-LEFT-LINE")) {
          theChar=(char)('m');
        } else if (cS.equals("LOW-RIGHT-LINE")) {
          theChar=(char)(']'+32);
        } else if (cS.equals("VLINE")) {
          theChar=(char)(']');
        } else if (cS.equals("HLINE")) {
          theChar=petconvert('C');
        } else if (cS.equals("UP")) {
          gotoXY(cursX,cursY-1);
          continue;
        } else if (cS.equals("DOWN")) {
          gotoXY(cursX,cursY+1);
          continue;
        } else if (cS.equals("CR")) {
          // same as println()
          if (cursY<maxY-1) {
            cursY++;
          } else {
            // we are on the last line of the screen so scroll it
            scrollscreen(); if (slowdown) scrolldelay();
          }
          cursX=0;
          continue;
        } else {
          continue;
        }
      } else { theChar=petconvert(theChar); }
      /* normal processing */
      screenchar[cursX][cursY]=theChar;
      screencharColour[cursX][cursY]=cursColour;
      if (cursX==maxX-1) { // same as in println
        cursX=0;
        if (cursY==maxY-1) {
          scrollscreen(); if (slowdown) scrolldelay();
        } else {
          cursY++;
        }
      } else {
        cursX++;
      }
    }
    repaint(); // another one!
  }

  public void println(String line) {

    print(line);

    // same as println()
    if (cursY<maxY-1) {
      cursY++;
    } else {
      // we are on the last line of the screen so scroll it
      scrollscreen(); if (slowdown) scrolldelay();
    }
    cursX=0;

    // now signal it to be redrawn
    repaint();
  }

  public void println() {
    // here we define the line to be written on the screen
    // for now, implement this as an array of lines
    // should really stop it from repainting whilst inside this routine
    if (cursY<maxY-1) {
      cursY++;
    } else {
      scrollscreen(); if (slowdown) scrolldelay();
    }
    cursX=0;

    // now signal it to be redrawn
    repaint();
  }

  public void setcursColour(short colour) {
    cursColour=colour;
    repaint();
  }

  public void setcursColour(String colour) {
    for (int i=0; i<16; ++i) {
      if (colour.equals(colourname[i])) { setcursColour((short)i); }
    } 
    return;
  }

  public void writechar(char ch) {
    // here we define the line to be written on the screen
    // for now, implement this as an array of lines
    // should really stop it from repainting whilst inside this routine
    // note, doesnt wrap yet
    screenchar[cursX][cursY]=petconvert(ch);
    screencharColour[cursX][cursY]=cursColour;
    if (cursX==maxX-1) { println(); } else { cursX++; }
    // now signal it to be redrawn
    repaint();
  }
 
  public void writechar_raw(char ch) {
    // here we define the line to be written on the screen
    // for now, implement this as an array of lines
    // should really stop it from repainting whilst inside this routine
    // note, doesnt wrap yet
    screenchar[cursX][cursY]=ch;
    screencharColour[cursX][cursY]=cursColour;
    if (cursX==maxX-1) { println(); } else { cursX++; }
    // now signal it to be redrawn
    repaint();
  }
 
  public void backspace() {
    if (cursX>0) {
      screenchar[cursX-1][cursY]=' ';
      cursX--;
    }
    repaint();
  }

  public void gotoXY(int tocursX, int tocursY) {
    // if you try to move off the screen, it will try to fix that up
    if (tocursX<0 && tocursY>0) { tocursY--; tocursX=maxX-1; }
    if (tocursY<0) { tocursY=0; }
    if (tocursX>=maxX) { tocursY++; tocursX=0; }
    if (tocursY>=maxY) { tocursY=maxY-1; scrollscreen(); }
    if (tocursX>=0 && tocursX<maxX && tocursY>=0 && tocursY<maxY) {
      cursX=tocursX;
      cursY=tocursY;
      repaint();
    } 
  }

  // Timings
  long tstart;
  long ttime_paint;
  long ttime_paintl;
  long ttime_paintm;
  long tcount_paint=0;

//         Graphics2D offGraphics;

  //public void paint(Graphics g)
  // try this
  public synchronized void paint(Graphics g)
  {
     Graphics2D g2d = (Graphics2D) g;

     if (true) {
       tstart=System.currentTimeMillis();
       tcount_paint++;
     }
 
     // sometimes this is not ready yet
     if (offGraphics != null) {
         // draw all the lines
         if (modImage != null && modImage2 != null ) { // hold off until ready
           for (int j=0; j<maxY; ++j) {
               for (int i=0; i<maxX; i++) {
      
                 if (pscreenchar[i][j]==screenchar[i][j] && pscreencharColour[i][j]==screencharColour[i][j]) { } else {
                   pscreenchar[i][j]=screenchar[i][j];
                   pscreencharColour[i][j]=screencharColour[i][j];

                  if (scale==1) {
                   drawchar(screenchar[i][j],0+25*scale+i*8*scale,j*8*scale+25*scale+topY,screencharColour[i][j]);
                  } else {
                   drawchar2(screenchar[i][j],0+25*scale+i*8*scale,j*8*scale+25*scale+topY,screencharColour[i][j]);
                  }
                 }
               }
               if (true && j==4) {
                 ttime_paintl=System.currentTimeMillis()-tstart;
               }
           }
           // drawcursor - in the curColor
           if (cursEnabled && cursVisible) {
                 if (pscreenchar[cursX][cursY]==(char)(' '+128) && pscreencharColour[cursX][cursY]==cursColour) { } else {
                   pscreenchar[cursX][cursY]=(char)(' '+128);
                   pscreencharColour[cursX][cursY]=cursColour;

               if (scale==1) {
                 drawchar((char)(' '+128),0+25*scale+cursX*8*scale,cursY*8*scale+25*scale+topY,cursColour);
               } else {
                 drawchar2((char)(' '+128),0+25*scale+cursX*8*scale,cursY*8*scale+25*scale+topY,cursColour);
               }
             }
           }
         }

       if (true) {
         ttime_paintm=System.currentTimeMillis()-tstart;
       }

       if (bgtrans) {
         if (newoffGraphics != null) { //here
           newoffGraphics.drawImage(bgImage,  0,0, 8*scale*maxX+50*scale, 8*scale*maxY+50*scale+topY,
                                              0,0, 8*scale*maxX+50*scale, 8*scale*maxY+50*scale+topY,this);
           newoffGraphics.drawImage(screenBuffImage,0,0,8*scale*maxX+50*scale,8*scale*maxY+50*scale+topY,
               0,0,8*scale*maxX+50*scale,8*scale*maxY+50*scale+topY,this);
    
           g2d.drawImage(newoffImage,0,0,this);
         }
       } else {
         // normally
         if (false) {
           g2d.drawImage(screenBuffImage,0,0,8*scale*maxX+50*scale,8*scale*maxY+50*scale+topY,
               0,0,8*scale*maxX+50*scale,8*scale*maxY+50*scale+topY,this);
         } else {
           if (newoffGraphics != null) { //here
             // this method seems twice as efficient, but needs proper testing - yes it is faster
             newoffGraphics.drawImage(screenBuffImage,0,0,8*scale*maxX+50*scale,8*scale*maxY+50*scale+topY,
                 0,0,8*scale*maxX+50*scale,8*scale*maxY+50*scale+topY,this);
             g2d.drawImage(newoffImage,0,0,this);
           }
         }

       }

     }

     if (true) {
       ttime_paint=System.currentTimeMillis()-tstart;
     }
  }

  public void printstats() {
    System.out.println("Config: scale="+scale+" rows="+maxY);
    System.out.println("ttime-paintm          = "+ttime_paintm+" ms");
    System.out.println("ttime-paintl (5 lines)= "+ttime_paintl+" ms");
    System.out.println("ttime-paint           = "+ttime_paint+" ms");
    System.out.println("ttime-paint draw      = "+(ttime_paint-ttime_paintm)+" ms");
    System.out.println("tcount-paint = #"+tcount_paint);
    System.out.println("--------------------------");
  }

  boolean dots[][];
  boolean dots2[][];
  public void drawchar_init() {
    dots=new boolean[32*8][8*8];
    //System.out.println("Allocated space for dots");
    if (dots==null) {
      System.out.println("but it is null");
    }
    for (int posx=0; posx<32; ++posx) {
    for (int posy=0; posy<8; ++posy) {
    for (int i=0; i<8; ++i) {
      for (int j=0; j<8; ++j) {
        int rgb=modImage.getRGB(posx*8+i,posy*8+j);
        if ((rgb & 0xFFFFFF) ==0 ) {
          dots[posx*8+i][posy*8+j]=true;
        } else {
          dots[posx*8+i][posy*8+j]=false;
        }
      }
    }
    }
    }

    dots2=new boolean[32*16][8*16];
    //System.out.println("Allocated space for dots");
    if (dots2==null) {
      System.out.println("but it is null");
    }
    for (int posx=0; posx<32; ++posx) {
    for (int posy=0; posy<8; ++posy) {
    for (int i=0; i<16; ++i) {
      for (int j=0; j<16; ++j) {
        int rgb=modImage2.getRGB(posx*16+i,posy*16+j);
        if ((rgb & 0xFFFFFF) ==0 ) {
          dots2[posx*16+i][posy*16+j]=true;
        } else {
          dots2[posx*16+i][posy*16+j]=false;
        }
      }
    }
    }
    }
  }

  char[][] pscreenchar=new char[MAXmaxX][MAXmaxY];
  short[][] pscreencharColour=new short[MAXmaxX][MAXmaxY];

  public void drawchar(char ch, int x, int y, int colour) {
    int pos;
    int posx;
    int posy;
    int col;

    pos=ch; posx=pos%32; posy=pos/32;
    col=fullcolour[colour];
    // modify here the colours of the source to match what we want
    // then draw it onto the offGraphics place
    for (int i=0; i<8; ++i) {
      for (int j=0; j<8; ++j) {
        //int rgb=modImage.getRGB(posx*8+i,posy*8+j);
        int xx=i*scale+x;
        int yy=j*scale+y;
        if (dots[posx*8+i][posy*8+j]) {
          if (faint) screenBuffImage.setRGB(xx,yy,col & 0x60FFFFFF); // and here
          else screenBuffImage.setRGB(xx,yy,col);
          if (scale==2) {
            screenBuffImage.setRGB(xx+1,yy,col);
            screenBuffImage.setRGB(xx,yy+1,col);
            screenBuffImage.setRGB(xx+1,yy+1,col);
          }
        } else {
          if (bgtrans && bgshadow && (j>0 && dots[posx*8+i][posy*8+j-1] ||
               (i>0 && dots[posx*8+i-1][posy*8+j]))) {
            //screenBuffImage.setRGB(xx,yy,backgroundColour & 0x50FFFFFF); //here // 60 worked well //40good, but sometimes too suttle
            screenBuffImage.setRGB(xx,yy,backgroundColour & 0x70FFFFFF); //here // 60 worked well //40good, but sometimes too suttle
          } else
          if (bgtrans) screenBuffImage.setRGB(xx,yy,backgroundColour & 0x00FFFFFF); //here
          else screenBuffImage.setRGB(xx,yy,backgroundColour);
          if (scale==2) {
            screenBuffImage.setRGB(xx+1,yy,backgroundColour);
            screenBuffImage.setRGB(xx,yy+1,backgroundColour);
            screenBuffImage.setRGB(xx+1,yy+1,backgroundColour);
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

    pos=ch; posx=pos%32; posy=pos/32;
    col=fullcolour[colour];
    // modify here the colours of the source to match what we want
    // then draw it onto the offGraphics place
    for (int i=0; i<16; ++i) {
      for (int j=0; j<16; ++j) {
        int xx=i+x;
        int yy=j+y;
        if (dots2[posx*16+i][posy*16+j]) {
          screenBuffImage.setRGB(xx,yy,col);
        } else {
          screenBuffImage.setRGB(xx,yy,backgroundColour);
        }
      }
    }
  }


  public void clearscreen() {
    for (int j=0; j<MAXmaxY; ++j) {  //clear ALL the screen
      for (int i=0; i<MAXmaxX; ++i) {
        screenchar[i][j]=' ';
        screencharColour[i][j]=0;
      }
    }
    cursX=0; cursY=0;
  }

  public String getline() {
    // return the current line above the cursor  (we would have hit enter already)
    String rets="";
    int y=cursY-1;  if (y<0) { y=0; }
    for (int i=0; i<maxX; ++i) {
      rets=rets+petunconvert(screenchar[i][y]);
    }
    return rets;
  }

  public void startupscreen() {
    backgroundColour=fullcolour[6];
    borderColour=fullcolour[6+8];
    clearscreen();
    reshapeScreen(); // just to see - this is a dodgy work around!!! when changing background or border colours, must reshape screen
    C64Screen.out.println("");
    C64Screen.out.println("    **** commodore 64 basic v2 ****");
    C64Screen.out.println("");
    C64Screen.out.println(" 64k ram system  38911 basic bytes free");
    //C64Screen.out.println("");
    //C64Screen.out.println("ready.");
  }

  public void initcolour() {
    modImage=toBufferedImage(charsetUpp);
    modImage2=toBufferedImage(charsetUpp2);
  }
  public void update(Graphics g)
  {
    paint(g);
  }

/// key stuff
  boolean altdown=false;
  boolean ctrldown=false;
  boolean tabdown=false;

  public void keyPressed(KeyEvent e)
  {
    // first look at it to see if it is just a shift/control/alt event
    if (e.getKeyCode()==KeyEvent.VK_ALT) { altdown=true; }
    if (e.getKeyCode()==KeyEvent.VK_CONTROL) { ctrldown=true; }
    if (e.getKeyCode()==KeyEvent.VK_UP || e.getKeyCode()==KeyEvent.VK_DOWN 
        || e.getKeyCode()==KeyEvent.VK_LEFT || e.getKeyCode()==KeyEvent.VK_RIGHT) { 
      keybuf[keybuftop]=0;
      keybuftop++;
      if (keybuftop>=keybufmax) { keybuftop=0; }
      keybuf[keybuftop]=(char)e.getKeyCode();
      keybuftop++;
      if (keybuftop>=keybufmax) { keybuftop=0; }
      return;
    }
    if (e.getKeyCode()==KeyEvent.VK_TAB) { 
      tabdown=true; 
      return;
    }
    if (e.getKeyChar()!=KeyEvent.CHAR_UNDEFINED) {
      
      // special codes first
      if ((char)e.getKeyCode()=='H' && e.isAltDown()) { faint=!faint; forcedrepaint(); return; } // really needs more than this to trigger
      if ((char)e.getKeyCode()=='B' && e.isAltDown() && bgtrans_ability) { bgtrans=!bgtrans; reshapeScreen(); return; } // really needs more than this to trigger
      if ((char)e.getKeyCode()=='D' && e.isAltDown()) { bgshadow=!bgshadow; forcedrepaint(); return; } // really needs more than this to trigger
      if ((char)e.getKeyCode()=='S' && e.isAltDown()) { slowdown=!slowdown; forcedrepaint(); return; } // really needs more than this to trigger

      keybuf[keybuftop]=e.getKeyChar();
      //if (altdown) { keybuf[keybuftop]+=128; }
      //if (ctrldown && keybuf[keybuftop]>='0' && keybuf[keybuftop]<='9') { keybuf[keybuftop]+=256; }
      if (e.isAltDown()) { keybuf[keybuftop]+=128; }
      //if (e.isControlDown() && keybuf[keybuftop]>='0' && keybuf[keybuftop]<='9') { keybuf[keybuftop]+=256; }
      else if (e.isControlDown() && keybuf[keybuftop]>='0' && keybuf[keybuftop]<='9') { keybuf[keybuftop]+=128; }
      else if (e.isControlDown()) { keybuf[keybuftop]+='a'+128-1; }
      else if (tabdown && keybuf[keybuftop]>='0' && keybuf[keybuftop]<='9') { keybuf[keybuftop]+=256; }

      //notwork//if (tabdown && keybuf[keybuftop]>='0' && keybuf[keybuftop]<='9') { keybuf[keybuftop]+=256; }
      //notwork//if (e.isControlDown() && keybuf[keybuftop]>='0' && keybuf[keybuftop]<='9') { keybuf[keybuftop]+=256+8; }

      //if (ctrldown && keybuf[keybuftop]>='!' && keybuf[keybuftop]<=')') { keybuf[keybuftop]+=256+8-33; }
      keybuftop++;
      if (keybuftop>=keybufmax) {
        keybuftop=0;
      }
    } else {
      // do nothing
    }
  }
 
  public String screenInput() 
  {
    while (true) {
      char ch;
      ch=givemekey();
      if (ch == 0) { // meta keys
        ch=givemekey();
        if (ch == KeyEvent.VK_UP) { gotoXY(cursX,cursY-1); }
        else if (ch == KeyEvent.VK_DOWN) { gotoXY(cursX,cursY+1); }
        else if (ch == KeyEvent.VK_LEFT) { gotoXY(cursX-1,cursY); }
        else if (ch == KeyEvent.VK_RIGHT) { gotoXY(cursX+1,cursY); }
      } else if (ch == '\n') { println(); return getline();}
      else if (ch == java.awt.event.KeyEvent.VK_BACK_SPACE) { backspace(); }
      else if (ch >=256+'0' && ch <=256+'9') { setcursColour((short)(ch%16)); }
      else if (ch >=128+'0' && ch <=128+'9') { setcursColour((short)((ch+8)%16)); }
      else { writechar(ch); }
    }
  }

  public void keyReleased(KeyEvent e)
  {
    if (e.getKeyCode()==KeyEvent.VK_ALT) { altdown=false; }
    if (e.getKeyCode()==KeyEvent.VK_CONTROL) { ctrldown=false; }
    if (e.getKeyCode()==KeyEvent.VK_TAB) { tabdown=false; }
  }

  public void keyTyped(KeyEvent e)
  {
  }
 
  public boolean hasinput() {
    if (keybuftop!=keybufbot) { return true; };
    return false;
  }

  public char givemekey() {
    return givemekey(true);
  }

  public char givemekey(boolean hasCursor) {
    char returnval;
    // is this the right spot?
    if (hasCursor) { cursEnabled=true; cursVisible=true; repaint(); }
    forcedrepaint(); //dodgy work around
    // should block until there is a key!
    while (keybuftop==keybufbot) { 
      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {}
    }
    returnval=keybuf[keybufbot];
    keybufbot++;
    if (keybufbot>=keybufmax) {
      keybufbot=0;
    } 
    if (hasCursor) { cursEnabled=false; cursVisible=false; } //repaint(); 
    return returnval;
  }

  // TRY this
  //int qval=0; // this works
  long qval=0;

  public void forcedrepaint() {
    qval=0; repaint();
  }

  boolean screenneedsupdate=false;
  public void repaint_ifupdated() {
    if (screenneedsupdate) {
      qval=0; repaint();
      screenneedsupdate=false;
    }
  }

  public void repaint() {
    //if (qval%100==0) { // this works!
    // the value 100 needs tuning, 100 gives mostly good output on powerful, non-loaded machine
    // this is an extreme slowdown...
    if (true && slowdown && System.currentTimeMillis()-qval>10) { // was 200 // was 200 again
      // call the real one
      super.repaint();
      qval=System.currentTimeMillis();
    } else if ((false || !slowdown) && System.currentTimeMillis()-qval>100) { // was 200 // was 200 again // had it set to 50 for a while
      // call the real one
      super.repaint();
      qval=System.currentTimeMillis();
    } else {
      // set a timer, to get it to repaint finally, (after everything stops)
      // one shot, just to repaint, if it is already going, reset the timer
      screenneedsupdate=true;
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
            return (BufferedImage)image;
        }
    
        // This code ensures that all the pixels in the image are loaded
        image = new ImageIcon(image).getImage();
    
        // Determine if the image has transparent pixels; for this method's
        // implementation, see e661 Determining If an Image Has Transparent Pixels
        //boolean hasAlpha =  ComponentColorModel.hasAlpha(image);
        boolean hasAlpha=true;
    
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
            GraphicsDevice gs = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gs.getDefaultConfiguration();
            bimage = gc.createCompatibleImage(
                image.getWidth(null), image.getHeight(null), transparency);
        } catch (HeadlessException e) {
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
