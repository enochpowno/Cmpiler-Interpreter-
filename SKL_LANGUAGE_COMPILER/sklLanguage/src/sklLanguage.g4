grammar sklLanguage;


compilationUnit
 : classBodyDeclaration* EOF
 ;
variableModifier
 : FINAL
 ;

typeParameters
 : LT typeParameter (COMMA typeParameter)* GT
 ;

typeParameter
 : IDENTIFIER
 ;

typeBound
 : typeType ('&' typeType)*
 ;


classBodyDeclaration
 : SEMI

 | memberDeclaration
 ;

memberDeclaration
 : methodDeclaration
 | genericMethodDeclaration
 | fieldDeclaration
 ;



methodDeclaration
 : typeTypeOrVoid IDENTIFIER formalParameters (LBRACK RBRACK)* (THROWS qualifiedNameList)? methodBody #methodDeclaration_1
 | IDENTIFIER formalParameters (LBRACK RBRACK)* (THROWS qualifiedNameList)? methodBody #methodDeclaration_2
 ;

methodBody
 : LBRACE statement+ RBRACE
 ;
typeTypeOrVoid
 : typeType
 | VOID
 ;
genericMethodDeclaration
 : typeParameters methodDeclaration
 ;

fieldDeclaration
 : variableModifier? typeType variableDeclarators SEMI
 ;
constDeclaration
 : typeType constantDeclarator (COMMA constantDeclarator)* SEMI
 ;
constantDeclarator
 : IDENTIFIER (LBRACK RBRACK)* ASSIGN variableInitializer
 ;
variableDeclarators
 : variableDeclarator
 ;
variableDeclarator
 : variableDeclaratorId (ASSIGN variableInitializer)?
 ;
variableDeclaratorId
 : IDENTIFIER (LBRACK RBRACK)*
 ;
variableInitializer
 : arrayInitializer
 | expression
 | inputStream
 ;
arrayInitializer
 : LBRACE (variableInitializer (COMMA variableInitializer)* (COMMA)?)? RBRACE #arrayInitializer_1
 | LBRACK (DECIMAL_LITERAL| IDENTIFIER) RBRACK #arrayInitializer_2
 ;
qualifiedNameList
 : qualifiedName (COMMA qualifiedName)*
 ;
formalParameters
 : LPAREN formalParameterList? RPAREN

 ;
formalParameterList
 : formalParameter (COMMA formalParameter)* (COMMA lastFormalParameter)?
 | lastFormalParameter
 ;
formalParameter
 : typeType variableDeclaratorId
 ;
lastFormalParameter
 : variableModifier* typeType '...' variableDeclaratorId
 ;
qualifiedName
 : IDENTIFIER (DOT IDENTIFIER)*
 ;
literal
 : integerLiteral
 | floatLiteral
 | CHAR_LITERAL
 | STRING_LITERAL
 | BOOL_LITERAL
 | NULL_LITERAL
 ;
integerLiteral
 : DECIMAL_LITERAL
 ;
floatLiteral
 : FLOAT_LITERAL
 ;
elementValuePairs
 : elementValuePair (COMMA elementValuePair)*
 ;
elementValuePair
 : IDENTIFIER ASSIGN elementValue
 ;
elementValue
 : expression
 | elementValueArrayInitializer
 ;
elementValueArrayInitializer
 : LBRACE (elementValue (COMMA elementValue)*)? (COMMA)? RBRACE
 ;

block
 : statement*
 ;

localVariableDeclaration
 : variableModifier* typeType variableDeclarators
 ;
statement
 : localVariableDeclaration SEMI #statement_1
 | IF parExpression LBRACE block RBRACE (ELSE IF parExpression LBRACE block RBRACE)* (ELSE LBRACE block RBRACE)? #statement_2
 | FOR LPAREN forControl RPAREN LBRACE block RBRACE #statement_3
 | WHILE parExpression LBRACE block RBRACE  #statement_4
 | DO LBRACE block  RBRACE WHILE parExpression SEMI #statement_5
 | TRY LBRACE block RBRACE (catchClause+ finallyBlock? | finallyBlock) #statement_6
 | TRY resourceSpecification LBRACE block RBRACE catchClause* finallyBlock? #statement_7
 | RETURN expression? SEMI #statement_8
 | BREAK IDENTIFIER? SEMI #statement_9
 | SEMI #statement_10
 | statementExpression=expression SEMI #statement_11
 | identifierLabel=IDENTIFIER COLON statement #statement_12
 | outputStream #statement_13
 ;
catchClause
 : CATCH LPAREN variableModifier* catchType IDENTIFIER RPAREN LBRACE statement* RBRACE
 ;
catchType
 : qualifiedName ('|' qualifiedName)*
 ;
finallyBlock

 : FINALLY LBRACE block RBRACE
 ;
resourceSpecification
 : LPAREN resources SEMI? RPAREN
 ;
resources
 : resource (SEMI resource)*
 ;
resource
 : variableModifier* variableDeclaratorId ASSIGN expression
 ;
forControl
 : enhancedForControl
 | forInit SEMI comparison SEMI forUpdate=expressionList?
 ;
forInit
 : localVariableDeclaration
 | expressionList
 ;
enhancedForControl
 : variableModifier* typeType variableDeclaratorId COLON expression
 ;
// EXPRESSIONS
parExpression
 : LPAREN comparison RPAREN
 ;

comparison
: BANG? (expression) bop=(LE | GE | GT | LT| EQUAL | NOTEQUAL) BANG? (expression) additionalComparison?
 | BANG? LPAREN comparison RPAREN additionalComparison?
 ;

additionalComparison
 : (AND|OR) comparison
 | (AND|OR) LPAREN comparison RPAREN
 ;


expressionList
 : expression (COMMA expression)*
 ;

 methodCall
 : IDENTIFIER LPAREN expressionList? RPAREN #correctFunc
 | IDENTIFIER LPAREN expressionList? RPAREN (LPAREN expressionList? RPAREN)+ #wrongFunc
 ;

expression
 : primary #expression_1
 | expression bop=DOT IDENTIFIER #expression_2
 | expression LBRACK expression RBRACK #expression_3
 | methodCall #expression_4
 | LPAREN typeType RPAREN expression #expression_5
 | prefix=BANG expression #expression_8
 | expression bop=(ADD|SUB|MUL|DIV|MOD) expression #expression_10
 | <assoc=right> expression bop=(ASSIGN | ADD_ASSIGN | SUB_ASSIGN | MUL_ASSIGN | DIV_ASSIGN | MOD_ASSIGN ) expression #expression_17
 | creator #expression_18
 | IDENTIFIER ASSIGN inputStream #expression_19
 ;

primary
 : LPAREN expression RPAREN #primary_1
 | literal #primary_2
 | literal (INC|DEC) #literalError
 | IDENTIFIER #primary_3
 | (IDENTIFIER+ '"' | '"' IDENTIFIER+ ) #missingQuote
 ;

 creator
     : createdName (arrayCreatorRest)
     ;

createdName
 : IDENTIFIER typeArgumentsOrDiamond? (DOT IDENTIFIER typeArgumentsOrDiamond?)*
 | primitiveType
 ;


arrayCreatorRest
 : LBRACK (RBRACK (LBRACK RBRACK)* arrayInitializer | expression RBRACK (LBRACK expression RBRACK)* (LBRACK RBRACK)*)


 ;
 typeArgumentsOrDiamond
     : LT GT
     | typeArguments
     ;

 nonWildcardTypeArgumentsOrDiamond
     : LT GT
     | nonWildcardTypeArguments
     ;

 nonWildcardTypeArguments
     : LT typeList GT
     ;

 typeArguments
     : LT typeArgument (COMMA typeArgument)* GT
     ;

 typeArgument
     : typeType
     ;

// END ADD FOR ARRAY
typeList
 : typeType (COMMA typeType)*
 ;
typeType
 : primitiveType (LBRACK RBRACK)*
 ;
primitiveType
 : BOOLEAN
 | CHAR
 | INT
 | DOUBLE
 | STRING
 ;

superSuffix
 : arguments
 | DOT IDENTIFIER arguments?
 ;

arguments
 : LPAREN expressionList? RPAREN
 ;
outputStream
// : OUTPUT LPAREN RPAREN SEMI #outputBlank
 : OUTPUT LPAREN '"' RPAREN SEMI #outputMissingQuote
 | OUTPUT LPAREN (expression (ADD expression)*)? RPAREN SEMI #outputCorrect
 | OUTPUT LPAREN (expression (ADD expression)* ADD (ADD expression)* | ADD expression) RPAREN SEMI #outputExtraPlus
 | OUTPUT LPAREN expression+ RPAREN SEMI #outputMissingPlus
 ;

inputStream
// : INPUT LPAREN RPAREN #inputBlank
 : (INPUT LPAREN '"' RPAREN | INPUT LPAREN (expression (ADD expression)*)? ADD '"' RPAREN) #inputMissingQuote
 | INPUT LPAREN (expression (ADD expression)*)? RPAREN #inputCorrect
 | INPUT LPAREN (expression (ADD expression)*)? '"' RPAREN #inputExtraQuote
 | INPUT LPAREN (expression (ADD expression)* ADD (ADD expression)* | ADD expression)RPAREN #inputExtraPlus
 | INPUT LPAREN expression+ RPAREN #inputMissingPlus
 ;

/** Lexer Rules **/

BOOLEAN:    'boolean'; 
BREAK:      'stop';
CATCH:      'catch'; 
CHAR:       'char'; 
DO:         'do'; 
DOUBLE:     'float'; 
ELSE:       'else'; 
FINAL:      'constant'; 
FINALLY:    'lastly'; 
FOR:        'for'; 
IF:         'if';
INT:        'int'; 
NEW:        'create';
RETURN:     'return'; 
THROWS:     'throws';
TRY:        'try';
VOID:       'void'; 
WHILE:      'while';
OUTPUT:     'output'
      |    'printme' ;
START:      'start';
STRING:     'String';
INPUT:      'inputInt'
     |     'inputFloat'
     |     'inputChar'
     |     'inputString'
     ;
// Literals
//DECIMAL_LITERAL: ('0' | [1-9] (Digits? | '_'+ Digits)) [lL]?;
DECIMAL_LITERAL: ('-')? Digits ; //negative or positive dec
FLOAT_LITERAL: ('-')?(Digits DOT Digits? | DOT Digits)  //negative or positive float
 | Digits 
 ;

BOOL_LITERAL: 'T'
 | 'F'
 ;

CHAR_LITERAL: '\'' (~['\\\r\n] | EscapeSequence) '\'';
STRING_LITERAL
 : '"' (~["\\\r\n]
 | EscapeSequence)* '"'
 ;
NULL_LITERAL: 'empty';

// Separators
LPAREN:     '(';
RPAREN:     ')';
LBRACE:     '{';
RBRACE:     '}';
LBRACK:     '[';
RBRACK:     ']';
SEMI:       ';';
COMMA:      ',';
DOT:        '.';

// Operators
ASSIGN:     '=';
GT:         '>';
LT:         '<';
BANG:       '!';
COLON:      ':';
EQUAL:      '=='; 
LE:         '<=';
GE:         '>=';
NOTEQUAL:   '!='; 
AND:        'and';
OR:         'or';
INC:        '++';
DEC:        '--';
ADD:        '+';
SUB:        '-';
MUL:        '*';
DIV:        '/';
MOD:        '%';
ADD_ASSIGN: '+=';
SUB_ASSIGN: '-=';
MUL_ASSIGN: '*=';
DIV_ASSIGN: '/=';
MOD_ASSIGN: '%=';
OTHERS: '?' | '~' | '#' ;

// Whitespace and comments
WS: [ \t\r\n\u000C]+ -> channel(HIDDEN);
COMMENT: '/*' (COMMENT|.)*? '*/' -> channel(HIDDEN);
//LINE_COMMENT: '//' ~[\r\n]* -> channel(HIDDEN);
LINE_COMMENT: '//' ~[\r\n]* -> channel(HIDDEN);

// Identifiers
IDENTIFIER: Letter LetterOrDigit*;

// Fragment rules
fragment ExponentPart
 : [eE] [+-]? Digits
 ;
fragment EscapeSequence
 : '\\' [btnfr"'\\]
 | '\\' ([0-3]? [0-7])? [0-7]
 ;
fragment Digits
 : [0-9] ([0-9_]* [0-9])?
 ;
fragment LetterOrDigit
 : Letter
 | [0-9]
 ;
fragment Letter
 : [a-zA-Z$_] // these are the "java letters" below 0x7F
 | ~[\u0000-\u007F\uD800-\uDBFF] // covers all characters above 0x7F which are not a surrogate
 | [\uD800-\uDBFF] [\uDC00-\uDFFF] // covers UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF
 ;