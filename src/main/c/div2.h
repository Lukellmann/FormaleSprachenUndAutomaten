#include "WHILE.h"
#include "mul.h"
#include "sub.h"

// needs z[5], z[6], y[3]
#define DIV2             \
    x[2] = x[1];         \
    z[5] = x[2];         \
    y[3] = 0;            \
    x[1] = y[3];         \
    PHDIV2;              \
    WHILE (x[0] != 0) DO \
        y[3] = y[3] + 1; \
        x[1] = y[3];     \
        x[2] = z[5];     \
        PHDIV2;          \
    END;                 \
    x[0] = y[3];

#define PHDIV2   \
    z[6] = x[2]; \
    x[2] = 2;    \
    MUL;         \
    x[1] = z[6]; \
    x[2] = x[0]; \
    SUB
