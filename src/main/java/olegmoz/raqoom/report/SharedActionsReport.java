package olegmoz.raqoom.report;

import olegmoz.raqoom.ClassInfo;
import olegmoz.raqoom.ComponentName;

import java.io.File;
import java.util.Collection;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

public class SharedActionsReport {

    private final Collection<Component> components;

    public SharedActionsReport(Collection<Component> components) {
        this.components = components;
    }

    public void write(File csv) {
        try (var writer = new java.io.FileWriter(csv, true)) {
            for (Component component : components.stream().sorted(comparing(Component::name)).toList()) {
                for (ClassInfo action : component.actions().stream().sorted(comparing(ClassInfo::simpleName)).toList()) {
                    var otherComponents = components.stream()
                            .filter(c -> c != component)
                            .filter(c -> c.actions().contains(action))
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

        Collection<ClassInfo> actions();
    }
}
