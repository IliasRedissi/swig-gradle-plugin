#pragma(once)

#include "string"

class Company {
    std::string name;

public:
    Company(std::string name) {
        this->name = name;
    }

    virtual std::string getName();
};