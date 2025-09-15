package org.aleksid.my_ai.util;

public class PrintUtils {

    public static String prettyPrint(String raw) {
        StringBuilder sb = new StringBuilder();
        int indent = 0;
        boolean inQuotes = false;

        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);

            if (c == '\'' && (i == 0 || raw.charAt(i - 1) != '\\')) {
                inQuotes = !inQuotes;
            }

            if (!inQuotes) {
                switch (c) {
                    case '{':
                    case '[':
                        sb.append(c).append('\n');
                        indent++;
                        appendIndent(sb, indent);
                        continue;
                    case '}':
                    case ']':
                        sb.append('\n');
                        indent--;
                        appendIndent(sb, indent);
                        sb.append(c);
                        continue;
                    case ',':
                        sb.append(c).append('\n');
                        appendIndent(sb, indent);
                        continue;
                    case '=':
                        sb.append(" = ");
                        continue;
                    default:
                        // fallthrough
                }
            }

            sb.append(c);
        }

        return sb.toString()
                // отделить крупные секции пустой строкой для читаемости
                .replace("prompt =", "\nPrompt =")
                .replace("context =", "\nContext =");
    }

    private static void appendIndent(StringBuilder sb, int indent) {
        for (int j = 0; j < indent; j++) {
            sb.append("  "); // два пробела на уровень
        }
    }
}
