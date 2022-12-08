package msm;

import org.apache.commons.io.FileUtils;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Scanner;

public class AddPluginDialog extends JDialog {
    private final JDialog dialog;
    private static String serverFolder;
    private String[][] availablePluginsArray = null;

    private final ServerTab.PluginsDialog pdialog;

    private JList<String> availablePlugins;

    private PluginInformationPanel rightPane;

    private JSplitPane contents = new JSplitPane();


    AddPluginDialog(msm.ServerTab.PluginsDialog parent, String serverFolder) {
        super(parent, LanguageManager.getTranslationsFromFile("AddPlugin"), true);
        dialog = this; pdialog = parent; this.serverFolder = serverFolder;
        setMinimumSize(new Dimension(450, 330));

        JMenuBar menuBar = new JMenuBar();
        JMenu other = new JMenu(LanguageManager.getTranslationsFromFile("More"));

        JScrollPane leftPane;
        rightPane = new PluginInformationPanel();
        JPanel emptyPanel = new EmptyPluginInformationPanel();



        try {
            int i = 0;
            Scanner s = new Scanner(new File(SysConst.getConfPath() + "availableplugins.txt"));
            while (s.hasNextLine()) {
                s.nextLine();
                i++;
            }
            availablePluginsArray = new String[4][i];

            // array of String with the following structure:

            /*
                    [0]     [{title1, title2, title3}]   <--- array of TITLES
                    [1]     [{version1, version2, version3}]     <--- array of VERSIONS
                    [2]     [{link1, link2, link3}]     <--- array of LINKS
                    [3]     [{description1, description2, description3}]  <--- array of DESCRIPTIONS

            */

            s.close(); s = new Scanner(new File(SysConst.getConfPath() + "availableplugins.txt"));
            for (int j = 0; j < i; j++) {
                String str = s.nextLine();
                String[] tokens = str.split("_", 3);
                for (int h = 0; h < 3; h++) {
                    availablePluginsArray[h][j] = tokens[h];
                }
            }
            s.close();
            if (LanguageManager.getCurrentLang() == LanguageManager.ENGLISH) s = new Scanner(new File(SysConst.getLangsPath() + "eng" + File.separator + "PluginsDescriptions.txt"));
            else if (LanguageManager.getCurrentLang() == LanguageManager.ITALIAN) s = new Scanner(new File(SysConst.getLangsPath() + "ita" + File.separator + "PluginsDescriptions.txt"));

            for (int w = 0; w < 3; w++) {
                availablePluginsArray[3][w] = s.nextLine();
            }
            s.close();

            availablePlugins = new JList<String>(availablePluginsArray[0]); availablePlugins.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            availablePlugins.addListSelectionListener(new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent e) {
                    updateInformationPanel();
                }
            });
            leftPane = new JScrollPane(availablePlugins, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED); leftPane.setBackground(Color.white);
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }


        JMenuItem addPluginFromFile = new JMenuItem(LanguageManager.getTranslationsFromFile("AddPluginFromFile"));
        addPluginFromFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                chooser.setAcceptAllFileFilterUsed(false);
                FileNameExtensionFilter filter = new FileNameExtensionFilter("JAR file", "jar");
                chooser.setFileFilter(filter);
                int r = chooser.showOpenDialog(dialog);
                if (r == JFileChooser.APPROVE_OPTION) {
                    try {
                        FileUtils.copyFile(chooser.getSelectedFile(), new File(serverFolder + File.separator + "plugins" + File.separator + chooser.getSelectedFile().getName()));
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(dialog, LanguageManager.getTranslationsFromFile("PluginAddingError"), LanguageManager.getTranslationsFromFile("Error"), JOptionPane.ERROR_MESSAGE);
                    }
                    PluginInformationPanel.addPluginToConfigFile(chooser.getSelectedFile().getName().replace(".jar", ""), "unknown");
                    pdialog.loadPlugins();
                    dialog.dispose();
                }
            }
        }); other.add(addPluginFromFile);

        JMenuItem updatePluginsList = new JMenuItem(LanguageManager.getTranslationsFromFile("UpdateList"));
        updatePluginsList.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    FileUtils.copyURLToFile(new URL("https://raw.githubusercontent.com/maurotramonti/msm/main/conf/availableplugins.txt"), new File(SysConst.getConfPath() + "availableplugins.txt"));
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(dialog, LanguageManager.getTranslationsFromFile("UpdatingError"), LanguageManager.getTranslationsFromFile("Error"), JOptionPane.ERROR_MESSAGE);
                }
            }
        }); other.add(updatePluginsList);

        contents.setLeftComponent(leftPane); contents.setRightComponent(emptyPanel);
        leftPane.setMinimumSize(new Dimension(100, 330));  // to avoid the "disappearance" of the JList because of stupid users

        setContentPane(contents);
        menuBar.add(other); setJMenuBar(menuBar);
        setVisible(true);
    }

    class PluginInformationPanel extends JPanel {
        final JLabel title, version, installedLabel;

        InstallOrUpdatePluginButton ioupb = new InstallOrUpdatePluginButton();
        JTextPane description;

        PluginInformationPanel() {
            super(new GridBagLayout()); setBackground(Color.white);
            GridBagConstraints gbc = new GridBagConstraints();

            title = new JLabel(); version = new JLabel(); installedLabel = new JLabel(LanguageManager.getTranslationsFromFile("Installed")); installedLabel.setForeground(Color.green); description = new JTextPane(); description.setEditable(false);
            gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.LINE_START; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(10, 10, 10, 10);

            add(title, gbc); gbc.gridx = 1; add(version, gbc);
            gbc.gridx = 2; add(installedLabel, gbc); installedLabel.setVisible(false);

            gbc.gridy = 1; gbc.gridx = 0; gbc.gridwidth = 3; add(new JSeparator(), gbc);
            gbc.gridy = 2; add(description, gbc);
            gbc.gridy = 3; add(ioupb, gbc);

        }

        class InstallOrUpdatePluginButton extends  JButton implements ActionListener {
            InstallOrUpdatePluginButton() {
                super(LanguageManager.getTranslationsFromFile("Install"));
                setBackground(Color.white); addActionListener(this);
            }
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = availablePlugins.getSelectedIndex();
                WaitingDialog wd = new WaitingDialog(dialog);
                String[] controlVar = new String[1];
                new DownloadPluginThread(wd, controlVar).start();
                wd.setVisible(true);
                if (controlVar[0].equals("success")) addPluginToConfigFile(availablePluginsArray[0][index], availablePluginsArray[1][index]);
                updateInformationPanel();

            }

            class WaitingDialog extends JDialog {
                WaitingDialog(JDialog parent) {
                    super(parent, LanguageManager.getTranslationsFromFile("Wait"), true); setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
                    add(new JLabel(LanguageManager.getTranslationsFromFile("DownloadInProgress")));
                    pack();
                    setResizable(false);
                }
            }

            class DownloadPluginThread extends Thread {
                JDialog waitingDialog;
                String[] cvar;
                DownloadPluginThread(JDialog d, String[] cvar) {
                    waitingDialog = d; this.cvar = cvar;
                }
                @Override
                public void run() {
                    try {
                        int index = availablePlugins.getSelectedIndex();
                        FileUtils.copyURLToFile(new URL(availablePluginsArray[2][index]), new File(serverFolder + File.separator + "plugins" + File.separator + availablePluginsArray[0][index] + ".jar"));
                        this.cvar[0] = "success";
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(waitingDialog, LanguageManager.getTranslationsFromFile("DownloadError"), LanguageManager.getTranslationsFromFile("Error"), JOptionPane.ERROR_MESSAGE);
                        this.cvar[0] = "failure";
                        ex.printStackTrace();
                    }
                    waitingDialog.dispose();
                }
            }
        }

        public void setTitle(String title) {
            this.title.setText("<html><h3>" + title + "</h3></html>");
        }

        public void setVersion(String version) {
            this.version.setText(version);
        }

        public void setDescription(String description) {
            this.description.setText(description);
        }

        public void setInstalled(boolean installed) {
            if (installed) {
                this.installedLabel.setVisible(true);
                this.ioupb.setLabel(LanguageManager.getTranslationsFromFile("Update"));
            }
            else {
                this.installedLabel.setVisible(false);
                this.ioupb.setLabel(LanguageManager.getTranslationsFromFile("Install"));
            }
        }

        public static void addPluginToConfigFile(String pluginName, String pluginVersion) {
            File pluginsFile = new File(serverFolder + File.separator + "plugins.msm");
            try {
                Scanner s = new Scanner(pluginsFile);
                String[] plugins;
                int pnum = 0;
                while (s.hasNextLine()) {
                    s.nextLine();
                    pnum++;
                }
                s.close(); s = new Scanner(pluginsFile);
                if (pnum > 0) {
                    plugins = new String[pnum + 1];
                    boolean isAnUpdate = false;
                    for (int i = 0; i < pnum; i++) {
                        String str = s.nextLine();
                        if (str.contains(pluginName)) {
                            plugins[i] = pluginName + ":" + pluginVersion;
                            isAnUpdate = true;
                        } else plugins[i] = str;
                    }
                    if (!isAnUpdate) plugins[plugins.length - 1] = pluginName + ":" + pluginVersion;
                } else {
                    plugins = new String[1]; plugins[0] = pluginName + ":" + pluginVersion;
                }

                s.close();
                BufferedWriter bw = new BufferedWriter(new FileWriter(pluginsFile));
                for (int j = 0; j < plugins.length; j++) {
                    if (plugins[j] == null || plugins[j].equals("")) continue;
                    bw.write(plugins[j] + "\n");
                }
                bw.close();

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }

    private void updateInformationPanel() {
        int index = availablePlugins.getSelectedIndex();
        rightPane.setTitle(availablePluginsArray[0][index]);
        rightPane.setVersion(availablePluginsArray[1][index]);
        rightPane.setDescription(availablePluginsArray[3][index]);
        rightPane.setInstalled(false);
        try {
            Scanner s = new Scanner(new File(serverFolder + File.separator + "plugins.msm"));
            while (s.hasNextLine()) {
                if (s.nextLine().contains(availablePluginsArray[0][index])) {
                    rightPane.setInstalled(true);
                    break;
                }
            }
            s.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        contents.setRightComponent(rightPane);
    }

    class EmptyPluginInformationPanel extends JPanel {
        EmptyPluginInformationPanel() {
            super();
            add(new JLabel(LanguageManager.getTranslationsFromFile("NoPluginSelected")));
        }
    }

}
