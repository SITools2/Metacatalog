1/ Description
______________

Identification                       : SITools2/Metacatalogue
Date                                 : 2013-12-10
Version                              : 0.7
Developper                           : AKKA Technologies
Type                                 : Prototype
Repository url                       : https://github.com/SITools2/Metacatalog
Project page                         : 
Classification                       : 
Characteristics                      : 
Role/Function                        : 
Reference tag                        : 

2/ Changes

__________
- 0.7 (10/12/2013)

	- New feature :
		-> Suggestion service using Thesaurus entries
		-> Search service 
			- uses Thesaurus entries validation
			- returns facets
		-> New validation process
			- mandatory fields
			- thesaurus fields
		-> Extract location from footprint
		-> Extract resolution domain concept from resolution field
	
	- Enhancement :
		-> Update iso19139 fields extraction
		-> Update GeoJSON fields extraction
		
		
__________
- 0.6 (06/11/2013)
		
	- Enhancement :
		-> Update Solr Version to 4.5
			- Can perform geographic queries natively
		-> Testability improvement

__________
- 0.5 (08/02/2013)
	- New feature :
		-> IzPack installer for easy install process
		-> Client-User module for SITools2 for metacatalogue administration
		-> Healpix geographic indexation
		-> New security policy for proxyfied services
		-> Harvesting process launch asynchronously for easy progress status

		
	- Enhancement :
		-> Solr scripts have been improved
		-> GeoSud catalog CSW harvesting improvement
		
___________
- 0.4 (08/01/2013)
	- New feature :
		-> New internal model, the names of all fields have been changed
		-> WMS and Download service are proxyfied via a SITools2 proxy resource
		-> New security policy for services
		-> The url in an opensearch harvesting configuration must be a template (like in the opensearch.xml file)
		
	- Enhancement :
		-> CSW harvesting compatible with geosud catalog
		-> Paging on opensearch harvesting have been improved
		
___________
- 0.3 (12/12/2012)
	- Bug fixed :
		-> Solr geographic insertion is dependent of the database table schema.
___________
- 0.2 (05/12/2012)
	- New Features :
		-> Opensearch dataset description service
		-> Opensearch search service
		-> Public / private services managment 
		
	- Enhancement :
		-> Better geographic data managment in the database

	-Bug fixed : 
		-> The number of records found doesn't match the number of records in the JSON file
___________
- 0.1 (20/11/2012)
	- First version of the metacatalogue
	