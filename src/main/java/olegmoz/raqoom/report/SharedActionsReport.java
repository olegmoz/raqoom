package olegmoz.raqoom.report;

import olegmoz.raqoom.ClassInfo;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

public class SharedActionsReport {

    public Set<Violation> report(Collection<Component> components) {
        return components.stream()
                .flatMap(component -> component.actions().stream()
                        .map(action -> new ActionComponent(action, component)))
                .collect(Collectors.groupingBy(
                        ActionComponent::action,
                        Collectors.mapping(ActionComponent::component, toSet())))
                .entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .map(entry -> new Violation(entry.getKey(), entry.getValue()))
                .collect(toSet());
    }

    private record ActionComponent(ClassInfo action, Component component) {
    }

    public record Violation(ClassInfo action, Set<Component> components) {
    }

    public interface Component {
        Collection<ClassInfo> actions();
    }
}
