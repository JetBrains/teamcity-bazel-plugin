

grammar BazelBuildFile ;

buildFile : ( variable | function )* ;

function  : ID '('
                ( value | namedParameter )
                (',' ( value | namedParameter ))* ','?
                ')' ;

variable  : ID '=' value ;

namedParameter: ID '=' value ;

value     : STRING
          | NUMBER
          | array
          | tuple
          | object
          | function
          | value ( '+' | '-' | '*' | '/' ) value
          | 'not' value
          | ID
          | ID 'for' ID 'in' value ;

array     : '[' value (',' value)* ','? ']'
          | '[' ']' ;

tuple     : '(' value ( ',' value )* ','? ')' ;

object    : '{' objectPair ( ',' objectPair )* ','? '}'
          | '{' '}'  ;

objectPair: ( STRING | NUMBER | ID ) ':' value ;

ID : [a-zA-Z] [_a-zA-Z0-9]* ; // match usual identifier spec

STRING:
    ('"'  (ESC | ~["\\])* '"'  ) |
    ('\'' (ESC | '\\\'' | ~['\\])* '\'' ) ;

fragment ESC: '\\' (["\\/bfnrt] | UNICODE);
fragment UNICODE : 'u' HEX HEX HEX HEX;
fragment HEX : [0-9a-fA-F];

NUMBER
   : '-'? INT ('.' [0-9] +)?
   ;

fragment INT
   : '0' | [1-9] [0-9]*
   ;

WS : [ \r\t\n]+ -> skip ;

COMMENT : '#' ~[\r\n]* '\r'? '\n' -> skip ;