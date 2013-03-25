package fr.cnes.sitools.metacatalogue.search.solr.spatial.parser;

import healpix.essentials.RangeSet;
import healpix.essentials.Scheme;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.CachingWrapperFilter;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.FilteredQuery;
import org.apache.lucene.search.Query;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QueryParsing;
import org.apache.solr.search.SolrQueryParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.cnes.sitools.SearchGeometryEngine.CoordSystem;
import fr.cnes.sitools.SearchGeometryEngine.GeometryIndex;
import fr.cnes.sitools.SearchGeometryEngine.Point;
import fr.cnes.sitools.SearchGeometryEngine.Polygon;
import fr.cnes.sitools.SearchGeometryEngine.RingIndex;
import fr.cnes.sitools.metacatalogue.search.solr.spatial.HealpixGeometryTool;
import fr.cnes.sitools.metacatalogue.search.solr.spatial.OpenwisGeometryTool;

/**
 * The Class OpenwisLuceneQParser.
 * <P>
 * Explanation goes here.
 * <P>
 */
public class GeometryLuceneQParser extends QParser {

  /** The openwis spatial request parameter. */
  private static final String GEO_SPATIAL_REQUEST = "geoRequest";

  /** The PARSING_TYPE. */
  public static final String PARSING_TYPE = "geoRequest.type";

  /** The PARSING_TYPE. */
  public static final GeoParsingTypes DEFAULT_PARSING_TYPE = GeoParsingTypes.POSTGIS;

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(GeometryLuceneQParser.class);

  /** The parser. */
  private SolrQueryParser lparser;

  /** The geometry filter. */
  private final OpenwisGeometryTool geometryFilter;

  /**
   * Instantiates a new OpenWOS lucene q parser.
   * 
   * @param qstr
   *          the query strings
   * @param localParams
   *          the local params
   * @param params
   *          the params
   * @param req
   *          the request
   */
  public GeometryLuceneQParser(String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
    super(qstr, localParams, params, req);
    OpenwisGeometryTool filter = null;
    try {
      filter = OpenwisGeometryTool.getInstance();
    }
    catch (SecurityException e) {
      logger.error("Fail to create geometry filter", e);
    }
    finally {
      geometryFilter = filter;
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.apache.solr.search.QParser#parse()
   */
  @Override
  public Query parse() throws ParseException {
    String qstr = getString();

    String defaultField = getParam(CommonParams.DF);
    if (defaultField == null) {
      defaultField = getReq().getSchema().getDefaultSearchFieldName();
    }
    lparser = new SolrQueryParser(this, defaultField);

    // these could either be checked & set here, or in the SolrQueryParser constructor
    String opParam = getParam(QueryParsing.OP);
    if (opParam != null) {
      lparser.setDefaultOperator("AND".equals(opParam) ? QueryParser.Operator.AND : QueryParser.Operator.OR);
    }
    else {
      // try to get default operator from schema
      QueryParser.Operator operator = getReq().getSchema().getSolrQueryParser(null).getDefaultOperator();
      lparser.setDefaultOperator(null == operator ? QueryParser.Operator.OR : operator);
    }

    String parsingTypeStr = getParam(PARSING_TYPE);
    GeoParsingTypes parsingType = DEFAULT_PARSING_TYPE;
    if (parsingTypeStr != null && !parsingTypeStr.isEmpty()) {
      try {
        parsingType = GeoParsingTypes.valueOf(parsingTypeStr);
      }
      catch (Exception e) {
        logger.warn("ParsingType : " + parsingTypeStr + " not supported", e);
        parsingType = DEFAULT_PARSING_TYPE;
      }
    }

    if (parsingType == GeoParsingTypes.HEALPIX || parsingType == GeoParsingTypes.BOTH) {
      if (qstr != null && !qstr.isEmpty()) {
        qstr += " AND ";
      }
      else {
        qstr = new String();
      }

      try {
        qstr += getGeometryCriteriaHealpix();
      }
      catch (Exception e) {
        throw new ParseException(e.getMessage());
      }
    }

    logger.info("GEO PARSING TYPE : " + parsingType);
    logger.info("Executed query : " + qstr);

    // Parse query
    Query query = lparser.parse(qstr);
    if (parsingType == GeoParsingTypes.POSTGIS || parsingType == GeoParsingTypes.BOTH) {
      query = addSpatialFilter(query);
    }
    return query;
  }

  /**
   * Adds the spatial filter.
   * 
   * @param query
   *          the query
   * @return the query
   */
  private Query addSpatialFilter(Query query) {
    Query result = query;

    String bbox = getParam(GEO_SPATIAL_REQUEST);
    // String filterVersion = getParam(FILTER_VERSION);
    if (bbox != null && !"".equals(bbox)) {
      try {
        Filter filter = null;
        filter = geometryFilter.buildFilter(query, bbox);

        if (filter != null) {
          result = new FilteredQuery(query, new CachingWrapperFilter(filter));
        }
      }
      catch (Exception e) {
        logger.error("Could not create spatial filter", e);
      }
    }
    return result;
  }

  private String getGeometryCriteriaHealpix() throws Exception {

    String box = getParam(GEO_SPATIAL_REQUEST);

    if (box != null && !box.isEmpty()) {
      String[] boxCoord = box.split(",");

      double long1 = new Double(boxCoord[0]);
      double lat1 = new Double(boxCoord[1]);
      double long2 = new Double(boxCoord[2]);
      double lat2 = new Double(boxCoord[3]);

      CoordSystem coordSystem = CoordSystem.GEOCENTRIC;

      Point point1 = new Point(long1, lat1, coordSystem);
      Point point2 = new Point(long2, lat2, coordSystem);

      Polygon shape = new Polygon(point1, point2);

      RingIndex healpixIndex = (RingIndex) GeometryIndex.createIndex(shape, Scheme.RING);
      healpixIndex.setOrder(healpixIndex.getOrder() + HealpixGeometryTool.HEALPIX_BEST_ORDER_PRECISION);
      if (healpixIndex.getOrder() >= HealpixGeometryTool.MAX_HEALIPX_ORDER) {
        healpixIndex.setOrder(HealpixGeometryTool.MAX_HEALIPX_ORDER);
      }

      int nbOrder = healpixIndex.getOrder();

      // faire iterator
      // RangeSet.ValueIterator valueIter = ((RangeSet) index).valueIterator();

      RangeSet healpixResult = ((RangeSet) healpixIndex.getIndex());

      String constraint = "(";

      boolean first = true;
      for (int i = 0; i < healpixResult.size(); i++) {
        if (first) {
          first = false;
        }
        else {
          constraint += " OR ";
        }
        constraint += String.format("healpix-order-%s:", nbOrder);
        constraint += String.format("[%s TO %s]", healpixResult.ivbegin(i), healpixResult.ivend(i));

      }

      constraint = constraint.concat(")");
      System.out.println("Constraint healpix : " + constraint);
      return constraint;
    }
    else {
      return null;
    }

  }

  /**
   * {@inheritDoc}
   * 
   * @see org.apache.solr.search.QParser#getDefaultHighlightFields()
   */
  @Override
  public String[] getDefaultHighlightFields() {
    return new String[] { lparser.getField() };
  }

}