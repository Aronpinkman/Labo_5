% Reglas de las cuatro operaciones basicas en Prolog.
% A y B representan los numeros ingresados; R es el resultado.

sumar(A, B, R) :-
    R is A + B.

restar(A, B, R) :-
    R is A - B.

multiplicar(A, B, R) :-
    R is A * B.

% La division solo es valida cuando el divisor es distinto de cero.
dividir(A, B, R) :-
    B =\= 0,
    R is A / B.
