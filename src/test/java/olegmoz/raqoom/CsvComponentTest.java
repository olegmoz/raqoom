package olegmoz.raqoom;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CsvComponentTest {

    @TempDir
    Path tempDir;

    @Test
    public void write_to_non_existing_file() throws IOException {
        // given
        var csvFile = tempDir.resolve("example.csv").toFile();
        var component = new CsvComponent(csvFile);

        // when
        component.write(new TestComponent());

        // then
        assertThat(csvFile).exists();
        List<String> lines = Files.readAllLines(csvFile.toPath());
        assertThat(lines).hasSize(2);
        assertThat(lines.get(0)).isEqualTo("org.example.Action1,Action1,true");
        assertThat(lines.get(1)).isEqualTo("org.example.Action2,Action2,true");
    }

    @Test
    public void write_fails_when_file_exists() throws IOException {
        // given
        var csvFile = tempDir.resolve("test.csv").toFile();
        Files.createFile(csvFile.toPath());
        var csvComponent = new CsvComponent(csvFile);
        var component = new Component() {
            @Override
            public Collection<ClassInfo> actions() {
                return List.of();
            }
        };

        // then
        assertThatThrownBy(() -> csvComponent.write(component))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("File already exists");
    }

    @Test
    public void actions_throws_exception_when_csv_does_not_exist() {
        // given
        var csvFile = tempDir.resolve("nonexistent.csv").toFile();
        var component = new CsvComponent(csvFile);

        // then
        assertThatThrownBy(component::actions)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("File does not exist");
    }

    @Test
    public void actions_returns_only_actions() throws IOException {
        // given
        var csvFile = tempDir.resolve("mixed.csv").toFile();
        var csvContent = """
            org.example.Action1,Action1,true
            org.example.NotAction,NotAction,false
            org.example.Action2,Action2,true
            """;
        Files.writeString(csvFile.toPath(), csvContent);
        var component = new CsvComponent(csvFile);

        // when
        var actions = component.actions();

        // then
        assertThat(actions).hasSize(2);
        var fullNames = actions.stream().map(ClassInfo::fullName).toList();
        assertThat(fullNames).containsExactlyInAnyOrder("org.example.Action1", "org.example.Action2");
    }

    @Test
    public void actions_provides_correct_class_info() throws IOException {
        // given
        var csvFile = tempDir.resolve("test.csv").toFile();
        var csvContent = """
            org.example.CreateAction,CreateAction,true
            org.example.UpdateAction,UpdateAction,true
            """;
        Files.writeString(csvFile.toPath(), csvContent);
        var component = new CsvComponent(csvFile);

        // when
        var actions = component.actions();

        // then
        assertThat(actions).hasSize(2);
        var createAction = actions.stream()
            .filter(a -> a.fullName().equals("org.example.CreateAction"))
            .findFirst()
            .orElseThrow();
        assertThat(createAction.simpleName()).isEqualTo("CreateAction");
        var updateAction = actions.stream()
            .filter(a -> a.fullName().equals("org.example.UpdateAction"))
            .findFirst()
            .orElseThrow();
        assertThat(updateAction.simpleName()).isEqualTo("UpdateAction");
    }

    @Test
    public void actions_throws_when_line_incomplete() throws IOException {
        // given
        var csvFile = tempDir.resolve("incomplete_line.csv").toFile();
        var csvContent = """
            org.example.Action1,Action1,true
            incomplete,line
            org.example.Action2,Action2,true
            """;
        Files.writeString(csvFile.toPath(), csvContent);
        var component = new CsvComponent(csvFile);

        // then
        assertThatThrownBy(component::actions)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Incomplete line");
    }

    @Test
    public void actions_throws_when_action_row_malfromed() throws IOException {
        // given
        var csvFile = tempDir.resolve("action_malformed.csv").toFile();
        var csvContent = """
            org.example.Action,Action,yes
            """;
        Files.writeString(csvFile.toPath(), csvContent);
        var component = new CsvComponent(csvFile);

        // then
        assertThatThrownBy(component::actions)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid boolean value");
    }

    private static class TestComponent implements Component {
        @Override
        public Collection<ClassInfo> actions() {
            return List.of(
                    new ClassInfoStub("org.example.Action1", "Action1", "Action1"),
                    new ClassInfoStub("org.example.Action2", "Action2", "Action2")
            );
        }
    }
}