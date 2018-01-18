package ichttt.mclang2json;

import com.google.gson.stream.JsonWriter;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Lang2JsonConverter {
    public enum FileParseResult {
        SUCCESS, NO_LANG_FILES, ABORT, ERRORS
    }

    public static Path prevPath = Paths.get(".");

    public static void main(String[] args) {
        LangConverterGui.init();
    }

    public static FileParseResult parseFolder(JFrame parent) {
        JFileChooser chooser = new JFileChooser(prevPath.toFile());
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
            File dir = chooser.getSelectedFile();
            if (!dir.isDirectory()) {
                System.err.println("File " + dir + " is not directory");
                return FileParseResult.ERRORS;
            }
            File[] files = dir.listFiles();
            if (files == null || files.length == 0)
                return FileParseResult.NO_LANG_FILES;
            boolean didRun = false;
            boolean errors = false;
            for (File f : files) {
                String fileName = f.toString();
                if (!fileName.endsWith(".lang"))
                    continue;
                Path output = getOutput(f);
                try {
                    parse(f, output.toFile());
                } catch (IOException e) {
                    System.err.println("Could not convert file: IO error");
                    e.printStackTrace();
                    errors = true;
                } catch (RuntimeException e) {
                    System.err.println("Could not convert file: Unknown error. Invalid lang file?");
                    e.printStackTrace();
                    errors = true;
                }
                didRun = true;
            }
            if (errors)
                return FileParseResult.ERRORS;
            if (didRun)
                return FileParseResult.SUCCESS;
            else
                return FileParseResult.NO_LANG_FILES;
        }
        return FileParseResult.ABORT;
    }

    public static String parseFile(JFrame parent) throws IOException {
        JFileChooser chooser = new JFileChooser(prevPath.toFile());
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.getName().endsWith(".lang") || f.isDirectory();
            }

            @Override
            public String getDescription() {
                return "Minecraft language file";
            }
        });

        if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            Path output = getOutput(file);
            parse(file, output.toFile());
            return output.toString();
        }
        return null;
    }

    private static Path getOutput(File file) {
        String fileString = file.toString();
        if (fileString.endsWith(".lang"))
            fileString = fileString.substring(0, fileString.length() - ".lang".length());
        fileString = fileString.concat(".json");
        Path outputPath = Paths.get(fileString);
        if (file.isFile())
            prevPath = outputPath.getParent();
        else
            prevPath = outputPath;
        return outputPath;
    }

    private static void parse(File file, File output) throws IOException {
        BufferedReader reader = null;
        JsonWriter writer = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            writer = new JsonWriter(new FileWriter(output));
            writer.beginObject();
            String BASE_INTENT = LangConverterGui.getIntent();
            writer.setIndent(BASE_INTENT);
            boolean extraIndent = false;
            String line;
            while ((line = reader.readLine()) != null) {
                String[] split = line.split("=");
                if (line.trim().isEmpty()) {
                    writer.setIndent("\n" + BASE_INTENT);
                    extraIndent = true;
                    continue;
                }

                if (split.length < 2)
                    throw new RuntimeException("Invalid line " + line);

                String key = remapKey(split[0]);
                StringBuilder value = new StringBuilder();
                for (int i = 1; i < split.length; i++)
                    value.append(split[i]);

                writer.name(key).value(value.toString());
                if (extraIndent) {
                    extraIndent = false;
                    writer.setIndent(BASE_INTENT);
                }
            }
            writer.endObject();
            writer.flush();
        } finally {
            tryClose(reader);
            tryClose(writer);
        }
    }

    private static String remapKey(String key) {
        if (key.startsWith("item.") && key.endsWith(".name")) {
            return key.substring(0, key.length() - ".name".length()); //very basic remapping
        } else if (key.startsWith("tile.") && key.endsWith(".name")) {
            return "block".concat(key.substring("tile".length(), key.length() - ".name".length()));
        }
        return key;
    }

    private static void tryClose(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                System.err.println("Error closing file!");
                e.printStackTrace();
            }
        }
    }
}
