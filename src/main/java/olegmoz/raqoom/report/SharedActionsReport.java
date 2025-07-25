package olegmoz.raqoom.report;

import olegmoz.raqoom.ClassInfo;

import java.io.File;
import java.util.Collection;

public class SharedActionsReport {

    private final SharedClassesReport<Component> delegate;

    public SharedActionsReport(Collection<Component> components) {
        this.delegate = new SharedClassesReport<>(components, Component::actions);
    }

    public void write(File csv) {
        delegate.write(csv);
    }

    public interface Component extends SharedClassesReport.Component {
        Collection<ClassInfo> actions();
    }
}
