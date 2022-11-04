"""
This module contains all of the dependencies installed via rules_jvm_external
"""

#####################
# JAVA DEPENDENCIES #
#####################
MAVEN_REPOS = [
    "https://repo1.maven.org/maven2",
]

VERTX_VERSION = "4.3.4"
JAVA_POET_VERSION = "1.13.0"

JAVA_DEPENDENCIES = [
    "io.vertx:vertx-core:%s" % VERTX_VERSION,
    "io.vertx:vertx-unit:%s" % VERTX_VERSION,
    "com.squareup:javapoet:%s" % JAVA_POET_VERSION,
]

#############################
#     TEST DEPENDENCIES     #
#############################
JUNIT_JUPITER_VERSION = "5.9.1"

JAVA_TEST_DEPENDENCIES = [
    "org.junit.jupiter:junit-jupiter-api:%s" % JUNIT_JUPITER_VERSION,
    "org.junit.jupiter:junit-jupiter-engine:%s" % JUNIT_JUPITER_VERSION,
]