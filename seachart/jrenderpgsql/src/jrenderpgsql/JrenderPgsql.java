// License: GPL. For details, see LICENSE file.
package jrenderpgsql;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import jrendercore.JRenderCore;
import jrendercore.JRenderCore.Bounds;
import render.ChartContext;
import render.Renderer;
import s57.S57map;
import s57.S57map.Feature;
import s57.S57map.Snode;
import s57.S57osm;

import org.postgis.Geometry;
import org.postgis.LinearRing;
import org.postgis.LineString;
import org.postgis.MultiPolygon;
import org.postgis.PGgeometry;
import org.postgis.Point;
import org.postgis.PointComposedGeom;
import org.postgis.Polygon;

/**
 * @author Frederik Ramm
 *         Based on Jrender by Malcom Herring
 */
public final class JrenderPgsql {
    private JrenderPgsql() {
        // Hide default constructor for utilities classes
    }

    // These are static so we can build the OSM XmlDocument without passing lots of
    // parameters
    static Document osmDoc;
    static Element osmElem;
    static int nid = 0;
    static int wid = 1000000; // Offset so the document IDs are [probably] unique
    static HashMap<Point, Element> nodes = new HashMap<Point, Element>();

    // keep track of the bounds of the OSM data received
    static double minlon = 999.0;
    static double minlat = 999.0;
    static double maxlat = -999.0;
    static double maxlon = -999.0;

    static boolean debug = false;

    /**
     * helper for adding a <node> tag to the pseudo OSM XML
     * 
     * @param p     the node coordinates
     * @param reuse whether an existing node at the same point should be reused
     * @returns The node element created (re-uses nodes at same location)
     */
    private static Element addNode(Point p, boolean reuse) {
        // We can optioanlly reuse a node, if a way references the same exact postion
        // Actual nodes with tags should be called with reuse = false
        if (reuse) {
            Element existing = nodes.get(p);

            if (existing != null)
                return existing;
        }

        double lat = p.getY();
        double lon = p.getX();
        Element node = osmDoc.createElement("node");

        if (lat > maxlat)
            maxlat = lat;
        if (lat < minlat)
            minlat = lat;
        if (lon > maxlon)
            maxlon = lon;
        if (lon < minlon)
            minlon = lon;

        node.setAttribute("id", String.valueOf(++nid));
        node.setAttribute("lat", String.valueOf(lat));
        node.setAttribute("lon", String.valueOf(lon));
        node.setAttribute("version", "1");
        node.setAttribute("user", "1");
        node.setAttribute("uid", "1");
        node.setAttribute("changeset", "1");
        node.setAttribute("timestamp", "1980-01-01T00:00:00Z");

        osmElem.appendChild(node);
        nodes.put(p, node);

        return node;
    }

    private static Element addPoly(Polygon po, String table, String osmid) {
        if (po.numRings() > 1) {
            System.err.println("warning: polygons with holes not supported (" + table + " id=" + osmid + ")");
        }

        // Just add the first ring
        LinearRing lr = (LinearRing) po.getRing(0);

        return addWay(lr);
    }

    private static void addTag(Element elem, String k, String v) {
        if (k == null || k == "")
            return;

        Element tag = osmDoc.createElement("tag");

        tag.setAttribute("k", k);
        tag.setAttribute("v", v);

        elem.appendChild(tag);
    }

    private static Element addWay(PointComposedGeom geom) {
        Element way = osmDoc.createElement("way");

        way.setAttribute("id", String.valueOf(++wid));
        way.setAttribute("version", "1");
        way.setAttribute("user", "1");
        way.setAttribute("uid", "1");
        way.setAttribute("changeset", "1");
        way.setAttribute("timestamp", "1980-01-01T00:00:00Z");

        for (int i = 0; i < geom.numPoints(); i++) {
            Point p = geom.getPoint(i);
            Element node = addNode(p, true);
            Element nodeRef = osmDoc.createElement("nd");

            nodeRef.setAttribute("ref", node.getAttribute("id"));

            way.appendChild(nodeRef);
        }

        osmElem.appendChild(way);

        return way;
    }

    private static void createOsmDocument() throws ParserConfigurationException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

        osmDoc = dBuilder.newDocument();
        osmElem = osmDoc.createElement("osm");

        osmElem.setAttribute("version", "0.6");
        osmDoc.appendChild(osmElem);
    }

    /**
     * helper for adding a PostGIS geometry to pseudo OSM XML
     * 
     * @returns either the node or the way string buffer, depending on geom type
     */
    private static Element decodeGeom(PGgeometry geom, String table, String osmid) {
        if (geom.getGeoType() == Geometry.POINT) {
            // Don't reuse a node as we need to add tags to it
            return addNode((Point) geom.getGeometry(), false);
        } else if (geom.getGeoType() == Geometry.LINESTRING) {
            LineString ls = (LineString) geom.getGeometry();

            return addWay(ls);
        } else if (geom.getGeoType() == Geometry.POLYGON) {
            Polygon poly = (Polygon) geom.getGeometry();

            return addPoly(poly, table, osmid);
        } else if (geom.getGeoType() == Geometry.MULTIPOLYGON) {
            MultiPolygon po = (MultiPolygon) geom.getGeometry();
            Element elem = null;

            for (Polygon p : po.getPolygons()) {
                elem = addPoly(p, table, osmid);
            }

            return elem;
        }

        System.err.println("bad geo type: " + geom.getGeoType());
        System.exit(-1);
        return null;
    }

    private static void insertBounds() {
        Element bounds = osmDoc.createElement("bounds");
        org.w3c.dom.Node firstChild = osmElem.getFirstChild();

        if (firstChild == null)
            return;

        bounds.setAttribute("minlon", String.valueOf(minlon));
        bounds.setAttribute("maxlon", String.valueOf(maxlon));
        bounds.setAttribute("minlat", String.valueOf(minlat));
        bounds.setAttribute("maxlat", String.valueOf(maxlat));

        osmElem.insertBefore(bounds, firstChild);
    }

    public static void main(String[] args) throws Exception {

        // parse command line
        // ------------------

        ArrayList<String> remain = new ArrayList<String>();
        double scale = 1.0;
        int tilesize = 256;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--scale")) {
                scale = Double.parseDouble(args[++i]);
            } else if (args[i].equals("--tilesize")) {
                tilesize = Integer.parseInt(args[++i]);
            } else if (args[i].equals("--debug")) {
                debug = true;
            } else {
                remain.add(args[i]);
            }
        }

        if (remain.size() < 5) {
            System.err.println(
                    "Usage: java -jar jrenderpgsql.jar [--scale x] [--tilesize x] [--debug] <database connection string> <zoom> <xtile> <ytile> <outputfile>");
            System.err.println(
                    "format of the database connection string: jdbc:postgresql:///dbname?user=myuser&password=mypwd");
            System.exit(-1);
        }

        final String dburl = remain.get(0);
        final int zoom = Integer.parseInt(remain.get(1));
        final int xtile = Integer.parseInt(remain.get(2));
        final int ytile = Integer.parseInt(remain.get(3));
        final String outfile = remain.get(4);

        Connection c = null;
        try {
            Class.forName("org.postgresql.Driver");
            c = DriverManager.getConnection(dburl);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(-1);
        }

        // Create the abstraction for the specified parameters
        final JRenderCore core = new JRenderCore(tilesize, scale);
        final Bounds bounds = core.getEpsg3857Bounds(zoom, xtile, ytile);
        final double border_merc = core.getMercatorBorder(zoom);

        // The document needs to be created before any queries are made so it's ready to
        // be populated
        createOsmDocument();

        // request data from PostGIS
        // -------------------------
        // This assumes the given database has the usual osm2pgsql tables,
        // and a "tags" column (i.e. imported with --hstore). Caution, if
        // the import was made with --hstore-match-only then not all
        // seamarks will be present.

        Statement stmt = c.createStatement();
        for (String table : new String[] { "planet_osm_point", "planet_osm_line", "planet_osm_polygon" }) {
            String query = "SELECT st_transform(way,4326) as mygeom, * FROM "
                    + table + " WHERE tags?'seamark:type' AND way && "
                    + "st_setsrid(st_makebox2d(st_makepoint(" + (bounds.west - border_merc)
                    + "," + (bounds.south - border_merc) + "), st_makepoint("
                    + (bounds.east + border_merc) + "," + (bounds.north + border_merc) + ")),3857)";
            if (debug)
                System.out.println(query);
            ResultSet rs = stmt.executeQuery(query);

            // analyse the result
            // ------------------
            // The result will contain of these columns:
            // 1. the spherical mercator geometry column "way" which we ignore
            // 2. the new geometry column "mygeom" which we use
            // 3. the "tags" column which requires special treatment
            // 4. lots of other, "normal" columns which we treat "normally"

            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();
            int ogeomcol = 0;
            int geomcol = 0;
            int tagscol = 0;
            int idcol = 0;
            for (int i = 1; i < colCount; i++) {
                String n = meta.getColumnName(i);
                if (n.equals("way")) {
                    ogeomcol = i;
                } else if (n.equals("tags")) {
                    tagscol = i;
                } else if (n.equals("osm_id")) {
                    idcol = i;
                } else if (n.equals("mygeom")) {
                    geomcol = i;
                }
            }
            if (geomcol == 0) {
                System.err.println("no geometry column in table " + table + "\n");
                System.exit(-1);
            }
            if (tagscol == 0) {
                System.err.println("no tags column in table " + table + "\n");
                System.exit(-1);
            }

            // read data
            // ---------
            // for each row, write a geomtry to the output stream, and
            // assemble its tags from the "normal" columns plus the "tags"
            // column.

            while (rs.next()) {
                PGgeometry geom = (PGgeometry) rs.getObject(geomcol);
                String osmid = (idcol > 0) ? rs.getString(idcol) : "nil";
                Element elem = decodeGeom(geom, table, osmid);

                if (elem == null) {
                    continue;
                }

                for (int i = 1; i < colCount; i++) {
                    // These fields are ignored
                    if (i == ogeomcol)
                        continue;
                    if (i == geomcol)
                        continue;
                    if (i == tagscol)
                        continue;

                    // The rest are tags
                    String k = meta.getColumnName(i);
                    String v = rs.getString(i);

                    if (v != null) {
                        addTag(elem, k, v);
                    }
                }

                PGHStore h = new PGHStore(rs.getString(tagscol));
                for (Object k : h.keySet()) {
                    addTag(elem, (String) k, (String) (h.get(k)));
                }
            }
        }

        // Once the added all the nodes and ways, the bounds can inserted as the 1st
        // element
        insertBounds();

        if (debug) {
            javax.xml.transform.TransformerFactory tf = javax.xml.transform.TransformerFactory.newInstance();
            javax.xml.transform.Transformer trans = tf.newTransformer();
            java.io.StringWriter sw = new java.io.StringWriter();

            trans.setOutputProperty(OutputKeys.INDENT, "yes");
            trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            trans.transform(new javax.xml.transform.dom.DOMSource(osmDoc),
                    new javax.xml.transform.stream.StreamResult(sw));

            System.out.println(sw.toString());
        }

        // The pseudo OSM file is now complete, and we feed it to the S57
        // library where it will be parsed again.
        core.loadOsmData(osmDoc);
        core.render(zoom, xtile, ytile, outfile);

        System.exit(0);
    }
}
