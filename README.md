# Project 1: UTDLang Lexical Analyzer

## Purpose

This project implements a lexical analyzer (scanner) for the UTDLang subset language using **JFlex** for lexical specification and **CUP** (Java Cup) for terminal declarations. The scanner recognizes keywords, operators, literals, identifiers, comments, and whitespace according to the grammar defined in `project-1.pdf`.

## Prerequisites

- **Java** (JDK 8 or later)
- **JFlex** (`jflex-full-1.8.2.jar`) — included in project root
- **CUP** (`java-cup-11b.jar`) — included in project root

## Building and Running

### Linux / macOS

```bash
make all
make run
```

This compiles `Lexer.java`, `parser.java`, and `sym.java` from `tokens.jflex` and `grammar.cup`, then runs `LexerTest` on `basicTest.txt` and redirects output to `basicTest-output.txt`.

### Windows (PowerShell)

```powershell
# 1. Generate Lexer.java from tokens.jflex
java -jar jflex-full-1.8.2.jar tokens.jflex

# 2. Generate parser.java and sym.java from grammar.cup
Get-Content grammar.cup | java -jar java-cup-11b.jar -interface

# 3. Compile all Java files
javac -cp ".;java-cup-11b.jar" Lexer.java parser.java sym.java LexerTest.java

# 4. Run LexerTest on an input file and redirect output
java -cp ".;java-cup-11b.jar" LexerTest basicTest.txt > basicTest-output.txt
```

## Testing Additional Input Files

To test a custom input file:

```bash
# Linux/macOS
java -cp .:java-cup-11b.jar LexerTest <input_file> > <input_file>-output.txt

# Windows
java -cp ".;java-cup-11b.jar" LexerTest <input_file> | Out-File -Encoding UTF8 <input_file>-output.txt
```

**Output naming convention:** If input is `mytest.txt`, output must be named `mytest-output.txt`.

### Example

```powershell
java -cp ".;java-cup-11b.jar" LexerTest basicRegex.txt > basicRegex-output.txt
java -cp ".;java-cup-11b.jar" LexerTest basicFails.txt > basicFails-output.txt
```

## Expected Output Format

Each recognized token is printed as:

```
Token #<sym_id>, with value = <token_text>; at line <line_number>, column <column_number>
```

Invalid characters trigger:

```
Illegal char, '<char>' line: <line_number>, column: <column_number>
```

## Project Structure

- **`tokens.jflex`** — JFlex lexical specification with pattern definitions and lexical rules
- **`grammar.cup`** — CUP terminal declarations (Phase 1; parser productions added in Phase 2)
- **`LexerTest.java`** — Test harness that reads input file and prints token stream
- **`Lexer.java`** — Generated from `tokens.jflex` by JFlex
- **`parser.java`, `sym.java`** — Generated from `grammar.cup` by CUP
- **`Makefile`** — Build automation for Linux/macOS
- **`testfiles/`** — Sample input files for testing