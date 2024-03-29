/////////////////////////////////////////////////////////////////////////////////
//
//
//
/////////////////////////////////////////////////////////////////////////////////

// just doxx did it
import java.awt.image.*; // for Image
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;        // for key events
import java.awt.Toolkit.*;      // for image loading

import java.io.*;
import javax.imageio.ImageIO;
import java.awt.image.*;
import java.awt.geom.*;

import java.net.*; // for reading from http


public class GraphicsDevice extends JFrame implements MouseListener, MouseMotionListener {

  Image newoffImage;
  Graphics2D newoffGraphics;

  Image[] newoffImageB = new Image[2];
  Graphics2D[] newoffGraphicsB=new Graphics2D[2];

  Random generator = new Random();
  final static boolean graphics_debug=false;


//  int sizex=1000; 
//  int sizey=1000;
  int tby=30;
  int tbx=0;
  final int default_sizex=768;
  final int default_sizey=1004+tby;
  int sizex=default_sizex;   
  int sizey=default_sizey;
  
  boolean blitting=true;
  int blit=0;
  int paintblit=0;
   
  public GraphicsDevice(int x, int y) {
    super("ijk64 graphics");
    initDevice(x,y);
  }
  public GraphicsDevice() {
    super("ijk64 graphics");
    initDevice(sizex,sizey-tby);
  }
  
  void initDevice(int x, int y) {
    System.out.printf("Graphics window insets = left=%d right=%d top=%d bottom=%d\n",getInsets().left,getInsets().right,getInsets().top,getInsets().bottom);

tby=3700;

    sizex=x;
    //
    ////setResizable(false);  // for now
    //
    //sizey=tby+y;
    //setSize(sizex,sizey);
    getRootPane().setPreferredSize(new Dimension(x,y));
//tby=0; setUndecorated(true);
    pack();
    setVisible(true); // start AWT painting.
    //tby=getHeight()-sizey;
    //System.out.printf("Graphics: getHeight()=%d tby=%d getWidth()=%d\n",getRootPane().getHeight(),tby,getRootPane().getWidth());

// very bad - but just for now
    try { Thread.sleep(100); } 
    catch(InterruptedException ex) {
      Thread.currentThread().interrupt();
    }

    tby=getInsets().top;
    tbx=getInsets().left;
    sizey=tby+y;

    if (graphics_debug) {
      System.out.printf("Graphics: getHeight()=%d tby=%d getWidth()=%d\n",getRootPane().getHeight(),tby,getRootPane().getWidth());
      System.out.printf("Graphics window insets = left=%d right=%d top=%d bottom=%d\n",getInsets().left,getInsets().right,getInsets().top,getInsets().bottom);
      System.out.printf("Graphics contentPane insets = left=%d right=%d top=%d bottom=%d\n",getContentPane().getInsets().left,getContentPane().getInsets().right,getContentPane().getInsets().top,getContentPane().getInsets().bottom);
      System.out.printf("Graphics contentPane insets = left=%d right=%d top=%d bottom=%d\n",getRootPane().getInsets().left,getRootPane().getInsets().right,getRootPane().getInsets().top,getRootPane().getInsets().bottom);
      System.out.println("frame width : "+getWidth());
      System.out.println("frame height: "+getHeight());
      System.out.println("frame width : "+getWidth());
      System.out.println("frame height: "+getLayeredPane().getComponent(0).getHeight());
      //System.out.println("frame height: "+getLayeredPane().getComponent(1).getHeight());
      System.out.println("content pane width : "+getContentPane().getWidth());
      System.out.println("content pane height: "+getContentPane().getHeight());
      System.out.println("width  of left + right  borders: "+(getWidth()-getContentPane ().getWidth()));
      System.out.println("height of top  + bottom borders: "+(getHeight()-getContentPane().getHeight()));
      System.out.println("title height = "+(getBounds().height-getContentPane().getBounds().height) );
    }
      System.out.printf("Graphics window insets = left=%d right=%d top=%d bottom=%d\n",getInsets().left,getInsets().right,getInsets().top,getInsets().bottom);
      System.out.println("title height = "+(getBounds().height-getContentPane().getBounds().height) );

    if (true)
      setResizable(true); // sometimes too soon
    else {
    // so wait for 2 seconds
    new java.util.Timer().schedule( 
      new java.util.TimerTask() {
        //@Override
        public void run() {
          setResizable(true);
            // your code here
        }
      }, 2000 
    );
    }

    addMouseListener(this);
    addMouseMotionListener(this);
        if (blitting) {
	  newoffImageB[0] = createImage(sizex,sizey);
	  newoffImageB[1] = createImage(sizex,sizey);
	  newoffGraphicsB[0] = (Graphics2D) newoffImageB[0].getGraphics();
	  newoffGraphicsB[1] = (Graphics2D) newoffImageB[1].getGraphics();
          if (blitting) { 
	     blit++; if (blit>1) blit=0; 
	     newoffGraphics = newoffGraphicsB[blit];
             paintblit=blit;
          }
	} else {
	  newoffImage = createImage(sizex,sizey);
	  //newoffImage = createVolatileImage(sizex,sizey);
	  newoffGraphics = (Graphics2D) newoffImage.getGraphics();
	}
        command_ANTIALIAS(1);

  }

  void redirectkeys(C64Screen screen) {
    addKeyListener(screen);       // for listening to key strokes
  }
      
  void save(String filename) throws IOException {
    //Graphics2D cg = newoffImage.createGraphics();
    //try {

      BufferedImage image;
      if (blitting) { image = (BufferedImage)newoffImageB[paintblit]; }
      else  image=(BufferedImage)newoffImage;
      //if (ImageIO.write(image, "png", new File(filename+".png"))) {
      if (ImageIO.write(image.getSubimage(0,tby,sizex,sizey-tby), "png", new File(filename+".png"))) {
        System.out.println("-- saved");
      }
    //} catch (IOException e) {
      //e.printStackTrace();
    //}
  }

  void createDisplay() {
	   // assumed
  }

  public void resetDevice(int x, int y) {
    if (x!=sizex || y+tby!=sizey) {
      // weve changed the size - we need to reset it
      initDevice(x,y);
      //sizex=x;
      //sizey=tby+y;
      //setSize(sizex,sizey); // should really clear it too?
      //setVisible(true); // start AWT painting.
      System.out.printf("Setting graphics size %d %d\n",sizex,sizey);
    }
    command_ENDFRAME();
    fsize=16;
    topimage=0; //reset this back
    circleCentered=true; // reset this too
    command_ANTIALIAS(1);
  }
  public void resetDevice() {
    resetDevice(default_sizex,default_sizey-tby);
  }
   
  void doupdate() {
	  // after every draw OR at the endfram   
     repaint();
  }

/*******************************************************************************************/

   Color colorindex[] = {
	   Color.WHITE             // 0
	  ,Color.RED
	  ,Color.GREEN
	  ,Color.BLUE
      ,Color.BLACK
      ,new Color(128,0,128)   //new Color(16*13,32,255)     // 5 PURPLE
      ,Color.YELLOW
      ,Color.CYAN
      ,Color.ORANGE
      ,new Color(128,0,0)
      ,new Color(255,128,128)    // Color.PINK
      ,Color.DARK_GRAY
      ,Color.GRAY
      ,new Color(128,255,128)
      ,new Color(128,128,255)
      ,Color.LIGHT_GRAY
  };


   int fsize=16;
   String fontuse="Monospaced";
   int lsize=1;
   int valevent=0;
   int valx=0;
   int valy=0;
   boolean inframe=false;
   
/*******************************************************************************************/
   /************/
   /* COMMANDS */
   /************/
   
   // do I need this
   public synchronized void command_BEGINFRAME() {
     paintblit=blit; // necessary
     inframe=true;
   }

   public void command_ENDFRAME() {
     paintblit=blit;
     inframe=false;
     doupdate();   
   }

   public static void command_SLEEP(int milliseconds) {
       try {
         Thread.sleep(milliseconds);       // in the extreme slowdown
       }
       catch(InterruptedException e) {
       }
   }

   public synchronized void command_CLS() { /* try this */
     if (blitting && inframe && paintblit==blit) { 
	     blit++; if (blit>1) blit=0; 
	     newoffGraphics = newoffGraphicsB[blit];
     }
     newoffGraphics.setColor(Color.WHITE);
     newoffGraphics.fillRect(0, 0, sizex,sizey+tby);	   
     //newoffGraphics.clearRect(0, 0, sizex,sizey+tby);	   
     if (!inframe) doupdate();
   }
         
   boolean circleCentered=true;

   public void command_CIRCLE_CONTROL(int control) {
     if (control==1) { circleCentered=true; }
     else { circleCentered=false; }
   }

   //public void command_CIRCLE(int x1, int y1, int r, int col, int fill) {
   public void command_CIRCLE(double x1, double y1, double r, int col, int fill) {
     newoffGraphics.setColor(colorindex[col]);
     if (circleCentered) {
       if (fill!=0) {
	       // this change will break some of the newer programs
         //newoffGraphics.fillOval(x1-r/2,y1+tby-r/2,r,r); // was diameter
	 if ((smoothness&2)==0) {
           newoffGraphics.fillOval((int)x1-(int)r,(int)y1+tby-(int)r,((int)r)*2,((int)r)*2);
	 } else {
	   newoffGraphics.fill(new Ellipse2D.Double(x1-r, y1+tby-r, r*2.0, r*2.0));
	 }
       } else {
         //newoffGraphics.drawOval(x1-r/2,y1+tby-r/2,r,r); // was diameter
	 if ((smoothness&2)==0) {
           newoffGraphics.drawOval((int)x1-(int)r,(int)y1+tby-(int)r,((int)r)*2,((int)r)*2);
	 } else {
	   newoffGraphics.draw(new Ellipse2D.Double(x1-r, y1+tby-r, r*2.0, r*2.0));
	 }
       }
     } else {
       if (fill!=0) {
         newoffGraphics.fillOval((int)x1,(int)y1+tby,(int)r,(int)r);
       } else {
         newoffGraphics.drawOval((int)x1,(int)y1+tby,(int)r,(int)r);
       }
     }
     if (!inframe) doupdate();
   }

   //public void command_RECT(int x1, int y1, int x2, int y2, int col) {
   public void command_RECT(double x1, double y1, double x2, double y2, int col) {
     newoffGraphics.setColor(colorindex[col]);
     if ((smoothness&2)==0) {
       newoffGraphics.fillRect((int)x1,(int)y1+tby,(int)x2-(int)x1,(int)y2-(int)y1);
     } else {
       newoffGraphics.fill(new Rectangle2D.Double(x1,y1+tby,x2-x1,y2-y1));
     }
     if (!inframe) doupdate();
   }

   //public void command_PSET(int x1, int y1, int col) {
   public void command_PSET(double x1, double y1, int col) {
     newoffGraphics.setColor(colorindex[col]);
     if ((smoothness&2)==0) {
       newoffGraphics.fillRect((int)x1,(int)y1+tby,1,1);
     } else {
       //newoffGraphics.draw(new Rectangle2D.Double(x1,y1+tby,1.0,1.0));
       newoffGraphics.draw(new Line2D.Double(x1,y1+tby,x1,y1+tby)); //line to itself
     }
     if (!inframe) doupdate();
   }

   //public void command_FILL(int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4, int col) {
   public void command_FILL(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4, int col) {
     if (col<16) 
       newoffGraphics.setColor(colorindex[col]);
     else
       newoffGraphics.setColor(new Color(col));
     if ((smoothness&2)==0) {
       int xpoints[]= {(int)x1, (int)x2, (int)x3, (int)x4, (int)x1};
       int ypoints[]= {(int)y1+tby, (int)y2+tby, (int)y3+tby, (int)y4+tby, (int)y1+tby};
       newoffGraphics.fillPolygon(xpoints,ypoints,5);
     } else {
       double xpoints[]= {x1, x2, x3, x4, x1};
       double ypoints[]= {y1+tby, y2+tby, y3+tby, y4+tby, y1+tby};
       Path2D prettyPoly = new Path2D.Double();
       boolean isFirst = true;
       for (int i = 0; i < 4; i++) {
         if (isFirst) {
           prettyPoly.moveTo(xpoints[i], ypoints[i]);
           isFirst = false;
         } else {
           prettyPoly.lineTo(xpoints[i], ypoints[i]);
         }
       }
       prettyPoly.closePath();
       //wanted newoffGraphics.fill(new Polygon2D(xpoints,ypoints,5));
       newoffGraphics.fill(prettyPoly);
     }
     if (!inframe) doupdate();
   }

   //public void command_LINE(int x1, int y1, int x2, int y2, int col) {
   public void command_LINE(double x1, double y1, double x2, double y2, int col) {
     newoffGraphics.setColor(colorindex[col]);
     if ((smoothness&2)==0) {
       newoffGraphics.drawLine((int)x1,(int)y1+tby,(int)x2,(int)y2+tby);
     } else {
       newoffGraphics.draw(new Line2D.Double(x1,y1+tby,x2,y2+tby));
     }
     if (!inframe) doupdate();
   }

   public void command_FSET(String fontname, int s) {
     if (fontname == null) return; // quiet ignore
     fontuse=fontname;
     fsize=s;
   }

   int smoothness=0;
   // aa smooth
   // 0->0
   // 1->1
   // 2->3 = antialias and smooth (double) drawing
   public void command_ANTIALIAS(int aa) {
	   if (aa>=2) aa=3; 
    smoothness=aa; // keep this for deciding if we do double based line and point draws
    if (blitting) {
       if ((aa&1)==0)  {
          newoffGraphicsB[0].setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
          newoffGraphicsB[1].setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
       } else {
          newoffGraphicsB[0].setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
          newoffGraphicsB[1].setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
       }
       if ((aa&2)==0)  {
          newoffGraphicsB[0].setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT); // was DEFAULT
          newoffGraphicsB[1].setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT); // was DEFAULT
       } else {
          newoffGraphicsB[0].setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
          newoffGraphicsB[1].setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
       }
    } else {
       if ((aa&1)==0)  {
         newoffGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
       } else {
         newoffGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
       }
       if ((aa&2)==0)  {
          newoffGraphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT);
       } else {
          newoffGraphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
       }
    }
   }

   public void command_LSET(int w, int c) {
     if (c==4) {
       if (w==0)   // 0,4 off
         command_ANTIALIAS(0);
       else        // 1,4 on
         command_ANTIALIAS(1);
     } else {
       lsize=w;
       if (blitting) {
         newoffGraphicsB[0].setStroke(new BasicStroke(w));
         newoffGraphicsB[1].setStroke(new BasicStroke(w));
       } else
         newoffGraphics.setStroke(new BasicStroke(w));
     }
   }

	
   public void command_GPRINT(String text, int x, int y, int col) {
      if (text == null) text=""; // stop bad error if a numeric was passed
      //Font font= new Font ("Monospaced", Font.BOLD, fsize); //maybe keep this global?
      Font font= new Font (fontuse, Font.BOLD, fsize); //maybe keep this global?
      newoffGraphics.setFont(font);
      newoffGraphics.setColor(colorindex[col]);
      newoffGraphics.drawString(text,x,y+tby);
      if (!inframe) doupdate();
   }
   
   // these only make sense if they get called with the right addresses
   public void command_POKE(int address, int val) {
	   if (address==1024) valevent=val&0xFF;
   }

   public int command_PEEK(int address) {
	   if (address==1024) { return valevent; }
	   if (address==1030) { return valx&0xFF; }
	   if (address==1031) { return (valx>>8)&0xFF; }
	   if (address==1032) { return valy&0xFF; }
	   if (address==1033) { return (valy>>8)&0xFF; }
	   return 0;
   }
   
   /* new things for drawing images*/
   int topimage=0;
   private static final int MAXIMAGE=100;
   BufferedImage[] imgarray= new BufferedImage[MAXIMAGE]; // bad hard code at moment
   
   public void command_SAVEIMAGE(String filename) throws IOException {
       save(filename);
   }

   public int command_LOADIMAGE(String filename) throws IOException {
     if (filename.startsWith("+")) { //temp
       save(filename.substring(1));
       return -1;
     }
	 //if (topimage+1==20) return -1;
	 if (topimage+1==MAXIMAGE) topimage=0; // instead, wind it back
     try {
       if (filename.startsWith("http")) {
	  URL url = new URL(filename);
         imgarray[topimage] = ImageIO.read(url.openStream());
       } else {
         imgarray[topimage] = ImageIO.read(new File(filename));
       }
     } catch (IOException e) {
		 return -1;
     }
     return topimage++;
   }

   public void command_DRAWIMAGE(int imgno, int x, int y, double scale, double rotation) {
       if (imgno<0) imgno=topimage-1; //display the last one
       AffineTransform tx = AffineTransform.getScaleInstance(scale, scale);
//       tx.translate(x,y+(double)tby/scale); // was but scales the x,y too
       tx.translate(x/scale,(y+(double)tby)/scale);
       if (rotation!=0.0) tx.rotate(rotation); 
       //AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
       //newoffGraphics.drawImage(imgarray[imgno],tx,null);
       AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BICUBIC);
       newoffGraphics.drawImage(imgarray[imgno],op,0,0);

     if (!inframe) doupdate();
   }
   
   
/*******************************************************************************************/

  public void mousePressed(MouseEvent e)
  {
    valx=e.getX();
    valy=e.getY()-tby;
    // also set type to 1
    valevent=1;    
  }

  public void mouseReleased(MouseEvent e)
  {
    valx=e.getX();
    valy=e.getY()-tby;
    // also set type to 1
    valevent=3;    
  }

  public void mouseDragged(MouseEvent e)
  {
    valx=e.getX();
    valy=e.getY()-tby;
    // also set type to 1
    valevent=2;    
  }
   
   
   void dothings() {
     // just in a loop

     command_CLS();
     while (true) {

       // do out of paint drawing
       
       //newoffGraphics.drawLine(124,24,235,252);
       command_LINE(
         (int)(generator.nextDouble()*1000)
         ,(int)(generator.nextDouble()*1000)
         ,(int)(generator.nextDouble()*1000)
         ,(int)(generator.nextDouble()*1000)
         ,(int)(generator.nextDouble()*16)
       );
       command_GPRINT("x",(int)(generator.nextDouble()*1000)
         ,(int)(generator.nextDouble()*1000),3);

       if (command_PEEK(1024)!=0) {
         command_GPRINT("m="+command_PEEK(1024),
           command_PEEK(1030)+command_PEEK(1031)*256,
           command_PEEK(1032)+command_PEEK(1033)*256
           ,9
         );
         command_POKE(1024,0);
	   }
         
       doupdate();
       try {
         Thread.sleep(1);       // in the extreme slowdown
       }
       catch(InterruptedException e) {
       }
     }

   }
 
  public void mouseExited(MouseEvent e)
  {
  }

  public void mouseEntered(MouseEvent e)
  {
  }

  public void mouseClicked(MouseEvent e)
  {
	}
	
	public void mouseMoved(MouseEvent e)
  {
	}
	  
   
  public synchronized void paint(Graphics g) {
    Graphics2D g2d = (Graphics2D) g;


    if(blitting) {
      //if(!inframe)
          //g2d.drawImage(newoffImageB[1-blit], 0, 0, this);
      //else
       if (!inframe || paintblit!=blit)
          g2d.drawImage(newoffImageB[paintblit], tbx, 0, this);
    } else {
      if(!inframe)
          g2d.drawImage(newoffImage, tbx, 0, this);
    }

  }

  public void update(Graphics g) {
    if(!inframe)
      paint(g);
  }

        public static void main(String[] args) {
                //JFrame frame = new JFrame();
                //Canvas gui = new Canvas();
                //frame.getContentPane().add(gui);
                /*Thread gameThread = new Thread(new GameLoop(gui));*/
                /*gameThread.setPriority(Thread.MIN_PRIORITY);*/
                /*gameThread.start();  start Game processing.*/
                GraphicsDevice graphicsDevice= new GraphicsDevice();
                graphicsDevice.dothings();
        }
  

} /* class */

/////////
// END //
/////////
