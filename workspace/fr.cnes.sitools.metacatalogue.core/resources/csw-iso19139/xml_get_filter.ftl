<Filter xmlns="http://www.opengis.net/ogc" xmlns:gml="http://www.opengis.net/gml">
	<PropertyIsLessThan>
		<PropertyName>Modified</PropertyName>
		<Literal>${date?string("yyyy-MM-dd HH:mm:ss")}</Literal>					
	</PropertyIsLessThan>
</Filter>