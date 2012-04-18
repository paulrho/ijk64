// just doxx did it
import java.awt.image.*; // for Image
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;        // for key events
import java.awt.Toolkit.*;      // for image loading

public class GraphicsDevice extends JFrame implements MouseListener, MouseMotionListener {

  Image newoffImage;
  Graphics2D newoffGraphics;

  Random generator = new Random();

  int sizex=1000; 
  int sizey=1000;
   
  GraphicsDevice() {
    setSize(sizex,sizey);
    setVisible(true); // start AWT painting.
    addMouseListener(this);
    addMouseMotionListener(this);
	newoffImage = createImage(1000,1000);
	newoffGraphics = (Graphics2D) newoffImage.getGraphics();
    if (true) newoffGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
  }
      
  void createDisplay() {
	   // assumed
  }

  public void resetDevice() {
    command_ENDFRAME();
    fsize=16;
  }
   
  void doupdate() {
	  // after every draw OR at the endfram   
     repaint();
  }

/*******************************************************************************************/

   Color colorindex[] = {Color.WHITE,Color.RED,Color.GREEN,Color.BLUE
      ,Color.BLACK
      ,new Color(255,0,255)
      ,Color.YELLOW
      ,Color.CYAN
      ,Color.ORANGE
      ,new Color(128,0,0)
      ,Color.PINK
      ,Color.DARK_GRAY
      ,Color.GRAY
      ,new Color(128,255,128)
      ,new Color(128,128,255)
      ,Color.LIGHT_GRAY
  };


   int fsize=16;
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

   public void command_SLEEP(int milliseconds) {
       try {
         Thread.sleep(milliseconds);       // in the extreme slowdown
       }
       catch(InterruptedException e) {
       }
   }

   public synchronized void command_CLS() { /* try this */
     newoffGraphics.setColor(Color.WHITE);
     newoffGraphics.fillRect(0, 0, sizex,sizey);	   
     if (!inframe) doupdate();
   }
         
   public void command_RECT(int x1, int y1, int x2, int y2, int col) {
     newoffGraphics.setColor(colorindex[col]);
     newoffGraphics.fillRect(x1,y1,x2-x1,y2-y1);
     if (!inframe) doupdate();
   }

   public void command_LINE(int x1, int y1, int x2, int y2, int col) {
     newoffGraphics.setColor(colorindex[col]);
     newoffGraphics.drawLine(x1,y1,x2,y2);
     if (!inframe) doupdate();
   }

   public void command_FSET(String fontname, int s) {
     fsize=s;
   }
	
   public void command_GPRINT(String text, int x, int y, int col) {
      Font font= new Font ("Monospaced", Font.BOLD, fsize); //maybe keep this global?
      newoffGraphics.setFont(font);
      newoffGraphics.setColor(colorindex[col]);
      newoffGraphics.drawString(text,x,y);
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
   
/*******************************************************************************************/

  public void mousePressed(MouseEvent e)
  {
    valx=e.getX();
    valy=e.getY();
    // also set type to 1
    valevent=1;    
  }

  public void mouseReleased(MouseEvent e)
  {
    valx=e.getX();
    valy=e.getY();
    // also set type to 1
    valevent=3;    
  }

  public void mouseDragged(MouseEvent e)
  {
    valx=e.getX();
    valy=e.getY();
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
