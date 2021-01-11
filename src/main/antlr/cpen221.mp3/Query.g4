grammar Query;

@header {
package cpen221.mp3;
}

@members {
    // This method makes the lexer or parser stop running if it encounters
    // invalid input and throw a RuntimeException.
    public void reportErrorsAsExceptions() {
        //removeErrorListeners();

        addErrorListener(new ExceptionThrowingErrorListener());
    }

    private static class ExceptionThrowingErrorListener
                                              extends BaseErrorListener {
        @Override
        public void syntaxError(Recognizer<?, ?> recognizer,
                Object offendingSymbol, int line, int charPositionInLine,
                String msg, RecognitionException e) {
            throw new RuntimeException(msg);
        }
    }
}

GET : 'get';
WHERE: 'where';
IS : 'is';


PAGE : 'page';
AUTHOR : 'author';
CATEGORY : 'category';

TITLE : 'title';

LPAREN : '(';
RPAREN : ')';
AND : 'and';
OR : 'or';

STRING : '\'' ( ~'\'' | '\'\'' )* '\'';

SORTED : ASC | DESC;
ASC : 'asc';
DESC : 'desc';

WHITESPACE : [ \t\r\n]+ -> skip ;

query : GET item WHERE condition SORTED? EOF;
condition : LPAREN condition AND condition RPAREN | LPAREN condition OR condition RPAREN | simple_condition;
simple_condition : TITLE IS STRING | AUTHOR IS STRING | CATEGORY IS STRING;
item : PAGE | AUTHOR | CATEGORY;

