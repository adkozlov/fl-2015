grammar ArithmeticExpressions

package ru.spbau.kozlov.slr

E expression { int value }
T term { int value }
F factor { int value }

+ PLUS
- MINUS
* MULTIPLY
( LEFT_PARENTHESIS
) RIGHT_PARENTHESIS
int INTEGER { int value , boolean isZero }

E -> T {
  $0.value
}
E -> T + E {
  $0.value + $2.value
}
T -> F {
  $0.value
}
T -> F * T {
  $0.value * $2.value
}
F -> int {
  $0.value
}
F -> - int {
  -$1.value
}
F -> ( E ) {
  $1.value
}

start E