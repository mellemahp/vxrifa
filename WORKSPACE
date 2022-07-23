workspace(name = "vxrifa")

#######################
# Base bazel tool imports
#######################
load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

#######################
# Java Library Imports
#######################
# import libraries from maven
RULES_JVM_EXTERNAL_TAG = "4.2"

RULES_JVM_EXTERNAL_SHA = "cd1a77b7b02e8e008439ca76fd34f5b07aecb8c752961f9640dea15e9e5ba1ca"

http_archive(
    name = "rules_jvm_external",
    sha256 = RULES_JVM_EXTERNAL_SHA,
    strip_prefix = "rules_jvm_external-%s" % RULES_JVM_EXTERNAL_TAG,
    url = "https://github.com/bazelbuild/rules_jvm_external/archive/%s.zip" % RULES_JVM_EXTERNAL_TAG,
)

load("@rules_jvm_external//:repositories.bzl", "rules_jvm_external_deps")

rules_jvm_external_deps()

load("@rules_jvm_external//:setup.bzl", "rules_jvm_external_setup")

rules_jvm_external_setup()

load("@rules_jvm_external//:defs.bzl", "maven_install")

MAVEN_REPOS = ["https://repo1.maven.org/maven2"]


###########################
# Maven Dependencies
###########################
maven_install(
    name = "vxrifaMaven",
    artifacts = [
        "io.vertx:vertx-core:4.2.1",
        "io.vertx:vertx-unit:4.2.1",
        "com.squareup:javapoet:1.13.0",
        # test dependencies
        "org.junit.jupiter:junit-jupiter-api:5.8.2",
        "org.junit.jupiter:junit-jupiter-engine:5.8.2",
    ],
    repositories = MAVEN_REPOS,
)
