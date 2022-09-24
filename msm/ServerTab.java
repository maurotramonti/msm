package msm;

import javax.swing.*;
import java.awt.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.io.*;
import java.util.Scanner;


class ServerTab extends JPanel {
    public String title, folder, iconPath;
    public boolean imported, running = false;
    public Process serverProcess;

    private JPanel contents, buttons;   
    private String[] confdata;

    private JLabel titleLabel, ramLabel, javaExeLabel, iconLabel;



    private JTextArea outputArea;
    private JScrollPane outputPane;

    private JTextField commandInput = new JTextField();
    private ServerTab.SendCommandButton sendCommandButton = new ServerTab.SendCommandButton();

    private ServerTab.PrintOutputThread pot;
    private ServerTab.ServerThread serverThread;


    private OutputStream serverProcessInput;

    private ServerTab.LaunchButton launchButton = new ServerTab.LaunchButton();
    private ServerTab.OptionButton optionButton = new ServerTab.OptionButton();

    public final File confFile;
    
    private MSMFrame parentFrame;
    
    ServerTab(File conffile, MSMFrame f) {
        super(new BorderLayout(0, 10));
        setBackground(Color.white);

        parentFrame = f;
        confFile = conffile;
        

        GridBagConstraints gbc = new GridBagConstraints();
        contents = new JPanel(new GridBagLayout()); contents.setBackground(Color.white);
        buttons = new JPanel(new FlowLayout()); buttons.setBackground(Color.white);

        
        confdata = new String[9];

        outputArea = new JTextArea(); outputArea.setRows(6);

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

        if (confdata[7].equals("imported")) imported = true;
        else imported = false;


        gbc.gridy = 0; gbc.gridx = 0; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER; gbc.insets = new Insets(10, 10, 10, 10);
        this.titleLabel = new JLabel(confdata[0], new ImageIcon(new ImageIcon(confdata[1]).getImage().getScaledInstance(48, 48,  Image.SCALE_DEFAULT)), SwingConstants.LEFT); titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        contents.add(titleLabel, gbc);

        // Informazioni sul server

        gbc.gridy = 1; gbc.anchor = GridBagConstraints.LINE_START; gbc.gridwidth = 1;
        contents.add(new JLabel(LanguageManager.getTranslationsFromFile("ServerPath")), gbc);

        gbc.gridy = 2; contents.add(new JLabel(LanguageManager.getTranslationsFromFile("MinecraftVersion")), gbc);

        gbc.gridy = 3; contents.add(new JLabel(LanguageManager.getTranslationsFromFile("JavaPath")), gbc);


        gbc.gridy = 4; contents.add(new JLabel(LanguageManager.getTranslationsFromFile("RamQuantity")), gbc);

        gbc.gridy = 1; gbc.gridx = 1; contents.add(new JLabel(confdata[2]), gbc);

        
        gbc.gridy = 2; contents.add(new JLabel(confdata[3] + " " + confdata[6]), gbc);

        javaExeLabel = new JLabel(confdata[4]);
        gbc.gridy = 3; contents.add(javaExeLabel, gbc);


        ramLabel = new JLabel(confdata[5] + " MB");
        gbc.gridy = 4; contents.add(ramLabel, gbc);

        gbc.gridy = 7; gbc.gridx = 0; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.HORIZONTAL;

        outputArea.setEditable(false); 

        outputPane = new JScrollPane(outputArea); outputArea.setLineWrap(true); 
        outputPane.setVisible(false); 
        
        outputPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        outputPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        outputPane.getVerticalScrollBar().setUnitIncrement(16);

        contents.add(outputPane, gbc); 

        commandInput.setVisible(false); sendCommandButton.setVisible(false); commandInput.setToolTipText(LanguageManager.getTranslationsFromFile("CommandToolTip"));

        gbc.gridy = 6; gbc.gridx = 0; gbc.gridwidth = 2; contents.add(commandInput, gbc);
        gbc.gridx = 2; gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.LINE_END;  contents.add(sendCommandButton, gbc);

        buttons.add(optionButton); buttons.add(launchButton); buttons.add(new ServerTab.OpenServerFolder());

        add(contents, BorderLayout.NORTH);
        add(buttons, BorderLayout.SOUTH);

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
        titleLabel.setIcon(new ImageIcon(new ImageIcon(confdata[1]).getImage().getScaledInstance(48, 48,  Image.SCALE_DEFAULT)));
        parentFrame.tPane.setIconAt(parentFrame.tPane.getSelectedIndex(), new ImageIcon(new ImageIcon(confdata[1]).getImage().getScaledInstance(24, 24, Image.SCALE_DEFAULT)));
    }

    class LaunchButton extends JButton implements ActionListener {
        private boolean activated = false;
        LaunchButton() {
            super(LanguageManager.getTranslationsFromFile("Launch"));
            setBackground(Color.white);
            addActionListener(this);
        }

        @Override 
        public void actionPerformed(ActionEvent e) {
            ProcessBuilder pb = new ProcessBuilder(confdata[4], "-Xmx" + confdata[5] + "M", "-Xms" + confdata[5] + "M", "-jar", confdata[2] + File.separator + "server.jar", "nogui");
            pb.directory(new File(confdata[2]));
            pb.redirectErrorStream(true);
            pb.redirectOutput(new File(confdata[2] + File.separator + "log.txt"));
            serverThread = new ServerTab.ServerThread(pb);
            serverThread.start();
            
            pot = new ServerTab.PrintOutputThread();
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
                sendCommandButton.setVisible(true); commandInput.setVisible(true);
                serverProcess.waitFor();       
                    

            } catch (InterruptedException ex) {
                serverProcess.destroy();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(parentFrame, LanguageManager.getTranslationsFromFile("StartServerError"), LanguageManager.getTranslationsFromFile("Error"), JOptionPane.ERROR_MESSAGE);
            } finally {
                running = false;
                launchButton.setEnabled(true);
                optionButton.setEnabled(true);
                sendCommandButton.setVisible(false); commandInput.setVisible(false);

            }
            
        }
    }

    class PrintOutputThread extends Thread {

        @Override
        public void run() {
            outputPane.setVisible(true);
            parentFrame.pack();   
            boolean firstIteration = true;        
            
            while (serverThread.isAlive() && Thread.currentThread().isInterrupted() == false) {
                try {
                    Scanner scanner = new Scanner(new File(confdata[2] + File.separator + "log.txt"));
                    String s = new String("");
                    while (scanner.hasNextLine()) s = new String(s + scanner.nextLine() + '\n');
                    outputArea.setText(s); outputArea.setRows(6);                                  
                    scanner.close();
                    parentFrame.pack();
                    this.sleep(500); 
                } catch (IOException ex) {
                    ex.printStackTrace();
                } catch (InterruptedException ex) {
                    break;
                }
            }
            outputArea.setText(""); outputPane.setVisible(false); parentFrame.pack();
            
        }
    }

    class OpenServerFolder extends JButton implements ActionListener {
        OpenServerFolder() {
            super(LanguageManager.getTranslationsFromFile("OpenServerFolder"));
            setBackground(Color.white);
            addActionListener(this);
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                Desktop.getDesktop().open(new File(confdata[2]));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    class SendCommandButton extends JButton implements ActionListener {
        SendCommandButton() {
            super(LanguageManager.getTranslationsFromFile("Send"));
            setBackground(Color.white);
            setSize(30, 20);
            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            byte[] command = (commandInput.getText() + '\n').getBytes();
            try {
                serverProcessInput.write(command);
                serverProcessInput.flush(); 
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(parentFrame, LanguageManager.getTranslationsFromFile("SendCommandError"), LanguageManager.getTranslationsFromFile("Error"), JOptionPane.ERROR_MESSAGE);
            } finally {
                commandInput.setText("");
            }
        }
    }

    class OptionButton extends JButton implements ActionListener {
        OptionButton() {
            super(LanguageManager.getTranslationsFromFile("ServerOptions"));
            setBackground(Color.white);
            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            GameOptionsDialog god = new GameOptionsDialog(parentFrame);
        }
    }


}