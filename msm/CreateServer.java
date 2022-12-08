package msm;

import org.apache.commons.io.FileUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Scanner;

class CreateServerDialog extends JDialog {
    private final MSMFrame parentFrame;
    private final JDialog dialog;

    private final JTextField nameInput, javaExeInput, iconInput;
    private final JSlider ramInput;

    private final CreateServerDialog.JRECheckBox jreCheckBox;
    private final CreateServerDialog.JREFileBrowser jreFileBrowser;

    private CreateServerDialog.CreatingServerDialog csd;

    private JComboBox<String> mcver, servertype;
    CreateServerDialog(MSMFrame parent) {
        super(parent, LanguageManager.getTranslationsFromFile("CreateServer"), true);
        parentFrame = parent;
        dialog = this;
        this.setLayout(new BorderLayout());
        this.setSize(400, 300);
        this.setResizable(false);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        GridBagConstraints gbc = new GridBagConstraints();

        JPanel contents = new JPanel(new GridBagLayout()); contents.setBackground(Color.white);
        JPanel buttons = new JPanel(new FlowLayout()); buttons.setBackground(Color.white);

        buttons.add(new CreateServerDialog.CancelButton()); buttons.add(new CreateServerDialog.ConfirmButton());

        this.add(contents, BorderLayout.NORTH); this.add(buttons, BorderLayout.SOUTH);

        gbc.gridx = 0; gbc.gridy = 0; gbc.insets = new Insets(10, 10, 10, 10); gbc.anchor = GridBagConstraints.LINE_START;
        contents.add(new JLabel(LanguageManager.getTranslationsFromFile("Name")), gbc);

        gbc.gridy = 1; contents.add(new JLabel(LanguageManager.getTranslationsFromFile("JavaPath")), gbc);

        gbc.gridy = 3; contents.add(new JLabel(LanguageManager.getTranslationsFromFile("ServerIcon")), gbc);
        gbc.gridy = 4; contents.add(new JLabel(LanguageManager.getTranslationsFromFile("RamQuantity")), gbc);

        gbc.gridy = 5; contents.add(new JLabel(LanguageManager.getTranslationsFromFile("MinecraftVersion")), gbc);

        nameInput = new JTextField("", 16);
        gbc.gridy = 0; gbc.gridx = 1; 
        contents.add(nameInput, gbc);

        javaExeInput = new JTextField(30);
        gbc.gridy = 1; contents.add(javaExeInput, gbc);

        jreFileBrowser = new CreateServerDialog.JREFileBrowser();
        gbc.gridx = 2; contents.add(jreFileBrowser, gbc);

        gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 2; gbc.insets = new Insets(0, 10, 10, 10); 
        jreCheckBox = new CreateServerDialog.JRECheckBox(); contents.add(jreCheckBox, gbc);

        gbc.insets = new Insets(10, 10, 10, 10); gbc.gridx = 1; gbc.gridy = 3; gbc.gridwidth = 1;
        iconInput = new JTextField(30); iconInput.setToolTipText(LanguageManager.getTranslationsFromFile("ServerIconTooltip")); contents.add(iconInput, gbc);

        gbc.gridx = 2; contents.add(new CreateServerDialog.IconFileBrowser(), gbc);

        ramInput = new JSlider(800, 2000, 1500); ramInput.setBackground(Color.white); ramInput.setMajorTickSpacing(200); ramInput.setMinorTickSpacing(50);ramInput.setPaintLabels(true); ramInput.setPaintTicks(true);
        gbc.gridy = 4; gbc.gridx = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL; contents.add(ramInput, gbc);

        gbc.gridy = 5; gbc.gridx = 2; gbc.gridwidth = 1;

        final String[] availversions = {"1.19.3"};
        final String[] availableservers = {"Vanilla", "PaperMC"};

        mcver = new JComboBox<String>(availversions); contents.add(mcver, gbc);
        gbc.gridx = 1; 
        servertype = new JComboBox<String>(availableservers); contents.add(servertype, gbc);
        servertype.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                JComboBox<String> cb = (JComboBox<String>) e.getSource();
                if (((String) cb.getSelectedItem()).equals("Vanilla")) mcver.setModel(new DefaultComboBoxModel<>(new String[]{"1.19.3"}));
                else if (((String) cb.getSelectedItem()).equals("PaperMC")) mcver.setModel(new DefaultComboBoxModel<>(new String[]{"1.19.2"}));
            }
        });


        this.pack();
        this.setVisible(true);
    }

    class CancelButton extends JButton implements ActionListener {
        CancelButton() {
            super(LanguageManager.getTranslationsFromFile("Cancel"));
            addActionListener(this); setBackground(Color.white);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            dialog.dispose();
        }
    }

    class ConfirmButton extends JButton implements ActionListener {
        ConfirmButton() {
            super(LanguageManager.getTranslationsFromFile("Submit"));
            addActionListener(this); setBackground(Color.white);
        }

        @Override
        public void actionPerformed(ActionEvent e) {

            csd = new CreateServerDialog.CreatingServerDialog(dialog);
            CreateServerDialog.CreateServerThread cst = new CreateServerDialog.CreateServerThread(); cst.start();
            csd.setVisible(true);
        }
    }

    class CreatingServerDialog extends JDialog {
        public JLabel statusLabel;

        CreatingServerDialog(JDialog parent) {
            super(parent, LanguageManager.getTranslationsFromFile("Wait"), true);
            this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            this.setLayout(new GridBagLayout());
            this.setBackground(Color.white);
            this.setResizable(false);

            GridBagConstraints gbc = new GridBagConstraints();

            gbc.gridx = 0; gbc.gridy = 0; gbc.insets = new Insets(20, 20, 20, 20);

            statusLabel = new JLabel(); statusLabel.setBackground(Color.white); this.add(statusLabel, gbc); 

            gbc.gridy = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.anchor = GridBagConstraints.CENTER;
            JProgressBar pbar = new JProgressBar(); pbar.setIndeterminate(true); pbar.setBackground(Color.white);

            
            this.add(pbar, gbc);

            this.pack();

        }
    }

    class CreateServerThread extends Thread {
        @Override
        public void run() {
            if (parentFrame.tPane.getTabCount() >= 28) {
                JOptionPane.showMessageDialog(csd, LanguageManager.getTranslationsFromFile("ServersLimit"), LanguageManager.getTranslationsFromFile("Warning"), JOptionPane.WARNING_MESSAGE);
                csd.dispose();
                return;
            }

            if (nameInput.getText().replaceAll(" ", "").equals("")) {
                JOptionPane.showMessageDialog(csd, LanguageManager.getTranslationsFromFile("EmptyServerName"), LanguageManager.getTranslationsFromFile("Error"), JOptionPane.ERROR_MESSAGE);
                csd.dispose();
                return;
            }
            if (javaExeInput.getText().equals("") && !jreCheckBox.isSelected()) {
                JOptionPane.showMessageDialog(dialog, LanguageManager.getTranslationsFromFile("EmptyJavaExec"), LanguageManager.getTranslationsFromFile("Error"), JOptionPane.ERROR_MESSAGE);
                csd.dispose();
                return;
            }
            if (!jreCheckBox.isSelected() && ((System.getProperty("os.name").contains("Windows") && !javaExeInput.getText().endsWith(".exe")) || new File(javaExeInput.getText()).exists() == false)) {
                JOptionPane.showMessageDialog(dialog, LanguageManager.getTranslationsFromFile("BadJavaExec"), LanguageManager.getTranslationsFromFile("Error"), JOptionPane.ERROR_MESSAGE);
                csd.dispose();
                return;
            }
            String newServerFolder = SysConst.getServersPath() + nameInput.getText().replaceAll(" ", "");
            File serverFolder = new File(newServerFolder);
            boolean success = serverFolder.mkdir();
            if (!success) {
                JOptionPane.showMessageDialog(dialog, LanguageManager.getTranslationsFromFile("CannotCreateDir"), LanguageManager.getTranslationsFromFile("Error"), JOptionPane.ERROR_MESSAGE);
                csd.dispose();
                return;
            } 

            File configFile = new File(newServerFolder + File.separator + "config.msm");
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(configFile));
                bw.write(nameInput.getText() + '\n');
                if (!iconInput.getText().equals("")) bw.write(iconInput.getText() + '\n');
                else bw.write(SysConst.getImagesPath() + "default_icon.jpg\n");
                bw.write(newServerFolder + '\n');
                bw.write((String) mcver.getSelectedItem() + '\n');
                if (!jreCheckBox.isSelected()) {
                    bw.write(javaExeInput.getText() + '\n');
                } else {
                    String javaHome = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
                    System.out.println(javaHome);
                    if (System.getProperty("os.name").contains("Windows")) javaHome = new String(javaHome + ".exe");
                    bw.write(javaHome + '\n');
                }
                
                bw.write(Integer.toString(ramInput.getValue()) + '\n');
                bw.write((String) servertype.getSelectedItem());
                bw.write("\nnative");
                bw.close();

                csd.statusLabel.setText(LanguageManager.getTranslationsFromFile("DownloadingServer"));

                String urlString = "";
                try {
                    if (((String) servertype.getSelectedItem()).equals("Vanilla")) {
                        Scanner scanner = new Scanner(new File(SysConst.getConfPath() + "vanillalink.txt"));

                        if (((String) mcver.getSelectedItem()).equals("1.19.3")) {
                            while (scanner.hasNextLine()) {
                                String s = scanner.nextLine();
                                if (s.contains("1193:")) urlString = s.replaceAll("1193:", "");
                            }
                        }
                    } else if (((String) servertype.getSelectedItem()).equals("PaperMC")) {
                        Scanner scanner = new Scanner(new File(SysConst.getConfPath() + "paperlink.txt"));
                        if (((String) mcver.getSelectedItem()).equals("1.19.2")) {
                            while (scanner.hasNextLine()) {
                                String s = scanner.nextLine();
                                if (s.contains("1192:")) urlString = s.replaceAll("1192:", "");
                            }
                        }
                    } else {
                        throw new IOException("Software non valido.");
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(dialog, LanguageManager.getTranslationsFromFile("CreatingServerError"), LanguageManager.getTranslationsFromFile("Error"), JOptionPane.ERROR_MESSAGE);
                    FileUtils.delete(serverFolder);
                    csd.dispose();
                    return;
                }

                FileUtils.copyURLToFile(new URL(urlString), new File(newServerFolder + File.separator + "server.jar"), 30000,30000);
                File iconFile = new File(iconInput.getText());
                if (iconFile.exists()) FileUtils.copyFile(iconFile, new File(newServerFolder + File.separator + "server-icon.png"));

                
                int index = parentFrame.tPane.getTabCount();
                parentFrame.serverTabs[index] = new ServerTab(configFile, parentFrame);
                parentFrame.tPane.addTab(nameInput.getText(), null, parentFrame.serverTabs[index]);
                parentFrame.tPane.setSelectedIndex(index);
                parentFrame.pack();
                parentFrame.serverTabs[index].reload();
                
            
                File eula = new File(newServerFolder + File.separator + "eula.txt");
                bw = new BufferedWriter(new FileWriter(eula));
                bw.write("eula=true");
                bw.close();
    

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(dialog, LanguageManager.getTranslationsFromFile("CreatingServerError"), LanguageManager.getTranslationsFromFile("Error"), JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
                try {
                    FileUtils.deleteDirectory(serverFolder);
                } catch (IOException exc) {}
                csd.dispose();
                return;
            }

            csd.dispose();
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
            super("Sfoglia...");
            setBackground(Color.white); 
            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if (System.getProperty("os.name").contains("Windows")) {
                FileNameExtensionFilter filter = new FileNameExtensionFilter(LanguageManager.getTranslationsFromFile("Executables"), "exe");
                chooser.setFileFilter(filter); 
                chooser.setAcceptAllFileFilterUsed(false);
            }
            int r = chooser.showOpenDialog(dialog);
            if (r == JFileChooser.APPROVE_OPTION) {
                javaExeInput.setText(chooser.getSelectedFile().getAbsolutePath());
            } 
        }
    }

    class IconFileBrowser extends JButton implements ActionListener {
        IconFileBrowser() {
            super(LanguageManager.getTranslationsFromFile("Browse"));
            setBackground(Color.white);
            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser chooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter(LanguageManager.getTranslationsFromFile("PNGImages"), "png");
            chooser.setFileFilter(filter); chooser.setFileSelectionMode(JFileChooser.FILES_ONLY); chooser.setAcceptAllFileFilterUsed(false);
            int r = chooser.showOpenDialog(dialog);
            if (r == JFileChooser.APPROVE_OPTION) {
                iconInput.setText(chooser.getSelectedFile().getAbsolutePath());
            } 
        }
    }



}