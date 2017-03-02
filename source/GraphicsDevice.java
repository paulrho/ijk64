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

public class GraphicsDevice extends JFrame implements MouseListener, MouseMotionListener {

  Image newoffImage;
  Graphics2D newoffGraphics;

  Random generator = new Random();

//  int sizex=1000; 
//  int sizey=1000;
  int tby=30;
  final int default_sizex=768;
  final int default_sizey=1004+tby;
  int sizex=default_sizex;   
  int sizey=default_sizey;
  
   
  public GraphicsDevice(int x, int y) {
    super("ijk64 graphics");
    initDevice(x,y);
  }
  public GraphicsDevice() {
    super("ijk64 graphics");
    initDevice(sizex,sizey-tby);
  }
  
  void initDevice(int x, int y) {
    sizex=x;
    sizey=tby+y;
    setSize(sizex,sizey);
    setVisible(true); // start AWT painting.
    addMouseListener(this);
    addMouseMotionListener(this);
	newoffImage = createImage(sizex,sizey);
	newoffGraphics = (Graphics2D) newoffImage.getGraphics();
    if (true) newoffGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
  }
      
  void save(String filename) {
    //Graphics2D cg = newoffImage.createGraphics();
    try {
      if (ImageIO.write((BufferedImage)newoffImage, "png", new File(filename+".png"))) {
        System.out.println("-- saved");
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
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
   
   public void command_BEGINFRAME() {
     inframe=true;
   }

   public void command_ENDFRAME() {
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
     newoffGraphics.setColor(Color.WHITE);
     newoffGraphics.fillRect(0, 0, sizex,sizey+tby);	   
     //newoffGraphics.clearRect(0, 0, sizex,sizey+tby);	   
     if (!inframe) doupdate();
   }
         
   public void command_CIRCLE(int x1, int y1, int r, int col, int fill) {
     newoffGraphics.setColor(colorindex[col]);
     if (fill!=0) {
       newoffGraphics.fillOval(x1,y1+tby,r,r);
     } else {
       newoffGraphics.drawOval(x1,y1+tby,r,r);
     }
     if (!inframe) doupdate();
   }

   public void command_RECT(int x1, int y1, int x2, int y2, int col) {
     newoffGraphics.setColor(colorindex[col]);
     newoffGraphics.fillRect(x1,y1+tby,x2-x1,y2-y1);
     if (!inframe) doupdate();
   }

   public void command_FILL(int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4, int col) {
     int xpoints[]= {x1, x2, x3, x4, x1};
     int ypoints[]= {y1, y2, y3, y4, y1};
     if (col<16) 
       newoffGraphics.setColor(colorindex[col]);
     else
       newoffGraphics.setColor(new Color(col));
     newoffGraphics.fillPolygon(xpoints,ypoints,5);
     if (!inframe) doupdate();
   }

   public void command_LINE(int x1, int y1, int x2, int y2, int col) {
     newoffGraphics.setColor(colorindex[col]);
     newoffGraphics.drawLine(x1,y1+tby,x2,y2+tby);
     if (!inframe) doupdate();
   }

   public void command_FSET(String fontname, int s) {
	 fontuse=fontname;
     fsize=s;
   }

   public void command_LSET(int w, int c) {
     if (c==4) {
       if (w==0)   // 0,4 off
         newoffGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
       else        // 1,4 on
         newoffGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
     } else {
       lsize=w;
       newoffGraphics.setStroke(new BasicStroke(w));
     }
   }

	
   public void command_GPRINT(String text, int x, int y, int col) {
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
   BufferedImage[] imgarray= new BufferedImage[20]; // bad hard code at moment
   
   public void command_SAVEIMAGE(String filename) {
       save(filename);
   }

   public int command_LOADIMAGE(String filename) {
     if (filename.startsWith("+")) { //temp
       save(filename.substring(1));
       return -1;
     }
	 if (topimage+1==20) return -1;
     try {
       imgarray[topimage] = ImageIO.read(new File(filename));
     } catch (IOException e) {
		 return -1;
     }
     return topimage++;
   }

   public void command_DRAWIMAGE(int imgno, int x, int y, double scale, double rotation) {
       AffineTransform tx = AffineTransform.getScaleInstance(scale, scale);
//       tx.translate(x,y+(double)tby/scale); // was but scales the x,y too
       tx.translate(x/scale,(y+(double)tby)/scale);
       if (rotation!=0.0) tx.rotate(rotation); 
       AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
       newoffGraphics.drawImage(imgarray[imgno],tx,null);

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


    if(!inframe)
          g2d.drawImage(newoffImage, 0, 0, this);

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

/*******
 * END *
 *******/

