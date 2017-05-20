add_library(
    # Name
    Core

    # Shared Library
    SHARED

    # Sources
    Core.cpp
    Utils.cpp
)

# add system library to build Core
target_link_libraries(
    Core

    log
)
