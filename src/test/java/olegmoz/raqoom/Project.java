package olegmoz.raqoom;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class Project {

    private final String name;

    public Project(String name) {
        this.name = name;
    }

    public void build() {
        var processBuilder = new ProcessBuilder("./gradlew", "jar").directory(root());
        try {
            var process = processBuilder.start();
            try (var reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }
            var exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IllegalStateException("Build process failed with code %s".formatted(exitCode));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed building project '%s'".formatted(name), e);
        }
        var jar = jar();
        if (!jar.exists()) {
            throw new IllegalStateException("Cannot find jar file %s".formatted(jar));
        }
    }

    public File jar() {
        return new File(root(), "build/libs/" + name + "-1.0-SNAPSHOT.jar");
    }

    private File root() {
        return new File("src/test/resources/" + name);
    }
}
