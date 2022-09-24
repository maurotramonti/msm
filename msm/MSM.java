package msm;

import javax.swing.*;
import java.awt.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.io.*;
import java.util.Scanner;
import java.net.*;

class SysConst {
    static final String system = System.getProperty("os.name");
    public static String getPrePath() {
        if (system.contains("Windows")) return System.getenv("LOCALAPPDATA") +  "\\msm\\";
        else return "/etc/msm/";
    }
    public static String getLogoPath() {
        if (system.contains("Windows")) return System.getenv("PROGRAMFILES") + "\\msm\\msm.png";
        else return "/usr/share/icons/msm.png";
    }
    public static String getJavaLogoPath() {
        if (system.contains("Windows")) return System.getenv("PROGRAMFILES") + "\\msm\\javalogo.png";
        else return "/usr/share/icons/javalogo.png";
    }
}

class HelpMethods {
    public static void downloadFile(String fileURL, String outFile) throws IOException {
        URL url = new URL(fileURL);
        InputStream is = url.openStream();
        // Stream to the destionation file
        FileOutputStream fos = new FileOutputStream(outFile);
		// Read bytes from URL to the local file
        byte[] buffer = new byte[4096];
        int bytesRead = 0;

        while ((bytesRead = is.read(buffer)) != -1) {
        	fos.write(buffer, 0, bytesRead);
        }

        // Close destination stream
        fos.close();
        // Close URL stream
        is.close();
    }
    public static void deleteDirectory(File file) {
        // store all the paths of files and folders present
        // inside directory
        try {
            for (File subfile : file.listFiles()) {
  
                // if it is a subfolder,e.g Rohan and Ritik,
                // recursiley call function to empty subfolder
                if (subfile.isDirectory()) {
                    deleteDirectory(subfile);
                }
  
                // delete files and empty subfolders
                subfile.delete();
            }
            file.delete();
        } catch (NullPointerException ex) {
            return;
        }
    }
}


class MSMFrame extends JFrame implements WindowListener {
    public MSMFrame frame;
    
    public JTabbedPane tPane = new JTabbedPane();
    public ServerTab[] serverTabs = new ServerTab[32];

    private JMenu server, preferences, help; 
    private JMenuItem[] menuItems = new JMenuItem[11];



    MSMFrame() {
        super("Minecraft Server Manager 1.0");
        frame = this;
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setResizable(false);
        this.setIconImage(Toolkit.getDefaultToolkit().getImage(SysConst.getLogoPath()));
        this.addWindowListener(this);

        loadLanguage();

        try {
            Scanner s = new Scanner(new File(SysConst.getPrePath() + "conf" + File.separator + "lastcheck.txt"));
            final long millis = Long.parseLong(s.nextLine());
            s.close();
            if ((System.currentTimeMillis() - millis) >= 86400000) { // è passato almeno un giorno
                checkUpdates(true);
            }
        } catch (IOException ex) {}

        String[] menuItemActs = LanguageManager.getMenuItemsInfo(LanguageManager.MI_ACTS);
        String[] menuItemLbls = LanguageManager.getMenuItemsInfo(LanguageManager.MI_LBLS);

        JMenuBar menuBar = new JMenuBar();

        server = new JMenu("Server"); preferences = new JMenu(LanguageManager.getTranslationsFromFile("Preferences")); help = new JMenu(LanguageManager.getTranslationsFromFile("Help"));
        menuBar.add(server); menuBar.add(preferences); menuBar.add(help);


        for (int i = 0; i < 5; i++) {
            menuItems[i] = new JMenuItem(menuItemLbls[i]);
            menuItems[i].setActionCommand(menuItemActs[i]);
            menuItems[i].addActionListener(new ServerMenuHandler());
            server.add(menuItems[i]);
        }

        for (int i = 5; i < 6; i++) {
            menuItems[i] = new JMenuItem(menuItemLbls[i]);
            menuItems[i].setActionCommand(menuItemActs[i]);
            menuItems[i].addActionListener(new PreferencesMenuHandler());
            preferences.add(menuItems[i]);
        }

        for (int i = 6; i < 10; i++) {
            menuItems[i] = new JMenuItem(menuItemLbls[i]);
            menuItems[i].setActionCommand(menuItemActs[i]);
            menuItems[i].addActionListener(new HelpMenuHandler());
            help.add(menuItems[i]);
        }

        loadServers();


        this.add(tPane);
        if (tPane.getTabCount() > 0) this.pack();
            
        this.setJMenuBar(menuBar);
        new MenuItemsController().start();
        
        this.setVisible(true);
    }

     // Custom window listener

    @Override
    public void windowClosing(WindowEvent e) {
        boolean somethingRunning = false;
        for (ServerTab st : serverTabs) {
            try {
                if (st.running) somethingRunning = true;
            } catch (NullPointerException ex) {
                break;
            }
        }
        if (somethingRunning) {
            int r = JOptionPane.showConfirmDialog(frame, LanguageManager.getTranslationsFromFile("StopServersConfirm"), LanguageManager.getTranslationsFromFile("Warning"), JOptionPane.YES_NO_OPTION);
            if (r == JOptionPane.YES_OPTION) {
                for (ServerTab st : serverTabs) {
                    try {
                        st.serverProcess.destroy(); 
                    } catch (NullPointerException ex) {
                        continue;
                    }
                }
            
            }
            else if (r == JOptionPane.NO_OPTION) return;
        }

        System.exit(0);
        
    }

    @Override
    public void windowDeactivated(WindowEvent e) {}

    @Override
    public void windowActivated(WindowEvent e) {}

    @Override
    public void windowDeiconified(WindowEvent e) {}

    @Override
    public void windowIconified(WindowEvent e) {}

    @Override
    public void windowClosed(WindowEvent e) {}

    @Override
    public void windowOpened(WindowEvent e) {}


    public void loadServers() {
        File serversFolder = new File(SysConst.getPrePath() + "servers");
    
        File[] servers = serversFolder.listFiles();
        int i = 0;
        
        try {
            for (File s : servers) {
                if (s.isDirectory()) {
                    serverTabs[i] = new ServerTab(new File(s.getAbsolutePath() + File.separator + "config.msm"), this);
                    tPane.addTab(serverTabs[i].title, new ImageIcon(new ImageIcon(serverTabs[i].iconPath).getImage().getScaledInstance(24, 24, Image.SCALE_DEFAULT)), serverTabs[i]);
                    i++;                    
                }
            } 
        } catch (NullPointerException ex) {}
        if (tPane.getTabCount() == 0) this.setSize(400, 400);
    } 

    public void checkUpdates(boolean showOnlyIfPositive) {
        final int internalVersion = 100;
        try {
            HelpMethods.downloadFile("https://raw.githubusercontent.com/maurotramonti/msm/main/latest.txt", SysConst.getPrePath() + "conf" + File.separator + "latest.txt");
            Scanner scanner = new Scanner(new File(SysConst.getPrePath() + "conf" + File.separator + "latest.txt"));
            final int readVersion = Integer.parseInt(scanner.nextLine());
            System.out.println(readVersion);
            if (readVersion > internalVersion) {
                JOptionPane.showMessageDialog(frame, LanguageManager.getTranslationsFromFile("UpdatesAvailable"), LanguageManager.getTranslationsFromFile("CheckUpdatesTitle"), JOptionPane.INFORMATION_MESSAGE);
            } else {
                if (showOnlyIfPositive == false) JOptionPane.showMessageDialog(frame, LanguageManager.getTranslationsFromFile("NoUpdates"), LanguageManager.getTranslationsFromFile("CheckUpdatesTitle"), JOptionPane.INFORMATION_MESSAGE);                    
            }
            scanner.close();

            HelpMethods.downloadFile("https://raw.githubusercontent.com/maurotramonti/msm/main/paperlink.txt", SysConst.getPrePath() + "conf" + File.separator + "paperlink.txt");
            HelpMethods.downloadFile("https://raw.githubusercontent.com/maurotramonti/msm/main/vanillalink.txt", SysConst.getPrePath() + "conf" + File.separator + "vanillalink.txt");

            File lastcheckfile = new File(SysConst.getPrePath() + "conf" + File.separator + "lastcheck.txt");
            BufferedWriter bw = new BufferedWriter(new FileWriter(lastcheckfile));
            bw.write(Long.toString(System.currentTimeMillis()));
            bw.close();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, LanguageManager.getTranslationsFromFile("CheckUpdatesErr"), LanguageManager.getTranslationsFromFile("Warning"), JOptionPane.ERROR_MESSAGE);
            return;
        }       
        
    }

    

    private void loadLanguage() {
        try {
            Scanner scanner = new Scanner(new File(SysConst.getPrePath() + "conf" + File.separator + "language.txt"));
            String s = scanner.nextLine();
            if (s.equals("none")) {
                scanner.close();
                LanguageManager.setLang(LanguageManager.ENGLISH);
                new PreferencesMenuHandler().actionPerformed(null);
                loadLanguage();
            }
            if (s.equals("English")) LanguageManager.setLang(LanguageManager.ENGLISH);
            else if (s.equals("Italiano")) LanguageManager.setLang(LanguageManager.ITALIAN);
            scanner.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    class MenuItemsController extends Thread {
        @Override
        public void run() {
            while (Thread.currentThread().isInterrupted() == false) {
                try {
                    if (tPane.getTabCount() == 0) {
                        menuItems[1].setEnabled(false);
                        menuItems[4].setEnabled(false);
                    } else if (((ServerTab) tPane.getSelectedComponent()).running)  {
                        menuItems[1].setEnabled(false);
                        menuItems[4].setEnabled(false);
                    } else {
                        menuItems[1].setEnabled(true);
                        menuItems[4].setEnabled(true);
                    }

                    if (tPane.getTabCount() > 0) {
                        if (((ServerTab) tPane.getSelectedComponent()).imported && ((ServerTab) tPane.getSelectedComponent()).running == false) {
                            menuItems[3].setEnabled(true);
                        } else menuItems[3].setEnabled(false);
                    }
                    this.sleep(500);
                } catch (InterruptedException ex) {
                    break;
                }
            }
        }
    }

}



public class MSM {
    static MSMFrame msmf;
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}
        msmf = new MSMFrame();
    }

    public static MSMFrame getMSMFrame() {
        return msmf;
    }
}