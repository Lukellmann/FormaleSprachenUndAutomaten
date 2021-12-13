#include "mul.h"

// needs z[5], z[6], y[3]
#define POT                                   \
    /* copy input */                          \
    z[5] = x[1]; z[6] = x[2];                 \
                                              \
    /* Pg; g(x1) = 1 */                       \
    x[0] = 1;                                 \
                                              \
    y[3] = 1;                                 \
                                              \
    LOOP (z[6]) DO                            \
        x[1] = z[5];                          \
        x[2] = y[3] - 1;                      \
        x[3] = x[0];                          \
                                              \
        /* Ph; h(x1, x2, x3) = mul(x1, x3) */ \
        x[1] = x[1]; x[2] = x[3]; MUL;        \
                                              \
        y[3] = y[3] + 1;                      \
    END
