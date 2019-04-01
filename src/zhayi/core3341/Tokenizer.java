package zhayi.core3341;

import java.io.*;
import java.util.*;
import java.lang.StringBuilder;
import java.util.logging.Level;
import java.util.logging.Logger;

import zhayi.core3341.CoreError.*;

/**
 * Tokenizer for CORE language for CSE 3341 Project PA1
 *
 * @author Yi Zhang
 */
public class Tokenizer implements Iterable<Token> {
    private static Logger log;
    private static List<String> tokenId = Arrays.asList("", "program", "begin", "end", "int", "if", "then", "else", "while",
            "loop", "read", "write", "and", "or", ";", ",", "=", "!", "[", "]", "(", ")", "+", "-", "*", "!=", "==",
            ">=", "<=", ">", "<", "NUM", "ID", "~EOF~");

    private final static int MAXLEN_NUM_ID = 8;

    private List<Token> tokens = new ArrayList<>();
    private int currentIndex = 0;
    private Character currentChar = null;
    private int line = 1;

    Tokenizer() {
    }

    public Token getCurrent() {
        if (currentIndex == tokens.size()) return tokens.get(currentIndex - 1);
        return tokens.get(currentIndex);
    }

    public Token next() {
        assert (hasNext());

        currentIndex++;
        return getCurrent();
    }

    public boolean hasNext() {
        return currentIndex < tokens.size();
    }

    private int getTokenId(final String token) {
        return tokenId.indexOf(token);
    }

    public Iterator<Token> iterator() {
        return tokens.iterator();
    }

    void tokenize(String string) throws InvalidTokenException {
        FileReader fr;
        try {
            fr = new FileReader(string);
            currentChar = this.nextChar(fr);
            while (currentChar != null) {
                String anToken;
                if (currentChar == '\n') {
                    line++;
                    currentChar = nextChar(fr);
                } else if (Character.isWhitespace(currentChar)) {
                    currentChar = nextChar(fr);
                } else if (Character.isLowerCase(currentChar)) {
                    anToken = getAlphanumericString(fr);
                    if (tokenId.indexOf(anToken) < 0)
                        raiseError("Unknown reserve word " + anToken);
                    tokens.add(new Token(anToken, line, getTokenId(anToken)));
                } else if (Character.isDigit(currentChar) || Character.isUpperCase(currentChar)) {
                    if (Character.isUpperCase(currentChar)) {
                        anToken = getAlphanumericString(fr);
                        if (!anToken.matches("[A-Z][A-Z]*[0-9]*") || anToken.length() > MAXLEN_NUM_ID)
                            raiseError("Invalid identifier " + anToken);
                        tokens.add(new Token(anToken, line, Token.ID));
                    } else {
                        anToken = getAlphanumericString(fr);
                        if (!anToken.matches("[0-9][0-9]*") || anToken.length() > MAXLEN_NUM_ID)
                            raiseError("Invalid numeral " + anToken);
                        tokens.add(new Token(anToken, line, Token.NUM));
                    }
                } else {
                    char c;
                    if (currentChar == '!') {
                        c = nextChar(fr);
                        if (c == '=') {
                            tokens.add(new Token("!=", line, Token.NEQ));
                            currentChar = nextChar(fr);
                            continue;
                        }
                        tokens.add(new Token("!", line, Token.NOT));
                        currentChar = c;
                    } else if (currentChar == '>') {
                        c = nextChar(fr);
                        if (c == '=') {
                            tokens.add(new Token(">=", line, Token.GEQ));
                            currentChar = nextChar(fr);
                            continue;
                        }
                        tokens.add(new Token(">", line, Token.GT));
                        currentChar = c;
                    } else if (currentChar == '<') {
                        c = nextChar(fr);
                        if (c == '=') {
                            this.tokens.add(new Token("<=", line, Token.LEQ));
                            this.currentChar = this.nextChar(fr);
                            continue;
                        }
                        this.tokens.add(new Token("<", line, Token.LT));
                        currentChar = c;
                    } else if (currentChar == '=') {
                        c = this.nextChar(fr);
                        if (c == '=') {
                            tokens.add(new Token("==", line, Token.EQ));
                            currentChar = nextChar(fr);
                            continue;
                        }
                        tokens.add(new Token("=", line, Token.ASSIGN));
                        currentChar = c;
                    } else {
                        c = currentChar;
                        if (getTokenId(Character.toString(c)) < 0)
                            raiseError(String.format("Invalid symbol %s", c));
                        tokens.add(new Token(Character.toString(c), line, getTokenId(Character.toString(c))));
                        currentChar = nextChar(fr);
                    }
                }
            }
            fr.close();
        } catch (FileNotFoundException fnfEx) {
            System.err.printf("File %s not found", string);
            fnfEx.printStackTrace();
        } catch (IOException ioEx) {
            ioEx.printStackTrace();
        } finally {
            tokens.add(new Token("~EOF~", line, Token.EOF));
        }
    }

    private String getAlphanumericString(FileReader fileReader) {
        StringBuilder sb = new StringBuilder();
        while (currentChar != null && Character.isLetterOrDigit(currentChar)) {
            sb.append(currentChar);
            currentChar = nextChar(fileReader);
        }
        return sb.toString();
    }

    private void raiseError(String msg) throws InvalidTokenException {
        String info = String.format("Tokenizer Error: [Line %d] %s", line, msg);
        throw new InvalidTokenException(info);
    }

    private Character nextChar(FileReader fileReader) {
        Character c = null;
        try {
            int n = fileReader.read();
            if (n != -1) c = (char) n;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return c;
    }

    public static void main(String[] args) {
        log = Logger.getLogger("TokenizerMain");
        log.setLevel(Level.SEVERE);

        Tokenizer object = new Tokenizer();
        try {
            object.tokenize(args[0]);
        }
        catch (CoreError.InvalidTokenException invalidTokenException) {
            log.severe(invalidTokenException.getLocalizedMessage());
            System.exit(invalidTokenException.hashCode());
        }
        for (Token t: object) {
            System.out.println(t);
        }
    }
}

