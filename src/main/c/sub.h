#include "sub1.h"

// needs z[2], z[3], y[2]
#define SUB                                \
    /* copy input */                       \
    z[2] = x[1]; z[3] = x[2];              \
                                           \
    /* Pg; g(x1) = x1 */                   \
    x[0] = x[1];                           \
                                           \
    y[2] = 1;                              \
                                           \
    LOOP (z[3]) DO                         \
        x[1] = z[2];                       \
        x[2] = y[2] - 1;                   \
        x[3] = x[0];                       \
                                           \
        /* Ph; h(x1, x2, x3) = sub1(x3) */ \
        x[1] = x[3]; SUB1;                 \
                                           \
        y[2] = y[2] + 1;                   \
    END
