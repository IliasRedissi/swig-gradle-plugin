%module SampleGroovy

%include "std_string.i"

%import SWIG_SAMPLEWRAPPER_INTERFACE

%pragma(java) jniclassimports=%{
import com.redissi.sample.*;
%}

%typemap(javaimports) SWIGTYPE %{
import com.redissi.sample.*;
%}

%{
#include "include/Person.h"
#include "include/Employee.h"
%}

%include "include/Person.h"
%include "include/Employee.h"
