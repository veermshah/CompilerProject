import java_cup.runtime.*;


%%

%cup
%line
%column
%unicode
%class Lexer

%{

Symbol newSym(int tokenId) {
    return new Symbol(tokenId, yyline, yycolumn);
}

Symbol newSym(int tokenId, Object value) {
    return new Symbol(tokenId, yyline, yycolumn, value);
}

%}


whitespace = [ \n\t\r]
identifier = [A-Za-z][A-Za-z0-9]*
intlit = [0-9]+
floatlit = [0-9]+\.[0-9]+
esc_char = \\ ['\\tn]
esc_str = \\ [\\tn\"]
charlit = ' ([^\'\\\\\n] | {esc_char}) '
string_char = ([^\"\\\n\t] | {esc_str})
strlit = \" {string_char}* \"
line_comment = \\\\[^\n]*
block_comment = \\\*([^*]|\*[^\\])*\*\\


%%

{whitespace}    { /* Ignore whitespace. */ }
{line_comment}  { /* Ignore line comment. */ }
{block_comment} { /* Ignore block comment. */ }

"class"         { return newSym(sym.CLASS, yytext()); }
"void"          { return newSym(sym.VOID, yytext()); }
"int"           { return newSym(sym.INT, yytext()); }
"char"          { return newSym(sym.CHAR, yytext()); }
"bool"          { return newSym(sym.BOOL, yytext()); }
"float"         { return newSym(sym.FLOAT, yytext()); }
"string"        { return newSym(sym.STRING, yytext()); }
"final"         { return newSym(sym.FINAL, yytext()); }

"if"            { return newSym(sym.IF, yytext()); }
"else"          { return newSym(sym.ELSE, yytext()); }
"while"         { return newSym(sym.WHILE, yytext()); }
"return"        { return newSym(sym.RETURN, yytext()); }

"read"          { return newSym(sym.READ, yytext()); }
"print"         { return newSym(sym.PRINT, yytext()); }
"printline"     { return newSym(sym.PRINTLINE, yytext()); }

"true"          { return newSym(sym.TRUE, yytext()); }
"false"         { return newSym(sym.FALSE, yytext()); }

"&&"            { return newSym(sym.AND, yytext()); }
"||"            { return newSym(sym.OR, yytext()); }
"++"            { return newSym(sym.PLUSPLUS, yytext()); }
"--"            { return newSym(sym.MINUSMINUS, yytext()); }
"<="            { return newSym(sym.LE, yytext()); }
">="            { return newSym(sym.GE, yytext()); }
"=="            { return newSym(sym.EQ, yytext()); }
"<>"            { return newSym(sym.NE, yytext()); }

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

"("             { return newSym(sym.LPAREN, yytext()); }
")"             { return newSym(sym.RPAREN, yytext()); }
"{"             { return newSym(sym.LBRACE, yytext()); }
"}"             { return newSym(sym.RBRACE, yytext()); }
"["             { return newSym(sym.LBRACKET, yytext()); }
"]"             { return newSym(sym.RBRACKET, yytext()); }
";"             { return newSym(sym.SEMICOLON, yytext()); }
","             { return newSym(sym.COMMA, yytext()); }

{floatlit}      { return newSym(sym.FLOAT_LIT, yytext()); }
{intlit}        { return newSym(sym.INT_LIT, yytext()); }
{charlit}       { return newSym(sym.CHAR_LIT, yytext()); }
{strlit}        { return newSym(sym.STRING_LIT, yytext()); }

{identifier}    { return newSym(sym.ID, yytext()); }

.               { System.out.println("Illegal char, '" + yytext() +
                    "' line: " + yyline + ", column: " + yycolumn); } 
