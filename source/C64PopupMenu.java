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

/* PopupMenuDemo.java requires images/middle.gif. */

/*
 * Like MenuDemo, but with popup menus added.
 */
public class C64PopupMenu implements ActionListener, ItemListener {

    String newline = "\n";
    JCheckBoxMenuItem cbMenuItem_bgtrans;

    public void createPopupMenu() {
        JMenuItem menuItem;
        JCheckBoxMenuItem cbMenuItem;
        JRadioButtonMenuItem rbMenuItem;
        JMenu submenu;

	// try this
	// this does fix the problem of the menu dissappearing when completely within the bounds of the window
	javax.swing.JPopupMenu.setDefaultLightWeightPopupEnabled(false);
	//

        //Create the popup menu.
        JPopupMenu popup = new JPopupMenu();
        menuItem = new JMenuItem("JEBI");
	menuItem.setEnabled(false);
        menuItem.addActionListener(this);
        popup.add(menuItem);
        menuItem = new JMenuItem("Help");
        menuItem.addActionListener(this);
        popup.add(menuItem);

        popup.addSeparator();

        ImageIcon icon = createImageIcon("run.png");
        menuItem = new JMenuItem("Run program",icon);
        menuItem.addActionListener(this);
        popup.add(menuItem);

        icon = createImageIcon("applet-critical-blank.png");
        menuItem = new JMenuItem("Reset machine",icon);
        menuItem.addActionListener(this);
        popup.add(menuItem);

        popup.addSeparator();

        cbMenuItem_bgtrans = new JCheckBoxMenuItem("Transparent background");
        cbMenuItem_bgtrans.setMnemonic(KeyEvent.VK_B);
        cbMenuItem_bgtrans.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_B, ActionEvent.ALT_MASK));
        cbMenuItem_bgtrans.addItemListener(this);
        popup.add(cbMenuItem_bgtrans);

        cbMenuItem = new JCheckBoxMenuItem("Hidden (faint) text");
        cbMenuItem.setMnemonic(KeyEvent.VK_H);
        cbMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_H, ActionEvent.ALT_MASK));
        cbMenuItem.addItemListener(this);
        popup.add(cbMenuItem);

        cbMenuItem = new JCheckBoxMenuItem("Drop shadow");
        cbMenuItem.setMnemonic(KeyEvent.VK_D);
        cbMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_D, ActionEvent.ALT_MASK));
        cbMenuItem.addItemListener(this);
        popup.add(cbMenuItem);

        cbMenuItem = new JCheckBoxMenuItem("Uppercase");
        cbMenuItem.addItemListener(this);
        popup.add(cbMenuItem);

        cbMenuItem = new JCheckBoxMenuItem("Paint slowdown");
        cbMenuItem.setMnemonic(KeyEvent.VK_S);
        cbMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_S, ActionEvent.ALT_MASK));
        cbMenuItem.addItemListener(this);
        popup.add(cbMenuItem);

        //-------------------------------------------------------------
        //a group of radio button menu items
        popup.addSeparator();
        ButtonGroup group = new ButtonGroup();

        rbMenuItem = new JRadioButtonMenuItem("40 columns");
        rbMenuItem.setSelected(true);
        rbMenuItem.setMnemonic(KeyEvent.VK_S);
        group.add(rbMenuItem);
        rbMenuItem.addActionListener(this);
        popup.add(rbMenuItem);

        rbMenuItem = new JRadioButtonMenuItem("80 columns");
        rbMenuItem.setMnemonic(KeyEvent.VK_O);
        group.add(rbMenuItem);
        rbMenuItem.addActionListener(this);
        popup.add(rbMenuItem);

        //-------------------------------------------------------------
        //a group of check box menu items
        popup.addSeparator();
        group = new ButtonGroup();

        rbMenuItem = new JRadioButtonMenuItem("25 rows");
        rbMenuItem.setSelected(true);
        rbMenuItem.setMnemonic(KeyEvent.VK_R);
        group.add(rbMenuItem);
        rbMenuItem.addActionListener(this);
        popup.add(rbMenuItem);

        rbMenuItem = new JRadioButtonMenuItem("40 rows");
        rbMenuItem.setMnemonic(KeyEvent.VK_O);
        group.add(rbMenuItem);
        rbMenuItem.addActionListener(this);
        popup.add(rbMenuItem);

        rbMenuItem = new JRadioButtonMenuItem("90 rows");
        rbMenuItem.setMnemonic(KeyEvent.VK_O);
        group.add(rbMenuItem);
        rbMenuItem.addActionListener(this);
        popup.add(rbMenuItem);

        //-------------------------------------------------------------
        //a group of check box menu items
        popup.addSeparator();
        group = new ButtonGroup();

        rbMenuItem = new JRadioButtonMenuItem("Single pixel ( * 1 )");
        rbMenuItem.setSelected(true);
        rbMenuItem.setMnemonic(KeyEvent.VK_R);
        group.add(rbMenuItem);
        rbMenuItem.addActionListener(this);
        popup.add(rbMenuItem);

        rbMenuItem = new JRadioButtonMenuItem("Double pixel ( * 2 )");
        rbMenuItem.setMnemonic(KeyEvent.VK_O);
        group.add(rbMenuItem);
        rbMenuItem.addActionListener(this);
        popup.add(rbMenuItem);

        rbMenuItem = new JRadioButtonMenuItem("Triple pixel ( * 3 )");
        rbMenuItem.setMnemonic(KeyEvent.VK_O);
        group.add(rbMenuItem);
        rbMenuItem.addActionListener(this);
        popup.add(rbMenuItem);

        rbMenuItem = new JRadioButtonMenuItem("Double height ( Y*2 )");
        rbMenuItem.setMnemonic(KeyEvent.VK_O);
        group.add(rbMenuItem);
        rbMenuItem.addActionListener(this);
        popup.add(rbMenuItem);

        //-------------------------------------------------------------
        //a group of check box menu items
        popup.addSeparator();
	//------------------------------------------------------------
        submenu = new JMenu("Show keyboard translations");
        popup.add(submenu);
        JMenu PCsubmenu = new JMenu("PC keyboard");
        submenu.add(PCsubmenu);

        JMenu PCsubmenu_plain= new JMenu("Plain set");
        PCsubmenu.add(PCsubmenu_plain);
        icon = createImageIcon("psd.jpg");
        menuItem = new JMenuItem("Plain set",icon);
        PCsubmenu_plain.add(menuItem);

        JMenu PCsubmenu_shift= new JMenu("Shifted set");
        PCsubmenu.add(PCsubmenu_shift);
        icon = createImageIcon("psd.jpg");
        menuItem = new JMenuItem("Shifted set",icon);
        PCsubmenu_shift.add(menuItem);

        JMenu PCsubmenu_comm= new JMenu("C= set");
        PCsubmenu.add(PCsubmenu_comm);
        icon = createImageIcon("psd.jpg");
        menuItem = new JMenuItem("C= set",icon);
        PCsubmenu_comm.add(menuItem);

        JMenu PCsubmenu_ctrl= new JMenu("CTRL= set");
        PCsubmenu.add(PCsubmenu_ctrl);
        icon = createImageIcon("psd.jpg");
        menuItem = new JMenuItem("CTRL set",icon);
        PCsubmenu_ctrl.add(menuItem);


	//------------------------------------------------------------
        submenu = new JMenu("Preset Modes");
        submenu.setMnemonic(KeyEvent.VK_S);
        menuItem.addActionListener(this);
        popup.add(submenu);
        menuItem = new JMenuItem("Commodore 128 80x25 monitor mode (double height pixels)");
        submenu.add(menuItem);
        menuItem = new JMenuItem("Commodore 64 default mode");
        submenu.add(menuItem);
        menuItem = new JMenuItem("Commodore 64 BIG screen");
        submenu.add(menuItem);

	//------------------------------------------------------------
        submenu = new JMenu("Show character sets");
        submenu.setMnemonic(KeyEvent.VK_S);
        menuItem.addActionListener(this);
        popup.add(submenu);
        menuItem = new JMenuItem("Uppercase set");
        submenu.add(menuItem);
        menuItem = new JMenuItem("Lowercase set");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_2, ActionEvent.ALT_MASK));
        menuItem.addActionListener(this);
        submenu.add(menuItem);

	//------------------------------------------------------------
        submenu = new JMenu("Show commands");
        submenu.setMnemonic(KeyEvent.VK_S);
        menuItem.addActionListener(this);
        popup.add(submenu);
        menuItem = new JMenuItem("META-CHARSET [0|1]"); submenu.add(menuItem);
        menuItem = new JMenuItem("META-SCALE [0|1|2]"); submenu.add(menuItem);
        menuItem = new JMenuItem("META-SCALEY [?]"); submenu.add(menuItem);

	//------------------------------------------------------------
        submenu = new JMenu("Command Line parameters");
        popup.add(submenu);
        menuItem = new JMenuItem("C64 basicfilename.basic ..."); submenu.add(menuItem);

	//------------------------------------------------------------
        popup.addSeparator();

        menuItem = new JMenuItem("About");
        menuItem.addActionListener(this);
        popup.add(menuItem);

        //Add listener to the text area so the popup menu can come up.
        MouseListener popupListener = new PopupListener(popup);
	// attach to screen
        ((JFrame)C64Screen.out).addMouseListener(popupListener);
    }

    public void actionPerformed(ActionEvent e) {
        JMenuItem source = (JMenuItem)(e.getSource());
        String s = "Action event detected."
                   + newline
                   + "    Event source: " + source.getText()
                   + " (an instance of " + getClassName(source) + ")";
        System.out.print(s + newline);
	if (source.getText().equals("80 columns")) {
		System.out.print("Setting to 80 columns...\n");
        	C64Screen.out.setCols(80);
	} else if (source.getText().equals("40 columns")) {
		System.out.print("Setting to 40 columns...\n");
        	C64Screen.out.setCols(40);
	} else if (source.getText().equals("Single pixel ( * 1 )")) {
		System.out.print("Setting single pixel...\n");
        	C64Screen.out.setScale(1);
	} else if (source.getText().equals("Double pixel ( * 2 )")) {
		System.out.print("Setting double pixel...\n");
        	C64Screen.out.setScale(2);
	} else if (source.getText().equals("Triple pixel ( * 3 )")) {
		System.out.print("Setting triple pixel...\n");
        	C64Screen.out.setScale(3);
	} else if (source.getText().equals("Double height ( Y*2 )")) {
		System.out.print("Setting double height pixel...\n");
        	C64Screen.out.setScaleY(2);
	} else if (source.getText().equals("25 rows")) {
		System.out.print("Setting 25 rows...\n");
        	C64Screen.out.setRows(25);
	} else if (source.getText().equals("40 rows")) {
		System.out.print("Setting 40 rows...\n");
        	C64Screen.out.setRows(40);
	} else if (source.getText().equals("90 rows")) {
		System.out.print("Setting 90 rows...\n");
        	C64Screen.out.setRows(90);
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
		C64Screen.out.bgtrans = (e.getStateChange() == ItemEvent.SELECTED) ? true:false;
        	C64Screen.out.setBackgroundTransparent(C64Screen.out.bgtrans);
	}


    }

    // Returns just the class name -- no package info.
    protected String getClassName(Object o) {
        String classString = o.getClass().getName();
        int dotIndex = classString.lastIndexOf(".");
        return classString.substring(dotIndex+1);
    }

    /** Returns an ImageIcon, or null if the path was invalid. */
    protected static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = C64PopupMenu.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    public C64PopupMenu() {
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
		cbMenuItem_bgtrans.setSelected(C64Screen.out.bgtrans);

                popup.show(e.getComponent(),
                           e.getX(), e.getY());
            }
        }
    }
}
