#pragma once

#include <string>

class Person {
    std::string name;

public:
    Person(std::string name) {
        this->name = name;
    }

    virtual std::string getName();
};