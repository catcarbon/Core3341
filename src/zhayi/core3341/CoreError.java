package zhayi.core3341;

/**
 * CoreError Exceptions for CSE 3341 Project
 *
 * @author Yi Zhang
 * @email zhang.5281@osu.edu
 */
public class CoreError {

    static class UninitializedException extends InterpreterException {
        UninitializedException(String msg) {
            super(msg);
        }
    }

    static class UnexpectedTokenException extends InterpreterException {
        UnexpectedTokenException(String msg) {
            super(msg);
        }
    }

    static class UnderflowOverflowException extends InterpreterException {
        UnderflowOverflowException(String msg) {
            super(msg);
        }
    }

    static class InvalidInputException extends InterpreterException {
        InvalidInputException(String msg) {
            super(msg);
        }
    }

    static class UndeclaredException extends InterpreterException {
        UndeclaredException(String msg) {
            super(msg);
        }
    }

    static class RedeclaredException extends InterpreterException {
        RedeclaredException(String msg) {
            super(msg);
        }
    }

    static class ConsumeMismatchException extends InterpreterException {
        ConsumeMismatchException(String msg) {
            super(msg);
        }
    }

    static class InvalidTokenException extends InterpreterException {
        InvalidTokenException(String msg) {
            super(msg);
        }
    }

    static class InterpreterException extends Exception {
        InterpreterException(String msg) {
            super(msg);
        }
    }

}


