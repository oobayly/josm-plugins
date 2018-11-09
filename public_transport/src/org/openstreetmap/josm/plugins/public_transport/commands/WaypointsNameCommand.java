// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.public_transport.commands;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.Collection;

import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.plugins.public_transport.TransText;
import org.openstreetmap.josm.plugins.public_transport.models.WaypointTableModel;

public class WaypointsNameCommand extends Command {
    private int workingLine = 0;

    private WaypointTableModel waypointTM = null;

    private String oldName = null;

    private String name = null;

    private String oldShelter = null;

    private TransText shelter;

    public WaypointsNameCommand(WaypointTableModel waypointTM, int workingLine, String name,
            TransText shelter) {
        super(MainApplication.getLayerManager().getEditDataSet());
        this.waypointTM = waypointTM;
        this.workingLine = workingLine;
        if (waypointTM.nodes.elementAt(workingLine) != null) {
            oldName = waypointTM.nodes.elementAt(workingLine).get("name");
            oldShelter = waypointTM.nodes.elementAt(workingLine).get("shelter");
        }
        this.name = name;
        this.shelter = shelter;
    }

    @Override
    public boolean executeCommand() {
        if (waypointTM.nodes.elementAt(workingLine) != null) {
            waypointTM.nodes.elementAt(workingLine).put("name", name);
            waypointTM.nodes.elementAt(workingLine).put("shelter", shelter.text);
        }
        waypointTM.inEvent = true;
        if (name == null)
            waypointTM.setValueAt("", workingLine, 1);
        else
            waypointTM.setValueAt(name, workingLine, 1);
        waypointTM.setValueAt(shelter, workingLine, 2);
        waypointTM.inEvent = false;
        return true;
    }

    @Override
    public void undoCommand() {
        if (waypointTM.nodes.elementAt(workingLine) != null) {
            waypointTM.nodes.elementAt(workingLine).put("name", oldName);
            waypointTM.nodes.elementAt(workingLine).put("shelter", oldShelter);
        }
        waypointTM.inEvent = true;
        if (oldName == null)
            waypointTM.setValueAt("", workingLine, 1);
        else
            waypointTM.setValueAt(oldName, workingLine, 1);
        waypointTM.setValueAt(new TransText(oldShelter), workingLine, 2);
        waypointTM.inEvent = false;
    }

    @Override
    public void fillModifiedData(Collection<OsmPrimitive> modified,
            Collection<OsmPrimitive> deleted, Collection<OsmPrimitive> added) {
    }

    @Override
    public String getDescriptionText() {
        return tr("Public Transport: Edit waypoint name");
    }
}
