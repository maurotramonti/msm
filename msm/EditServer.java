package msm;

import javax.swing.*;
import java.awt.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.filechooser.*;
import java.net.*;
import java.util.Scanner;


class EditServerDialog extends JDialog {
    private MSMFrame parentFrame;
    private JDialog dialog;

    private String serverFolder;
    private ServerTab currentTab;

    private JPanel contents, buttons;

    private JTextField nameInput, javaExeInput, iconInput;
    private JSlider ramInput;

    private EditServerDialog.JRECheckBox jreCheckBox;
    private EditServerDialog.JREFileBrowser jreFileBrowser;

    EditServerDialog(MSMFrame parent, ServerTab currentTab) {
        super(parent, LanguageManager.getTranslationsFromFile("EditServer"), true);
        parentFrame = parent;
        dialog = this;
        this.setLayout(new BorderLayout());
        this.setSize(400, 300);
        this.setResizable(false);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        this.currentTab = currentTab;
        serverFolder = this.currentTab.folder;
        File conffile = new File(serverFolder + File.separator + "config.msm");
        Scanner scanner = null;
        try {
            scanner = new Scanner(conffile);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        

        GridBagConstraints gbc = new GridBagConstraints();

        contents = new JPanel(new GridBagLayout()); contents.setBackground(Color.white);
        buttons = new JPanel(new FlowLayout()); buttons.setBackground(Color.white);

        buttons.add(new EditServerDialog.CancelButton()); buttons.add(new EditServerDialog.ConfirmButton());

        this.add(contents, BorderLayout.NORTH); this.add(buttons, BorderLayout.SOUTH);

        gbc.gridx = 0; gbc.gridy = 0; gbc.insets = new Insets(10, 10, 10, 10); gbc.anchor = GridBagConstraints.LINE_START;
        contents.add(new JLabel(LanguageManager.getTranslationsFromFile("JavaPath")), gbc);


        gbc.gridy = 2; contents.add(new JLabel(LanguageManager.getTranslationsFromFile("ServerIcon")), gbc);
        gbc.gridy = 3; contents.add(new JLabel(LanguageManager.getTranslationsFromFile("RamQuantity")), gbc);

        scanner.nextLine();
        iconInput = new JTextField(scanner.nextLine(), 30);
        scanner.nextLine(); scanner.nextLine(); 
        javaExeInput = new JTextField(scanner.nextLine(), 30);
        gbc.gridx = 1; gbc.gridy = 0; contents.add(javaExeInput, gbc);

        jreFileBrowser = new EditServerDialog.JREFileBrowser();
        gbc.gridx = 2; contents.add(jreFileBrowser, gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 2; gbc.insets = new Insets(0, 10, 10, 10); 
        jreCheckBox = new EditServerDialog.JRECheckBox(); contents.add(jreCheckBox, gbc);

        gbc.insets = new Insets(10, 10, 10, 10); gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 1;
        contents.add(iconInput, gbc);

        gbc.gridx = 2; contents.add(new EditServerDialog.IconFileBrowser(), gbc);

        ramInput = new JSlider(800, 2000, Integer.parseInt(scanner.nextLine())); ramInput.setBackground(Color.white); ramInput.setMajorTickSpacing(200); ramInput.setMinorTickSpacing(50);ramInput.setPaintLabels(true); ramInput.setPaintTicks(true);
        gbc.gridy = 3; gbc.gridx = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL; contents.add(ramInput, gbc);

        scanner.close();

        this.pack();
        this.setVisible(true);
    }

    class CancelButton extends JButton implements ActionListener {
        CancelButton() {
            super("Annulla");
            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            dialog.dispose();
        }
    }

    class ConfirmButton extends JButton implements ActionListener {
        ConfirmButton() {
            super("Conferma");
            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (nameInput.getText().replaceAll(" ", "").equals("")) {
                JOptionPane.showMessageDialog(dialog, LanguageManager.getTranslationsFromFile("EmptySerevrName"), LanguageManager.getTranslationsFromFile("Error"), JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (javaExeInput.getText().equals("") && jreCheckBox.isSelected() == false) {
                JOptionPane.showMessageDialog(dialog, LanguageManager.getTranslationsFromFile("EmptyJavaExec"), LanguageManager.getTranslationsFromFile("Error"), JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (jreCheckBox.isSelected() == false && ((System.getProperty("os.name").contains("Windows") && javaExeInput.getText().endsWith(".exe") == false) || new File(javaExeInput.getText()).exists() == false)) {
                JOptionPane.showMessageDialog(dialog, LanguageManager.getTranslationsFromFile("BadJavaExec"), LanguageManager.getTranslationsFromFile("Error"), JOptionPane.ERROR_MESSAGE);
                return;
            }
            File configFile = new File(serverFolder + File.separator + "config.msm");
            try {
                Scanner s = new Scanner(configFile);
                String[] existingSettings = new String[8];
                for (int i = 0; i < 8; i++) {
                    existingSettings[i] = s.nextLine();
                    if (existingSettings[i].endsWith("\n") == false) existingSettings[i] = new String(existingSettings[i] + '\n');
                }
                s.close();
                copyFile(new File(iconInput.getText()), new File(serverFolder + File.separator + "server-icon.png"));
                BufferedWriter bw = new BufferedWriter(new FileWriter(configFile));
                bw.write(existingSettings[0]); 
                bw.write(iconInput.getText() + '\n');
                bw.write(existingSettings[2]);
                bw.write(existingSettings[3]);
                if (jreCheckBox.isSelected() == false) {
                    bw.write(javaExeInput.getText() + '\n');
                } else {
                    String javaHome = new String(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java");
                    if (System.getProperty("os.name").contains("Windows")) javaHome = new String(javaHome + ".exe");
                    bw.write(javaHome + '\n');
                }
                bw.write(Integer.toString(ramInput.getValue()) + '\n');
                bw.write(existingSettings[7]);
                bw.close(); 

                currentTab.reload();

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(dialog, LanguageManager.getTranslationsFromFile("EditError"), LanguageManager.getTranslationsFromFile("Error"), JOptionPane.ERROR_MESSAGE);
            } finally {
                dialog.dispose();
            }
        }
        
    }

    
    private void copyFile(File source, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;

        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            try {
                is.close();
                os.close();
            } catch (NullPointerException ex) {}
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
            super("Sfoglia...");
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