#pragma(once)

struct Company {
    char *name;
#ifdef COMPANY_YEAR
    int foundationYear;
#endif
};
