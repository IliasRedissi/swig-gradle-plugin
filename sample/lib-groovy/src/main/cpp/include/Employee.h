#pragma once

#include <string>
#include "Person.h"
#include "Company.h"

class Employee : public Person {
    Company* company;
public:
    Employee(std::string name, Company* company) : Person(name) {
        this->company = company;
    }

    virtual Company* getCompany();
};
