package common;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class that can be used statically to profile code execution times.
 */
public class Profiler {
    public static final boolean ENABLED = false;
    private static final NumberFormat FORMATTER = new DecimalFormat("#0.00");

    private static Map<String, ProfileData> runningProfiles = new HashMap<String, ProfileData>();
    private static int totalProfiles = 0;
    private static int totalProfilesOnLastPrint = 0;

    private static class ProfileData {
        public long startTime;
        public long numProfiles;
        public double totalTime;

        public double getAverage() {
            return totalTime / numProfiles;
        }
    }

    public static void time(String name) {
        if (ENABLED) {
            ProfileData data = runningProfiles.get(name);
            if (data == null) {
                data = new ProfileData();
                runningProfiles.put(name, data);
            }

            data.startTime = System.nanoTime();
        }
    }

    public static long timeEnd(String name) {
        if (ENABLED) {
            ProfileData data = runningProfiles.get(name);
            long elapsed = System.nanoTime() - data.startTime;

            data.totalTime += elapsed / 1e6;
            data.numProfiles += 1;
            totalProfiles += 1;

            return elapsed;
        }
        return 0;
    }

    public static void printTimes(int since) {
        if (ENABLED) {
            if (totalProfiles - totalProfilesOnLastPrint < since) {
                return;
            }

            totalProfilesOnLastPrint = totalProfiles;
            List<Map.Entry<String, String>> entries = new ArrayList<Map.Entry<String, String>>(runningProfiles.size());
            for (Map.Entry<String, ProfileData> entry : runningProfiles.entrySet()) {
                entries.add(new AbstractMap.SimpleEntry<String, String>(
                    entry.getKey(), format(entry.getValue().getAverage())
                ));
            }

            for (int i = 0; i < 3; i++) {
                for (Map.Entry<String, String> entry : entries) {
                    int len = Math.max(entry.getKey().length(), entry.getValue().length());
                    System.out.print("| ");
                    if (i == 0) {
                        System.out.print(centered(entry.getKey(), len));
                    } else if (i == 1) {
                        System.out.print(repeat("-", len));
                    } else if (i == 2) {
                        System.out.print(centered(entry.getValue(), len));
                    }
                    System.out.print(" ");
                }
                System.out.println("|");
            }
            System.out.println();
            runningProfiles.clear();
        }
    }

    private static String format(double millis) {
        if (millis < 1) {
            return FORMATTER.format(millis * 1000) + " \u00B5s";
        }
        if (millis < 1000) {
            return FORMATTER.format(millis) + " ms";
        }
        return FORMATTER.format(millis / 1000) + " s";
    }

    private static String repeat(String str, int times) {
        byte[] allBytes = new byte[str.length() * times];
        byte[] origBytes = str.getBytes();
        for (int i = 0; i < times; i++) {
            System.arraycopy(origBytes, 0, allBytes, i * origBytes.length, origBytes.length);
        }
        return new String(allBytes);
    }

    private static String centered(String str, int len) {
        int difference = (len - str.length()) / 2;
        return repeat(" ", difference) + str + repeat(" ", len - str.length() - difference);
    }
}
