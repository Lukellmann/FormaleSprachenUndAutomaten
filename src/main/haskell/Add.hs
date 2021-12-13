module Add (add) where

import Numeric.Natural (Natural)

g x1 = x1

h x1 x2 x3 = x3 + 1

add :: Natural -> Natural -> Natural
add x 0 = g x
add x y = h x (y - 1) (add x (y - 1))
