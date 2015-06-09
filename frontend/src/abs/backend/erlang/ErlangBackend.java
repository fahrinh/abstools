/** 
 * This file is licensed under the terms of the Modified BSD License.
 */
package abs.backend.erlang;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;

import abs.common.NotImplementedYetException;
import abs.frontend.ast.Model;
import abs.frontend.parser.Main;

/**
 * Translates given ABS Files to an Erlang program
 * 
 * @author Georg Göri
 * 
 */
public class ErlangBackend extends Main {

    private static final File DEFAULT_DEST_DIR = new File("gen/erl/");

    public static void main(final String... args) {
        try {
            new ErlangBackend().compile(args);
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

    @Override
    public List<String> parseArgs(String[] args) {
        List<String> restArgs = super.parseArgs(args);
        List<String> remainingArgs = new ArrayList<String>();

        for (int i = 0; i < restArgs.size(); i++) {
            String arg = restArgs.get(i);
            if (arg.equals("-erlang")) {
                // nothing to do
            } else {
                remainingArgs.add(arg);
            }
        }

        return remainingArgs;
    }

    @Override
    protected void printUsage() {
        super.printUsage();
        System.out.println("Erlang Backend:\n");
    }

    private void compile(String[] args) throws Exception {
        final Model model = parse(args);
        if (model.hasParserErrors() || model.hasErrors() || model.hasTypeErrors())
            printParserErrorAndExit();
        compile(model, DEFAULT_DEST_DIR, verbose);
    }

    public static void compile(Model m, File destDir, boolean verbose) throws IOException, InterruptedException {
        if (verbose) System.out.println("Generating Erlang code...");
        ErlApp erlApp = new ErlApp(destDir);
        m.generateErlangCode(erlApp);
        erlApp.close();
	String[] rebarProgram = new String[] {"escript", "bin/rebar", "compile"};
        Process p = Runtime.getRuntime().exec(rebarProgram, null, destDir);
        p.waitFor();
        if (verbose) IOUtils.copy(p.getInputStream(), System.out);
    }

}
