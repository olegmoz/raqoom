package olegmoz.raqoom.report;

import olegmoz.raqoom.ClassInfo;
import olegmoz.raqoom.ComponentName;

import java.io.File;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static olegmoz.raqoom.report.SharedClassesReport.Component;

class SharedClassesReport<T extends Component> {

    private final Collection<T> components;
    private final Function<T, Collection<ClassInfo>> classesExtractor;

    SharedClassesReport(Collection<T> components, Function<T, Collection<ClassInfo>> classesExtractor) {
        this.components = components;
        this.classesExtractor = classesExtractor;
    }

    void write(File csv) {
        try (var writer = new java.io.FileWriter(csv, true)) {
            for (T component : components.stream().sorted(comparing(Component::name)).toList()) {
                for (ClassInfo action : classesExtractor.apply(component).stream().sorted(comparing(ClassInfo::simpleName)).toList()) {
                    var otherComponents = components.stream()
                            .filter(c -> c != component)
                            .filter(c -> classesExtractor.apply(c).contains(action))
                            .toList();
                    if (!otherComponents.isEmpty()) {
                        var componentNames = otherComponents.stream()
                                .map(Component::name)
                                .map(ComponentName::value)
                                .sorted()
                                .collect(Collectors.joining(" "));
                        writer.write(String.format("%s,%s,%s\n", component.name().value(), action.simpleName(), componentNames));
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to write file", e);
        }
    }

    public interface Component {
        ComponentName name();
    }
}
