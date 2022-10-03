package msm;

import org.apache.commons.io.FileUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

class MakeBackupDialog extends JDialog {

    private final MSMFrame parentFrame;
    private final JDialog dialog;
    
    private final JTextField destinationFolder = new JTextField(28);
    private final JTextField name = new JTextField(16);


    MakeBackupDialog(MSMFrame parent) {
        super(parent, LanguageManager.getTranslationsFromFile("MakeBackup"), true);
        dialog = this; parentFrame = parent;
        setResizable(false); setLayout(new BorderLayout()); setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        JPanel contents, buttons;
        contents = new JPanel(); contents.setBackground(Color.white); contents.setLayout(new GridBagLayout());
        buttons = new JPanel(); buttons.setBackground(Color.white); buttons.setLayout(new FlowLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(15, 9, 8, 9);
        contents.add(new JLabel(LanguageManager.getTranslationsFromFile("Name")));
        gbc.gridy = 1; contents.add(new JLabel(LanguageManager.getTranslationsFromFile("DestinationFolder")), gbc);

        gbc.gridx = 1; gbc.gridy = 0; contents.add(name, gbc);
        gbc.gridy = 1; contents.add(destinationFolder, gbc);
        gbc.gridx = 2; contents.add(new MakeBackupDialog.DestFolderBrowser(), gbc);

        buttons.add(new MakeBackupDialog.CancelButton()); buttons.add(new MakeBackupDialog.SubmitButton());
        this.add(contents, BorderLayout.NORTH); this.add(buttons, BorderLayout.SOUTH);
        this.pack();
        this.setVisible(true);
    }


    class SubmitButton extends JButton implements ActionListener {
        SubmitButton() {
            super(LanguageManager.getTranslationsFromFile("Submit"));
            addActionListener(this); setBackground(Color.white);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            File destFold = new File(destinationFolder.getText());
            if (!destFold.exists() || !destFold.isDirectory()) {
                JOptionPane.showMessageDialog(dialog, LanguageManager.getTranslationsFromFile("DestinationFolderError"), LanguageManager.getTranslationsFromFile("Error"), JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (name.getText().replaceAll(" ", "").equals("")) {
                JOptionPane.showMessageDialog(dialog, LanguageManager.getTranslationsFromFile("EmptyName"), LanguageManager.getTranslationsFromFile("Error"), JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                File destDir = new File(destFold.getAbsolutePath() + File.separator + name.getText());
                FileUtils.moveDirectory(new File(parentFrame.serverTabs[parentFrame.tPane.getSelectedIndex()].folder), destDir);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            dialog.dispose();
        }
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

    class DestFolderBrowser extends JButton implements ActionListener {
        DestFolderBrowser() {
            super(LanguageManager.getTranslationsFromFile("Browse"));
            addActionListener(this); setBackground(Color.white);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int r = fc.showOpenDialog(dialog);
            if (r == JFileChooser.APPROVE_OPTION) {
                destinationFolder.setText(fc.getSelectedFile().getAbsolutePath());
            }
        }
    }
}