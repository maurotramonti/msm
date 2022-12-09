package msm;

import org.apache.commons.io.FileUtils;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.*;
import java.net.URL;
import java.util.Scanner;


class ServerTab extends JPanel {
    public String title, folder, iconPath, software, serverVersion;
    public boolean imported, running = false;
    public Process serverProcess;

    public File confFile;

    private final JLabel titleLabel, pathLabel, ramLabel, javaExeLabel;

    private ServerConsoleDialog scd;



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


        if (!new File(confFile.getParent() + File.separator + "server.properties").exists()) {
            optionButton.setEnabled(false);
            pluginsButton.setEnabled(false);
        }

        if (software.equals("Vanilla")) pluginsButton.setEnabled(false);


        buttons.add(pluginsButton); buttons.add(optionButton); buttons.add(launchButton); buttons.add(new ServerTab.OpenServerFolder()); buttons.add(updateButton);

        add(contents, BorderLayout.NORTH);
        add(buttons, BorderLayout.SOUTH);

    }

    class ServerConsoleDialog extends JFrame {
        public final JTextPane outputArea;
        public final JScrollPane outputPane;

        private final JTextField commandInput = new JTextField(52);
        private final ServerTab.SendCommandButton sendCommandButton = new ServerTab.SendCommandButton();

        ServerConsoleDialog() {
            super("Console - " + title);
            this.setIconImage(Toolkit.getDefaultToolkit().getImage(SysConst.getImagesPath() + "msm.png"));
            setLayout(new GridBagLayout()); setResizable(false); setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            GridBagConstraints gbc = new GridBagConstraints();

            commandInput.setToolTipText(LanguageManager.getTranslationsFromFile("CommandToolTip"));

            outputArea = new JTextPane();

            outputArea.setEditable(false);

            outputPane = new JScrollPane(outputArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            outputPane.setOpaque(true);

            outputPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            outputPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
            outputPane.getVerticalScrollBar().setUnitIncrement(16);

            gbc.gridx = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.gridy = 0; gbc.insets = new Insets(8, 8, 8, 8); gbc.gridwidth = 2;
            add(commandInput, gbc);

            gbc.gridx = 2; gbc.anchor = GridBagConstraints.LINE_END; gbc.gridwidth = 1; add(sendCommandButton, gbc);

            gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 3;
            outputPane.setPreferredSize(new Dimension(500, 200)); outputPane.revalidate();
            add(outputPane, gbc);
            pack();
            setVisible(true);
        }
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

            scd = new ServerConsoleDialog();

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
                scd.dispose();
            }
            
        }
    }

    class PrintOutputThread extends Thread {

        @Override
        public void run() {
            while (serverThread.isAlive() && !Thread.currentThread().isInterrupted()) {
                try {
                    Scanner scanner = new Scanner(new File(folder + File.separator + "log.txt"));
                    String s = "";
                    while (scanner.hasNextLine()) s = s + scanner.nextLine() + '\n';
                    scd.outputArea.setText(s);
                    scanner.close();
                    Thread.sleep(500);
                } catch (IOException ex) {
                    ex.printStackTrace();
                } catch (InterruptedException ex) {
                    break;
                }
            }
            scd.outputArea.setText("");

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
            byte[] command = (scd.commandInput.getText() + '\n').getBytes();
            try {
                serverProcessInput.write(command);
                serverProcessInput.flush(); 
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(parentFrame, LanguageManager.getTranslationsFromFile("SendCommandError"), LanguageManager.getTranslationsFromFile("Error"), JOptionPane.ERROR_MESSAGE);
            } finally {
                scd.commandInput.setText("");
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

    class PluginsDialog extends JDialog {
        private final DeletePluginButton dpb = new DeletePluginButton();

        private final PluginsDialog dialog;

        private JScrollPane spane = new JScrollPane();
        private JTable pluginsTable;

        private String[][] pluginsList = null;


        PluginsDialog(JFrame parent) {
            super(parent, "Plugins", true);
            this.dialog = this;
            setLayout(new BorderLayout(0, 20)); setResizable(true); setBackground(Color.white); setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            getContentPane().setBackground(Color.white);


            loadPlugins();
            setMinimumSize(new Dimension(440, 270)); setSize(new Dimension(440, 270)); spane.setMinimumSize(new Dimension(230, 230));
            add(spane, BorderLayout.CENTER);
            JPanel buttons = new JPanel(new FlowLayout()); buttons.setBackground(Color.white);
            buttons.add(new AddPluginButton()); buttons.add(dpb); buttons.add(new OpenPluginsFolder());
            pluginsTable.setBackground(Color.white); pluginsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            add(buttons, BorderLayout.SOUTH);


            setVisible(true);
        }

        public void loadPlugins() {
            final String[] columnNames = {LanguageManager.getTranslationsFromFile("Name").replace(":", "").replace("<b>", "").replace("</b>", ""), LanguageManager.getTranslationsFromFile("Version")};

            try {
                File pluginsListFile = new File(folder + File.separator + "plugins.msm");
                if (!pluginsListFile.exists()) pluginsListFile.createNewFile();
                Scanner s = new Scanner(pluginsListFile);
                int rowCount = 0;
                while (s.hasNextLine()) {
                    s.nextLine();
                    rowCount++;
                }
                pluginsList = new String[rowCount][2];
                s.close(); s = new Scanner(new File(folder + File.separator + "plugins.msm"));
                for (int i = 0; i < rowCount; i++) {
                    String[] tokens = s.nextLine().split(":", 2);
                    pluginsList[i][0] = tokens[0];
                    pluginsList[i][1] = tokens[1];
                }
                s.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            pluginsTable = new JTable(pluginsList, columnNames) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            pluginsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent e) {
                    if (pluginsTable.getSelectedRow() == -1) {
                        dpb.setEnabled(false);
                    } else dpb.setEnabled(true);
                }
            });
            spane.setViewportView(pluginsTable);

        }

        class DeletePluginButton extends JButton implements ActionListener {
            DeletePluginButton() {
                super(LanguageManager.getTranslationsFromFile("DeletePlugin"));
                addActionListener(this); setBackground(Color.white); setEnabled(false);
            }
            @Override
            public void actionPerformed(ActionEvent e) {
                int r = JOptionPane.showConfirmDialog(dialog, LanguageManager.getTranslationsFromFile("DeletePluginConfirm"), LanguageManager.getTranslationsFromFile("Confirm"), JOptionPane.YES_NO_OPTION);
                if (r == JOptionPane.NO_OPTION) return;
                String pluginSelectedName = "";
                try {
                    pluginSelectedName = pluginsList[pluginsTable.getSelectedRow()][0];
                    if(!new File(folder + File.separator + "plugins" + File.separator + pluginSelectedName + ".jar").delete()) {
                        JOptionPane.showMessageDialog(dialog, LanguageManager.getTranslationsFromFile("DeletePluginError"), LanguageManager.getTranslationsFromFile("Error"), JOptionPane.ERROR_MESSAGE);
                    } else {
                        File[] directories = new File(folder + File.separator + "plugins").listFiles();
                        for (File f : directories) {
                            if (f.isDirectory() && f.getName().contains(pluginSelectedName))
                                FileUtils.deleteDirectory(f);
                        }
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                try {
                    Scanner s = new Scanner(new File(folder + File.separator + "plugins.msm"));
                    int rowToDelete = -1;
                    int rowCount = 0;
                    while (s.hasNextLine()) {
                        s.nextLine();
                        rowCount++;
                    }
                    s.close(); s = new Scanner(new File(folder + File.separator + "plugins.msm"));
                    String[] rows = new String[rowCount];
                    for (int h = 0; h < rowCount; h++) {
                        rows[h] = s.nextLine();
                        if (rows[h].contains(pluginSelectedName)) rowToDelete = h;
                    }

                    s.close();
                    BufferedWriter bw = new BufferedWriter(new FileWriter(folder + File.separator + "plugins.msm"));
                    for (int z = 0; z < rowCount; z++) {
                        if (z == rowToDelete) continue;
                        bw.write(rows[z] + "\n");
                    }
                    bw.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }





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
                new AddPluginDialog(dialog, folder);
                loadPlugins();
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

    }
}