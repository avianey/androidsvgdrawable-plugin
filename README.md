Android SVG Drawable plugin
=========================

[![Build Status](https://travis-ci.org/avianey/androidsvgdrawable-plugin.png?branch=master)](https://travis-ci.org/avianey/androidsvgdrawable-plugin)  

Every Android application should provide [alternative resources](http://developer.android.com/guide/topics/resources/providing-resources.html#AlternativeResources) to support specific device configurations such as `portrait`, `landscape`, `small`, `large`, `us`, `fr`, ... Because we don't want to edit ~~thousands of~~ several images every time we need to change a single pixel, a color, a shadow size or a text value, this  plugin generates for you **density specific** bitmap drawable resources from qualified SVG files **at build time**.  

The only thing you have to do is to provide one or more qualified SVG files that will be converted for you at build time into as many as needed bitmaps and organized into configuration-specific drawable directories... at least one for each targeted screen density !  

```xml
<plugin>
    <groupId>fr.avianey.mojo</groupId>
    <artifactId>androidsvgdrawable-plugin</artifactId>
    <version>1.1.1</version>
</plugin>
```

    mvn gendrawable:gen

This plugin supports any configuration `<qualifier>` available on the Android platform and can generate [NinePatch](http://developer.android.com/reference/android/graphics/NinePatch.html) drawable as well. To increase your productivity, a mask functionnality allow you to define generic layers, filters, clip-path, etc...  and to reuse them accross multiple SVG files.  

Enjoy :-) !

-   [Input SVG files](#input-svg-files)
   -   [Expected SVG file names](#expected-file-names)
   -   [SVG Bounding Box](#bounding-box)
   -   [Generated bitmaps](#generated-bitmaps)
-   [Nine-Patch support](#nine-patch-support)
-   [SVG Masking](#svg-masking)
   -   [MASK file format](#mask-file-format)
   -   [Generated MASKED files](#generated-mask-files)
-   [How to use the plugin](#how-to-use-the-plugin)
	-   [POM configuration](#pom-configuration)
	-   [Plugin options](#plugin-options)
-   [Sample](#sample)
-   [License](#license)

The key words "MUST", "MUST NOT", "REQUIRED", "SHALL", "SHALL NOT", "SHOULD", "SHOULD NOT", "RECOMMENDED",  "MAY", and "OPTIONAL" in this document are to be interpreted as described in [RFC 2119](https://www.ietf.org/rfc/rfc2119.txt).

## Input SVG files 

This plugin is based on [Apache Batik 1.7](http://xmlgraphics.apache.org/batik/) and thus support most of the [SVG 1.1 Specification](http://www.w3.org/TR/SVG11/). See the [Batik status page](http://xmlgraphics.apache.org/batik/status.html) for the list of supported, not-supported and partially supported SVG 1.1 features.

#### Expected file names

Input SVG file name must match the following pattern : `\w+(-{qualifier})+.svg` where :

-   `\w+` is the SVG *unqualified* name part
-   `(-{qualifier})+` is the SVG *qualified* name part

Each SVG file **MUST** provide a valid reference density qualifier in its name. The generated bitmap (JPG or PNG) will be scaled relatively to its reference density. The generated bitmap for the reference density will have the same dimensions as defined by the SVG bounding box.  

Example of valid SVG file name :

-   `icon-mdpi.svg`
-   `flag-fr-land-mdpi.svg`
-   `more_complex_name-land-mdpi-fr-sw700dp.svg`  

Qualifiers can appear in any order within the SVG file name. The plugin takes care for you of re-ordering the qualifiers properly as expected by the Android plateform. For a full list of supported qualifiers, see [Providing Alternative Resources](http://developer.android.com/guide/topics/resources/providing-resources.html#AlternativeResources) on the Android developper website.

Example of invalid SVG file name :

-  `icon.svg`
	-  no density qualifier
-  `flag-fr.svg`
	-  no density qualifier
-  `invalid-classifier-mdpi.svg`
	-  `classifier` is not a valid Android resource qualifier
-  `éléphant-mdpi.svg`
	-  invalide `\w` character

#### Bounding Box

As explained above, each input SVG file name **MUST** provide a density qualifier. This qualifier is used to compute the scaled `width` and `height` of the generated bitmaps. The reference `width` and `height` for the reference density are those defined on the `width` and `height` attributes of the `<svg>` root element of the input SVG file :

```xml
<svg
   x="0"
   y="0"
   width="96"
   height="96"
```
	  
This will define the bounding box of the drawable content. Everything that is drawn outside off this bounding box will not be rendered in the generated bitmaps. `x` and `y` attributes are default to `0px` if not present. 

Inkscape provides a way to make the SVG bounding box described above match the content edges :  

1. Open File > Document Properties `[CTRL]`+`[SHIFT]`+`[D]`
2. In the Page tab Page Size > Custom Size > Resize page to content

If you want the bounding box to be larger than the content (with extra border), you'll need to add an extra transparent shape that is larger than the content and that match the desired width and height before using this method.  
  
You **SHOULD** adjust your input SVG file `width` and `height` to be a multiple of `32` and set its reference density to `mdpi` so they can be scaled to any density without rounding the bounding box. The `width` and `height` attributes of the `<svg>` element are rouded (when necessary) to the smallest integer that is greater than or equal to the value of the scaled attribute. You **SHOULD** use round integer value expressed in pixels `px` or without unit of length as much as possible...   

It's also possible to use valid SVG unit of length such as `mm`, `cm`, `pt`, `in`. Use it with caution :-).  

#### Generated bitmaps

The generated bitmaps are named against `\w+.png`, `\w+.jpg` or `\w+.9.png` if it's a nine-patch drawable and are generated into a `/res/drawable(-{qualifier})*` directory where :  

-   `\w+` is the unqualified part of the input SVG file
-   `(-{qualifier})*` is the re-ordered qualified part of the input SVG file
   -   Except for the density qualifier

#### Nine-Patch support

If you want to generate bitmaps as NinePatch Drawable, you **MUST** provide the **stretchable area** and **padding box** definitions as defined in the Android documentation related to [nine-patch](http://developer.android.com/guide/topics/graphics/2d-graphics.html#nine-patch). The Nine-Patch configuration file consists in a JSON Array containing at least on entry :

```javascript
[
    {
        "name" : "phone.*",  // \w+ part of the SVG file name (JAVA regexp)
        "qualifiers" : [ // optionnal array of qualifiers to filter SVG input 
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
	... // other nine-patch config
]
```
If no segment is provided along an edge, the whole edge will be filled.  

If you have different SVG with the same name but with different qualifiers, you can provide a specific Nine-Patch configuration by using an array of qualifiers. A Nine-Patch configuration apply only to input SVG files which qualified name part match **ALL** of the Nine-Patch qualifiers.

## SVG Masking

SVGMASK takes advantage of the SVG `<image>` element and allow you to define generic layers, masks and filters as well as Nine-Patch configuration for the generated bitmaps.

#### MASK file format

SVGMASK files are particular SVG files named against the same rule as [input SVG files](#input-svg-files) except for the `.svgmask` extension. SVGMASK files **MUST** contain at least one *capturing* `<image>` element with a `xlink:href` attribute which value matches against `#\{(.*)\}` and where the captured part of the attribute value is a valid [JAVA Pattern](http://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html) :   

-   `<image x="0" y="0" width="10" height="10" xlink:href="#{btn_.*}"/>`
-   `<image x="0" y="0" width="10" height="10" xlink:href="#{[a-z]{2}}"/>`

SVGMASK files **MAY** contains more than one *capturing* `<image>` element as well as standard SVG `<image>` elements.  

Each *capturing* `<image>` element a list of matching
An input SVG file can be captured by a *capturing* `<image>` element of a SVGMASK. Captured input SVG files **MUST** verify the two following conditions :  

-   The unqualified name of the input SVG file match the capturing regexp of the *capturing* `<image>` element
-   The qualified name of the input SVG file contains all of the qualifiers values defined in the SVGMASK qualified name
   -   The density qualifier of the SVGMASK file is not taken into account 

#### Generated masked files

SVGMASK files are not directly converted into bitmaps. SVGMASK files are converted into temporary SVG files that are added to the list of [input SVG files](#input-svg-files) to convert. Temporary SVG files are copies of the SVGMASK files DOM in which the `xlink:href` attribute value of each *capturing* `<image>` is replaced by a `file:///` URI linking to an input SVG file.

A carthesian product is made between each set of captured input SVG file for each *capturing* `<image>` element of the SVGMASK file. A temporary SVG file is created for each `xlink:href` attribute combination in the resulting product set except for combinations that contains input SVG files with incompatible qualifiers. Incompatible SVG files are input SVG files that defines different values for a same configuration qualifier type.

Combination that use the same URI for two *capturing* `<image>` element can be skipped or not depending on the value of the `useSameSvgOnlyOnceInMask` maven parameter.

For each kept product set entry the generated temporary SVG file name is the concatenation of :

1.  The SVGMASK unqualified name
2.  The input SVG file unqualified name for each *capturing* `<image>` element `xlink:href` URI
	-   in the order of the *capturing* `<image>` element in the SVG file DOM
3.  The union of the qualifier values for the SVGMASK and all of the linked input SVG files
	-   The density qualifier of the SVGMASK is used
	-   The SVGMASK bounding box is used as reference
	-   Linked input SVG files are scalled according to the `<image>`element specifications

Resulting bitmaps are generated from temporary SVG files :

-   see [Generated bitmaps](#generated-bitmaps)

Nine-Patch configuration file elements **MAY** reference temporary SVG files : 

-   see [Nine-Patch support](nine-patch-support)

## How to use the plugin

#### POM configuration

To use the plugin in your Android Maven projects you need to add the following plugin dependency to your project `pom.xml` :

```xml
<plugins>
	...
    <plugin>
        <groupId>fr.avianey.mojo</groupId>
        <artifactId>androidsvgdrawable-plugin</artifactId>
        <configuration>
            <!-- where to pick the svg -->
            <from>${project.basedir}/svg</from>
            <!-- where to generate them -->
            <to>${project.basedir}/res</to>
            <!-- use key/value pairs to rename resources (key => value) -->
            <rename>
                <phone_to_rename>phone</phone_to_rename>
            </rename>
            <!-- skip already generated resources (always|never|ifModified) -->
            <override>ifModified</override>
            <!-- create or skip missing directories -->
            <createMissingDirectories>true</createMissingDirectories>
            <targetedDensities>
                <!-- let skip ldpi & tvdpi here -->
                <densitys>hdpi</densitys>
                <densitys>xhdpi</densitys>
                <densitys>xxhdpi</densitys>
                <densitys>xxxhdpi</densitys>
            </targetedDensities>
            <!-- use no qualifier for mdpi drawables -->
            <fallbackDensity>mdpi</fallbackDensity>
            <!-- use the 'icon' svg to generate the High Res Icon for Google Play -->
            <highResIcon>icon</highResIcon>
            <!-- NinePatch config file -->
            <ninePatchConfig>${project.basedir}/svg/9patch.json</ninePatchConfig>
        </configuration>
		<!-- generate bitmap before the generate-sources goal -->
        <executions>
            <execution>
                <phase>initialize</phase>
                <goals>
                    <goal>gen</goal>
                </goals>
            </execution>
        </executions>
    </plugin>
	...
</plugins>
```

#### Plugin options

The plugin can be configured using the following options : 

###### from (since 1.0.0) :

Path to the directory that contains the SVG files to generate drawable from.  
SVG files **MUST** be named according the following rules and **MUST** contain a valid density qualifier (ldpi, mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi, tvdpi) :

-   `\w+(-{qualifier})+.svg`  

###### to (since 1.0.0) :

Path to the Android `res/` directory that contains the various `drawable/` directories.

###### createMissingDirectories (since 1.0.0) :

Set it to "false" if you don't want the plugin to create missing drawable(-{qualifier})*/ directories.  
The default value is set to `true`.

###### ninePatchConfig (since 1.0.0) :

Path to the 9-Patch JSON configuration file.

###### override (since 1.0.0) :

Whether or not already existing and up to date PNG should be override at build time :

-   `always` : bitmaps are always recreated
-   `never` : bitmaps are never recreated if a bitmaps with the same file name already exists
-   `ifModified` : bitmaps are recreated only if the SVG file is more recent than the existing bitmaps file  

Default value is set to `always`.

###### rename (since 1.0.0) :

Use this map to change the name of the generated drawable.  
Note that names provided in the 9-patch configuration file applies to the `\w+` part of the SVG file name **BEFORE** any renaming.

###### targetedDensities (since 1.0.0) :

List of the desired densities for the generated drawable.  
If not specified, a drawable is generate for each supported density qualifier.  

-   ldpi
-   mdpi
-   hdpi
-   xhdpi
-   xxhdpi
-   xxxhdpi
-   tvdpi

###### fallbackDensity (since 1.0.0) :

The density for unqualified drawable directories.  If set to `mdpi`, mdpi bitmaps will be generated into `/drawable` and not `/drawable-mdpi`.  
Default value is `mdpi`.

###### highResIcon (since 1.0.0) :

The name of the SVG resource to use to generate an **High-Res** icon for the Google Play.  
The SVG **SHOULD** have a square Bounding Box (height = width) and will be generated into `${project.basedir}`.

###### outputFormat (since 1.1.0) :

The format of the generated bitmaps :  

-   PNG (default)
-   JPG (no alpha)

The outputFormat is specified per Maven `<execution><configuration>`. I fyou want to generate PNG and JPG for the same Android application, you should use two `<execution>` elements for the plugin.

###### jpgQuality (since 1.1.0) :

The quality use for the JPG compression between 0 and 100 (higher is better).  
Default value is `85` (like Gimp).

###### jpgBackgroundColor (since 1.1.0) :

The background color used for the generated JPG bitmaps.  
Default is `0xFFFFFFFF` (opaque white).

As of Maven 3.0.3 you should be able to set an hexa value like `0xFFFF0000` (red).

###### svgMaskDirectory (since 1.1.0) :

An optionnal directory to pick the SVGMASK files from.  
Default to the same directory as the `from` parameter.

###### svgMaskResourcesDirectory (since 1.1.0) :

An optionnal directory to pick the SVG files to mask from.  
Default to the same directory as the `svgMaskDirectory` parameter.

###### useSameSvgOnlyOnceInMask (since 1.1.0) :

Tell the plugin to skip SVGMASK combinations that use the same SVG resource more than once.  
Default is `true`.

## Sample

The sample application use this plugin to generate all of its drawables and illustrates the use of every single option of the plugin. Simply run :  

    mvn clean install

#### Generates density specific PNG from one SVG file :

###### mdpi :
![mdpi generated flag][flag-mdpi]
###### hdpi :
![hdpi generated flag][flag-hdpi]
###### xhdpi :
![xhdpi generated flag][flag-xhdpi]
###### xxhdpi :
![xxhdpi generated flag][flag-xxhdpi]

#### Generates PNG and qualified directories for you :
###### input SVG files :
![SVG input files][svg-files]
###### generated directories :
![qualified Android resource directories][generated]
###### generated PNG in **drawable-fr-land-xxxhdpi** :
![generated drawable for fr-land][drawable-fr-land-xxxhdpi]
###### generated PNG in **drawable-land-xxxhdpi** :
![generated drawable for land][drawable-land-xxxhdpi]

[generated]: http://avianey.github.io/androidsvgdrawable-plugin/generated.png "Generated resource directories"
[drawable-fr-land-xxxhdpi]: http://avianey.github.io/androidsvgdrawable-plugin/drawable-fr-land-xxxhdpi.png "Landscape xxxhdpi french flag"
[drawable-land-xxxhdpi]: http://avianey.github.io/androidsvgdrawable-plugin/drawable-land-xxxhdpi.png "Landscape xxxhdpi USA flag"
[flag-hdpi]: http://avianey.github.io/androidsvgdrawable-plugin/flag-hdpi.png "Generated hdpi flag"
[flag-mdpi]: http://avianey.github.io/androidsvgdrawable-plugin/flag-mdpi.png "Generated mdpi flag"
[flag-xhdpi]: http://avianey.github.io/androidsvgdrawable-plugin/flag-xhdpi.png "Generated xhdpi flag"
[flag-xxhdpi]: http://avianey.github.io/androidsvgdrawable-plugin/flag-xxhdpi.png "Generated xxhdpi flags"
[svg-files]: http://avianey.github.io/androidsvgdrawable-plugin/svg-files.png "Input SVG files"

## License

```
Copyright 2013, 2014 Antoine Vianey  

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
