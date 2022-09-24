package msm;

import javax.swing.*;
import java.awt.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.io.*;

class ServerMenuHandler extends MSM implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        MSMFrame frame = getMSMFrame().frame;
        ServerTab current = (ServerTab) frame.tPane.getSelectedComponent();
        if (e.getActionCommand().equals("New server")) {
            CreateServerDialog dialog = new CreateServerDialog(frame);
        } else if (e.getActionCommand().equals("Delete server")) {
            
            int r = JOptionPane.showConfirmDialog(frame, LanguageManager.getTranslationsFromFile("DeleteServerConfirm"), LanguageManager.getTranslationsFromFile("Warning"), JOptionPane.YES_NO_OPTION);
            if (r == JOptionPane.YES_OPTION) {
                File sfolder = new File(current.folder);
                HelpMethods.deleteDirectory(sfolder);
                sfolder = new File(current.confFile.getParent());
                HelpMethods.deleteDirectory(sfolder);
                frame.tPane.remove(current);
            }
        } else if (e.getActionCommand().equals("Edit server")) {
            EditServerDialog dialog = new EditServerDialog(frame, current);
        } else if (e.getActionCommand().equals("Remove server")) {
            int r = JOptionPane.showConfirmDialog(frame, LanguageManager.getTranslationsFromFile("RemoveServerConfirm"), LanguageManager.getTranslationsFromFile("Warning"), JOptionPane.YES_NO_OPTION);
            if (r == JOptionPane.YES_OPTION) {
                File sfolder = new File(current.confFile.getParent());
                HelpMethods.deleteDirectory(sfolder);
                frame.tPane.remove(current);
            }
        } else if (e.getActionCommand().equals("Import server...")) {
            ImportServerDialog isd = new ImportServerDialog(frame);
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
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(SysConst.getPrePath() + "conf" + File.separator + "language.txt")));
            bw.write(s);
            bw.close();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(getMSMFrame().frame, LanguageManager.getTranslationsFromFile("LanguageError"), LanguageManager.getTranslationsFromFile("Error"), JOptionPane.ERROR_MESSAGE);
        }
    }    
}

class HelpMenuHandler extends MSM implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("About MSM")) {
            JOptionPane.showMessageDialog(getMSMFrame().frame, LanguageManager.getTranslationsFromFile("VersionString"), LanguageManager.getTranslationsFromFile("VersionInfo"), JOptionPane.INFORMATION_MESSAGE, new ImageIcon(SysConst.getLogoPath()));
        } else if (e.getActionCommand().equals("About Java")) {
            JOptionPane.showMessageDialog(getMSMFrame().frame, LanguageManager.getJavaVersionString(LanguageManager.getCurrentLang()), LanguageManager.getTranslationsFromFile("AboutJava"), JOptionPane.INFORMATION_MESSAGE, new ImageIcon(SysConst.getJavaLogoPath()));
        } else if (e.getActionCommand().equals("Check updates")) {
            getMSMFrame().checkUpdates(false);
        } else if (e.getActionCommand().equals("Report bug")) {
            JOptionPane.showMessageDialog(getMSMFrame().frame, LanguageManager.getTranslationsFromFile("ReportBugText"), LanguageManager.getTranslationsFromFile("ReportBug"), JOptionPane.INFORMATION_MESSAGE);
        }
    }
}

