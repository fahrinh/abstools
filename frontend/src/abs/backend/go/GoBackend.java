/** 
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved. 
 * This file is licensed under the terms of the Modified BSD License.
 */
package abs.backend.go;

import abs.common.NotImplementedYetException;
import abs.frontend.ast.*;
import abs.frontend.parser.Main;
import abs.frontend.typechecker.*;
import com.google.common.base.CaseFormat;

import java.io.File;
import java.util.*;
import java.util.List;

public class GoBackend extends Main {

    public final static String CHARSET = "UTF-8";
    private static final Map<String, String> dataTypeMap = initDataTypeMap();
    private static final String[] JAVA_RESERVED_WORDS_ARRAY = {"abstract", "do", "import", "public", "throws",
            "boolean", "double", "instanceof", "return", "transient", "break", "else", "int", "short", "try", "byte",
            "extends", "interface", "static", "void", "case", "final", "long", "strictfp", "volatile", "catch",
            "finally", "native", "super", "while", "char", "float", "new", "switch", "class", "for", "package",
            "synchronized", "continue", "if", "private", "this", "default", "implements", "protected", "throw",
            "const", "goto", "null", "true", "false", "abs"};
    private static final Set<String> JAVA_RESERVED_WORDS = new HashSet<String>();
    private File destDir;
    public static String DEST_PACKAGE;
    private boolean sourceOnly = false;
    private boolean omitDebug = false;

    public static void main(final String... args) {
        try {
            new GoBackend().compile(args);
        } catch (NotImplementedYetException e) {
            System.err.println(e.getMessage());
            System.exit(0);
        } catch (Exception e) {
            System.err.println("An error occurred during compilation:\n" + e.getMessage());

            if (Arrays.asList(args).contains("-debug")) {
                e.printStackTrace();
            }

            System.exit(1);
        }
    }

    private static Map<String, String> initDataTypeMap() {
        final Map<String, String> res = new HashMap<String, String>();
        res.put("Int", "int");
//        res.put("Rat", ABSRational.class.getName());
        res.put("Bool", "bool");
        res.put("String", "string");
//        res.put("Fut", ABSFut.class.getName());
//        res.put("Unit", ABSUnit.class.getName());
//        res.put("Process", ABSProcess.class.getName());
        return res;
    }

    public static boolean isBuiltinDataType(Type absType) {
        if (absType.isDataType())
            return dataTypeMap.containsKey(((DataTypeType)absType).getDecl().getName());
        else
            return false;
    }

    public static String getGoType(ConstructorArg u) {
        return getGoType(u.getTypeUse());
    }

    public static String getGoType(TypeUse absType) {
        return getQualifiedString(absType.getType());
    }

    public static String getQualifiedString(String s) {
        return s;
    }

    public static String getQualifiedString(Name name) {
        return getQualifiedString(name.getString());
    }

    public static String getQualifiedString(Type absType) {
        String res = null;
        if (absType.isDataType()) {
            DataTypeType dt = (DataTypeType) absType;
            res = dataTypeMap.get(dt.getDecl().getName());
            if (res != null)
                return res;

            StringBuilder sb = new StringBuilder();
            if (dt.hasTypeArgs() && !containsUnboundedType(dt.getTypeArgs())) {
                sb.append("<");
                boolean first = true;
                for (Type t : dt.getTypeArgs()) {
                    if (first)
                        first = false;
                    else
                        sb.append(',');
                    sb.append(getQualifiedString(t));
                }
                sb.append(">");
            }
            return getQualifiedString(dt.getDecl()) + sb.toString();
            /*
             * if (dt.hasTypeArgs() && !containsUnboundedType(dt.getTypeArgs()))
             * {
             *
             * sb.append("<"); boolean first = true; for (Type t :
             * dt.getTypeArgs()) { if (first) first = false; else
             * sb.append(','); sb.append(getQualifiedString(t)); }
             * sb.append(">"); }
             */
        } else if (absType.isInterfaceType()) {
            InterfaceType it = (InterfaceType) absType;
            System.err.println(">>>INTERFACE TYPE " + getQualifiedString(it.getDecl()));
            return getQualifiedString(it.getDecl());
        } else if (absType.isTypeParameter()) {
            TypeParameter tp = (TypeParameter) absType;
            System.err.println(">>>TYPE " + tp.getDecl().getName());
            return tp.getDecl().getName();
        } else if (absType.isBoundedType()) {
            BoundedType bt = (BoundedType) absType;
            if (bt.hasBoundType())
                return getQualifiedString(bt.getBoundType());
            return "?";
        } else if (absType.isAnyType()) {
            return "java.lang.Object";
        } else if (absType.isUnionType()) {
            return getQualifiedString(((UnionType) absType).getOriginatingClass());
        }

        throw new RuntimeException("Type " + absType.getClass().getName() + " not yet supported by Go backend");
    }

    private static boolean containsUnboundedType(List<Type> typeArgs) {
        for (Type t : typeArgs) {
            if (t.isBoundedType()) {
                BoundedType bt = (BoundedType)t;
                if (!bt.hasBoundType()) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String getQualifiedString(Decl decl) {
        return decl.getModuleDecl().getName().toLowerCase() + "." + getGoName(decl);
    }

    public static String getConstructorName(DataTypeDecl dataType, String name) {
        return truncate(dataType.getName() + "_" + name);
    }

    public static String getConstructorName(DataConstructor decl) {
        return getConstructorName(((DataTypeType) decl.getType()).getDecl(), decl.getName());
    }

    public static String getInterfaceName(String name) {
        return truncate(name + "_i");
    }

    public static String getClassName(String name) {
//        return truncate(name + "_c");
        return truncate(name + "");
    }

//    static {
//        for (String s : JAVA_RESERVED_WORDS_ARRAY) {
//            JAVA_RESERVED_WORDS.add(s);
//        }
//        // add methods from ABSObject to reserved words:
//        for (Method m : ABSObject.class.getMethods()) {
//            JAVA_RESERVED_WORDS.add(m.getName());
//        }
//        // the run method is special, because it can be overridden
//        JAVA_RESERVED_WORDS.remove("run");
//    }

    public static String getProductName(String name) {
        return truncate(name + "_prod");
    }

    public static String getReconfigurationName(String from, String to) {
        return truncate(from + "__" + to + "_recf");
    }

    public static String getDeltaName(String name) {
        return truncate(name + "_delta");
    }

    public static String getDeltaPackageName(String name) {
        return truncate(GoBackendConstants.LIB_DELTAS_PACKAGE + "." + name);
    }

    public static String getUpdateName(String name) {
        return truncate(name + "_upd");
    }

    public static String getModifierPackageName(String name) {
        return truncate(GoBackendConstants.LIB_DELTAS_PACKAGE + "." + name);
    }

    public static String getModifierName() {
        return truncate("Mod_" + getRandomName());
    }

    public static String getFunctionName(String name) {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, truncate(escapeReservedWords(name) + "_f"));
    }

    public static String getMethodName(String name) {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, escapeReservedWords(name));
    }

    public static String getVariableNameUpperCamel(String name) {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, escapeReservedWords(name));
    }

    public static String getVariableName(String name) {
        return escapeReservedWords(name);
    }

    public static String getReceiverName(String name) {
        return new String("" + name.charAt(0)).toLowerCase();
    }

    private static String escapeReservedWords(String name) {
        if (JAVA_RESERVED_WORDS.contains(name)) {
            return name + "__";
        } else {
            return name;
        }
    }

    public static String getGoName(Decl decl) {
        String result;
        if (decl instanceof FunctionDecl) {
            result = getFunctionName(decl.getName());
        } else if (decl instanceof DataConstructor) {
            result = getConstructorName((DataConstructor) decl);
        } else if (decl instanceof ClassDecl) {
            result = getClassName(decl.getName());
        } else if (decl instanceof InterfaceDecl) {
            result = getInterfaceName(decl.getName());
        } else {
            result = truncate(decl.getName());
        }
        return result;
    }

    public static String getGoName(ModuleModifier mod) {
        String result;
        if (mod instanceof ClassModifier) {
            result = getClassName(mod.getName());
        } else {
            result = truncate(mod.getName());
        }
        return result;
    }

    // Shorten name to 255 chars as files with these names are created
    private static String truncate(String s) {
        int maxlength = 200;
        if (s.length() < maxlength) {
            return s;
        } else {
            String prefix = s.substring(0, maxlength);
            int suffix = s.hashCode(); // We do not consider collisions as highly unlikely
            return prefix + suffix;
        }
    }

    /**
     * get the java name main blocks
     */
    public static String getGoNameForMainBlock(ModuleDecl module) {
        return module.getName().toLowerCase() + "_main";
    }

    /**
     * get the fully qualified java name for the main block of a given module
     */
    public static String getFullGoNameForMainBlock(ModuleDecl module) {
        return module.getName() + "." + getGoNameForMainBlock(module);
    }

    /**
     * Just return a randomly generated string
     */
    public static String getRandomName() {
        return Integer.toHexString(UUID.randomUUID().hashCode());
    }

    @Override
    public List<String> parseArgs(String[] args) {
        List<String> restArgs = super.parseArgs(args);
        List<String> remainingArgs = new ArrayList<String>();

        String goPath = System.getenv("GOPATH");
        if(goPath == null || !new File(goPath).isDirectory()) {
            System.err.println("Please set GOPATH environment variable!");
            System.exit(1);
        }

        for (int i = 0; i < restArgs.size(); i++) {
            String arg = restArgs.get(i);
            if (arg.equals("-d")) {
                i++;
                if (i < restArgs.size()) {
                    DEST_PACKAGE = args[++i];
                    File goPathSrc = new File(goPath, "src");
                    destDir = new File(goPathSrc, DEST_PACKAGE);
                }
            } else if (arg.equals("-sourceonly")) {
                this.sourceOnly = true;
            } else if (arg.equals("-debug")) {
                /* Print stacktrace on exception, used in main(), must be removed from remaining args. */
            } else if (arg.equals("-go")) {
                // nothing to do
            } else {
                remainingArgs.add(arg);
            }
        }

        if (DEST_PACKAGE == null) {
            System.err.println("Please provide a destination package!\nThis package directory will be created inside GOPATH/src along with generated source code.");
            System.exit(1);
        }

        return remainingArgs;
    }

    protected void printUsage() {
        super.printUsage();
        System.out.println("Go Backend:\n"
                + "  -d <dir>       generate files to GOPATH/src/<dir>\n"
                + "  -debug         print stacktrace on exception\n"
                + "  -sourceonly    do not generate class files\n");
    }

    private void compile(String[] args) throws Exception {
        final Model model = parse(args);
        if (model.hasParserErrors() || model.hasErrors() || model.hasTypeErrors())
            printParserErrorAndExit();
        destDir.mkdirs();
        if (!destDir.exists()) {
            System.err.println("Destination directory " + destDir.getAbsolutePath() + " does not exist!");
            System.exit(1);
        }

        if (!destDir.canWrite()) {
            System.err.println("Destination directory " + destDir.getAbsolutePath() + " cannot be written to!");
            System.exit(1);
        }

        compile(model, destDir);
    }

    private void compile(Model m, File destDir) throws Exception {
        m.includeDebug = !this.omitDebug;
        GoCode goCode = new GoCode(destDir);

        if (verbose) System.out.println("Generating Go code...");
        m.generateGoCode(goCode);

        if (!sourceOnly) {
            if (verbose) System.out.println("Compiling generated Go code...");
            goCode.compile();
        }

        goCode.prettyFormat();
    }
}
