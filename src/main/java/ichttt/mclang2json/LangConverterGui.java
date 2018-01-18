package ichttt.mclang2json;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class LangConverterGui implements ActionListener {
    private static final LangConverterGui INSTANCE = new LangConverterGui();
    private static final String[] ALLOWED_INTENT = new String[] {"2x Space", "4x Space", "1x Tab"}; //remember to sync getIntent
    private static final JFrame frame = new JFrame("Minecraft Lang2Json Converter");
    private static final JPanel panel = new JPanel();
    private static final JLabel intendLabel = new JLabel("Intend:");
    private static final JComboBox<String> intents = new JComboBox<>(ALLOWED_INTENT);
    private static final JButton exit = setupButton("exit");
    private static final JButton convertFolder = setupButton("convert folder");
    private static final JButton convertFile = setupButton("convert file");

    public static void init() {
        panel.setLayout(new GridBagLayout());
        frame.setContentPane(panel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        GridBagConstraints layout = new GridBagConstraints();
        layout.gridx = 1;
        layout.gridy = 3;
        layout.gridwidth = 2;
        layout.weightx = 1;
        layout.weighty = 0.3;
        layout.fill = GridBagConstraints.BOTH;
        panel.add(exit, layout);

        layout.gridy = 1;
        layout.gridwidth = 1;
        layout.weightx = 0.5;
        panel.add(convertFolder, layout);

        layout.gridx = 2;
        panel.add(convertFile, layout);

        layout.gridy = 2;
        layout.gridx = 1;
        layout.weightx = 0.3;
        panel.add(intendLabel, layout);

        layout.weightx = 0.7;
        layout.gridx = 2;
        panel.add(intents, layout);

        frame.setMinimumSize(new Dimension(160, 100));
        frame.setPreferredSize(new Dimension(480, 270));
        frame.pack();
        frame.setVisible(true);
    }

    private static JButton setupButton(String name) {
        JButton button = new JButton(name);
        button.addActionListener(INSTANCE);
        return button;
    }

    public static String getIntent() {
        String intent = ALLOWED_INTENT[intents.getSelectedIndex()];
        switch (intent) {
            case "2x Space":
                return "  ";
            case "4x Space":
                return "    ";
            case "1x Tab":
                return "\t";
            default:
                throw new RuntimeException("Unknown Intent " + intent);
        }
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == exit) {
            System.exit(0);
        } else if (event.getSource() == convertFolder) {
            Lang2JsonConverter.FileParseResult result = Lang2JsonConverter.parseFolder(frame);
            switch (result) {
                case SUCCESS:
                    JOptionPane.showMessageDialog(frame, "Successfully converted all files in the folder " + Lang2JsonConverter.prevPath);
                    break;
                case NO_LANG_FILES:
                    JOptionPane.showMessageDialog(frame, "Could not find any lang files in the folder " + Lang2JsonConverter.prevPath, "Could not convert", JOptionPane.WARNING_MESSAGE);
                    break;
                case ABORT:
                    JOptionPane.showMessageDialog(frame, "Action cancelled as no directory has been chosen");
                    break;
                case ERRORS:
                    JOptionPane.showMessageDialog(frame, "There were errors parsing some files. Please see the log for more information", "Could not convert", JOptionPane.ERROR_MESSAGE);
                    break;
            }
        } else if (event.getSource() == convertFile) {
            String newFileName = saveParseFile();
            if (newFileName != null)
                JOptionPane.showMessageDialog(frame, "Successfully created " + newFileName);
        }
    }

    private static String saveParseFile() {
        try {
            return Lang2JsonConverter.parseFile(frame);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Could not convert file: IO error" +
                    "\n" + e.getLocalizedMessage(), "Error converting file", JOptionPane.ERROR_MESSAGE);
        } catch (RuntimeException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Could not convert file: Unknown error. Invalid lang file?" +
                    "\n" + e.getLocalizedMessage(), "Error converting file", JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }
}
