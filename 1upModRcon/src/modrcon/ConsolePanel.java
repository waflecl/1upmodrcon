package modrcon;

import java.awt.event.MouseEvent;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseListener;
import java.awt.datatransfer.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

/**
 * A JPanel that holds the console and console icons.
 *
 * TODO: set console to courier new
 *
 * @author Pyrite[1up]
 */
public class ConsolePanel extends JPanel implements ActionListener, MouseListener {

    /** A reference to the Main Window */
    private MainWindow parent;

    /** Holds the buttons below the console. */
    private JPanel buttonPanel;

    private ConsoleTextPane taConsole;
    private JScrollPane jsp;

    /** A reference to the PropertyManager. */
    private PropertyManager pm;

    private JLabel iconCopy;
    private JLabel iconClear;
    private JLabel iconFind;
    private JLabel iconSave;
    private JLabel lblAutoQueryStatus;
    private JCheckBox autoQueryCheck;

    /** The console's right click menu. */
    private JPopupMenu popup;

    /** Timer to control Auto-Query Status checkbox. */
    private final Timer autoQueryTimer;

    public ConsolePanel(MainWindow owner) {
        super();
        this.parent = owner;

        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createTitledBorder("Console"));

        taConsole = new ConsoleTextPane();
        taConsole.setFont(new Font("Monospaced", Font.PLAIN, 12));
        pm = new PropertyManager();
        taConsole.setBackground(Color.decode(pm.getConsoleBGColor()));
        taConsole.setForeground(Color.decode(pm.getConsoleFGColor()));
        taConsole.setEditable(false);
        jsp = new JScrollPane(taConsole);
        
        iconCopy = new JLabel();
        iconClear = new JLabel();
        iconFind = new JLabel();
        iconSave = new JLabel();
        lblAutoQueryStatus = new JLabel("Auto-Query Status");
        autoQueryCheck = new JCheckBox();
        iconCopy.setIcon(new ImageIcon(getClass().getResource("/modrcon/resources/copy.png")));
        iconClear.setIcon(new ImageIcon(getClass().getResource("/modrcon/resources/files_remove.png")));
        iconFind.setIcon(new ImageIcon(getClass().getResource("/modrcon/resources/find.png")));
        iconSave.setIcon(new ImageIcon(getClass().getResource("/modrcon/resources/save.png")));
        iconCopy.setToolTipText("Copy selected text.");
        iconClear.setToolTipText("Clear console contents.");
        iconFind.setToolTipText("Search for one or more words in the console.");
        iconSave.setToolTipText("Save console to log.");
        iconCopy.addMouseListener(this);
        iconClear.addMouseListener(this);
        iconFind.addMouseListener(this);
        iconSave.addMouseListener(this);

        // Auto-Query Status Timer and ActionListener
        autoQueryTimer = new Timer(1000, this);
        ActionListener autoQueryListener = new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                AbstractButton abstractButton = (AbstractButton)actionEvent.getSource();
                boolean selected = abstractButton.getModel().isSelected();
                if (selected) {
                    pm = new PropertyManager();
                    autoQueryTimer.setDelay(pm.getAutoQueryInterval() * 1000);
                    autoQueryTimer.setInitialDelay(1);
                    autoQueryTimer.start();
                }
                else {
                    autoQueryTimer.stop();
                }
            }
        };
        autoQueryCheck.addActionListener(autoQueryListener);

        popup = getConsolePopupMenu();
        this.taConsole.addMouseListener(this);

        buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(iconCopy);
        buttonPanel.add(Box.createHorizontalStrut(5));
        buttonPanel.add(iconClear);
        buttonPanel.add(Box.createHorizontalStrut(5));
        buttonPanel.add(iconFind);
        buttonPanel.add(Box.createHorizontalStrut(5));
        buttonPanel.add(iconSave);
        buttonPanel.add(Box.createGlue());
        buttonPanel.add(autoQueryCheck);
        buttonPanel.add(lblAutoQueryStatus);

        this.add(jsp, BorderLayout.CENTER);
        this.add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPopupMenu getConsolePopupMenu() {
        JPopupMenu p = new JPopupMenu();
        JMenuItem selectAll = new JMenuItem(new MenuAction("Select All", this.parent));
        JMenuItem copySelected = new JMenuItem(new MenuAction("Copy Selected", this.parent));
        JMenuItem saveSelected = new JMenuItem(new MenuAction("Save Selected to File", this.parent));
        JMenuItem saveConsole = new JMenuItem(new MenuAction("Save Console As...", this.parent));
        JMenuItem clearConsole = new JMenuItem(new MenuAction("Clear Console", this.parent));
        JMenuItem loadHistory = new JMenuItem(new MenuAction("Load History", this.parent));
        JMenuItem serverInfo = new JMenuItem(new MenuAction("Server Info", this.parent));
        JMenuItem sendCP = new JMenuItem(new MenuAction("Send Connectionless Packet", this.parent));
        p.add(selectAll);
        p.add(copySelected);
        p.add(saveSelected);
        p.add(saveConsole);
        p.add(clearConsole);
        p.add(loadHistory);
        p.addSeparator();
        p.add(serverInfo);
        p.addSeparator();
        p.add(sendCP);
        return p;
    }

    public void logHistory(String output) {
        if (pm.getRememberConsoleHistory()) {
            HistoryDatabase hdb = new HistoryDatabase();
            hdb.addHistory(output);
            hdb.saveDatabase();
        }
    }

    public void selectAllText() {
        this.taConsole.requestFocusInWindow();
        this.taConsole.selectAll();
    }

    public void setConsoleBackground(Color c) {
        this.taConsole.setBackground(c);
    }

    public void setConsoleForeground(Color c) {
        this.taConsole.setForeground(c);
    }

    public String getSelectedText() {
        return this.taConsole.getSelectedText();
    }

    public String getConsoleText() {
        return this.taConsole.getText();
    }

    public void scrollToTop() {
        this.taConsole.setCaretPosition(0);
    }

    public void findText() {
        new SearchConsoleDialog(this.parent, this.taConsole);
    }

    public void clearConsole() {
        this.taConsole.setText("");
    }

    public void loadConsoleHistory() {
        this.clearConsole();
        HistoryDatabase hdb = new HistoryDatabase();
        this.taConsole.append(hdb.getConsoleHistory(), "default");
    }

    public void appendToConsole(String text) {
        this.logHistory(text);
        this.taConsole.append(text, "default");
    }

    public void appendCommand(String command) {
        java.util.Date now = new java.util.Date();
        java.text.DateFormat dateFormat = new java.text.SimpleDateFormat("M/d/yyyy hh:mm:ss aa");
        String dateString = dateFormat.format(now);
        this.logHistory("Command: "+command+" - "+dateString+"\n");
        this.taConsole.appendCommand(command);
    }

    public void appendToConsole(String text, String styleName) {
        this.logHistory(text);
        this.taConsole.append(text, styleName);
    }

    public void appendWithColor(String playerName) {
        this.logHistory(playerName);
        this.taConsole.appendWithColors(playerName);
    }
    
    public void saveConsole(String contents) {
        JFileChooser file = new JFileChooser();
        int choice = file.showSaveDialog(parent);
        if (choice == 0) {
            String path = file.getSelectedFile().getAbsolutePath();
            try {
                FileWriter outFile = new FileWriter(path);
                PrintWriter out = new PrintWriter(outFile);
                out.print(contents);
                this.appendToConsole("\nConsole log saved to: "+path+"\n");
                out.close();
                JOptionPane.showMessageDialog(this.parent, "File Saved.");
            }
            catch (IOException e) {
                JOptionPane.showMessageDialog(this.parent, e.getMessage());
            }
        }
    }

    public void saveConsole() {
        this.saveConsole(getConsoleText());
    }

    public void saveSelected() {
        this.saveConsole(getSelectedText());
    }

    public void mouseClicked(MouseEvent e) {
        if (e.getSource() == this.iconCopy) {
            StringSelection data = new StringSelection(this.getSelectedText());
            Clipboard clipboard = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(data, data);
        }
        else if (e.getSource() == this.iconClear) {
            this.clearConsole();
        }
        else if (e.getSource() == this.iconFind) {
            this.findText();
        }
        else if (e.getSource() == this.iconSave) {
            this.saveConsole();
        }
        else if (e.getSource() == this.taConsole) {
            if (e.getButton() == MouseEvent.BUTTON3 && e.getClickCount() == 1) {
                this.popup.show(this.taConsole, e.getX(), e.getY());
            }
        }
    }

    public void mousePressed(MouseEvent e) {
        if (e.getSource() == this.taConsole) {
            setCursor(new Cursor(Cursor.TEXT_CURSOR));
        } else {
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (e.getSource() == this.taConsole) {
            setCursor(new Cursor(Cursor.TEXT_CURSOR));
        } else {
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }

    public void mouseEntered(MouseEvent e) {
        if (e.getSource() == this.taConsole) {
            setCursor(new Cursor(Cursor.TEXT_CURSOR));
        } else {
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
    }

    public void mouseExited(MouseEvent e) {
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.autoQueryTimer) {
            // Send Status Command to Console
            this.parent.controlPanel.sendStatusCommand();
        }
    }

}
