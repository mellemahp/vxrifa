#######################
# RIFA
#######################
java_library(
    name = "vertx_rifa",
    srcs = glob(["src/main/java/io/github/nsforth/vxrifa/**/*.java"]),
    resources = glob(["src/main/resources/**/*"]),
    deps = [
        "@maven//:io_vertx_vertx_core",
        "@maven//:io_vertx_vertx_unit",
        "@maven//:com_squareup_javapoet",
    ],
    visibility = ["//visibility:public"]
)