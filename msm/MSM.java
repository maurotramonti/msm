package msm;

import org.apache.commons.io.FileUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Scanner;


class SysConst {
    static final String system = System.getProperty("os.name");
    public static String getConfPath() {
        if (system.contains("Windows")) return System.getenv("LOCALAPPDATA") +  "\\msm\\conf\\";
        else return "/etc/msm/conf/";
    }

    public static String getServersPath() {
        if (system.contains("Windows")) return System.getenv("LOCALAPPDATA") + "\\msm\\servers\\";
        else return "/etc/msm/servers/";
    }
    public static String getImagesPath() {
        if (system.contains("Windows")) return System.getenv("LOCALAPPDATA") + "\\msm\\images\\";
        else return "/etc/msm/images/";
    }

    public static String getLangsPath() {
        if (system.contains("Windows")) return System.getenv("LOCALAPPDATA") + "\\msm\\langs\\";
        else return "/etc/msm/langs/";
    }
}

class MSMFrame extends JFrame implements WindowListener {
    public MSMFrame frame;
    
    public JTabbedPane tPane = new JTabbedPane();
    public ServerTab[] serverTabs = new ServerTab[32];

    private final JMenuItem[] menuItems = new JMenuItem[12];



    MSMFrame() {
        super("Minecraft Server Manager 1.2");
        frame = this;
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setIconImage(Toolkit.getDefaultToolkit().getImage(SysConst.getImagesPath() + "msm.png"));
        this.addWindowListener(this);


        loadLanguage();

        try {
            Scanner s = new Scanner(new File(SysConst.getConfPath() + "lastcheck.txt"));
            final long millis = Long.parseLong(s.nextLine());
            s.close();
            if ((System.currentTimeMillis() - millis) >= 86400000) { // è passato almeno un giorno
                checkUpdates(true);
            }
        } catch (IOException ex) {}

        String[] menuItemActs = LanguageManager.getMenuItemsInfo(LanguageManager.MI_ACTS);
        String[] menuItemLbls = LanguageManager.getMenuItemsInfo(LanguageManager.MI_LBLS);

        JMenuBar menuBar = new JMenuBar();

        JMenu server = new JMenu("Server"); JMenu preferences = new JMenu(LanguageManager.getTranslationsFromFile("Preferences")); JMenu help = new JMenu(LanguageManager.getTranslationsFromFile("Help"));
        menuBar.add(server); menuBar.add(preferences); menuBar.add(help);


        for (int i = 0; i < 6; i++) {
            menuItems[i] = new JMenuItem(menuItemLbls[i]);
            menuItems[i].setActionCommand(menuItemActs[i]);
            menuItems[i].addActionListener(new ServerMenuHandler());
            server.add(menuItems[i]);
        }

        for (int i = 6; i < 7; i++) {
            menuItems[i] = new JMenuItem(menuItemLbls[i]);
            menuItems[i].setActionCommand(menuItemActs[i]);
            menuItems[i].addActionListener(new PreferencesMenuHandler());
            preferences.add(menuItems[i]);
        }

        for (int i = 7; i < 12; i++) {
            menuItems[i] = new JMenuItem(menuItemLbls[i]);
            menuItems[i].setActionCommand(menuItemActs[i]);
            menuItems[i].addActionListener(new HelpMenuHandler());
            help.add(menuItems[i]);
        }

        loadServers();


        this.add(tPane);
        this.setMinimumSize(new Dimension(600, 350));

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
                    } catch (NullPointerException ex) {}
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
        File serversFolder = new File(SysConst.getServersPath());
    
        File[] servers = serversFolder.listFiles();
        if (servers != null) {

            int i = 0;
            for (File s : servers) {
                if (s.isDirectory()) {
                    serverTabs[i] = new ServerTab(new File(s.getAbsolutePath() + File.separator + "config.msm"), this);
                    tPane.addTab(serverTabs[i].title, new ImageIcon(new ImageIcon(serverTabs[i].iconPath).getImage().getScaledInstance(24, 24, Image.SCALE_DEFAULT)), serverTabs[i]);
                    i++;
                }
            }
        }
    } 

    public void checkUpdates(boolean showOnlyIfPositive) {
        final int internalVersion = 100;
        try {
            FileUtils.copyURLToFile(new URL("https://raw.githubusercontent.com/maurotramonti/msm/main/conf/latest.txt"), new File(SysConst.getConfPath() + "latest.txt"), 30000,30000);
            Scanner scanner = new Scanner(new File(SysConst.getConfPath() + "latest.txt"));
            final int readVersion = Integer.parseInt(scanner.nextLine());
            System.out.println(readVersion);
            if (readVersion > internalVersion) {
                JOptionPane.showMessageDialog(frame, LanguageManager.getTranslationsFromFile("UpdatesAvailable"), LanguageManager.getTranslationsFromFile("CheckUpdatesTitle"), JOptionPane.INFORMATION_MESSAGE);
            } else {
                if (!showOnlyIfPositive) JOptionPane.showMessageDialog(frame, LanguageManager.getTranslationsFromFile("NoUpdates"), LanguageManager.getTranslationsFromFile("CheckUpdatesTitle"), JOptionPane.INFORMATION_MESSAGE);
            }
            scanner.close();

            FileUtils.copyURLToFile(new URL("https://raw.githubusercontent.com/maurotramonti/msm/main/conf/paperlink.txt"), new File(SysConst.getConfPath() + "paperlink.txt"), 30000,30000);
            FileUtils.copyURLToFile(new URL("https://raw.githubusercontent.com/maurotramonti/msm/main/conf/vanillalink.txt"), new File(SysConst.getConfPath() + "vanillalink.txt"), 30000,30000);

            File lastcheckfile = new File(SysConst.getConfPath() + "lastcheck.txt");
            BufferedWriter bw = new BufferedWriter(new FileWriter(lastcheckfile));
            bw.write(Long.toString(System.currentTimeMillis()));
            bw.close();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, LanguageManager.getTranslationsFromFile("CheckUpdatesErr"), LanguageManager.getTranslationsFromFile("Warning"), JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
        
    }

    

    private void loadLanguage() {
        try {
            Scanner scanner = new Scanner(new File(SysConst.getConfPath() + "language.txt"));
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
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    if (tPane.getTabCount() == 0) {
                        menuItems[1].setEnabled(false);
                        menuItems[4].setEnabled(false);
                        menuItems[5].setEnabled(false);
                    } else {
                        if (((ServerTab) tPane.getSelectedComponent()).running) {
                            menuItems[1].setEnabled(false);
                            menuItems[4].setEnabled(false);
                        } else {
                            menuItems[1].setEnabled(true);
                            menuItems[4].setEnabled(true);
                        }
                        menuItems[5].setEnabled(true);
                    }

                    if (tPane.getTabCount() > 0) {
                        if (((ServerTab) tPane.getSelectedComponent()).imported && ((ServerTab) tPane.getSelectedComponent()).running == false) {
                            menuItems[3].setEnabled(true);
                        } else menuItems[3].setEnabled(false);
                    }
                    Thread.sleep(500);
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
