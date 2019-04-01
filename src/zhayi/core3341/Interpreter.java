package zhayi.core3341;
import zhayi.core3341.CoreError.InterpreterException;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Interpreter entry point for CSE 3341 Project PA3
 *
 * @author Yi Zhang
 */
public final class Interpreter {

    private Logger interpreterLog;

    private Tokenizer tokenizer;
    private Parser parser;

    /**
     * Setup logger
     */
    private Interpreter() {
        interpreterLog = Logger.getLogger("Interpreter");
        interpreterLog.setLevel(Level.SEVERE);
        interpreterLog.fine("Interpreter logger created");
    }

    /**
     * Entry point
     * @param args command line arguments
     */
    public static void main(String[] args) {
        Interpreter interpreter = new Interpreter();

        try {
            if (args.length < 2) {
                throw new InterpreterException(
                        "Invalid number of arguments\n" +
                                "Usage: java -jar Core.jar <option> <test-file>\n" +
                                "where <option> includes\n" +
                                "\t-t\tRun tokenizer only\n" +
                                "\t-p\tRun parser only\n" +
                                "\t-i\tRun the full interpreter"
                );
            }

            switch (args[0]) {
                case "-t":
                    interpreter.tokenizer = new Tokenizer();
                    interpreter.tokenizer.tokenize(args[1]);
                    for (Token token : interpreter.tokenizer)
                        System.out.println(token);
                    System.exit(0);
                case "-p":
                    interpreter.parser = new Parser();
                    interpreter.parser.parse(args[1]);
                    interpreter.parser.printParser();
                    System.exit(0);
                case "-i":
                    interpreter.parser = new Parser();
                    interpreter.parser.parse(args[1]);
                    interpreter.parser.execProg(interpreter.interpreterLog);
                    System.exit(0);
                default:
                    throw new InterpreterException(
                            "Invalid number of arguments\n" +
                                    "Usage: java -jar Core.jar <option> <test-file>\n" +
                                    "where <option> includes\n" +
                                    "\t-t\tRun tokenizer only\n" +
                                    "\t-p\tRun parser only\n" +
                                    "\t-i\tRun the full interpreter"
                    );
            }
        } catch (InterpreterException ex) {
            interpreter.interpreterLog.severe(ex.getLocalizedMessage());
            System.exit(ex.hashCode());
        }
    }
}
