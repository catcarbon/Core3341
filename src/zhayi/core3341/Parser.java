package zhayi.core3341;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Parser entry point for CSE 3341 Project PA2
 *
 * @author Yi Zhang
 */
public final class Parser {
    private ProgNode prog;
    private Tokenizer tokenizer;

    private static Logger parseLog;
    private static Logger tokenLog;

    Parser() {
        prog = new ProgNode();
        tokenizer = new Tokenizer();
        parseLog = Logger.getLogger("Parser");
        parseLog.setLevel(Level.SEVERE);
        parseLog.fine("Parser logger created");
        tokenLog = Logger.getLogger("Tokenizer");
        tokenLog.setLevel(Level.SEVERE);
        tokenLog.fine("Tokenizer logger created");
    }

    private void initTokenStream(String path) {
        try {
            tokenizer.tokenize(path);
        } catch (CoreError.InvalidTokenException ex) {
            tokenLog.severe(ex.getLocalizedMessage());
            System.exit(ex.hashCode());
        }
    }

    void execProg(Logger log) {
        try {
            prog.execProg();
        } catch (CoreError.InterpreterException ex) {
            log.severe(ex.getLocalizedMessage());
            System.exit(ex.hashCode());
        }
    }

    void parse(String path) {
        initTokenStream(path);
        try {
            prog.parseProg(tokenizer);
        } catch (CoreError.InterpreterException ex) {
            parseLog.severe(ex.getLocalizedMessage());
            System.exit(ex.hashCode());
        }

    }

    void printParser() {
        prog.print();
    }

    public static void main(String[] args) {
        Parser parser = new Parser();
        parser.parse(args[0]);
        parser.printParser();
    }
}
