package olegmoz.raqoom.report;

import olegmoz.raqoom.ClassInfo;
import olegmoz.raqoom.ComponentName;
import olegmoz.raqoom.report.SharedActionsReport.Component;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static olegmoz.raqoom.ClassInfoStub.cl;
import static olegmoz.raqoom.report.SharedActionsReportTest.StubComponent.component;
import static org.assertj.core.api.Assertions.assertThat;

class SharedActionsReportTest {

    @Test
    void write_report_to_file(@TempDir File tempDir) throws Exception {
        // given
        var create = cl("Create");
        var update = cl("Update");
        var delete = cl("Delete");
        var reset = cl("Reset");
        var terminate = cl("Terminate");
        var a = component("a", reset, update, delete);
        var b = component("b", create, update);
        var c = component("c", create, update, delete);
        var d = component("d", terminate);
        var report = new SharedActionsReport(Set.of(a, b, c, d));
        var file = new File(tempDir, "report.csv");

        // when
        report.write(file);

        // then
        var content = Files.readString(file.toPath());
        assertThat(content).isEqualTo("""
                a,Delete,c
                a,Update,b c
                b,Create,c
                b,Update,a c
                c,Create,b
                c,Delete,a
                c,Update,a b
                """);
    }

    static class StubComponent implements Component {

        private final ComponentName name;
        private final List<ClassInfo> actions;

        public static StubComponent component(String name, ClassInfo... actions) {
            return new StubComponent(name, Arrays.asList(actions));
        }

        public StubComponent(String name, List<ClassInfo> actions) {
            this.name = new ComponentName(name);
            this.actions = actions;
        }

        @Override
        public ComponentName name() {
            return name;
        }

        @Override
        public Collection<ClassInfo> actions() {
            return actions;
        }

        @Override
        public String toString() {
            return name.toString();
        }
    }
}