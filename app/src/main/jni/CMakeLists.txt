add_library(
    # Name
    Core

    # Shared Library
    SHARED

    # Sources
    Core.c
    utils.c
)

# add system library to build Core
target_link_libraries(
    Core

    log
)
