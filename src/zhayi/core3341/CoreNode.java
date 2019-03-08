package zhayi.core3341;

import java.io.PrintStream;
import java.util.*;

import zhayi.core3341.CoreError.*;

/**
 * Node Implementation of CORE language for CSE 3341 Project PA2
 *
 * @author Yi Zhang
 * @email zhang.5281@osu.edu
 */
public abstract class CoreNode {
    /**
     * Syntax error reporting template.
     * raiseSyntax, raiseConsumeMismatch, raiseUnexpected
     */
    private static String SYNTAX_TEMPLATE = "Syntax Error: [Line %d] %s";

    /**
     * Context error reporting template.
     * checkUndeclared, checkRedeclared
     */
    private static String CONTEXT_TEMPLATE = "Context Error: [Line %d] %s";

    /**
     * Indent style, i.e. two whitespaces by assignment statement.
     */
    private String indent;

    /**
     * Code block level
     */
    int level;

    /**
     * Reference to root {@code ProgNode} for accessing symbol table.
     */
    ProgNode prog;

    /**
     * Unused. Extends the ability to output result to stream other than {@code System.out}.
     */
    PrintStream out;

    /**
     * Default constructor.
     */
    CoreNode() {
        indent = "  ";
        level = 1;
    }

    /**
     * Adds access to symbol table.
     * @param p Top level {@code ProgNode}
     */
    CoreNode(ProgNode p) {
        this();
        prog = p;
    }

    /**
     * Gives appropriate indent level for each line of code.
     * @return {@code indent} multiplied by {@code level}
     */
    protected String getIndent() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level; i++)
            sb.append(indent);
        return sb.toString();
    }

    /**
     * NOTE:    All consume methods asserts {@code Tokenizer} instance not null.
     *          If a parse method (uses {@code Tokenizer}) first calls to any consume method,
     *          it need not assert {@code Tokenizer} not null
     *
     * Consume one token and try match its code. Moves to the next token on success.
     * @param t {@code Tokenizer} instance
     * @param code {@code Token} type to be matched
     * @return {@code t.getCurrent()} if {@code t.getCurrent().code == code}
     * @throws InterpreterException either {@code UnexpectedTokenException} if encountered EOF
     *          or {@code ConsumeMismatchException}
     */
    Token matchConsume(Tokenizer t, int code) throws InterpreterException {
        assert(t != null);

        Token curr = t.getCurrent();
        if (code != Token.EOF && curr.code == Token.EOF) {
            raiseUnexpected(curr.line, "Unexpected EOF while scanning for " + code);
        }
        if (curr.code != code) {
            String info = String.format("Expected token %d, got '%s'", code, curr.name);
            raiseConsumeMismatch(curr.line, info);
        }
        t.next();
        return curr;
    }

    /**
     * Test (peek) if the next token matches given code. Does not move to the next token.
     * @param t {@code Tokenizer} instance
     * @param code {@code Token} type to be matched
     * @return true if {@code t.getCurrent().code == code} else false
     */
    boolean testConsume(Tokenizer t, int code) {
        assert(t != null);

        return t.getCurrent().code == code;
    }

    /**
     * Consume one token and try match its code within a range. Moves to the next token on success.
     * Only used for comp-op matching in PA2.
     * @param t {@code Tokenizer} instance
     * @param min lower range
     * @param max higher range, inclusive
     * @return {@code t.getCurrent()} if {@code t.getCurrent().code >= min && t.getCurrent().code <= max}
     * @throws InterpreterException either {@code UnexpectedTokenException} if encountered EOF
     *          or {@code ConsumeMismatchException}
     */
    Token rangeConsume(Tokenizer t, int min, int max) throws InterpreterException {
        assert(t != null);

        Token curr = t.getCurrent();
        if (curr.code < min || curr.code > max) {
            if (curr.code == Token.EOF) {
                String info = String.format("Unexpected EOF while scanning for token between %d and %d", min, max);
                raiseUnexpected(curr.line, info);
            }
            String info = String.format("Expected token between %d and %d, got %d", min, max, curr.code);
            raiseConsumeMismatch(t.getCurrent().line, info);
        }
        t.next();
        return curr;
    }

    /**
     * @deprecated
     * Raise generic syntax error.
     * @param line line number of occurrence
     * @param msg explanation of error
     * @throws InterpreterException to be caught and printed by {@code Parser}
     */
    @Deprecated
    void raiseSyntax(int line, String msg) throws InterpreterException {
        String info = String.format(SYNTAX_TEMPLATE, line, msg);
        throw new InterpreterException(info);
    }

    /**
     * Raise when token mismatches. Used only by consume methods.
     * @param line line number of occurrence
     * @param msg explanation of error
     * @throws ConsumeMismatchException to be caught and printed by {@code Parser}
     */
    private void raiseConsumeMismatch(int line, String msg) throws ConsumeMismatchException {
        String info = String.format(SYNTAX_TEMPLATE, line, msg);
        throw new ConsumeMismatchException(info);
    }

    /**
     * Raise when unexpected token encountered.
     * @param line line number of occurrence
     * @param msg explanation of error
     * @throws UnexpectedTokenException to be caught and printed by {@code Parser}
     */
    void raiseUnexpected(int line, String msg) throws UnexpectedTokenException {
        String info = String.format(SYNTAX_TEMPLATE, line, msg);
        throw new UnexpectedTokenException(info);
    }

    /**
     * Throws exception if a symbol has been already declared.
     * @param curr {@code Token} with type {@code Token.ID}
     * @throws RedeclaredException if {@code prog.vars.containsKey(curr.name)}
     */
    void checkRedeclared(Token curr) throws RedeclaredException {
        assert(curr != null);

        if (prog.vars.containsKey(curr.name)) {
            String declared = String.format("%s already declared", curr.name);
            String info = String.format(CONTEXT_TEMPLATE, curr.line, declared);
            throw new RedeclaredException(info);
        }
    }

    /**
     * Throws exception if a symbol has not been declared.
     * @param curr {@code Token} with type {@code Token.ID}
     * @throws UndeclaredException if {@code !prog.vars.containsKey(curr.name)}
     */
    void checkUndeclared(Token curr) throws UndeclaredException {
        assert(curr != null);

        if (!prog.vars.containsKey(curr.name)) {
            String undeclared = String.format("Using undeclared variable %s", curr.name);
            String info = String.format(CONTEXT_TEMPLATE, curr.line, undeclared);
            throw new UndeclaredException(info);
        }
    }
}

/**
 * Root level node for each program, contains symbol table.
 */
final class ProgNode extends CoreNode {
    /**
     * Max of 20 distinct user-defined variables. Not enforced.
     */
    private static int MAX_CAPACITY = 20;

    /**
     * Declaration sequence node.
     */
    private DeclSeqNode decls;

    /**
     * Statement sequence node.
     */
    private StmtSeqNode stmts;

    /**
     * Program symbol table.
     */
    HashMap<String, Integer> vars;

    /**
     * Default constructor.
     */
    ProgNode() {
        vars = new HashMap<>();
        prog = this;
    }

    /**
     * Prints the program by recursively calling print.
     */
    void print() {
        assert(decls != null && stmts != null);

        System.out.println("program ");
        decls.print();
        System.out.printf("%sbegin\n", getIndent());
        stmts.print();
        System.out.printf("%send\n", getIndent());
    }

    /**
     * Fills content of {@code this.decls, this.stmts, this.vars} by consuming {@code Tokenizer}.
     * @param t {@code Tokenizer} instance
     * @throws InterpreterException if any recursive parse call or {@code matchConsume(Tokenizer, int)} failed
     */
    void parseProg(Tokenizer t) throws InterpreterException {
        assert(t != null);

        matchConsume(t, Token.PROGRAM);
        decls = new DeclSeqNode(this);
        decls.parseDeclSeq(t);
        matchConsume(t, Token.BEGIN);
        stmts = new StmtSeqNode(this, level);
        stmts.parseStmtSeq(t);
        matchConsume(t, Token.END);
        matchConsume(t, Token.EOF);
    }
}

/**
 * Block level node that contains sequence of {@code DeclNode} in {@code ArrayList<T>}
 */
final class DeclSeqNode extends CoreNode {
    /**
     * All declare nodes should be at level 1 by project definition.
     */
    static int DEFAULT_LEVEL = 1;

    /**
     * Stores each individual line of declaration.
     */
    private ArrayList<DeclNode> decls;

    /**
     * Constructor must be called with {@code ProgNode} to reference symbol table.
     * @param p Root {@code ProgNode}
     */
    DeclSeqNode(ProgNode p) {
        super(p);
        level = DEFAULT_LEVEL;
        decls = new ArrayList<>();
    }

    /**
     * Calls {@code print()} for each {@code DeclNode} in {@code this.decls}.
     */
    public void print() {
        for (DeclNode n: decls) n.print();
    }

    /**
     * Fills content of {@code this.decls} by consuming {@code Tokenizer}.
     * @param t {@code Tokenizer} instance
     * @throws InterpreterException if any recursive call to {@code parseDecl()} failed
     */
    void parseDeclSeq(Tokenizer t) throws InterpreterException {
        while (testConsume(t, Token.INT)) {
            DeclNode curr = new DeclNode(prog);
            curr.parseDecl(t);
            decls.add(curr);
        }
    }
}

/**
 * Line level node that instantiates symbols in symbol table and stores symbol names declared on its line.
 */
final class DeclNode extends CoreNode {
    /**
     * Stores symbol names declared on this line of code.
     */
    private ArrayList<String> lineVars;

    /**
     * Constructor must be called with {@code ProgNode} to reference symbol table.
     * @param p Root {@code ProgNode}
     */
    DeclNode(ProgNode p) {
        super(p);
        level = DeclSeqNode.DEFAULT_LEVEL;
        lineVars = new ArrayList<>();
    }

    /**
     * Print declaration line. All symbols declared on this line will be shown.
     */
    public void print() {
        System.out.printf("%sint %s;\n", getIndent(), String.join(", ", lineVars));
    }

    /**
     * Fills content of {@code this.lineVar} by consuming {@code Tokenizer}.
     * @param t {@code Tokenizer} instance
     * @throws InterpreterException
     *          {@code RedeclaredException} if {@code checkRedeclared()} failed;
     *          {@code ConsumeMismatchException} if any {@code matchConsume()} failed.
     */
    void parseDecl(Tokenizer t) throws InterpreterException {
        matchConsume(t, Token.INT);

        Token curr = matchConsume(t, Token.ID);
        do {
            checkRedeclared(curr);
            prog.vars.put(curr.name, null);
            lineVars.add(curr.name);

            if(!this.testConsume(t, Token.COMMA)) break;
            this.matchConsume(t, Token.COMMA);
            curr = matchConsume(t, Token.ID);
        } while (true);

        this.matchConsume(t, Token.SEMICOL);
    }
}

/**
 * Block level node that contains sequence of {@code StmtNode} in {@code ArrayList<T>}
 */
final class StmtSeqNode extends CoreNode {
    /**
     * Default block level.
     */
    static int DEFAULT_LEVEL = 1;

    /**
     * Stores each individual line of {@code StmtNode}.
     */
    private ArrayList<StmtNode> stmts;

    /**
     * Constructor must be called with {@code ProgNode} to reference symbol table.
     * @param p Root {@code ProgNode}
     */
    private StmtSeqNode(ProgNode p) {
        super(p);
        level = DEFAULT_LEVEL;
        stmts = new ArrayList<>();
    }

    /**
     * Constructor with alternate block level.
     * @param p Root {@code ProgNode}
     * @param l alternate block level
     */
    StmtSeqNode(ProgNode p, int l) {
        this(p);
        level = l;
    }

    /**
     * Calls {@code print()} for each {@code StmtNode} in {@code this.stmts}.
     */
    public void print() {
        for (StmtNode n: stmts) n.print();
    }

    /**
     * Iteratively creates and calls to parse {@code StmtNode} until a terminating {@code Token} is reached.
     * @param t {@code Tokenizer} instance
     * @throws InterpreterException if any recursive parse call fails;
     *          or any {@code curr} isn't a {@code StmtNode} starting or {@code StmtSeqNode} terminating {@code Token}.
     */
    void parseStmtSeq(Tokenizer t) throws InterpreterException {
        assert(t != null);

        Token curr = t.getCurrent();
        while (curr.code != Token.EOF) {
            StmtNode stmt = null;
            switch (curr.code) {
                case Token.IF:
                    stmt = new StmtNode(prog, StmtNode.StmtType.IF, level + 1);
                    stmt.parseIfLoop(t);
                    break;
                case Token.WHILE:
                    stmt = new StmtNode(prog, StmtNode.StmtType.LOOP, level + 1);
                    stmt.parseIfLoop(t);
                    break;
                case Token.READ:
                    stmt = new StmtNode(prog, StmtNode.StmtType.IN, level + 1);
                    stmt.parseInOut(t);
                    break;
                case Token.WRITE:
                    stmt = new StmtNode(prog, StmtNode.StmtType.OUT, level + 1);
                    stmt.parseInOut(t);
                    break;
                case Token.ID:
                    stmt = new StmtNode(prog, StmtNode.StmtType.ASSIGN, level + 1);
                    stmt.parseAssign(t);
                    break;
                case Token.END:
                    return;
                case Token.ELSE:
                    return;
                default:
                    this.raiseUnexpected(curr.line, String.format("Expected statement, got '%s'", curr.name));
            }
            if (stmt != null) stmts.add(stmt);
            else throw new Error("Variable stmts reference lost after switch");
            curr = t.getCurrent();
        }
    }
}

/**
 * Line level statement node.
 */
final class StmtNode extends CoreNode {
    /**
     * Statement type as defined by CORE specification.
     */
    private StmtType type;

    /**
     * Assign type statement id.
     */
    private String assignId;

    /**
     * Assign type statement expression.
     */
    private ExpNode assignExp;

    /**
     * If or loop type statement condition.
     */
    private CondNode if_loopCond;

    /**
     * Statement block for if or loop type statement.
     */
    private StmtSeqNode if_loopStmtSeq;

    /**
     * Alternate statement block for if-else type statement.
     */
    private StmtSeqNode elseStmtSeq;

    /**
     * Accessed symbols for in or out type statement.
     */
    private ArrayList<String> in_outIdList;

    /**
     * Constructor must be called with {@code ProgNode} to reference symbol table.
     * @param p Root {@code ProgNode}
     * @param t Type of statement
     */
    private StmtNode(ProgNode p, StmtType t) {
        super(p);
        level = StmtSeqNode.DEFAULT_LEVEL;
        type = t;
    }

    /**
     * Constructor with alternate level.
     * @param p Root {@code ProgNode}
     * @param t Type of statement
     * @param l Alternate level
     */
    StmtNode(ProgNode p, StmtType t, int l) {
        this(p, t);
        level = l;
    }

    /**
     * Print statement. If statement contains {@code StmtSeqNode}, recursively call print to that node.
     */
    void print() {
        assert(type != null);

        switch (type) {
            case ASSIGN:
                System.out.printf("%s%s = %s;\n", getIndent(), assignId, assignExp.getExp());
                break;
            case IF:
                System.out.printf("%sif %s then\n", getIndent(), if_loopCond.getCond());
                this.if_loopStmtSeq.print();
                if (this.elseStmtSeq != null) {
                    System.out.printf("%selse\n", getIndent());
                    this.elseStmtSeq.print();
                }
                System.out.printf("%send;\n", getIndent());
                break;
            case LOOP:
                System.out.printf("%swhile %s loop\n", getIndent(), if_loopCond.getCond());
                this.if_loopStmtSeq.print();
                System.out.printf("%send;\n", getIndent());
                break;
            case IN:
                System.out.printf("%sread %s;\n", getIndent(), String.join(", ", in_outIdList));
                break;
            case OUT:
                System.out.printf("%swrite %s;\n", getIndent(), String.join(", ", in_outIdList));
                break;
            default:
                throw new IllegalStateException("Uninitialized stmt not caught by assert");
        }
    }

    /**
     * Parse if type statement.
     * @param t {@code Tokenizer} instance
     * @throws InterpreterException if any {@code matchConsume()} failed or any recursive parse call failed.
     */
    void parseIfLoop(Tokenizer t) throws InterpreterException {
        if (type == StmtType.IF) matchConsume(t, Token.IF);
        else if (type == StmtType.LOOP) matchConsume(t, Token.WHILE);
        else throw new Error("Unexpected call to parseIfLoop on type " + type);

        if_loopCond = new CondNode(prog);
        if_loopCond.parseCond(t);

        if (type == StmtType.IF) matchConsume(t, Token.THEN);
        else matchConsume(t, Token.LOOP);

        if_loopStmtSeq = new StmtSeqNode(prog, level);
        if_loopStmtSeq.parseStmtSeq(t);

        if (type == StmtType.IF && testConsume(t, Token.ELSE)) {
            matchConsume(t, Token.ELSE);
            elseStmtSeq = new StmtSeqNode(prog, level);
            elseStmtSeq.parseStmtSeq(t);
        }

        matchConsume(t, Token.END);
        matchConsume(t, Token.SEMICOL);
    }

    /**
     * Parse in/out type statement.
     * @param t {@code Tokenizer} instance
     * @throws InterpreterException if any {@code matchConsume()} failed or any recursive parse call failed.
     */
    void parseInOut(Tokenizer t) throws CoreError.InterpreterException {
        if (type == StmtType.IN) matchConsume(t, Token.READ);
        else if (type == StmtType.OUT) matchConsume(t, Token.WRITE);
        else throw new Error("Unexpected call to parseInOut on type " + type);

        in_outIdList = new ArrayList<>();

        do {
            Token curr = matchConsume(t, Token.ID);
            checkUndeclared(curr);
            in_outIdList.add(curr.name);

            if (!testConsume(t, Token.COMMA)) break;
            matchConsume(t, Token.COMMA);
        } while (testConsume(t, Token.ID));

        matchConsume(t, Token.SEMICOL);
    }

    /**
     * Parse assign type statement.
     * @param t {@code Tokenizer} instance
     * @throws InterpreterException if any {@code matchConsume()} failed or any recursive parse call failed.
     */
    void parseAssign(Tokenizer t) throws CoreError.InterpreterException {
        if (type != StmtType.ASSIGN)
            throw new Error("Unexpected call to parseAssign on type " + type);

        Token curr = matchConsume(t, Token.ID);
        checkUndeclared(curr);
        assignId = curr.name;

        matchConsume(t, Token.ASSIGN);

        assignExp = new ExpNode(prog);
        assignExp.parseExp(t);

        matchConsume(t, Token.SEMICOL);
    }

    /**
     * Statement type enums as defined by CORE specification.
     */
    enum StmtType {
        ASSIGN,
        IF,
        LOOP,
        IN,
        OUT
    }
}

/**
 * Sub-line level expression node.
 */
final class ExpNode extends CoreNode {
    /**
     * Expression type as defined by CORE specification.
     */
    private ExpType type;

    /**
     * Only term node, or left hand side term node.
     */
    private TermNode term;

    /**
     * Right hand side expression node for plus/minus type expression.
     */
    private ExpNode exp;

    /**
     * Constructor must be called with root {@code ProgNode} to access symbol table.
     * @param p Root {@code ProgNode}
     */
    ExpNode(ProgNode p) {
        super(p);
    }

    /**
     * Return a sub-line string representation of {@code this}
     * @return formatted expression representation defined by PA2 statement
     */
    String getExp() {
        assert(type != null);

        switch (this.type) {
            case TERM:
                return this.term.getTerm();
            case PLUS:
                return String.format("%s + %s", this.term.getTerm(), this.exp.getExp());
            case MINUS:
                return String.format("%s - %s", this.term.getTerm(), this.exp.getExp());
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public String toString() {
        return getExp();
    }

    /**
     * Parse expression node.
     * @param t {@code Tokenizer} instance
     * @throws InterpreterException if {@code matchConsume()} failed or any recursive parse call failed.
     */
    void parseExp(Tokenizer t) throws InterpreterException {
        term = new TermNode(prog);
        term.parseTerm(t);
        if (testConsume(t, Token.PLUS)) {
            type = ExpType.PLUS;
            matchConsume(t, Token.PLUS);
            exp = new ExpNode(prog);
            exp.parseExp(t);
        } else if (testConsume(t, Token.MINUS)) {
            type = ExpType.MINUS;
            matchConsume(t, Token.MINUS);
            exp = new ExpNode(prog);
            exp.parseExp(t);
        } else {
            type = ExpType.TERM;
        }
    }

    /**
     * Expression type enums as defined in CORE specification.
     */
    enum ExpType {
        TERM,
        PLUS,
        MINUS
    }
}

/**
 * Sub-line level term node.
 */
final class TermNode extends CoreNode {
    /**
     * Term type as defined by CORE specification.
     */
    private TermType type;

    /**
     * Factor node.
     */
    private FacNode fac;

    /**
     * Trail term node for multiply type term.
     */
    private TermNode term;

    /**
     * Constructor must be called with root {@code ProgNode} to access symbol table.
     * @param p Root {@code ProgNode}
     */
    TermNode(ProgNode p) {
        super(p);
    }

    /**
     * Return a sub-line string representation of {@code this}
     * @return formatted term representation defined by PA2 statement
     */
    String getTerm() {
        assert(type != null);

        switch (this.type) {
            case FAC:
                return this.fac.getFac();
            case MUL:
                return String.format("%s * %s", this.fac.getFac(), this.term.getTerm());
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public String toString() {
        return getTerm();
    }

    /**
     * Parse term node.
     * @param t {@code Tokenizer} instance
     * @throws InterpreterException if {@code matchConsume()} failed or any recursive parse call failed.
     */
    void parseTerm(Tokenizer t) throws InterpreterException {
        fac = new FacNode(prog);
        fac.parseFac(t);
        if (testConsume(t, Token.STAR)) {
            type = TermType.MUL;
            matchConsume(t, Token.STAR);
            term = new TermNode(prog);
            term.parseTerm(t);
        } else {
            type = TermType.FAC;
        }
    }

    /**
     * Term type enums as defined in CORE specification.
     */
    enum TermType {
        FAC,
        MUL
    }
}

/**
 * Sub-line level factor node.
 */
final class FacNode extends CoreNode {
    /**
     * Factor type as defined by CORE specification.
     */
    private FacType type;

    /**
     * Id for id type factor.
     */
    private String id;

    /**
     * Integer value for numeric type factor.
     */
    private int value;

    /**
     * Expression node for expression type factor.
     */
    private ExpNode exp;

    /**
     * Constructor must be called with root {@code ProgNode} to access symbol table.
     * @param p Root {@code ProgNode}
     */
    FacNode(ProgNode p) {
        super(p);
    }

    /**
     * Return a sub-line string representation of {@code this}
     * @return formatted factor representation defined by PA2 statement
     */
    String getFac() {
        assert (type != null);

        switch (type) {
            case ID:
                return id;
            case NUM:
                return Integer.toString(value);
            case EXP:
                return String.format("( %s )", exp.getExp());
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public String toString() {
        return getFac();
    }

    /**
     * Parse factor node.
     * @param t {@code Tokenizer} instance
     * @throws InterpreterException if {@code matchConsume()} failed or any recursive parse call failed.
     */
    void parseFac(Tokenizer t) throws InterpreterException {
        assert(t != null);

        Token curr = t.getCurrent();
        switch (curr.code) {
            case Token.ID:
                type = FacType.ID;
                matchConsume(t, Token.ID);
                checkUndeclared(curr);
                id = curr.name;
                break;
            case Token.NUM:
                type = FacType.NUM;
                matchConsume(t, Token.NUM);
                value = Integer.parseInt(curr.name);
                break;
            case Token.LPAREN:
                type = FacType.EXP;
                matchConsume(t, Token.LPAREN);
                exp = new ExpNode(prog);
                exp.parseExp(t);
                matchConsume(t, Token.RPAREN);
                break;
            default:
                raiseUnexpected(curr.line, "Expected factor, got " + curr.name);
        }
    }

    /**
     * Factor type enums as defined in CORE specification.
     */
    enum FacType {
        NUM,
        ID,
        EXP
    }
}

/**
 * Sub-line level condition node.
 */
final class CondNode extends CoreNode {
    /**
     * Condition type as defined by CORE specification.
     */
    private CondType type;

    /**
     * Compare node for compare or not type condition.
     */
    private CompNode comp;

    /**
     * Left hand side condition for and/or type condition.
     */
    private CondNode cond1;

    /**
     * Right hand side condition for and/or type condition.
     */
    private CondNode cond2;

    /**
     * Constructor must be called with root {@code ProgNode} to access symbol table.
     * @param p Root {@code ProgNode}
     */
    CondNode(ProgNode p) {
        super(p);
    }

    /**
     * Returns sub-line level representation of {@code this}.
     * @return formatted condition representation defined by PA2 statement
     */
    String getCond() {
        assert (type != null);

        switch (type) {
            case COMP:
                return comp.getComp();
            case NOT:
                return String.format("!%s", cond1.getCond());
            case AND:
                return String.format("[ %s and %s ]", cond1.getCond(), cond2.getCond());
            case OR:
                return String.format("[ %s or %s ]", cond1.getCond(), cond2.getCond());
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public String toString() {
        return getCond();
    }

    /**
     * Parse condition node.
     * @param t {@code Tokenizer} instance
     * @throws InterpreterException if {@code matchConsume()} failed or any recursive parse call failed.
     */
    void parseCond(Tokenizer t) throws InterpreterException {
        if (testConsume(t, Token.LPAREN)) {
            type = CondType.COMP;
            comp = new CompNode(prog);
            comp.parseComp(t);
        } else if (testConsume(t, Token.NOT)) {
            type = CondType.NOT;
            matchConsume(t, Token.NOT);
            cond1 = new CondNode(prog);
            cond1.parseCond(t);
        } else if (testConsume(t, Token.LBRACK)) {
            matchConsume(t, Token.LBRACK);
            cond1 = new CondNode(prog);
            cond1.parseCond(t);
            if (testConsume(t, Token.AND)) {
                type = CondType.AND;
                matchConsume(t, Token.AND);
            } else if (testConsume(t, Token.OR)) {
                type = CondType.OR;
                matchConsume(t, Token.OR);
            } else {
                Token curr = t.next();
                raiseUnexpected(curr.line, "Expected compound condition operator, got " + curr.name);
            }
            cond2 = new CondNode(prog);
            cond2.parseCond(t);
            matchConsume(t, Token.RBRACK);
        } else {
            Token curr = t.next();
            raiseUnexpected(curr.line, "Expected condition, got " + curr.name);
        }
    }

    /**
     * Condition type enums as defined in CORE specification.
     */
    enum CondType {
        COMP,
        NOT,
        AND,
        OR
    }
}

/**
 * Sub-line level compare node.
 */
final class CompNode extends CoreNode {
    /**
     * Compare type as defined by CORE specification.
     */
    private Token type;

    /**
     * Left hand side factor node.
     */
    private FacNode fac1;

    /**
     * Right hand side factor node.
     */
    private FacNode fac2;

    /**
     * Constructor must be called with root {@code ProgNode} to access symbol table.
     * @param p Root {@code ProgNode}
     */
    CompNode(ProgNode p) {
        super(p);
    }

    /**
     * Returns sub-line level representation of {@code this}.
     * @return formatted compare representation defined by PA2 statement
     */
    String getComp() {
        assert (type != null);

        return String.format("( %s %s %s )", fac1.getFac(), type.name, fac2.getFac());
    }

    @Override
    public String toString() {
        return getComp();
    }

    /**
     * Parse compare node.
     * @param t {@code Tokenizer} instance
     * @throws InterpreterException if {@code matchConsume()} failed or any recursive parse call failed
     */
    void parseComp(Tokenizer t) throws InterpreterException {
        matchConsume(t, Token.LPAREN);
        fac1 = new FacNode(prog);
        fac1.parseFac(t);
        type = rangeConsume(t, Token.NEQ, Token.LT);
        fac2 = new FacNode(prog);
        fac2.parseFac(t);
        matchConsume(t, Token.RPAREN);
    }
}
