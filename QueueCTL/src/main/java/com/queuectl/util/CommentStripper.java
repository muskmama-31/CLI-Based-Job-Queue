package com.queuectl.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CommentStripper {
    private static final Set<String> SKIP_DIRS = new HashSet<>(Arrays.asList(
            ".git", ".svn", ".hg", "target", ".idea", ".vscode",
            "generated-sources", "generated-test-sources", "archive-tmp"));

    public static void main(String[] args) throws IOException {
        Path root = args.length > 0 ? Paths.get(args[0]) : Paths.get("");
        if (!root.isAbsolute()) {
            root = root.toAbsolutePath().normalize();
        }

        final int[] counters = new int[2];

        Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                Path name = dir.getFileName();
                if (name != null && SKIP_DIRS.contains(name.toString())) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                String fn = file.getFileName().toString().toLowerCase();
                try {
                    if (fn.endsWith(".java")) {
                        processFile(file, ContentType.JAVA, counters);
                    } else if (fn.endsWith(".xml")) {
                        processFile(file, ContentType.XML, counters);
                    } else if (fn.endsWith(".properties") || fn.endsWith(".yml") || fn.endsWith(".yaml")) {
                        processFile(file, ContentType.HASH_LINE, counters);
                    } else if (fn.endsWith(".cmd") || fn.endsWith(".bat")) {
                        processFile(file, ContentType.HASH_LINE, counters);
                    }
                } catch (Exception e) {
                    System.err.println("[WARN] Failed to process " + file + ": " + e.getMessage());
                }
                return FileVisitResult.CONTINUE;
            }
        });

        System.out.println("Visited files: " + counters[0] + ", Modified: " + counters[1]);
    }

    enum ContentType {
        JAVA, XML, HASH_LINE
    }

    private static void processFile(Path file, ContentType type, int[] counters) throws IOException {
        counters[0]++;
        String original = Files.readString(file, StandardCharsets.UTF_8);
        String stripped;
        switch (type) {
            case JAVA:
                stripped = stripJavaComments(original);
                break;
            case XML:
                stripped = stripXmlComments(original);
                break;
            case HASH_LINE:
                stripped = stripHashLineComments(original);
                break;
            default:
                stripped = original;
        }
        if (!stripped.equals(original)) {
            Files.writeString(file, stripped, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.CREATE);
            counters[1]++;
        }
    }

    static String stripJavaComments(String s) {
        StringBuilder out = new StringBuilder(s.length());
        final int NORMAL = 0, SLASH = 1, LINE = 2, BLOCK = 3, STAR_IN_BLOCK = 4, STR = 5, CHR = 6, ESC = 7;
        int state = NORMAL;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (state) {
                case 0:
                    if (c == '/') {
                        state = SLASH;
                    } else if (c == '"') {
                        out.append(c);
                        state = STR;
                    } else if (c == '\'') {
                        out.append(c);
                        state = CHR;
                    } else {
                        out.append(c);
                    }
                    break;
                case 1:
                    if (c == '/') {
                        state = LINE;
                    } else if (c == '*') {
                        state = BLOCK;
                    } else {
                        out.append('/').append(c);
                        state = NORMAL;
                    }
                    break;
                case 2:
                    if (c == '\n' || c == '\r') {
                        out.append(c);
                        state = NORMAL;
                    }
                    break;
                case 3:
                    if (c == '*') {
                        state = STAR_IN_BLOCK;
                    } else if (c == '\n' || c == '\r') {
                        out.append(c);
                    }
                    break;
                case 4:
                    if (c == '/') {
                        state = NORMAL;
                    } else if (c == '*') {

                    } else {
                        if (c == '\n' || c == '\r')
                            out.append(c);
                        state = BLOCK;
                    }
                    break;
                case 5:
                    out.append(c);
                    if (c == '\\') {
                        state = ESC;
                    } else if (c == '"') {
                        state = NORMAL;
                    }
                    break;
                case 6:
                    out.append(c);
                    if (c == '\\') {
                        state = ESC;
                    } else if (c == '\'') {
                        state = NORMAL;
                    }
                    break;
                case 7:
                    out.append(c);

                    break;
            }

        }

        StringBuilder sb = new StringBuilder(s.length());
        boolean inStr = false, inChr = false, inLine = false, inBlock = false, escape = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            char next = (i + 1 < s.length()) ? s.charAt(i + 1) : '\0';

            if (inLine) {
                if (c == '\n') {
                    sb.append(c);
                    inLine = false;
                }
                continue;
            }
            if (inBlock) {
                if (c == '*' && next == '/') {
                    i++;
                    inBlock = false;
                } else if (c == '\n' || c == '\r') {
                    sb.append(c);
                }
                continue;
            }
            if (inStr) {
                sb.append(c);
                if (escape) {
                    escape = false;
                } else if (c == '\\') {
                    escape = true;
                } else if (c == '"') {
                    inStr = false;
                }
                continue;
            }
            if (inChr) {
                sb.append(c);
                if (escape) {
                    escape = false;
                } else if (c == '\\') {
                    escape = true;
                } else if (c == '\'') {
                    inChr = false;
                }
                continue;
            }

            if (c == '/' && next == '/') {
                inLine = true;
                i++;
                continue;
            }
            if (c == '/' && next == '*') {
                inBlock = true;
                i++;
                continue;
            }
            if (c == '"') {
                inStr = true;
                sb.append(c);
                continue;
            }
            if (c == '\'') {
                inChr = true;
                sb.append(c);
                continue;
            }
            sb.append(c);
        }
        return sb.toString();
    }

    static String stripXmlComments(String s) {
        StringBuilder out = new StringBuilder(s.length());
        int i = 0;
        while (i < s.length()) {
            int start = s.indexOf("<!--", i);
            if (start < 0) {
                out.append(s, i, s.length());
                break;
            }
            out.append(s, i, start);
            int end = s.indexOf("-->", start + 4);
            if (end < 0) {

                break;
            }

            for (int k = start; k <= end + 3 && k < s.length(); k++) {
                char c = s.charAt(k);
                if (c == '\n' || c == '\r')
                    out.append(c);
            }
            i = end + 3;
        }
        return out.toString();
    }

    static String stripHashLineComments(String s) {
        String[] lines = s.split("\n", -1);
        StringBuilder out = new StringBuilder(s.length());
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String t = line.stripLeading();
            if (t.startsWith("#") || t.startsWith("!")) {

            } else {
                out.append(line);
            }
            if (i < lines.length - 1)
                out.append('\n');
        }
        return out.toString();
    }
}
