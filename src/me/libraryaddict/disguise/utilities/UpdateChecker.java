package me.libraryaddict.disguise.utilities;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Pattern;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class UpdateChecker {
    private String latestVersion;

    private boolean checkHigher(String currentVersion, String newVersion) {
        String current = toReadable(currentVersion);
        String newVers = toReadable(newVersion);
        return current.compareTo(newVers) < 0;
    }

    public void checkUpdate(String currentVersion) {
        String version = getBukkitVersion();
        if (version != null) {
            if (checkHigher(currentVersion, version)) {
                latestVersion = version;
            }
        }
    }

    /**
     * Asks bukkit for the version
     */
    private String getBukkitVersion() {
        try {
            URLConnection conn = new URL("https://api.curseforge.com/servermods/files?projectIds=72490").openConnection();
            conn.addRequestProperty("User-Agent", "Lib's Disguises Update Checker");
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            JSONArray array = (JSONArray) JSONValue.parse(reader.readLine());
            if (!array.isEmpty()) {
                JSONObject latest = (JSONObject) array.get(array.size() - 1);
                String version = (String) latest.get("name");
                version = version.substring(version.lastIndexOf(" ") + 1);
                if (version.length() <= 7) {
                    return version;
                }
            }
        } catch (Exception e) {
            System.out.print("[LibsDisguises] Failed to check for a update on bukkit. " + e.getMessage());
        }
        return null;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    private String toReadable(String version) {
        String[] split = Pattern.compile(".", Pattern.LITERAL).split(version.replace("v", ""));
        version = "";
        for (String s : split)
            version += String.format("%4s", s);
        return version;
    }
}