package splat.lexer;

import java.io.File;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileReader;
import java.util.List;
import java.util.ArrayList;

public class Lexer {

	private File progFile;
	private int lineNumber;
	private int columnNumber;
	private BufferedReader reader;
	private int currentChar;
	private boolean endOfFile;

	public Lexer(File progFile) {
		// TODO Auto-generated constructor stub
		this.progFile = progFile;
		this.lineNumber = 1;
		this.columnNumber = 0;
		this.endOfFile = false;
	}

	public List<Token> tokenize() throws LexException {
		// TODO Auto-generated method stub
		List<Token> tokens = new ArrayList<>();

		try {
			reader = new BufferedReader(new FileReader(progFile));
			advance(); // Initialize currentChar

			while (!endOfFile) {
				// Skip whitespace and update line/column numbers
				skipWhitespace();

				// Record the starting position of the token
				int tokenLine = lineNumber;
				int tokenColumn = columnNumber;

				if (Character.isLetter(currentChar)) {
					// Handle identifiers and keywords
					String value = readIdentifier();
					tokens.add(new Token(value, tokenLine, tokenColumn));

				} else if (Character.isDigit(currentChar)) {
					// Handle numbers
					String value = readNumber();
					tokens.add(new Token(value, tokenLine, tokenColumn));

				} else if (currentChar == '"') {
					// Handle strings
					String value = readString();
					tokens.add(new Token(value, tokenLine, tokenColumn));

				} else if (isOperatorStart(currentChar)) {
					// Handle operators and delimiters
					String value = readOperator();
					tokens.add(new Token(value, tokenLine, tokenColumn));

				} else if (currentChar == -1) {
					// End of file
					endOfFile = true;

				} else {
					// Handle invalid characters
					throw new LexException("Unexpected character: '" + (char)currentChar + "'", lineNumber, columnNumber);
				}
			}

			reader.close();

		} catch (IOException e) {
			throw new LexException("Error reading file: " + e.getMessage(), lineNumber, columnNumber);
		}

		return tokens;

	}

	private void advance() throws IOException {
		currentChar = reader.read();
		columnNumber++;

		if (currentChar == '\n') {
			lineNumber++;
			columnNumber = 0;
		}
	}

	private void skipWhitespace() throws IOException {
		while (!endOfFile && Character.isWhitespace(currentChar)) {
			if (currentChar == '\n') {
				lineNumber++;
				columnNumber = 0;
			}
			advance();
		}
	}

	private String readIdentifier() throws IOException {
		StringBuilder result = new StringBuilder();

		while (!endOfFile && (Character.isLetterOrDigit(currentChar) || currentChar == '_')) {
			result.append((char) currentChar);
			advance();
		}

		return result.toString();
	}

	private String readNumber() throws IOException, LexException {
		StringBuilder result = new StringBuilder();
		boolean hasDecimalPoint = false;

		while (!endOfFile && (Character.isDigit(currentChar) || currentChar == '.')) {
			if (currentChar == '.') {
				if (hasDecimalPoint) {
					throw new LexException("Invalid number format", lineNumber, columnNumber);
				}
				hasDecimalPoint = true;
			}
			result.append((char) currentChar);
			advance();
		}

		return result.toString();
	}

	private String readString() throws IOException, LexException {
		StringBuilder result = new StringBuilder();
		advance(); // Skip the opening quote

		while (!endOfFile && currentChar != '"') {
			if (currentChar == '\\') {
				// Handle escape sequences if needed
				advance();
				if (currentChar == 'n') {
					result.append('\n');
				} else if (currentChar == 't') {
					result.append('\t');
				} else if (currentChar == '"' || currentChar == '\\') {
					result.append((char) currentChar);
				} else {
					throw new LexException("Invalid escape sequence", lineNumber, columnNumber);
				}
			} else {
				result.append((char) currentChar);
			}
			advance();
		}

		if (currentChar != '"') {
			throw new LexException("Unterminated string literal", lineNumber, columnNumber);
		}

		advance(); // Skip the closing quote
		return result.toString();
	}

	private boolean isOperatorStart(char ch) {
		return "+-*/%=!<>&|".indexOf(ch) != -1;
	}

	private Token readOperator() throws IOException, LexException {
		StringBuilder operator = new StringBuilder();
		int tokenLine = lineNumber;
		int tokenColumn = columnNumber;

		// Read the first character of the operator
		char firstChar = (char) currentChar;
		operator.append(firstChar);
		advance();

		// Initialize opValue with the first character
		String opValue = operator.toString();

		// Check for multi-character operators
		if (firstChar == '+') {
			if (currentChar == '+' || currentChar == '=') {
				operator.append((char) currentChar);
				advance();
			}
		} else if (firstChar == '-') {
			if (currentChar == '-' || currentChar == '=') {
				operator.append((char) currentChar);
				advance();
			}
		} else if (firstChar == '*') {
			if (currentChar == '=') {
				operator.append((char) currentChar);
				advance();
			}
		} else if (firstChar == '/') {
			if (currentChar == '=') {
				operator.append((char) currentChar);
				advance();
			}
		} else if (firstChar == '%') {
			if (currentChar == '=') {
				operator.append((char) currentChar);
				advance();
			}
		} else if (firstChar == '=') {
			if (currentChar == '=') {
				operator.append((char) currentChar);
				advance();
			}
		} else if (firstChar == '!') {
			if (currentChar == '=') {
				operator.append((char) currentChar);
				advance();
			}
		} else if (firstChar == '<') {
			if (currentChar == '=') {
				operator.append((char) currentChar);
				advance();
			}
		} else if (firstChar == '>') {
			if (currentChar == '=') {
				operator.append((char) currentChar);
				advance();
			}
		} else if (firstChar == '&') {
			if (currentChar == '&') {
				operator.append((char) currentChar);
				advance();
			}
		} else if (firstChar == '|') {
			if (currentChar == '|') {
				operator.append((char) currentChar);
				advance();
			}
		}

		opValue = operator.toString();

		// Validate the operator
		if (!isValidOperator(opValue)) {
			throw new LexException("Invalid operator: " + opValue, tokenLine, tokenColumn);
		}

		return new Token(opValue, tokenLine, tokenColumn);
	}

}

