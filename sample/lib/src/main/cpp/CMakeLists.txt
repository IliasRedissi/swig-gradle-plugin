cmake_minimum_required(VERSION 3.18.1)

set(CMAKE_VERBOSE_MAKEFILE 1)

project("sample")

add_library(sample
            SHARED
            Company.cpp)

include_directories(.)

if(COMPANY_YEAR)
    target_compile_definitions(sample PUBLIC COMPANY_YEAR)
endif()

find_library(log-lib log)

target_link_libraries(sample ${log-lib})

find_package(SampleWrapper REQUIRED CONFIG)

target_link_libraries(SampleWrapper sample)