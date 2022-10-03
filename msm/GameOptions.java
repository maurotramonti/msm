package msm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;


class GameOptionsDialog extends JDialog {
    final JDialog dialog;


    String currentGamemode, currentDifficulty;
    final File serverProperties;

    int currentPlayers;

    String[] serverPropertiesLines = new String[57];

    final int GAMEMODE_INDEX = 5; final int DIFFICULTY_INDEX = 16; final int PVP_INDEX = 13; final int MOTD_INDEX = 11; final int PLAYERS_INDEX = 21; final int FLIGHT_INDEX = 24;

    JSpinner playerNumber;
    JComboBox<String> difficulty, gamemode;
    final JCheckBox pvp = new JCheckBox(), flight = new JCheckBox();
    final JTextField serverDescr = new JTextField();



    GameOptionsDialog(MSMFrame parent) {
        super(parent, LanguageManager.getTranslationsFromFile("GameOptions"), true);
        dialog = this;
        this.setLayout(new BorderLayout());
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        serverProperties = new File(((ServerTab) parent.tPane.getSelectedComponent()).folder + File.separator + "server.properties");

        JPanel contents = new JPanel(new GridBagLayout()); contents.setBackground(Color.white);
        JPanel buttons = new JPanel(new FlowLayout()); buttons.setBackground(Color.white);

        GridBagConstraints gbc = new GridBagConstraints();

        Scanner s;
        try {
            s = new Scanner(serverProperties);
            for (int i = 0; i < 57; i++) serverPropertiesLines[i] = s.nextLine();
            currentGamemode = serverPropertiesLines[GAMEMODE_INDEX].replace("gamemode=", "");
            currentDifficulty = serverPropertiesLines[DIFFICULTY_INDEX].replace("difficulty=", "");
            if (serverPropertiesLines[PVP_INDEX].contains("true")) pvp.setSelected(true);
            if (serverPropertiesLines[FLIGHT_INDEX].contains("true")) flight.setSelected(true);
            serverDescr.setText(serverPropertiesLines[MOTD_INDEX].replace("motd=", ""));  
            currentPlayers = Integer.parseInt(serverPropertiesLines[PLAYERS_INDEX].replace("max-players=", ""));         

            s.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }

        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.LINE_START; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(10, 10, 10, 10);
        contents.add(new JLabel(LanguageManager.getTranslationsFromFile("PlayerNumber")), gbc);

        gbc.gridy = 1; contents.add(new JLabel(LanguageManager.getTranslationsFromFile("Difficulty")), gbc);
        gbc.gridy = 2; contents.add(new JLabel(LanguageManager.getTranslationsFromFile("Gamemode")), gbc);
        gbc.gridy = 3; contents.add(new JLabel("PVP: "), gbc);
        gbc.gridy = 4; contents.add(new JLabel(LanguageManager.getTranslationsFromFile("AllowFlight")), gbc);
        gbc.gridy = 5; contents.add(new JLabel(LanguageManager.getTranslationsFromFile("ServerDescription")), gbc);
        gbc.gridy = 6; gbc.gridwidth = 2; contents.add(new EditMoreOptionsButton(), gbc);

        playerNumber = new JSpinner(new SpinnerNumberModel(currentPlayers, 1, 100, 1));
        gbc.gridy = 0; gbc.gridx = 1; gbc.gridwidth = 1; contents.add(playerNumber);

        final String[] difficulties = {"peaceful", "easy", "normal", "hard"};

        difficulty = new JComboBox<String>(difficulties); difficulty.setSelectedItem(currentDifficulty);
        gbc.gridy = 1; contents.add(difficulty, gbc);

        final String[] gamemodes = {"creative", "survival", "adventure"};

        gamemode = new JComboBox<String>(gamemodes); gamemode.setSelectedItem(currentGamemode);
        gbc.gridy = 2; contents.add(gamemode, gbc);

        pvp.setBackground(Color.white);
        gbc.gridy = 3; contents.add(pvp, gbc);

        flight.setBackground(Color.white);
        gbc.gridy = 4; contents.add(flight, gbc);

        gbc.gridy = 5; gbc.ipadx = 18; contents.add(serverDescr, gbc);
        


        buttons.add(new CancelButton()); buttons.add(new ConfirmButton()); 

        this.add(contents, BorderLayout.NORTH); this.add(buttons, BorderLayout.SOUTH);
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
            
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(serverProperties));
                for (int i = 0; i < 57; i++) {
                    if (i == GAMEMODE_INDEX) {
                        bw.write("gamemode=" + (String) gamemode.getSelectedItem() + '\n');
                        continue;
                    }
                    else if (i == MOTD_INDEX) {
                        bw.write("motd=" + serverDescr.getText() + '\n');
                        continue;
                    }
                    else if (i == PVP_INDEX) {
                        if (pvp.isSelected()) bw.write("pvp=true\n");
                        else bw.write("pvp=false\n");                        
                        continue;
                    }
                    else if (i == DIFFICULTY_INDEX) {
                        bw.write("difficulty=" + (String) difficulty.getSelectedItem() + '\n');
                        continue;
                    }
                    else if (i == PLAYERS_INDEX) {
                        bw.write("max-players=" +  Integer.toString((Integer) playerNumber.getValue()) + '\n');
                        continue;
                    }
                    else if (i == FLIGHT_INDEX) {
                        if (flight.isSelected()) bw.write("allow-flight=true\n");
                        else bw.write("allow-flight=false\n");                        
                        continue;
                    }
                    bw.write(serverPropertiesLines[i] + "\n");
                    
                }
                bw.close();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(dialog, LanguageManager.getTranslationsFromFile("GameOptionsError"), LanguageManager.getTranslationsFromFile("Error"), JOptionPane.ERROR_MESSAGE);
            } finally {
                
                dialog.dispose();
            }
            
        }
    }

    class EditMoreOptionsButton extends JButton implements ActionListener {
        EditMoreOptionsButton() {
            super(LanguageManager.getTranslationsFromFile("MoreOptions"));
            setBackground(Color.white);
            addActionListener(this);
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                Desktop.getDesktop().open(serverProperties);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(dialog, LanguageManager.getTranslationsFromFile("UnexpectedError"), LanguageManager.getTranslationsFromFile("Error"), JOptionPane.ERROR_MESSAGE);
                return;
            }
            dialog.dispose();
        }
    }

}