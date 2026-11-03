package com.face_chtj.base_iotutils.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class MemoryStatsParser {
    static final String PACKAGES_BEGIN = "__UID_MEMORY_PACKAGES_BEGIN__";
    static final String PACKAGES_END = "__UID_MEMORY_PACKAGES_END__";
    static final String PS_BEGIN = "__UID_MEMORY_PS_BEGIN__";
    static final String PS_END = "__UID_MEMORY_PS_END__";
    static final String MEMINFO_BEGIN = "__UID_MEMORY_MEMINFO_BEGIN__";
    static final String MEMINFO_END = "__UID_MEMORY_MEMINFO_END__";

    private MemoryStatsParser() {
    }

    static ParsedResult parse(String output) throws MemoryStats.StatsException {
        if (output == null || output.isEmpty()) {
            throw unsupported("Memory command returned no output");
        }

        Map<Integer, ProcessIdentity> identities = new HashMap<>();
        Map<Integer, List<String>> packageNamesByUid = new HashMap<>();
        List<RawPss> pssRecords = new ArrayList<>();
        boolean inPackages = false;
        boolean inPs = false;
        boolean inMeminfo = false;
        boolean sawPackagesBegin = false;
        boolean sawPackagesEnd = false;
        boolean sawPsBegin = false;
        boolean sawPsEnd = false;
        boolean sawMeminfoBegin = false;
        boolean sawRssCategory = false;
        boolean inPssSection = false;
        boolean sawRam = false;
        int compactVersion = -1;

        String[] lines = output.split("\\r?\\n");
        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (PACKAGES_BEGIN.equals(line)) {
                sawPackagesBegin = true;
                inPackages = true;
                continue;
            }
            if (PACKAGES_END.equals(line)) {
                sawPackagesEnd = true;
                inPackages = false;
                continue;
            }
            if (PS_BEGIN.equals(line)) {
                sawPsBegin = true;
                inPs = true;
                continue;
            }
            if (PS_END.equals(line)) {
                sawPsEnd = true;
                inPs = false;
                continue;
            }
            if (MEMINFO_BEGIN.equals(line)) {
                sawMeminfoBegin = true;
                inMeminfo = true;
                continue;
            }
            if (MEMINFO_END.equals(line)) {
                inMeminfo = false;
                continue;
            }

            if (inPackages) {
                parsePackageLine(line, packageNamesByUid);
                continue;
            }
            if (inPs) {
                parsePsLine(line, identities);
                continue;
            }
            if (!inMeminfo || line.isEmpty()) {
                continue;
            }

            if (line.startsWith("version,")) {
                String[] fields = line.split(",", -1);
                if (fields.length >= 2) {
                    compactVersion = parseInt(fields[1], "compact format version");
                }
                continue;
            }
            if (!inPssSection && line.startsWith("cat,")) {
                sawRssCategory = true;
                continue;
            }
            if (!inPssSection && sawRssCategory && line.startsWith("oom,")) {
                inPssSection = true;
            }
            if (inPssSection && line.startsWith("proc,")) {
                pssRecords.add(parseCompactProcess(line));
                continue;
            }
            if (line.startsWith("ram,")) {
                sawRam = true;
            }
        }

        if (!sawPackagesBegin || !sawPackagesEnd || !sawPsBegin || !sawPsEnd
                || !sawMeminfoBegin) {
            throw unsupported("Memory command output is missing section markers");
        }
        if (packageNamesByUid.isEmpty()) {
            throw unsupported("The package/UID map is empty");
        }
        if (compactVersion != 1) {
            throw unsupported("Unsupported dumpsys meminfo compact version: " + compactVersion);
        }
        if (identities.isEmpty()) {
            throw unsupported("The PID/UID map is empty");
        }
        if (!inPssSection || pssRecords.isEmpty() || !sawRam) {
            throw unsupported("The PSS section is missing from dumpsys meminfo output");
        }

        List<ParsedProcess> processes = new ArrayList<>(pssRecords.size());
        for (RawPss pss : pssRecords) {
            ProcessIdentity identity = identities.get(pss.pid);
            int uid = identity != null ? identity.uid : -1;
            String name = pss.name;
            if ((name == null || name.isEmpty()) && identity != null) {
                name = identity.name;
            }
            processes.add(new ParsedProcess(pss.pid, uid, name, pss.pssBytes));
        }
        for (List<String> packageNames : packageNamesByUid.values()) {
            Collections.sort(packageNames);
        }
        return new ParsedResult(processes, packageNamesByUid);
    }

    private static void parsePackageLine(String line,
            Map<Integer, List<String>> packageNamesByUid) throws MemoryStats.StatsException {
        if (!line.startsWith("package:")) {
            return;
        }
        int uidSeparator = line.lastIndexOf(" uid:");
        if (uidSeparator <= "package:".length()) {
            throw unsupported("Malformed package/UID row: " + line);
        }
        String packageName = line.substring("package:".length(), uidSeparator).trim();
        int uid = parseInt(line.substring(uidSeparator + " uid:".length()), "package uid");
        if (packageName.isEmpty() || uid < 0) {
            throw unsupported("Malformed package/UID row: " + line);
        }
        List<String> packageNames = packageNamesByUid.get(uid);
        if (packageNames == null) {
            packageNames = new ArrayList<>();
            packageNamesByUid.put(uid, packageNames);
        }
        if (!packageNames.contains(packageName)) {
            packageNames.add(packageName);
        }
    }

    private static void parsePsLine(String line, Map<Integer, ProcessIdentity> identities) {
        if (line.isEmpty() || line.startsWith("PID ") || line.equals("PID UID NAME")) {
            return;
        }
        String[] fields = line.split("\\s+", 3);
        if (fields.length < 2) {
            return;
        }
        try {
            int pid = Integer.parseInt(fields[0]);
            int uid = Integer.parseInt(fields[1]);
            String name = fields.length >= 3 ? fields[2] : "";
            identities.put(pid, new ProcessIdentity(uid, name));
        } catch (NumberFormatException ignored) {
            // Ignore headers and transient malformed rows from ps.
        }
    }

    private static RawPss parseCompactProcess(String line) throws MemoryStats.StatsException {
        String[] fields = line.split(",", -1);
        if (fields.length < 7) {
            throw unsupported("Malformed compact process row: " + line);
        }

        // Parse from the right because a native process name can theoretically contain a comma.
        int pidIndex = fields.length - 4;
        int pssIndex = fields.length - 3;
        int pid = parseInt(fields[pidIndex], "process pid");
        long pssKb = parseLong(fields[pssIndex], "process PSS");
        if (pssKb < 0L || pssKb > Long.MAX_VALUE / 1024L) {
            throw unsupported("Invalid process PSS in row: " + line);
        }

        StringBuilder name = new StringBuilder();
        for (int i = 2; i < pidIndex; i++) {
            if (name.length() > 0) {
                name.append(',');
            }
            name.append(fields[i]);
        }
        return new RawPss(pid, name.toString(), pssKb * 1024L);
    }

    private static int parseInt(String value, String label) throws MemoryStats.StatsException {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException error) {
            throw unsupported("Invalid " + label + ": " + value, error);
        }
    }

    private static long parseLong(String value, String label) throws MemoryStats.StatsException {
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException error) {
            throw unsupported("Invalid " + label + ": " + value, error);
        }
    }

    private static MemoryStats.StatsException unsupported(String message) {
        return unsupported(message, null);
    }

    private static MemoryStats.StatsException unsupported(String message, Throwable cause) {
        return new MemoryStats.StatsException(
                MemoryStats.ErrorCode.UNSUPPORTED_OUTPUT, message, cause);
    }

    static final class ParsedResult {
        final List<ParsedProcess> processes;
        final Map<Integer, List<String>> packageNamesByUid;

        ParsedResult(List<ParsedProcess> processes,
                Map<Integer, List<String>> packageNamesByUid) {
            this.processes = processes;
            this.packageNamesByUid = packageNamesByUid;
        }
    }

    static final class ParsedProcess {
        final int pid;
        final int uid;
        final String name;
        final long pssBytes;

        ParsedProcess(int pid, int uid, String name, long pssBytes) {
            this.pid = pid;
            this.uid = uid;
            this.name = name;
            this.pssBytes = pssBytes;
        }
    }

    private static final class ProcessIdentity {
        final int uid;
        final String name;

        ProcessIdentity(int uid, String name) {
            this.uid = uid;
            this.name = name;
        }
    }

    private static final class RawPss {
        final int pid;
        final String name;
        final long pssBytes;

        RawPss(int pid, String name, long pssBytes) {
            this.pid = pid;
            this.name = name;
            this.pssBytes = pssBytes;
        }
    }
}
