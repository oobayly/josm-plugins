// License: GPL. For details, see LICENSE file.
package render;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.AffineTransform;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.function.Consumer;

import render.ChartContext.RuleSet;
import render.Renderer.LabelStyle;
import s57.S57att.Att;
import s57.S57map.AttMap;
import s57.S57map.Feature;
import s57.S57map.ObjTab;
import s57.S57map.Pflag;
import s57.S57map.Rflag;
import s57.S57obj.Obj;
import s57.S57val;
import s57.S57val.AddMRK;
import s57.S57val.AttVal;
import s57.S57val.BcnSHP;
import s57.S57val.BnkWTW;
import s57.S57val.BoySHP;
import s57.S57val.CatACH;
import s57.S57val.CatCBL;
import s57.S57val.CatCRN;
import s57.S57val.CatDIS;
import s57.S57val.CatHAF;
import s57.S57val.CatLAM;
import s57.S57val.CatLIT;
import s57.S57val.CatLMK;
import s57.S57val.CatMOR;
import s57.S57val.CatNMK;
import s57.S57val.CatOBS;
import s57.S57val.CatOFP;
import s57.S57val.CatOPA;
import s57.S57val.CatPIL;
import s57.S57val.CatPIP;
import s57.S57val.CatREA;
import s57.S57val.CatROD;
import s57.S57val.CatSCF;
import s57.S57val.CatSEA;
import s57.S57val.CatSIL;
import s57.S57val.CatSIT;
import s57.S57val.CatSIW;
import s57.S57val.CatSLC;
import s57.S57val.CatWED;
import s57.S57val.CatWRK;
import s57.S57val.ColCOL;
import s57.S57val.ColPAT;
import s57.S57val.FncFNC;
import s57.S57val.MarSYS;
import s57.S57val.NatSUR;
import s57.S57val.NatQUA;
import s57.S57val.StsSTS;
import s57.S57val.TecSOU;
import s57.S57val.TopSHP;
import s57.S57val.TrfTRF;
import s57.S57val.UniHLU;
import s57.S57val.WatLEV;
import s57.S57val.CatVAN;
import symbols.Areas;
import symbols.Beacons;
import symbols.Buoys;
import symbols.Facilities;
import symbols.Harbours;
import symbols.Landmarks;
import symbols.Notices;
import symbols.Symbols;
import symbols.Symbols.Delta;
import symbols.Symbols.Handle;
import symbols.Symbols.LineStyle;
import symbols.Symbols.Patt;
import symbols.Symbols.Scheme;
import symbols.Symbols.Symbol;
import symbols.Topmarks;

/**
 * @author Malcolm Herring
 */
public class Rules {
	static final DecimalFormat df = new DecimalFormat("#.#");

	static final EnumMap<ColCOL, Color> bodyColours = new EnumMap<>(ColCOL.class);
	static {
		bodyColours.put(ColCOL.COL_UNK, new Color(0, true));
		bodyColours.put(ColCOL.COL_WHT, new Color(0xffffff));
		bodyColours.put(ColCOL.COL_BLK, new Color(0x000000));
		bodyColours.put(ColCOL.COL_RED, new Color(0xd40000));
		bodyColours.put(ColCOL.COL_GRN, new Color(0x00d400));
		bodyColours.put(ColCOL.COL_BLU, Color.blue);
		bodyColours.put(ColCOL.COL_YEL, new Color(0xffd400));
		bodyColours.put(ColCOL.COL_GRY, Color.gray);
		bodyColours.put(ColCOL.COL_BRN, new Color(0x8b4513));
		bodyColours.put(ColCOL.COL_AMB, new Color(0xfbf00f));
		bodyColours.put(ColCOL.COL_VIO, new Color(0xee82ee));
		bodyColours.put(ColCOL.COL_ORG, Color.orange);
		bodyColours.put(ColCOL.COL_MAG, new Color(0xf000f0));
		bodyColours.put(ColCOL.COL_PNK, Color.pink);
	}

	static final EnumMap<ColCOL, String> colourLetters = new EnumMap<>(ColCOL.class);
	static {
		colourLetters.put(ColCOL.COL_UNK, "");
		colourLetters.put(ColCOL.COL_WHT, "W");
		colourLetters.put(ColCOL.COL_BLK, "B");
		colourLetters.put(ColCOL.COL_RED, "R");
		colourLetters.put(ColCOL.COL_GRN, "G");
		colourLetters.put(ColCOL.COL_BLU, "Bu");
		colourLetters.put(ColCOL.COL_YEL, "Y");
		colourLetters.put(ColCOL.COL_GRY, "Gr");
		colourLetters.put(ColCOL.COL_BRN, "Bn");
		colourLetters.put(ColCOL.COL_AMB, "Am");
		colourLetters.put(ColCOL.COL_VIO, "Vi");
		colourLetters.put(ColCOL.COL_ORG, "Or");
		colourLetters.put(ColCOL.COL_MAG, "Mg");
		colourLetters.put(ColCOL.COL_PNK, "Pk");
	}

	static final EnumMap<ColPAT, Patt> pattMap = new EnumMap<>(ColPAT.class);
	static {
		pattMap.put(ColPAT.PAT_UNKN, Patt.Z);
		pattMap.put(ColPAT.PAT_HORI, Patt.H);
		pattMap.put(ColPAT.PAT_VERT, Patt.V);
		pattMap.put(ColPAT.PAT_DIAG, Patt.D);
		pattMap.put(ColPAT.PAT_BRDR, Patt.B);
		pattMap.put(ColPAT.PAT_SQUR, Patt.S);
		pattMap.put(ColPAT.PAT_CROS, Patt.C);
		pattMap.put(ColPAT.PAT_SALT, Patt.X);
		pattMap.put(ColPAT.PAT_STRP, Patt.H);
	}

	protected final Renderer renderer;
	private final Signals signals;

	public Rules(Renderer renderer) {
		this.renderer = renderer;
		this.signals = new Signals(renderer);
	}

	String getName(Feature feature) {
		AttVal<?> name = feature.atts.get(Att.OBJNAM);
		if (name == null) {
			AttMap atts = feature.objs.get(feature.type).get(0);
			if (atts != null) {
				name = atts.get(Att.OBJNAM);
			}
		}
		return (name != null) ? ((String) name.val).replace("&quot;", "\"") : null;
	}

	public void addName(Feature feature,int z, Font font) {
		addName(feature, z, font, Color.black, new Delta(Handle.CC, new AffineTransform()));
	}

	public void addName(Feature feature,int z, Font font, Color colour) {
		addName(feature, z, font, colour, new Delta(Handle.CC, new AffineTransform()));
	}

	public void addName(Feature feature,int z, Font font, Delta delta) {
		addName(feature, z, font, Color.black, delta);
	}

	public void addName(Feature feature, int z, Font font, Color colour, Delta delta) {
		if (renderer.zoom >= z) {
			String name = getName(feature);
			if (name != null) {
				renderer.labelText(feature, name, font, colour, delta);
			}
		}
	}

	AttMap getAtts(Feature feature, Obj obj, int idx) {
		HashMap<Integer, AttMap> objs = feature.objs.get(obj);
		if (objs == null)
			return null;
		else
			return objs.get(idx);
	}

	public Object getAttVal(Feature feature, Obj obj, Att att) {
		AttMap atts;
		HashMap<Integer, AttMap> objs;
		AttVal<?> item;
		if ((objs = feature.objs.get(obj)) != null)
			atts = objs.get(0);
		else
			return null;
		if ((atts == null) || ((item = atts.get(att)) == null))
			return null;
		else
			return item.val;
	}

	public String getAttStr(Feature feature, Obj obj, Att att) {
		String str = (String) getAttVal(feature, obj, att);
		if (str != null) {
			return str;
		}
		return "";
	}

	@SuppressWarnings("unchecked")
	public Enum<?> getAttEnum(Feature feature, Obj obj, Att att) {
		ArrayList<?> list = (ArrayList<?>) getAttVal(feature, obj, att);
		if (list != null) {
			return ((ArrayList<Enum<?>>) list).get(0);
		}
		return S57val.unknAtt(att);
	}

	@SuppressWarnings("unchecked")
	public ArrayList<?> getAttList(Feature feature, Obj obj, Att att) {
		ArrayList<Enum<?>> list = (ArrayList<Enum<?>>) getAttVal(feature, obj, att);
		if (list != null) {
			return list;
		}
		list = new ArrayList<>();
		list.add(S57val.unknAtt(att));
		return list;
	}

	@SuppressWarnings("unchecked")
	Scheme getScheme(Feature feature, Obj obj) {
		ArrayList<Color> colours = new ArrayList<>();
		for (ColCOL col : (ArrayList<ColCOL>) getAttList(feature, obj, Att.COLOUR)) {
			colours.add(bodyColours.get(col));
		}
		ArrayList<Patt> patterns = new ArrayList<>();
		for (ColPAT pat : (ArrayList<ColPAT>) getAttList(feature, obj, Att.COLPAT)) {
			patterns.add(pattMap.get(pat));
		}
		return new Scheme(patterns, colours);
	}

	boolean hasAttribute(Feature feature, Obj obj, Att att) {
		AttMap atts;
		if ((atts = getAtts(feature, obj, 0)) != null) {
			AttVal<?> item = atts.get(att);
			return item != null;
		}
		return false;
	}

	boolean testAttribute(Feature feature, Obj obj, Att att, Object val) {
		AttMap atts;
		if ((atts = getAtts(feature, obj, 0)) != null) {
			AttVal<?> item = atts.get(att);
			if (item != null) {
				switch (item.conv) {
				case S:
				case A:
					return ((String) item.val).equals(val);
				case E:
				case L:
					return ((ArrayList<?>) item.val).contains(val);
				case F:
				case I:
					return item.val == val;
				}
			}
		}
		return false;
	}

	boolean hasObject(Feature feature, Obj obj) {
		return feature.objs.containsKey(obj);
	}

	// public Feature feature;
	// ArrayList<Feature> objects;

	// boolean testObject(Obj obj) {
	// 	return ((objects = renderer.map.features.get(obj)) != null);
	// }

	void testObject(Object obj, Consumer<Feature> fn) {
		final ArrayList<Feature> objects = renderer.map.features.get(obj);

		if (objects != null) {
			for (Feature f: objects) {
				if (f.reln == Rflag.MASTER) {
					fn.accept(f);
				}
			}
		} 
	}

	// boolean testFeature(Feature f) {
	// 	return ((feature = f).reln == Rflag.MASTER);
	// }

	public boolean rules() {
		try {
			if ((renderer.context.ruleset() == RuleSet.ALL) || (renderer.context.ruleset() == RuleSet.BASE)) {
				testObject(Obj.LNDARE, f-> areas(f));
				testObject(Obj.SOUNDG, f-> depths(f));
				testObject(Obj.DEPCNT, f-> depths(f));
				testObject(Obj.TESARE, f-> areas(f));
				testObject(Obj.BUAARE, f-> areas(f));
				testObject(Obj.HRBFAC, f-> areas(f));
				testObject(Obj.HRBBSN, f-> areas(f));
				testObject(Obj.LOKBSN, f-> areas(f));
				testObject(Obj.LKBSPT, f-> areas(f));
				testObject(Obj.COALNE, f-> areas(f));
				testObject(Obj.LAKARE, f-> areas(f));
				testObject(Obj.RIVERS, f-> waterways(f));
				testObject(Obj.CANALS, f-> waterways(f));
				testObject(Obj.DEPARE, f-> areas(f));
				testObject(Obj.ROADWY, f-> highways(f));
				testObject(Obj.RAILWY, f-> highways(f));
			}
			testObject(Obj.SLCONS, f-> shoreline(f));
			if ((renderer.context.ruleset() == RuleSet.ALL) || (renderer.context.ruleset() == RuleSet.SEAMARK)) {
				testObject(Obj.PIPSOL, f-> pipelines(f));
				testObject(Obj.CBLSUB, f-> cables(f));
				testObject(Obj.PIPOHD, f-> pipelines(f));
				testObject(Obj.CBLOHD, f-> cables(f));
				testObject(Obj.TSEZNE, f-> separation(f));
				testObject(Obj.TSSCRS, f-> separation(f));
				testObject(Obj.TSSRON, f-> separation(f));
				testObject(Obj.TSELNE, f-> separation(f));
				testObject(Obj.TSSLPT, f-> separation(f));
				testObject(Obj.TSSBND, f-> separation(f));
				testObject(Obj.ISTZNE, f-> separation(f));
				testObject(Obj.SBDARE, f-> areas(f));
				testObject(Obj.SPRING, f-> areas(f));
				testObject(Obj.SNDWAV, f-> areas(f));
				testObject(Obj.WEDKLP, f-> areas(f));
				testObject(Obj.SEGRAS, f-> areas(f));
				testObject(Obj.OSPARE, f-> areas(f));
				testObject(Obj.FAIRWY, f-> areas(f));
				testObject(Obj.DRGARE, f-> areas(f));
				testObject(Obj.RESARE, f-> areas(f));
				testObject(Obj.PRCARE, f-> areas(f));
				testObject(Obj.SPLARE, f-> areas(f));
				testObject(Obj.SEAARE, f-> areas(f));
				testObject(Obj.CBLARE, f-> areas(f));
				testObject(Obj.PIPARE, f-> areas(f));
				testObject(Obj.DMPGRD, f-> areas(f));
				testObject(Obj.OBSTRN, f-> obstructions(f));
				testObject(Obj.UWTROC, f-> obstructions(f));
				testObject(Obj.MARCUL, f-> areas(f));
				testObject(Obj.RECTRC, f-> transits(f));
				testObject(Obj.NAVLNE, f-> transits(f));
				testObject(Obj.HRBFAC, f-> harbours(f));
				testObject(Obj.ACHARE, f-> harbours(f));
				testObject(Obj.ACHBRT, f-> harbours(f));
				testObject(Obj.BERTHS, f-> harbours(f));
				testObject(Obj.DISMAR, f-> distances(f));
				testObject(Obj.HULKES, f-> ports(f));
				testObject(Obj.CRANES, f-> ports(f));
				testObject(Obj.LNDMRK, f-> landmarks(f));
				testObject(Obj.SILTNK, f-> landmarks(f));
				testObject(Obj.BUISGL, f-> harbours(f));
				testObject(Obj.MORFAC, f-> moorings(f));
				testObject(Obj.NOTMRK, f-> notices(f));
				testObject(Obj.SMCFAC, f-> marinas(f));
				testObject(Obj.BRIDGE, f-> bridges(f));
				testObject(Obj.PILPNT, f-> points(f));
				testObject(Obj.TOPMAR, f-> points(f));
				testObject(Obj.DAYMAR, f-> points(f));
				testObject(Obj.FOGSIG, f-> points(f));
				testObject(Obj.RDOCAL, f-> callpoint(f));
				testObject(Obj.LITMIN, f-> lights(f));
				testObject(Obj.LITMAJ, f-> lights(f));
				testObject(Obj.LIGHTS, f-> lights(f));
				testObject(Obj.SISTAT, f-> stations(f));
				testObject(Obj.SISTAW, f-> stations(f));
				testObject(Obj.CGUSTA, f-> stations(f));
				testObject(Obj.RDOSTA, f-> stations(f));
				testObject(Obj.RADRFL, f-> stations(f));
				testObject(Obj.RADSTA, f-> stations(f));
				testObject(Obj.RTPBCN, f-> stations(f));
				testObject(Obj.RSCSTA, f-> stations(f));
				testObject(Obj.PILBOP, f-> stations(f));
				testObject(Obj.WTWGAG, f-> gauges(f));
				testObject(Obj.OFSPLF, f-> platforms(f));
				testObject(Obj.WRECKS, f-> wrecks(f));
				testObject(Obj.LITVES, f-> floats(f));
				testObject(Obj.LITFLT, f-> floats(f));
				testObject(Obj.BOYINB, f-> floats(f));
				testObject(Obj.BOYLAT, f-> buoys(f));
				testObject(Obj.BOYCAR, f-> buoys(f));
				testObject(Obj.BOYISD, f-> buoys(f));
				testObject(Obj.BOYSAW, f-> buoys(f));
				testObject(Obj.BOYSPP, f-> buoys(f));
				testObject(Obj.BCNLAT, f-> beacons(f));
				testObject(Obj.BCNCAR, f-> beacons(f));
				testObject(Obj.BCNISD, f-> beacons(f));
				testObject(Obj.BCNSAW, f-> beacons(f));
				testObject(Obj.BCNSPP, f-> beacons(f));
				testObject(Obj.VAATON, f-> virtual(f));
			}
		} catch (ConcurrentModificationException e) {
			return false;
		} catch (Exception e) {
		    e.printStackTrace();
			return true;
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	private void areas(Feature feature) {
		String name = getName(feature);
		switch (feature.type) {
		case TESARE:
			renderer.lineSymbols(feature, Areas.LimitDash, 0.0, Areas.LimitCC, null, 30, Symbols.Mline);
			break;
		case BUAARE:
			renderer.lineVector(feature, new LineStyle(new Color(0x20000000, true)));
			break;
		case COALNE:
			if (renderer.zoom >= 12)
				renderer.lineVector(feature, new LineStyle(Color.black, 10));
			break;
		case DEPARE:
			Double depmax = (Double) getAttVal(feature, Obj.DEPARE, Att.DRVAL2);
			if (depmax != null) {
				if (depmax <= 0.0) {
					renderer.lineVector(feature, new LineStyle(Symbols.Gdries));
				} else if (depmax <= 2.0) {
					renderer.lineVector(feature, new LineStyle(Color.blue, 2, new Color(0x2090ff)));
				} else if (depmax <= 5.0) {
					renderer.lineVector(feature, new LineStyle(Color.blue, 2, new Color(0x40a0ff)));
				} else if (depmax <= 10.0) {
					renderer.lineVector(feature, new LineStyle(Color.blue, 2, new Color(0x60b0ff)));
				} else if (depmax <= 15.0) {
					renderer.lineVector(feature, new LineStyle(Color.blue, 2, new Color(0x80c0ff)));
				} else if (depmax <= 20.0) {
					renderer.lineVector(feature, new LineStyle(Color.blue, 2, new Color(0xa0d0ff)));
				} else if (depmax <= 50.0) {
					renderer.lineVector(feature, new LineStyle(Color.blue, 2, new Color(0xc0e0ff)));
				} else {
					renderer.lineVector(feature, new LineStyle(Color.blue, 2, new Color(0xe0f0ff)));
				}
			}
			break;
		case CANALS:
		case LAKARE:
		case RIVERS:
			if ((renderer.zoom >= 12) || (feature.geom.area > 10.0))
				renderer.lineVector(feature, new LineStyle(Symbols.Bwater, 11, Symbols.Bwater));
			break;
		case DRGARE:
			if (renderer.zoom < 16)
				renderer.lineVector(feature, new LineStyle(Color.black, 8, new float[] { 25, 25 }, new Color(0x40ffffff, true)));
			else
				renderer.lineVector(feature, new LineStyle(Color.black, 8, new float[] { 25, 25 }));
			addName(feature, 12, new Font("Arial", Font.PLAIN, 100), new Delta(Handle.CC, new AffineTransform()));
			break;
		case FAIRWY:
            if (renderer.zoom >= 12) {
                if (feature.geom.area > 1.0) {
                    if (renderer.zoom < 16)
                        renderer.lineVector(feature, new LineStyle(new Color(0x20ffffff, true)));
                    else
                        renderer.lineVector(feature, new LineStyle(Symbols.Mline, 8, new float[] { 50, 50 }));
                } else {
                    if (renderer.zoom >= 14)
                        renderer.lineVector(feature, new LineStyle(new Color(0x20ffffff, true)));
                }
            }
			break;
		case LKBSPT:
		case LOKBSN:
		case HRBBSN:
			if (renderer.zoom >= 12) {
				renderer.lineVector(feature, new LineStyle(Color.black, 10, Symbols.Bwater));
			} else {
				renderer.lineVector(feature, new LineStyle(Symbols.Bwater));
			}
			break;
		case HRBFAC:
			if (feature.objs.get(Obj.HRBBSN) != null) {
				if (renderer.zoom >= 12) {
					renderer.lineVector(feature, new LineStyle(Color.black, 10, Symbols.Bwater));
				} else {
					renderer.lineVector(feature, new LineStyle(Symbols.Bwater));
				}
			}
			break;
		case LNDARE:
			renderer.lineVector(feature, new LineStyle(Symbols.Yland));
			break;
		case MARCUL:
			if (renderer.zoom >= 12) {
				if (renderer.zoom >= 14) {
					renderer.symbol(feature, Areas.MarineFarm);
				}
				if ((feature.geom.area > 0.2) || ((feature.geom.area > 0.05) && (renderer.zoom >= 14)) || ((feature.geom.area > 0.005) && (renderer.zoom >= 16))) {
					renderer.lineVector(feature, new LineStyle(Color.black, 4, new float[] { 10, 10 }));
				}
			}
			break;
		case OSPARE:
			if (testAttribute(feature, feature.type, Att.CATOPA, CatOPA.OPA_WIND)) {
				renderer.symbol(feature, Areas.WindFarm);
				renderer.lineVector(feature, new LineStyle(Color.black, 12, new float[] { 40, 40 }));
				addName(feature, 15, new Font("Arial", Font.BOLD, 80), new Delta(Handle.TC, AffineTransform.getTranslateInstance(0, 120)));
			}
			break;
		case RESARE:
		case MIPARE:
		case DMPGRD:
			if (renderer.zoom >= 12) {
				renderer.lineSymbols(feature, Areas.Restricted, 1.0, null, null, 0, Symbols.Mline);
				if (testAttribute(feature, feature.type, Att.CATREA, CatREA.REA_NWAK)) {
					renderer.symbol(feature, Areas.NoWake);
				}
			}
			break;
		case PRCARE:
			if (renderer.zoom >= 12) {
				renderer.lineVector(feature, new LineStyle(Symbols.Mline, 10, new float[] { 40, 40 }));
			}
			break;
		case SEAARE:
			switch ((CatSEA) getAttEnum(feature, feature.type, Att.CATSEA)) {
			case SEA_RECH:
				if ((renderer.zoom >= 15) && (name != null))
					if (feature.geom.prim == Pflag.LINE) {
						renderer.lineText(feature, name, new Font("Arial", Font.PLAIN, 60), Color.black, -40);
					} else {
						renderer.labelText(feature, name, new Font("Arial", Font.PLAIN, 60), Color.black, new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, 0)));
					}
				break;
			case SEA_BAY:
				if ((renderer.zoom >= 15) && (name != null))
					if (feature.geom.prim == Pflag.LINE) {
						renderer.lineText(feature, name, new Font("Arial", Font.PLAIN, 60), Color.black, -40);
					} else {
						renderer.labelText(feature, name, new Font("Arial", Font.PLAIN, 60), Color.black, new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, 0)));
					}
				break;
			case SEA_SHOL:
				if (renderer.zoom >= 14) {
					if (feature.geom.prim == Pflag.AREA) {
						renderer.lineVector(feature, new LineStyle(new Color(0xc480ff), 4, new float[] { 25, 25 }));
						if (name != null) {
							renderer.labelText(feature, name, new Font("Arial", Font.ITALIC, 75), Color.black, new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, -40)));
							renderer.labelText(feature, "(Shoal)", new Font("Arial", Font.PLAIN, 60), Color.black, new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, 20)));
						}
					} else if (feature.geom.prim == Pflag.LINE) {
						if (name != null) {
							renderer.lineText(feature, name, new Font("Arial", Font.ITALIC, 75), Color.black, -40);
							renderer.lineText(feature, "(Shoal)", new Font("Arial", Font.PLAIN, 60), Color.black, 20);
						}
					} else {
						if (name != null) {
							renderer.labelText(feature, name, new Font("Arial", Font.ITALIC, 75), Color.black, new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, -40)));
							renderer.labelText(feature, "(Shoal)", new Font("Arial", Font.PLAIN, 60), Color.black, new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, 20)));
						}
					}
				}
				break;
			case SEA_GAT:
			case SEA_NRRW:
				addName(feature, 12, new Font("Arial", Font.PLAIN, 100));
				break;
			default:
				break;
			}
			break;
		case SNDWAV:
			if (renderer.zoom >= 12)
				renderer.fillPattern(feature, Areas.Sandwaves);
			break;
		case SBDARE:
			if (renderer.zoom >= 14) {
				String str = "";
				String sep = ".";
				if (hasAttribute(feature, feature.type, Att.NATSUR)) {
					ArrayList<NatSUR> surs = (ArrayList<NatSUR>) getAttList(feature, feature.type, Att.NATSUR);
					ArrayList<NatQUA> quas = new ArrayList<NatQUA>();
					if (hasAttribute(feature, feature.type, Att.NATQUA)) {
						quas = (ArrayList<NatQUA>) getAttList(feature, feature.type, Att.NATQUA);
					}
					for (int i = 0; i < surs.size(); i++) {
						if (!str.isEmpty()) {
							str += sep;
							sep = ".";
						}
						if (quas.size() > i) {
							switch (quas.get(i)) {
							case QUA_FINE:
								str += "f";
								break;
							case QUA_MEDM:
								str += "m";
								break;
							case QUA_CORS:
								str += "c";
								break;
							case QUA_BRKN:
								str += "bk";
								break;
							case QUA_STKY:
								str += "sy";
								break;
							case QUA_SOFT:
								str += "so";
								break;
							case QUA_STIF:
								str += "sf";
								break;
							case QUA_VCNC:
								str += "v";
								break;
							case QUA_CALC:
								str += "ca";
								break;
							case QUA_HARD:
								str += "h";
								break;
							default:
								break;
							}
						}
						switch (surs.get(i)) {
						case SUR_MUD:
							str += "M";
							break;
						case SUR_CLAY:
							str += "Cy";
							break;
						case SUR_SILT:
							str += "Si";
							break;
						case SUR_SAND:
							str += "S";
							break;
						case SUR_STON:
							str += "St";
							break;
						case SUR_GRVL:
							str += "G";
							break;
						case SUR_PBBL:
							str += "P";
							break;
						case SUR_CBBL:
							str += "Cb";
							break;
						case SUR_ROCK:
							str += "R";
							break;
						case SUR_LAVA:
							str += "Lv";
							break;
						case SUR_CORL:
							str += "Co";
							break;
						case SUR_SHEL:
							str += "Sh";
							break;
						case SUR_BLDR:
							str += "Bo";
							break;
						default:
							str = str.substring(0, str.length() - 1) + "/";
							sep = "";
							break;
						}
					}
					if (!str.isEmpty()) {
						renderer.labelText(feature, str, new Font("Arial", Font.ITALIC, 40), Color.black, new Delta(Handle.CC));
					}
				}
			}
			break;
		case WEDKLP:
			if (renderer.zoom >= 14) {
				switch ((CatWED) getAttEnum(feature, feature.type, Att.CATWED)) {
				case WED_KELP:
					if (feature.geom.prim == Pflag.AREA) {
						renderer.fillPattern(feature, Areas.KelpA);
					} else {
						renderer.symbol(feature, Areas.KelpS);
					}
					break;
				case WED_SWED:
					renderer.labelText(feature, "Wd", new Font("Arial", Font.ITALIC, 40), Color.black, new Delta(Handle.CC));
					break;
				case WED_SGRS:
					renderer.labelText(feature, "Sg", new Font("Arial", Font.ITALIC, 40), Color.black, new Delta(Handle.CC));
					break;
				case WED_SGSO:
					break;
				default:
					break;
				}
			}
			break;
		case SEGRAS:
			renderer.labelText(feature, "Sg", new Font("Arial", Font.ITALIC, 40), Color.black, new Delta(Handle.CC));
			break;
		case SPRING:
			renderer.symbol(feature, Areas.Spring);
			break;
		case SPLARE:
			if (renderer.zoom >= 12) {
				renderer.symbol(feature, Areas.Plane, new Scheme(Symbols.Msymb));
				renderer.lineSymbols(feature, Areas.Restricted, 0.5, Areas.LinePlane, null, 10, Symbols.Mline);
				addName(feature, 15, new Font("Arial", Font.BOLD, 80), new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, -90)));
			}
			break;
		case CBLARE:
			if (renderer.zoom >= 12) {
			renderer.lineSymbols(feature, Areas.Restricted, 1.0, Areas.Cable, null, 4, Symbols.Mline);
			}
			break;
		case PIPARE:
			if (renderer.zoom >= 12) {
			renderer.lineSymbols(feature, Areas.Restricted, 1.0, Areas.Pipeline, null, 4, Symbols.Mline);
			break;
			}
		default:
			break;
		}
	}

	@SuppressWarnings("unchecked")
	private void beacons(Feature feature) {
		if ((renderer.zoom >= 14) || ((renderer.zoom >= 12) && ((feature.type == Obj.BCNLAT) || (feature.type == Obj.BCNCAR)))
				|| ((renderer.zoom >= 11) && ((feature.type == Obj.BCNSAW) || hasObject(feature, Obj.RTPBCN)))) {
			if (testAttribute(feature, feature.type, Att.STATUS, StsSTS.STS_ILLD)) {
				renderer.symbol(feature, Beacons.Floodlight);
			}
			BcnSHP shape = (BcnSHP) getAttEnum(feature, feature.type, Att.BCNSHP);
			if (shape == BcnSHP.BCN_UNKN)
				shape = BcnSHP.BCN_PILE;
			if ((shape == BcnSHP.BCN_WTHY) && (feature.type == Obj.BCNLAT)) {
				switch ((CatLAM) getAttEnum(feature, feature.type, Att.CATLAM)) {
				case LAM_PORT:
					renderer.symbol(feature, Beacons.WithyPort);
					break;
				case LAM_STBD:
					renderer.symbol(feature, Beacons.WithyStarboard);
					break;
				default:
					renderer.symbol(feature, Beacons.Stake, getScheme(feature, feature.type));
				}
			} else if (shape == BcnSHP.BCN_PRCH && feature.type == Obj.BCNLAT && !feature.objs.containsKey(Obj.TOPMAR)) {
				switch ((CatLAM) getAttEnum(feature, feature.type, Att.CATLAM)) {
				case LAM_PORT:
					renderer.symbol(feature, Beacons.PerchPort);
					break;
				case LAM_STBD:
					renderer.symbol(feature, Beacons.PerchStarboard);
					break;
				default:
					renderer.symbol(feature, Beacons.Stake, getScheme(feature, feature.type));
				}
			} else {
				renderer.symbol(feature, Beacons.Shapes.get(shape), getScheme(feature, feature.type));
				if (feature.objs.containsKey(Obj.TOPMAR)) {
					AttMap topmap = feature.objs.get(Obj.TOPMAR).get(0);
					if (testAttribute(feature, Obj.TOPMAR, Att.STATUS, StsSTS.STS_ILLD)) {
						renderer.symbol(feature, Beacons.Floodlight);
					}
					if (topmap.containsKey(Att.TOPSHP)) {
						renderer.symbol(feature, Topmarks.Shapes.get(((ArrayList<TopSHP>) topmap.get(Att.TOPSHP).val).get(0)), getScheme(feature, Obj.TOPMAR), Topmarks.BeaconDelta);
					}
				} else if (feature.objs.containsKey(Obj.DAYMAR)) {
					AttMap topmap = feature.objs.get(Obj.DAYMAR).get(0);
					if (topmap.containsKey(Att.TOPSHP)) {
						renderer.symbol(feature, Topmarks.Shapes.get(((ArrayList<TopSHP>) topmap.get(Att.TOPSHP).val).get(0)), getScheme(feature, Obj.DAYMAR), Topmarks.BeaconDelta);
					}
				}
			}
			if (hasObject(feature, Obj.NOTMRK))
				notices(feature);
			addName(feature, 15, new Font("Arial", Font.BOLD, 40), new Delta(Handle.BL, AffineTransform.getTranslateInstance(60, -50)));
			signals.addSignals(feature);
		}
	}

	@SuppressWarnings("unchecked")
	private void buoys(Feature feature) {
		if ((renderer.zoom >= 14) || ((renderer.zoom >= 12) && ((feature.type == Obj.BOYLAT) || (feature.type == Obj.BOYCAR))) || ((renderer.zoom >= 11) && ((feature.type == Obj.BOYSAW) || hasObject(feature, Obj.RTPBCN)))) {
			BoySHP shape = (BoySHP) getAttEnum(feature, feature.type, Att.BOYSHP);
			if (shape == BoySHP.BOY_UNKN)
				shape = BoySHP.BOY_PILR;
			renderer.symbol(feature, Buoys.Shapes.get(shape), getScheme(feature, feature.type));
			if (feature.objs.containsKey(Obj.TOPMAR)) {
				AttMap topmap = feature.objs.get(Obj.TOPMAR).get(0);
				if (topmap.containsKey(Att.TOPSHP)) {
					renderer.symbol(feature, Topmarks.Shapes.get(((ArrayList<TopSHP>) topmap.get(Att.TOPSHP).val).get(0)), getScheme(feature, Obj.TOPMAR), Topmarks.BuoyDeltas.get(shape));
				}
			} else if (feature.objs.containsKey(Obj.DAYMAR)) {
				AttMap topmap = feature.objs.get(Obj.DAYMAR).get(0);
				if (topmap.containsKey(Att.TOPSHP)) {
					renderer.symbol(feature, Topmarks.Shapes.get(((ArrayList<TopSHP>) topmap.get(Att.TOPSHP).val).get(0)), getScheme(feature, Obj.DAYMAR), Topmarks.BuoyDeltas.get(shape));
				}
			}
			addName(feature, 15, new Font("Arial", Font.BOLD, 40), new Delta(Handle.BL, AffineTransform.getTranslateInstance(60, -50)));
			signals.addSignals(feature);
		}
	}

	private void bridges(Feature feature) {
		if (renderer.zoom >= 16) {
			double verclr, verccl, vercop, horclr;
			AttMap atts = feature.objs.get(Obj.BRIDGE).get(0);
			String vstr = "";
			String hstr = "";
			if (atts != null) {
				if (atts.containsKey(Att.HORCLR)) {
					horclr = (Double) atts.get(Att.HORCLR).val;
					hstr = String.valueOf(horclr);
				}
				if (atts.containsKey(Att.VERCLR)) {
					verclr = (Double) atts.get(Att.VERCLR).val;
				} else {
					verclr = atts.containsKey(Att.VERCSA) ? (Double) atts.get(Att.VERCSA).val : 0;
				}
				verccl = atts.containsKey(Att.VERCCL) ? (Double) atts.get(Att.VERCCL).val : 0;
				vercop = atts.containsKey(Att.VERCOP) ? (Double) atts.get(Att.VERCOP).val : 0;
				if (verclr > 0) {
					vstr += String.valueOf(verclr);
				} else if (verccl > 0) {
					if (vercop == 0) {
						vstr += String.valueOf(verccl) + "/-";
					} else {
						vstr += String.valueOf(verccl) + "/" + String.valueOf(vercop);
					}
				}
				if (hstr.isEmpty() && !vstr.isEmpty()) {
					renderer.labelText(feature, vstr, new Font("Arial", Font.PLAIN, 30), Color.black, LabelStyle.VCLR, Color.black, Color.white, new Delta(Handle.CC));
				} else if (!hstr.isEmpty() && !vstr.isEmpty()) {
					renderer.labelText(feature, vstr, new Font("Arial", Font.PLAIN, 30), Color.black, LabelStyle.VCLR, Color.black, Color.white, new Delta(Handle.BC));
					renderer.labelText(feature, hstr, new Font("Arial", Font.PLAIN, 30), Color.black, LabelStyle.HCLR, Color.black, Color.white, new Delta(Handle.TC));
				} else if (!hstr.isEmpty() && vstr.isEmpty()) {
					renderer.labelText(feature, hstr, new Font("Arial", Font.PLAIN, 30), Color.black, LabelStyle.HCLR, Color.black, Color.white, new Delta(Handle.CC));
				}
			}
            signals.addSignals(feature);
		}
	}

	private void cables(Feature feature) {
		if (((renderer.zoom >= 14) && (feature.geom.length > 2) && (feature.geom.length < 20)) || ((renderer.zoom >= 16) && (feature.geom.length <= 2))) {
			if (feature.type == Obj.CBLSUB) {
				renderer.lineSymbols(feature, Areas.Cable, 0.0, null, null, 0, Symbols.Mline);
			} else if (feature.type == Obj.CBLOHD) {
				AttMap atts = feature.objs.get(Obj.CBLOHD).get(0);
				if (atts != null && atts.containsKey(Att.CATCBL) && atts.get(Att.CATCBL).val == CatCBL.CBL_POWR) {
					renderer.lineSymbols(feature, Areas.CableDash, 0, Areas.CableDot, Areas.CableFlash, 2, Color.black);
				} else {
					renderer.lineSymbols(feature, Areas.CableDash, 0, Areas.CableDot, null, 2, Color.black);
				}
				if (atts != null) {
					if (atts.containsKey(Att.VERCLR)) {
						renderer.labelText(feature, String.valueOf(atts.get(Att.VERCLR).val), new Font("Arial", Font.PLAIN, 50), Color.black, LabelStyle.VCLR, Color.black, new Delta(Handle.TC, AffineTransform.getTranslateInstance(0, 25)));
					} else if (atts.containsKey(Att.VERCSA)) {
						renderer.labelText(feature, String.valueOf(atts.get(Att.VERCSA).val), new Font("Arial", Font.PLAIN, 50), Color.black, LabelStyle.PCLR, Color.black, new Delta(Handle.TC, AffineTransform.getTranslateInstance(0, 25)));
					}
				}
			}
		}
	}

	private void callpoint(Feature feature) {
		if (renderer.zoom >= 14) {
			Symbol symb = Harbours.CallPoint2;
			TrfTRF trf = (TrfTRF) getAttEnum(feature, feature.type, Att.TRAFIC);
			if (trf != TrfTRF.TRF_TWOW) {
				symb = Harbours.CallPoint1;
			}
			Double orient = 0.0;
			if ((orient = (Double) getAttVal(feature, feature.type, Att.ORIENT)) == null) {
				orient = 0.0;
			}
			renderer.symbol(feature, symb, new Delta(Handle.CC, AffineTransform.getRotateInstance(Math.toRadians(orient))));
			String chn;
			if (!(chn = getAttStr(feature, feature.type, Att.COMCHA)).isEmpty()) {
				renderer.labelText(feature, ("Ch." + chn), new Font("Arial", Font.PLAIN, 50), Color.black, new Delta(Handle.TC, AffineTransform.getTranslateInstance(0, 50)));
			}
		}
	}

	private void depths(Feature feature) {
		switch (feature.type) {
		case SOUNDG:
			if (testAttribute(feature, Obj.SOUNDG, Att.TECSOU, TecSOU.SOU_COMP) && hasAttribute(feature, Obj.SOUNDG, Att.VALSOU)) {
				double depth = (double) getAttVal(feature, Obj.SOUNDG, Att.VALSOU);
				Color col = new Color(0x00ffffff, true);
				if (depth > 0.0) col =  new Color(0x2090ff);
				if (depth > 2.0) col =  new Color(0x40a0ff);
				if (depth > 5.0) col =  new Color(0x60b0ff);
				if (depth > 10.0) col = new Color(0x80c0ff);
				if (depth > 15.0) col = new Color(0xa0d0ff);
				if (depth > 20.0) col = new Color(0xc0e0ff);
				if (depth > 50.0) col = new Color(0xe0f0ff);
				renderer.rasterPixel(feature, Math.toRadians(1.0/60.0/16.0), col);
			} else if ((renderer.zoom >= 14) && hasAttribute(feature, Obj.SOUNDG, Att.VALSOU)) {
				double depth = (double) getAttVal(feature, Obj.SOUNDG, Att.VALSOU);
				String dstr = df.format(depth);
				String[] tok = dstr.split("[-.]");
				String ul = "";
				String id = tok[0];
				String dd = "";
				if (tok[0].equals("")) {
					for (int i = 0; i < tok[1].length(); i++) {
						ul += "_";
					}
					id = tok[1];
					dd = (tok.length == 3) ? tok[2] : "";
				} else {
					dd = (tok.length == 2) ? tok[1] : "";
				}
				renderer.labelText(feature, ul, new Font("Arial", Font.PLAIN, 30), Color.black, new Delta(Handle.RC, AffineTransform.getTranslateInstance(10, 15)));
				renderer.labelText(feature, id, new Font("Arial", Font.PLAIN, 30), Color.black, new Delta(Handle.RC, AffineTransform.getTranslateInstance(10, 0)));
				renderer.labelText(feature, dd, new Font("Arial", Font.PLAIN, 20), Color.black, new Delta(Handle.LC, AffineTransform.getTranslateInstance(15, 10)));
			}
			break;
		case DEPCNT:
			renderer.lineVector(feature, new LineStyle(Color.blue, 2));
			break;
		default:
			break;
		}
	}

	private void distances(Feature feature) {
		if (renderer.zoom >= 14) {
			if (!testAttribute(feature, Obj.DISMAR, Att.CATDIS, CatDIS.DIS_NONI)) {
				renderer.symbol(feature, Harbours.DistanceI);
			} else {
				renderer.symbol(feature, Harbours.DistanceU);
			}
			if (renderer.zoom >= 15) {
				AttMap atts = getAtts(feature, Obj.DISMAR, 0);
				if (atts != null && atts.containsKey(Att.WTWDIS)) {
					Double dist = (Double) atts.get(Att.WTWDIS).val;
					String str = "";
					if (atts.containsKey(Att.HUNITS)) {
						switch ((UniHLU) getAttEnum(feature, Obj.DISMAR, Att.HUNITS)) {
						case HLU_METR:
							str += "m ";
							break;
						case HLU_FEET:
							str += "ft ";
							break;
						case HLU_HMTR:
							str += "hm ";
							break;
						case HLU_KMTR:
							str += "km ";
							break;
						case HLU_SMIL:
							str += "M ";
							break;
						case HLU_NMIL:
							str += "NM ";
							break;
						default:
							break;
						}
					}
					str += String.format("%3.1f", dist);
					renderer.labelText(feature, str, new Font("Arial", Font.PLAIN, 40), Color.black, new Delta(Handle.CC, AffineTransform.getTranslateInstance(0, 45)));
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void floats(Feature feature) {
		if ((renderer.zoom >= 12) || ((renderer.zoom >= 11) && ((feature.type == Obj.LITVES) || (feature.type == Obj.BOYINB) || hasObject(feature, Obj.RTPBCN)))) {
			switch (feature.type) {
			case LITVES:
				renderer.symbol(feature, Buoys.Super, getScheme(feature, feature.type));
				break;
			case LITFLT:
				renderer.symbol(feature, Buoys.Float, getScheme(feature, feature.type));
				break;
			case BOYINB:
				renderer.symbol(feature, Buoys.Super, getScheme(feature, feature.type));
				break;
			default:
				break;
			}
			if (feature.objs.containsKey(Obj.TOPMAR)) {
				AttMap topmap = feature.objs.get(Obj.TOPMAR).get(0);
				if (topmap.containsKey(Att.TOPSHP)) {
					renderer.symbol(feature, Topmarks.Shapes.get(((ArrayList<TopSHP>) topmap.get(Att.TOPSHP).val).get(0)), getScheme(feature, Obj.TOPMAR), Topmarks.FloatDelta);
				}
			} else if (feature.objs.containsKey(Obj.DAYMAR)) {
				AttMap topmap = feature.objs.get(Obj.DAYMAR).get(0);
				if (topmap.containsKey(Att.TOPSHP)) {
					renderer.symbol(feature, Topmarks.Shapes.get(((ArrayList<TopSHP>) topmap.get(Att.TOPSHP).val).get(0)), getScheme(feature, Obj.DAYMAR), Topmarks.FloatDelta);
				}
			}
			addName(feature, 15, new Font("Arial", Font.BOLD, 40), new Delta(Handle.BL, AffineTransform.getTranslateInstance(20, -50)));
			signals.addSignals(feature);
		}
	}

	private void gauges(Feature feature) {
		if (renderer.zoom >= 14) {
			renderer.symbol(feature, Harbours.TideGauge);
			addName(feature, 15, new Font("Arial", Font.BOLD, 40), new Delta(Handle.BL, AffineTransform.getTranslateInstance(20, -50)));
			signals.addSignals(feature);
		}
	}

	@SuppressWarnings("unchecked")
	private void harbours(Feature feature) {
		String name = getName(feature);
		switch (feature.type) {
		case ACHBRT:
			if (renderer.zoom >= 14) {
				renderer.symbol(feature, Harbours.Anchor, new Scheme(Symbols.Msymb));
				if (renderer.zoom >= 15) {
					renderer.labelText(feature, name == null ? "" : name, new Font("Arial", Font.PLAIN, 30), Symbols.Msymb, LabelStyle.RRCT, Symbols.Msymb, Color.white, new Delta(Handle.BC));
				}
			}
			if (getAttVal(feature, Obj.ACHBRT, Att.RADIUS) != null) {
				double radius;
				if ((radius = (Double) getAttVal(feature, Obj.ACHBRT, Att.RADIUS)) != 0) {
					UniHLU units = (UniHLU) getAttEnum(feature, Obj.ACHBRT, Att.HUNITS);
					if (units == UniHLU.HLU_UNKN) {
						units = UniHLU.HLU_METR;
					}
					renderer.lineCircle(feature, new LineStyle(Symbols.Mline, 4, new float[] { 10, 10 }, null), radius, units);
				}
			}
			break;
		case ACHARE:
			if (renderer.zoom >= 12) {
				ArrayList<CatACH> cats = (ArrayList<CatACH>) getAttList(feature, Obj.ACHARE, Att.CATACH);
				if (feature.geom.prim != Pflag.AREA) {
					renderer.symbol(feature, Harbours.Anchorage, new Scheme(Color.black));
				} else {
					if (cats.contains(CatACH.ACH_SMCM)) {
						renderer.symbol(feature, Buoys.Shapes.get(BoySHP.BOY_SPHR), new Scheme(Symbols.Msymb));
			        	renderer.symbol(feature, Topmarks.TopMooring, Topmarks.BuoyDeltas.get(BoySHP.BOY_SPHR));
					} else {
						renderer.symbol(feature, Harbours.Anchorage, new Scheme(Symbols.Mline));
					}
					renderer.lineSymbols(feature, Areas.Restricted, 1.0, Areas.LineAnchor, null, 10, Symbols.Mline);
				}
				addName(feature, 15, new Font("Arial", Font.BOLD, 60), Symbols.Mline, new Delta(Handle.LC, AffineTransform.getTranslateInstance(70, 0)));
				ArrayList<StsSTS> sts = (ArrayList<StsSTS>) getAttList(feature, Obj.ACHARE, Att.STATUS);
				if (renderer.zoom >= 15 && sts.contains(StsSTS.STS_RESV)) {
					renderer.labelText(feature, "Reserved", new Font("Arial", Font.PLAIN, 50), Symbols.Mline, new Delta(Handle.TC, AffineTransform.getTranslateInstance(0, 60)));
				}
				int dy = (cats.size() - 1) * -30;
				for (CatACH cat : cats) {
					switch (cat) {
					case ACH_DEEP:
						renderer.labelText(feature, "DW", new Font("Arial", Font.BOLD, 50), Symbols.Msymb, new Delta(Handle.RC, AffineTransform.getTranslateInstance(-60, dy)));
						dy += 60;
						break;
					case ACH_TANK:
						renderer.labelText(feature, "Tanker", new Font("Arial", Font.BOLD, 50), Symbols.Msymb, new Delta(Handle.RC, AffineTransform.getTranslateInstance(-60, dy)));
						dy += 60;
						break;
					case ACH_H24P:
						renderer.labelText(feature, "24h", new Font("Arial", Font.BOLD, 50), Symbols.Msymb, new Delta(Handle.RC, AffineTransform.getTranslateInstance(-60, dy)));
						dy += 60;
						break;
					case ACH_EXPL:
						renderer.symbol(feature, Harbours.Explosives, new Scheme(Symbols.Msymb), new Delta(Handle.RC, AffineTransform.getTranslateInstance(-60, dy)));
						dy += 60;
						break;
					case ACH_QUAR:
						renderer.symbol(feature, Harbours.Hospital, new Scheme(Symbols.Msymb), new Delta(Handle.RC, AffineTransform.getTranslateInstance(-60, dy)));
						dy += 60;
						break;
					case ACH_SEAP:
						renderer.symbol(feature, Areas.Seaplane, new Scheme(Symbols.Msymb), new Delta(Handle.RC, AffineTransform.getTranslateInstance(-60, dy)));
						dy += 60;
						break;
					case ACH_SMCF:
					case ACH_SMCM:
						renderer.labelText(feature, "Small", new Font("Arial", Font.PLAIN, 40), Symbols.Msymb, new Delta(Handle.RC, AffineTransform.getTranslateInstance(-60, dy)));
						renderer.labelText(feature, "Craft", new Font("Arial", Font.PLAIN, 40), Symbols.Msymb, new Delta(Handle.LC, AffineTransform.getTranslateInstance(60, dy)));
						dy += 60;
						break;
					default:
					}
				}
			}
			break;
		case BERTHS:
			if (renderer.zoom >= 14) {
				renderer.lineVector(feature, new LineStyle(Symbols.Mline, 6, new float[] { 20, 20 }));
				renderer.labelText(feature, name == null ? " " : name, new Font("Arial", Font.PLAIN, 40), Symbols.Msymb, LabelStyle.RRCT, Symbols.Mline, Color.white);
			}
			break;
        case BUISGL:
            if (renderer.zoom >= 15) {
                renderer.lineVector(feature, new LineStyle(Color.black, 8, new Color(0xffc0c0c0, true)));
                if (testAttribute(feature, Obj.BUISGL, Att.FUNCTN, FncFNC.FNC_LOOK)) {
                    renderer.labelText(feature, "Lookout", new Font("Arial", Font.PLAIN, 40), Color.black, new Delta(Handle.CC, AffineTransform.getTranslateInstance(0, 50)));
                    addName(feature, 15, new Font("Arial", Font.BOLD, 40), new Delta(Handle.CC, AffineTransform.getTranslateInstance(0, -50)));
                }
                if (renderer.zoom >= 16) {
                	if (testAttribute(feature, Obj.BUISGL, Att.STATUS, StsSTS.STS_ILLD)) {
                		renderer.symbol(feature, Beacons.Floodlight);
                	}
                	ArrayList<Symbol> symbols = new ArrayList<>();
                	ArrayList<FncFNC> fncs = (ArrayList<FncFNC>) getAttList(feature, Obj.BUISGL, Att.FUNCTN);
                	for (FncFNC fnc : fncs) {
                		symbols.add(Landmarks.Funcs.get(fnc));
                	}
                	if (feature.objs.containsKey(Obj.SMCFAC)) {
                		ArrayList<CatSCF> scfs = (ArrayList<CatSCF>) getAttList(feature, Obj.SMCFAC, Att.CATSCF);
                		for (CatSCF scf : scfs) {
                			symbols.add(Facilities.Cats.get(scf));
                		}
                	}
                	renderer.cluster(feature, symbols);
                	signals.addSignals(feature);
                }
            }
            break;
		case HRBFAC:
			if (renderer.zoom >= 12) {
				ArrayList<CatHAF> cathaf = (ArrayList<CatHAF>) getAttList(feature, Obj.HRBFAC, Att.CATHAF);
				if (cathaf.size() == 1) {
					switch (cathaf.get(0)) {
					case HAF_MRNA:
						renderer.symbol(feature, Harbours.Marina);
						break;
					case HAF_MANF:
						renderer.symbol(feature, Harbours.MarinaNF);
						break;
					case HAF_FISH:
						renderer.symbol(feature, Harbours.Fishing);
						break;
					default:
						renderer.symbol(feature, Harbours.Harbour);
						break;
					}
				} else {
					renderer.symbol(feature, Harbours.Harbour);
				}
                addName(feature, 15, new Font("Arial", Font.BOLD, 40), new Delta(Handle.CC, AffineTransform.getTranslateInstance(0, -80)));
			}
			break;
		default:
			break;
		}
	}

	@SuppressWarnings("unchecked")
	private void highways(Feature feature) {
		switch (feature.type) {
		case ROADWY:
			ArrayList<CatROD> cat = (ArrayList<CatROD>) getAttList(feature, Obj.ROADWY, Att.CATROD);
			if (cat.size() > 0) {
				switch (cat.get(0)) {
				case ROD_MWAY:
					renderer.lineVector(feature, new LineStyle(Color.black, 20));
					break;
				case ROD_MAJR:
					renderer.lineVector(feature, new LineStyle(Color.black, 15));
					break;
				case ROD_MINR:
					renderer.lineVector(feature, new LineStyle(Color.black, 10));
					break;
				default:
					renderer.lineVector(feature, new LineStyle(Color.black, 5));
				}
			} else {
				renderer.lineVector(feature, new LineStyle(Color.black, 5));
			}
			break;
		case RAILWY:
			renderer.lineVector(feature, new LineStyle(Color.gray, 10));
			renderer.lineVector(feature, new LineStyle(Color.black, 10, new float[] { 30, 30 }));
			break;
		default:
		}
	}

	@SuppressWarnings("unchecked")
	private void landmarks(Feature feature) {
		if (!hasAttribute(feature, Obj.LNDMRK, Att.CATLMK) && (!hasAttribute(feature, Obj.LNDMRK, Att.FUNCTN) || testAttribute(feature, Obj.LNDMRK, Att.FUNCTN, FncFNC.FNC_LGHT)) && hasObject(feature, Obj.LIGHTS)) {
			lights(feature);
		} else if (renderer.zoom >= 12) {
			switch (feature.type) {
			case LNDMRK:
				if (testAttribute(feature, Obj.LNDMRK, Att.STATUS, StsSTS.STS_ILLD)) {
					renderer.symbol(feature, Beacons.Floodlight);
				}
				ArrayList<CatLMK> cats = (ArrayList<CatLMK>) getAttList(feature, feature.type, Att.CATLMK);
				Symbol catSym = Landmarks.Shapes.get(cats.get(0));
				ArrayList<FncFNC> fncs = (ArrayList<FncFNC>) getAttList(feature, feature.type, Att.FUNCTN);
				Symbol fncSym = Landmarks.Funcs.get(fncs.get(0));
				if ((fncs.get(0) == FncFNC.FNC_CHCH) && (cats.get(0) == CatLMK.LMK_TOWR))
					catSym = Landmarks.ChurchTower;
				if (cats.get(0) == CatLMK.LMK_RADR)
					fncSym = Landmarks.RadioTV;
				renderer.symbol(feature, catSym);
				renderer.symbol(feature, fncSym);
				break;
			case SILTNK:
				if (testAttribute(feature, feature.type, Att.CATSIL, CatSIL.SIL_WTRT))
					renderer.symbol(feature, Landmarks.WaterTower);
				break;
			default:
				break;
			}
			if (renderer.zoom >= 15) {
				renderer.colLetters(feature, getAttList(feature, feature.type, Att.COLOUR));
			}
			signals.addSignals(feature);
			addName(feature, 15, new Font("Arial", Font.BOLD, 40), new Delta(Handle.BL, AffineTransform.getTranslateInstance(60, -50)));
		}
	}

	@SuppressWarnings("unchecked")
	private void lights(Feature feature) {
		boolean ok = false;
		switch (feature.type) {
		case LITMAJ:
		case LNDMRK:
            renderer.symbol(feature, Beacons.LightMajor);
			if (renderer.zoom >= 12) {
				ok = true;
			} else {
			    signals.lights(feature);
			}
			break;
		case LITMIN:
		case LIGHTS:
		case PILPNT:
			if (renderer.zoom >= 14) {
				if (testAttribute(feature, Obj.LIGHTS, Att.CATLIT, CatLIT.LIT_FLDL)) {
					renderer.symbol(feature, Beacons.Floodlight, new Delta(Handle.CC, AffineTransform.getRotateInstance(Math.toRadians(90))));
					renderer.symbol(feature, Harbours.SignalStation);
				} else {
					renderer.symbol(feature, Beacons.LightMinor);
				}
				ok = true;
			}
			break;
		default:
			break;
		}
		if (ok) {
			if (feature.objs.containsKey(Obj.TOPMAR)) {
				if (testAttribute(feature, Obj.TOPMAR, Att.STATUS, StsSTS.STS_ILLD)) {
					renderer.symbol(feature, Beacons.Floodlight);
				}
				AttMap topmap = feature.objs.get(Obj.TOPMAR).get(0);
				if (topmap.containsKey(Att.TOPSHP)) {
					renderer.symbol(feature, Topmarks.Shapes.get(((ArrayList<TopSHP>) topmap.get(Att.TOPSHP).val).get(0)), getScheme(feature, Obj.TOPMAR), Topmarks.LightDelta);
				}
			} else if (feature.objs.containsKey(Obj.DAYMAR)) {
				AttMap topmap = feature.objs.get(Obj.DAYMAR).get(0);
				if (topmap.containsKey(Att.TOPSHP)) {
					renderer.symbol(feature, Topmarks.Shapes.get(((ArrayList<TopSHP>) topmap.get(Att.TOPSHP).val).get(0)), getScheme(feature, Obj.DAYMAR), Topmarks.LightDelta);
				}
			}
			signals.addSignals(feature);
			addName(feature, 15, new Font("Arial", Font.BOLD, 40), new Delta(Handle.BL, AffineTransform.getTranslateInstance(0, -50)));
		}
	}

	@SuppressWarnings("unchecked")
	private void marinas(Feature feature) {
		if (renderer.zoom >= 16) {
			ArrayList<Symbol> symbols = new ArrayList<>();
			ArrayList<CatSCF> scfs = (ArrayList<CatSCF>) getAttList(feature, Obj.SMCFAC, Att.CATSCF);
			for (CatSCF scf : scfs) {
			    Symbol sym = Facilities.Cats.get(scf);
			    if (sym != null) symbols.add(sym);
			}
			renderer.cluster(feature, symbols);
		}
	}

	private void moorings(Feature feature) {
		if (renderer.zoom >= 14) {
			switch ((CatMOR) getAttEnum(feature, feature.type, Att.CATMOR)) {
			case MOR_DLPN:
			    if (feature.geom.prim == Pflag.AREA) {
			        renderer.lineVector(feature, new LineStyle(Color.black, 4, Symbols.Yland));
			    } else {
			        renderer.symbol(feature, Harbours.Dolphin);
			    }
	            signals.addSignals(feature);
				break;
			case MOR_DDPN:
				renderer.symbol(feature, Harbours.DeviationDolphin);
	            signals.addSignals(feature);
				break;
			case MOR_BLRD:
			case MOR_POST:
				renderer.symbol(feature, Harbours.Bollard);
				break;
			case MOR_BUOY:
			    if (renderer.zoom >= 16) {
			        BoySHP shape = (BoySHP) getAttEnum(feature, feature.type, Att.BOYSHP);
			        if (shape == BoySHP.BOY_UNKN) {
			            shape = BoySHP.BOY_SPHR;
			        }
			        renderer.symbol(feature, Buoys.Shapes.get(shape), (1.0 / (1.0 + (0.25 * (18 - renderer.zoom)))), getScheme(feature, feature.type));
			        renderer.symbol(feature, Topmarks.TopMooring, (1.0 / (1.0 + (0.25 * (18 - renderer.zoom)))), Topmarks.BuoyDeltas.get(shape));
		          signals.addSignals(feature);
		          addName(feature, 15, new Font("Arial", Font.BOLD, 40), new Delta(Handle.BL, AffineTransform.getTranslateInstance(60, -50)));
			    }
				break;
			default:
				break;
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void notices(Feature feature) {
		if (renderer.zoom >= 14) {
			double dx = 0.0, dy = 0.0;
			switch (feature.type) {
			case BCNCAR:
			case BCNISD:
			case BCNLAT:
			case BCNSAW:
			case BCNSPP:
				if (testAttribute(feature, Obj.TOPMAR, Att.TOPSHP, TopSHP.TOP_BORD) || testAttribute(feature, Obj.DAYMAR, Att.TOPSHP, TopSHP.TOP_BORD)) {
					dy = -100.0;
				} else {
					dy = -45.0;
				}
				break;
			case NOTMRK:
				dy = 0.0;
				break;
			default:
				return;
			}
			MarSYS sys = MarSYS.SYS_CEVN;
			BnkWTW bnk = BnkWTW.BWW_UNKN;
			AttVal<?> att = feature.atts.get(Att.MARSYS);
			if (att != null)
				sys = (MarSYS) att.val;
			att = feature.atts.get(Att.BNKWTW);
			if (att != null)
				bnk = (BnkWTW) att.val;
			ObjTab objs = feature.objs.get(Obj.NOTMRK);
			int n = objs.size();
			if (n > 5) {
				renderer.symbol(feature, Notices.Notice, new Delta(Handle.CC, AffineTransform.getTranslateInstance(dx, dy)));
			} else {
				int i = 0;
				for (AttMap atts : objs.values()) {
					if (atts.get(Att.MARSYS) != null)
						sys = ((ArrayList<MarSYS>) atts.get(Att.MARSYS).val).get(0);
					if (atts.get(Att.BNKWTW) != null)
						bnk = ((ArrayList<BnkWTW>) atts.get(Att.BNKWTW).val).get(0);
					CatNMK cat = CatNMK.NMK_UNKN;
					if (atts.get(Att.CATNMK) != null)
						cat = ((ArrayList<CatNMK>) atts.get(Att.CATNMK).val).get(0);
					Symbol sym = Notices.getNotice(cat, sys, bnk);
					Scheme sch = Notices.getScheme(sys, bnk);
					ArrayList<AddMRK> add = new ArrayList<>();
					if (atts.get(Att.ADDMRK) != null)
						add = (ArrayList<AddMRK>) atts.get(Att.ADDMRK).val;
					Handle h = Handle.CC;
					double ax = 0.0;
					double ay = 0.0;
					switch (i) {
					case 0:
						if (n != 1)
							h = null;
						break;
					case 1:
						if (n <= 3) {
							h = Handle.RC;
							ax = -30;
							ay = dy;
						} else {
							h = Handle.BR;
						}
						break;
					case 2:
						if (n <= 3)
							h = Handle.LC;
						else
							h = Handle.BL;
						break;
					case 3:
						if (n == 4)
							h = Handle.TC;
						else
							h = Handle.TR;
						break;
					case 4:
						h = Handle.TL;
						break;
					}
					if (h != null) {
						renderer.symbol(feature, sym, sch, new Delta(h, AffineTransform.getTranslateInstance(dx, dy)));
						if (!add.isEmpty())
							renderer.symbol(feature, Notices.NoticeBoard, new Delta(Handle.BC, AffineTransform.getTranslateInstance(ax, ay - 30)));
					}
					i++;
				}
			}
		}
	}

	private void obstructions(Feature feature) {
		if ((renderer.zoom >= 12) && (feature.type == Obj.OBSTRN)) {
			if (getAttEnum(feature, feature.type, Att.CATOBS) == CatOBS.OBS_BOOM) {
				renderer.lineVector(feature, new LineStyle(Color.black, 5, new float[] { 20, 20 }, null));
				if (renderer.zoom >= 15) {
					renderer.lineText(feature, "Boom", new Font("Arial", Font.PLAIN, 40), Color.black, -20);
				}
			}
			if (getAttEnum(feature, feature.type, Att.CATOBS) == CatOBS.OBS_FLGD) {
					renderer.symbol(feature, Areas.Foul, new Scheme(Color.black));
					if (feature.geom.prim == Pflag.AREA) {
					renderer.lineSymbols(feature, Areas.Dash, 1.0, Areas.LineFoul, null, 10, Color.black);
				}
			}
		}
		if ((renderer.zoom >= 14) && (feature.type == Obj.UWTROC)) {
			switch ((WatLEV) getAttEnum(feature, feature.type, Att.WATLEV)) {
			case LEV_CVRS:
				renderer.symbol(feature, Areas.RockC);
				break;
			case LEV_AWSH:
				renderer.symbol(feature, Areas.RockA);
				break;
			default:
				renderer.symbol(feature, Areas.Rock);
				break;
			}
		}
	}

	private void pipelines(Feature feature) {
		if ((renderer.zoom >= 14)  && (feature.geom.length < 20)) {
			if (feature.type == Obj.PIPSOL) {
				switch ((CatPIP) getAttEnum(feature, feature.type, Att.CATPIP)) {
				case PIP_ITAK:
				case PIP_OFAL:
				case PIP_SEWR:
					renderer.lineSymbols(feature, Areas.Pipeline, 0.33, null, null, 0, Color.black);
					break;
				default:
					renderer.lineSymbols(feature, Areas.Pipeline, 0.33, null, null, 0, Symbols.Msymb);
				}
			} else if (feature.type == Obj.PIPOHD) {
				renderer.lineVector(feature, new LineStyle(Color.black, 8));
				AttMap atts = feature.atts;
				double verclr = 0;
				if (atts != null) {
					if (atts.containsKey(Att.VERCLR)) {
						verclr = (Double) atts.get(Att.VERCLR).val;
					} else {
						verclr = atts.containsKey(Att.VERCSA) ? (Double) atts.get(Att.VERCSA).val : 0;
					}
					if (verclr > 0) {
						renderer.labelText(feature, String.valueOf(verclr), new Font("Arial", Font.PLAIN, 50), Color.black, LabelStyle.VCLR, Color.black, new Delta(Handle.TC, AffineTransform.getTranslateInstance(0, 25)));
					}
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void platforms(Feature feature) {
		ArrayList<CatOFP> cats = (ArrayList<CatOFP>) getAttList(feature, Obj.OFSPLF, Att.CATOFP);
		if (cats.get(0) == CatOFP.OFP_FPSO)
			renderer.symbol(feature, Buoys.Storage);
		else
			renderer.symbol(feature, Landmarks.Platform);
		if (testAttribute(feature, feature.type, Att.STATUS, StsSTS.STS_ILLD)) {
			renderer.symbol(feature, Beacons.Floodlight);
		}
		addName(feature, 15, new Font("Arial", Font.BOLD, 40), new Delta(Handle.BL, AffineTransform.getTranslateInstance(20, -50)));
		signals.addSignals(feature);
	}

	@SuppressWarnings("unchecked")
	private void points(Feature feature) {
		boolean ok = false;
		switch (feature.type) {
		case FOGSIG:
			if (renderer.zoom >= 12) {
				if (feature.objs.containsKey(Obj.LIGHTS))
					lights(feature);
				else
					renderer.symbol(feature, Harbours.Post);
				ok = true;
			}
			break;
		default:
			if (renderer.zoom >= 14) {
				if (testAttribute(feature, feature.type, Att.STATUS, StsSTS.STS_ILLD)) {
					renderer.symbol(feature, Beacons.Floodlight);
				}
				if (feature.objs.containsKey(Obj.LIGHTS))
					lights(feature);
				else
					renderer.symbol(feature, Harbours.Post);
				ok = true;
			}
			break;
		}
		if (ok) {
			if (feature.objs.containsKey(Obj.TOPMAR)) {
				AttMap topmap = feature.objs.get(Obj.TOPMAR).get(0);
				if (topmap.containsKey(Att.TOPSHP)) {
					renderer.symbol(feature, Topmarks.Shapes.get(((ArrayList<TopSHP>) topmap.get(Att.TOPSHP).val).get(0)), getScheme(feature, Obj.TOPMAR), null);
				}
			} else if (feature.objs.containsKey(Obj.DAYMAR)) {
				AttMap topmap = feature.objs.get(Obj.DAYMAR).get(0);
				if (topmap.containsKey(Att.TOPSHP)) {
					renderer.symbol(feature, Topmarks.Shapes.get(((ArrayList<TopSHP>) topmap.get(Att.TOPSHP).val).get(0)), getScheme(feature, Obj.DAYMAR), null);
				}
			}
			signals.addSignals(feature);
		}
	}

	private void ports(Feature feature) {
		if (renderer.zoom >= 14) {
			if (feature.type == Obj.CRANES) {
				if ((CatCRN) getAttEnum(feature, feature.type, Att.CATCRN) == CatCRN.CRN_CONT)
					renderer.symbol(feature, Harbours.ContainerCrane);
				else
					renderer.symbol(feature, Harbours.PortCrane);
			} else if (feature.type == Obj.HULKES) {
				renderer.lineVector(feature, new LineStyle(Color.black, 4, null, new Color(0xffe000)));
				addName(feature, 15, new Font("Arial", Font.BOLD, 40));
			}
		}
	}

	private void separation(Feature feature) {
		switch (feature.type) {
		case TSEZNE:
		case TSSCRS:
		case TSSRON:
			if (renderer.zoom <= 15)
				renderer.lineVector(feature, new LineStyle(Symbols.Mtss));
			else
				renderer.lineVector(feature, new LineStyle(Symbols.Mtss, 20, null, null));
			addName(feature, 10, new Font("Arial", Font.BOLD, 150), Symbols.Mline);
			break;
		case TSELNE:
			renderer.lineVector(feature, new LineStyle(Symbols.Mtss, 20, null, null));
			break;
		case TSSLPT:
			renderer.lineSymbols(feature, Areas.LaneArrow, 0.5, null, null, 0, Symbols.Mtss);
			break;
		case TSSBND:
			renderer.lineVector(feature, new LineStyle(Symbols.Mtss, 20, new float[] { 40, 40 }, null));
			break;
		case ISTZNE:
			renderer.lineSymbols(feature, Areas.Restricted, 1.0, null, null, 0, Symbols.Mtss);
			break;
		default:
			break;
		}
	}

	@SuppressWarnings("unchecked")
	private void shoreline(Feature feature) {
		CatSLC cat = (CatSLC) getAttEnum(feature, feature.type, Att.CATSLC);
		if ((renderer.context.ruleset() == RuleSet.ALL) || (renderer.context.ruleset() == RuleSet.BASE)) {
			if ((cat != CatSLC.SLC_SWAY) && (cat != CatSLC.SLC_TWAL)) {
				if (renderer.zoom >= 12) {
					renderer.lineVector(feature, new LineStyle(Color.black, 10, Symbols.Yland));
				} else {
					renderer.lineVector(feature, new LineStyle(Symbols.Yland));
				}
			}
		}
		if ((renderer.context.ruleset() == RuleSet.ALL) || (renderer.context.ruleset() == RuleSet.SEAMARK)) {
			if (renderer.zoom >= 12) {
				switch (cat) {
				case SLC_TWAL:
					WatLEV lev = (WatLEV) getAttEnum(feature, feature.type, Att.WATLEV);
					if (lev == WatLEV.LEV_CVRS) {
						renderer.lineVector(feature, new LineStyle(Color.black, 10, new float[] { 40, 40 }, null));
						if (renderer.zoom >= 15)
							renderer.lineText(feature, "(covers)", new Font("Arial", Font.PLAIN, 40), Color.black, 80);
					} else {
						renderer.lineVector(feature, new LineStyle(Color.black, 10, null, null));
					}
					if (renderer.zoom >= 15)
						renderer.lineText(feature, "Training Wall", new Font("Arial", Font.PLAIN, 40), Color.black, -30);
					break;
				case SLC_SWAY:
					renderer.lineVector(feature, new LineStyle(Color.black, 2, null, new Color(0xffe000)));
					if ((renderer.zoom >= 16) && feature.objs.containsKey(Obj.SMCFAC)) {
						ArrayList<Symbol> symbols = new ArrayList<>();
						ArrayList<CatSCF> scfs = (ArrayList<CatSCF>) getAttList(feature, Obj.SMCFAC, Att.CATSCF);
						for (CatSCF scf : scfs) {
							symbols.add(Facilities.Cats.get(scf));
						}
						renderer.cluster(feature, symbols);
					}
					break;
				default:
					break;
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void stations(Feature feature) {
		if (renderer.zoom >= 14) {
			String str = "";
			switch (feature.type) {
			case SISTAT:
				renderer.symbol(feature, Harbours.SignalStation);
				str = "SS";
				ArrayList<CatSIT> tcats = (ArrayList<CatSIT>) getAttList(feature, Obj.SISTAT, Att.CATSIT);
				switch (tcats.get(0)) {
				case SIT_IPT:
					str += "(INT)";
					break;
				case SIT_PRTE:
					str += "(Traffic)";
					break;
				case SIT_PRTC:
					str += "(Port Control)";
					break;
				case SIT_LOCK:
					str += "(Lock)";
					break;
				case SIT_BRDG:
					str += "(Bridge)";
					break;
				default:
					break;
				}
				break;
			case SISTAW:
				renderer.symbol(feature, Harbours.SignalStation);
				str = "SS";
				str = "SS";
				ArrayList<CatSIW> wcats = (ArrayList<CatSIW>) getAttList(feature, Obj.SISTAW, Att.CATSIW);
				switch (wcats.get(0)) {
				case SIW_STRM:
					str += "(Storm)";
					break;
				case SIW_WTHR:
					str += "(Weather)";
					break;
				case SIW_ICE:
					str += "(Ice)";
					break;
				case SIW_TIDG:
					str = "Tide gauge";
					break;
				case SIW_TIDS:
					str = "Tide scale";
					break;
				case SIW_TIDE:
					str += "(Tide)";
					break;
				case SIW_TSTR:
					str += "(Stream)";
					break;
				case SIW_DNGR:
					str += "(Danger)";
					break;
				case SIW_MILY:
					str += "(Firing)";
					break;
				case SIW_TIME:
					str += "(Time)";
					break;
				default:
					break;
				}
				break;
			case RDOSTA:
			case RTPBCN:
				renderer.symbol(feature, Harbours.SignalStation);
				renderer.symbol(feature, Beacons.RadarStation);
				break;
			case RADRFL:
				renderer.symbol(feature, Topmarks.RadarReflector);
				break;
			case RADSTA:
				renderer.symbol(feature, Harbours.SignalStation);
				renderer.symbol(feature, Beacons.RadarStation);
				break;
			case PILBOP:
				renderer.symbol(feature, Harbours.Pilot);
				addName(feature, 15, new Font("Arial", Font.BOLD, 40), Symbols.Msymb, new Delta(Handle.LC, AffineTransform.getTranslateInstance(70, -40)));
				CatPIL cat = (CatPIL) getAttEnum(feature, feature.type, Att.CATPIL);
				if (cat == CatPIL.PIL_HELI) {
					renderer.labelText(feature, "H", new Font("Arial", Font.PLAIN, 40), Symbols.Msymb, new Delta(Handle.LC, AffineTransform.getTranslateInstance(70, 0)));
				}
				break;
			case CGUSTA:
				renderer.symbol(feature, Harbours.SignalStation);
				str = "CG";
				if (feature.objs.containsKey(Obj.RSCSTA))
					renderer.symbol(feature, Harbours.Rescue, new Delta(Handle.CC, AffineTransform.getTranslateInstance(130, 0)));
				break;
			case RSCSTA:
				renderer.symbol(feature, Harbours.Rescue);
				break;
			default:
				break;
			}
			if ((renderer.zoom >= 15) && !str.isEmpty()) {
				renderer.labelText(feature, str, new Font("Arial", Font.PLAIN, 40), Color.black, new Delta(Handle.CC, AffineTransform.getTranslateInstance(0, -50)));
			}
			signals.addSignals(feature);
		}
	}

	private void transits(Feature feature) {
		if (renderer.zoom >= 14) {
			if (feature.type == Obj.RECTRC)
				renderer.lineVector(feature, new LineStyle(Color.black, 5, null, null));
			else if (feature.type == Obj.NAVLNE)
				renderer.lineVector(feature, new LineStyle(Color.black, 5, new float[] { 25, 25 }, null));
		}
		if (renderer.zoom >= 15) {
			String str = "";
			String name = getName(feature);
			if (name != null)
				str += name + " ";
			Double ort;
			if ((ort = (Double) getAttVal(feature, feature.type, Att.ORIENT)) != null) {
				str += df.format(ort) + "º";
				if (!str.isEmpty())
					renderer.lineText(feature, str, new Font("Arial", Font.PLAIN, 40), Color.black, -20);
			}
		}
	}

    @SuppressWarnings("unchecked")
    private void virtual(Feature feature) {
        if (renderer.zoom >= 12) {
            renderer.symbol(feature, Harbours.SignalStation, new Scheme(Symbols.Msymb));
            renderer.symbol(feature, Beacons.RadarStation, new Scheme(Symbols.Msymb));
           ArrayList<CatVAN> cats = (ArrayList<CatVAN>) getAttList(feature, Obj.VAATON, Att.CATVAN);
            for (CatVAN van : cats) {
                switch (van) {
                case VAN_NCAR:
                    renderer.symbol(feature, Topmarks.TopNorth, new Scheme(Symbols.Msymb), new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, -25)));
                    break;
                case VAN_SCAR:
                    renderer.symbol(feature, Topmarks.TopSouth, new Scheme(Symbols.Msymb), new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, -25)));
                    break;
                case VAN_ECAR:
                    renderer.symbol(feature, Topmarks.TopEast, new Scheme(Symbols.Msymb), new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, -25)));
                    break;
                case VAN_WCAR:
                    renderer.symbol(feature, Topmarks.TopWest, new Scheme(Symbols.Msymb), new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, -25)));
                    break;
                case VAN_PLAT:
                case VAN_PCHS:
                    renderer.symbol(feature, Topmarks.TopCan, new Scheme(Symbols.Msymb), new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, -25)));
                    break;
                case VAN_SLAT:
                case VAN_PCHP:
                    renderer.symbol(feature, Topmarks.TopCone, new Scheme(Symbols.Msymb), new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, -25)));
                    break;
                case VAN_IDGR:
                    renderer.symbol(feature, Topmarks.TopIsol, new Scheme(Symbols.Msymb), new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, -25)));
                    break;
                case VAN_SAFW:
                    renderer.symbol(feature, Topmarks.TopSphere, new Scheme(Symbols.Msymb), new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, -25)));
                    break;
                case VAN_SPPM:
                    renderer.symbol(feature, Topmarks.TopX, new Scheme(Symbols.Msymb), new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, -25)));
                    break;
                case VAN_WREK:
                    renderer.symbol(feature, Topmarks.TopCross, new Scheme(Symbols.Msymb), new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, -25)));
                    break;
                default:
                    break;
                }
            }
        }
        addName(feature, 15, new Font("Arial", Font.BOLD, 40), new Delta(Handle.BL, AffineTransform.getTranslateInstance(50, 0)));
        if (renderer.zoom >= 15) {
                renderer.labelText(feature, "V-AIS", new Font("Arial", Font.PLAIN, 40), Symbols.Msymb, new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, 70)));
        }
    }
        
	private void waterways(Feature feature) {
		renderer.lineVector(feature, new LineStyle(Symbols.Bwater, 20, (feature.geom.prim == Pflag.AREA) ? Symbols.Bwater : null));
	}

	private void wrecks(Feature feature) {
		if (renderer.zoom >= 14) {
			switch ((CatWRK) getAttEnum(feature, feature.type, Att.CATWRK)) {
			case WRK_DNGR:
			case WRK_MSTS:
				renderer.symbol(feature, Areas.WreckD);
				break;
			case WRK_HULS:
				renderer.symbol(feature, Areas.WreckS);
				break;
			default:
				renderer.symbol(feature, Areas.WreckND);
			}
			addName(feature, 15, new Font("Arial", Font.BOLD, 40), new Delta(Handle.BC, AffineTransform.getTranslateInstance(0, -60)));
		}
	}
}
