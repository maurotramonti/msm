package msm;

import org.apache.commons.io.FileUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Scanner;


class ServerTab extends JPanel {
    public String title, folder, iconPath, software, serverVersion;
    public boolean imported, running = false;
    public Process serverProcess;

    public File confFile;

    private final JLabel titleLabel, pathLabel, ramLabel, javaExeLabel;



    private final JTextArea outputArea;
    private final JScrollPane outputPane;

    private final JTextField commandInput = new JTextField();
    private final ServerTab.SendCommandButton sendCommandButton = new ServerTab.SendCommandButton();

    private final ServerTab.PluginsButton pluginsButton = new ServerTab.PluginsButton();

    private ServerTab.ServerThread serverThread;


    private OutputStream serverProcessInput;

    private final ServerTab.LaunchButton launchButton = new ServerTab.LaunchButton();
    private final ServerTab.OptionButton optionButton = new ServerTab.OptionButton();

    private final ServerTab.UpdateButton updateButton = new ServerTab.UpdateButton();

    
    private final MSMFrame parentFrame;
    ServerTab(File conffile, MSMFrame f) {
        super(new BorderLayout(0, 10));

        final String[] confdata;

        parentFrame = f;
        confFile = conffile;

        this.setBackground((Color.white));

        GridBagConstraints gbc = new GridBagConstraints();

        JPanel contents = new JPanel(new GridBagLayout()); contents.setBackground((Color.white));
        JPanel buttons = new JPanel(); buttons.setBackground(Color.white);

        
        confdata = new String[9];

        outputArea = new JTextArea(); outputArea.setRows(6); outputArea.setBackground(Color.white);

        try {
            Scanner scanner = new Scanner(conffile);
            for (int i = 0; i < 8; i++) {
                confdata[i] = scanner.nextLine();
            }
            scanner.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        title = confdata[0];
        iconPath = confdata[1];
        folder = confdata[2];
        serverVersion = confdata[3];
        software = confdata[6];

        imported = confdata[7].equals("imported");


        gbc.gridy = 0; gbc.gridx = 0; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER; gbc.insets = new Insets(10, 10, 10, 10);
        titleLabel = new JLabel(confdata[0], new ImageIcon(new ImageIcon(confdata[1]).getImage().getScaledInstance(48, 48,  Image.SCALE_DEFAULT)), SwingConstants.LEFT); titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        contents.add(titleLabel, gbc);



        // Server information

        gbc.gridy = 1; gbc.anchor = GridBagConstraints.LINE_START; gbc.gridwidth = 1;
        contents.add(new JLabel(LanguageManager.getTranslationsFromFile("ServerPath")), gbc);

        gbc.gridy = 2; contents.add(new JLabel(LanguageManager.getTranslationsFromFile("MinecraftVersion")), gbc);

        gbc.gridy = 3; contents.add(new JLabel(LanguageManager.getTranslationsFromFile("JavaPath")), gbc);

        gbc.gridy = 4; contents.add(new JLabel(LanguageManager.getTranslationsFromFile("RamQuantity")), gbc);

        pathLabel = new JLabel(confdata[2]);

        gbc.gridy = 1; gbc.gridx = 1; contents.add(pathLabel, gbc);

        gbc.gridy = 2; contents.add(new JLabel(confdata[3] + " " + confdata[6]), gbc);

        javaExeLabel = new JLabel(confdata[4]);
        gbc.gridy = 3; contents.add(javaExeLabel, gbc);

        ramLabel = new JLabel(confdata[5] + " MB");
        gbc.gridy = 4; contents.add(ramLabel, gbc);

        gbc.gridy = 7; gbc.gridx = 0; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.HORIZONTAL;

        outputArea.setEditable(false); 

        outputPane = new JScrollPane(outputArea); outputArea.setLineWrap(true); 
        outputPane.setVisible(false); outputPane.setOpaque(true);
        
        outputPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        outputPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        outputPane.getVerticalScrollBar().setUnitIncrement(16);

        contents.add(outputPane, gbc); 

        commandInput.setVisible(false); sendCommandButton.setVisible(false); commandInput.setToolTipText(LanguageManager.getTranslationsFromFile("CommandToolTip"));

        gbc.gridy = 6; gbc.gridx = 0; gbc.gridwidth = 2; contents.add(commandInput, gbc);
        gbc.gridx = 2; gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.LINE_END;  contents.add(sendCommandButton, gbc);

        if (!new File(confFile.getParent() + File.separator + "server.properties").exists()) {
            optionButton.setEnabled(false);
            pluginsButton.setEnabled(false);
        }

        if (software.equals("Vanilla")) pluginsButton.setEnabled(false);


        buttons.add(pluginsButton); buttons.add(optionButton); buttons.add(launchButton); buttons.add(new ServerTab.OpenServerFolder()); buttons.add(updateButton);

        add(contents, BorderLayout.NORTH);
        add(buttons, BorderLayout.SOUTH);

    }

    public void reload() {
        String[] confdata = new String[8];
        try {
            Scanner scanner = new Scanner(confFile);
            for (int i = 0; i < 8; i++) {
                confdata[i] = scanner.nextLine();
            }
            scanner.close();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        javaExeLabel.setText(confdata[4]); ramLabel.setText(confdata[5] + " MB");
        titleLabel.setText(confdata[0]);
        titleLabel.setIcon(new ImageIcon(new ImageIcon(confdata[1]).getImage().getScaledInstance(48, 48,  Image.SCALE_DEFAULT)));
        ramLabel.setText(confdata[5] + " MB");
        pathLabel.setText(confdata[2]); folder = confdata[2];
        parentFrame.tPane.setIconAt(parentFrame.tPane.getSelectedIndex(), new ImageIcon(new ImageIcon(confdata[1]).getImage().getScaledInstance(24, 24, Image.SCALE_DEFAULT)));
        parentFrame.pack();
    }



    class LaunchButton extends JButton implements ActionListener {
        LaunchButton() {
            super(LanguageManager.getTranslationsFromFile("Launch"));
            addActionListener(this);
            setOpaque(false);
        }

        @Override 
        public void actionPerformed(ActionEvent e) {
            ProcessBuilder pb = new ProcessBuilder(javaExeLabel.getText(), "-Xmx" + ramLabel.getText().replaceAll(" MB", "M"), "-Xms" + ramLabel.getText().replaceAll(" MB", "M"), "-jar", folder + File.separator + "server.jar", "nogui");
            pb.directory(new File(folder));
            pb.redirectErrorStream(true);
            pb.redirectOutput(new File(folder + File.separator + "log.txt"));
            serverThread = new ServerTab.ServerThread(pb);
            serverThread.start();

            PrintOutputThread pot = new PrintOutputThread();
            pot.start();            
        }


    }

    class ServerThread extends Thread {
        ProcessBuilder pb;

        ServerThread(ProcessBuilder pb) {
            this.pb = pb;
        }
        public void run() {
            try {
                running = true;
                serverProcess = pb.start();
                serverProcessInput = serverProcess.getOutputStream();
                launchButton.setEnabled(false);
                optionButton.setEnabled(false);
                updateButton.setEnabled(false);
                pluginsButton.setEnabled(false);
                sendCommandButton.setVisible(true); commandInput.setVisible(true);
                serverProcess.waitFor();       
                    

            } catch (InterruptedException ex) {
                serverProcess.destroy();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(parentFrame, LanguageManager.getTranslationsFromFile("StartServerError"), LanguageManager.getTranslationsFromFile("Error"), JOptionPane.ERROR_MESSAGE);
            } finally {
                running = false;
                launchButton.setEnabled(true);
                optionButton.setEnabled(true);
                updateButton.setEnabled(true);
                pluginsButton.setEnabled(true);
                sendCommandButton.setVisible(false); commandInput.setVisible(false);

            }
            
        }
    }

    class PrintOutputThread extends Thread {

        @Override
        public void run() {
            outputPane.setVisible(true);
            
            while (serverThread.isAlive() && !Thread.currentThread().isInterrupted()) {
                try {
                    Scanner scanner = new Scanner(new File(folder + File.separator + "log.txt"));
                    String s = "";
                    while (scanner.hasNextLine()) s = s + scanner.nextLine() + '\n';
                    outputArea.setText(s); outputArea.setRows(6);                                  
                    scanner.close();
                    parentFrame.pack();
                    Thread.sleep(500);
                } catch (IOException ex) {
                    ex.printStackTrace();
                } catch (InterruptedException ex) {
                    break;
                }
            }
            outputArea.setText(""); outputPane.setVisible(false); parentFrame.pack();
            
        }
    }

    class OpenServerFolder extends JButton implements ActionListener {
        OpenServerFolder() {
            super(LanguageManager.getTranslationsFromFile("OpenServerFolder"));
            setOpaque(false);
            addActionListener(this);
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                Desktop.getDesktop().open(new File(folder));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(parentFrame, LanguageManager.getTranslationsFromFile("UnexpectedError"), LanguageManager.getTranslationsFromFile("Error"), JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    class SendCommandButton extends JButton implements ActionListener {
        SendCommandButton() {
            super(LanguageManager.getTranslationsFromFile("Send"));
            setOpaque(false);
            setSize(30, 20);
            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            byte[] command = (commandInput.getText() + '\n').getBytes();
            try {
                serverProcessInput.write(command);
                serverProcessInput.flush(); 
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(parentFrame, LanguageManager.getTranslationsFromFile("SendCommandError"), LanguageManager.getTranslationsFromFile("Error"), JOptionPane.ERROR_MESSAGE);
            } finally {
                commandInput.setText("");
            }
        }
    }

    class OptionButton extends JButton implements ActionListener {
        OptionButton() {
            super(LanguageManager.getTranslationsFromFile("GameOptions"));
            setOpaque(false);
            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            new GameOptionsDialog(parentFrame);
        }
    }

    class UpdatingServerDialog extends JDialog {
        UpdatingServerDialog(JFrame parent) {
            super(parent, LanguageManager.getTranslationsFromFile("Wait"), true);
            this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            this.setLayout(new GridBagLayout());
            this.setBackground(Color.white);
            this.setResizable(false);

            GridBagConstraints gbc = new GridBagConstraints();

            gbc.gridx = 0; gbc.gridy = 0; gbc.insets = new Insets(20, 20, 20, 20);

            this.add(new JLabel(LanguageManager.getTranslationsFromFile("DownloadingServer")), gbc);

            gbc.gridy = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.anchor = GridBagConstraints.CENTER;
            JProgressBar pbar = new JProgressBar(); pbar.setIndeterminate(true); pbar.setBackground(Color.white);


            this.add(pbar, gbc);

            this.pack();


        }
    }

    class UpdateButton extends JButton implements ActionListener {
        UpdateButton() {
            super(LanguageManager.getTranslationsFromFile("UpdateServer"));
            addActionListener(this); setBackground(Color.white);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            ServerTab.UpdatingServerDialog usd = new ServerTab.UpdatingServerDialog(parentFrame);
            new ServerTab.DownloadUpdatedServer(usd).start();
            usd.setVisible(true);
        }
    }

    class DownloadUpdatedServer extends Thread {
        JDialog updatingDialog;
        DownloadUpdatedServer(JDialog usd) {
            this.updatingDialog = usd;
        }
        @Override
        public void run() {
            try {
                File softwareLink;
                String urlString = null;
                if (software.equals("Vanilla")) {
                    softwareLink = new File(SysConst.getConfPath() + "vanillalink.txt");
                    urlString = new Scanner(softwareLink).nextLine();
                }
                else if (software.equals("PaperMC")) {
                    softwareLink = new File(SysConst.getConfPath() + "paperlink.txt");
                    String targetVersion = serverVersion.replaceAll("\\.", "") + ":";
                    Scanner scanner = new Scanner(softwareLink);
                    String s;
                    while (scanner.hasNextLine()) {
                        s = scanner.nextLine();
                        if (s.contains(targetVersion)) {
                            urlString = s.replaceAll(targetVersion, "");
                            break;
                        }
                    }
                    scanner.close();
                }
                FileUtils.copyURLToFile(new URL(urlString), new File(folder + File.separator + "server.jar"), 30000, 30000);
                JOptionPane.showMessageDialog(updatingDialog, LanguageManager.getTranslationsFromFile("DownloadSuccessful"), LanguageManager.getTranslationsFromFile("Info"), JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(updatingDialog, LanguageManager.getTranslationsFromFile("CheckUpdatesErr"), LanguageManager.getTranslationsFromFile("Error"), JOptionPane.ERROR_MESSAGE);
            } finally {
                updatingDialog.dispose();
            }
        }
    }

    class PluginsButton extends JButton implements ActionListener {
        PluginsButton() {
            super("Plugins");
            addActionListener(this); setBackground(Color.white);
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            new PluginsDialog(parentFrame);
        }
    }

    class PluginsDialog extends JDialog implements WindowListener {
        private final DeletePluginButton dpb = new DeletePluginButton();

        private final JDialog dialog;

        private final ButtonsCheckThread bct = new ButtonsCheckThread();
        private final JList<String> pluginsList;

        private String[] pluginsNameList = new String[64];

        PluginsDialog(JFrame parent) {
            super(parent, "Plugins", true);
            this.dialog = this;
            setLayout(new GridBagLayout()); setResizable(false); setBackground(Color.white); setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            getContentPane().setBackground(Color.white);



            GridBagConstraints gbc = new GridBagConstraints();

            gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1; gbc.gridheight = 3; gbc.ipadx = 120; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.anchor = GridBagConstraints.PAGE_START; gbc.insets = new Insets(10, 10, 10, 10);

            pluginsList = new JList<String>(pluginsNameList); pluginsList.setBackground(Color.white);
            loadPlugins(); add(pluginsList, gbc);
            gbc.ipadx = 0; gbc.ipady = 0; gbc.gridx = 1; gbc.gridheight = 1; add(new AddPluginButton(), gbc);
            gbc.gridy = 1; add(dpb, gbc);
            gbc.gridy = 2; add(new OpenPluginsFolder(), gbc);


            bct.start();

            pack();
            setVisible(true);
        }

        @Override
        public void windowClosing(WindowEvent w) {
            bct.interrupt();
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

        public void loadPlugins() {
            try {
                File pluginsDir = new File(folder + File.separator + "plugins");
                File[] filesList = pluginsDir.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.endsWith(".jar");
                    }
                });

                int i = 0; pluginsNameList = new String[64];
                for (File f : filesList) {
                    pluginsNameList[i] = f.getName().replaceAll(".jar", "");
                    i++;
                }
            } catch (NullPointerException ex) {
                ex.printStackTrace();
            }
            pluginsList.setListData(pluginsNameList);
            dialog.pack();
        }

        class DeletePluginButton extends JButton implements ActionListener {
            DeletePluginButton() {
                super(LanguageManager.getTranslationsFromFile("DeletePlugin"));
                addActionListener(this); setBackground(Color.white);
            }
            @Override
            public void actionPerformed(ActionEvent e) {
                int r = JOptionPane.showConfirmDialog(dialog, LanguageManager.getTranslationsFromFile("DeletePluginConfirm"), LanguageManager.getTranslationsFromFile("Confirm"), JOptionPane.YES_NO_OPTION);
                if (r == JOptionPane.NO_OPTION) return;
                try {
                    String pluginSelectedName = pluginsNameList[pluginsList.getSelectedIndex()];
                    if(!new File(folder + File.separator + "plugins" + File.separator + pluginSelectedName + ".jar").delete()) {
                        JOptionPane.showMessageDialog(dialog, LanguageManager.getTranslationsFromFile("DeletePluginError"), LanguageManager.getTranslationsFromFile("Error"), JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    File[] directories = new File(folder + File.separator + "plugins").listFiles();
                    for (File f : directories) {
                        if (f.isDirectory() && f.getName().contains(pluginSelectedName)) FileUtils.deleteDirectory(f);
                    }
                } catch (IOException ex) {}
                loadPlugins();
            }
        }

        class AddPluginButton extends JButton implements ActionListener {

            AddPluginButton() {
                super(LanguageManager.getTranslationsFromFile("AddPlugin"));
                addActionListener(this); setBackground(Color.white);
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY); chooser.setAcceptAllFileFilterUsed(false);
                FileNameExtensionFilter filter = new FileNameExtensionFilter("JAR file", "jar");
                chooser.setFileFilter(filter);
                int r = chooser.showOpenDialog(dialog);
                if (r == JFileChooser.APPROVE_OPTION) {
                    try {
                        FileUtils.copyFile(chooser.getSelectedFile(), new File(folder + File.separator + "plugins" + File.separator + chooser.getSelectedFile().getName()));
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(dialog, LanguageManager.getTranslationsFromFile("PluginAddingError"), LanguageManager.getTranslationsFromFile("Error"), JOptionPane.ERROR_MESSAGE);
                    }
                    loadPlugins();
                    pluginsList.setListData(pluginsNameList);
                }
            }
        }

        class OpenPluginsFolder extends JButton implements ActionListener {
            OpenPluginsFolder() {
                super(LanguageManager.getTranslationsFromFile("OpenPluginFolder"));
                addActionListener(this); setBackground(Color.white);
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Desktop.getDesktop().open(new File(folder + File.separator + "plugins" + File.separator));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(parentFrame, LanguageManager.getTranslationsFromFile("UnexpectedError"), LanguageManager.getTranslationsFromFile("Error"), JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        }

        class ButtonsCheckThread extends Thread {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    if (pluginsList.getSelectedIndex() == -1) {
                        dpb.setEnabled(false);
                    } else {
                        dpb.setEnabled(true);
                    }
                }
            }
        }
    }
}