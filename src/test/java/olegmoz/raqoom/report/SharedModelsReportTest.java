package olegmoz.raqoom.report;

import olegmoz.raqoom.ClassInfo;
import olegmoz.raqoom.ComponentName;
import olegmoz.raqoom.report.SharedModelsReport.Component;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static olegmoz.raqoom.ClassInfoStub.cl;
import static olegmoz.raqoom.report.SharedModelsReportTest.StubComponent.component;
import static org.assertj.core.api.Assertions.assertThat;

class SharedModelsReportTest {

    @Test
    void write_report_to_file(@TempDir File tempDir) throws Exception {
        // given
        var user = cl("User");
        var account = cl("Account");
        var card = cl("Card");
        var payment = cl("Payment");
        var statement = cl("Statement");
        var a = component("a", payment, account, card);
        var b = component("b", user, account);
        var c = component("c", user, account, card);
        var d = component("d", statement);
        var report = new SharedModelsReport(Set.of(a, b, c, d));
        var file = new File(tempDir, "report.csv");

        // when
        report.write(file);

        // then
        var content = Files.readString(file.toPath());
        assertThat(content).isEqualTo("""
                a,Account,b c
                a,Card,c
                b,Account,a c
                b,User,c
                c,Account,a b
                c,Card,a
                c,User,b
                """);
    }

    static class StubComponent implements Component {

        private final ComponentName name;
        private final List<ClassInfo> models;

        public static StubComponent component(String name, ClassInfo... models) {
            return new StubComponent(name, Arrays.asList(models));
        }

        public StubComponent(String name, List<ClassInfo> models) {
            this.name = new ComponentName(name);
            this.models = models;
        }

        @Override
        public ComponentName name() {
            return name;
        }

        @Override
        public Collection<ClassInfo> models() {
            return models;
        }

        @Override
        public String toString() {
            return name.toString();
        }
    }
}