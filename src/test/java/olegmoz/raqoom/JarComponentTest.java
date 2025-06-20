package olegmoz.raqoom;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;

public class JarComponentTest {

    static Project PROJECT;

    JarComponent component;

    @BeforeAll
    public static void beforeClass() {
        PROJECT = new Project("test-project");
        PROJECT.build();
    }

    @BeforeEach
    public void setUp() {
        component = new JarComponent("test", PROJECT.jar());
    }

    @Test
    public void return_component_name() {
        // when
        var name = component.name();

        // then
        assertThat(name.value()).isEqualTo("test");
    }

    @Test
    public void list_all_classes() {
        // when
        var classes = component.classes();

        // then
        var names = classes.stream().map(ClassInfo::fullName).collect(toSet());
        assertThat(names).isEqualTo(Set.of(
                "org.example.Main",
                "org.example.Action",
                "org.example.SomeAction",
                "org.example.Model",
                "org.example.SomeModel"));
    }

    @ParameterizedTest
    @CsvSource({
            "org.example.Main,Main",
            "org.example.Action,Action",
            "org.example.SomeAction,SomeAction"
    })
    public void provide_correct_simple_names(String full, String simple) {
        // when
        var classes = component.classes();
        var clazz = classes.stream()
                .filter(c -> c.fullName().equals(full))
                .findAny()
                .orElseThrow();

        // when
        var actual = clazz.simpleName();

        // then
        assertThat(actual).isEqualTo(simple);
    }

    @ParameterizedTest
    @CsvSource({
            "Main, false",
            "Action, false",
            "SomeAction, true"
    })
    public void identify_actions(String cl, boolean expected) {
        // given
        var classes = component.classes();
        var clazz = classes.stream()
                .filter(c -> c.simpleName().equals(cl))
                .findAny()
                .orElseThrow();

        // when
        var actual = clazz.isAction();

        // then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void list_actions() {
        // when
        var actions = component.actions();

        // then
        assertThat(actions).hasSize(1);
        var names = actions.stream().map(ClassInfo::fullName).collect(toSet());
        assertThat(names).isEqualTo(Set.of("org.example.SomeAction"));
    }

    @ParameterizedTest
    @CsvSource({
            "Main, false",
            "Model, false",
            "SomeModel, true"
    })
    public void identify_models(String cl, boolean expected) {
        // given
        var classes = component.classes();
        var clazz = classes.stream()
                .filter(c -> c.simpleName().equals(cl))
                .findAny()
                .orElseThrow();

        // when
        var actual = clazz.isModel();

        // then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void list_models() {
        // when
        var models = component.models();

        // then
        assertThat(models).hasSize(1);
        var names = models.stream().map(ClassInfo::fullName).collect(toSet());
        assertThat(names).isEqualTo(Set.of("org.example.SomeModel"));
    }
}