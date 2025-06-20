package olegmoz.raqoom;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Comparator.comparing;

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
        return read().stream().filter(cl -> cl.isAction).map(cl -> (ClassInfo) cl).toList();
    }

    @Override
    public Collection<ClassInfo> models() {
        return read().stream().filter(cl -> cl.isModel).map(cl -> (ClassInfo) cl).toList();
    }

    List<CsvClassInfo> read() {
        if (!csv.exists()) {
            throw new IllegalStateException("File does not exist: %s".formatted(csv));
        }

        var classes = new ArrayList<CsvClassInfo>();
        try (var reader = new java.io.BufferedReader(new java.io.FileReader(csv))) {
            String line;
            int lineNo = 0;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                String[] parts = line.split(",");
                if (parts.length < 4) {
                    throw new IllegalArgumentException("Incomplete line #%d '%s' ".formatted(lineNo, line));
                }
                boolean isAction = parseBooleanStrict(parts[2], line, lineNo);
                boolean isModel = parseBooleanStrict(parts[3], line, lineNo);
                classes.add(new CsvClassInfo(parts[0], parts[1], isAction, isModel));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: %s".formatted(csv), e);
        }
        return classes;
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
            var classes = Stream.concat(
                    component.actions().stream().map(CsvClassInfo::action),
                    component.models().stream().map(CsvClassInfo::model)
            ).sorted(comparing(ClassInfo::fullName)).toList();
            for (CsvClassInfo cl : classes) {
                write(writer, cl);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to write CSV file", e);
        }
    }

    private void write(FileWriter writer, CsvClassInfo info) throws IOException {
        writer.write(format("%s,%s,%s,%s\n", info.fullName(), info.simpleName(), info.isAction, info.isModel));
    }

    private static String extractName(File csv) {
        String fileName = csv.getName();
        if (fileName.endsWith(CSV_EXTENSION)) {
            return fileName.substring(0, fileName.length() - CSV_EXTENSION.length());
        }
        return fileName;
    }

    record CsvClassInfo(String fullName, String simpleName, boolean isAction, boolean isModel) implements ClassInfo {

        public static CsvClassInfo action(ClassInfo base) {
            return new CsvClassInfo(base, true, false);
        }

        public static CsvClassInfo model(ClassInfo base) {
            return new CsvClassInfo(base, false, true);
        }

        CsvClassInfo(ClassInfo base, boolean isAction, boolean isModel) {
            this(base.fullName(), base.simpleName(), isAction, isModel);
        }
    }
}