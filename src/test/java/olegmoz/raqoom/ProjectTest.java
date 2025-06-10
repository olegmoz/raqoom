package olegmoz.raqoom;

import org.assertj.core.api.ThrowableAssert;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

public class ProjectTest {

    @Test
    public void build() {
        // given
        var project = new Project("test-project");

        // when
        ThrowableAssert.ThrowingCallable code = project::build;

        // then
        assertThatCode(code).doesNotThrowAnyException();
    }
}