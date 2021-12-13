#include "add.h"

// needs z[3], z[4], y[2]
#define MUL                                   \
    /* copy input */                          \
    z[3] = x[1]; z[4] = x[2];                 \
                                              \
    /* Pg; g(x1) = 0 */                       \
    x[0] = 0;                                 \
                                              \
    y[2] = 1;                                 \
                                              \
    LOOP (z[4]) DO                            \
        x[1] = z[3];                          \
        x[2] = y[2] - 1;                      \
        x[3] = x[0];                          \
                                              \
        /* Ph; h(x1, x2, x3) = add(x1, x3) */ \
        x[1] = x[1]; x[2] = x[3]; ADD;        \
                                              \
        y[2] = y[2] + 1;                      \
    END
