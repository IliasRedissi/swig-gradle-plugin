cmake_minimum_required(VERSION 3.18.1)

set(CMAKE_VERBOSE_MAKEFILE 1)

project("sample-groovy")

add_library(sampleGroovy
            SHARED
            Employee.cpp
            Person.cpp)

include_directories(.)

find_library(log-lib log)

find_package(lib REQUIRED CONFIG)

target_link_libraries(sampleGroovy ${log-lib} lib::sample)

find_package(SampleGroovyWrapper REQUIRED CONFIG)

target_link_libraries(SampleGroovyWrapper sampleGroovy)