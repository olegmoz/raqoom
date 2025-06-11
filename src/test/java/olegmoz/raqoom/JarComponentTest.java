package olegmoz.raqoom;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JarComponentTest {

    @Test
    public void list_all_classes() {
        // given
        var project = new Project("test-project");
        project.build();
        var component = new JarComponent(project.jar());

        // when
        var classes = component.classes();

        // then
        assertThat(classes).hasSize(1);
        var cl = classes.iterator().next();
        assertThat(cl.fullName()).isEqualTo("org.example.Main");
        assertThat(cl.simpleName()).isEqualTo("Main");
    }
}