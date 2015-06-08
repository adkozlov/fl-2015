grammar BoolExpression

package ru.spbau.kozlov.slr.bool

E Expression { boolean value }
T Term { boolean value }
F Factor { boolean value }

|| Or
! Not
&& And
( LeftParenthesis
) RightParenthesis
true True { boolean value }
false False { boolean value }

E -> T {
  $0.value
}
E -> T || E {
  $0.value || $2.value
}
T -> F {
  $0.value
}
T -> F && T {
  $0.value && $2.value
}
F -> true {
  true
}
F -> false {
  false
}
F -> ! true {
  false
}
F -> ! false {
  true
}
F -> ( E ) {
  $1.value
}
F -> ! ( E ) {
  $2.value
}

start E