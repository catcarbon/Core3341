package zhayi.core3341;

/**
 * Tokens of CORE language for CSE 3341 Project
 *
 * @author Yi Zhang
 * @email zhang.5281@osu.edu
 */
public class Token {
    static final int PROGRAM = 1;
    static final int BEGIN = 2;
    static final int END = 3;
    static final int INT = 4;
    static final int IF = 5;
    static final int THEN = 6;
    static final int ELSE = 7;
    static final int WHILE = 8;
    static final int LOOP = 9;
    static final int READ = 10;
    static final int WRITE = 11;
    static final int AND = 12;
    static final int OR = 13;
    static final int SEMICOL = 14;
    static final int COMMA = 15;
    static final int ASSIGN = 16;
    static final int NOT = 17;
    static final int LBRACK = 18;
    static final int RBRACK = 19;
    static final int LPAREN = 20;
    static final int RPAREN = 21;
    static final int PLUS = 22;
    static final int MINUS = 23;
    static final int STAR = 24;
    static final int NEQ = 25;
    static final int EQ = 26;
    static final int GEQ = 27;
    static final int LEQ = 28;
    static final int GT = 29;
    static final int LT = 30;
    static final int NUM = 31;
    static final int ID = 32;
    static final int EOF = 33;
    final String name;
    final int code;
    final int line;

    Token(String string, int n, int n2) {
        this.name = string;
        this.line = n;
        this.code = n2;
    }

    @Override
    public String toString() {
        return Integer.toString(this.code);
    }
}

