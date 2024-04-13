package configuration;

public class AgentConfig {
    private String pkg;

    public boolean isDecompile() {
        return decompile;
    }

    public void setDecompile(boolean decompile) {
        this.decompile = decompile;
    }

    private boolean decompile;
    private String version;

    public String getJar() {
        return jar;
    }

    public void setJar(String jar) {
        this.jar = jar.replace("\\", "\\\\");
    }

    private String jar;

    public Boolean getRemoveDetectedPattern() {
        return removeDetectedPattern;
    }

    public void setRemoveDetectedPattern(Boolean removeDetectedPattern) {
        this.removeDetectedPattern = removeDetectedPattern;
    }

    private Boolean removeDetectedPattern;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    private String path;

    public String getExcludes() {
        return excludes;
    }

    public void setExcludes(String excludes) {
        this.excludes = excludes.replace("\\", "\\\\");
    }

    private String excludes;

    public String getPkg() {
        return pkg;
    }

    public void setPkg(String pkg) {
        this.pkg = pkg;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }


}
