package msm;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


class ImportServerDialog extends JDialog {
    private final MSMFrame parentFrame;
    private final JDialog dialog;

    private final JTextField nameInput, javaExeInput, serverJarInput;
    private final JSlider ramInput;

    private final JComboBox<String> mcver, servertype;

    private final ImportServerDialog.JRECheckBox jreCheckBox;
    private final ImportServerDialog.JREFileBrowser jreFileBrowser;





    ImportServerDialog(MSMFrame parent) {
        super(parent, LanguageManager.getTranslationsFromFile("ImportServer"), true);
        dialog = this;
        parentFrame = parent;
        this.setLayout(new BorderLayout());
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel buttons = new JPanel(new FlowLayout()); buttons.setBackground(Color.white);
        JPanel contents = new JPanel(new GridBagLayout()); contents.setBackground(Color.white);

        GridBagConstraints gbc = new GridBagConstraints();

        buttons.add(new CancelButton()); buttons.add(new ConfirmButton());

        gbc.gridy = 1; gbc.gridx = 0; gbc.anchor = GridBagConstraints.LINE_START; gbc.insets = new Insets(10, 10, 10, 10); gbc.fill = GridBagConstraints.HORIZONTAL;
        contents.add(new JLabel(LanguageManager.getTranslationsFromFile("ServerPath")), gbc);
        gbc.gridy = 0; contents.add(new JLabel(LanguageManager.getTranslationsFromFile("ServerName")), gbc);
        gbc.gridy = 2; contents.add(new JLabel(LanguageManager.getTranslationsFromFile("JavaPath")), gbc);
        gbc.gridy = 4; contents.add(new JLabel(LanguageManager.getTranslationsFromFile("RamQuantity")), gbc);
        gbc.gridy = 5; contents.add(new JLabel(LanguageManager.getTranslationsFromFile("MinecraftVersion")), gbc);

        nameInput = new JTextField("");
        gbc.gridy = 0; gbc.gridx = 1; contents.add(nameInput, gbc);

        serverJarInput = new JTextField(30);
        gbc.gridy = 1; contents.add(serverJarInput, gbc);
        gbc.gridx = 2; contents.add(new ServerJarBrowser(), gbc);

        javaExeInput = new JTextField();
        gbc.gridy = 2; gbc.gridx = 1; contents.add(javaExeInput, gbc);

        jreFileBrowser = new JREFileBrowser();
        gbc.gridx = 2; contents.add(jreFileBrowser, gbc);

        jreCheckBox = new JRECheckBox();
        gbc.gridx = 1; gbc.gridy = 3; gbc.gridwidth = 2; contents.add(jreCheckBox, gbc);

        ramInput = new JSlider(800, 2000, 1500); ramInput.setBackground(Color.white); ramInput.setMajorTickSpacing(200); ramInput.setMinorTickSpacing(50);ramInput.setPaintLabels(true); ramInput.setPaintTicks(true);
        gbc.gridy = 4; contents.add(ramInput, gbc);

        final String[] availversions = {"1.19"};
        final String[] availableservers = {"Vanilla", "PaperMC"};

        gbc.gridy = 5; gbc.gridx = 2; gbc.gridwidth = 1; 
        mcver = new JComboBox<String>(availversions); contents.add(mcver, gbc);
        gbc.gridx = 1; 
        servertype = new JComboBox<String>(availableservers); contents.add(servertype, gbc);
        
        
        



        this.add(buttons, BorderLayout.SOUTH); this.add(contents, BorderLayout.NORTH);
        this.pack();
        this.setVisible(true);

    }

    class CancelButton extends JButton implements ActionListener {
        CancelButton() {
            super(LanguageManager.getTranslationsFromFile("Cancel"));
            setBackground(Color.white);
            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            dialog.dispose();
        }
    }

    class ConfirmButton extends JButton implements ActionListener {
        ConfirmButton() {
            super(LanguageManager.getTranslationsFromFile("Submit"));
            setBackground(Color.white);
            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (parentFrame.tPane.getTabCount() >= 28) {
                JOptionPane.showMessageDialog(dialog, LanguageManager.getTranslationsFromFile("ServersLimit"), LanguageManager.getTranslationsFromFile("Warning"), JOptionPane.WARNING_MESSAGE);
                dialog.dispose();
                return;
            }
            if (nameInput.getText().replaceAll(" ", "").equals("")) {
                JOptionPane.showMessageDialog(dialog, LanguageManager.getTranslationsFromFile("EmptyServerName"), LanguageManager.getTranslationsFromFile("Error"), JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (serverJarInput.getText().equals("")) {
                JOptionPane.showMessageDialog(dialog, LanguageManager.getTranslationsFromFile("EmptyServerPath"), LanguageManager.getTranslationsFromFile("Error"), JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!serverJarInput.getText().endsWith(".jar") || !new File(serverJarInput.getText()).exists()) {
                JOptionPane.showMessageDialog(dialog, LanguageManager.getTranslationsFromFile("BadServerFile"), LanguageManager.getTranslationsFromFile("Error"), JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (javaExeInput.getText().equals("") && !jreCheckBox.isSelected()) {
                JOptionPane.showMessageDialog(dialog, LanguageManager.getTranslationsFromFile("EmptyJavaExec"), LanguageManager.getTranslationsFromFile("Error"), JOptionPane.ERROR_MESSAGE);
                return;
            }
            if ((System.getProperty("os.name").contains("Windows") && (!jreCheckBox.isSelected() && !javaExeInput.getText().endsWith(".exe"))) || !new File(javaExeInput.getText()).exists()) {
                JOptionPane.showMessageDialog(dialog, LanguageManager.getTranslationsFromFile("BadJavaExec"), LanguageManager.getTranslationsFromFile("Error"), JOptionPane.ERROR_MESSAGE);
                return;
            }

            String newServerFolder = SysConst.getServersPath() + nameInput.getText().replaceAll(" ", "");
            String originalServerFolder = (new File(serverJarInput.getText()).getParent());
            File serverFolder = new File(newServerFolder);
            boolean success = serverFolder.mkdir();
            if (!success) {
                JOptionPane.showMessageDialog(dialog, LanguageManager.getTranslationsFromFile("CannotCreateDir"), LanguageManager.getTranslationsFromFile("Error"), JOptionPane.ERROR_MESSAGE);
                dialog.dispose();
                return;
            }

            File configFile = new File(newServerFolder + File.separator + "config.msm");
            try {
                File oldServerFile = new File(serverJarInput.getText());
                File newServerFile = new File(oldServerFile.getParent() + File.separator + "server.jar");
                if(!oldServerFile.renameTo(newServerFile)) throw new IOException("Unable to rename the server file.");
                BufferedWriter bw = new BufferedWriter(new FileWriter(configFile));
                bw.write(nameInput.getText() + '\n');
                if (new File(originalServerFolder + File.separator + "server-icon.png").exists()) {
                    bw.write(originalServerFolder + File.separator + "server-icon.png" + '\n');
                } else bw.write("\n");                
                bw.write(originalServerFolder + '\n');
                bw.write((String) mcver.getSelectedItem() + '\n');
                if (!jreCheckBox.isSelected()) {
                    bw.write(javaExeInput.getText() + '\n');
                } else {
                    String javaHome = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
                    if (System.getProperty("os.name").contains("Windows")) javaHome = new String(javaHome + ".exe");
                    bw.write(javaHome + '\n');
                }
            
                bw.write(Integer.toString(ramInput.getValue()) + '\n');
                bw.write((String) servertype.getSelectedItem());
                bw.write("\nimported");
                bw.close();
                
                int index = parentFrame.tPane.getTabCount();
                parentFrame.serverTabs[index] = new ServerTab(configFile, parentFrame);
                parentFrame.tPane.addTab(nameInput.getText(), null, parentFrame.serverTabs[index]);
                parentFrame.tPane.setSelectedIndex(index);
                parentFrame.pack();
                parentFrame.serverTabs[index].reload();    

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(dialog, LanguageManager.getTranslationsFromFile("ImportError"), LanguageManager.getTranslationsFromFile("ImportError"), JOptionPane.ERROR_MESSAGE);
                dialog.dispose();
                return;
            }

            dialog.dispose();
        }
    }

    class JRECheckBox extends JCheckBox implements ActionListener {
        JRECheckBox() {
            super(LanguageManager.getTranslationsFromFile("UseDefaultPath"));
            setBackground(Color.white);
            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (this.isSelected()) {
                javaExeInput.setEnabled(false);
                jreFileBrowser.setEnabled(false);
            } else {
                javaExeInput.setEnabled(true);
                jreFileBrowser.setEnabled(true);
            }
        }
    }

    class JREFileBrowser extends JButton implements ActionListener {
        JREFileBrowser() {
            super(LanguageManager.getTranslationsFromFile("Browse"));
            setBackground(Color.white); 
            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

            if (System.getProperty("os.name").contains("Windows")) {
                FileNameExtensionFilter filter = new FileNameExtensionFilter(LanguageManager.getTranslationsFromFile("Executables"), "exe");
                chooser.setFileFilter(filter); chooser.setFileSelectionMode(JFileChooser.FILES_ONLY); chooser.setAcceptAllFileFilterUsed(false);
            }
            int r = chooser.showOpenDialog(dialog);
            if (r == JFileChooser.APPROVE_OPTION) {
                javaExeInput.setText(chooser.getSelectedFile().getAbsolutePath());
            } 
        }
    }

    class ServerJarBrowser extends JButton implements ActionListener {
        ServerJarBrowser() {
            super(LanguageManager.getTranslationsFromFile("Browse"));
            setBackground(Color.white); 
            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser chooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter("JAR file", "jar");
            chooser.setFileFilter(filter); chooser.setFileSelectionMode(JFileChooser.FILES_ONLY); chooser.setAcceptAllFileFilterUsed(false);
            int r = chooser.showOpenDialog(dialog);
            if (r == JFileChooser.APPROVE_OPTION) {
                serverJarInput.setText(chooser.getSelectedFile().getAbsolutePath());
            } 
        }
    }
}