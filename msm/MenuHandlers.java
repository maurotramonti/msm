package msm;

import org.apache.commons.io.FileUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

class ServerMenuHandler extends MSM implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        MSMFrame frame = getMSMFrame().frame;
        ServerTab current = (ServerTab) frame.tPane.getSelectedComponent();
        if (e.getActionCommand().equals("New server")) {
            new msm.CreateServerDialog(frame);
        } else if (e.getActionCommand().equals("Delete server")) {
            
            int r = JOptionPane.showConfirmDialog(frame, msm.LanguageManager.getTranslationsFromFile("DeleteServerConfirm"), msm.LanguageManager.getTranslationsFromFile("Warning"), JOptionPane.YES_NO_OPTION);
            if (r == JOptionPane.YES_OPTION) {
                try {
                    File sfolder = new File(current.folder);
                    FileUtils.deleteDirectory(sfolder);
                    sfolder = new File(current.confFile.getParent());
                    FileUtils.deleteDirectory(sfolder);
                    frame.tPane.remove(current);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame, msm.LanguageManager.getTranslationsFromFile("UnexpectedError"), LanguageManager.getTranslationsFromFile("Error"), JOptionPane.ERROR_MESSAGE);
                }

            }
        } else if (e.getActionCommand().equals("Edit server")) {
            new EditServerDialog(frame, current);
        } else if (e.getActionCommand().equals("Remove server")) {
            int r = JOptionPane.showConfirmDialog(frame, LanguageManager.getTranslationsFromFile("RemoveServerConfirm"), LanguageManager.getTranslationsFromFile("Warning"), JOptionPane.YES_NO_OPTION);
            if (r == JOptionPane.YES_OPTION) {
                try {
                    File sfolder = new File(current.confFile.getParent());
                    FileUtils.deleteDirectory(sfolder);
                    frame.tPane.remove(current);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame, msm.LanguageManager.getTranslationsFromFile("UnexpectedError"), LanguageManager.getTranslationsFromFile("Error"), JOptionPane.ERROR_MESSAGE);
                }

            }
        } else if (e.getActionCommand().equals("Import server...")) {
            new ImportServerDialog(frame);
        } else if (e.getActionCommand().equals("Make backup")) {
            new MakeBackupDialog(frame);
        }
    }
}

class PreferencesMenuHandler extends MSM implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        JFrame frame;
        try {
            frame = getMSMFrame().frame;
        } catch (NullPointerException ex) {
            frame = null;
        }
        String s = (String) JOptionPane.showInputDialog(frame, LanguageManager.getTranslationsFromFile("SelectLanguage"), LanguageManager.getTranslationsFromFile("Language"), JOptionPane.QUESTION_MESSAGE, null, LanguageManager.langs, LanguageManager.langs[LanguageManager.getCurrentLang()]);            
        if (s == null) return;
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(SysConst.getConfPath() + "language.txt"));
            bw.write(s);
            bw.close();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(frame, LanguageManager.getTranslationsFromFile("LanguageError"), LanguageManager.getTranslationsFromFile("Error"), JOptionPane.ERROR_MESSAGE);
        }
    }    
}

class HelpMenuHandler extends MSM implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("About MSM")) {
            JOptionPane.showMessageDialog(getMSMFrame().frame, LanguageManager.getTranslationsFromFile("VersionString"), LanguageManager.getTranslationsFromFile("VersionInfo"), JOptionPane.INFORMATION_MESSAGE, new ImageIcon(SysConst.getImagesPath() + "msm.png"));
        } else if (e.getActionCommand().equals("About Java")) {
            JOptionPane.showMessageDialog(getMSMFrame().frame, LanguageManager.getJavaVersionString(LanguageManager.getCurrentLang()), LanguageManager.getTranslationsFromFile("AboutJava"), JOptionPane.INFORMATION_MESSAGE, new ImageIcon(SysConst.getImagesPath() + "javalogo.png"));
        } else if (e.getActionCommand().equals("Check updates")) {
            getMSMFrame().checkUpdates(false);
        } else if (e.getActionCommand().equals("Report bug")) {
            JOptionPane.showMessageDialog(getMSMFrame().frame, LanguageManager.getTranslationsFromFile("ReportBugText"), LanguageManager.getTranslationsFromFile("ReportBug"), JOptionPane.INFORMATION_MESSAGE);
        } else if (e.getActionCommand().equals("Changelog")) {
            JOptionPane.showMessageDialog(getMSMFrame().frame, LanguageManager.getTranslationsFromFile("Changelog"), "Changelog", JOptionPane.PLAIN_MESSAGE);
        }
    }
}

