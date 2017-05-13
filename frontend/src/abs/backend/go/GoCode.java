/**
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved. 
 * This file is licensed under the terms of the Modified BSD License.
 */
package abs.backend.go;


import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class GoCode {
    private final File srcDir;
    private final List<File> files = new ArrayList<File>();
    private final List<String> mainClasses = new ArrayList<String>();

    public GoCode() throws IOException {
        srcDir = File.createTempFile("absgobackend", Long.toString(System.nanoTime()));
        srcDir.delete();
        srcDir.mkdir();
    }

    public GoCode(File srcDir) {
        this.srcDir = srcDir;
    }

    public void addFile(File file) {
        files.add(file);
    }

    public String[] getFileNames() {
        String[] res = new String[files.size()];
        int i = 0;
        for (File f : files) {
            res[i++] = f.getAbsolutePath();
        }
        return res;
    }

    public Package createMainPackage() {
        return new Package("main", true);
    }
    public Package createPackage(String packageName) {
        return new Package(packageName, false);
    }

    public File getSrcDir() {
        return srcDir;
    }

    public void deleteCode() {
        deleteDir(srcDir);
    }

    private void deleteDir(File dir) {
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                deleteDir(f);
            } else {
                f.delete();
            }
        }
        dir.delete();
    }

    public void compile() throws GoCodeGenerationException {
        compile(srcDir);
    }

    public void compile(File destDir) throws GoCodeGenerationException {
        compile(destDir.getAbsolutePath());
    }

    public void compile(String... args) throws GoCodeGenerationException {
        ArrayList<String> args2 = new ArrayList<String>();
        args2.addAll(Arrays.asList(args));
        args2.addAll(Arrays.asList(getFileNames()));
        abs.backend.go.GoCompiler.compile(args2.toArray(new String[0]));
    }

    public void prettyFormat() throws IOException, InterruptedException {
        Process goFmtProcess = Runtime.getRuntime().exec(new String[]{"gofmt", "-s", "-w", srcDir.getAbsolutePath()});
        goFmtProcess.waitFor();

        Process goImportsProcess = Runtime.getRuntime().exec(new String[]{"goimports", "-w", srcDir.getAbsolutePath()});
        goImportsProcess.waitFor();
    }

    public String getFirstMainClass() {
        if (mainClasses.isEmpty())
            throw new IllegalStateException("There is no main class");

        return mainClasses.get(0);
    }

    public String toString() {
        StringBuilder res = new StringBuilder();

        for (File f : files) {
            append(res, f);
        }

        return res.toString();
    }

    private void append(StringBuilder res, File f) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(f));
            try {
                while (reader.ready()) {
                    res.append(reader.readLine() + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public class Package {
        public final boolean isMainPackage;
        public final String packageName;
        public final File packageDir;
        private String firstPackagePart;

        public Package(String packageName, boolean isMainPackage) {
            this.isMainPackage = isMainPackage;
            this.packageName = packageName;
            this.packageDir = new File(srcDir, isMainPackage ? "" : packageName.replace('.', File.separatorChar));
//            this.firstPackagePart = packageName.split("\\.")[0];
            packageDir.mkdirs();
        }

        public File createGoFile(String name) throws IOException, GoCodeGenerationException {
//            if (name.equals(firstPackagePart)) {
//                if (name.equals("Main")) {
//                    throw new GoCodeGenerationException("The Go backend does not support main blocks in " +
//                    		"modules with name 'Main'. Please try to use a different name.");
//                }
//                throw new GoCodeGenerationException("The Go backend does not support using the name " +
//                      name + " as module name, because it collides with a generated classname. " +
//                      		"Please try to use a different name.");
//            }
            File file = new File(packageDir, name + ".go");
            addFile(file);
            file.createNewFile();
            return file;
        }

        public void addMainClass(String s) {
            mainClasses.add((isMainPackage ? "" : packageName + ".") + s);
        }
    }

}
