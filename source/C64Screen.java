//import java.io;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*; // for key events
import java.awt.Toolkit.*; // for image loading
import java.awt.image.*;

import java.util.*;

// things to do:
// . change the screen writing process from being done EVERY paint
//   to being done on change only, and painting the static image
//   -tried this 10's to 100's of times slower!!!
// . not display until all scrolls etc finished (otherwise blanking etc shows up)
//--------------------------------------------------------------------------
class C64Screen extends JFrame implements KeyListener {
  Graphics2D offGraphics;
  Image offImage;
  int cursX=0;
  int cursY=0;
  short cursColour=2;
  boolean cursVisible=false;

  static public C64Screen out=null; // a pointer to the last (really first) instansiated screen for 
    // external use (like Screen.out, but C64Screen.screen)

  String colourname[]={"BLACK","WHITE","RED","CYAN","MAGENTA","GREEN","BLUE","YELLOW","ORANGE",
                        "BROWN","LIGHT RED","GREY 1","GREY 2","LIGHT GREEN","LIGHT BLUE","GREY 3"};
  int fullcolour[]={
        0xFF000000, // black
        0xFFFFFFFF, // white
        0xFFE00000, // red //0xFFFF2020, // red
        0xFF20FFFF, // cyan
        0xFFD020FF, // purple        was magenta 0xFFFF20FF, 
        0xFF20E020, // green         was 0xFF20FF20, // green
        0xFF2020A0, // blue          was 0xFF2020A0, // dark blue
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
  int scale=2;
  int maxX=40;
  static int maxY=25;
  int topY=12;
  //String lines[]=new String[maxY];
  char[][] screenchar=new char[maxX][maxY];
  short[][] screencharColour=new short[maxX][maxY];

  //key
  static int keybufmax=1000;
  int keybuftop=0;
  int keybufbot=0;
  char keybuf[] = new char[keybufmax];
  //end key

  // charset
  //Image charsetUpp;
  Image charsetUpp;
  Graphics2D charsetUppGraphics;
  //Image charsetUppoffImage;
  // should we start a cursor flashing thread?
  Image charsetUpp_BB[]=new Image[16]; // repainted blue blue
  BufferedImage modImage;
  BufferedImage charBuffImage=new BufferedImage(64,64,BufferedImage.TYPE_INT_ARGB);

  public C64Screen(String title) {
    super(title);
    // create the screeen 
    setSize(scale*maxX*8+50*scale,scale*maxY*8+50*scale+topY);
    setVisible(true);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    addKeyListener(this); // for listening to key strokes

    offImage=createImage(scale*maxX*8+50*scale,scale*maxY*8+50*scale+topY);
    offGraphics=(Graphics2D) offImage.getGraphics();

    create_screen_updater();

    Toolkit kit = Toolkit.getDefaultToolkit();
    charsetUpp=kit.getImage("c64_upp.gif");
    initcolour(); 
    // initialise screen
    clearscreen();
    out=this; // for external references
    return;
  }

  // this isn't working
  public void create_screen_updater() {
    javax.swing.Timer timer1 = new javax.swing.Timer(200, new 
      ActionListener() {
        public void actionPerformed(ActionEvent event) {
          //scrollscreen();
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

  public char petconvert(char ch) {
    int pos=0;
    if (ch>='a' && ch<='z') pos=1+(ch-'a');
    else if (ch>='A' && ch<='Z') pos=1+(ch-'A')+64;
    else if (ch>='a'+128 && ch<='z'+128) pos=1+(ch-'a')-32;
    else if (ch==' '+128) pos=32+128;  // reversed block
    else if (ch>='0' && ch<='9' || ch>=' ' && ch<='?') pos=(ch);
    else if (ch>=91 && ch<=95) pos=(ch-64);
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
    for (i=0; i<line.length(); ++i) {
      screenchar[cursX][cursY]=petconvert(line.charAt(i));
      screencharColour[cursX][cursY]=cursColour;
      //drawrelevent(cursX,cursX+1,cursY,cursY+1);
      cursX++;
      if (cursX==maxX) {
        scrollscreen();
        cursX=0;
      }
    }
    if (cursX==maxX) {
      scrollscreen();
      cursX=0;
    }
    repaint(); // another one!
  }
  public void println(String line) {

    if (cursY<maxY-1) {
      //lines[cursY++]=line;
      for (int i=0; i<line.length(); ++i) {
        screenchar[cursX][cursY]=petconvert(line.charAt(i));
        screencharColour[cursX][cursY]=cursColour;
        //drawrelevent(cursX,cursX+1,cursY,cursY+1);
        cursX++;
        if (cursX==maxX) {
          cursY++;
          if (cursY==maxY) {
            cursY--;
            scrollscreen();
          }
          cursX=0;
        }
      }
      cursY++;
    } else {
      //lines[cursY]=line;
      for (int i=0; i<line.length(); ++i) {
        screenchar[cursX][cursY]=petconvert(line.charAt(i));
        screencharColour[cursX][cursY]=cursColour;
        //drawrelevent(cursX,cursX+1,cursY,cursY+1);
        cursX++;
        if (cursX==maxX) {
          cursY++;
          if (cursY==maxY) {
            cursY--;
            scrollscreen();
          }
          cursX=0;
        }
      }
      scrollscreen();
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
      scrollscreen();
      //lines[cursY]="";
      for (int i=0; i<cursX; ++i) {
        screenchar[i][cursY]=' ';
      }
      //drawrelevent(0,cursX,cursY,cursY+1);
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
    //if (lines[cursY]==null) { lines[cursY]=""; }
    //lines[cursY]=lines[cursY]+ch;
    screenchar[cursX][cursY]=petconvert(ch);
    screencharColour[cursX][cursY]=cursColour;
    //drawrelevent(cursX,cursX+1,cursY,cursY+1);
    cursX++;
    if (cursX==maxX) { println(); }
    // now signal it to be redrawn
    repaint();
  }
 
  public void writechar_raw(char ch) {
    // here we define the line to be written on the screen
    // for now, implement this as an array of lines
    // should really stop it from repainting whilst inside this routine
    // note, doesnt wrap yet
    //if (lines[cursY]==null) { lines[cursY]=""; }
    //lines[cursY]=lines[cursY]+ch;
    screenchar[cursX][cursY]=ch;
    screencharColour[cursX][cursY]=cursColour;
    //drawrelevent(cursX,cursX+1,cursY,cursY+1);
    cursX++;
    if (cursX==maxX) { println(); }
    // now signal it to be redrawn
    repaint();
  }
 
  public void backspace() {
    if (cursX>0) {
      screenchar[cursX-1][cursY]=' ';
      //drawrelevent(cursX-1,cursX,cursY,cursY+1);
      cursX--;
    }
    repaint();
  }

  //public void paint(Graphics g)
  // try this
  public synchronized void paint(Graphics g)
  {
     Graphics2D g2d = (Graphics2D) g;

     // sometimes this is not ready yet
     if (offGraphics != null) {
       if (false) {
         offGraphics.setColor(new Color(borderColour,true)); // this colour was such a good guess!
         offGraphics.fillRect(0,0,scale*maxX*8+50*scale,scale*maxY*8+50*scale+topY);
         offGraphics.setColor(new Color(backgroundColour,true)); // this colour was such a good guess!
         offGraphics.fillRect(25*scale,25*scale+topY,scale*maxX*8,scale*maxY*8);
       }
         // draw all the lines
         if (modImage != null) { // hold off until ready
           for (int j=0; j<maxY; ++j) {
               for (int i=0; i<maxX; i++) {
                 drawchar(screenchar[i][j],0+25*scale+i*8*scale,j*8*scale+25*scale+topY,screencharColour[i][j]);
               }
           }
           // drawcursor - in the curColor
           if (cursVisible) {
             drawchar((char)(' '+128),0+25*scale+cursX*8*scale,cursY*8*scale+25*scale+topY,cursColour);
           }
         }
       g2d.drawImage(offImage,0,0,this);
     }
  }

  public void drawrelevent(int x0, int x1, int y0, int y1) {
    // redraw only where it is relevent
         // draw all the lines
         if (modImage != null) { // hold off until ready
           for (int j=y0; j<y1; ++j) {
               for (int i=x0; i<x1; i++) {
                 drawchar(screenchar[i][j],0+25*scale+i*8*scale,j*8*scale+25*scale+topY,screencharColour[i][j]);
               }
           }
           // drawcursor - in the curColor
           if (cursVisible) {
             drawchar((char)(' '+128),0+25*scale+cursX*8*scale,cursY*8*scale+25*scale+topY,cursColour);
           }
         }
  }

  public void drawchar_old(char ch, int x, int y) {
    // we want to draw char ch, to work out where it is:
    // 
    int pos;
    int posx;
    int posy;
    pos=0;
    if (ch>='a' && ch<='z') pos=1+(ch-'a');
    else if (ch>='A' && ch<='Z') pos=1+(ch-'A')+64;
    else if (ch==' ') pos=32;
    else if (ch=='.') pos=46;
    else if (ch>='0' && ch<='9' || ch>='(' && ch<=')') pos=(ch);
    posx=pos%32; posy=pos/32;
    offGraphics.drawImage(charsetUpp,x,y,x+8,y+8,posx*8,posy*8,posx*8+8,posy*8+8,this);
  }

  Image gradientImage;

  public void drawchar(char ch, int x, int y, int colour) {
    int pos;
    int posx;
    int posy;
    pos=ch; posx=pos%32; posy=pos/32;

    // modify here the colours of the source to match what we want
    // then draw it onto the offGraphics place
    for (int i=0; i<8; ++i) {
      for (int j=0; j<8; ++j) {
        int rgb=modImage.getRGB(posx*8+i,posy*8+j);
        if ((rgb & 0xFFFFFF) ==0 ) { 
          charBuffImage.setRGB(i*scale,j*scale,fullcolour[colour]);
          if (scale==2) {
            charBuffImage.setRGB(i*scale+1,j*scale,fullcolour[colour]);
            charBuffImage.setRGB(i*scale,j*scale+1,fullcolour[colour]);
            charBuffImage.setRGB(i*scale+1,j*scale+1,fullcolour[colour]);
          }
          //modImage.setRGB(i,j,fullcolour[colour]);
        } else {
          //charBuffImage.setRGB(i,j,0xFF323C64);
          charBuffImage.setRGB(i*scale,j*scale,backgroundColour);
          if (scale==2) {
            charBuffImage.setRGB(i*scale+1,j*scale,backgroundColour);
            charBuffImage.setRGB(i*scale,j*scale+1,backgroundColour);
            charBuffImage.setRGB(i*scale+1,j*scale+1,backgroundColour);
          }
          //modImage.setRGB(i,j,backgroundColour);
        }
      }
    }
    offGraphics.drawImage(charBuffImage,x,y,x+8*scale,y+8*scale,0,0,8*scale,8*scale,this);
    // scaling is too slow... offGraphics.drawImage(charBuffImage,x,y,x+8*scale,y+8*scale,0,0,8,8,this);
    //offGraphics.drawImage(modImage,x,y,x+8,y+8,0,0,8,8,this);
  }

  public void clearscreen() {
    for (int j=0; j<maxY; ++j) {
      for (int i=0; i<maxX; ++i) {
        screenchar[i][j]=' ';
        screencharColour[i][j]=0;
      }
    }
    cursX=0; cursY=0;
         offGraphics.setColor(new Color(borderColour,true)); // this colour was such a good guess!
         offGraphics.fillRect(0,0,scale*maxX*8+50*scale,scale*maxY*8+50*scale+topY);
         offGraphics.setColor(new Color(backgroundColour,true)); // this colour was such a good guess!
         offGraphics.fillRect(25*scale,25*scale+topY,scale*maxX*8,scale*maxY*8);
    //drawrelevent(0,maxX,0,maxY);
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
    C64Screen.out.println("");
    C64Screen.out.println("    **** commodore 64 basic v2 ****");
    C64Screen.out.println("");
    C64Screen.out.println(" 64k ram system  38911 basic bytes free");
    C64Screen.out.println("");
    C64Screen.out.println("ready.");
  }

  public void initcolour() {
    // here we initially repaint the characters (this method means
    // we would need to do it for every different colour
    GradientFilter imgf = new GradientFilter();
    ImageProducer ip = charsetUpp.getSource();
    ip = new FilteredImageSource(ip, imgf);
    charsetUpp_BB[0] = getToolkit().createImage(ip);
    //offGraphics.drawImage(gradientImage,x,y,x+8,y+8,posx*8,posy*8,posx*8+8,posy*8+8,this);
    modImage=toBufferedImage(charsetUpp);
    modImage.setRGB(1,1,0xFF00FF00);
    //charsetUpp_BB[0]=modImage;
  }
  public void update(Graphics g)
  {
    paint(g);
  }

/// key stuff
  boolean altdown=false;
  boolean ctrldown=false;

  public void keyPressed(KeyEvent e)
  {
    // first look at it to see if it is just a shift/control/alt event
    if (e.getKeyCode()==KeyEvent.VK_ALT) { altdown=true; }
    if (e.getKeyCode()==KeyEvent.VK_CONTROL) { ctrldown=true; }
    if (e.getKeyChar()!=KeyEvent.CHAR_UNDEFINED) {
      
      keybuf[keybuftop]=e.getKeyChar();
      if (altdown) { keybuf[keybuftop]+=128; }
      if (ctrldown && keybuf[keybuftop]>='0' && keybuf[keybuftop]<='9') { keybuf[keybuftop]+=256; }
      keybuftop++;
      if (keybuftop>=keybufmax) {
        keybuftop=0;
      }
    }
  }
 
  public void keyReleased(KeyEvent e)
  {
    if (e.getKeyCode()==KeyEvent.VK_ALT) { altdown=false; }
    if (e.getKeyCode()==KeyEvent.VK_CONTROL) { ctrldown=false; }
  }

  public void keyTyped(KeyEvent e)
  {
  }
 
  public boolean hasinput() {
    if (keybuftop!=keybufbot) { return true; };
    return false;
  }

  public char givemekey() {
    char returnval;
    // is this the right spot?
    cursVisible=true; repaint();
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
    cursVisible=false; //repaint();
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
    if (System.currentTimeMillis()-qval>200) {
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

///


//class MyRGBImageFilter extends RGBImageFilter

        //add("Center", new ImageCanvas(
            //getToolkit().getImage(filename)));

class GradientFilter extends RGBImageFilter {
    //float[] hsb = new float[3];
    int width, height;

    public void setDimensions(int w, int h) {
        super.setDimensions(w, h);
        width = w;
        height = h;
    }

       //public int filterRGB(int x, int y, int r)
        //{       return  ((r & 0xff00ff00)|((r & 0xff0000) >> 16)
                        //|((r & 0xff) << 16));
        //}
       public int filterRGB(int x, int y, int r) { 
         int ret;
         //if ((r & 0xffffff) == 0) ret=0xffffffff;
         //else ret=0;
         if ((r & 0xffffff) == 0) ret=0xFFA0AAFF;
         else ret=0xFF323C64;
         return ret;
       }

    //public int filterRGB(int x, int y, int rgb) {
        //Color c = new Color(rgb);
        //Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), hsb);
        //hsb[2] += .5f - (float)x / width;
        //hsb[2] += .5f - (float)y / height;
        //hsb[2] = Math.max(0.0f, Math.min(1.0f, hsb[2]));
        //return Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
    //}

}

///end

