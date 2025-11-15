package com.xsasakihaise.hellascontrol;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Represents the human-readable metadata file bundled with the core mod. The
 * file lists version strings, dependencies, and feature highlights that can be
 * displayed by administrative tools or launcher UIs to describe the server's
 * Hellas stack.
 */
public class HellasControlInfoConfig {

    private transient final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private File configFile;

    private String version = null;
    private String[] dependencies = new String[0];
    private String[] features = new String[0];

    private boolean valid = false;

    /**
     * Loads the JSON descriptor from {@code config/hellas/patcher/} under the
     * given server root. If the file is missing, the class falls back to the
     * in-jar defaults.
     *
     * @param serverRoot base directory of the dedicated server instance
     */
    public void load(File serverRoot) {
        File configDir = new File(serverRoot, "config/hellas/patcher/");
        if (!configDir.exists()) configDir.mkdirs();

        configFile = new File(configDir, "hellas_patcher_info.json");

        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                HellasControlInfoConfig loaded = gson.fromJson(reader, HellasControlInfoConfig.class);
                if (loaded != null) {
                    this.version = loaded.version;
                    this.dependencies = loaded.dependencies != null ? loaded.dependencies : new String[0];
                    this.features = loaded.features != null ? loaded.features : new String[0];
                    this.valid = true;
                    return;
                }
            } catch (IOException | JsonSyntaxException e) {
                e.printStackTrace();
                this.valid = false;
                return;
            }
        }

        if (this.valid) return;

        loadDefaultsFromResource();
    }

    /**
     * Loads {@code config/hellaspatcher.json} from the mod JAR. This ensures we
     * always have a default set of descriptive strings even if the server never
     * exported the editable JSON file.
     */
    public void loadDefaultsFromResource() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("config/hellaspatcher.json")) {
            if (is != null) {
                try (InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                    HellasControlInfoConfig loaded = gson.fromJson(isr, HellasControlInfoConfig.class);
                    if (loaded != null) {
                        this.version = loaded.version;
                        this.dependencies = loaded.dependencies != null ? loaded.dependencies : new String[0];
                        this.features = loaded.features != null ? loaded.features : new String[0];
                        this.valid = true;
                        return;
                    }
                }
            } else {
                this.valid = false;
            }
        } catch (IOException | JsonSyntaxException e) {
            e.printStackTrace();
            this.valid = false;
        }
    }

    /**
     * @return {@code true} if the JSON descriptor parsed successfully
     */
    public boolean isValid() { return valid; }

    /**
     * Persists the in-memory representation to disk. This is typically called
     * only when a server admin edits values via an in-game UI.
     */
    public void save() {
        if (configFile == null) return;
        try (FileWriter writer = new FileWriter(configFile)) {
            gson.toJson(this, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return semantic version string displayed to users
     */
    public String getVersion() { return version; }

    /**
     * @return list of dependent Hellas modules or modids
     */
    public String[] getDependencies() { return dependencies; }

    /**
     * @return highlight strings describing this installation
     */
    public String[] getFeatures() { return features; }
}