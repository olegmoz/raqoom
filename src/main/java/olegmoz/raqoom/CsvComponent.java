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
import static olegmoz.raqoom.ClassType.*;

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
        return read().stream().filter(cl -> cl.type() == ACTION).map(cl -> (ClassInfo) cl).toList();
    }

    @Override
    public Collection<ClassInfo> models() {
        return read().stream().filter(cl -> cl.type() == MODEL).map(cl -> (ClassInfo) cl).toList();
    }

    List<CsvClassInfo> read() {
        if (!csv.exists()) {
            throw new IllegalStateException("File does not exist: %s".formatted(csv));
        }

        var classes = new ArrayList<CsvClassInfo>();
        try (var reader = new java.io.BufferedReader(new java.io.FileReader(csv))) {
            String line;
            var lineNo = 0;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                var parts = line.split(",");
                var type = parts.length > 2 ? parseType(parts[2], line, lineNo) : UNKNOWN;
                if (parts.length < 2) {
                    throw new IllegalArgumentException("Incomplete line #%d '%s' ".formatted(lineNo, line));
                }
                var fullName = parts[0];
                var simpleName = parts[1];
                classes.add(new CsvClassInfo(fullName, simpleName, type));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: %s".formatted(csv), e);
        }
        return classes;
    }

    private static ClassType parseType(String part, String line, int lineNo) {
        String value = part.trim().toLowerCase();
        if (value.isEmpty()) {
            return UNKNOWN;
        }
        return switch (value) {
            case "action" -> ACTION;
            case "model" -> MODEL;
            default -> throw new IllegalArgumentException(
                    "Invalid value '%s' in line: #%d '%s' ".formatted(part, lineNo, line));
        };
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
        var type = info.type() != UNKNOWN ? info.type().toString() : "";
        writer.write(format("%s,%s,%s\n", info.fullName(), info.simpleName(), type));
    }

    private static String extractName(File csv) {
        String fileName = csv.getName();
        if (fileName.endsWith(CSV_EXTENSION)) {
            return fileName.substring(0, fileName.length() - CSV_EXTENSION.length());
        }
        return fileName;
    }

    record CsvClassInfo(String fullName, String simpleName, ClassType type) implements ClassInfo {

        public static CsvClassInfo action(ClassInfo base) {
            return new CsvClassInfo(base, ACTION);
        }

        public static CsvClassInfo model(ClassInfo base) {
            return new CsvClassInfo(base, MODEL);
        }

        CsvClassInfo(ClassInfo base, ClassType type) {
            this(base.fullName(), base.simpleName(), type);
        }
    }
}