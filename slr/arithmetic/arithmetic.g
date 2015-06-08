grammar ArithmeticExpressions

package ru.spbau.kozlov.slr.arithmetic

E Expression { int value }
T Term { int value }
F Factor { int value }

+ Plus
- Minus
* Multiply
( LeftParenthesis
) RightParenthesis
int Integer { int value }

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
F -> - ( E ) {
  -$2.value
}

start E