JAVA=java
JAVAC=javac
JFLEX=$(JAVA) -jar jflex-full-1.8.2.jar
CUPJAR=java-cup-11b.jar
CUP=$(JAVA) -jar $(CUPJAR)
RUN_INPUT ?= example3In.txt
RUN_OUTPUT ?= $(RUN_INPUT:.txt=-output.txt)

ifeq ($(OS),Windows_NT)
CP=.;$(CUPJAR)
CAT=type
else
CP=.:$(CUPJAR)
CAT=cat
endif

.PHONY: default all run clean

default: run

run: all
		$(JAVA) -cp "$(CP)" TypeCheckingTest $(RUN_INPUT) > $(RUN_OUTPUT) 2>&1
		$(CAT) $(RUN_INPUT)
		$(CAT) $(RUN_OUTPUT)

all: Lexer.java parser.java
		$(JAVAC) -cp "$(CP)" *.java

clean:
ifeq ($(OS),Windows_NT)
		@if exist *.class del /Q *.class
		@if exist *~ del /Q *~
		@if exist *.bak del /Q *.bak
		@if exist Lexer.java del /Q Lexer.java
		@if exist parser.java del /Q parser.java
		@if exist sym.java del /Q sym.java
else
		rm -f *.class *~ *.bak Lexer.java parser.java sym.java
endif

Lexer.java: tokens.jflex
		$(JFLEX) tokens.jflex

parser.java: grammar.cup
		$(CUP) -interface < grammar.cup

parserD.java: grammar.cup
		$(CUP) -interface -dump < grammar.cup
