CHANGELOG
=========


###### 2.0.1 [IN PROGRESS]
 -  [ADD] New `raw` value for `OutputType` to generate drawable from SVG as is
 -  [ADD] Use `TaskInputs` and `TaskOutputs` to plug with gradle **UP-TO-DATE** mechanism
 -  [ADD] Multiple input directory #34
     -  Gradle task **MUST** now use FileCollection instead of File
     -  replace `from = file("dir")` with  `from = files("dir1", "dir2", ...)`
     -  idem for `svgMaskFiles` and `svgMaskResourceFiles` options which have been renamed /!\
 -  [CHG] Upgrade batik version
 -  [CHG] Upgrade gradle version
 -  [FIX] Fix SVG mask resource filter #42
 -  [FIX] Fix use of constrained density qualifiers in SVG mask resources #45


###### 2.0.0 [05 SEP 2015]
 -  [ADD] Support for new `round` qualifier #35
 -  [ADD] Custom bound scaling for input SVG #38
 -  [FIX] Better error message
 -  [DEL] Remove high resolution icon generation
 -  [DEL] Remove `rename` option


###### 1.0.2 [08 JUN 2015]
 -  [ADD] Add support for `mipmap` generation #10
 -  [ADD] New `watch` value for `uiMode` qualifier
 -  [FIX] Fix output directory creation when `createMissingDirectory` is set to `true` #22
 -  [FIX] More detailed exception when failing #27
 -  [FIX] Fix SVG bounds calculation #29

###### 1.0.1 [02 MAR 2015]
 -  [FIX] Disable `fallbackDensity` option

###### 1.0.0 [21 DEC 2014]
 -  Initial version
