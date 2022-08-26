%module SampleGroovy

%include "std_string.i"

%import "../../../../lib/src/main/swig/Sample.i"

%{
#include "include/Person.h"
#include "include/Employee.h"
%}

%include "include/Person.h"
%include "include/Employee.h"
