/** 
 * Copyright (c) 2009-2011, The HATS Consortium. All rights reserved. 
 * This file is licensed under the terms of the Modified BSD License.
 */
package abs.backend.go;

import java.io.*;

import static java.lang.Runtime.getRuntime;

class GoCompiler {
    private static final String DEFAULT_PREFIX = "-encoding " + GoBackend.CHARSET+" -source 5 -nowarn -noExit ";

    public static void main(String... args) throws GoCodeGenerationException {
        if (!compile(args))
            System.exit(1);
    }

    public static boolean compile(String[] args) throws GoCodeGenerationException {
        StringBuffer sb = new StringBuffer();
        for (String s : args) {
            sb.append(s+" ");
        }
        return compile(sb.toString());
    }
    
    public static boolean compile(String args) throws GoCodeGenerationException {
        StringWriter outWriter = new StringWriter();
        StringWriter errWriter = new StringWriter();
//        boolean res = BatchCompiler.compile(DEFAULT_PREFIX + args, new PrintWriter(outWriter), new PrintWriter(errWriter), null);
        Process versionCheck = null;
        try {
            versionCheck = getRuntime().exec(new String[] { "go", "run", args });
            versionCheck.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
        BufferedReader ir = new BufferedReader(new InputStreamReader(versionCheck.getInputStream()));
//        if (!res) {
//            String errorString = errWriter.toString();
//            throw new GoCodeGenerationException("There seems to be a bug in the ABS Java backend. " +
//                    "The generated code contains errors:\n" + errorString);
//        }
        return true;
    }
}
