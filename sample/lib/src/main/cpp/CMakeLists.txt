cmake_minimum_required(VERSION 3.18.1)

set(CMAKE_VERBOSE_MAKEFILE 1)

project("sample" LANGUAGES C)

add_library(sample SHARED include/Company.h)

set_target_properties(sample PROPERTIES LINKER_LANGUAGE C)

include_directories(include)

if(COMPANY_YEAR)
    target_compile_definitions(sample INTERFACE COMPANY_YEAR)
endif()

find_package(SampleWrapper REQUIRED CONFIG)

target_link_libraries(SampleWrapper PUBLIC sample)