/*-***
 *
 * This file defines a stand-alone lexical analyzer for a subset of the Pascal
 * programming language.  This is the same lexer that will later be integrated
 * with a CUP-based parser.  Here the lexer is driven by the simple Java test
 * program in ./PascalLexerTest.java, q.v.  See 330 Lecture Notes 2 and the
 * Assignment 2 writeup for further discussion.
 *
 */


import java_cup.runtime.*;


%%
/*-*
 * LEXICAL FUNCTIONS:
 */

%cup
%line
%column
%unicode
%class Lexer

%{

/**
 * Return a new Symbol with the given token id, and with the current line and
 * column numbers.
 */
Symbol newSym(int tokenId) {
    return new Symbol(tokenId, yyline, yycolumn);
}

/**
 * Return a new Symbol with the given token id, the current line and column
 * numbers, and the given token value.  The value is used for tokens such as
 * identifiers and numbers.
 */
Symbol newSym(int tokenId, Object value) {
    return new Symbol(tokenId, yyline, yycolumn, value);
}

%}


/*-*
 * PATTERN DEFINITIONS:
 */

// Whitespace: space, tab, newline, carriage return
whitespace = [ \n\t\r]

// Identifier: letter followed by zero or more letters or digits
identifier = [A-Za-z][A-Za-z0-9]*

// Integer literal: one or more digits (leading zeros allowed)
intlit = [0-9]+

// Float literal: digits, dot, digits (both sides required)
floatlit = [0-9]+\.[0-9]+

// Escape sequence for character literals
// Matches: backslash followed by ', \, t, or n
esc_char = \\ ['\\tn]

// Escape sequence for string literals
// Matches: backslash followed by \\, t, n, or "
esc_str = \\ [\\tn\"]

// Character literal: 'X' where X is any non-quote/backslash/newline char OR a valid char escape seq
// Single quote escaped inside character class for regex safety
charlit = ' ([^\'\\\\\n] | {esc_char}) '

// String character: any non-quote/backslash/newline/tab char OR a valid string escape seq
// NOTE: Parentheses around alternation are CRITICAL so that {string_char}* applies to both alternatives
string_char = ([^\"\\\n\t] | {esc_str})

// String literal: "..." containing string_chars
strlit = \" {string_char}* \"

// Line comment: starts with \\ (two backslashes), ends at newline
line_comment = \\\\[^\n]*

// Block comment: starts with \* (backslash-asterisk), ends at first *\ (asterisk-backslash)
block_comment = \\\* ([^*] | \* [^\\])* \*\\
// Line comment: starts with \\ (two backslashes), ends at newline
line_comment = \\\\[^\n]*

// Block comment: starts with \* (backslash-asterisk), ends at first *\ (asterisk-backslash)
block_comment = \\\*([^*]|\*[^\\])*\*\\


%%
/**
 * LEXICAL RULES:
 */

/**
 * Implement terminals here, ORDER MATTERS!
 * 1. Comments and whitespace (ignore)
 * 2. Keywords (before ID to take precedence)
 * 3. Multi-character operators (before single-char)
 * 4. Single-character operators and punctuation
 * 5. Literals (floatlit before intlit)
 * 6. Identifier
 * 7. Illegal character fallback
 */

// Ignore whitespace
{whitespace}    { /* Ignore whitespace. */ }

// Ignore comments
{line_comment}  { /* Ignore line comment. */ }
{block_comment} { /* Ignore block comment. */ }

// Keywords (type-related)
"class"         { return newSym(sym.CLASS, yytext()); }
"void"          { return newSym(sym.VOID, yytext()); }
"int"           { return newSym(sym.INT, yytext()); }
"char"          { return newSym(sym.CHAR, yytext()); }
"bool"          { return newSym(sym.BOOL, yytext()); }
"float"         { return newSym(sym.FLOAT, yytext()); }
"final"         { return newSym(sym.FINAL, yytext()); }

// Keywords (control flow)
"if"            { return newSym(sym.IF, yytext()); }
"else"          { return newSym(sym.ELSE, yytext()); }
"while"         { return newSym(sym.WHILE, yytext()); }
"return"        { return newSym(sym.RETURN, yytext()); }

// Keywords (I/O)
"read"          { return newSym(sym.READ, yytext()); }
"print"         { return newSym(sym.PRINT, yytext()); }
"printline"     { return newSym(sym.PRINTLINE, yytext()); }

// Boolean literals
"true"          { return newSym(sym.TRUE, yytext()); }
"false"         { return newSym(sym.FALSE, yytext()); }

// Multi-character operators (BEFORE single-char to take precedence)
"&&"            { return newSym(sym.AND, yytext()); }
"||"            { return newSym(sym.OR, yytext()); }
"++"            { return newSym(sym.PLUSPLUS, yytext()); }
"--"            { return newSym(sym.MINUSMINUS, yytext()); }
"<="            { return newSym(sym.LE, yytext()); }
">="            { return newSym(sym.GE, yytext()); }
"=="            { return newSym(sym.EQ, yytext()); }
"<>"            { return newSym(sym.NE, yytext()); }

// Single-character operators
"<"             { return newSym(sym.LT, yytext()); }
">"             { return newSym(sym.GT, yytext()); }
"="             { return newSym(sym.ASSIGN, yytext()); }
"+"             { return newSym(sym.PLUS, yytext()); }
"-"             { return newSym(sym.MINUS, yytext()); }
"*"             { return newSym(sym.MULT, yytext()); }
"/"             { return newSym(sym.DIV, yytext()); }
"~"             { return newSym(sym.TILDE, yytext()); }
"?"             { return newSym(sym.QUESTION, yytext()); }
":"             { return newSym(sym.COLON, yytext()); }

// Punctuation / Delimiters
"("             { return newSym(sym.LPAREN, yytext()); }
")"             { return newSym(sym.RPAREN, yytext()); }
"{"             { return newSym(sym.LBRACE, yytext()); }
"}"             { return newSym(sym.RBRACE, yytext()); }
"["             { return newSym(sym.LBRACKET, yytext()); }
"]"             { return newSym(sym.RBRACKET, yytext()); }
";"             { return newSym(sym.SEMICOLON, yytext()); }
","             { return newSym(sym.COMMA, yytext()); }

// Literals (float BEFORE int to avoid partial match)
{floatlit}      { return newSym(sym.FLOAT_LIT, yytext()); }
{intlit}        { return newSym(sym.INT_LIT, yytext()); }
{charlit}       { return newSym(sym.CHAR_LIT, yytext()); }
{strlit}        { return newSym(sym.STRING_LIT, yytext()); }

// Identifier (AFTER keywords so keywords take precedence)
{identifier}    { return newSym(sym.ID, yytext()); }

// Illegal character fallback
.               { System.out.println("Illegal char, '" + yytext() +
                    "' line: " + yyline + ", column: " + yycolumn); } 