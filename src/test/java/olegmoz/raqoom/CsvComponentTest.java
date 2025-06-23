package olegmoz.raqoom;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

import static olegmoz.raqoom.ClassInfoStub.cl;
import static olegmoz.raqoom.ClassType.ACTION;
import static olegmoz.raqoom.ClassType.MODEL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CsvComponentTest {

    @TempDir
    Path tempDir;

    @Test
    public void return_component_name() {
        // given
        var csvFile = tempDir.resolve("some_name.csv").toFile();
        var component = new CsvComponent(csvFile);

        // when
        var name = component.name();

        // then
        assertThat(name.value()).isEqualTo("some_name");
    }

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
        assertThat(lines).hasSize(4);
        assertThat(lines.get(0)).isEqualTo("org.example.AccountModel,AccountModel,MODEL");
        assertThat(lines.get(1)).isEqualTo("org.example.CreateAction,CreateAction,ACTION");
        assertThat(lines.get(2)).isEqualTo("org.example.DeleteAction,DeleteAction,ACTION");
        assertThat(lines.get(3)).isEqualTo("org.example.UserModel,UserModel,MODEL");
    }

    @Test
    public void write_fails_when_file_exists() throws IOException {
        // given
        var csvFile = tempDir.resolve("test.csv").toFile();
        Files.createFile(csvFile.toPath());
        var csvComponent = new CsvComponent(csvFile);
        var component = new TestComponent();

        // then
        assertThatThrownBy(() -> csvComponent.write(component))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("File already exists");
    }

    @Test
    public void read_throws_exception_when_csv_does_not_exist() {
        // given
        var csvFile = tempDir.resolve("nonexistent.csv").toFile();
        var component = new CsvComponent(csvFile);

        // then
        assertThatThrownBy(component::read)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("File does not exist");
    }

    @Test
    public void read_provides_correct_class_info() throws IOException {
        // given
        var csvFile = tempDir.resolve("test.csv").toFile();
        var csvContent = """
                org.example.CreateAction,CreateAction,ACTION
                org.example.UserModel,UserModel,MODEL
                """;
        Files.writeString(csvFile.toPath(), csvContent);
        var component = new CsvComponent(csvFile);

        // when
        var classes = component.read();

        // then
        assertThat(classes).hasSize(2);
        var createAction = classes.stream()
                .filter(a -> a.fullName().equals("org.example.CreateAction"))
                .findFirst()
                .orElseThrow();
        assertThat(createAction.simpleName()).isEqualTo("CreateAction");
        assertThat(createAction.type()).isEqualTo(ACTION);
        var userModel = classes.stream()
                .filter(a -> a.fullName().equals("org.example.UserModel"))
                .findFirst()
                .orElseThrow();
        assertThat(userModel.simpleName()).isEqualTo("UserModel");
        assertThat(userModel.type()).isEqualTo(MODEL);
    }

    @Test
    public void read_throws_when_line_incomplete() throws IOException {
        // given
        var csvFile = tempDir.resolve("incomplete_line.csv").toFile();
        var csvContent = """
                org.example.Action1,Action1,ACTION
                incomplete line
                org.example.Model1,Model1,MODEL
                """;
        Files.writeString(csvFile.toPath(), csvContent);
        var component = new CsvComponent(csvFile);

        // then
        assertThatThrownBy(component::read)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Incomplete line #2")
                .hasMessageContaining("incomplete line");
    }

    @Test
    public void read_throws_when_row_malformed() throws IOException {
        // given
        var csvFile = tempDir.resolve("row_malformed.csv").toFile();
        var csvContent = """
                org.example.Action,Action,SOMETHING
                """;
        Files.writeString(csvFile.toPath(), csvContent);
        var component = new CsvComponent(csvFile);

        // then
        assertThatThrownBy(component::read)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid value");
    }

    @Test
    public void actions_returns_only_actions() throws IOException {
        // given
        var csvFile = tempDir.resolve("mixed.csv").toFile();
        var csvContent = """
                org.example.Action1,Action1,ACTION
                org.example.NotAction,NotAction,
                org.example.Action2,Action2,ACTION
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
    public void models_returns_only_models() throws IOException {
        // given
        var csvFile = tempDir.resolve("mixed.csv").toFile();
        var csvContent = """
                org.example.Model1,Model1,MODEL
                org.example.NotModel,NotModel,
                org.example.Model2,Model2,MODEL
                """;
        Files.writeString(csvFile.toPath(), csvContent);
        var component = new CsvComponent(csvFile);

        // when
        var models = component.models();

        // then
        assertThat(models).hasSize(2);
        var fullNames = models.stream().map(ClassInfo::fullName).toList();
        assertThat(fullNames).containsExactlyInAnyOrder("org.example.Model1", "org.example.Model2");
    }

    private static class TestComponent implements Component {
        @Override
        public ComponentName name() {
            return new ComponentName("test");
        }

        @Override
        public Collection<ClassInfo> actions() {
            return List.of(cl("CreateAction"), cl("DeleteAction"));
        }

        @Override
        public Collection<ClassInfo> models() {
            return List.of(cl("UserModel"), cl("AccountModel"));
        }
    }
}