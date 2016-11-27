/**
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved.
 * This file is licensed under the terms of the Modified BSD License.
 */
package abs.backend.go;

import abs.common.Constants;
import abs.common.NotImplementedYetException;
import abs.frontend.ast.*;
import abs.frontend.typechecker.DataTypeType;
import abs.frontend.typechecker.Type;
import abs.frontend.typechecker.UnionType;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;

public class GoGeneratorHelper {

    private static final String FLI_METHOD_PREFIX = "fli_";
    private static long tempCounter = 0;

    public static void generateHelpLine(ASTNode<?> node, PrintStream stream) {
        stream.println("// " + node.getPositionString());
    }

    public static void generateArgs(PrintStream stream, List<PureExp> args, java.util.List<Type> types) {
        generateArgs(stream, null, args, types);
    }

    public static void generateArgs(PrintStream stream, String firstArg, List<PureExp> args, java.util.List<Type> types) {
        stream.print("{");
        boolean first = true;

        if (firstArg != null) {
            stream.print(firstArg);
            first = false;
        }
//        TODO
        for (int i = 0; i < args.getNumChild(); i++) {
            PureExp e = args.getChild(i);
            if (!first)
                stream.print(", ");
            e.generateGo(stream);
//            if (types.get(i).isIntType() && e.getType().isRatType())
//                stream.print(".truncate()");
            first = false;
        }
        stream.print("}");
    }

    public static void generateArgs(PrintStream stream, ClassDecl classDecl, List<PureExp> args) {
        stream.print("{");
        boolean first = true;

        for (int i = 0; i < args.getNumChild(); i++) {
            PureExp e = args.getChild(i);
            ParamDecl paramDecl = classDecl.getParams().getChild(i);

            if (!first)
                stream.print(", ");

            stream.print(GoBackend.getVariableNameUpperCamel(paramDecl.getName()) + ":");
            e.generateGo(stream);

            first = false;
        }
        stream.print("}");
    }


    public static void generateParamArgs(PrintStream stream, List<ParamDecl> params) {
        generateParamArgs(stream, null, params);
    }

    public static void generateParamArgs(PrintStream stream, String firstArg, List<ParamDecl> params) {
        stream.print("(");
        boolean first = true;

        if (firstArg != null) {
            stream.print(firstArg);
            first = false;
        }

        for (ParamDecl d : params) {
            if (!first)
                stream.print(", ");
            stream.print(GoBackend.getVariableName(d.getName()));
            first = false;
        }
        stream.print(")");
    }

    public static void generateParams(PrintStream stream, List<ParamDecl> params) {
        generateParams(stream, null, params);
    }

    public static void generateParams(PrintStream stream, String firstArg, List<ParamDecl> params) {
        stream.print("(");

        boolean first = true;
        if (firstArg != null) {
            stream.print(firstArg);
            first = false;
        }
//        TODO
        for (ParamDecl d : params) {
            if (!first)
                stream.print(", ");
            // stream.print("final ");
            d.generateGo(stream);
            first = false;
        }
        stream.print(")");
    }

    public static void generateTypeParameters(PrintStream stream, Decl dtd,
                                              boolean plusExtends) {
        List<TypeParameterDecl> typeParams = null;
        if (dtd instanceof HasTypeParameters) {
            typeParams = ((HasTypeParameters) dtd).getTypeParameters();
        } else
            return;
        if (typeParams.getNumChild() > 0) {
            stream.print("<");
            boolean isFirst = true;
            for (TypeParameterDecl d : typeParams) {
                if (isFirst)
                    isFirst = false;
                else
                    stream.print(",");
                stream.print(d.getName());
//                TODO
//                if (plusExtends)
//                    stream.print(" extends " + ABSValue.class.getName());
            }
            stream.print(">");
        }
    }

    public static void generateBuiltInFnApp(PrintStream stream, FnApp app) {
        FunctionDecl d = (FunctionDecl) app.getDecl();
        String name = d.getName();
        if (!builtInFunctionExists(name)) {
            throw new NotImplementedYetException(app, "The built in function '" + name + "' is not implemented in the Go backend.");
        }

        // if builtin function returns a non-builtin type, cast the returned value to that type
        boolean returnsGeneratedDataType = !GoBackend.isBuiltinDataType(app.getType());
        if (returnsGeneratedDataType)
            stream.print("((" + GoBackend.getQualifiedString(app.getType()) + ")");

//        TODO
//        stream.print(ABSBuiltInFunctions.class.getName() + "." + name);
        String firstArgs = null;
        if (Constants.isFunctionalBreakPointFunctionName(d.getModuleDecl().getName() + "." + name))
            firstArgs = generateFunctionalBreakPointArgs(app);
        generateArgs(stream, firstArgs, app.getParams(), d.getTypes());

        if (returnsGeneratedDataType)
            stream.print(")");
    }

    private static String generateFunctionalBreakPointArgs(FnApp app) {
        return new StringBuilder("\"").append(app.getCompilationUnit().getFileName().replace("\\", "\\\\"))
                .append("\", ").append(app.getStartLine()).toString();
    }

    private static boolean builtInFunctionExists(String name) {
//        TODO
//        for (Method m : ABSBuiltInFunctions.class.getMethods()) {
//            if (m.getName().equals(name)) {
//                return true;
//            }
//        }
        return false;
    }

    public static String getDebugString(ASTNode<?> node) {
        return getDebugString(node, node.getStartLine());
    }

    public static String getDebugString(ASTNode<?> node, int line) {
        String fileName = node.getCompilationUnit().getFileName().replace("\\", "\\\\");
        return "if (__ABS_getRuntime().debuggingEnabled()) __ABS_getRuntime().nextStep(\""
                + fileName + "\"," + line + ");";
    }

    public static void generateMethodSig(PrintStream stream, MethodSig sig, boolean async) {
        generateMethodSig(stream, sig, async, "", "");
    }

    public static void generateMethodSig(PrintStream stream, MethodSig sig, boolean async, String modifier, String prefix) {
        GoGeneratorHelper.generateHelpLine(sig, stream);

        boolean isClassDecl = sig.getContextDecl() instanceof ClassDecl;

        if (isClassDecl) {
            stream.print("func " + modifier);
            String declName = sig.getContextDecl().getName();
            stream.print("(" + GoBackend.getReceiverName(declName) + " *" + GoBackend.getClassName(declName) + ") ");
        }

        if (async) {
            prefix = "async_";
//            TODO
//            stream.print(ABSFut.class.getName() + "<");
        }

        if (async) {
            stream.print(">");
        }
        stream.print(prefix + GoBackend.getMethodName(sig.getName()) + " ");
        GoGeneratorHelper.generateParams(stream, sig.getParams());
        stream.print(" ");
        sig.getReturnType().generateGo(stream);
    }

    public static void generateAsyncMethod(PrintStream stream, MethodImpl method) {
        final MethodSig sig = method.getMethodSig();
        generateMethodSig(stream, sig, true, "", "");
        stream.println(" {");
//        TODO
//        stream.print("return (" + ABSFut.class.getName() + ")");

        generateAsyncCall(stream, "this", null, method.getContextDecl().getType(), null, sig.getParams(), sig, new List<Annotation>());
        stream.println(";");
        stream.println("}");
    }

    public static void generateAsyncCall(PrintStream stream, AsyncCall call) {
        final PureExp callee = call.getCallee();
        final List<PureExp> params = call.getParams();
        final MethodSig sig = call.getMethodSig();
        final List<Annotation> annotations = call.getAnnotations();

        generateAsyncCall(stream, null, callee, callee.getType(), params, null, sig, annotations);
    }

    private static void generateAsyncCall(PrintStream stream, final String calleeString,
                                          final PureExp callee, final Type calleeType, final List<PureExp> args,
                                          final List<ParamDecl> params,
                                          final MethodSig sig,
                                          final List<Annotation> annotations) {
//        TODO
//        final java.util.List<Type> paramTypes = sig.getTypes();
//        stream.print(ABSRuntime.class.getName() + ".getCurrentRuntime().asyncCall(");
//        String targetType = GoBackend.getQualifiedString(calleeType);
//        stream.println("new " + AbstractAsyncCallRT.class.getName() + "<" + targetType + ">(");
//        stream.println("this,");
//        if (callee instanceof ThisExp) {
//            if (calleeString != null)
//                stream.print(calleeString);
//            else
//                callee.generateGo(stream);
//        } else {
//            stream.print(ABSRuntime.class.getName() + ".checkForNull(");
//            if (calleeString != null)
//                stream.print(calleeString);
//            else
//                callee.generateGo(stream);
//            stream.print(")");
//        }
//        stream.println(",");
//        PureExp rtAttr;
//        rtAttr = CompilerUtils.getAnnotationValueFromSimpleName(annotations, "Deadline");
//        if (rtAttr == null) stream.print("new ABS.StdLib.Duration_InfDuration()"); else rtAttr.generateGo(stream);
//        stream.println(",");
//        rtAttr = CompilerUtils.getAnnotationValueFromSimpleName(annotations, "Cost");
//        if (rtAttr == null) stream.print("new ABS.StdLib.Duration_InfDuration()"); else rtAttr.generateGo(stream);
//        stream.println(",");
//        rtAttr = CompilerUtils.getAnnotationValueFromSimpleName(annotations, "Critical");
//        if (rtAttr == null) stream.print(ABSBool.class.getName() + ".FALSE"); else rtAttr.generateGo(stream);
//
//        stream.println(") {");
//        int i = 0;
//        for (Type t : paramTypes) {
//            stream.println(GoBackend.getQualifiedString(t) + " arg" + i + ";");
//            i++;
//        }
//
//        generateTaskGetArgsMethod(stream, paramTypes.size());
//        generateTaskInitMethod(stream, paramTypes);
//
//        stream.println("public java.lang.String methodName() {");
//        stream.println("return \"" + sig.getName() + "\";");
//        stream.println("}");
//
//        stream.println("public Object execute() {");
//        stream.print("return target." + GoBackend.getMethodName(sig.getName()) + "(");
//        for (i = 0; i < paramTypes.size(); i++) {
//            if (i > 0) stream.print(",");
//            stream.println("arg" + i);
//            if (paramTypes.get(i).isIntType()) stream.print(".truncate()");
//        }
//        stream.println(");");
//        stream.println("}");
//        stream.print("}.init");
//        if (args != null)
//            GoGeneratorHelper.generateArgs(stream,args, paramTypes);
//        else
//            GoGeneratorHelper.generateParamArgs(stream, params);
//        stream.println(")");
    }

    public static void generateAwaitAsyncCall(PrintStream stream, AwaitAsyncCall call) {
        final PureExp callee = call.getCallee();
        final List<PureExp> params = call.getParams();
        final MethodSig sig = call.getMethodSig();
        final List<Annotation> annotations = call.getAnnotations();
        // FIXME: implement await, assignment afterwards

//        OutputStream exprOStream = new ByteArrayOutputStream();
//        PrintStream exprStream = new PrintStream(exprOStream);
//        ClaimGuard guard = new ClaimGuard();
//        // Necessary temporary variables are written to "stream" and the
//        // await-expression is written to exprStream
//
//        // FIXME: implement await, assignment afterwards
//        guard.generateGoGuard(stream, exprStream);
//        stream.print(GoBackendConstants.ABSRUNTIME + ".await(");
//        stream.print(exprOStream.toString());
//        stream.println(");");

        generateAsyncCall(stream, null, callee, callee.getType(), params, null, sig, annotations);
    }

    private static void generateTaskInitMethod(PrintStream stream, final java.util.List<Type> paramTypes) {
        int i;
        stream.print("public " + abs.backend.java.lib.runtime.AsyncCall.class.getName() + "<?> init(");
        i = 0;
        for (Type t : paramTypes) {
            if (i > 0) stream.print(",");
            stream.print(GoBackend.getQualifiedString(t) + " _arg" + i);
            i++;
        }
        stream.println(") {");
        for (i = 0; i < paramTypes.size(); i++) {
            stream.println("arg" + i + " = _arg" + i + ";");
        }
        stream.println("return this;");
        stream.println("}");
    }

    private static void generateTaskGetArgsMethod(PrintStream stream, final int n) {
//        TODO
//        stream.println("public java.util.List<" + ABSValue.class.getName() + "> getArgs() {");
//        stream.println("return java.util.Arrays.asList(new " + ABSValue.class.getName() + "[] {");
//        generateArgStringList(stream, n);
//        stream.println("});");
//        stream.println("}");
    }

    private static void generateArgStringList(PrintStream stream, final int n) {
        for (int i = 0; i < n; i++) {
            if (i > 0) stream.print(",");
            stream.print("arg" + i);
        }
    }

    public static void generateClassDecl(PrintStream stream, final ClassDecl decl) {
        new ClassDeclGenerator(stream, decl).generate();
    }

    public static void generateMethodImpl(PrintStream stream, final MethodImpl m) {
        // Async variant TODO needed in Go ?
//        GoGeneratorHelper.generateAsyncMethod(stream, m);

        // Sync variant
        generateMethodSig(stream, m.getMethodSig(), false, "", "");
        generateMethodBody(stream, m, false);

        if (m.isForeign()) {
            generateFLIMethod(stream, m);
        }

    }

    public static void generateMethodBody(PrintStream stream, final MethodImpl m, boolean isFliMethod) {
        boolean addReturn = false;
        if (m.getMethodSig().getReturnType().getType().isUnitType()) {
            if (m.getBlock().getNumStmt() == 0 ||
                    (!(m.getBlock().getStmt(m.getBlock().getNumStmt() - 1) instanceof ReturnStmt))) {
                addReturn = true;
            }
        }

//        stream.println(" {");
//        TODO
//        stream.println("__ABS_checkSameCOG(); ");

        if (!isFliMethod && m.isForeign()) {
            stream.print("return this.");
            stream.print(FLI_METHOD_PREFIX + GoBackend.getMethodName(m.getMethodSig().getName()));
            GoGeneratorHelper.generateParamArgs(stream, m.getMethodSig().getParams());
            stream.println(";");
        } else {
//            TODO
//            stream.println("if (__ABS_getRuntime().debuggingEnabled()) {");
//            stream.println(Task.class.getName() + "<?> __ABS_currentTask = __ABS_getRuntime().getCurrentTask();");
//            stream.println("__ABS_currentTask.newStackFrame(this, \"" + m.getMethodSig().getName() + "\");");
//            for (ParamDecl d : m.getMethodSig().getParams()) {
//                stream.print("__ABS_currentTask.setLocalVariable(");
//                stream.println("\"" + d.getName() + "\"," + d.getName() + ");");
//            }
//            stream.println("}");
//            TODO
            m.getBlock().generateGo(stream, addReturn);

        }
//        stream.println("}");
    }

    private static void generateFLIMethod(PrintStream stream, MethodImpl m) {
        generateMethodSig(stream, m.getMethodSig(), false, "", FLI_METHOD_PREFIX);
        generateMethodBody(stream, m, true);
    }

    /**
     * removes the gen folder and all its contents
     *
     * @param code
     */
    public static void cleanGenFolder(GoCode code) {
        File genDir = code.getSrcDir();
        cleanGenFolderRecursively(genDir);
    }

    private static void cleanGenFolderRecursively(File dir) {
        if (dir == null) throw new IllegalArgumentException();
        if (!dir.exists()) return;
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                cleanGenFolderRecursively(f);
            } else {
                if (f.getAbsolutePath().endsWith(".go")
                        || f.getAbsolutePath().endsWith(".class")) {
                    f.delete();
                }
            }
        }
        dir.delete();
    }

    public static void printEscapedString(PrintStream stream, String content) {
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            switch (c) {
                case '\t':
                    stream.append('\\').append('t');
                    break;
                case '\b':
                    stream.append('\\').append('b');
                    break;
                case '\n':
                    stream.append('\\').append('n');
                    break;
                case '\r':
                    stream.append('\\').append('r');
                    break;
                case '\f':
                    stream.append('\\').append('f');
                    break;
                case '\'':
                    stream.append('\\').append('\'');
                    break;
                case '\"':
                    stream.append('\\').append('\"');
                    break;
                case '\\':
                    stream.append('\\').append('\\');
                    break;
                default:
                    stream.append(c);
            }
        }
    }

    public static void generateExprGuard(ExpGuard expGuard, PrintStream beforeAwaitStream, PrintStream stream) {
        PureExp expr = expGuard.getPureExp();

        replaceLocalVariables((PureExp) expr.copy(), beforeAwaitStream);

//        TODO
//        stream.print("new " + GoBackendConstants.EXPGUARD + "() { public " + ABSBool.class.getName() + " evaluateExp() { return ");
//        TODO
//        expGuard.getPureExp().generateGo(stream);
        stream.print("; }}");
    }

    /**
     * replace all uses of local variables and parameters by a use of a newly introduced
     * temporary final local variable
     */
    private static void replaceLocalVariables(ASTNode<?> astNode, PrintStream beforeAwaitStream) {
        if (isLocalVarUse(astNode)) {
            VarUse v = (VarUse) astNode;
            replaceVarUse(beforeAwaitStream, v, (TypedVarOrFieldDecl) v.getDecl());
        } else {
            // process children:
            for (int i = 0; i < astNode.getNumChild(); i++) {
                ASTNode<?> child = astNode.getChild(i);
                replaceLocalVariables(child, beforeAwaitStream);
            }
        }
    }

    /***
     * checks if astNode is a use of a local variable or parameter
     */
    private static boolean isLocalVarUse(ASTNode<?> astNode) {
        if (astNode instanceof VarUse) {
            VarUse v = (VarUse) astNode;
            VarOrFieldDecl decl = v.getDecl();
            if (decl instanceof VarDecl || decl instanceof ParamDecl) {
                return !(decl.getParent() instanceof LetExp);
            }
        }
        return false;
    }

    /**
     * replaces a varUse v of the local variable vDecl by a new temporary variable, which will be
     * written to beforeAwaitStream
     */
    private static void replaceVarUse(PrintStream beforeAwaitStream, VarUse v, TypedVarOrFieldDecl vDecl) {
        String name = GoBackend.getVariableName(vDecl.getName());
        String tempName = "temp$" + tempCounter + "$" + name;
        tempCounter = Math.max(tempCounter + 1, 0);
        // copy value of variable to temporary, final variable
        beforeAwaitStream.print("final ");
//        TODO
//        vDecl.getAccess().generateGo(beforeAwaitStream);
        beforeAwaitStream.print(" " + tempName + " = " + name + ";");
        // replace variable name with name of temporary variable
        v.setName(tempName);
    }

    public static void generateAwaitStmt(AwaitStmt awaitStmt, PrintStream stream) {
        OutputStream exprOStream = new ByteArrayOutputStream();
        PrintStream exprStream = new PrintStream(exprOStream);
        // Necessary temporary variables are written to "stream" and the 
        // await-expression is written to exprStream
//        TODO
//        awaitStmt.getGuard().generateGoGuard(stream, exprStream);
        stream.print(GoBackendConstants.ABSRUNTIME + ".await(");
        stream.print(exprOStream.toString());
        stream.println(");");
    }

    public static String generateUserSchedulingStrategy(NewExp exp, PureExp scheduler) {
//        TODO
//        String className = "UserSchedulingStrategy_" + GoBackend.getRandomName();
//        PrintStream stream = null;
//        try {
//            GoCode.Package pkg = exp.getModuleDecl().getGoPackage();
//            File file = pkg.createGoFile(className);
//            stream = new CodeStream(file);
//
//            stream.println("package " + pkg.packageName + ";");
//            stream.print("public final class " + className);
//            stream.println(" extends " + UserSchedulingStrategy.class.getName() + " {");
//
//            stream.println("public synchronized " + ABSProcess.class.getName() + " userschedule(Object q) {");
//            stream.println("ABS.StdLib.List<" + ABSProcess.class.getName() + "> queue = (ABS.StdLib.List<" + ABSProcess.class.getName() + ">) q;");
//
//            // call the given scheduling function
//            // here goes whatever is specified in the Scheduler annotation
//            // i.e. a function call or some other code that returns a Process
//            stream.println("// user-specified scheduler expression");
//            stream.print("return ");
//            scheduler.generateGo(stream);
//            stream.println(";");
//            stream.println("}");
//            stream.println("}");
//
//            // connect generated TaskSchedulingStrategy to the cog's TaskScheduler
//            return pkg.packageName + "." + className;
//
//        } catch (GoCodeGenerationException e) {
//            // TODO properly handle exception
//            e.printStackTrace();
//        } catch (IOException e) {
//            // TODO properly handle exception
//            e.printStackTrace();
//        } finally {
//            if (stream != null)
//                stream.close();
//        }
        return null;
    }

}
