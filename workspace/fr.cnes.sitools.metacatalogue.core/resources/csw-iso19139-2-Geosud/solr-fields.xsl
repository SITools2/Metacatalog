<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gco="http://www.isotc211.org/2005/gco"
	xmlns:gml="http://www.opengis.net/gml" xmlns:srv="http://www.isotc211.org/2005/srv"
	xmlns:gmx="http://www.isotc211.org/2005/gmx" xmlns:gmi="http://www.isotc211.org/2005/gmi"
	xmlns:ns14="http://ptsc.fr/" version="2.0">


	<xsl:include href="./convert/functions.xsl" />


	<xsl:output method="xml" version="1.0" encoding="iso-8859-1"
		indent="yes" />


	<xsl:param name="inspire">
		true
	</xsl:param>


	<xsl:variable name="useDateAsTemporalExtent" select="false()" />



	<xsl:template match="/">
		<doc>
			<xsl:apply-templates select="gmi:MI_Metadata"
				mode="metadata" />
		</doc>
	</xsl:template>





	<xsl:template match="*" mode="metadata">


		<xsl:for-each select="gmd:fileIdentifier/gco:CharacterString">
			<field name="_uuid">urn:ogc:def:EOP:GEOSUD:Geosud:<xsl:value-of select="string(.)" /></field>
			<field name="id">urn:ogc:def:EOP:GEOSUD:Geosud:<xsl:value-of select="string(.)" /></field>
		</xsl:for-each>

		<xsl:for-each
			select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword/gco:CharacterString">
			<field name="keywords"><xsl:value-of select="string(.)" /></field>
		</xsl:for-each>

		<field name="resourceType">dataset</field>


		<xsl:for-each select="gmd:identificationInfo//gmd:MD_DataIdentification">

			<xsl:for-each select="gmd:abstract/gco:CharacterString">
				<field name="description"><xsl:value-of select="string(.)" /></field>
			</xsl:for-each>


			<xsl:for-each select="gmd:citation/gmd:CI_Citation">


				<xsl:for-each select="gmd:title/gco:CharacterString">
					<field name="title"><xsl:value-of select="string(.)" /></field>
				</xsl:for-each>


			</xsl:for-each>

			<xsl:for-each select="gmd:spatialResolution/gmd:MD_Resolution">
				<xsl:for-each select="gmd:distance/gco:Distance">
					<field name="acquisitionSetup.resolution"><xsl:value-of select="string(.)" /></field>
				</xsl:for-each>
			</xsl:for-each>



			<field name="quicklook"><xsl:value-of select="string(gmd:graphicOverview[1]/gmd:MD_BrowseGraphic/gmd:fileName/gco:CharacterString)" /></field>

			<field name="thumbnail"><xsl:value-of select="string(gmd:graphicOverview[2]/gmd:MD_BrowseGraphic/gmd:fileName/gco:CharacterString)" /></field>

			<xsl:for-each select="gmd:resourceFormat/gmd:MD_Format">
				
				<field name="distributionAccess.format"><xsl:value-of select="string(gmd:name/gco:CharacterString)" /></field>
				
				<field name="distributionAccess.version"><xsl:value-of select="string(gmd:version/gco:CharacterString)" /></field>

			</xsl:for-each>
			
			
			<xsl:for-each select="gmd:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod">
				
				<field name="characterisationAxis.temporalAxis.min"><xsl:value-of select="string(gml:beginPosition)" /></field>
				
				<field name="characterisationAxis.temporalAxis.max"><xsl:value-of select="string(gml:endPosition)" /></field>
				
			</xsl:for-each>

		</xsl:for-each>

		<!-- Services browse WMS -->
		<xsl:for-each
			select="gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/ns14:PTSC_WMSParameters">
			<xsl:for-each select="ns14:type">
				<field name="services.browse.layer.type"><xsl:value-of select="string(.)" /></field>
			</xsl:for-each>

			<xsl:for-each select="ns14:url">
				<field name="services.browse.layer.url"><xsl:value-of select="string(.)" /></field>
			</xsl:for-each>

			<xsl:for-each select="ns14:layers">
				<field name="services.browse.layer.layers"><xsl:value-of select="string(.)" /></field>
			</xsl:for-each>

			<xsl:for-each select="ns14:version">
				<field name="services.browse.layer.version"><xsl:value-of select="string(.)" /></field>
			</xsl:for-each>

			<xsl:for-each select="ns14:bbox">
				<field name="services.browse.layer.bbox"><xsl:value-of select="string(.)" /></field>
			</xsl:for-each>

			<xsl:for-each select="ns14:srs">
				<field name="services.browse.layer.srs"><xsl:value-of select="string(.)" /></field>
			</xsl:for-each>



		</xsl:for-each>

		<!-- Services download -->
		<xsl:for-each
			select="gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:linkage/gmd:URL">
			<field name="distributionAccess.url"><xsl:value-of select="string(.)" /></field>
		</xsl:for-each>


		<xsl:for-each
			select="gmd:dataQualityInfo/*/gmd:lineage/*/gmd:statement/gco:CharacterString">
			<field name="lineage"><xsl:value-of select="string(.)" /></field>
		</xsl:for-each>

		<xsl:for-each select="gmd:language/gco:CharacterString">
			<field name="language"><xsl:value-of select="string(.)" /></field>
		</xsl:for-each>


		<xsl:for-each select="gmd:dateStamp/gco:DateTime|gmd:dateStamp/gco:Date">
			<field name="modificationDate"><xsl:value-of select="string(.)" /></field>
		</xsl:for-each>


		<xsl:for-each select="gmd:contact/*">
			<xsl:for-each select="gmd:organisationName/gco:CharacterString">
				<field name="acquisitionSetup.facility.organisationName"><xsl:value-of select="string(.)" /></field>
			</xsl:for-each>

			<xsl:for-each
				select="gmd:contactInfo/gmd:CI_CONTACT/gmd:address/gmd:CI_ADDRESS/gmd:electronicMailAddress/gco:CharacterString">
				<field name="acquisitionSetup.facility.emailAddress"><xsl:value-of select="string(.)" /></field>
			</xsl:for-each>

			<xsl:for-each select="gmd:role/gmd:CI_RoleCode">
				<field name="acquisitionSetup.facility.role"><xsl:value-of select="string(.)" /></field>
			</xsl:for-each>
		</xsl:for-each>

		<xsl:for-each
			select="gmi:acquisitionInformation/gmi:MI_AcquisitionInformation/gmi:instrument/gmi:MI_Instrument/gmi:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString">
			<field name="acquisitionSetup.instrument"><xsl:value-of select="string(.)" /></field>
		</xsl:for-each>

		<xsl:for-each
			select="gmi:acquisitionInformation/gmi:MI_AcquisitionInformation/gmi:platform/gmi:MI_Platform/gmi:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString">
			<field name="acquisitionSetup.platform"><xsl:value-of select="string(.)" /></field>
		</xsl:for-each>

		<field name="acquisitionSetup"><xsl:value-of select="string(gmi:acquisitionInformation/gmi:MI_AcquisitionInformation/gmi:platform/gmi:MI_Platform/gmi:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString)" />/<xsl:value-of select="gmi:acquisitionInformation/gmi:MI_AcquisitionInformation/gmi:instrument/gmi:MI_Instrument/gmi:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString" /></field>


	</xsl:template>

</xsl:stylesheet>