package test;

import java.io.File;

public class ProjectStructurePrinter {

    public static void main(String[] args) {
        // Change this to your project root path
        String projectPath = System.getProperty("user.dir");

        System.out.println("========================================");
        System.out.println("PROJECT STRUCTURE");
        System.out.println("========================================");
        System.out.println(projectPath);
        System.out.println("========================================");

        File root = new File(projectPath);
        printTree(root, "", true);

        System.out.println("========================================");
        System.out.println("DONE");
    }

    private static void printTree(File file, String prefix, boolean isLast) {
        // Skip hidden folders and common non-essential directories
        String name = file.getName();
        if (name.equals(".git") || name.equals(".idea")
                || name.equals("target") || name.equals(".mvn")
                || name.equals("node_modules") || name.equals(".settings")
                || name.equals("bin") || name.equals("out")) {
            return;
        }

        String connector = isLast ? "└── " : "├── ";
        System.out.println(prefix + connector + name
                + (file.isDirectory() ? "/" : ""));

        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                // Sort: directories first, then files
                java.util.Arrays.sort(children, (a, b) -> {
                    if (a.isDirectory() && !b.isDirectory()) return -1;
                    if (!a.isDirectory() && b.isDirectory()) return 1;
                    return a.getName().compareToIgnoreCase(b.getName());
                });

                for (int i = 0; i < children.length; i++) {
                    String newPrefix = prefix + (isLast ? "    " : "│   ");
                    printTree(children[i], newPrefix,
                            i == children.length - 1);
                }
            }
        }
    }
}