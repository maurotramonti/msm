package msm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;
import java.io.*;
import java.util.Scanner;

class LanguageManager extends MSM {
    public static final int ITALIAN = 1;
    public static final int ENGLISH = 0;

    public static final int MI_ACTS = 0;
    public static final int MI_LBLS = 1;

    
    public static final String[] langs = {"English","Italiano"};
    private static int lang;

    public static String[] getMenuItemsInfo(int typeOfInfo) {
        if (typeOfInfo == MI_ACTS) {
            String[] tmp = {"New server", "Edit server", "Import server...", "Remove server", "Delete server", "Language", "Check updates", "About MSM", "About Java", "Report bug"};           
            return tmp;
        } else if (typeOfInfo == MI_LBLS) {              
            String[] tmp = {getTranslationsFromFile("NewServer", lang), getTranslationsFromFile("EditServer", lang), getTranslationsFromFile("ImportServer", lang), getTranslationsFromFile("RemoveServer", lang), getTranslationsFromFile("DeleteServer", lang), getTranslationsFromFile("Language", lang), getTranslationsFromFile("CheckUpdates", lang), getTranslationsFromFile("AboutMSM", lang), getTranslationsFromFile("AboutJava", lang), getTranslationsFromFile("ReportBug", lang)};
            return tmp;
        } else return null;
    }
    public static String getTranslationsFromFile(String property) {
        return getTranslationsFromFile(property, getCurrentLang());
    }

    public static String getTranslationsFromFile(String property, int lang) {
        String prefix, contents = "";
        switch (lang) {
            case 0: 
                prefix = SysConst.getPrePath() + File.separator + "langs" + File.separator + "eng" + File.separator;
                break;
            case 1:
                prefix = SysConst.getPrePath() + File.separator + "langs" + File.separator + "ita" + File.separator;
                break;
            default:
                prefix = SysConst.getPrePath() + File.separator + "langs" + File.separator + "eng" + File.separator;
                break;
        }
        try {
            File lf = new File(prefix + property + ".txt");
            Scanner s = new Scanner(lf);
            do {
                contents = contents + s.nextLine() + '\n';
            } while (s.hasNextLine());
            s.close();
        } catch (FileNotFoundException e) {
            System.out.println("Missing translation: " + property + ".txt");
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
