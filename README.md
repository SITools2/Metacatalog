 ![](workspace/metacatalogue-install-izpack/res/img/sitools_metacatalog.png) ![](https://github.com/SITools2/core-v2/raw/master/workspace/client-public/res/images/logo_01_petiteTaille.png)
# SITools2 Metacatalog
## Description
The SITools2 metacatalog is a web server used to harvest earth observation  catalogs (CSW and Opensearch) and to provide a fast search engine on all the data harvested. 

It is based on top of the SITools2 framework

SITools2 github page : [http://sourceforge.net/projects/sitools2/](http://sourceforge.net/projects/sitools2/ "SITools2 Web Site")

For more information on SITools2 : [https://github.com/SITools2/core-v2](https://github.com/SITools2/core-v2 "SITools2 Github Page")

Release notes : [README.txt](workspace/fr.cnes.sitools.metacatalogue.core/README.txt)

## Build the SITools2 Metacatalog

The metacatalog is composed of 2 parts:

- A standalone REST server which is used to harvest and index catalogs
- A SITools2 extension, which is a set of extensions to the SITools2 server

### Build SITools2

#### Build from sources
Follow the instruction on the page [https://github.com/SITools2/core-v2](https://github.com/SITools2/core-v2 "SITools2 Github Page") to build SITools2

#### Build from Installer
Download the [Sitools2 installer](https://sourceforge.net/projects/sitools2/files/latest/download "SITools2 Installer")

Execute the installer

	$ java -jar SITools2-<version>-install.jar

And follow the instructions

### Getting the sources

	$ git clone https://github.com/SITools2/Metacatalog.git metacatalog

### Build the metacatalog server

#### Pre-build configuration

	$ cd metacatalog/workspace/fr.cnes.sitools.metacatalogue.core/

Edit `build.properties` and change the value of the `HOST` property (for example HOST = new-dev)

Make a copy of `conf/build/properties/build-example.properties` to `conf/build/properties/build-new-dev.properties`

	$ cp conf/build/properties/build-example.properties conf/build/properties/build-new-dev.properties


Edit the newly created file and set the `ROOT_DIRECTORY` property to the `metacatalogue` folder

#### Build the sources

Build the sources using ant

	$ ant

### Build the SITools2 extensions

	$ cd ../../workspace/fr.cnes.sitools.ext.metacatalogue/

Edit `build.properties` and set the `ROOT_DIRECTORY` property to the SITools2 path (`sitools2-v2`) and the `SITOOLS_METACATALOGUE_CORE_DIR` to the `metacatalog/workspace/fr.cnes.sitools.metacatalogue.core` folder

#### Build the sources

Build the sources using ant

	$ ant

#### Copy the generated jar

Copy the generated Jar to the extension folder of SITools2

	$ cp dist/fr.cnes.sitools.ext.metacatalogue.jar ../../../sitools2-v2/workspace/fr.cnes.sitools.core/ext

## Building the installer from the sources
### Build jars

Build the server and extensions jars as explained above

### Build the installer

	$ cd metacatalog/workspace/metacatalogue-install-izpack

Edit `build.properties` and set the `ROOT_DIRECTORY` property to the SITools2 path (`sitools2-v2`) and the `SITOOLS_METACATALOGUE_CORE_DIR` to the `metacatalog` folder

Build the installer using ant

	$ ant

## Installing from the installer
	$ java -jar SITools2-Metacatalogue-<version>-install.jar

## Start SITools2 metacatalog

### Start the metacatalog server

	$ sh ./metacatalog/workspace/fr.cnes.sitools.metacatalogue.core/metacatalogue.sh start

### Start the SITools2 server
	
	$ sh ../sitools2-v2/sitools.sh start
or 

	$ ../sitools2-v2/workspace/fr.cnes.sitools.core/sitools start
if `source` is not installed on your computer.

### Start the SolR server

	$ cd metacatalog/Solr

	$ sh ./solr start

Wait for a few seconds and stop the SolR server using ctlr+c
Copy all jar from the `metacatalog_specific_libs` folder to the lib SolR lib folder

	$ cp metacatalog_specific_libs/* solr-webapp/webapp/WEB-INF/lib/

![](https://github.com/SITools2/core-v2/raw/master/workspace/client-public/res/images/logo_01_petiteTaille.png)