rootProject.name = "platform-spring-boot-conventions"

includeBuild("build-logic")

include("bom")
include("starter-observability")
include("starter-security-resource-server")
include("starter-token-client")
include("starter-token-client-feign")
include("starter-error-handling")
