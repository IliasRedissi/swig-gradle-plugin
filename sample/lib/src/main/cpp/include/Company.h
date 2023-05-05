#pragma(once)

#include "string"

class Company {
    std::string name;
    int foundationYear;

public:
    Company(std::string name, int foundationYear) {
        this->name = name;
        this->foundationYear = foundationYear;
    }

    virtual std::string getName();

#ifdef COMPANY_YEAR
    virtual int getFoundationYear();
#endif

};