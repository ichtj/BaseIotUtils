cmake_minimum_required(VERSION 3.10.2)

project("dual_camera")

# ================= 基础库 =================

find_library(log-lib log)
find_library(m-lib m)
find_library(z-lib z)

# ================= dual_camera（C++）=================

add_library(
        dual_camera
        SHARED
        CameraToolsNew.cpp
)

target_compile_options(
        dual_camera
        PRIVATE -std=c++14
)

target_link_libraries(
        dual_camera
        ${log-lib}
        ${m-lib}
        ${z-lib}
)

# ================= crashtest（C++）=================

add_library(
        crashtest
        SHARED
        crashtest.cpp
)

target_compile_options(
        crashtest
        PRIVATE -std=c++14
)

target_link_libraries(
        crashtest
        ${log-lib}
        ${m-lib}
        ${z-lib}
)

# ================= hidtest（C语言）=================

add_library(
        hidtest
        SHARED
        hid_gadget_test.c
)

# ⭐ C语言要用这个
target_compile_options(
        hidtest
        PRIVATE -std=c11
)

target_link_libraries(
        hidtest
        ${log-lib}
        ${m-lib}
        ${z-lib}
)