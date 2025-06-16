package olegmoz.raqoom;

import olegmoz.raqoom.report.SharedActionsReport;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.jar.JarFile;

public class JarComponent implements SharedActionsReport.Component {

    private static final String CLASS_EXT = ".class";
    private static final String ACTION = "org.example.Action";

    private final File jar;

    public JarComponent(File jar) {
        this.jar = jar;
    }

    @Override
    public Collection<ClassInfo> actions() {
        return classes().stream()
                .filter(JarClassInfo::isAction)
                .map(c -> (ClassInfo) c).toList();
    }

    Collection<JarClassInfo> classes() {
        var classes = new ArrayList<JarClassInfo>();
        try (var jarFile = new JarFile(jar); var loader = new URLClassLoader(new URL[]{jar.toURI().toURL()})) {
            var entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                var entry = entries.nextElement().getName();
                if (entry.endsWith(CLASS_EXT)) {
                    var className = entry.replace('/', '.')
                            .substring(0, entry.length() - CLASS_EXT.length());
                    classes.add(toClassInfo(loader.loadClass(className)));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to list classes from jar '%s'".formatted(jar), e);
        }
        return classes;
    }

    private JarClassInfo toClassInfo(Class<?> cl) {
        return new JarClassInfo(cl);
    }

    static final class JarClassInfo implements ClassInfo {
        private final Class<?> raw;

        private JarClassInfo(Class<?> raw) {
            this.raw = raw;
        }

        @Override
        public String fullName() {
            return raw.getName();
        }

        @Override
        public String simpleName() {
            return raw.getSimpleName();
        }

        public boolean isAction() {
            return Arrays.stream(raw.getInterfaces())
                    .map(Class::getName)
                    .anyMatch(name -> name.equals(ACTION));
        }
    }
}
