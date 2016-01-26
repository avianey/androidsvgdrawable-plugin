Android SVG Drawable plugin
=========================

[![Build Status](https://travis-ci.org/avianey/androidsvgdrawable-plugin.png?branch=master)](https://travis-ci.org/avianey/androidsvgdrawable-plugin)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/fr.avianey.androidsvgdrawable/gradle-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/fr.avianey.androidsvgdrawable/gradle-plugin)  

Every Android application should provide [alternative resources](http://developer.android.com/guide/topics/resources/providing-resources.html#AlternativeResources) to support specific device configurations such as `portrait`, `landscape`, `small`, `large`, `us`, `fr`, ... Because we don't want to edit ~~thousands of~~ several images every time we need to change a single pixel, a color, a shadow size or a text value, this  plugin generates for you **density specific** bitmap drawable resources from qualified SVG files **at build time**.  

The only thing you have to do is to provide one or more qualified SVG files that will be converted for you at build time into as many as needed bitmaps and organized into configuration-specific drawable directories... at least one for each targeted screen density ! You'll never deal with raster resources anymore...

This plugin handles any configuration `<qualifier>` supported by the Android SDK and can generate [NinePatch](http://developer.android.com/reference/android/graphics/NinePatch.html) drawable as well. To increase your productivity, a (incubating) mask functionnality allow you to define generic layers, filters, clip-path, etc...  and let you reuse them across multiple SVG files. That's [DRY](http://en.wikipedia.org/wiki/Don%27t_repeat_yourself)  applied to Android drawable resources. 

Enjoy :wink: !

-   [Gradle quick start](#gradle)
-   [Maven quick start](#maven)
-   [How to use](#how-to-use)
    -   [Input SVG files](#input-svg-files)
        -   [Expected SVG file names](#expected-file-names)
        -   [SVG Bounding Box](#bounding-box)
        -   [Adjusting the Bounding Box](#adjusting-the-bounding-box)
        -   [Generated bitmaps](#generated-bitmaps)
    -   [Nine-Patch support](#nine-patch-support)
    -   [SVG Masking](#svg-masking)
        -   [MASK file format](#mask-file-format)
        -   [Generated MASKED files](#generated-mask-files)
    -   [Plugin options](#plugin-options)
	    -   [Options list](#options-list)
	    -   [Typical Gradle configuration](#typical-gradle-configuration)
	    -   [Typical Maven configuration](#typical-maven-configuration)
-   [Best practices](#best-practices)
-   [Who's using it](#whos-using-it)
-   [License](#license)


:warning: Read CHANGELOG.md when upgrading the plugin version

## Gradle

Add maven central repository and the plugin reference to your build script :

```gradle
buildscript {
    repositories {
        jcenter()
        mavenCentral()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:1.3.1'
        classpath('fr.avianey.androidsvgdrawable:gradle-plugin:3.0.0') {
            // should be excluded to avoid conflict
            exclude group: 'xerces'
        }
    }
}
```

You can configure one or more Task with the desired configuration into your build.gradle and apply the `androidsvgdrawable` plugin. Tasks will be executed for you when necessary to generate drawable resources :

```gradle
apply plugin: 'com.android.application'
apply plugin: "androidsvgdrawable"

// create a task to convert SVG to PNG
task svgToPng(type: fr.avianey.androidsvgdrawable.gradle.SvgDrawableTask) {
    // configuration, see sample project
}
```
If you don't want the plugin to execute the task automatically, you can call your task directly through Gradle :  

 ```
gradlew svgToPng
 ```
 
You can define as many task as you need.

## Maven

Add the plugin to your pom.xml :

```xml
<plugin>
    <groupId>fr.avianey.androidsvgdrawable</groupId>
    <artifactId>maven-plugin</artifactId>
    <version>3.0.0</version>
    <executions>
        <execution>
            <id>gendrawable-png</id>
            <configuration><!-- see sample project --></configuration>
            <phase>initialize</phase>
            <goals>
                <goal>gen</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```
The plugin `gen` mojo can also be executed on demand through its goal prefix :
```
mvn svgdrawable:gen
```

## How to use

The key words "MUST", "MUST NOT", "REQUIRED", "SHALL", "SHALL NOT", "SHOULD", "SHOULD NOT", "RECOMMENDED",  "MAY", and "OPTIONAL" in this document are to be interpreted as described in [RFC 2119](https://www.ietf.org/rfc/rfc2119.txt).

### Input SVG files 

This plugin is based on [Apache Batik 1.7](http://xmlgraphics.apache.org/batik/) and thus supports most of the [SVG 1.1 Specification](http://www.w3.org/TR/SVG11/). See the [Batik status page](http://xmlgraphics.apache.org/batik/status.html) for the list of supported, not-supported and partially supported SVG 1.1 features.

#### Expected file names

Input SVG file names **MUST** match the following pattern : `\w+(-{qualifier})+.svg`. It is composed of three parts :

1.  `\w+` is the SVG *unqualified* name part
2.  `(-{qualifier})+` is the SVG *qualified* name part
3.  `.svg` is the extension of the file

Each SVG file **MUST** provide a valid reference density qualifier in its name. The generated bitmap (JPG or PNG) will be scaled relatively to its reference density. The generated bitmap for the reference density will have the same dimensions as defined by the [SVG bounding box](#bounding-box).  

Qualifiers can appear in any order within the SVG file name. The plugin takes care of re-ordering the qualifiers properly as expected by the Android SDK. For a full list of supported qualifiers, see [Providing Alternative Resources](http://developer.android.com/guide/topics/resources/providing-resources.html#AlternativeResources) on the Android developper website.

Example of valid SVG file name :

-   `icon-mdpi.svg`
-   `flag-fr-land-mdpi.svg`
-   `flag-fr-land-w64mdpi.svg`
-   `more_complex_name-land-mdpi-fr-sw700dp.svg`  

Example of invalid SVG file name :

-  `icon.svg`
	-  no qualifier at all
-  `flag-fr.svg`
	-  no density qualifier
-  `invalid-classifier-mdpi.svg`
	-  `classifier` is not a valid Android resource qualifier
-  `éléphant-mdpi.svg`
	-  invalide `\w` character in the *unqualified* name part

#### Bounding Box

As explained above, each input SVG file name **MUST** provide a density qualifier. This qualifier is used to compute the scaled `width` and `height` of the generated bitmaps. The reference `width` and `height` for the reference density are those defined by the `width` and `height` attributes of the `<svg>` root element of the input SVG file :

```xml
<svg
   x="0"
   y="0"
   width="96"
   height="96"
```
	  
This will define the bounding box of the drawable content. Everything that is drawn outside off this bounding box will not be rendered in the generated bitmaps. `x` and `y` attributes are default to `0px` if not present. 

Inkscape provides a way to make the SVG bounding box match the content of the document :  

1.  Open File > Document Properties `[CTRL]`+`[SHIFT]`+`[D]`
2.  In the Page tab select Page Size > Custom Size > Resize page to content

If you want the bounding box to be larger than the content (with extra border), you'll need to add an extra transparent shape that is larger than the content and that match the desired width and height before using this tip.  
  
You **SHOULD** adjust your input SVG file `width` and `height` to be a multiple of `32` and set its reference density to `mdpi` so they can be scaled to any density without rounding the bounding box. The `width` and `height` attributes of the `<svg>` element are rouded (when necessary) to the smallest integer that is greater than or equal to the value of the scaled attribute. You **SHOULD** use round integer values expressed in pixels `px` or without unit of length as much as possible...   

It's also possible to use valid SVG unit of length such as `mm`, `cm`, `pt`, `in`. Use it with caution :-).  

#### Adjusting the Bounding Box

If you don't want to adjust your input SVG file `width` and `height` you can use a **constrained** density qualifier that will adjust the input SVG border to the given size in pixels.  
  
Given the following SVG file you don't want to edit :

```xml
<svg
   x="0"
   y="0"
   width="500"
   height="500"
```

You can adjust it to be 32 pixels width or height at the `mdpi` density by changing its name to :

-   `name-w32mdpi.svg`
-   `name-h32mdpi.svg`

This will force the given size in pixel for the output drawable width or height at the specified density (preserving the aspect ratio of the SVG Bounding Box). Output drawable for other densities will be scaled regarding the **constrained** size specified in the SVG file name instead of the size specified in the `width` and `height` attribute of the `<svg>` element.

#### Generated bitmaps

The generated bitmaps are named against `\w+.png`, `\w+.jpg` or `\w+.9.png` if it's a nine-patch drawable and are generated into a `/res/drawable(-{qualifier})*` directory where :  

-   `\w+` is the *unqualified* part of the input SVG file
-   `(-{qualifier})*` is the re-ordered *qualified* part of the input SVG file minus the density qualifier

#### Nine-Patch support

If you want to generate bitmaps as NinePatch Drawable, you **MUST** provide Nine-Patch configuration file that specifies the **stretchable area** and the **padding box** as defined in the Android documentation related to [nine-patch](http://developer.android.com/guide/topics/graphics/2d-graphics.html#nine-patch). The Nine-Patch configuration file consists in a JSON Array containing at least one entry :

```javascript
[
    {
        "name" : "phone.*",  // unqualified part of the SVG file name (JAVA regexp)
        "qualifiers" : [ // optionnal array of qualifiers to filter input SVG 
            "land" // this config applies only to \w+.*-land.*.svg files
        ], 
        "stretch" : { // the stretchable area configuration
            "x" : [ // segments of the top edge of the NinePatch
                [3, 43],
				... // you can add as many segments as you want
            ],
            "y" : [ // segments of the left edge of the NinePatch
                [3, 29]
				... // you can add as many segments as you want
            ]
        },
		"content" : { // the padding box configuration
            "x" : [ // segments of the bottom edge of the NinePatch
                [3, 43]
				... // you should provide only one segment here
            ],
            "y" : [ // segments of the right edge of the NinePatch
                [3, 29]
				... // you should provide only one segment here
            ]
		}
    },
    // ... other nine-patch config
]
```
If no segment is provided along an edge, the whole edge will be filled.  

If you have different SVG with the same name but with different qualifiers, you can provide a specific Nine-Patch configuration by using an array of qualifiers. A Nine-Patch configuration apply only to input SVG files which qualified name part match **ALL** of the Nine-Patch qualifiers.  
  
For input SVG files that use a **constrained** density qualifier (adjusted Bounding Box), the **stretchable area** and the **padding box** segments **MUST NOT** be specified using the adjusted Bounding Box. They **MUST** use the regular Bounding Box of the input SVG file.

### SVG Masking

SVG Masking takes advantage of the SVG `<image>` element and allow you to define generic layers, masks and filters as well as Nine-Patch configuration for the generated bitmaps. 

#### MASK file format

SVGMASK files are particular SVG files named against the same rule as [input SVG files](#input-svg-files) except for the `.svgmask` extension. SVGMASK files **MUST** contain at least one *capturing* `<image>` element with a `xlink:href` attribute which value matches against `#\{(.*)\}` and where the captured part of the attribute value is a valid [JAVA Pattern](http://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html) :   

-   `<image x="0" y="0" width="10" height="10" xlink:href="#{btn_.*}"/>`
-   `<image x="0" y="0" width="10" height="10" xlink:href="#{[a-z]{2}}"/>`

SVGMASK files **MAY** contains more than one *capturing* `<image>` element as well as standard SVG `<image>` elements.  

#### Generated masked files

SVGMASK files are not directly converted into bitmaps. SVGMASK files are converted into temporary SVG files that are added to the list of [input SVG files](#input-svg-files) to convert. Those temporary SVG files are copies of the SVGMASK files DOM in which the `xlink:href` attribute value of each *capturing* `<image>` is replaced by a `file:///` URI linking to an captured input SVG file.

An input SVG file is captured by a *capturing* `<image>` element of a SVGMASK if it verifies the two following conditions :  

1.  The unqualified name of the input SVG file match the capturing regexp of the *capturing* `<image>` element
2.  The qualified name of the input SVG file contains all of the qualifiers values defined in the SVGMASK qualified name (except for the density qualifier that is not taken into account)

A carthesian product is made between each set of captured input SVG file for each *capturing* `<image>` element of the SVGMASK file. A temporary SVG file is created for each `xlink:href` attribute combination in the resulting product set except for combinations that contains input SVG files with incompatible qualifiers. Incompatible SVG files are input SVG files that defines different values for a same configuration qualifier type.

Combination that uses the same URI for two *capturing* `<image>` elements can be skipped or not depending on the value of the `useSameSvgOnlyOnceInMask` parameter.

For each kept entry of the product set the generated temporary SVG file name is the concatenation of :

1.  The SVGMASK unqualified name
2.  The input SVG file unqualified name for each *capturing* `<image>` element `xlink:href` URI
	-   in the order of the *capturing* `<image>` element in the SVG file DOM
3.  The union of the qualifier values for the SVGMASK and all of the linked input SVG files
	-   The density qualifier of the SVGMASK is used
	-   The SVGMASK bounding box is used as reference
	-   Linked input SVG files are scalled according to the `<image>`element specifications

Resulting bitmaps are generated from those temporary SVG files following the same rules as for standard [Generated bitmaps](#generated-bitmaps). They can also produce Nine-Patch drawable as the Nine-Patch configuration file elements **MAY** reference temporary SVG files,see [Nine-Patch support](nine-patch-support).

### Plugin options

#### Options list

The plugin can be configured using the following options : 

| Name | Format | Description |  
| :--- | :----- | :---------- |  
|from|FileCollection|Collection of Path that contains the SVG files to generate drawable from. SVG files **MUST** be named against `\w+(-{qualifier})+.svg` and **MUST** contain a valid density qualifier (ldpi, mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi, tvdpi). Could point to single files or to directories that will be scanned to find SVG files.|  
|to|File|Path to the Android `res/` directory that contains the various `drawable/` directories.|  
|createMissingDirectories|boolean|Set it to `false` if you don't want the plugin to create missing drawable(-{qualifier})*/ directories. The default value is set to `true`.|  
|ninePatchConfig|File|Path to the 9-Patch JSON configuration file.|  
|overwriteMode|`always`, `never` or `ifModified`|Whether or not already existing and up to date PNG should be overridden at build time.|  
|targetedDensities|List|List of the desired densities for the generated drawable. If not specified, a drawable is generate for each density qualifier that is supported by the android SDK.|  
|outputFormat|`PNG` or `JPG`|The format of the generated bitmaps. Nine-Patch support apply only for the `PNG` output format.|  
|outputType|`drawable` or `mipmap`|The output directory for the generated bitmaps. Nine-Patch support apply only for the `drawable` output type.|  
|jpgQuality|Integer|The quality use for the JPG compression between 0 and 100 (higher is better). Default value is `85` (like Gimp).|  
|jpgBackgroundColor|Integer|The background color used for the generated JPG bitmaps. Default is `0xFFFFFFFF` (opaque white).|  
|svgMaskFiles|FileCollection|An optionnal collection of Path to pick the SVGMASK files from. Default to the same directory as the `from` parameter.|  
|svgMaskResourceFiles|FileCollection|An optionnal collection of Path to pick the SVG files to mask from. Default to the same directory as the `svgMaskedSvgOutputDirectory` parameter.|  
|useSameSvgOnlyOnceInMask|boolean|Tell the plugin to skip SVGMASK combinations that use the same SVG resource more than once. Default is `true`.|  

#### Typical Gradle configuration

Check the [Gradle sample project](https://github.com/avianey/androidsvgdrawable-plugin/tree/master/sample/gradle) . 

#### Typical Maven configuration

Check the [Maven sample project](https://github.com/avianey/androidsvgdrawable-plugin/tree/master/sample/maven) . 

#### Typical LibGDX configuration

Check the [LibGDX sample project](https://github.com/avianey/androidsvgdrawable-plugin/tree/master/sample/libgdx) . 

## Best practices

1.  Use a custom temporary output directory for every configuration
    * see https://github.com/avianey/androidsvgdrawable-plugin/wiki/How-to-use-with-flavor  
2.  `overwriteMode` **SHOULD** be forced to `always` for release build
3.  Perform a `clean` when you upgrade `androidsvgdrawable-plugin`

## Who's using it

*  [Bubble Level](https://play.google.com/store/apps/details?id=net.androgames.level)
*  [Yatzy (Dice Game)](https://play.google.com/store/apps/details?id=fr.pixelprose.dice)
  
Pull Request to add yours !

## License

```
Copyright 2013, 2014, 2015 Antoine Vianey  

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at  

http://www.apache.org/licenses/LICENSE-2.0  

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
