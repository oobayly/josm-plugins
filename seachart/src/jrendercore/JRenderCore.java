package jrendercore;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import javax.imageio.ImageIO;

import org.w3c.dom.Document;

import render.ChartContext;
import render.Renderer;
import s57.S57map;
import s57.S57map.Feature;
import s57.S57map.Snode;
import s57.S57osm;

/**
 * An abstracted renderer that handles rendering of Documents and InputStreams
 * for use with the jrender and jrenderpgsql CLI tools.
 */
public final class JRenderCore {
    public final static int DEFAULT_TILE_SIZE = 256;

    private final static double RADIUS = 6378137.0;
    private final static double MERCATOR_OFFSET = RADIUS * Math.PI;
    private final static double MERCATOR_WIDTH = 2 * MERCATOR_OFFSET;

    private final int tileSize;
    private final double scale;
    private final JRenderContext context;

    private int emptySize = -1;
    private S57map map;

    /** Creates an instance of JRenderCore with a tile size of 256px. */
    public JRenderCore() {
        this(DEFAULT_TILE_SIZE);
    }

    /** Creates an instance of JRenderCore with the specified tile size. */
    public JRenderCore(int tileSize) {
        this(tileSize, (double) tileSize / DEFAULT_TILE_SIZE);
    }

    /** Creates an instance of JRenderCore with the specified tile size and scale */
    public JRenderCore(int tileSize, double scale) {
        this.tileSize = tileSize;
        this.scale = scale;
        this.context = new JRenderContext();
    }

    /** Gets the size of an empty PNG. */
    private int getEmptySize() throws IOException {
        if (emptySize == -1) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_ARGB), "png", bos);

            emptySize = bos.size();
        }

        return emptySize;
    }

    /**
     * Gets the bounds of the specified tile in Web Mercator projection (EPSG:3857)
     * 
     * @return The bounding box with coordinates in radians.
     */
    public Bounds getEpsg3857Bounds(int zoom, int tileX, int tileY) {
        final int pow = 1 << zoom; // 2^zoom

        // Top left of the tile
        final double left = tileX;
        final double top = pow - tileY;
        
        // Bottom right of the tile.
        // Don't assume the the tileSize requested is a single standard tile
        // eg. A tile size of 2048 may be requested at a scale=1, therefore the area
        // will actually be 8x8 standard tiles
        final double right = left + (double)tileSize / DEFAULT_TILE_SIZE / scale;
        final double bottom = top - (double)tileSize / DEFAULT_TILE_SIZE / scale;

        final double west = left / pow * MERCATOR_WIDTH - MERCATOR_OFFSET;
        final double east = right / pow * MERCATOR_WIDTH - MERCATOR_OFFSET;
        final double north = top / pow * MERCATOR_WIDTH - MERCATOR_OFFSET;
        final double south = bottom / pow * MERCATOR_WIDTH - MERCATOR_OFFSET;

        return new Bounds(south, west, north, east);
    }

    /**
     * Gets the Web Mercator projection border size for the specified zoom level.
     * 
     * @return The border in radians. Useful for padding out a bounding box.
     */
    public double getMercatorBorder(int zoom) {
        final int pow = 1 << zoom; // 2^zoom
        final int border = (zoom < 12) ? tileSize >> (11 - zoom) : tileSize;

        return (MERCATOR_WIDTH / tileSize / pow) * border;
    }

    /** Gets the path to a tile's PNG file. */
    public static Path getTilePath(String prefix, final int zoom, final int tileX, final int tileY) {
        return Paths.get(prefix, String.valueOf(zoom), String.valueOf(tileX), String.format("%d.png", tileY));
    }

    /** Loads the specified file containing OSM data into the map. */
    public void loadOsmData(String fileName) throws Exception {
        loadOsmData(new File(fileName));
    }

    /** Loads the specified file containing OSM data into the map. */
    public void loadOsmData(File file) throws Exception {
        map = new S57map(true);
        S57osm.OSMmap(file, map, false);
    }

    /** Loads the specified document containing OSM data into the map. */
    public void loadOsmData(Document osmDoc) throws Exception {
        map = new S57map(true);
        S57osm.OSMmap(osmDoc, map, false);
    }

    /** Recursively remove empty directories */
    private static void removeEmptyDirs(File dir) {
        final File[] files = dir.listFiles();

        if (files != null) {
            for (File child : files) {
                if (child.isDirectory()) {
                    removeEmptyDirs(child);
                }
            }
        }

        if (files == null || files.length == 0) {
            dir.delete();
        }
    }

    /** Render the specified tile to the specified file. */
    public void render(int zoom, int tileX, int tileY, String file) throws Exception {
        FileOutputStream fos = new FileOutputStream(file);

        render(zoom, tileX, tileY, fos);

        fos.close();
    }

    /** Render the specified tile to the specified file. */
    public void render(int zoom, int tileX, int tileY, File file) throws Exception {
        FileOutputStream fos = new FileOutputStream(file);

        render(zoom, tileX, tileY, fos);

        fos.close();
    }

    /** Render the specified tile to the specified OutputStream. */
    public void render(int zoom, int tileX, int tileY, OutputStream oStream) throws Exception {
        context.setZoom(zoom);

        final BufferedImage img = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g2 = img.createGraphics();

        // Translate to the origin of the tile
        g2.translate(-(double)tileX * DEFAULT_TILE_SIZE * scale, -(double)tileY * DEFAULT_TILE_SIZE * scale);

        // Render
        Renderer.reRender(
                g2,
                new Rectangle(tileSize, tileSize),
                zoom,
                scale * Math.pow(2, (zoom - 12)), // Symbol scale factor
                map,
                context);

        ImageIO.write(img, "png", oStream);
    }

    /**
     * Recursively renders the specified tile to the output path.
     * 
     * @return A BatchResponse object that contains the files created and deleted.
     */
    public BatchResponse render(final int zoom, int tileX, int tileY, final int maxZoom, String path) throws Exception {
        if (maxZoom < zoom) {
            throw new InvalidParameterException("maxZoom cannot be less than zoom");
        }

        // The size of an empty png
        emptySize = getEmptySize();

        final BatchResponse response = new BatchResponse();

        render(zoom, tileX, tileY, maxZoom, path, response);
        removeEmptyDirs(new File(path));

        return response;
    }

    /** Recursively renders the specified tile. */
    private void render(
            final int zoom, final int tileX, final int tileY,
            final int maxZoom, final String path, final BatchResponse response) throws Exception {

        // The file that wil be written to
        final File outFile = getTilePath(path, zoom, tileX, tileY).toFile();
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();

        // Render the tile
        render(zoom, tileX, tileY, bos);

        final boolean hasData = bos.size() > emptySize;

        if (hasData) {
            // If the image contains content, then it's written to the file system
            outFile.getParentFile().mkdirs();

            FileOutputStream fos = new FileOutputStream(outFile);

            bos.writeTo(fos);
            bos.close();

            // List of file created - put <path> tiles/<zoom>/<x>/<y>.png
            response.sends.add(String.format("put %s %s", outFile, getTilePath("tiles", zoom, tileX, tileY)));
        } else if (outFile.exists()) {
            // Otherwise if a previous version exists, remove it
            outFile.delete();

            // List of files deleted
            response.sends.add(String.format("rm %s", outFile));
        }

        // Only recurse if less than max zoom, and the ZL < 16, or there is some data
        // rendered
        if ((zoom < maxZoom) && (hasData || (zoom < 16))) {
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 2; j++) {
                    render(zoom + 1, tileX * 2 + i, tileY * 2 + j, maxZoom, path, response);
                }
            }
        }
    }

    /** Represents a bounding box. */
    public static class Bounds {
        public final double east;

        public final double north;

        public final double south;

        public final double west;

        public Bounds(double south, double west, double north, double east) {
            this.east = east;
            this.north = north;
            this.south = south;
            this.west = west;
        }
    }

    public static class BatchResponse {
        public final ArrayList<String> deletes = new ArrayList<>();
        public final ArrayList<String> sends = new ArrayList<>();

        public boolean hasData() {
            return deletes.size() != 0 || sends.size() != 0;
        }
    }

    private class JRenderContext implements ChartContext {
        private int zoom = 0;
        private double pow = 1;

        @Override
        public Point2D getPoint(Snode coord) {
            // Number of pixels across the map per radian
            final double pixels = (double) DEFAULT_TILE_SIZE * scale * pow / 2 / Math.PI;

            // https://en.wikipedia.org/wiki/Web_Mercator_projection
            final double x = (coord.lon + Math.PI);
            final double y = (Math.PI - Math.log(Math.tan(Math.PI / 4 + coord.lat / 2)));

            return new Point2D.Double(x * pixels, y * pixels);
        }

        @Override
        public double mile(Feature feature) {
            // To get the length of a NM in pixels, we need to find the number of pixels between
            // the minute of latitude about the feature's location
            final double halfNM = Math.toRadians(0.5 / 60);
            final double lat = feature.geom.centre.lat - halfNM;
            final double lat2 = feature.geom.centre.lat + halfNM;
   
            // Using the derivative may be faster...
            final double y = (Math.PI - Math.log(Math.tan(Math.PI / 4 + lat / 2)));
            final double y2 = (Math.PI - Math.log(Math.tan(Math.PI / 4 + lat2 / 2)));
            
            return (y - y2) * (double) DEFAULT_TILE_SIZE * scale * pow / 2 / Math.PI;
        }

        @Override
        public boolean clip() {
            return false;
        }

        @Override
        public int grid() {
            return 0;
        }

        @Override
        public Chart chart() {
            return null;
        }

        @Override
        public Color background(S57map map) {
            return new Color(0, true);
        }

        @Override
        public RuleSet ruleset() {
            return RuleSet.SEAMARK;
        }

        public void setZoom(int value) {
            if (zoom != value) {
                this.zoom = value;
                this.pow = 1 << value; // 2^value
            }
        }
    }
}
