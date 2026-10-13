package context;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import utilitaire.MappingMethodClass;

public class FrameworkContext {

    private File rootDirectory;

    private Path uploadFolder;

    private List<String> classNames;

    private Map<String, List<MappingMethodClass>> mappings;

    public File getRootDirectory() {
        return rootDirectory;
    }

    public void setRootDirectory(File rootDirectory) {
        this.rootDirectory = rootDirectory;
    }

    public Path getUploadFolder() {
        return uploadFolder;
    }

    public void setUploadFolder(Path uploadFolder) {
        this.uploadFolder = uploadFolder;
    }

    public List<String> getClassNames() {
        return classNames;
    }

    public void setClassNames(List<String> classNames) {
        this.classNames = classNames;
    }

    public Map<String, List<MappingMethodClass>> getMappings() {
        return mappings;
    }

    public void setMappings(Map<String, List<MappingMethodClass>> mappings) {
        this.mappings = mappings;
    }
}