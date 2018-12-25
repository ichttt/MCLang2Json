package ichttt.mclang2json;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class LangConverterGui implements ActionListener {
    private static final LangConverterGui INSTANCE = new LangConverterGui();
    private static final String[] ALLOWED_INTENT = new String[] {"2x Space", "4x Space", "1x Tab"}; //remember to sync getIntent
    private static final JFrame frame = new JFrame("Minecraft Lang2Json Converter - Version " + Lang2JsonConverter.VERSION);
    private static final JPanel panel = new JPanel();
    private static final JLabel intendLabel = setupBase(new JLabel("Intend:"));
    private static final JComboBox<String> intents = setupBase(new JComboBox<>(ALLOWED_INTENT));
    private static final JButton convertFolder = setupButton("convert folder");
    private static final JButton convertFile = setupButton("convert file");
    private static final JCheckBox keepComment = setupBase(new JCheckBox("Keep Comments"));

    public static void init() {
        panel.setLayout(new GridBagLayout());
        frame.setContentPane(panel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        GridBagConstraints layout = new GridBagConstraints();
        layout.gridx = 1;
        layout.weighty = 0.5;
        layout.fill = GridBagConstraints.BOTH;
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

        layout.gridy = 3;
        layout.gridx = 1;
        layout.weighty = 0.2;
        layout.gridwidth = 2;
        keepComment.setSelected(true);
        keepComment.setToolTipText("Converts lang file comments (lines starting with #) to json with key _comment.\nThis can produce jsons with duplicate _comment keys");
        panel.add(keepComment, layout);

        frame.setMinimumSize(new Dimension(160, 100));
        frame.setPreferredSize(new Dimension(480, 270));
        frame.pack();
        frame.setVisible(true);
    }

    private static JButton setupButton(String name) {
        JButton button = new JButton(name);
        button.addActionListener(INSTANCE);
        return setupBase(button);
    }

    private static <T extends Component> T setupBase(T base) {
        Font font = base.getFont();
        base.setFont(new Font(font.getName(), font.getStyle(), 24));
        return base;
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
        if (event.getSource() == convertFolder) {
            Lang2JsonConverter.FileParseResult result = Lang2JsonConverter.parseFolder(frame, keepComment.isSelected());
            switch (result) {
                case SUCCESS:
                    JOptionPane.showMessageDialog(frame, "Successfully converted all files in the folder " + Lang2JsonConverter.prevPath);
                    break;
                case NO_LANG_FILES:
                    JOptionPane.showMessageDialog(frame, "Could not find any lang files in the folder " + Lang2JsonConverter.prevPath, "Could not convert", JOptionPane.WARNING_MESSAGE);
                    break;
                case ABORT:
                    System.out.println("Canceling");
//                    JOptionPane.showMessageDialog(frame, "Action cancelled as no directory has been chosen");
                    break;
                case ERRORS:
                    JOptionPane.showMessageDialog(frame, "There were errors parsing some files. Please see the log for more information", "Could not convert", JOptionPane.ERROR_MESSAGE);
                    break;
            }
        } else if (event.getSource() == convertFile) {
            String newFileName = safeParseFile();
            if (newFileName != null)
                JOptionPane.showMessageDialog(frame, "Successfully created " + newFileName);
        }
    }

    private static String safeParseFile() {
        try {
            return Lang2JsonConverter.parseFile(frame, keepComment.isSelected());
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
