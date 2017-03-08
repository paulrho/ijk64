/////////////////////////////////////////////////////////////////////////////////
//
//
//
/////////////////////////////////////////////////////////////////////////////////
import java.awt.*;
import java.awt.event.*;
import javax.swing.JPopupMenu;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.ButtonGroup;
//import javax.swing.JMenuBar;
import javax.swing.KeyStroke;
import javax.swing.ImageIcon;

import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JFrame;

import javax.swing.*;
import javax.swing.filechooser.*;

import javax.swing.event.*; // popupmenulistener

// for Files
 import java.io.*;

/* PopupMenuDemo.java requires images/middle.gif. */

/*
 * Like MenuDemo, but with popup menus added.
 */
public class C64PopupMenu implements ActionListener, ItemListener {

    String newline = "\n";
    JCheckBoxMenuItem cbMenuItem_bgtrans;
    Machine machine;
    C64Screen screen;
    String arg;
    String command;
    boolean forcedcompletion=false;

    public void createPopupMenu() {
        JMenuItem menuItem;
        JCheckBoxMenuItem cbMenuItem;
        JRadioButtonMenuItem rbMenuItem;
        JMenu submenu;
        JMenu submenu2;

  
	// try this
	// this does fix the problem of the menu dissappearing when completely within the bounds of the window
	javax.swing.JPopupMenu.setDefaultLightWeightPopupEnabled(false);
	//

        //Create the popup menu.
        JPopupMenu popup = new JPopupMenu();

        //popup.setLabel("JEBI"); // doesnt work

        ImageIcon icon = createImageIcon("images/run.png");
        menuItem = new JMenuItem("JEBI / ijk64",icon);
	menuItem.setEnabled(false);
        menuItem.addActionListener(this);
        popup.add(menuItem);

        popup.addSeparator();

        //-------------------------------------------------
        //icon = createImageIcon("player_play.png");
        submenu = new JMenu("File...");
        submenu.setMnemonic(KeyEvent.VK_F);
        submenu.addActionListener(this);
        popup.add(submenu);

        icon = createImageIcon("images/filenew.png");
        menuItem = new JMenuItem("New",icon);
        submenu.setMnemonic(KeyEvent.VK_N);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_N, ActionEvent.CTRL_MASK));
        menuItem.addActionListener(this);
        submenu.add(menuItem);

        icon = createImageIcon("images/fileopen.png");
        menuItem = new JMenuItem("Directory",icon);
        submenu.setMnemonic(KeyEvent.VK_O);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        menuItem.addActionListener(this);
        submenu.add(menuItem);

        icon = createImageIcon("images/fileopen.png");
        menuItem = new JMenuItem("Open...",icon);
        submenu.setMnemonic(KeyEvent.VK_O);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        menuItem.addActionListener(this);
        submenu.add(menuItem);

        icon = createImageIcon("images/fileopen.png");
        menuItem = new JMenuItem("Open (and run)...",icon);
        submenu.setMnemonic(KeyEvent.VK_O);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        menuItem.addActionListener(this);
        submenu.add(menuItem);

        submenu.addSeparator();
        icon = createImageIcon("images/filesave.png");
        menuItem = new JMenuItem("Save",icon);
	//menuItem.setEnabled(false);
        submenu.setMnemonic(KeyEvent.VK_S);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        menuItem.addActionListener(this);
        submenu.add(menuItem);

        submenu.addSeparator();
        icon = createImageIcon("images/exit.png");
        menuItem = new JMenuItem("Exit",icon);
        submenu.setMnemonic(KeyEvent.VK_X);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_X, ActionEvent.CTRL_MASK));
        menuItem.addActionListener(this);
        submenu.add(menuItem);

        icon = createImageIcon("images/run.png");
        menuItem = new JMenuItem("Run program",icon);
        menuItem.addActionListener(this);
        popup.add(menuItem);

        icon = createImageIcon("images/applet-critical-blank.png");
        menuItem = new JMenuItem("Break run",icon);
        menuItem.addActionListener(this);
        popup.add(menuItem);

        icon = createImageIcon("images/editpaste.png");
        menuItem = new JMenuItem("List program",icon);
        menuItem.addActionListener(this);
        popup.add(menuItem);

        icon = createImageIcon("images/applet-critical-blank.png");
        menuItem = new JMenuItem("Reset machine",icon);
        menuItem.addActionListener(this);
        popup.add(menuItem);

        popup.addSeparator();

        submenu2 = new JMenu("Keys...");
        submenu2.setMnemonic(KeyEvent.VK_F);
        submenu2.addActionListener(this);
        popup.add(submenu2);

        menuItem = new JMenuItem("C= [Ctrl]");
        menuItem.addItemListener(this);
        submenu2.add(menuItem);

        menuItem = new JMenuItem("CTRL [Tab]");
        menuItem.addItemListener(this);
        submenu2.add(menuItem);

        menuItem = new JMenuItem("CLR (clear screen) [CTRL HOME]");
        menuItem.addItemListener(this);
        submenu2.add(menuItem);

        menuItem = new JMenuItem("HOME (move to top of screen)");
        menuItem.addItemListener(this);
        submenu2.add(menuItem);

        menuItem = new JMenuItem("BREAK [SHIFT ESC]");
        menuItem.addItemListener(this);
        submenu2.add(menuItem);

        submenu2.addSeparator();

        menuItem = new JMenuItem("End of Line [END]");
        menuItem.setMnemonic(KeyEvent.VK_END);
        //menuItem.setAccelerator(KeyStroke.getKeyStroke(
        //        KeyEvent.VK_END));
        menuItem.addItemListener(this);
        submenu2.add(menuItem);

        menuItem = new JMenuItem("Toggle insert [INSERT]");
        menuItem.addItemListener(this);
        submenu2.add(menuItem);

        menuItem = new JMenuItem("[DELETE]");
        menuItem.addItemListener(this);
        submenu2.add(menuItem);

        menuItem = new JMenuItem("Page Up [PAGE UP]");
        menuItem.addItemListener(this);
        submenu2.add(menuItem);

        menuItem = new JMenuItem("Page Down [PAGE DOWN]");
        menuItem.addItemListener(this);
        submenu2.add(menuItem);

        menuItem = new JMenuItem("Scroll Wheel (up/down)");
        menuItem.addItemListener(this);
        submenu2.add(menuItem);

        submenu2.addSeparator();

        menuItem = new JMenuItem("[CTRL]+[SHIFT]+C - copy");
        menuItem.addItemListener(this);
        submenu2.add(menuItem);

        menuItem = new JMenuItem("[CTRL]+[SHIFT]+V - paste");
        menuItem.addItemListener(this);
        submenu2.add(menuItem);

        submenu2 = new JMenu("Screen...");
        submenu2.setMnemonic(KeyEvent.VK_F);
        submenu2.addActionListener(this);
        popup.add(submenu2);

        cbMenuItem_bgtrans = new JCheckBoxMenuItem("Transparent background");
        cbMenuItem_bgtrans.setMnemonic(KeyEvent.VK_B);
        cbMenuItem_bgtrans.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_B, ActionEvent.ALT_MASK));
        cbMenuItem_bgtrans.addItemListener(this);
        submenu2.add(cbMenuItem_bgtrans);

        cbMenuItem = new JCheckBoxMenuItem("Hidden (faint) text");
        cbMenuItem.setSelected(screen.faint);
        cbMenuItem.setMnemonic(KeyEvent.VK_H);
        cbMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_H, ActionEvent.ALT_MASK));
        cbMenuItem.addItemListener(this);
        submenu2.add(cbMenuItem);

        cbMenuItem = new JCheckBoxMenuItem("Drop shadow");
        cbMenuItem.setSelected(screen.bgshadow);
        cbMenuItem.setMnemonic(KeyEvent.VK_D);
        cbMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_D, ActionEvent.ALT_MASK));
        cbMenuItem.addItemListener(this);
        submenu2.add(cbMenuItem);

        cbMenuItem = new JCheckBoxMenuItem("Uppercase");
        cbMenuItem.setSelected(true);
        cbMenuItem.addItemListener(this);
        submenu2.add(cbMenuItem);

        cbMenuItem = new JCheckBoxMenuItem("Sync background image");
        cbMenuItem.setSelected(true);
        cbMenuItem.addItemListener(this);
        submenu2.add(cbMenuItem);

        cbMenuItem = new JCheckBoxMenuItem("Frames");
        cbMenuItem.setSelected(!screen.isUndecorated());
        cbMenuItem.addItemListener(this);
        submenu2.add(cbMenuItem);

        cbMenuItem = new JCheckBoxMenuItem("SendToBack");
        cbMenuItem.setSelected(false);
        cbMenuItem.addItemListener(this);
        submenu2.add(cbMenuItem);


        submenu = new JMenu("Machine...");
        submenu.setMnemonic(KeyEvent.VK_F);
        submenu.addActionListener(this);
        popup.add(submenu);

        cbMenuItem = new JCheckBoxMenuItem("50% duty cycle");
        //cbMenuItem.setSelected(true);
        cbMenuItem.addItemListener(this);
        submenu.add(cbMenuItem);
        cbMenuItem = new JCheckBoxMenuItem("Syntax highlighting");
        cbMenuItem.setSelected(true);
        cbMenuItem.addItemListener(this);
        submenu.add(cbMenuItem);


	//------------------------------------------------------------
        submenu2.addSeparator();
        submenu = new JMenu("Preset Modes");
        submenu.setMnemonic(KeyEvent.VK_S);
        menuItem.addActionListener(this);
        submenu2.add(submenu);
        menuItem = new JMenuItem("Commodore 128 80x25 monitor mode (double height pixels)");
        menuItem.addActionListener(this);
        submenu.add(menuItem);
        menuItem = new JMenuItem("Commodore 64 default mode");
        menuItem.addActionListener(this);
        submenu.add(menuItem);
        menuItem = new JMenuItem("Commodore 64 BIG screen");
        menuItem.addActionListener(this);
        submenu.add(menuItem);
        menuItem = new JMenuItem("Lots of space 80x80");
        menuItem.addActionListener(this);
        submenu.add(menuItem);
        menuItem = new JMenuItem("Vic20");
        menuItem.addActionListener(this);
        submenu.add(menuItem);

        //-------------------------------------------------------------
        //a group of radio button menu items
        submenu2.addSeparator();
        ButtonGroup group = new ButtonGroup();

        rbMenuItem = new JRadioButtonMenuItem("40 columns");
        rbMenuItem.setSelected(true);
        rbMenuItem.setMnemonic(KeyEvent.VK_S);
        group.add(rbMenuItem);
        rbMenuItem.addActionListener(this);
        submenu2.add(rbMenuItem);

        rbMenuItem = new JRadioButtonMenuItem("80 columns");
        rbMenuItem.setMnemonic(KeyEvent.VK_O);
        group.add(rbMenuItem);
        rbMenuItem.addActionListener(this);
        submenu2.add(rbMenuItem);

        //-------------------------------------------------------------
        //a group of check box menu items
        submenu2.addSeparator();
        group = new ButtonGroup();

        rbMenuItem = new JRadioButtonMenuItem("25 rows");
        rbMenuItem.setSelected(true);
        rbMenuItem.setMnemonic(KeyEvent.VK_R);
        group.add(rbMenuItem);
        rbMenuItem.addActionListener(this);
        submenu2.add(rbMenuItem);

        rbMenuItem = new JRadioButtonMenuItem("40 rows");
        rbMenuItem.setMnemonic(KeyEvent.VK_O);
        group.add(rbMenuItem);
        rbMenuItem.addActionListener(this);
        submenu2.add(rbMenuItem);

        rbMenuItem = new JRadioButtonMenuItem("90 rows");
        rbMenuItem.setMnemonic(KeyEvent.VK_O);
        group.add(rbMenuItem);
        rbMenuItem.addActionListener(this);
        submenu2.add(rbMenuItem);

        //-------------------------------------------------------------
        //a group of check box menu items
        submenu2.addSeparator();
        group = new ButtonGroup();

        rbMenuItem = new JRadioButtonMenuItem("Single pixel ( * 1 )");
        rbMenuItem.setSelected(true);
        rbMenuItem.setMnemonic(KeyEvent.VK_R);
        group.add(rbMenuItem);
        rbMenuItem.addActionListener(this);
        submenu2.add(rbMenuItem);

        rbMenuItem = new JRadioButtonMenuItem("Double pixel ( * 2 )");
        rbMenuItem.setMnemonic(KeyEvent.VK_O);
        group.add(rbMenuItem);
        rbMenuItem.addActionListener(this);
        submenu2.add(rbMenuItem);

        rbMenuItem = new JRadioButtonMenuItem("Triple pixel ( * 3 )");
        rbMenuItem.setMnemonic(KeyEvent.VK_O);
        group.add(rbMenuItem);
        rbMenuItem.addActionListener(this);
        submenu2.add(rbMenuItem);

        rbMenuItem = new JRadioButtonMenuItem("Double height ( Y*2 )");
        rbMenuItem.setMnemonic(KeyEvent.VK_O);
        group.add(rbMenuItem);
        rbMenuItem.addActionListener(this);
        submenu2.add(rbMenuItem);


        submenu = new JMenu("Editor...");
        popup.add(submenu);
        // submenu

        cbMenuItem = new JCheckBoxMenuItem("Insert Chars");
        cbMenuItem.setMnemonic(KeyEvent.VK_S);
        cbMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_S, ActionEvent.ALT_MASK));
        cbMenuItem.addItemListener(this);
        submenu.add(cbMenuItem);

        cbMenuItem = new JCheckBoxMenuItem("Allow Scroll");
        cbMenuItem.setMnemonic(KeyEvent.VK_S);
        cbMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_S, ActionEvent.ALT_MASK));
        cbMenuItem.addItemListener(this);
        submenu.add(cbMenuItem);

        cbMenuItem = new JCheckBoxMenuItem("Allow Point");
        cbMenuItem.setMnemonic(KeyEvent.VK_S);
        cbMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_S, ActionEvent.ALT_MASK));
        cbMenuItem.addItemListener(this);
        submenu.add(cbMenuItem);

        cbMenuItem = new JCheckBoxMenuItem("Line length infinite");
        cbMenuItem.setMnemonic(KeyEvent.VK_S);
        cbMenuItem.addItemListener(this);
        submenu.add(cbMenuItem);


        //-------------------------------------------------------------
        //a group of check box menu items
        popup.addSeparator();

        submenu = new JMenu("Debugging...");
        popup.add(submenu);
        // submenu

        cbMenuItem = new JCheckBoxMenuItem("Paint slowdown");
        cbMenuItem.setMnemonic(KeyEvent.VK_S);
        cbMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_S, ActionEvent.ALT_MASK));
        cbMenuItem.addItemListener(this);
        submenu.add(cbMenuItem);

          cbMenuItem = new JCheckBoxMenuItem("Verbose");
          cbMenuItem.setMnemonic(KeyEvent.VK_V);
          cbMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                  KeyEvent.VK_V, ActionEvent.ALT_MASK));
          cbMenuItem.addItemListener(this);
          submenu.add(cbMenuItem);
  
          cbMenuItem = new JCheckBoxMenuItem("BASIC timing profiling");
          cbMenuItem.addItemListener(this);
          submenu.add(cbMenuItem);
  
          cbMenuItem = new JCheckBoxMenuItem("Evaluate engine verbose");
	  cbMenuItem.setEnabled(false);
          cbMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                  KeyEvent.VK_V, ActionEvent.ALT_MASK));
          cbMenuItem.addItemListener(this);
          submenu.add(cbMenuItem);
  
          cbMenuItem = new JCheckBoxMenuItem("Verbose Keycodes");
	  cbMenuItem.setEnabled(false);
          cbMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                  KeyEvent.VK_V, ActionEvent.ALT_MASK));
          cbMenuItem.addItemListener(this);
          submenu.add(cbMenuItem);
  
          cbMenuItem = new JCheckBoxMenuItem("Paint Timings");
	  cbMenuItem.setEnabled(false);
          cbMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                  KeyEvent.VK_V, ActionEvent.ALT_MASK));
          cbMenuItem.addItemListener(this);
          submenu.add(cbMenuItem);
  
          menuItem = new JMenuItem("Dumpstate now");
          menuItem.addActionListener(this);
          submenu.add(menuItem);

        popup.addSeparator(); // internal mechanics

	//------------------------------------------------------------
        submenu = new JMenu("Show keyboard translations");
        popup.add(submenu);
        JMenu PCsubmenu = new JMenu("PC keyboard");
        submenu.add(PCsubmenu);

        JMenu PCsubmenu_plain= new JMenu("Plain set");
        PCsubmenu.add(PCsubmenu_plain);
        icon = createImageIcon("images/psd2.jpg");
        menuItem = new JMenuItem("Plain set",icon);
        PCsubmenu_plain.add(menuItem);

        JMenu PCsubmenu_shift= new JMenu("Shifted set");
        PCsubmenu.add(PCsubmenu_shift);
        icon = createImageIcon("images/psd2.jpg");
        menuItem = new JMenuItem("Shifted set",icon);
        PCsubmenu_shift.add(menuItem);

        JMenu PCsubmenu_comm= new JMenu("C= set");
        PCsubmenu.add(PCsubmenu_comm);
        icon = createImageIcon("images/psd2.jpg");
        menuItem = new JMenuItem("C= set",icon);
        PCsubmenu_comm.add(menuItem);

        JMenu PCsubmenu_ctrl= new JMenu("CTRL= set");
        PCsubmenu.add(PCsubmenu_ctrl);
        icon = createImageIcon("images/psd2.jpg");
        menuItem = new JMenuItem("CTRL set",icon);
        PCsubmenu_ctrl.add(menuItem);

        JMenu C64submenu = new JMenu("C64 keyboard");
        submenu.add(C64submenu);
        icon = createImageIcon("images/c64c2.jpg");
        menuItem = new JMenuItem("Original",icon);
        C64submenu.add(menuItem);

        submenu = new JMenu("Show character sets");
        submenu.setMnemonic(KeyEvent.VK_S);
        popup.add(submenu);

        JMenu setmenu = new JMenu("Uppercase set");
        submenu.add(setmenu);
          icon = createImageIcon("images/c64_upp.gif");
          JMenuItem cset= new JMenuItem("Uppercase Set",icon);
          setmenu.add(cset);
        setmenu = new JMenu("Lowercase set");
        submenu.add(setmenu);
          icon = createImageIcon("images/c64_low.gif");
          cset= new JMenuItem("Lowercase Set",icon);
          setmenu.add(cset);

        //menuItem.setAccelerator(KeyStroke.getKeyStroke(
                //KeyEvent.VK_2, ActionEvent.ALT_MASK));
        //menuItem.addActionListener(this);
        //submenu.add(menuItem);

	//------------------------------------------------------------
        submenu = new JMenu("Show commands");
        submenu.setMnemonic(KeyEvent.VK_S);
        popup.add(submenu);

        JMenu submenu1 = new JMenu("META commands");
        submenu.add(submenu1);
          menuItem = new JMenuItem("META-CHARSET [0|1]"); submenu1.add(menuItem);
          menuItem = new JMenuItem("META-SCALE [0|1|2]"); submenu1.add(menuItem);
          menuItem = new JMenuItem("META-SCALEY [1|2]"); submenu1.add(menuItem);
          menuItem = new JMenuItem("META-ROWS n"); submenu1.add(menuItem);
          menuItem = new JMenuItem("META-COLS n"); submenu1.add(menuItem);
          menuItem = new JMenuItem("META-BGTRANS [0|1]"); submenu1.add(menuItem);
          menuItem = new JMenuItem("META-VERBOSE"); submenu1.add(menuItem);
          menuItem = new JMenuItem("META-TIMING"); submenu1.add(menuItem);
          menuItem = new JMenuItem("META-DUMPSTATE"); submenu1.add(menuItem); menuItem.addActionListener(this);
          menuItem = new JMenuItem("EXIT"); submenu1.add(menuItem);
        submenu2 = new JMenu("GRAPHICS/HAND commands");
        submenu.add(submenu2);
          menuItem = new JMenuItem("SCREEN [0|1]"); submenu2.add(menuItem);
          menuItem = new JMenuItem("GPRINT"); submenu2.add(menuItem);
          menuItem = new JMenuItem("BEGINFRAME"); submenu2.add(menuItem);
          menuItem = new JMenuItem("ENDFRAME"); submenu2.add(menuItem);
          menuItem = new JMenuItem("CLS"); submenu2.add(menuItem);
          menuItem = new JMenuItem("LINE"); submenu2.add(menuItem);
          menuItem = new JMenuItem("FSET"); submenu2.add(menuItem);
          menuItem = new JMenuItem("SLEEP"); submenu2.add(menuItem);
          menuItem = new JMenuItem("ALERT"); submenu2.add(menuItem);
          menuItem = new JMenuItem("RECT"); submenu2.add(menuItem);
          menuItem = new JMenuItem("FILES"); submenu2.add(menuItem);
          menuItem = new JMenuItem("LSET"); submenu2.add(menuItem);
          menuItem = new JMenuItem("IMAGELOAD"); submenu2.add(menuItem);
          menuItem = new JMenuItem("DRAWIMAGE"); submenu2.add(menuItem);
          menuItem = new JMenuItem("DESTROYIMAGE"); submenu2.add(menuItem);
        JMenu submenu3 = new JMenu("C64 commands");
        submenu.add(submenu3);
          menuItem = new JMenuItem("FOR TO STEP"); submenu3.add(menuItem);
          menuItem = new JMenuItem("NEXT"); submenu3.add(menuItem);
          menuItem = new JMenuItem("IF THEN"); submenu3.add(menuItem);
          menuItem = new JMenuItem("GOTO"); submenu3.add(menuItem);
          menuItem = new JMenuItem("GOSUB"); submenu3.add(menuItem);
          menuItem = new JMenuItem("RETURN"); submenu3.add(menuItem);
          menuItem = new JMenuItem("PRINT"); submenu3.add(menuItem);
          menuItem = new JMenuItem("DIM"); submenu3.add(menuItem);
          menuItem = new JMenuItem("GET"); submenu3.add(menuItem);
          menuItem = new JMenuItem("POKE"); submenu3.add(menuItem);
          menuItem = new JMenuItem("OPEN"); submenu3.add(menuItem);
          menuItem = new JMenuItem("INPUT"); submenu3.add(menuItem);
          menuItem = new JMenuItem("CLOSE"); submenu3.add(menuItem);
          menuItem = new JMenuItem("DATA"); submenu3.add(menuItem);
          menuItem = new JMenuItem("READ"); submenu3.add(menuItem);
          menuItem = new JMenuItem("RESTORE"); submenu3.add(menuItem);
          menuItem = new JMenuItem("END"); submenu3.add(menuItem);
          submenu3.addSeparator();
          menuItem = new JMenuItem("FAST"); submenu3.add(menuItem);
        submenu.addSeparator();
        menuItem = new JMenuItem("LIST"); submenu.add(menuItem);
        menuItem = new JMenuItem("LOAD"); submenu.add(menuItem);
        menuItem = new JMenuItem("RUN"); submenu.add(menuItem);
        menuItem = new JMenuItem("CONT"); submenu.add(menuItem);
        menuItem = new JMenuItem("CLR"); submenu.add(menuItem);
        menuItem = new JMenuItem("NEW"); submenu.add(menuItem);
        menuItem = new JMenuItem("SYS nnnnn"); submenu.add(menuItem);
        submenu.addSeparator();
        menuItem = new JMenuItem("HELP"); submenu.add(menuItem);

	//------------------------------------------------------------
        submenu = new JMenu("Command Line parameters");
        popup.add(submenu);
        menuItem = new JMenuItem("C64 basicfilename.basic ..."); submenu.add(menuItem);
        menuItem = new JMenuItem("  -c : center on start"); submenu.add(menuItem);
        menuItem = new JMenuItem("  -[1|2|3] : size of pixels"); submenu.add(menuItem);
        menuItem = new JMenuItem("  -r : run on startup"); submenu.add(menuItem);
        menuItem = new JMenuItem("  -b : blank screen no banner"); submenu.add(menuItem);
        menuItem = new JMenuItem("  -n : no frame"); submenu.add(menuItem);
        menuItem = new JMenuItem("  -t : transparent background"); submenu.add(menuItem);
        menuItem = new JMenuItem("  -h : command line help"); submenu.add(menuItem);

	//------------------------------------------------------------
        popup.addSeparator();

        icon = createImageIcon("images/help.png");
        menuItem = new JMenuItem("Help",icon);
        menuItem.addActionListener(this);
        popup.add(menuItem);

        icon = createImageIcon("images/editpaste.png");
        menuItem = new JMenuItem("About",icon);
        menuItem.addActionListener(this);
        popup.add(menuItem);

if (false) {
  popup.addPopupMenuListener(new PopupMenuListener() {

      @Override
      public void popupMenuCanceled(PopupMenuEvent e) {
          System.out.println("cancelled");
          screen.invalidate();
          screen.forcedrepaint();
      }

      @Override
      public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
          System.out.println("vanishing");
          screen.invalidate();
          screen.forcedrepaint();
      }

      @Override
      public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
          System.out.println("appearing");
      }
  });
}


        //Add listener to the text area so the popup menu can come up.
        MouseListener popupListener = new PopupListener(popup);
	// attach to screen
        ((JFrame)screen).addMouseListener(popupListener);
    }

   final static char PETSCII_ENTER = 13;

    public void actionPerformed(ActionEvent e) {
        JMenuItem source = (JMenuItem)(e.getSource());
        String s = "Action event detected."
                   + newline
                   + "    Event source: " + source.getText()
                   + " (an instance of " + getClassName(source) + ")";
        System.out.print(s + newline);
      if (source.getText().equals("80 columns")) {
        System.out.print("Setting to 80 columns...\n");
              screen.setCols(80);
      } else if (source.getText().equals("Commodore 128 80x25 monitor mode (double height pixels)")) {
              screen.setColsRows(80,25);
              screen.setScale(1);
              screen.setScaleY(2);
      } else if (source.getText().equals("Commodore 64 default mode")) {
              screen.setScaleY(1);
              screen.setColsRows(40,25);
              machine.performPOKE(53280,14);
              machine.performPOKE(53281,6);
      } else if (source.getText().equals("Commodore 64 BIG screen")) {
              screen.setScaleY(1);
              screen.setColsRows(40,25);
              screen.setScale(3);
      } else if (source.getText().equals("Lots of space 80x80")) {
              screen.setScaleY(1);
              screen.setScale(1);
              screen.setColsRows(80,80);
      } else if (source.getText().equals("Vic20")) {
              screen.setScaleY(1);
              screen.setScale(3);
              screen.setColsRows(22,23);
              machine.performPOKE(53281,1);
              machine.performPOKE(53280,3);
      } else if (source.getText().equals("40 columns")) {
        System.out.print("Setting to 40 columns...\n");
              screen.setCols(40);
      } else if (source.getText().equals("Single pixel ( * 1 )")) {
        System.out.print("Setting single pixel...\n");
              screen.setScale(1);
      } else if (source.getText().equals("Double pixel ( * 2 )")) {
        System.out.print("Setting double pixel...\n");
              screen.setScale(2);
      } else if (source.getText().equals("Triple pixel ( * 3 )")) {
        System.out.print("Setting triple pixel...\n");
              screen.setScale(3);
      } else if (source.getText().equals("Double height ( Y*2 )")) {
        System.out.print("Setting double height pixel...\n");
              screen.setScaleY(2);
      } else if (source.getText().equals("25 rows")) {
        System.out.print("Setting 25 rows...\n");
              screen.setRows(25);
      } else if (source.getText().equals("About")) {
                aboutBox();
      } else if (source.getText().equals("New")) {
              //oldway//System.out.print("New (sys)...\n");
              //oldway//addString("sys0");
              //oldway//addkey(PETSCII_ENTER);
        forcedcompletion=true; addkey(PETSCII_ENTER);
        command="new";      
      
      } else if (source.getText().equals("Exit")) {
        forcedcompletion=true; addkey(PETSCII_ENTER);
        command="exit";        

      } else if (source.getText().equals("List program")) {
        forcedcompletion=true; addkey(PETSCII_ENTER);
        command="list";       

      } else if (source.getText().equals("Run program")) {
        forcedcompletion=true; addkey(PETSCII_ENTER);
        command="run";       

      } else if (source.getText().equals("Break run")) {
        screen.has_controlC=true; // but chew it up
        forcedcompletion=false; addkey((char)C64Screen.BREAK_KEY); // no different to ENTER really
        command="";
        
      } else if (source.getText().equals("Save")) {
        System.out.print("Saving file...\n");
        forcedcompletion=true; addkey(PETSCII_ENTER);
        command="save";
        
      } else if (source.getText().equals("Reset machine")) {
        System.out.print("Resetting...\n");
        screen.has_controlC=true; // but chew it up
        forcedcompletion=true; addkey((char)C64Screen.BREAK_KEY); // no different to ENTER really
        //command="reset";
        command="new:meta-charset0:meta-rows25:meta-cols40:meta-scale1:meta-scaley1:print\"(clr)\"";        

      } else if (source.getText().equals("Open...")) {
        System.out.print("Opening file...\n");
        if (openFile()) {
          arg=fFile.getAbsolutePath();
          forcedcompletion=true; addkey(PETSCII_ENTER);
          //		  command="fileopen";
          command="load\""+arg+"\"";          
        }

      } else if (source.getText().equals("Open (and run)...")) {
        System.out.print("Opening file...\n");
        if (openFile()) {
          arg=fFile.getAbsolutePath();
          //addString("poprun");
          forcedcompletion=true; addkey(PETSCII_ENTER);
          //		    command="fileopenrun";
          command="load\""+arg+"\":run";          
        }
      } else if (source.getText().equals("Dumpstate now")) {
        System.out.print("dump state (reentrant?)\n");
                    screen.printstats();
        machine.dumpstate();
      } else if (source.getText().equals("META-DUMPSTATE")) {
        System.out.print("dump state (reentrant?)\n");
                    screen.printstats();
        machine.dumpstate();
      } else if (source.getText().equals("40 rows")) {
        System.out.print("Setting 40 rows...\n");
              screen.setRows(40);
      } else if (source.getText().equals("90 rows")) {
        System.out.print("Setting 90 rows...\n");
              screen.setRows(90);
      }
    }

    public void itemStateChanged(ItemEvent e) {
        JMenuItem source = (JMenuItem)(e.getSource());
        String s = "Item event detected."
                   + newline
                   + "    Event source: " + source.getText()
                   + " (an instance of " + getClassName(source) + ")"
                   + newline
                   + "    New state: "
                   + ((e.getStateChange() == ItemEvent.SELECTED) ?
                     "selected":"unselected");
        System.out.print(s + newline);
//Item event detected.
    //Event source: Transparent background (an instance of JCheckBoxMenuItem)
    //New state: selected
	if (source.getText().equals("Transparent background")) {
		System.out.print("Setting transparent state to ...\n");
		screen.bgtrans = (e.getStateChange() == ItemEvent.SELECTED) ? true:false;
        	screen.setBackgroundTransparent(screen.bgtrans);
	} else if (source.getText().equals("Drop shadow")) {
		System.out.print("Setting drop shadow ...\n");
                screen.bgshadow = (e.getStateChange() == ItemEvent.SELECTED) ? true:false;
        	//screen.setBackgroundTransparent(screen.bgtrans);
                screen.redrawScreen();
                screen.forcedrepaint();
	} else if (source.getText().equals("Hidden (faint) text")) {
		System.out.print("Setting hidden ...\n");
                screen.faint = (e.getStateChange() == ItemEvent.SELECTED) ? true:false;
        	//screen.setBackgroundTransparent(screen.bgtrans);
                screen.redrawScreen();
                screen.forcedrepaint();
	} else if (source.getText().equals("Uppercase")) {
		System.out.print("Setting charset ...\n");
                screen.changeCharSet( (e.getStateChange() == ItemEvent.SELECTED) ? 0:1);
	} else if (source.getText().equals("BASIC timing profiling")) {
		System.out.print("Setting BASIC profiling\n");
		machine.basictimer=( (e.getStateChange() == ItemEvent.SELECTED) ? true:false);
	} else if (source.getText().equals("Verbose")) {
                // not quiet right yet
		System.out.print("Setting verbosity not quite right yet...\n");
		machine.verbose=( (e.getStateChange() == ItemEvent.SELECTED) ? true:false);
		machine.evaluate_engine.verbose=( (e.getStateChange() == ItemEvent.SELECTED) ? true:false);
		screen.verbose=( (e.getStateChange() == ItemEvent.SELECTED) ? true:false);
	} else if (source.getText().equals("50% duty cycle")) {
                if (e.getStateChange() == ItemEvent.SELECTED) machine.partialDutyCycle=1;
                else machine.partialDutyCycle=0;
	} else if (source.getText().equals("Syntax highlighting")) {
                if (e.getStateChange() == ItemEvent.SELECTED) machine.hasSyntaxHighlighting=true;
                else machine.hasSyntaxHighlighting=false;
		System.out.print("syntaxhighlighting set to ");
		System.out.print(machine.hasSyntaxHighlighting?"true":"false");
		System.out.print("\n");
	} else if (source.getText().equals("Sync background image")) {
		System.out.print("Setting sendtoback ...\n");
                if (e.getStateChange() == ItemEvent.SELECTED) screen.shiftbgimage=true;
                else screen.shiftbgimage=false;
                screen.redrawScreen();
                screen.forcedrepaint();
	} else if (source.getText().equals("SendToBack")) {
		System.out.print("Setting sendtoback ...\n");
                if (e.getStateChange() == ItemEvent.SELECTED) {
                  screen.sendToBack=true;
                } else {
                  screen.sendToBack=false;
                }
        } else if (source.getText().equals("Frames")) {
		System.out.print("Setting frames ...\n");
                if (e.getStateChange() == ItemEvent.SELECTED) {
		//screen.setVisible(false);
		//screen.hide();
		//screen.setUndecorated(false);
		//screen.setVisible(true);
                        // decorated - framed
                        screen.dispose();
			screen.setUndecorated(false);
			screen.setResizable(true);
			//device.setFullScreenWindow(null); // comment this line if you want only undecorated frame
			screen.setVisible(true);
                } else {
                        // undecorated
                        screen.dispose();
			screen.setUndecorated(true);
			screen.setResizable(true);
			//device.setFullScreenWindow(screen); // comment this line if you want only undecorated frame
			screen.setVisible(true);
                }
	}


    }

    // Returns just the class name -- no package info.
    protected String getClassName(Object o) {
        String classString = o.getClass().getName();
        int dotIndex = classString.lastIndexOf(".");
        return classString.substring(dotIndex+1);
    }

    // Returns an ImageIcon, or null if the path was invalid. 
    protected static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = C64PopupMenu.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    public C64PopupMenu(Machine machine, C64Screen screen) {
	this.machine=machine;
        this.screen=screen;
	createPopupMenu();
    }

    class PopupListener extends MouseAdapter {
        JPopupMenu popup;

        PopupListener(JPopupMenu popupMenu) {
            popup = popupMenu;
        }

        public void mousePressed(MouseEvent e) {
	//System.out.print("mousepressed ");
            maybeShowPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
	//System.out.print("mousereleased ");
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
		// perhaps - read current settings here?
		cbMenuItem_bgtrans.setSelected(screen.bgtrans);

                popup.show(e.getComponent(),
                           e.getX(), e.getY());
            }
        }
    }

///
void addString(String str) {
  for (int i = 0; i < str.length(); ++i) {
      addkey(str.charAt(i));
  }
}

void addkey(char key)
{
          screen.keybuf[screen.keybuftop] = key;
          screen.keybuftop++; if (screen.keybuftop >= screen.keybufmax) { screen.keybuftop = 0; }
}

 File fFile = new File ("default.java");

boolean openFile () {

      JFileChooser fc = new JFileChooser ();
      fc.setDialogTitle ("Open File");

      // Choose only files, not directories
      fc.setFileSelectionMode ( JFileChooser.FILES_ONLY);

      // Start in current directory
      fc.setCurrentDirectory (new File ("."));

      // Set filter for Java source files.
      //fc.setFileFilter (fJavaFilter);

      // Now open chooser
      int result = fc.showOpenDialog ((JFrame)screen);

      if (result == JFileChooser.CANCEL_OPTION) {
          return false;
      } else if (result == JFileChooser.APPROVE_OPTION) {

          fFile = fc.getSelectedFile ();
          // Invoke the readFile method in this class
          //String file_string = readFile (fFile);

          //if (file_string != null)
              //fTextArea.setText (file_string);
          //else
              //return false;
         System.out.printf("File is %s\n",fFile);
      } else {
          return false;
      }
      return true;
   } // openFile

    public void aboutBox() {
        String myversion =
            "JEBI/C64/ijk64 version "
            + version.programVersion + "\n"  // was 3.0.48
            + "Paul Salanitri, Futex\n"
            + "Copyright (c) 2001-2007-2012-2016 P. Salanitri";
        String licence =
            "Redistribution and use in source and binary forms, with or\n"
            + "without modification, are permitted provided that the\n"
            + "following conditions are met:\n\n"
            + "1. Redistributions of source code must retain the above\n"
            + "   copyright notice, this list of conditions and the\n"
            + "   following disclaimer.\n"
            + "2. Redistributions in binary form must reproduce the above\n"
            + "   copyright notice, this list of conditions and the\n"
            + "   following disclaimer in the documentation and/or other\n"
            + "   materials provided with the distribution.\n"
            + "3. The name of the author may not be used to endorse or\n"
            + "   promote products derived from this software without\n"
            + "   specific prior written permission.\n\n"
            + "THIS SOFTWARE IS PROVIDED BY THE AUTHOR \"AS IS\" AND\n"
            + "ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT \n"
            + "LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY\n"
            + "AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.\n"
            + "IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT,\n"
            + "INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL\n"
            + "DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF\n"
            + "SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR\n"
            + "PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND\n"
            + "ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT\n"
            + "LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)\n"
            + "ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN\n"
            + "IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.";

        licence = "GPL";
        
        JTextArea licenceArea = new JTextArea(licence);
        licenceArea.setEditable(false);
        String javaVersion = System.getProperty("java.version");
        Object contents[] = new Object[] { myversion, licenceArea, 
        	"Java version " + javaVersion };
        //JOptionPane.showMessageDialog(parent, contents, "About GeomLab",
        JOptionPane.showMessageDialog( screen, contents, "About JEBI/C64/ijk64",
        	JOptionPane.INFORMATION_MESSAGE);
    }





}
//end///

