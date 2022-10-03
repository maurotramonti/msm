package msm;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

class LanguageManager extends MSM {
    public static final int ITALIAN = 1;
    public static final int ENGLISH = 0;

    public static final int MI_ACTS = 0;
    public static final int MI_LBLS = 1;

    
    public static final String[] langs = {"English","Italiano"};
    private static int lang;

    public static String[] getMenuItemsInfo(int typeOfInfo) {
        String[] tmp = new String[12];
        if (typeOfInfo == MI_ACTS) {
            tmp = new String[]{"New server", "Edit server", "Import server...", "Remove server", "Delete server", "Make backup", "Language", "Check updates", "About MSM", "About Java", "Report bug", "Changelog"};
        } else if (typeOfInfo == MI_LBLS) {              
            tmp = new String[]{getTranslationsFromFile("NewServer", lang), getTranslationsFromFile("EditServer", lang), getTranslationsFromFile("ImportServer", lang), getTranslationsFromFile("RemoveServer", lang), getTranslationsFromFile("DeleteServer", lang), getTranslationsFromFile("MakeBackup"), getTranslationsFromFile("Language", lang), getTranslationsFromFile("CheckUpdates", lang), getTranslationsFromFile("AboutMSM", lang), getTranslationsFromFile("AboutJava", lang), getTranslationsFromFile("ReportBug", lang), "Changelog"};
        }
        return tmp;
    }
    public static String getTranslationsFromFile(String property) {
        return getTranslationsFromFile(property, getCurrentLang());
    }

    public static String getTranslationsFromFile(String property, int lang) {
        String prefix = "", contents = "Missing translations.";
        switch (lang) {
            case 0:
                prefix = SysConst.getLangsPath() + "eng" + File.separator;
                break;
            case 1:
                prefix = SysConst.getLangsPath() + "ita" + File.separator;
                break;
        }
        try {
            File lf = new File(prefix + property + ".txt");
            Scanner s = new Scanner(lf, "UTF-8");
            contents = "";
            do {
                contents = contents + s.nextLine() + '\n';
            } while (s.hasNextLine());
            s.close();
        } catch (FileNotFoundException e) {
            JFrame frame;
            try {
                frame = getMSMFrame().frame;
            } catch (NullPointerException ex){
                frame = null;
            }
            JOptionPane.showMessageDialog(frame, LanguageManager.getTranslationsFromFile("MissingTranslation") + property + ".txt", LanguageManager.getTranslationsFromFile("Error"), JOptionPane.ERROR_MESSAGE);
        }
        return contents;
    }
    public static String getJavaVersionString(int lang) {
        if (lang == 0) return "Version: " + System.getProperty("java.vm.version") + "\nInstall path:  " + System.getProperty("java.home") + "\nOperating system: " + System.getProperty("os.name");
        else if (lang == 1) return "Versione: " + System.getProperty("java.vm.version") + "\nPercorso di installazione:  " + System.getProperty("java.home") + "\nSistema operativo: " + System.getProperty("os.name");
        return "none";
    }

    public static int getCurrentLang() {
        return lang;
    }

    public static void setLang(int l) {
        lang = l;
    }
    

}
