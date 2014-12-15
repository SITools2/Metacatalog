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
			<xsl:apply-templates select="gmd:MD_Metadata"
				mode="metadata" />
		</doc>
	</xsl:template>





	<xsl:template match="*" mode="metadata">
	
		<!-- identifier -->
		<xsl:if test="not(gmd:fileIdentifier/gco:CharacterString)" >
			<error name="identifier">Identifier not found for record <xsl:value-of select="gmd:fileIdentifier/gco:CharacterString"/> with this XPath expression "gmd:fileIdentifier/gco:CharacterString"</error>
		</xsl:if>
	
		<!-- authority -->
		<xsl:if test="not(gmd:contact/gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString)" >
			<error name="authority">Authority not found for record <xsl:value-of select="gmd:fileIdentifier/gco:CharacterString"/> with this XPath expression "gmd:contact/gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString"</error>
		</xsl:if>
		
		<!-- title -->
		<xsl:if test="not(gmd:identificationInfo//gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString)" >
			<error name="title">Title not found for record with <xsl:value-of select="gmd:fileIdentifier/gco:CharacterString"/> this Xpath expression "gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString"</error>
		</xsl:if>
		
		<!-- description -->
		<xsl:if test="not(gmd:identificationInfo//gmd:MD_DataIdentification/gmd:abstract/gco:CharacterString)" >
			<error name="description">Description not found for record <xsl:value-of select="gmd:fileIdentifier/gco:CharacterString"/> with this Xpath expression "gmd:identificationInfo/gmd:MD_DataIdentification/gmd:abstract/gco:CharacterString"</error>
		</xsl:if>
		
		<!-- product -->
		<xsl:if test="not(gmd:contentInfo/gmd:MD_ImageDescription/gmd:contentType/gmd:MD_CoverageContentTypeCode)" >
			<error name="product">Product not found for record <xsl:value-of select="gmd:fileIdentifier/gco:CharacterString"/> with this Xpath expression "gmd:contentInfo/gmd:MD_ImageDescription/gmd:contentType"</error>
		</xsl:if>
		
		<!-- platform -->
		<xsl:if test="not(gmi:acquisitionInformation/gmi:MI_AcquisitionInformation/gmi:platform/gmi:MI_Platform/gmi:identifier/gmd:MD_Identifier/gmd:code)" >
			<error name="platform">Platform not found for record <xsl:value-of select="gmd:fileIdentifier/gco:CharacterString"/> with this Xpath expression "gmi:acquisitionInformation/gmi:MI_AcquisitionInformation/gmi:platform/gmi:MI_Platform/gmi:identifier/gmd:MD_Identifier/gmd:code"</error>
		</xsl:if>
		
		<!-- instrument -->
		<xsl:if test="not(gmi:acquisitionInformation/gmi:MI_AcquisitionInformation/gmi:instrument/gmi:MI_Instrument/gmi:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString)" >
			<error name="instrument">Instrument not found for record <xsl:value-of select="gmd:fileIdentifier/gco:CharacterString"/> with this Xpath expression "gmi:acquisitionInformation/gmi:MI_AcquisitionInformation/gmi:instrument/gmi:MI_Instrument/gmi:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString"</error>
		</xsl:if>
		
		<!-- resolution -->
		<xsl:if test="not(gmd:identificationInfo//gmd:MD_DataIdentification/gmd:spatialResolution/gmd:MD_Resolution/gmd:distance/gco:Distance)" >
			<error name="resolution">Resolution not found for record <xsl:value-of select="gmd:fileIdentifier/gco:CharacterString"/> with this Xpath expression "gmd:spatialResolution/gmd:MD_Resolution/gmd:distance/gco:Distance"</error>
		</xsl:if>

		<!-- start date -->
		<xsl:if test="not(gmd:identificationInfo//gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:beginPosition)" >
			<error name="startdate">Start date not found for record <xsl:value-of select="gmd:fileIdentifier/gco:CharacterString"/> with this Xpath expression "gmd:identificationInfo//gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:beginPosition"</error>
		</xsl:if>

		<!-- completion date -->
		<xsl:if test="not(gmd:identificationInfo//gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:endPosition)" >
			<error name="completiondate">Completion date not found for record <xsl:value-of select="gmd:fileIdentifier/gco:CharacterString"/> with this Xpath expression "gmd:identificationInfo//gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:endPosition"</error>
		</xsl:if>

		<!-- footprint -->
		<xsl:if test="not(gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox)" >
			<error name="footprint">Footprint not found for record <xsl:value-of select="gmd:fileIdentifier/gco:CharacterString"/> with this Xpath expression "gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox"</error>
		</xsl:if>
		
		<!-- WMS -->
		<xsl:if test="not(gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:linkage/gmd:URL)" >
			<error name="wms" level="warning">WMS not found for record <xsl:value-of select="gmd:fileIdentifier/gco:CharacterString"/> with this Xpath expression "gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:linkage/gmd:URL"</error>
		</xsl:if>
		<xsl:if test="not(gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:protocol/gco:CharacterString)" >
			<error name="wms" level="warning">WMS VERSION not found for record <xsl:value-of select="gmd:fileIdentifier/gco:CharacterString"/> with this Xpath expression "gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:protocol/gco:CharacterString"</error>
		</xsl:if>
		<xsl:if test="not(gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:name/gco:CharacterString)" >
			<error name="wms" level="warning">WMS LAYERS not found for record <xsl:value-of select="gmd:fileIdentifier/gco:CharacterString"/> with this Xpath expression "gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:name/gco:CharacterString"</error>
		</xsl:if>
		<xsl:if test="not(gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox)" >
			<error name="wms" level="warning">WMS BBOX not found for record <xsl:value-of select="gmd:fileIdentifier/gco:CharacterString"/> with this Xpath expression "gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox"</error>
		</xsl:if>
		<xsl:if test="not(gmd:referenceSystemInfo/gmd:MD_ReferenceSystem/gmd:referenceSystemIdentifier/gmd:RS_Identifier/gmd:code/gco:CharacterString)" >
			<error name="wms" level="warning">WMS SRS not found for record <xsl:value-of select="gmd:fileIdentifier/gco:CharacterString"/> with this Xpath expression "gmd:referenceSystemInfo/gmd:MD_ReferenceSystem/gmd:referenceSystemIdentifier/gmd:RS_Identifier/gmd:code/gco:CharacterString"</error>
		</xsl:if>
		
		<field name="identifier">urn:ogc:def:EOP:<xsl:value-of select="string(gmd:contact/gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString)"/>:Geosud:<xsl:value-of select="string(gmd:fileIdentifier/gco:CharacterString)" /></field>

		<field name="project">geosud</field>
		
		<!--<field name="hierarchyLevelName"><xsl:value-of select="string(gmd:hierarchyLevelName/gco:CharacterString)" /></field>-->
		<xsl:variable name="hierarchy" select="string(gmd:hierarchyLevelName/gco:CharacterString)"> </xsl:variable>
		<xsl:if test="$hierarchy!='image'">
			<error name="hierarchyLevelName">HierarchyLevelName not set to "image" for record <xsl:value-of select="gmd:fileIdentifier/gco:CharacterString"/> </error>
		</xsl:if>
		

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
					<field name="resolution"><xsl:value-of select="string(.)" /></field>
				</xsl:for-each>
			</xsl:for-each>

			<field name="quicklook"><xsl:value-of select="string(gmd:graphicOverview[1]/gmd:MD_BrowseGraphic/gmd:fileName/gco:CharacterString)" /></field>

			<field name="thumbnail"><xsl:value-of select="string(gmd:graphicOverview[2]/gmd:MD_BrowseGraphic/gmd:fileName/gco:CharacterString)" /></field>

			<xsl:for-each select="gmd:resourceFormat/gmd:MD_Format">
				<field name="mimeType"><xsl:value-of select="string(gmd:name/gco:CharacterString)" /></field>
				<!--<field name="distributionAccess.version"><xsl:value-of select="string(gmd:version/gco:CharacterString)" /></field>-->
			</xsl:for-each>
			
			<xsl:for-each select="gmd:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod">
				<field name="startDate"><xsl:value-of select="string(gml:beginPosition)" /></field>
				<field name="completionDate"><xsl:value-of select="string(gml:endPosition)" /></field>
			</xsl:for-each>

		</xsl:for-each>

		<!-- Services browse WMS 
		<field name="wms">
			<xsl:value-of select="string(gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:linkage/gmd:URL)"/>
			?REQUEST=GetMap&#38;VERSION=<xsl:value-of select="string(gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:protocol/gco:CharacterString)"/>&#38;
			LAYERS=<xsl:value-of select="string(gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:name/gco:CharacterString)"/>&#38;BBOX=<xsl:value-of select="string(gmd:identificationInfo/gmd:MD_DataIdentification/gmd:geographicElement/gmd:EX_GeographicBoundingBox)"/>&#38;SRS=<xsl:value-of select="string(gmd:referenceSystemInfo/MD_ReferenceSystem/systemIdentifier/gmd:RS_Identifier/gmd:code/gco:CharacterString)"/>
		</field>-->
			
		<!-- Services download -->
		<xsl:for-each
			select="gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:linkage/gmd:URL">
			<field name="archive"><xsl:value-of select="string(.)" /></field>
		</xsl:for-each>

<!-- 
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
-->

		<xsl:for-each select="gmd:contact/*">

			<xsl:for-each select="gmd:organisationName/gco:CharacterString">
				<field name="authority"><xsl:value-of select="string(.)" /></field>
			</xsl:for-each>
<!--		
			<xsl:for-each
				select="gmd:contactInfo/gmd:CI_CONTACT/gmd:address/gmd:CI_ADDRESS/gmd:electronicMailAddress/gco:CharacterString">
				<field name="acquisitionSetup.facility.emailAddress"><xsl:value-of select="string(.)" /></field>
			</xsl:for-each>

			<xsl:for-each select="gmd:role/gmd:CI_RoleCode">
				<field name="acquisitionSetup.facility.role"><xsl:value-of select="string(.)" /></field>
			</xsl:for-each>
-->
		</xsl:for-each>

		
		<field name="productType"><xsl:value-of select="string(gmd:contentInfo/gmd:MD_ImageDescription/gmd:contentType/gmd:MD_CoverageContentTypeCode)"/></field>
		
		<field name="processingLevel"><xsl:value-of select="string(gmd:contentInfo/gmd:MD_ImageDescription/gmd:processingLevelCode/gmd:MD_Identifier/gmd:code/gco:CharacterString)"/></field>
		
		
		<xsl:for-each
			select="gmi:acquisitionInformation/gmi:MI_AcquisitionInformation/gmi:instrument/gmi:MI_Instrument/gmi:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString">
			<field name="instrument"><xsl:value-of select="string(.)" /></field>
		</xsl:for-each>

		<xsl:for-each
			select="gmi:acquisitionInformation/gmi:MI_AcquisitionInformation/gmi:platform/gmi:MI_Platform/gmi:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString">
			<field name="platform"><xsl:value-of select="string(.)" /></field>
		</xsl:for-each>
		
		
		<xsl:for-each select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:geographicElement/EX_GeographicBoundingBox">
			<field name="footprint"><xsl:value-of select="string(.)" /></field>
		</xsl:for-each>
		
	<!--	
		<xsl:for-each select="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword/gco:CharacterString">
			<field name="keywords"><xsl:value-of select="string(.)" /></field>
		</xsl:for-each>
	-->
		
		
	<!--
		<field name="acquisitionSetup"><xsl:value-of select="string(gmi:acquisitionInformation/gmi:MI_AcquisitionInformation/gmi:platform/gmi:MI_Platform/gmi:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString)" />/<xsl:value-of select="gmi:acquisitionInformation/gmi:MI_AcquisitionInformation/gmi:instrument/gmi:MI_Instrument/gmi:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString" /></field>
	-->

	</xsl:template>

</xsl:stylesheet>