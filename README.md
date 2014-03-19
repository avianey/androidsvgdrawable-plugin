Android Gen Drawable Maven plugin
=================================

[![Build Status](https://travis-ci.org/avianey/androidgendrawable-maven-plugin.png?branch=master)](https://travis-ci.org/avianey/androidgendrawable-maven-plugin)  

Every Android application should provide [alternative resources](http://developer.android.com/guide/topics/resources/providing-resources.html#AlternativeResources) to support specific device configurations such as portrait, landscape, small, large, us, fr, ... Because we don't want to edit ~~thousands of~~ several images every time we need to change a single pixel, a color or a text value, this Maven plugin generates for you density specific drawable resources from qualified SVG files. The only thing you have to do is to provide one or more qualified SVG files that will be converted for you at build time in as many bitmaps as needed and correctly placed into qualified drawable directories...at least one for each targeted screen density !  

```xml
<plugin>
    <groupId>fr.avianey.mojo</groupId>
    <artifactId>androidgendrawable-maven-plugin</artifactId>
    <version>1.0.1</version>
</plugin>
```

This plugin supports any configuration qualifier available on the Android platform and can generate [NinePatch](http://developer.android.com/reference/android/graphics/NinePatch.html) drawable as well :-)

-   [Input SVG files](#input-svg-files)
   -   [Expected SVG file names](#expected-file-names)
   -   [SVG Bounding Box](#bounding-box)
-   [Nine-Patch support](#nine-patch-support)
-   [SVG Masking](#svg-masking)
-   [How to use the plugin](#how-to-use-the-plugin)
	-   [POM configuration](#pom-configuration)
	-   [Plugin options](#plugin-options)
-   [Sample](#sample)
-   [License](#license)

## Input SVG files 

#### Expected file names

SVG file name must match the following pattern : `\w+(-{qualifier})+.svg`  

-   the generated images are named against `\w+.png`, `\w+.jpg` or `\w+.9.png` if it's a nine-patch drawable
-   files are generated into a `/res/drawable(-{qualifier})*` directory  

Each SVG file **MUST** provide a reference density qualifier in its name. The generated images (JPG or PNG) will be scaled relatively to this reference density. The generated files for the reference density will have the dimensions defined by the SVG bounding box.  

Example of valid SVG file name :

-   icon-mdpi.svg
-   flag-fr-land-mdpi.svg
-   more_complex_name-land-mdpi-fr-sw700dp.svg  

Qualifiers can appear in any order within the SVG file name. The plugin takes care of re-ordering the qualifiers properlyas expected by the Android plateform. For a full list of supported qualifiers, see [Providing Alternative Resources](http://developer.android.com/guide/topics/resources/providing-resources.html#AlternativeResources) on the Android developper website.

Example of invalid SVG file name :

-  icon.svg
	-  no density qualifier
-  flag-fr.svg
	-  no density qualifier
-  invalid-classifier-mdpi.svg
	-  `classifier` is not a valid Android resource qualifier
-  &eacute;l&eacute;phant-mdpi.svg
	-  invalide `\w` character

#### Bounding Box

As explained above, each SVG file name **MUST** provide a density qualifier. This qualifier is used to compute the scaled `width` and `height` for the generated images. The reference `width` and `height` for the density declared in the SVG file name are those defined on the `<svg>` root element of the SVG file :

```xml
<svg
   x="0px"
   y="0px"
   width="96"
   height="96"
```
	  
This will define the bounding box of the drawable content. Everything that is drawn outside off this bounding box will not be rendered in the generated bitmap drawable. `x` and `y` attributes are default to 0px if not present. 

Inkscape provides a way to make the SVG bounding box match the content edges :  

1. Open File > Document Properties `[CTRL]`+`[SHIFT]`+`[D]`
2. In the Page tab Page Size > Custom Size > Resize page to content

If you want the bounding box to be larger than the content (with extra border), you'll need to add an extra transparent shape that is larger than the content and that match the desired width and height before using this method.  
  
It is preferable for your SVG file dimensions to be a multiple of **32** and adjusted to **mdpi** so they can be scaled to any density without rounding the bounding box. The **"width"** and **"height"** attributes of the <svg> element are rouded to the smallest integer that is greater than or equal to the value of the attribute. **Use round integer value expressed in pixels ("px") or without unit of length as much as possible...**   

It's also possible to use valid SVG unit of length such as `mm`, `cm`, `pt`, `in`. Use it with caution :-).  

#### Nine-Patch support

If you want to generate a NinePatch Drawable, you must provide the **stretchable area** and **padding box** definitions as defined in the Android documentation related to [nine-patch](http://developer.android.com/guide/topics/graphics/2d-graphics.html#nine-patch). The configuration file is a JSON Array containing one entry for each SVG to convert into NinePatch drawable :

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

If you have different SVG with the same name but with different qualifiers, you can provide a specific 9-Patch configuration by using an array of qualifiers. The 9-Patch configuration will only apply to SVG which file name match **ALL** the provided qualifiers.

## SVG Masking

#### SVGMASK file format

SVGMASK files are particular SVG files named against the same rule as [input SVG files](#input-svg-files) except for the `.svgmask` extension and containing at least one *capturing* `<image>` element with a `xlink:href` which value matches `#\{(.*)\}` and where the captured part of the attribute value is a valid JAVA Pattern :   

-   `<image x="0" y="0" width="10" height="10" xlink:href="#{btn_.*}"/>`
-   `<image x="0" y="0" width="10" height="10" xlink:href="#{[a-z]{2}}"/>`

Standard `<image>` elements can still be use with reference to toher DOM element or other files :

-   `<image x="0" y="0" width="10" height="10" xlink:href="/path/to/other.svg"/>`
-   `<image x="0" y="0" width="10" height="10" xlink:href="#svgID1234"/>`

For each *capturing* `<image>` element a list of matching input SVG file is established. Matching input SVG files **MUST** verify the two following conditions :  

-   The unqualified name match the capturing regexp
-   Contains all of the SVGMASK qualifiers except the density qualifier 

#### Generated masked SVG

SVGMASK are not directly converted into bitmaps. SVGMASK files are converted into temporary SVG files that are added to the list of [input SVG files](#input-svg-files) to convert. Temporary SVG files is a copy of the SVGMASK file in which the `xlink:href` attribute values of each *capturing* `<image>` element has been replace by a `file:///` link to an input SVG file.

For each *capturing* `<image>` element inside the SVGMASK file, exactly one temporary SVG file is generated for each matching SVG input file. If a SVGMASK file contains more than one *capturing* `<image>` element, then a carthesian product is made between each set of matching SVG input file.  In the resulting product set, combination of input SVG files with incompatible qualifiers are skipped.  Two input SVG files are incompatible if they define a different value for the same qualifier type (except the density one).

The `useSameSvgOnlyOnceInMask` can be use to control if an input SVG file can be use more than once in the same combination of the resulting product set.

The generated temporary SVG file for each product set entry is the concatenation of :

-   The SVGMASK unqualified name
-   The input SVG file for each used input
	-   in the order of the `<image>` element that used the SVG file
-   The union of the qualifiers for the SVGMASK and the input SVG files
	-   The density qualifier of the SVGMASK is used
	-   The SVGMASK bounding box is used as reference

#### Generated bitmaps

Generated bitmaps are then created using the same rules as standard input SVG file.

#### SVGMASK and Nine-Patch 

Nine-Patch configuration is compatible with SVGMASK.

## How to use the plugin

#### POM configuration

To use the plugin in your Android Maven projects you need to add the following plugin dependency to your project `pom.xml` :

```xml
<plugins>
	...
    <plugin>
        <groupId>fr.avianey.mojo</groupId>
        <artifactId>androidgendrawable-maven-plugin</artifactId>
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
SVG files **MUST** be named according the following rules and **MUST** contain a density qualifier (mdpi,hdpi,...) :

-   `\w+(-{qualifier})+.svg`  

Generated drawable will be named :

-   `\w+.png`

The density qualifier provided in the SVG file name indicates that the Bounding Box size defined in the `<svg>` tag of the SVG file is the target size of the generated drawable for this density. Generated drawable for other densities are scaled according the `3:4:6:8:12:16` scaling ratio defined in the [Supporting Multiple Screens section](http://developer.android.com/guide/practices/screens_support.html) of the Android developers site.   

###### to (since 1.0.0) :

Path to the Android `res/` directory that contains the various `drawable/` directories.

###### createMissingDirectories (since 1.0.0) :

Set it to "false" if you don't want the plugin to create missing drawable(-{qualifier})*/ directories.  
The default value is set to `true`.

###### ninePatchConfig (since 1.0.0) :

Path to the 9-Patch JSON configuration file.

###### override (since 1.0.0) :

Whether or not already existing and up to date PNG should be override at build time :

-   `always` : PNG are always recreated
-   `never` : PNG are never recreated if a PNG with the same file name already exists
-   `ifModified` : PNG are recreated only if the SVG file is more recent than the existing PNG file  

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

The sample application use this plugin to generate all of its drawables and illustrates the use of every single option of the plugin.  

    mvn gendrawable:gen

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

[generated]: http://avianey.github.io/androidgendrawable-maven-plugin/generated.png "Generated resource directories"
[drawable-fr-land-xxxhdpi]: http://avianey.github.io/androidgendrawable-maven-plugin/drawable-fr-land-xxxhdpi.png "Landscape xxxhdpi french flag"
[drawable-land-xxxhdpi]: http://avianey.github.io/androidgendrawable-maven-plugin/drawable-land-xxxhdpi.png "Landscape xxxhdpi USA flag"
[flag-hdpi]: http://avianey.github.io/androidgendrawable-maven-plugin/flag-hdpi.png "Generated hdpi flag"
[flag-mdpi]: http://avianey.github.io/androidgendrawable-maven-plugin/flag-mdpi.png "Generated mdpi flag"
[flag-xhdpi]: http://avianey.github.io/androidgendrawable-maven-plugin/flag-xhdpi.png "Generated xhdpi flag"
[flag-xxhdpi]: http://avianey.github.io/androidgendrawable-maven-plugin/flag-xxhdpi.png "Generated xxhdpi flags"
[svg-files]: http://avianey.github.io/androidgendrawable-maven-plugin/svg-files.png "Input SVG files"

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
