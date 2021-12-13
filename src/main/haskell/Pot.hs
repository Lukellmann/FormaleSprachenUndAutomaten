module Pot (pot) where

import Mul (mul)
import Numeric.Natural (Natural)

g x1 = 1

h x1 x2 x3 = mul x1 x3

pot :: Natural -> Natural -> Natural
pot x 0 = g x
pot x y = h x (y - 1) (pot x (y - 1))
