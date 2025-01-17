// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.text.WordUtils;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.plugins.opendata.core.OdConstants;
import org.openstreetmap.josm.plugins.opendata.core.datasets.SimpleDataSetHandler;
import org.openstreetmap.josm.tools.Logging;

/**
 * Utilities for French names.
 */
public abstract class NamesFrUtils {

    private static Map<String, String> dictionary = initDictionary();

    public static final String checkDictionary(String value) {
        String result = "";
        for (String word : value.split(" ")) {
            if (!result.isEmpty()) {
                result += " ";
            }
            result += dictionary.containsKey(word) ? dictionary.get(word) : word;
        }
        return result;
    }

    private static Map<String, String> initDictionary() {
        Map<String, String> result = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                SimpleDataSetHandler.class.getResourceAsStream(OdConstants.DICTIONARY_FR), OdConstants.UTF8))) {
            String line = reader.readLine(); // Skip first line
            Logging.trace(line);
            while ((line = reader.readLine()) != null) {
                String[] tab = line.split(";");
                result.put(tab[0].replace("\"", ""), tab[1].replace("\"", ""));
            }
        } catch (IOException e) {
            Logging.error(e);
        }
        return result;
    }

    public static final String getStreetLabel(String label) {
        if (label == null) {
            return label;
        } else if (label.startsWith("All")) {
            return "Allée";
        } else if (label.equals("Autoroute")) {
            return label;
        } else if (label.startsWith("Anc")) {
            return "Ancien";
        } else if (label.startsWith("Av")) {
            return "Avenue";
        } else if (label.startsWith("Barr")) {
            return "Barrière";
        } else if (label.equals("Bd") || label.equals("Boulevard")) {
            return "Boulevard";
        } else if (label.startsWith("Bret")) {
            return "Bretelle";
        } else if (label.equals("Bre")) {
            return "Bré";
        } else if (label.equals("Caminot")) {
            return label;
        } else if (label.equals("Carrefour")) {
            return label;
        } else if (label.equals("Carré")) {
            return label;
        } else if (label.startsWith("Chemine")) {
            return "Cheminement";
        } else if (label.startsWith("Che")) {
            return "Chemin";
        } else if (label.startsWith("Cit")) {
            return "Cité";
        } else if (label.equals("Clos")) {
            return label;
        } else if (label.equals("Cote") || label.equals("Côte")) {
            return "Côte";
        } else if (label.equals("Cours")) {
            return label;
        } else if (label.startsWith("Dep") || label.startsWith("Dép")) {
            return "Départementale";
        } else if (label.startsWith("Dom")) {
            return "Domaine";
        } else if (label.equals("Dsc") || label.startsWith("Desc")) {
            return "Descente";
        } else if (label.equals("Esp") || label.startsWith("Espl")) {
            return "Esplanade";
        } else if (label.startsWith("Espa")) {
            return "Espace";
        } else if (label.equals("Giratoire")) {
            return label;
        } else if (label.equals("Grande-rue")) {
            return label;
        } else if (label.equals("Hameau")) {
            return label;
        } else if (label.startsWith("Imp") || label.equals("Ipasse")) {
            return "Impasse";
        } else if (label.startsWith("Itin")) {
            return "Itinéraire";
        } else if (label.equals("Jardin")) {
            return label;
        } else if (label.startsWith("L'") || label.equals("La") || label.equals("Le") || label.equals("Les") ||
                label.equals("Saint")) { // Lieux-dits
            return label;
        } else if (label.startsWith("Lot")) {
            return "Lotissement";
        } else if (label.equals("Mail")) {
            return label;
        } else if (label.equals("Mas")) {
            return label;
        } else if (label.startsWith("Nat")) {
            return "Nationale";
        } else if (label.equals("Parc")) {
            return label;
        } else if (label.equals("Passerelle")) {
            return label;
        } else if (label.startsWith("Pas")) {
            return "Passage";
        } else if (label.equals("Pch") || label.startsWith("Petit-chem")) {
            return "Petit-chemin";
        } else if (label.equals("Petit") || label.equals("Petite")) {
            return label;
        } else if (label.equals("Petite-allée")) {
            return label;
        } else if (label.equals("Petite-rue")) {
            return label;
        } else if (label.equals("Plan")) {
            return label;
        } else if (label.startsWith("Pl")) {
            return "Place";
        } else if (label.equals("Pont")) {
            return label;
        } else if (label.equals("Port")) {
            return label;
        } else if (label.equals("Porte")) {
            return label;
        } else if (label.startsWith("Prom")) {
            return "Promenade";
        } else if (label.equals("Prv") || label.startsWith("Parv")) {
            return "Parvis";
        } else if (label.startsWith("Qu")) {
            return "Quai";
        } else if (label.equals("Rampe")) {
            return label;
        } else if (label.startsWith("Res") || label.startsWith("Rés")) {
            return "Résidence";
        } else if (label.equals("Rocade")) {
            return label;
        } else if (label.equals("Rpt") || label.startsWith("Ron")) {
            return "Rond-Point";
        } else if (label.equals("Rte") || label.equals("Route")) {
            return "Route";
        } else if (label.equals("Rue") || label.equals("Rued")) {
            return "Rue";
        } else if (label.equals("Sentier")) {
            return label;
        } else if (label.startsWith("Sq")) {
            return "Square";
        } else if (label.equals("Théâtre")) {
            return "Théâtre";
        } else if (label.startsWith("Tra")) {
            return "Traverse";
        } else if (label.equals("Vieux")) {
            return label;
        } else if (label.equals("Voie")) {
            return label;
        } else if (label.equals("Zone")) {
            return label;
        } else {
            Logging.warn("unknown street label: "+label);
            return label;
        }
    }

    public static final String checkStreetName(OsmPrimitive p, String key) {
        String value = null;
        if (p != null) {
            value = p.get(key);
            if (value != null) {
                value = WordUtils.capitalizeFully(value);
                // Cas particuliers
                if (value.equals("Boulingrin")) { // square Boulingrin, mal formé
                    value = "Sq Boulingrin";
                } else if (value.matches("A[0-9]+")) { // Autoroutes sans le mot "Autoroute"
                    value = "Autoroute "+value;
                } else if (value.equals("All A61")) { // A61 qualifiée d'Allée ?
                    value = "Autoroute A61";
                } else if (value.startsWith("Che Vieux Che")) { // "Che" redondant
                    value = value.replaceFirst("Che ", "");
                } else if (value.startsWith("Petite Allee ")) { // Tiret, comme grand-rue, petite-rue
                    value = value.replaceFirst("Petite Allee ", "Petite-allée ");
                } else if (value.startsWith("Ld De ")) { // Lieux-dit
                    value = value.replaceFirst("Ld De ", "");
                }
                while (value.startsWith("Ld ")) { // Lieux-dit, inutile. Plus le cas avec "Ld Ld"
                    value = value.replaceFirst("Ld ", "");
                }
                if (value.startsWith("L ")) {
                    value = value.replaceFirst("L ", "L'");
                }
                String[] words = value.split(" ");
                if (words.length > 0) {
                    value = "";
                    List<String> list = Arrays.asList(words);
                    words[0] = getStreetLabel(words[0]);
                    if (words[0].equals("Ancien") && words.length > 1 && words[1].equals("Che")) {
                        words[1] = "Chemin";
                    }
                    for (int i = 0; i < words.length; i++) {
                        if (i > 0) {
                            value += " ";
                            // Prénoms/Noms propres abrégés
                            if (words[i].equals("A") && list.contains("Bernard")) {
                                words[i] = "Arnaud";
                            } else if (words[i].equals("A") && list.contains("Passerieu")) {
                                words[i] = "Ariste";
                            } else if (words[i].equals("A") && list.contains("Bougainville")) {
                                words[i] = "Antoine";
                            } else if (words[i].equals("Ch") && list.contains("Leconte")) {
                                words[i] = "Charles";
                            } else if (words[i].equals("Frs") && list.contains("Dugua")) {
                                words[i] = "François";
                            } else if (words[i].equals("G") && list.contains("Latecoere")) {
                                words[i] = "Georges";
                            } else if (words[i].equals("H") && list.contains("Lautrec")) {
                                words[i] = "Henri";
                            } else if (words[i].equals("J") && list.contains("Dieulafoy")) {
                                words[i] = "Jane";
                            } else if (words[i].equals("J") && (list.contains("Champollion") || list.contains("Stanislas"))) {
                                words[i] = "Jean";
                            } else if (words[i].equals("L") && list.contains("Zamenhof")) {
                                words[i] = "Ludwik";
                            } else if (words[i].equals("L") && list.contains("Sacha")) {
                                words[i] = "Lucien";
                                if (!list.contains("Et")) {
                                    words[i] += " et";
                                }
                            } else if (words[i].equals("L") && (list.contains("Vauquelin") || list.contains("Bougainville"))) {
                                words[i] = "Louis";
                            } else if (words[i].equals("M") && list.contains("Dieulafoy")) {
                                words[i] = "Marcel";
                            } else if (words[i].equals("M") && list.contains("Arifat")) {
                                words[i] = "Marie";
                            } else if (words[i].equals("N") && list.contains("Djamena")) {
                                words[i] = "N'";
                            } else if (words[i].equals("Oo")) {
                                words[i] = "Oô";
                            } else if (words[i].equals("Ph") && list.contains("Ravary")) {
                                words[i] = "Philippe";
                            } else if (words[i].equals("R") && list.contains("Folliot")) {
                                words[i] = "Raphaël";
                            } else if (words[i].equals("W") && list.contains("Booth")) {
                                words[i] = "William";
                            // Mots de liaison non couverts par le dictionnaire
                            } else if (words[i].equals("A")) {
                                words[i] = "à";
                            } else if (words[i].equals("D") || words[i].equals("L")) {
                                words[i] = words[i].toLowerCase()+"'";
                            } else if (words[i].equals("La") || words[i].equals("Le")) {
                                words[i] = words[i].toLowerCase();
                            }
                        }
                        value += words[i];
                    }
                }
                // Ponctuation
                value = value.replace("' ", "'");
                // Dictionnaire
                value = checkDictionary(value);
                p.put(key, value);
            }
        }
        return value;
    }
}
