#include "LOOP.h"

// needs z[1], y[1]
#define SUB1                     \
    /* copy input */             \
    z[1] = x[1];                 \
                                 \
    /* Pg; g = 0 */              \
    x[0] = 0;                    \
                                 \
    y[1] = 1;                    \
                                 \
    LOOP (z[1]) DO               \
        x[1] = y[1] - 1;         \
        x[2] = x[0];             \
                                 \
        /* Ph; h(x1, x2) = x1 */ \
        x[0] = x[1];             \
                                 \
        y[1] = y[1] + 1;         \
    END
