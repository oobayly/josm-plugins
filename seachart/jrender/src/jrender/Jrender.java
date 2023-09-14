// License: GPL. For details, see LICENSE file.
package jrender;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Paths;
import jrendercore.JRenderCore;
import jrendercore.JRenderCore.BatchResponse;

/**
 * @author Malcolm Herring
 */
public final class Jrender {
    private Jrender() {
        // Hide default constructor for utilities classes
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 5) {
            System.err.println(
                    "Usage: java -jar jrender.jar <osm source file or directory> <tile directory> <zoom> <xtile> <ytile>");
            System.exit(-1);
        }

        final String srcdir = args[0];
        final String dstdir = args[1];
        final int zoom = Integer.parseInt(args[2]);
        final int xtile = Integer.parseInt(args[3]);
        final int ytile = Integer.parseInt(args[4]);
        File srcfile = new File(srcdir);

        // If the passed srcdir is directory then expect a file
        // <xtile>-<ytile>-<zoom>.osm in the directory
        // otherwise just the the path provided
        if (srcfile.isDirectory()) {
            srcfile = Paths.get(srcdir, String.format("%d-%d-%d.osm", xtile, ytile, zoom)).toFile();
        }

        // Create the abstraction for the specified parameters
        final JRenderCore core = new JRenderCore();
        core.loadOsmData(srcfile);

        final BatchResponse response = core.render(zoom, xtile, ytile, zoom >= 12 ? 18 : zoom, dstdir);

        // Write the send file for the tile
        if (response.hasData()) {
            final File sendFile = Paths.get(
                srcfile.getParent(),
                String.format("%d-%d-%d.send", zoom, xtile, ytile)).toFile();
            final PrintWriter writer = new PrintWriter(sendFile, "UTF-8");

            for (String str : response.sends) {
                writer.println(str);
            }

            for (String str : response.deletes) {
                writer.println(str);
            }

            writer.close();
        }

        System.exit(0);
    }
}
