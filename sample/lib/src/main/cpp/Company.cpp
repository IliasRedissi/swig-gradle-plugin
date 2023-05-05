#include "include/Company.h"

std::string Company::getName() {
    return name;
}

#ifdef COMPANY_YEAR
int Company::getFoundationYear() {
    return foundationYear;
}
#endif