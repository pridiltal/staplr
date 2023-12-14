.onLoad <- function(libname, pkgname){
  pdftk_jar <- system.file('pdftk-java/pdftk.jar', package = pkgname, mustWork = TRUE)
  rJava::.jpackage(pkgname, morePaths = pdftk_jar, lib.loc = libname)
  jv <- rJava::.jcall("java/lang/System", "S", "getProperty", "java.runtime.version")
  if(substr(jv, 1L, 2L) == "1.") {
    jvn <- as.numeric(paste0(strsplit(jv, "[.]")[[1L]][1:2], collapse = "."))
    if(jvn < 1.8) stop("Java >= 8 is needed for staplr but it is not available")
  }
}
