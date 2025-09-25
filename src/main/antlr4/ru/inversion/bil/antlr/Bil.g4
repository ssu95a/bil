grammar Bil;

program
    : statement* returnExpression? EOF
    ;

statement
    : variableDeclaration ';'
    | assignment ';'
    | functionCall ';'
    | ifStatement
    | whileStatement
    | forStatement
    | returnStatement ';'
    | block
    | expression ';'
    ;

variableDeclaration
    : type ID ('=' expression)?
    ;

assignment
    : ID '=' expression
    ;

// Новое правило для объявления функции
functionDeclaration
    : type ID '(' parameterList? ')' block
    ;

// Правило для параметров функции (аналогично argumentList, но с типами)
parameterList
    : parameter (',' parameter)*
    ;

parameter
    : type ID
    ;

functionCall
    : ID '(' argumentList? ')'
    ;

methodCall
    : ID '.' ID '(' argumentList? ')'
    ;

argumentList
    : expression (',' expression)*
    ;

ifStatement
    : 'if' '(' expression ')' statement ('else' statement)?
    ;

whileStatement
    : 'while' '(' expression ')' statement
    ;

forStatement
    : 'for' '(' (variableDeclaration | assignment)? ';' expression? ';' expression? ')' statement
    ;

returnExpression
    : expression ';'?
    ;

returnStatement
    : 'return' expression?
    ;

block
    : '{' statement* '}'
    ;

expression
    : '-' expression                               # unaryMinusExpr  // Отрицательные числа
    | '+' expression                               # unaryPlusExpr   // Явный плюс (опционально)
    | expression op=('*' | '/' | '%') expression   # multiplicativeExpr
    | expression op=('+' | '-') expression         # additiveExpr
    | expression op=('<' | '>' | '<=' | '>=') expression # relationalExpr
    | expression op=('==' | '!=') expression       # equalityExpr
    | expression '&&' expression                   # logicalAndExpr
    | expression '||' expression                   # logicalOrExpr
    | '(' expression ')'                           # parenExpr
    | functionCall                                 # functionCallExpr
    | methodCall                                   # methodCallExpr
    | ID                                           # variableExpr
    | NUMBER                                       # numberExpr
    | STRING                                       # stringExpr
    | BOOL                                         # boolExpr
    | DATE                                         # dateExpr
    | TIME                                         # timeExpr
    | NULL                                         # nullExpr
    ;

type
    : 'int'
    | 'float'
    | 'string'
    | 'bool'
    | 'void'
    | 'money'
    | 'date'
    | 'time'
    ;

// Лексер
ID: [a-zA-Z_][a-zA-Z0-9_]*;
NUMBER: [0-9]+ ('.' [0-9]+)?;
STRING: '"' (~["\\] | '\\' .)* '"';
BOOL: 'true' | 'false';
NULL: 'null';
DATE: [0-9][0-9][0-9][0-9] '-' [0-9][0-9] '-' [0-9][0-9];
TIME: [0-9][0-9] ':' [0-9][0-9] (':' [0-9][0-9] ('.' [0-9]+)?)?;

WS: [ \t\r\n]+ -> skip;
COMMENT: '//' ~[\r\n]* -> skip;
MULTILINE_COMMENT: '/*' .*? '*/' -> skip;
COMMENT_FRU: '#' ~[\r\n]* -> skip;
