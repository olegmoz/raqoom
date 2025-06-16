package olegmoz.raqoom;

public class ClassInfoStub implements ClassInfo {

    public static ClassInfoStub cl(String simpleName) {
        return new ClassInfoStub("org.example." + simpleName, simpleName, simpleName);
    }

    private final String fullName;
    private final String simpleName;
    private final String printName;

    public ClassInfoStub(String fullName, String simpleName, String printName) {
        this.fullName = fullName;
        this.simpleName = simpleName;
        this.printName = printName;
    }

    @Override
    public String fullName() {
        return fullName;
    }

    @Override
    public String simpleName() {
        return simpleName;
    }

    @Override
    public String toString() {
        return printName;
    }
}
