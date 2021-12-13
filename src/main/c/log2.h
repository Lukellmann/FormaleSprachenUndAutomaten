#include "WHILE.h"
#include "pot.h"
#include "min.h"

// needs z[7], z[8], y[4]
#define LOG2             \
    x[2] = x[1];         \
    z[7] = x[2];         \
    y[4] = 0;            \
    x[1] = y[4];         \
    PHLOG2;              \
    WHILE (x[0] != 0) DO \
        y[4] = y[4] + 1; \
        x[1] = y[4];     \
        x[2] = z[7];     \
        PHLOG2;          \
    END                  \
    x[0] = y[4];

#define PHLOG2   \
    z[8] = x[2]; \
    x[2] = x[1]; \
    x[1] = 2;    \
    POT;         \
    x[1] = z[8]; \
    x[2] = x[0]; \
    MIN;
