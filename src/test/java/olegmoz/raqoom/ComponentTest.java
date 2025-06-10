package olegmoz.raqoom;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ComponentTest {

    @Test
    public void list_all_classes() {
        // given
        var project = new Project("test-project");
        project.build();
        var component = new Component(project.jar());

        // when
        var classes = component.classes();

        // then
        assertThat(classes).hasSize(1);
        var cl = classes.iterator().next();
        assertThat(cl.getName()).isEqualTo("org.example.Main");
    }
}