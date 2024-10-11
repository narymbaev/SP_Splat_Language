package splat.lexer;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Lexer {

	private File progFile;
	private int lineNumber;
	private int columnNumber;
	private BufferedReader reader;
	private int currentChar;

	public Lexer(File progFile) {
		this.progFile = progFile;
		this.lineNumber = 1;
		this.columnNumber = 0;
	}

	public List<Token> tokenize() throws LexException {
		List<Token> tokens = new ArrayList<Token>();
		try {
			// create reader
			this.reader = new BufferedReader(new FileReader(progFile));
			readNewCharacter();
			while (currentChar != -1) {
				if (Character.isWhitespace(currentChar)) {
					skipWhitespace();
				} else if (Character.isLetter(currentChar) || currentChar == '_') {
					tokens.add(readKeywordIdentifier());
				} else if (Character.isDigit(currentChar)) {
					tokens.add(readDigits());
				} else if (currentChar == '"') {
					tokens.add(readStringLiteral());
				} else if (isOperatorOrDelimiterStart((char) currentChar)) {
					tokens.add(readOperatorOrDelimiter());
				} else {
					throw new LexException("unexpected character: " + (char) currentChar, lineNumber, columnNumber);
				}
			}
			reader.close();
		} catch (IOException e) {
			throw new LexException("error while reading file", lineNumber, columnNumber);
		} finally {
			try {
            if (reader != null) {
                reader.close();
            }
			} catch (IOException e) {
				// exception
			}
		}

		return tokens;

	}

	private void readNewCharacter() throws IOException {
		currentChar = reader.read();
		columnNumber++;
		// after new line initialize column number and increment line number
		if (currentChar == '\n') {
			lineNumber++;
			columnNumber = 0;
		}
	}

	private boolean isOperatorOrDelimiterStart(char ch) {
		return ":;,+-*/%(){}[]=<>!".indexOf(ch) != -1;
	}

	private boolean isKeyword(String value) {
		String[] keywords = {
				"program", "begin", "end", "is", "while", "do", "if", "then", "else",
				"print", "print_line", "return", "and", "or", "not", "true", "false",
				"void", "Integer", "Boolean", "String"
		};
		for (String keyword : keywords) {
			if (value.equals(keyword)) {
				return true;
			}
		}
		return false;
	}

	private void skipWhitespace() throws IOException {
		while (Character.isWhitespace(currentChar)) {
			readNewCharacter();
		}
	}

	private Token readKeywordIdentifier() throws IOException {
		int tokenLine = lineNumber;
		int tokenColumn = columnNumber;
		StringBuilder sb = new StringBuilder();

		// first character is letter or underscore
		if (Character.isLetter(currentChar) || currentChar == '_') {
			sb.append((char) currentChar);
			readNewCharacter();
		}

		// subsequent can be any from the given characters [ a-z | A-Z | 0-9 | _ ]
		while (Character.isLetterOrDigit(currentChar) || currentChar == '_') {
			sb.append((char) currentChar);
			readNewCharacter();
		}

		String value = sb.toString();

		if (isKeyword(value)) {
			// keyword
			return new Token(value, tokenLine, tokenColumn);
		} else {
			// variable name or identifier
			return new Token(value, tokenLine, tokenColumn);
		}
	}

	private Token readDigits() throws IOException {
		StringBuilder sb = new StringBuilder();
		int tokenLine = lineNumber;
		int tokenColumn = columnNumber;

		while (Character.isDigit(currentChar)) {
			sb.append((char) currentChar);
			readNewCharacter();
		}

		String value = sb.toString();

		return new Token(value, tokenLine, tokenColumn);
	}


	private Token readOperatorOrDelimiter() throws IOException, LexException {
		int tokenLine = lineNumber;
		int tokenColumn = columnNumber;
		char ch = (char) currentChar;

		switch (ch) {
			case ':':
				readNewCharacter();
				if (currentChar == '=') {
					readNewCharacter();
					return new Token(":=", tokenLine, tokenColumn);
				} else {
					return new Token(":", tokenLine, tokenColumn);
				}
			case ';':
				readNewCharacter();
				return new Token(";", tokenLine, tokenColumn);
			case ',':
				readNewCharacter();
				return new Token(",", tokenLine, tokenColumn);
			case '(':
				readNewCharacter();
				return new Token("(", tokenLine, tokenColumn);
			case ')':
				readNewCharacter();
				return new Token(")", tokenLine, tokenColumn);
			case '>':
				readNewCharacter();
				if (currentChar == '=') {
					readNewCharacter();
					return new Token(">=", tokenLine, tokenColumn);
				} else {
					return new Token(">", tokenLine, tokenColumn);
				}
			case '<':
				readNewCharacter();
				if (currentChar == '=') {
					readNewCharacter();
					return new Token("<=", tokenLine, tokenColumn);
				} else {
					return new Token("<", tokenLine, tokenColumn);
				}
			case '=':
				readNewCharacter();
				if (currentChar == '=') {
					readNewCharacter();
					return new Token("==", tokenLine, tokenColumn);
				} else {
					// single '=' is not valid
					throw new LexException("unexpected character: =", tokenLine, tokenColumn);
				}
			case '+':
			case '-':
			case '*':
			case '/':
			case '%':
				readNewCharacter();
				return new Token(String.valueOf(ch), tokenLine, tokenColumn);
			default:
				throw new LexException("unexpected character: " + ch, tokenLine, tokenColumn);
		}
	}


	private Token readStringLiteral() throws IOException, LexException {
		StringBuilder sb = new StringBuilder();
		int tokenLine = lineNumber;
		int tokenColumn = columnNumber;

		readNewCharacter(); // skip first quote -> "

		while (currentChar != '"' && currentChar != -1) {
			if (currentChar == '\n') {
				throw new LexException("invalid character detected in the string", lineNumber, columnNumber);
			}
			sb.append((char) currentChar);
			readNewCharacter();
		}

		if (currentChar == -1) {
			throw new LexException("unexpected error during string read", tokenLine, tokenColumn);
		}

		readNewCharacter(); // skip last quote -> "

		String value = sb.toString();

		return new Token(value, tokenLine, tokenColumn);
	}

}
