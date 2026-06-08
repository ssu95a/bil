grammar Bil;

program
    : statement* returnExpression? EOF
    ;

statement
    : variableDeclaration ';'
    | assignment ';'
    | functionCall ';'
    | functionDeclaration
    | ifStatement
    | whileStatement
    | forStatement
    | returnStatement ';'
    | block
    | expression ';'
    | ';'
    ;

variableDeclaration
    : type ID ('=' expression)? (',' ID ('=' expression)?)*
    ;

assignment
    : ID '=' expression                                  # variableAssignment
    | arrayIndexAccess '=' expression                    # arrayElementAssignment
    ;

arrayIndexAccess
    : indexTarget '[' index=expression ']'
    ;

indexTarget
    : ID
    | functionCall
    | '(' expression ')'
    | arrayLiteral
    | mapLiteral
    ;

expression
    : '-' expression                               # unaryMinusExpr
    | '+' expression                               # unaryPlusExpr
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
    | '--' expression                              # preDecrementExpr
    | '++' expression                              # preIncrementExpr
    | expression '++'                              # postIncrementExpr
    | expression '--'                              # postDecrementExpr
    | CHAR                                         # charExpr
    | arrayLiteral                                 # arrayLiteralExpr
    | mapLiteral                                   # mapLiteralExpr
    | arrayIndexAccess                             # arrayAccessExpr
    ;

arrayLiteral
    : '[' expressionList? ']'
    ;

mapLiteral
    : '{' keyValueList? '}'
    ;

expressionList
    : expression (',' expression)*
    ;

keyValueList
    : keyValuePair (',' keyValuePair)*
    ;

keyValuePair
    : expression ':' expression
    ;

functionDeclaration
    : type ID '(' parameterList? ')' block
    ;

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
    : 'for' '(' (variableDeclaration | assignment)? ';' expression? ';' (forUpdateAssignment | expression)? ')' statement
    ;

forUpdateAssignment
    : assignment
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

type
    : 'int'
    | 'float'
    | 'string'
    | 'bool'
    | 'void'
    | 'money'
    | 'date'
    | 'time'
    | 'map'
    | 'array'
    ;

// Лексер
BOOL: 'true' | 'false';
NULL: 'null';
ID: [a-zA-Z_][a-zA-Z0-9_]*;
NUMBER: [0-9]+ ('.' [0-9]+)?;
STRING: '"' ( ESC | ~["\\] )* '"';
        fragment ESC: '\\' . ;

DATE: [0-9][0-9][0-9][0-9] '-' [0-9][0-9] '-' [0-9][0-9];
TIME: [0-9][0-9] ':' [0-9][0-9] (':' [0-9][0-9] ('.' [0-9]+)?)?;
CHAR: '\'' ( ESC | ~['\\] ) '\'';

WS: [ \t\r\n]+ -> skip;
COMMENT: '//' ~[\r\n]* -> skip;
MULTILINE_COMMENT: '/*' .*? '*/' -> skip;
COMMENT_FRU: '#' ~[\r\n]* -> skip;