module Mul (mul) where

import Add (add)
import Numeric.Natural (Natural)

g x1 = 0

h x1 x2 x3 = add x1 x3

mul :: Natural -> Natural -> Natural
mul x 0 = g x
mul x y = h x (y - 1) (mul x (y - 1))
