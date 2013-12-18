Android Gen Drawable Maven plugin
=================================

Every application should provide [alternative resources](http://developer.android.com/guide/topics/resources/providing-resources.html#AlternativeResources) to support specific device configurations. This Maven plugin handle it for you and generates density specific drawable resources from SVG files. The only thing you have to do is to provide one or more qualified SVG files that will be converted for you at build time in many PNGs... at least one for each targeted device density !  

This plugin supports any configuration qualifier available on the Android platform and can generate [NinePatch](http://developer.android.com/reference/android/graphics/NinePatch.html) drawable as well :-)

-   [Sample](#sample)
-   [Expected SVG file names](#expected-file-names)
-   [SVG Bounding Box](#bounding-box)
-   [9-Patch configuration](#9-patch-configuration)
-   [Pom.xml configuration](#pomxml-configuration)
-   [Plugin options](#plugin-options)
-   [License](#license)

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

## Input SVG files 

#### Expected file names

SVG file name must match the following pattern : **\w+(-{qualifier})+.svg**  
-   the generated PNG will be name \w+.png or \w+.9.png if it's a 9-patch drawable
-   the generated PNG will be saved into the /res/drawable(-{qualifier}) directory  

You **MUST** provide a reference density qualifier in your SVG file name. The generated PNG will be scaled relatively to this reference density. The generated PNG for the reference density will have the same dimension as mentionned in the SVG bounding box.  

Valid SVG file names :
-   icon-mdpi.svg
-   flag-fr-land-mdpi.svg
-   more_complex_name-land-mdpi-fr-sw700dp.svg  

Qualifiers can appear in any order in the SVG file name. The plugin will re-order the qualifiers properly.

#### Bounding Box

SVG files use to generate density specific drawable must specify a width and height like this :

```xml
<svg
   x="0px"
   y="0px"
   width="96"
   height="96"
```
	  
This will define the bounding box of the drawable content. Everything that is drawn outside off this bounding box will not be rendered in the generated PNG drawable.  
Inkscape provides a way to make the SVG bounding bow match the content edges.  

1. Open File > Document Properties [CTRL]+[SHIFT]+[D]
2. In the Page tab Page Size > Custom Size > Resize page to content

If you want the bounding box to be larger than the content (with extra border), you'll need to add an extra transparent shape that is larger than the content.  
  
It is preferable for your SVG file dimensions to be a multiple of **32** and adjusted to mdpi so they can be scaled to any density without rounding the bounding box.

#### 9-Patch configuration

If you want a SVG file to be generated as a NinePatch PNG, you must provide a NinePatch configuration file that will provide **stretch** and **content** areas configuration for your SVG file. The configuration file is a JSON Array containing one entry for each SVG to convert into NinePatch drawable :

```javascript
[
    {
        "name" : "phone",  // \w+ part of the SVG file name
        "qualifiers" : [ // optionnal array of qualifiers to filter SVG input 
			"land" // this config only apply to \w+.*-land.*.svg files
		], 
        "stretch" : { // the stretch area configuration
            "x" : [ // segments of the top edge of the NinePatch
                [3, 43],
				... // you can add as many segments as you want
            ],
            "y" : [ // segments of the left edge of the NinePatch
                [3, 29]
				... // you can add as many segments as you want
            ]
        },
		"content" : {
            "x" : [ // segments of the bottom edge of the NinePatch
                [3, 43]
				... // you can add as many segments as you want
            ],
            "y" : [ // segments of the right edge of the NinePatch
                [3, 29]
				... // you can add as many segments as you want
            ]
		}
    },
	... // other files
]
```
If no segment is provided along an edge, the whole edge will be filled.  
As shown in the sample maven project, if you have different SVG with the same name but with different qualifiers, you can provide specific 9-Patch configuration by providing a "qualifiers" array. The 9-Patch configuration will only apply to SVG which file name match the provided "qualifiers" array.

## How to use the plugin

#### pom.xml configuration

To use it in your Android Maven projects you need to add the following plugin dependency to your project pom.xml :

```xml
<plugins>
	...
    <plugin>
        <groupId>fr.avianey.mojo</groupId>
        <artifactId>androidgendrawable-maven-plugin</artifactId>
        <version>1.0.0</version>
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
            <!-- use the app icon to generate the High Res Icon for Google Play -->
            <highResIcon>icon</highResIcon>
            <!-- NinePatch config file -->
            <ninePatchConfig>${project.basedir}/svg/9patch.json</ninePatchConfig>
        </configuration>
		<!-- generate PNG before generate-sources -->
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

###### from :

Path to the directory that contains the SVG files to generate drawable from.  
SVG files **MUST** be named according the following rules and **MUST** contain a density qualifier (mdpi,hdpi,...) :

- **\w+(-{qualifier})+.svg**  

Generated drawable will be named :

- **\w+.png**  

The density qualifier provided in the SVG file name indicates that the Bounding Box size defined in the **<svg>** tag of the SVG file is the target size of the generated drawable for this density. Generated drawable for other densities are scaled according the **3:4:6:8:12:16** scaling ratio defined in the [Supporting Multiple Screens section](http://developer.android.com/guide/practices/screens_support.html) of the Android developers site.   

###### to :

Path to the Android res/ directory that contains the various drawable/ directories.

###### createMissingDirectories :

Set it to "false" if you don't want the plugin to create missing drawable(-{qualifier})*/ directories.  
The default value is set to "true".

###### ninePatchConfig :

Path to the 9-Patch JSON configuration file.

###### override :

Whether or not already existing and up to date PNG should be override at build time :
-   **always** : PNG are always recreated
-   **never** : PNG are never recreated if a PNG with the same file name already exists
-   **ifModified** : PNG are recreated only if the SVG file is more recent than the existing PNG file
Default value is set to **always**.

###### rename :

Use this map to change the name of the generated drawable.  
Note that names provided in the 9-patch configuration file applies to the **\w+** part of the SVG file name **BEFORE** any renaming.

###### targetedDensities :

List of the desired densities for the generated drawable.  
If not specified, a drawable is generate for each supported density qualifier.  
-   ldpi
-   mdpi
-   hdpi
-   xhdpi
-   xxhdpi
-   xxxhdpi
-   tvdpi

###### fallbackDensity :

The density for unqualified drawable directories.  If set to "mdpi", mdpi PNG will be generated into /drawable and not /drawable-mdpi.  
default value is **mdpi**.

###### highResIcon :

The name of the SVG resource to use to generate an **High-Res** icon for the Google Play.  
The SVG **SHOULD** have a square Bounding Box (height = width) and will be generated into ${project.basedir}.

## License

```
Copyright 2013 Antoine Vianey  

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
