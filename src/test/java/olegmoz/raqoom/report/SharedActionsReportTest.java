package olegmoz.raqoom.report;

import olegmoz.raqoom.ClassInfo;
import olegmoz.raqoom.report.SharedActionsReport.Component;
import olegmoz.raqoom.report.SharedActionsReport.Violation;
import org.junit.jupiter.api.Test;

import java.util.*;

import static olegmoz.raqoom.ClassInfoStub.cl;
import static olegmoz.raqoom.report.SharedActionsReportTest.StubComponent.component;
import static org.assertj.core.api.Assertions.assertThat;

class SharedActionsReportTest {

    private final SharedActionsReport subj = new SharedActionsReport();

    @Test
    public void report_no_violations_when_no_actions() {
        // given
        Set<Component> components = Set.of(component("a"), component("b"));

        // when
        var violations = subj.report(components);

        // then
        assertThat(violations).isEmpty();
    }

    @Test
    public void report_some_violations() {
        // given
        var create = cl("Create");
        var update = cl("Update");
        var delete = cl("Delete");
        var reset = cl("Reset");
        var a = component("a", create, update, delete);
        var b = component("b", update, reset);
        var c = component("c", update, delete, reset);

        // when
        var violations = subj.report(Set.of(a, b, c));

        // then
        assertThat(violations).hasSize(3);
        assertThat(violations.stream().filter(v -> v.action() == update).findAny())
                .isEqualTo(Optional.of(new Violation(update, Set.of(a, b, c))));
        assertThat(violations.stream().filter(v -> v.action() == delete).findAny())
                .isEqualTo(Optional.of(new Violation(delete, Set.of(a, c))));
        assertThat(violations.stream().filter(v -> v.action() == reset).findAny())
                .isEqualTo(Optional.of(new Violation(reset, Set.of(b, c))));
    }

    static class StubComponent implements Component {

        private final String name;
        private final List<ClassInfo> actions;

        public static StubComponent component(String name, ClassInfo... actions) {
            return new StubComponent(name, Arrays.asList(actions));
        }

        public StubComponent(String name, List<ClassInfo> actions) {
            this.name = name;
            this.actions = actions;
        }

        @Override
        public Collection<ClassInfo> actions() {
            return actions;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}