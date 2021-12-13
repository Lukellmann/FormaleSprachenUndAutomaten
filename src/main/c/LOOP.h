#ifndef LOOP_H
#define LOOP_H

#include <stdio.h>
#include <string.h>

#define PSTART                       \
    int x[100];                      \
    int y[100];                      \
    int z[100];                      \
    int yyx[100];                    \
    int yyy[100];                    \
    int yyz[100];                    \
    int i;                           \
    int main(int argc, char *argv[]) \
    {                                \
        for (i = 0; i < 99; i++)     \
        {                            \
            x[i] = 0;                \
        }                            \
        for (i = 1; i < argc; i++)   \
        {                            \
            x[i] = str2int(argv[i]); \
        }

#define LOOP(X) \
    for (yy##X = X; yy##X > 0; yy##X--)

#define DO {

#define END }

#define PEND              \
    printf("%d\n", x[0]); \
    return x[0];          \
    }

int str2int(char *c)
{
    int exponent;
    int erg;
    int j;
    int k;

    erg = 0;
    for (j = 0; j < strlen(c); j++)
    {
        exponent = 1;
        for (k = strlen(c) - j - 1; k > 0; k--)
        {
            exponent *= 10;
        }
        erg += (c[j] - 48) * exponent;
    }
    return erg;
}

#endif
