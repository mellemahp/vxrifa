#######################
# RIFA
#######################
java_library(
    name = "vertx_rifa",
    srcs = glob(["src/main/java/io/github/nsforth/vxrifa/**/*.java"]),
    resources = glob(["src/main/resources/**/*"]),
    deps = [
        "@vxrifaMaven//:io_vertx_vertx_core",
        "@vxrifaMaven//:io_vertx_vertx_unit",
        "@vxrifaMaven//:com_squareup_javapoet",
    ],
    visibility = ["//visibility:public"]
)