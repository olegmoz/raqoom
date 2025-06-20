package olegmoz.raqoom;

public record ComponentName(String value) implements Comparable<ComponentName> {
    public ComponentName {
        if (value.contains(" ")) {
            throw new IllegalArgumentException("Component name should not contain space: %s".formatted(value));
        }
    }

    @Override
    public int compareTo(ComponentName o) {
        return this.value.compareTo(o.value);
    }
}
