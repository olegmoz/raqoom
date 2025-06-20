package olegmoz.raqoom.report;

import olegmoz.raqoom.ClassInfo;

import java.io.File;
import java.util.Collection;

public class SharedModelsReport {

    private final SharedClassesReport<Component> delegate;

    public SharedModelsReport(Collection<Component> components) {
        this.delegate = new SharedClassesReport<>(components, Component::models);
    }

    public void write(File csv) {
        delegate.write(csv);
    }

    public interface Component extends SharedClassesReport.Component {
        Collection<ClassInfo> models();
    }
}
