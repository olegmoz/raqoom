package olegmoz.raqoom;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class CsvComponent implements Component {

    private static final String CSV_EXTENSION = ".csv";

    private final File csv;
    private final ComponentName name;

    public CsvComponent(File csv) {
        this.csv = csv;
        this.name = new ComponentName(extractName(csv));
    }

    @Override
    public ComponentName name() {
        return name;
    }

    @Override
    public Collection<ClassInfo> actions() {
        if (!csv.exists()) {
            throw new IllegalStateException("File does not exist: %s".formatted(csv));
        }

        var actions = new ArrayList<ClassInfo>();
        try (var reader = new java.io.BufferedReader(new java.io.FileReader(csv))) {
            String line;
            int lineNo = 0;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                String[] parts = line.split(",");
                if (parts.length < 3) {
                    throw new IllegalArgumentException("Incomplete line #%d '%s' ".formatted(lineNo, line));
                }
                boolean isAction = parseBooleanStrict(parts[2], line, lineNo);
                if (isAction) {
                    actions.add(new CsvClassInfo(parts[0], parts[1]));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: %s".formatted(csv), e);
        }
        return actions;
    }

    private static boolean parseBooleanStrict(String part, String line, int lineNo) {
        String value = part.trim();
        if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
            throw new IllegalArgumentException("Invalid boolean value '%s' in line: #%d '%s' ".formatted(value, lineNo, line));
        }
        return Boolean.parseBoolean(value);
    }

    public void write(Component component) {
        if (csv.exists()) {
            throw new IllegalStateException("File already exists: %s".formatted(csv));
        }
        try (FileWriter writer = new FileWriter(csv)) {
            for (ClassInfo info : component.actions()) {
                writeAction(writer, info);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to write CSV file", e);
        }
    }

    private void writeAction(FileWriter writer, ClassInfo info) throws IOException {
        writer.write(String.format("%s,%s,%s\n", info.fullName(), info.simpleName(), "true"));
    }

    private static String extractName(File csv) {
        String fileName = csv.getName();
        if (fileName.endsWith(CSV_EXTENSION)) {
            return fileName.substring(0, fileName.length() - CSV_EXTENSION.length());
        }
        return fileName;
    }

    private static class CsvClassInfo implements ClassInfo {
        private final String fullName;
        private final String simpleName;

        public CsvClassInfo(String fullName, String simpleName) {
            this.fullName = fullName;
            this.simpleName = simpleName;
        }

        @Override
        public String fullName() {
            return fullName;
        }

        @Override
        public String simpleName() {
            return simpleName;
        }
    }
}