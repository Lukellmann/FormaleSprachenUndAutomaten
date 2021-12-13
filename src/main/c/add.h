#include "LOOP.h"

// needs z[1], z[2], y[1]
#define ADD                              \
    /* copy input */                     \
    z[1] = x[1]; z[2] = x[2];            \
                                         \
    /* Pg; g(x1) = x1 */                 \
    x[0] = x[1];                         \
                                         \
    y[1] = 1;                            \
                                         \
    LOOP (z[2]) DO                       \
        x[1] = z[1];                     \
        x[2] = y[1] - 1;                 \
        x[3] = x[0];                     \
                                         \
        /* Ph; h(x1, x2, x3) = x3 + 1 */ \
        x[0] = x[3] + 1;                 \
                                         \
        y[1] = y[1] + 1;                 \
    END
