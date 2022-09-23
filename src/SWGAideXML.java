import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.sql.*;


/**
 * An Ewoog
 * Requires ojdbc6.jar to interface w. OracleDB
 */
public class SWGAideXML {


    //////////////Variables////////////////
    private static final String DB_IP = "66.85.75.4"; //Database Computer IP
    private static final String DB_PORT = "1521"; // Port for Database Connections
    private static final String DB_NAME = "swg"; // Database Name
    private static final String DB_USER = "swg"; //Username for DB
    private static final String DB_PASS = "$wG_$erver_22"; //Password for DB
    private static final String OUTPUT_PATH = "D:"; //Path where the XML will be created.
    ///////////////////////////////////////

    private static ArrayList<ResourceClass> rGroup;

    public static void main(String[] args) {
        int sleep_total = 0;
        while (true){
            execute();
            System.out.println("Sleeping for one hour.");
            sleep_total = 0;
            while (sleep_total<3600000){
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                sleep_total = sleep_total + 60000;
                System.out.println("Sleeping for "+((3600000-sleep_total)/60000)+" minutes.");
            }
        }

    }

    private static void execute() {
        try {
            rGroup = new ArrayList();
            buildGroup();
            Class.forName("oracle.jdbc.driver.OracleDriver");
            String url = "jdbc:oracle:thin:@"+DB_IP+":"+DB_PORT+":"+DB_NAME;
            Connection conn = DriverManager.getConnection(url, DB_USER, DB_PASS);
            Statement stmt = conn.createStatement();
            ResultSet rs;
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder;
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.newDocument();
            String date = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss").format(new Date());
            Element rootElement =
                    doc.createElementNS("generated at: "+date, "SWG_Resource_Dump");
            doc.appendChild(rootElement);

            rs = stmt.executeQuery("SELECT * FROM resource_types a WHERE a.depleted_timestamp > (SELECT clock.last_save_time FROM clock WHERE last_save_time>0)");
            while (rs.next()) {
                Element tResource = doc.createElement("resource");
                tResource.appendChild(getResourceElement(doc, tResource, "name", rs.getString("resource_name")));
                tResource.appendChild(getResourceElement(doc, tResource, "available_date", new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss").format(new Date())));
                String rClass = rs.getString("resource_class");
                rClass = getResourceClass(rClass);
                tResource.appendChild(getResourceElement(doc, tResource, "resource_class", rClass));
                statAppend(rs.getString("attributes"), doc, tResource);
                planetsAppend(rs.getString("fractal_seeds"), doc, tResource);
                rootElement.appendChild(tResource);
            }
            conn.close();
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            //for pretty print
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);
            StreamResult console = new StreamResult(System.out);
            StreamResult file = new StreamResult(new File(OUTPUT_PATH+"\\Apotheosis_Resource_Dump.xml"));
            transformer.transform(source, console);
            transformer.transform(source, file);

        } catch (Exception e) {
            System.err.println("Got an exception! ");
            System.err.println(e.getMessage());
        }

    }

    static class ResourceClass {
        String server_name;
        String class_name;
        ResourceClass(String a, String b){
            server_name = a;
            class_name = b;
        }
        public void setClass_name(String class_name) {
            this.class_name = class_name;
        }

        public void setServer_name(String server_name) {
            this.server_name = server_name;
        }

    }

    private static Node getResourceElement(Document doc, Element element, String name, String value) {
        Element node = doc.createElement(name);
        node.appendChild(doc.createTextNode(value));
        return node;
    }

    private static String getResourceClass(String classname) {
        classname = classname.replace("\"", "");
        for (ResourceClass e : rGroup
        ) {
            if (e.server_name.equalsIgnoreCase(classname)) {

                return e.class_name;
            }
        }
        return classname;
    }

    private static void statAppend(String stats, Document doc, Element subparent) {
        stats = stats.replace("\"", "");
        String datavalue[] = stats.split(":");
        Element tstats = doc.createElement("stats");
        for (String a :
                datavalue) {
            a = a.replace("res_", "");
            String temp[] = a.split(" ");
            String attb = temp[0];
            switch (temp[0]) {
                case "cold_resist":
                    attb = "CR";
                    break;
                case "conductivity":
                    attb = "CD";
                    break;
                case "decay_resist":
                    attb = "DR";
                    break;
                case "heat_resist":
                    attb = "HR";
                    break;
                case "malleability":
                    attb = "MA";
                    break;
                case "quality":
                    attb = "OQ";
                    break;
                case "shock_resistance":
                    attb = "SR";
                    break;
                case "toughness":
                    attb = "UT";
                    break;
                case "potential_energy":
                    attb = "PE";
                    break;
                case "flavor":
                    attb = "FL";
                    break;
                case  "entangle_resistance":
                    attb= "ER";
                    break;
                default:
                    attb = temp[0];
                    break;
            }
            tstats.appendChild(getResourceElement(doc, tstats, attb, temp[1]));

        }
        subparent.appendChild(tstats);
    }

    private static void planetsAppend(String planets, Document doc, Element tResource) {
        String datavalue[] = planets.split(":");
        Element tplanents = doc.createElement("planets");
        boolean kFlag = false;
        for (String a :
                datavalue) {
            a = a.substring(0, a.indexOf(' '));
            a.replace("\"", "");
            String planet_name = "";
            int planet_id;
            if (a == null || a.equalsIgnoreCase("")) {
                planet_id = 0;
            } else {
                planet_id = Integer.parseInt(a);
            }
            switch (planet_id) {
                case 10000006:
                    planet_name = "corellia";
                    break;
                case 10000007:
                    planet_name = "dantooine";
                    break;
                case 10000008:
                    planet_name = "dathomir";
                    break;
                case 10000009:
                    planet_name = "endor";
                    break;
                case 10000010:
                    planet_name = "lok";
                    break;
                case 10000011:
                    planet_name = "naboo";
                    break;
                case 10000012:
                    planet_name = "rori";
                    break;
                case 10000013:
                    planet_name = "talus";
                    break;
                case 10000014:
                    planet_name = "tatooine";
                    break;
                case 10000015:
                    planet_name = "tut";
                    break;
                case 10000016:
                    planet_name = "yavin 4";
                    break;
                case 10000031:
                case 10000032:
                case 10000033:
                case 10000034:
                case 10000035:
                case 10000036:
                case 10000037:
                    planet_name = "kashyyyk";
                    break;
                case 10000039:
                    planet_name = "mustafar";
                    break;
                case 89335060:
                    planet_name = "dxun";
                    break;
                case 0:
                    planet_name = "";
                    break;
                default:
                    planet_name = "UNKNOWN PLANET";
                    break;
            }
            if (kFlag && planet_name.equals("kashyyyk")){
                break;
            }
            if (planet_name.equals("kashyyyk")){
                kFlag = true;
            }
            if(planet_name != null && !planet_name.trim().isEmpty() && !planet_name.equals("UNKNOWN PLANET")) {
                tplanents.appendChild(getResourceElement(doc, tplanents, "planet", planet_name));
            }
        }
        tResource.appendChild(tplanents);
    }
    private static boolean buildGroup(){
        rGroup.add(new ResourceClass("aluminum_chromium", "Chromium Aluminum"));
        rGroup.add(new ResourceClass("aluminum_duralumin", "Duralumin Aluminum"));
        rGroup.add(new ResourceClass("aluminum_linksteel", "Link-Steel Aluminum"));
        rGroup.add(new ResourceClass("aluminum_mustafar", "Mustafarian Aluminum"));
        rGroup.add(new ResourceClass("aluminum_perovskitic", "Perovskitic Aluminum"));
        rGroup.add(new ResourceClass("aluminum_phrik", "Phrik Aluminum"));
        rGroup.add(new ResourceClass("aluminum_smelted", "Smelted Aluminum"));
        rGroup.add(new ResourceClass("aluminum_titanium", "Titanium Aluminum"));
        rGroup.add(new ResourceClass("armophous_baltaran", "Bal'ta'ran Crystal Amorphous Gemstone"));
        rGroup.add(new ResourceClass("armophous_baradium", "Baradium Amorphous Gemstone"));
        rGroup.add(new ResourceClass("armophous_bospridium", "Bospridium Amorphous Gemstone"));
        rGroup.add(new ResourceClass("armophous_mustafar_1", "Mustafarian Type 1 Crystal Amorphous Gem"));
        rGroup.add(new ResourceClass("armophous_mustafar_2", "Mustafarian Type 2 Crystal Amorphous Gem"));
        rGroup.add(new ResourceClass("armophous_plexite", "Plexite Amorphous Gemstone"));
        rGroup.add(new ResourceClass("armophous_regvis", "Regvis Amorphous Gemstone"));
        rGroup.add(new ResourceClass("armophous_rudic", "Rudic Amorphous Gemstone"));
        rGroup.add(new ResourceClass("armophous_ryll", "Ryll Amorphous Gemstone"));
        rGroup.add(new ResourceClass("armophous_sedrellium", "Sedrellium Amorphous Gemstone"));
        rGroup.add(new ResourceClass("armophous_stygium", "Stygium Amorphous Gemstone"));
        rGroup.add(new ResourceClass("armophous_vendusii", "Vendusii Crystal Amorphous Gemstone"));
        rGroup.add(new ResourceClass("bone", "Bone"));
        rGroup.add(new ResourceClass("bone_avian", "Avian Bone"));
        rGroup.add(new ResourceClass("bone_avian_corellia", "Corellian Avian Bones"));
        rGroup.add(new ResourceClass("bone_avian_dantooine", "Dantooine Avian Bones"));
        rGroup.add(new ResourceClass("bone_avian_dathomir", "Dathomirian Avian Bones"));
        rGroup.add(new ResourceClass("bone_avian_endor", "Endorian Avian Bones"));
        rGroup.add(new ResourceClass("bone_avian_kashyyyk", "Kashyyykian Avian Bones"));
        rGroup.add(new ResourceClass("bone_avian_lok", "Lokian Avian Bones"));
        rGroup.add(new ResourceClass("bone_avian_mustafar", "Mustafarian Avian Bones"));
        rGroup.add(new ResourceClass("bone_avian_naboo", "Nabooian Avian Bones"));
        rGroup.add(new ResourceClass("bone_avian_rori", "Rori Avian Bones"));
        rGroup.add(new ResourceClass("bone_avian_talus", "Talusian Avian Bones"));
        rGroup.add(new ResourceClass("bone_avian_tatooine", "Tatoonian Avian Bones"));
        rGroup.add(new ResourceClass("bone_avian_yavin4", "Yavinian Avian Bones"));
        rGroup.add(new ResourceClass("bone_avian_dxun", "Dxun Avian Bones"));
        rGroup.add(new ResourceClass("bone_horn", "Horn"));
        rGroup.add(new ResourceClass("bone_horn_corellia", "Corellian Horn"));
        rGroup.add(new ResourceClass("bone_horn_dantooine", "Dantooine Horn"));
        rGroup.add(new ResourceClass("bone_horn_dathomir", "Dathomirian Horn"));
        rGroup.add(new ResourceClass("bone_horn_endor", "Endorian Horn"));
        rGroup.add(new ResourceClass("bone_horn_ground", "Ground Horn"));
        rGroup.add(new ResourceClass("bone_horn_kashyyyk", "Kashyyykian Horn"));
        rGroup.add(new ResourceClass("bone_horn_lok", "Lokian Horn"));
        rGroup.add(new ResourceClass("bone_horn_mustafar", "Mustafarian Horn"));
        rGroup.add(new ResourceClass("bone_horn_naboo", "Nabooian Horn"));
        rGroup.add(new ResourceClass("bone_horn_rori", "Rori Horn"));
        rGroup.add(new ResourceClass("bone_horn_talus", "Talusian Horn"));
        rGroup.add(new ResourceClass("bone_horn_tatooine", "Tatooinian Horn"));
        rGroup.add(new ResourceClass("bone_horn_yavin4", "Yavinian Horn"));
        rGroup.add(new ResourceClass("bone_horn_dxun", "Dxun Horn"));
        rGroup.add(new ResourceClass("bone_mammal_corellia", "Corellian Animal Bones"));
        rGroup.add(new ResourceClass("bone_mammal_dantooine", "Dantooinian Animal Bones"));
        rGroup.add(new ResourceClass("bone_mammal_dathomir", "Dathomirian Animal Bones"));
        rGroup.add(new ResourceClass("bone_mammal_endor", "Endorian Animal Bones"));
        rGroup.add(new ResourceClass("bone_mammal_kashyyyk", "Kashyyykian Animal Bones"));
        rGroup.add(new ResourceClass("bone_mammal_lok", "Lokian Animal Bones"));
        rGroup.add(new ResourceClass("bone_mammal_mustafar", "Mustafarian Animal Bones"));
        rGroup.add(new ResourceClass("bone_mammal_naboo", "Nabooian Animal Bones"));
        rGroup.add(new ResourceClass("bone_mammal_rori", "Rori Animal Bones"));
        rGroup.add(new ResourceClass("bone_mammal_talus", "Talusian Animal Bones"));
        rGroup.add(new ResourceClass("bone_mammal_tatooine", "Tatooinian Animal Bones"));
        rGroup.add(new ResourceClass("bone_mammal_yavin4", "Yavinian Animal Bones"));
        rGroup.add(new ResourceClass("bone_mammal_dxun", "Dxun Animal Bones"));
        rGroup.add(new ResourceClass("cereal", "Cereal"));
        rGroup.add(new ResourceClass("chemical", "Chemical"));
        rGroup.add(new ResourceClass("chemical_compound", "Chemical Compound"));
        rGroup.add(new ResourceClass("combined_radioactive_isotpopes", "Combined Radioactive Isotopes"));
        rGroup.add(new ResourceClass("copper", "Copper"));
        rGroup.add(new ResourceClass("copper_beyrllius", "Beyrllius Copper"));
        rGroup.add(new ResourceClass("copper_borocarbitic", "Conductive Borcarbitic Copper"));
        rGroup.add(new ResourceClass("copper_codoan", "Codoan Copper"));
        rGroup.add(new ResourceClass("copper_desh", "Desh Copper"));
        rGroup.add(new ResourceClass("copper_diatium", "Diatium Copper"));
        rGroup.add(new ResourceClass("copper_kelsh", "Kelsh Copper"));
        rGroup.add(new ResourceClass("copper_mustafar", "Mustafarian Copper"));
        rGroup.add(new ResourceClass("copper_mythra", "Mythra Copper"));
        rGroup.add(new ResourceClass("copper_platinite", "Platinite Copper"));
        rGroup.add(new ResourceClass("copper_polysteel", "Polysteel Copper"));
        rGroup.add(new ResourceClass("copper_smelted", "Smelted Copper"));
        rGroup.add(new ResourceClass("copper_thallium", "Thallium Copper"));
        rGroup.add(new ResourceClass("corn", "Corn"));
        rGroup.add(new ResourceClass("corn_domesticated", "Domesticated Corn"));
        rGroup.add(new ResourceClass("corn_domesticated_corellia", "Corellian Domesticated Corn"));
        rGroup.add(new ResourceClass("corn_domesticated_dantooine", "Dantooine Domesticated Corn"));
        rGroup.add(new ResourceClass("corn_domesticated_dathomir", "Dathomirian Domesticated Corn"));
        rGroup.add(new ResourceClass("corn_domesticated_endor", "Endorian Domesticated Corn"));
        rGroup.add(new ResourceClass("corn_domesticated_kashyyyk", "Kashyyykian Domesticated Corn"));
        rGroup.add(new ResourceClass("corn_domesticated_lok", "Lokian Domesticated Corn"));
        rGroup.add(new ResourceClass("corn_domesticated_mustafar", "Mustafarian Domesticated Corn"));
        rGroup.add(new ResourceClass("corn_domesticated_naboo", "Nabooian Domesticated Corn"));
        rGroup.add(new ResourceClass("corn_domesticated_rori", "Rori Domesticated Corn"));
        rGroup.add(new ResourceClass("corn_domesticated_talus", "Talusian Domesticated Corn"));
        rGroup.add(new ResourceClass("corn_domesticated_tatooine", "Tatooinian Domesticated Corn"));
        rGroup.add(new ResourceClass("corn_domesticated_yavin4", "Yavinian Domesticated Corn"));
        rGroup.add(new ResourceClass("corn_domesticated_dxun", "Dxun Domesticated Corn"));
        rGroup.add(new ResourceClass("corn_wild", "Wild Corn"));
        rGroup.add(new ResourceClass("corn_wild_corellia", "Corellian Wild Corn"));
        rGroup.add(new ResourceClass("corn_wild_dantooine", "Dantooine Wild Corn"));
        rGroup.add(new ResourceClass("corn_wild_dathomir", "Dathomirian Wild Corn"));
        rGroup.add(new ResourceClass("corn_wild_endor", "Endorian Wild Corn"));
        rGroup.add(new ResourceClass("corn_wild_kashyyyk", "Kashyyykian Wild Corn"));
        rGroup.add(new ResourceClass("corn_wild_lok", "Lokian Wild Corn"));
        rGroup.add(new ResourceClass("corn_wild_mustafar", "Mustafarian Wild Corn"));
        rGroup.add(new ResourceClass("corn_wild_naboo", "Nabooian Wild Corn"));
        rGroup.add(new ResourceClass("corn_wild_rori", "Rori Wild Corn"));
        rGroup.add(new ResourceClass("corn_wild_talus", "Talusian Wild Corn"));
        rGroup.add(new ResourceClass("corn_wild_tatooine", "Tatooinian Wild Corn"));
        rGroup.add(new ResourceClass("corn_wild_yavin4", "Yavinian Wild Corn"));
        rGroup.add(new ResourceClass("corn_wild_dxun", "Dxun Wild Corn"));
        rGroup.add(new ResourceClass("creature_food", "Creature Food"));
        rGroup.add(new ResourceClass("creature_resources", "Creature Resources"));
        rGroup.add(new ResourceClass("creature_structural", "Creature Structural"));
        rGroup.add(new ResourceClass("crystalline_byrothsis", "Byrothsis Crystalline Gemstone"));
        rGroup.add(new ResourceClass("crystalline_gallinorian", "Gallinorian Rainbow Gem Crystalline Gemstone"));
        rGroup.add(new ResourceClass("crystalline_green_diamond", "Green Diamond Crystalline Gemstone"));
        rGroup.add(new ResourceClass("crystalline_kerol_firegem", "Kerol Fire-Gem Crystalline Gemstone"));
        rGroup.add(new ResourceClass("crystalline_laboi_mineral_crystal", "Laboi Mineral Crystal Crystalline Gemstone"));
        rGroup.add(new ResourceClass("crystalline_mustafar_1", "Mustafarian Type 1 Crystalline Gem"));
        rGroup.add(new ResourceClass("crystalline_mustafar_2", "Mustafarian Type 2 Crystalline Gem"));
        rGroup.add(new ResourceClass("crystalline_seafah_jewel", "Seafah Jewel Crystalline Gemstone"));
        rGroup.add(new ResourceClass("crystalline_sormahil_firegem", "Sormahil Fire Gem Crystalline Gemstone"));
        rGroup.add(new ResourceClass("crystalline_vertex", "Vertex Crystalline Gemstone"));
        rGroup.add(new ResourceClass("degraded_fuel_petrochem_solid", "Degraded Solid Petrochem Fuel"));
        rGroup.add(new ResourceClass("energy", "Energy"));
        rGroup.add(new ResourceClass("energy_renewable", "Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_site_limited", "Site-Restricted Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_site_limited_geothermal_corellia", "Corellian Geothermal Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_site_limited_geothermal_dantooine", "Dantooine Geothermal Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_site_limited_geothermal_dathomir", "Dathomirian Geothermal Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_site_limited_geothermal_endor", "Endorian Geothermal Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_site_limited_geothermal_kashyyyk", "Kashyyykian Geothermal Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_site_limited_geothermal_lok", "Lokian Geothermal Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_site_limited_geothermal_mustafar", "Mustafarian Geothermal Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_site_limited_geothermal_naboo", "Nabooian Geothermal Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_site_limited_geothermal_rori", "Rori Geothermal Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_site_limited_geothermal_talus", "Talusian Geothermal Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_site_limited_geothermal_tatooine", "Tatooinian Geothermal Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_site_limited_geothermal_weak", "Weak Geothermal Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_site_limited_geothermal_yavin4", "Yavinian Geothermal Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_site_limited_geothermal_dxun", "Dxun Geothermal Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_site_limited_hydron3_corellia", "Corellian Hydron-3 Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_site_limited_hydron3_dantooine", "Dantooine Hydron-3 Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_site_limited_hydron3_dathomir", "Dathomirian Hydron-3 Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_site_limited_hydron3_endor", "Endorian Hydron-3 Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_site_limited_hydron3_kashyyyk", "Kashyyykian Hydron-3 Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_site_limited_hydron3_lok", "Lokian Hydron-3 Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_site_limited_hydron3_mustafar", "Mustafarian Tidal Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_site_limited_hydron3_naboo", "Nabooian Hydron-3 Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_site_limited_hydron3_rori", "Rori Hydron-3 Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_site_limited_hydron3_talus", "Talusian Hydron-3 Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_site_limited_hydron3_tatooine", "Tatooinian Hydron-3 Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_site_limited_hydron3_weak", "Weak Hydron-3 Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_site_limited_hydron3_yavin4", "Yavinian Hydron-3 Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_site_limited_hydron3_dxun", "Dxun Hydron-3 Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_site_limited_tidal_corellia", "Corellian Tidal Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_site_limited_tidal_dantooine", "Dantooine Tidal Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_site_limited_tidal_dathomir", "Dathomirian Tidal Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_site_limited_tidal_endor", "Endorian Tidal Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_site_limited_tidal_kashyyyk", "Kashyyykian Tidal Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_site_limited_tidal_lok", "Lokian Tidal Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_site_limited_tidal_mustafar", "Mustafarian Tidal Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_site_limited_tidal_naboo", "Nabooian Tidal Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_site_limited_tidal_rori", "Rori Tidal Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_site_limited_tidal_talus", "Talusian Tidal Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_site_limited_tidal_tatooine", "Tatooinian Tidal Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_site_limited_tidal_weak", "Weak Tidal Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_site_limited_tidal_yavin4", "Yavinian Tidal Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_site_limited_tidal_dxun", "Dxun Tidal Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_unlimited", "Non Site-Restricted Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_unlimited_solar", "Solar Energy"));
        rGroup.add(new ResourceClass("energy_renewable_unlimited_solar_corellia", "Corellian Solar Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_unlimited_solar_dantooine", "Dantooine Solar Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_unlimited_solar_dathomir", "Dathomirian Solar Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_unlimited_solar_endor", "Endorian Solar Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_unlimited_solar_kashyyyk", "Kashyyykian Solar Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_unlimited_solar_lok", "Lokian Solar Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_unlimited_solar_mustafar", "Mustafarian Solar Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_unlimited_solar_naboo", "Nabooian Solar Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_unlimited_solar_rori", "Rori Solar Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_unlimited_solar_talus", "Talusian Solar Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_unlimited_solar_tatooine", "Tatooinian Solar Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_unlimited_solar_weak", "Weak Solar Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_unlimited_solar_yavin4", "Yavinian Solar Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_unlimited_solar_dxun", "Dxun Solar Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_unlimited_wind", "Wind Energy"));
        rGroup.add(new ResourceClass("energy_renewable_unlimited_wind_corellia", "Corellian Wind Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_unlimited_wind_dantooine", "Dantooine Wind Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_unlimited_wind_dathomir", "Dathomirian Wind Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_unlimited_wind_endor", "Endorian Wind Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_unlimited_wind_kashyyyk", "Kashyyykian Wind Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_unlimited_wind_lok", "Lokian Wind Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_unlimited_wind_mustafar", "Mustafarian Wind Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_unlimited_wind_naboo", "Nabooian Wind Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_unlimited_wind_rori", "Rori Wind Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_unlimited_wind_talus", "Talusian Wind Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_unlimited_wind_tatooine", "Tatooinian Wind Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_unlimited_wind_weak", "Weak Wind Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_unlimited_wind_yavin4", "Yavinian Wind Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_unlimited_wind_dxun", "Dxun Wind Renewable Energy"));
        rGroup.add(new ResourceClass("fiberplast", "Fiberplast"));
        rGroup.add(new ResourceClass("fiberplast_corellia", "Corellian Fiberplast"));
        rGroup.add(new ResourceClass("fiberplast_dantooine", "Dantooine Fiberplast"));
        rGroup.add(new ResourceClass("fiberplast_dathomir", "Dathomirian Fiberplast"));
        rGroup.add(new ResourceClass("fiberplast_endor", "Endorian Fiberplast"));
        rGroup.add(new ResourceClass("fiberplast_gravitonic", "Gravitonic Fiberplast"));
        rGroup.add(new ResourceClass("fiberplast_kashyyyk", "Kashyyykian Fiberplast"));
        rGroup.add(new ResourceClass("fiberplast_lok", "Lokian Fiberplast"));
        rGroup.add(new ResourceClass("fiberplast_mustafar", "Mustafarian Fiberplast"));
        rGroup.add(new ResourceClass("fiberplast_naboo", "Nabooian Fiberplast"));
        rGroup.add(new ResourceClass("fiberplast_rori", "Rori Fiberplast"));
        rGroup.add(new ResourceClass("fiberplast_talus", "Talusian Fiberplast"));
        rGroup.add(new ResourceClass("fiberplast_tatooine", "Tatooinian Fiberplast"));
        rGroup.add(new ResourceClass("fiberplast_yavin4", "Yavinian Fiberplast"));
        rGroup.add(new ResourceClass("fiberplast_dxun", "Dxun Fiberplast"));
        rGroup.add(new ResourceClass("flora_food", "Flora Food"));
        rGroup.add(new ResourceClass("flora_resources", "Flora Resources"));
        rGroup.add(new ResourceClass("flora_structural", "Flora Structural"));
        rGroup.add(new ResourceClass("fruit", "Fruit"));
        rGroup.add(new ResourceClass("fruit_berries", "Berries"));
        rGroup.add(new ResourceClass("fruit_berries_corellia", "Corellian Berry Fruit"));
        rGroup.add(new ResourceClass("fruit_berries_dantooine", "Dantooine Berry Fruit"));
        rGroup.add(new ResourceClass("fruit_berries_dathomir", "Dathomirian Berry Fruit"));
        rGroup.add(new ResourceClass("fruit_berries_endor", "Endorian Berry Fruit"));
        rGroup.add(new ResourceClass("fruit_berries_kashyyyk", "Kashyyykian Berry Fruit"));
        rGroup.add(new ResourceClass("fruit_berries_lok", "Lokian Berry Fruit"));
        rGroup.add(new ResourceClass("fruit_berries_mustafar", "Mustafarian Berry Fruit"));
        rGroup.add(new ResourceClass("fruit_berries_naboo", "Nabooina Berry Fruit"));
        rGroup.add(new ResourceClass("fruit_berries_rori", "Rori Berry Fruit"));
        rGroup.add(new ResourceClass("fruit_berries_talus", "Talusian Berry Fruit"));
        rGroup.add(new ResourceClass("fruit_berries_tatooine", "Tatooinian Berry Fruit"));
        rGroup.add(new ResourceClass("fruit_berries_yavin4", "Yavinian Berry Fruit"));
        rGroup.add(new ResourceClass("fruit_berries_dxun", "Dxun Berry Fruit"));
        rGroup.add(new ResourceClass("fruit_flowers", "Flowers"));
        rGroup.add(new ResourceClass("fruit_flowers_corellia", "Corellian Flower Fruit"));
        rGroup.add(new ResourceClass("fruit_flowers_dantooine", "Dantooine Flower Fruit"));
        rGroup.add(new ResourceClass("fruit_flowers_dathomir", "Dathomirian Flower Fruit"));
        rGroup.add(new ResourceClass("fruit_flowers_endor", "Endorian Flower Fruit"));
        rGroup.add(new ResourceClass("fruit_flowers_kashyyyk", "Kashyyykian Flower Fruit"));
        rGroup.add(new ResourceClass("fruit_flowers_lok", "Lokian Flower Fruit"));
        rGroup.add(new ResourceClass("fruit_flowers_mustafar", "Mustafarian Flower Fruit"));
        rGroup.add(new ResourceClass("fruit_flowers_naboo", "Nabooian Flower Fruit"));
        rGroup.add(new ResourceClass("fruit_flowers_rori", "Rorian Flower Fruit"));
        rGroup.add(new ResourceClass("fruit_flowers_talus", "Talusian Flower Fruit"));
        rGroup.add(new ResourceClass("fruit_flowers_tatooine", "Tatooinian Flower Fruit"));
        rGroup.add(new ResourceClass("fruit_flowers_yavin4", "Yavinian Flower Fruit"));
        rGroup.add(new ResourceClass("fruit_flowers_dxun", "Dxun Flower Fruit"));
        rGroup.add(new ResourceClass("fruit_fruits", "Fruits"));
        rGroup.add(new ResourceClass("fruit_fruits_corellia", "Corellian Fruit"));
        rGroup.add(new ResourceClass("fruit_fruits_dantooine", "Dantooine Fruit"));
        rGroup.add(new ResourceClass("fruit_fruits_dathomir", "Dathomirian Fruit"));
        rGroup.add(new ResourceClass("fruit_fruits_endor", "Endorian Fruit"));
        rGroup.add(new ResourceClass("fruit_fruits_kashyyyk", "Kashyyykian Fruit"));
        rGroup.add(new ResourceClass("fruit_fruits_lok", "Lokian Fruit"));
        rGroup.add(new ResourceClass("fruit_fruits_mustafar", "Mustafarian Fruit"));
        rGroup.add(new ResourceClass("fruit_fruits_naboo", "Nabooian Fruit"));
        rGroup.add(new ResourceClass("fruit_fruits_rori", "Rorian Fruit"));
        rGroup.add(new ResourceClass("fruit_fruits_talus", "Talusian Fruit"));
        rGroup.add(new ResourceClass("fruit_fruits_tatooine", "Tatooinian Fruit"));
        rGroup.add(new ResourceClass("fruit_fruits_yavin4", "Yavinian Fruit"));
        rGroup.add(new ResourceClass("fruit_fruits_dxun", "Dxun Fruit"));
        rGroup.add(new ResourceClass("fuel_petrochem_liquid", "Liquid Petrochem Fuel"));
        rGroup.add(new ResourceClass("fuel_petrochem_liquid_known", "Known Liquid Petrochem Fuel"));
        rGroup.add(new ResourceClass("fuel_petrochem_solid", "Solid Petrochem Fuel"));
        rGroup.add(new ResourceClass("fuel_petrochem_solid_known", "Known Solid Petrochem Fuel"));
        rGroup.add(new ResourceClass("gas", "Gas"));
        rGroup.add(new ResourceClass("gas_inert", "Inert Gas"));
        rGroup.add(new ResourceClass("gas_inert_bilal", "Bilal Inert Gas"));
        rGroup.add(new ResourceClass("gas_inert_corthel", "Corthel Inert Gas"));
        rGroup.add(new ResourceClass("gas_inert_culsion", "Culsion Inert Gas"));
        rGroup.add(new ResourceClass("gas_inert_dioxis", "Dioxis Inert Gas"));
        rGroup.add(new ResourceClass("gas_inert_hurlothrombic", "Hurlothrombic Inert Gas"));
        rGroup.add(new ResourceClass("gas_inert_hydron3", "Hydron-3 Inert Gas"));
        rGroup.add(new ResourceClass("gas_inert_kaylon", "Kaylon Inert Gas"));
        rGroup.add(new ResourceClass("gas_inert_known", "Known Inert Gas"));
        rGroup.add(new ResourceClass("gas_inert_korfaise", "Korfaise Inert Gas"));
        rGroup.add(new ResourceClass("gas_inert_malium", "Malium Inert Gas"));
        rGroup.add(new ResourceClass("gas_inert_methanagen", "Methanagen Inert Gas"));
        rGroup.add(new ResourceClass("gas_inert_mirth", "Mirth Inert Gas"));
        rGroup.add(new ResourceClass("gas_inert_mixed", "Mixed Inert Gas"));
        rGroup.add(new ResourceClass("gas_inert_obah", "Obah Inert Gas"));
        rGroup.add(new ResourceClass("gas_inert_rethin", "Rethin Inert Gas"));
        rGroup.add(new ResourceClass("gas_inert_unknown", "Unknown Inert Gas"));
        rGroup.add(new ResourceClass("gas_reactive", "Reactive Gas"));
        rGroup.add(new ResourceClass("gas_reactive_eleton", "Eleton Reactive Gas"));
        rGroup.add(new ResourceClass("gas_reactive_irolunn", "Irolunn Reactive Gas"));
        rGroup.add(new ResourceClass("gas_reactive_known", "Known Reactive Gas"));
        rGroup.add(new ResourceClass("gas_reactive_methane", "Methane Reactive Gas"));
        rGroup.add(new ResourceClass("gas_reactive_mixed", "Mixed Reactive Gas"));
        rGroup.add(new ResourceClass("gas_reactive_mustafar", "Mustafarian Reactive Gas"));
        rGroup.add(new ResourceClass("gas_reactive_organometallic", "Unstable Organometallic Reactive Gas"));
        rGroup.add(new ResourceClass("gas_reactive_orveth", "Orveth Reactive Gas"));
        rGroup.add(new ResourceClass("gas_reactive_sig", "Sig Reactive Gas"));
        rGroup.add(new ResourceClass("gas_reactive_skevon", "Skevon Reactive Gas"));
        rGroup.add(new ResourceClass("gas_reactive_tolium", "Tolium Reactive Gas"));
        rGroup.add(new ResourceClass("gas_reactive_unknown", "Unknown Reactive Gas"));
        rGroup.add(new ResourceClass("gemstone", "Gemstone"));
        rGroup.add(new ResourceClass("gemstone_armophous", "Amorphous Gemstone"));
        rGroup.add(new ResourceClass("gemstone_crystalline", "Crystalline Gemstone"));
        rGroup.add(new ResourceClass("gemstone_mixed_low_quality", "Low Quality Gemstone"));
        rGroup.add(new ResourceClass("gemstone_unknown", "Unknown Gem Type"));
        rGroup.add(new ResourceClass("ground_bones", "Ground Bones"));
        rGroup.add(new ResourceClass("hide", "Hide"));
        rGroup.add(new ResourceClass("hide_bristley", "Bristley Hide"));
        rGroup.add(new ResourceClass("hide_bristley_corellia", "Corellian Bristley Hide"));
        rGroup.add(new ResourceClass("hide_bristley_dantooine", "Dantooine Bristley Hide"));
        rGroup.add(new ResourceClass("hide_bristley_dathomir", "Dathomirian Bristley Hide"));
        rGroup.add(new ResourceClass("hide_bristley_endor", "Endorian Bristley Hide"));
        rGroup.add(new ResourceClass("hide_bristley_kashyyyk", "Kashyyykian Bristley Hide"));
        rGroup.add(new ResourceClass("hide_bristley_lok", "Lokian Bristley Hidee"));
        rGroup.add(new ResourceClass("hide_bristley_mustafar", "Mustafarian Bristley Hide"));
        rGroup.add(new ResourceClass("hide_bristley_naboo", "Nabooian Bristley Hide"));
        rGroup.add(new ResourceClass("hide_bristley_rori", "Rori Bristley Hide"));
        rGroup.add(new ResourceClass("hide_bristley_talus", "Talusian Bristley Hide"));
        rGroup.add(new ResourceClass("hide_bristley_tatooine", "Tatooinian Bristley Hide"));
        rGroup.add(new ResourceClass("hide_bristley_yavin4", "Yavinian Bristley Hide"));
        rGroup.add(new ResourceClass("hide_bristley_dxun", "Dxun Bristley Hide"));
        rGroup.add(new ResourceClass("hide_leathery", "Leathery Hide"));
        rGroup.add(new ResourceClass("hide_leathery_corellia", "Corellian Leathery Hide"));
        rGroup.add(new ResourceClass("hide_leathery_dantooine", "Dantooine Leathery Hide"));
        rGroup.add(new ResourceClass("hide_leathery_dathomir", "Dathomirian Leathery Hide"));
        rGroup.add(new ResourceClass("hide_leathery_endor", "Endorian Leathery Hide"));
        rGroup.add(new ResourceClass("hide_leathery_kashyyyk", "Kashyyykian Leathery Hide"));
        rGroup.add(new ResourceClass("hide_leathery_lok", "Lokian Leathery Hide"));
        rGroup.add(new ResourceClass("hide_leathery_mustafar", "Mustafarian Leathery Hide"));
        rGroup.add(new ResourceClass("hide_leathery_naboo", "Nabooian Leathery Hide"));
        rGroup.add(new ResourceClass("hide_leathery_rori", "Rori Leathery Hide"));
        rGroup.add(new ResourceClass("hide_leathery_talus", "Talusian Leathery Hide"));
        rGroup.add(new ResourceClass("hide_leathery_tatooine", "Tatooinian Leathery Hide"));
        rGroup.add(new ResourceClass("hide_leathery_yavin4", "Yavinian Leathery Hide"));
        rGroup.add(new ResourceClass("hide_leathery_dxun", "Dxun Leathery Hide"));
        rGroup.add(new ResourceClass("hide_scaley", "Scaley Hide"));
        rGroup.add(new ResourceClass("hide_scaley_corellia", "Corellian Scaley Hide"));
        rGroup.add(new ResourceClass("hide_scaley_dantooine", "Dantooine Scaley Hide"));
        rGroup.add(new ResourceClass("hide_scaley_dathomir", "Dathomirian Scaley Hide"));
        rGroup.add(new ResourceClass("hide_scaley_endor", "Endorian Scaley Hide"));
        rGroup.add(new ResourceClass("hide_scaley_kashyyyk", "Kashyyykian Scaley Hide"));
        rGroup.add(new ResourceClass("hide_scaley_lok", "Lokian Scaley Hide"));
        rGroup.add(new ResourceClass("hide_scaley_mustafar", "Mustafarian Scaley Hide"));
        rGroup.add(new ResourceClass("hide_scaley_naboo", "Nabooian Scaley Hide"));
        rGroup.add(new ResourceClass("hide_scaley_rori", "Rori Scaley Hide"));
        rGroup.add(new ResourceClass("hide_scaley_talus", "Talusian Scaley Hide"));
        rGroup.add(new ResourceClass("hide_scaley_tatooine", "Tatooinian Scaley Hide"));
        rGroup.add(new ResourceClass("hide_scaley_yavin4", "Yavinian Scaley Hide"));
        rGroup.add(new ResourceClass("hide_scaley_dxun", "Dxun Scaley Hide"));
        rGroup.add(new ResourceClass("hide_wooly", "Wooly Hide"));
        rGroup.add(new ResourceClass("hide_wooly_corellia", "Corellian Wooly Hide"));
        rGroup.add(new ResourceClass("hide_wooly_dantooine", "Dantooine Wooly Hide"));
        rGroup.add(new ResourceClass("hide_wooly_dathomir", "Dathomirian Wooly Hide"));
        rGroup.add(new ResourceClass("hide_wooly_endor", "Endorian Wooly Hide"));
        rGroup.add(new ResourceClass("hide_wooly_kashyyyk", "Kashyyykian Wooly Hide"));
        rGroup.add(new ResourceClass("hide_wooly_lok", "Lokian Wooly Hide"));
        rGroup.add(new ResourceClass("hide_wooly_mustafar", "Mustafarian Wooly Hide"));
        rGroup.add(new ResourceClass("hide_wooly_naboo", "Nabooian Wooly Hide"));
        rGroup.add(new ResourceClass("hide_wooly_rori", "Rorian Wooly Hide"));
        rGroup.add(new ResourceClass("hide_wooly_talus", "Talusian Wooly Hide"));
        rGroup.add(new ResourceClass("hide_wooly_tatooine", "Tatooinian Wooly Hide"));
        rGroup.add(new ResourceClass("hide_wooly_yavin4", "Yavinian Wooly Hide"));
        rGroup.add(new ResourceClass("hide_wooly_dxun", "Dxun Wooly Hide"));
        rGroup.add(new ResourceClass("inorganic", "Inorganic"));
        rGroup.add(new ResourceClass("iron", "Iron"));
        rGroup.add(new ResourceClass("iron_axidite", "Axidite Iron"));
        rGroup.add(new ResourceClass("iron_bronzium", "Bronzium Iron"));
        rGroup.add(new ResourceClass("iron_colat", "Colat Iron"));
        rGroup.add(new ResourceClass("iron_dolovite", "Dolovite Iron"));
        rGroup.add(new ResourceClass("iron_doonium", "Doonium Iron"));
        rGroup.add(new ResourceClass("iron_kammris", "Kammris Iron"));
        rGroup.add(new ResourceClass("iron_mustafar", "Mustafarian Iron"));
        rGroup.add(new ResourceClass("iron_plumbum", "Plumbum Iron"));
        rGroup.add(new ResourceClass("iron_polonium", "Polonium Iron"));
        rGroup.add(new ResourceClass("iron_smelted", "Smelted Iron"));
        rGroup.add(new ResourceClass("meat", "Meat"));
        rGroup.add(new ResourceClass("meat_avian", "Avian Meat"));
        rGroup.add(new ResourceClass("meat_avian_corellia", "Corellian Avian Meat"));
        rGroup.add(new ResourceClass("meat_avian_dantooine", "Dantooine Avian Meat"));
        rGroup.add(new ResourceClass("meat_avian_dathomir", "Dathomirian Avian Meat"));
        rGroup.add(new ResourceClass("meat_avian_endor", "Endorian Avian Meat"));
        rGroup.add(new ResourceClass("meat_avian_kashyyyk", "Kashyyykian Avian Meat"));
        rGroup.add(new ResourceClass("meat_avian_lok", "Lokian Avian Meat"));
        rGroup.add(new ResourceClass("meat_avian_mustafar", "Mustafarian Avian Meat"));
        rGroup.add(new ResourceClass("meat_avian_naboo", "Nabooian Avian Meat"));
        rGroup.add(new ResourceClass("meat_avian_rori", "Rori Avian Meat"));
        rGroup.add(new ResourceClass("meat_avian_talus", "Talusian Avian Meat"));
        rGroup.add(new ResourceClass("meat_avian_tatooine", "Tatooinian Avian Meat"));
        rGroup.add(new ResourceClass("meat_avian_yavin4", "Yavinian Avian Meat"));
        rGroup.add(new ResourceClass("meat_avian_dxun", "Dxun Avian Meat"));
        rGroup.add(new ResourceClass("meat_carnivore", "Carnivore Meat"));
        rGroup.add(new ResourceClass("meat_carnivore_corellia", "Corellian Carnivore Meat"));
        rGroup.add(new ResourceClass("meat_carnivore_dantooine", "Dantooine Carnivore Meat"));
        rGroup.add(new ResourceClass("meat_carnivore_dathomir", "Dathomirian Carnivore Meat"));
        rGroup.add(new ResourceClass("meat_carnivore_endor", "Endorian Carnivore Meat"));
        rGroup.add(new ResourceClass("meat_carnivore_kashyyyk", "Kashyyykian Carnivore Meat"));
        rGroup.add(new ResourceClass("meat_carnivore_lok", "Lokian Carnivore Meat"));
        rGroup.add(new ResourceClass("meat_carnivore_mustafar", "Mustafarian Carnivore Meat"));
        rGroup.add(new ResourceClass("meat_carnivore_naboo", "Nabooian Carnivore Meat"));
        rGroup.add(new ResourceClass("meat_carnivore_rori", "Rori Carnivore Meat"));
        rGroup.add(new ResourceClass("meat_carnivore_talus", "Talusian Carnivore Meat"));
        rGroup.add(new ResourceClass("meat_carnivore_tatooine", "Tatooinian Carnivore Meat"));
        rGroup.add(new ResourceClass("meat_carnivore_yavin4", "Yavinian Carnivore Meat"));
        rGroup.add(new ResourceClass("meat_carnivore_dxun", "Dxun Carnivore Meat"));
        rGroup.add(new ResourceClass("meat_domesticated", "Domesticated Meat"));
        rGroup.add(new ResourceClass("meat_domesticated_corellia", "Corellian Domesticated Meat"));
        rGroup.add(new ResourceClass("meat_domesticated_dantooine", "Dantooine Domesticated Meat"));
        rGroup.add(new ResourceClass("meat_domesticated_dathomir", "Dathomirian Domesticated Meat"));
        rGroup.add(new ResourceClass("meat_domesticated_endor", "Endorian Domesticated Meat"));
        rGroup.add(new ResourceClass("meat_domesticated_kashyyyk", "Kashyyykian Domesticated Meat"));
        rGroup.add(new ResourceClass("meat_domesticated_lok", "Lokian Domesticated Meat"));
        rGroup.add(new ResourceClass("meat_domesticated_mustafar", "Mustafarian Domesticated Meat"));
        rGroup.add(new ResourceClass("meat_domesticated_naboo", "Nabooian Domesticated Meat"));
        rGroup.add(new ResourceClass("meat_domesticated_rori", "Rori Domesticated Meat"));
        rGroup.add(new ResourceClass("meat_domesticated_talus", "Talusian Domesticated Meat"));
        rGroup.add(new ResourceClass("meat_domesticated_tatooine", "Tatooinian Domesticated Meat"));
        rGroup.add(new ResourceClass("meat_domesticated_yavin4", "Yavinian Domesticated Meat"));
        rGroup.add(new ResourceClass("meat_domesticated_dxun", "Dxun Domesticated Meat"));
        rGroup.add(new ResourceClass("meat_egg", "Egg"));
        rGroup.add(new ResourceClass("meat_egg_corellia", "Corellian Egg"));
        rGroup.add(new ResourceClass("meat_egg_dantooine", "Dantooine Egg"));
        rGroup.add(new ResourceClass("meat_egg_dathomir", "Dathomirian Egg"));
        rGroup.add(new ResourceClass("meat_egg_endor", "Endorian Egg"));
        rGroup.add(new ResourceClass("meat_egg_kashyyyk", "Kashyyykian Egg"));
        rGroup.add(new ResourceClass("meat_egg_lok", "Lokian Egg"));
        rGroup.add(new ResourceClass("meat_egg_mustafar", "Mustafarian Egg"));
        rGroup.add(new ResourceClass("meat_egg_naboo", "Nabooian Egg"));
        rGroup.add(new ResourceClass("meat_egg_rori", "Rori Egg"));
        rGroup.add(new ResourceClass("meat_egg_talus", "Talusian Egg"));
        rGroup.add(new ResourceClass("meat_egg_tatooine", "Tatooinian Egg"));
        rGroup.add(new ResourceClass("meat_egg_yavin4", "Yavinian Egg"));
        rGroup.add(new ResourceClass("meat_egg_dxun", "Dxun Egg"));
        rGroup.add(new ResourceClass("meat_herbivore", "Herbivore Meat"));
        rGroup.add(new ResourceClass("meat_herbivore_corellia", "Corellian Herbivore Meat"));
        rGroup.add(new ResourceClass("meat_herbivore_dantooine", "Dantooine Herbivore Meat"));
        rGroup.add(new ResourceClass("meat_herbivore_dathomir", "Dathomirian Herbivore Meat"));
        rGroup.add(new ResourceClass("meat_herbivore_endor", "Endorian Herbivore Meat"));
        rGroup.add(new ResourceClass("meat_herbivore_kashyyyk", "Kashyyykian Herbivore Meat"));
        rGroup.add(new ResourceClass("meat_herbivore_lok", "Lokian Herbivore Meat"));
        rGroup.add(new ResourceClass("meat_herbivore_mustafar", "Mustafarian Herbivore Meat"));
        rGroup.add(new ResourceClass("meat_herbivore_naboo", "Nabooian Herbivore Meat"));
        rGroup.add(new ResourceClass("meat_herbivore_rori", "Rori Herbivore Meat"));
        rGroup.add(new ResourceClass("meat_herbivore_talus", "Talusian Herbivore Meat"));
        rGroup.add(new ResourceClass("meat_herbivore_tatooine", "Tatooinian Herbivore Meat"));
        rGroup.add(new ResourceClass("meat_herbivore_yavin4", "Yavinian Herbivore Meat"));
        rGroup.add(new ResourceClass("meat_herbivore_dxun", "Dxun Herbivore Meat"));
        rGroup.add(new ResourceClass("meat_insect", "Insect Meat"));
        rGroup.add(new ResourceClass("meat_insect_corellia", "Corellian Insect Meat"));
        rGroup.add(new ResourceClass("meat_insect_dantooine", "Dantooine Insect Meat"));
        rGroup.add(new ResourceClass("meat_insect_dathomir", "Dathomirian Insect Meat"));
        rGroup.add(new ResourceClass("meat_insect_endor", "Endorian Insect Meat"));
        rGroup.add(new ResourceClass("meat_insect_kashyyyk", "Kashyyykian Insect Meat"));
        rGroup.add(new ResourceClass("meat_insect_lok", "Lokian Insect Meat"));
        rGroup.add(new ResourceClass("meat_insect_mustafar", "Mustafarian Insect Meat"));
        rGroup.add(new ResourceClass("meat_insect_naboo", "Nabooian Insect Meat"));
        rGroup.add(new ResourceClass("meat_insect_rori", "Rori Insect Meat"));
        rGroup.add(new ResourceClass("meat_insect_talus", "Talusian Insect Meat"));
        rGroup.add(new ResourceClass("meat_insect_tatooine", "Tatooinian Insect Meat"));
        rGroup.add(new ResourceClass("meat_insect_yavin4", "Yavinian Insect Meat"));
        rGroup.add(new ResourceClass("meat_insect_dxun", "Dxun Insect Meat"));
        rGroup.add(new ResourceClass("meat_reptilian_corellia", "Corellian Reptilian Meat"));
        rGroup.add(new ResourceClass("meat_reptilian_dantooine", "Dantooine Reptilian Meat"));
        rGroup.add(new ResourceClass("meat_reptilian_dathomir", "Dathomirian Reptilian Meat"));
        rGroup.add(new ResourceClass("meat_reptilian_endor", "Endorian Reptilian Meat"));
        rGroup.add(new ResourceClass("meat_reptilian_kashyyyk", "Kashyyykian Reptilian Meat"));
        rGroup.add(new ResourceClass("meat_reptilian_lok", "Lokian Reptilian Meat"));
        rGroup.add(new ResourceClass("meat_reptilian_mustafar", "Mustafarian Reptilian Meat"));
        rGroup.add(new ResourceClass("meat_reptilian_naboo", "Nabooian Reptilian Meat"));
        rGroup.add(new ResourceClass("meat_reptilian_rori", "Rori Reptilian Meat"));
        rGroup.add(new ResourceClass("meat_reptilian_talus", "Talusian Reptilian Meat"));
        rGroup.add(new ResourceClass("meat_reptilian_tatooine", "Tatooinian Reptilian Meat"));
        rGroup.add(new ResourceClass("meat_reptilian_yavin4", "Yavinian Reptilian Meat"));
        rGroup.add(new ResourceClass("meat_reptilian_dxun", "Dxun Reptilian Meat"));
        rGroup.add(new ResourceClass("meat_reptillian", "Reptilian Meat"));
        rGroup.add(new ResourceClass("meat_wild", "Wild Meat"));
        rGroup.add(new ResourceClass("meat_wild_corellia", "Corellian Wild Meat"));
        rGroup.add(new ResourceClass("meat_wild_dantooine", "Dantooine Wild Meat"));
        rGroup.add(new ResourceClass("meat_wild_dathomir", "Dathomirian Wild Meat"));
        rGroup.add(new ResourceClass("meat_wild_endor", "Endorian Wild Meat"));
        rGroup.add(new ResourceClass("meat_wild_kashyyyk", "Kashyyykian Wild Meat"));
        rGroup.add(new ResourceClass("meat_wild_lok", "Lokian Wild Meat"));
        rGroup.add(new ResourceClass("meat_wild_mustafar", "Mustafarian Wild Meat"));
        rGroup.add(new ResourceClass("meat_wild_naboo", "Nabooian Wild Meat"));
        rGroup.add(new ResourceClass("meat_wild_rori", "Rori Wild Meat"));
        rGroup.add(new ResourceClass("meat_wild_talus", "Talusian Wild Meat"));
        rGroup.add(new ResourceClass("meat_wild_tatooine", "Tatooinian Wild Meat"));
        rGroup.add(new ResourceClass("meat_wild_yavin4", "Yavinian Wild Meat"));
        rGroup.add(new ResourceClass("meat_wild_dxun", "Dxun Wild Meat"));
        rGroup.add(new ResourceClass("meatl_nonferrous_unknown", "Unknown Non-Ferrous Metal"));
        rGroup.add(new ResourceClass("metal", "Metal"));
        rGroup.add(new ResourceClass("metal_ferrous", "Ferrous Metal"));
        rGroup.add(new ResourceClass("metal_ferrous_unknown", "Unknown Ferrous Metal"));
        rGroup.add(new ResourceClass("metal_nonferrous", "Non-Ferrous Metal"));
        rGroup.add(new ResourceClass("milk", "Milk"));
        rGroup.add(new ResourceClass("milk_domesticated", "Domesticated Milk"));
        rGroup.add(new ResourceClass("milk_domesticated_corellia", "Corellian Domesticated Milk"));
        rGroup.add(new ResourceClass("milk_domesticated_dantooine", "Dantooine Domesticated Milk"));
        rGroup.add(new ResourceClass("milk_domesticated_dathomir", "Dathomirian Domesticated Milk"));
        rGroup.add(new ResourceClass("milk_domesticated_endor", "Endorian Domesticated Milk"));
        rGroup.add(new ResourceClass("milk_domesticated_kashyyyk", "Kashyyykian Domesticated Milk"));
        rGroup.add(new ResourceClass("milk_domesticated_lok", "Lokian Domesticated Milk"));
        rGroup.add(new ResourceClass("milk_domesticated_mustafar", "Mustafarian Domesticated Milk"));
        rGroup.add(new ResourceClass("milk_domesticated_naboo", "Nabooian Domesticated Milk"));
        rGroup.add(new ResourceClass("milk_domesticated_rori", "Rori Domesticated Milk"));
        rGroup.add(new ResourceClass("milk_domesticated_talus", "Talusian Domesticated Milk"));
        rGroup.add(new ResourceClass("milk_domesticated_tatooine", "Tatooinian Domesticated Milk"));
        rGroup.add(new ResourceClass("milk_domesticated_yavin4", "Yavinian Domesticated Milk"));
        rGroup.add(new ResourceClass("milk_domesticated_dxun", "Dxun Domesticated Milk"));
        rGroup.add(new ResourceClass("milk_homogenized", "Homogenized Milk"));
        rGroup.add(new ResourceClass("milk_wild", "Wild Milk"));
        rGroup.add(new ResourceClass("milk_wild_corellia", "Corellian Wild Milk"));
        rGroup.add(new ResourceClass("milk_wild_dantooine", "Dantooine Wild Milk"));
        rGroup.add(new ResourceClass("milk_wild_dathomir", "Dathomirian Wild Milk"));
        rGroup.add(new ResourceClass("milk_wild_endor", "Endorian Wild Milk"));
        rGroup.add(new ResourceClass("milk_wild_kashyyyk", "Kashyyykian Wild Milk"));
        rGroup.add(new ResourceClass("milk_wild_lok", "Lokian Wild Milk"));
        rGroup.add(new ResourceClass("milk_wild_mustafar", "Mustafarian Wild Milk"));
        rGroup.add(new ResourceClass("milk_wild_naboo", "Nabooian Wild Milk"));
        rGroup.add(new ResourceClass("milk_wild_rori", "Rori Wild Milk"));
        rGroup.add(new ResourceClass("milk_wild_talus", "Talusian Wild Milk"));
        rGroup.add(new ResourceClass("milk_wild_tatooine", "Tatooinian Wild Milk"));
        rGroup.add(new ResourceClass("milk_wild_yavin4", "Yavinian Wild Milk"));
        rGroup.add(new ResourceClass("milk_wild_dxun", "Dxun Wild Milk"));
        rGroup.add(new ResourceClass("mineral", "Mineral"));
        rGroup.add(new ResourceClass("mixed_fruits", "Mixed Fruits"));
        rGroup.add(new ResourceClass("mixed_vegetables", "Mixed Vegetables"));
        rGroup.add(new ResourceClass("oats", "Oats"));
        rGroup.add(new ResourceClass("oats_domesticated", "Domesticated Oats"));
        rGroup.add(new ResourceClass("oats_domesticated_corellia", "Corellian Domesticated Oats"));
        rGroup.add(new ResourceClass("oats_domesticated_dantooine", "Dantooinian Domesticated Oats"));
        rGroup.add(new ResourceClass("oats_domesticated_dathomir", "Dathomirian Domesticated Oats"));
        rGroup.add(new ResourceClass("oats_domesticated_endor", "Endorian Domesticated Oats"));
        rGroup.add(new ResourceClass("oats_domesticated_kashyyyk", "Kashyyykian Domesticated Oats"));
        rGroup.add(new ResourceClass("oats_domesticated_lok", "Lokian Domesticated Oats"));
        rGroup.add(new ResourceClass("oats_domesticated_mustafar", "Mustafarian Domesticated Oats"));
        rGroup.add(new ResourceClass("oats_domesticated_naboo", "Nabooian Domesticated Oats"));
        rGroup.add(new ResourceClass("oats_domesticated_rori", "Rori Domesticated Oats"));
        rGroup.add(new ResourceClass("oats_domesticated_talus", "Talusian Domesticated Oats"));
        rGroup.add(new ResourceClass("oats_domesticated_tatooine", "Tatooinian Domesticated Oats"));
        rGroup.add(new ResourceClass("oats_domesticated_yavin4", "Yavinian Domesticated Oats"));
        rGroup.add(new ResourceClass("oats_domesticated_dxun", "Dxun Domesticated Oats"));
        rGroup.add(new ResourceClass("oats_wild", "Wild Oats"));
        rGroup.add(new ResourceClass("oats_wild_corellia", "Corellian Wild Oats"));
        rGroup.add(new ResourceClass("oats_wild_dantooine", "Dantooinian Wild Oats"));
        rGroup.add(new ResourceClass("oats_wild_dathomir", "Dathomirian Wild Oats"));
        rGroup.add(new ResourceClass("oats_wild_endor", "Endorian Wild Oats"));
        rGroup.add(new ResourceClass("oats_wild_kashyyyk", "Kashyyykian Wild Oats"));
        rGroup.add(new ResourceClass("oats_wild_lok", "Lokian Wild Oats"));
        rGroup.add(new ResourceClass("oats_wild_mustafar", "Mustafarian Wild Oats"));
        rGroup.add(new ResourceClass("oats_wild_naboo", "Nabooian Wild Oats"));
        rGroup.add(new ResourceClass("oats_wild_rori", "Rori Wild Oats"));
        rGroup.add(new ResourceClass("oats_wild_talus", "Talusian Wild Oats"));
        rGroup.add(new ResourceClass("oats_wild_tatooine", "Tatooinian Wild Oats"));
        rGroup.add(new ResourceClass("oats_wild_yavin4", "Yavinian Wild Oats"));
        rGroup.add(new ResourceClass("oats_wild_dxun", "Dxun Wild Oats"));
        rGroup.add(new ResourceClass("ore", "Low-Grade Ore"));
        rGroup.add(new ResourceClass("ore_carbonate", "Carbonate Ore"));
        rGroup.add(new ResourceClass("ore_carbonate_alantium", "Alantium Carbonate Ore"));
        rGroup.add(new ResourceClass("ore_carbonate_barthierium", "Barthierium Carbonate Ore"));
        rGroup.add(new ResourceClass("ore_carbonate_chromite", "Chromite Carbonate Ore"));
        rGroup.add(new ResourceClass("ore_carbonate_frasium", "Frasium Carbonate Ore"));
        rGroup.add(new ResourceClass("ore_carbonate_lommite", "Lommite Carbonate Ore"));
        rGroup.add(new ResourceClass("ore_carbonate_low_grade", "Low Grade Ore (Sedimentary)"));
        rGroup.add(new ResourceClass("ore_carbonate_ostrine", "Ostrine Carbonate Ore"));
        rGroup.add(new ResourceClass("ore_carbonate_varium", "Varium Carbonate Ore"));
        rGroup.add(new ResourceClass("ore_carbonate_zinsiam", "Zinsiam Carbonate Ore"));
        rGroup.add(new ResourceClass("ore_extrusive", "Extrusive Ore"));
        rGroup.add(new ResourceClass("ore_extrusive_bene", "Bene Extrusive Ore"));
        rGroup.add(new ResourceClass("ore_extrusive_chronamite", "Chronamite Extrusive Ore"));
        rGroup.add(new ResourceClass("ore_extrusive_ilimium", "Ilimium Extrusive Ore"));
        rGroup.add(new ResourceClass("ore_extrusive_kalonterium", "Kalonterium Extrusive Ore"));
        rGroup.add(new ResourceClass("ore_extrusive_keschel", "Keschel Extrusive Ore"));
        rGroup.add(new ResourceClass("ore_extrusive_lidium", "Lidium Extrusive Ore"));
        rGroup.add(new ResourceClass("ore_extrusive_low_grade", "Low Grade Ore (Igneous)"));
        rGroup.add(new ResourceClass("ore_extrusive_maranium", "Maranium Extrusive Ore"));
        rGroup.add(new ResourceClass("ore_extrusive_mustafar", "Mustafarian Extrusive Ore"));
        rGroup.add(new ResourceClass("ore_extrusive_pholokite", "Pholokite Extrusive Ore"));
        rGroup.add(new ResourceClass("ore_extrusive_quadrenium", "Quadrenium Extrusive Ore"));
        rGroup.add(new ResourceClass("ore_extrusive_vintrium", "Vintrium Extrusive Ore"));
        rGroup.add(new ResourceClass("ore_igneous", "Igneous Ore"));
        rGroup.add(new ResourceClass("ore_igneous_unknown", "Unknown Igneous Ore"));
        rGroup.add(new ResourceClass("ore_intrusive", "Intrusive Ore"));
        rGroup.add(new ResourceClass("ore_intrusive_berubium", "Berubium Intrusive Ore"));
        rGroup.add(new ResourceClass("ore_intrusive_chanlon", "Chanlon Intrusive Ore"));
        rGroup.add(new ResourceClass("ore_intrusive_corintium", "Corintium Intrusive Ore"));
        rGroup.add(new ResourceClass("ore_intrusive_derillium", "Derillium Intrusive Ore"));
        rGroup.add(new ResourceClass("ore_intrusive_dylinium", "Dylinium Intrusive Ore"));
        rGroup.add(new ResourceClass("ore_intrusive_hollinium", "Hollinium Intrusive Ore"));
        rGroup.add(new ResourceClass("ore_intrusive_ionite", "Ionite Intrusive Ore"));
        rGroup.add(new ResourceClass("ore_intrusive_katrium", "Katrium Intrusive Ore"));
        rGroup.add(new ResourceClass("ore_intrusive_mustafar", "Mustafarian Intrusive Ore"));
        rGroup.add(new ResourceClass("ore_intrusive_oridium", "Oridium Intrusive Ore"));
        rGroup.add(new ResourceClass("ore_sedimentary", "Sedimentary Ore"));
        rGroup.add(new ResourceClass("ore_sedimentary_unknown", "Unknown Sedimentary Ore"));
        rGroup.add(new ResourceClass("ore_siliclastic", "Siliclastic Ore"));
        rGroup.add(new ResourceClass("ore_siliclastic_ardanium", "Ardanium Siliclastic Ore"));
        rGroup.add(new ResourceClass("ore_siliclastic_cortosis", "Cortosis Siliclastic Ore"));
        rGroup.add(new ResourceClass("ore_siliclastic_crism", "Crism Siliclastic Ore"));
        rGroup.add(new ResourceClass("ore_siliclastic_fermionic", "Fermionic Siliclastic Ore"));
        rGroup.add(new ResourceClass("ore_siliclastic_low_grade", "Low Grade Ore (Siliclastic)"));
        rGroup.add(new ResourceClass("ore_siliclastic_malab", "Malab Siliclastic Ore"));
        rGroup.add(new ResourceClass("ore_siliclastic_robindun", "Robindun Siliclastic Ore"));
        rGroup.add(new ResourceClass("ore_siliclastic_tertian", "Tertian Siliclastic Ore"));
        rGroup.add(new ResourceClass("organic", "Organic"));
        rGroup.add(new ResourceClass("petrochem_fuel_liquid_mustafar", "Mustafarian Liquid Petro Fuel"));
        rGroup.add(new ResourceClass("petrochem_fuel_liquid_type1", "Class 1 Liquid Petro Fuel"));
        rGroup.add(new ResourceClass("petrochem_fuel_liquid_type2", "Class 2 Liquid Petro Fuel"));
        rGroup.add(new ResourceClass("petrochem_fuel_liquid_type3", "Class 3 Liquid Petro Fuel"));
        rGroup.add(new ResourceClass("petrochem_fuel_liquid_type4", "Class 4 Liquid Petro Fuel"));
        rGroup.add(new ResourceClass("petrochem_fuel_liquid_type5", "Class 5 Liquid Petro Fuel"));
        rGroup.add(new ResourceClass("petrochem_fuel_liquid_type6", "Class 6 Liquid Petro Fuel"));
        rGroup.add(new ResourceClass("petrochem_fuel_liquid_type7", "Class 7 Liquid Petro Fuel"));
        rGroup.add(new ResourceClass("petrochem_fuel_liquid_unknown", "Unknown Liquid Petrochem Fuel"));
        rGroup.add(new ResourceClass("petrochem_fuel_solid_mustafar", "Mustafarian Solid Petro Fuel"));
        rGroup.add(new ResourceClass("petrochem_fuel_solid_type1", "Class 1 Solid Petro Fuel"));
        rGroup.add(new ResourceClass("petrochem_fuel_solid_type2", "Class 2 Solid Petro Fuel"));
        rGroup.add(new ResourceClass("petrochem_fuel_solid_type3", "Class 3 Solid Petro Fuel"));
        rGroup.add(new ResourceClass("petrochem_fuel_solid_type4", "Class 4 Solid Petro Fuel"));
        rGroup.add(new ResourceClass("petrochem_fuel_solid_type5", "Class 5 Solid Petro Fuel"));
        rGroup.add(new ResourceClass("petrochem_fuel_solid_type6", "Class 6 Solid Petro Fuel"));
        rGroup.add(new ResourceClass("petrochem_fuel_solid_type7", "Class 7 Solid Petro Fuel"));
        rGroup.add(new ResourceClass("petrochem_fuel_solid_unknown", "Unknown Solid Petrochem Fuel"));
        rGroup.add(new ResourceClass("petrochem_inert", "Inert Petrochemical"));
        rGroup.add(new ResourceClass("petrochem_inert_lubricating_oil", "Lubricating Oil"));
        rGroup.add(new ResourceClass("petrochem_inert_polymer", "Polymer"));
        rGroup.add(new ResourceClass("processed_cereal", "Processed Cereal"));
        rGroup.add(new ResourceClass("processed_meat", "Processed Meat"));
        rGroup.add(new ResourceClass("processed_seafood", "Processed Seafood"));
        rGroup.add(new ResourceClass("processed_wood", "Blended Wood"));
        rGroup.add(new ResourceClass("radioactive", "Radioactive"));
        rGroup.add(new ResourceClass("radioactive_known", "Known Radioactive"));
        rGroup.add(new ResourceClass("radioactive_mustafar", "Mustafarian Radioactive"));
        rGroup.add(new ResourceClass("radioactive_polymetric", "High Grade Polymetric Radioactive"));
        rGroup.add(new ResourceClass("radioactive_type1", "Class 1 Radioactive"));
        rGroup.add(new ResourceClass("radioactive_type2", "Class 2 Radioactive"));
        rGroup.add(new ResourceClass("radioactive_type3", "Class 3 Radioactive"));
        rGroup.add(new ResourceClass("radioactive_type4", "Class 4 Radioactive"));
        rGroup.add(new ResourceClass("radioactive_type5", "Class 5 Radioactive"));
        rGroup.add(new ResourceClass("radioactive_type6", "Class 6 Radioactive"));
        rGroup.add(new ResourceClass("radioactive_type7", "Class 7 Radioactive"));
        rGroup.add(new ResourceClass("radioactive_unknown", "Unknown Radioactive Isotopes"));
        rGroup.add(new ResourceClass("resources", "Resources"));
        rGroup.add(new ResourceClass("rice", "Rice"));
        rGroup.add(new ResourceClass("rice_domesticated", "Domesticated Rice"));
        rGroup.add(new ResourceClass("rice_domesticated_corellia", "Corellian Domesticated Rice"));
        rGroup.add(new ResourceClass("rice_domesticated_dantooine", "Dantooine Domesticated Rice"));
        rGroup.add(new ResourceClass("rice_domesticated_dathomir", "Dathomirian Domesticated Rice"));
        rGroup.add(new ResourceClass("rice_domesticated_endor", "Endorian Domesticated Rice"));
        rGroup.add(new ResourceClass("rice_domesticated_kashyyyk", "Kashyyykian Domesticated Rice"));
        rGroup.add(new ResourceClass("rice_domesticated_lok", "Lokian Domesticated Rice"));
        rGroup.add(new ResourceClass("rice_domesticated_mustafar", "Mustafarian Domesticated Rice"));
        rGroup.add(new ResourceClass("rice_domesticated_naboo", "Nabooian Domesticated Rice"));
        rGroup.add(new ResourceClass("rice_domesticated_rori", "Rori Domesticated Rice"));
        rGroup.add(new ResourceClass("rice_domesticated_talus", "Talusian Domesticated Rice"));
        rGroup.add(new ResourceClass("rice_domesticated_tatooine", "Tatooinian Domesticated Rice"));
        rGroup.add(new ResourceClass("rice_domesticated_yavin4", "Yavinian Domesticated Rice"));
        rGroup.add(new ResourceClass("rice_domesticated_dxun", "Dxun Domesticated Rice"));
        rGroup.add(new ResourceClass("rice_wild", "Wild Rice"));
        rGroup.add(new ResourceClass("rice_wild_corellia", "Corellian Wild Rice"));
        rGroup.add(new ResourceClass("rice_wild_dantooine", "Dantooine Wild Rice"));
        rGroup.add(new ResourceClass("rice_wild_dathomir", "Dathomirian Wild Rice"));
        rGroup.add(new ResourceClass("rice_wild_endor", "Endorian Wild Rice"));
        rGroup.add(new ResourceClass("rice_wild_kashyyyk", "Kashyyykian Wild Rice"));
        rGroup.add(new ResourceClass("rice_wild_lok", "Lokian Wild Rice"));
        rGroup.add(new ResourceClass("rice_wild_mustafar", "Mustafarian Wild Rice"));
        rGroup.add(new ResourceClass("rice_wild_naboo", "Nabooian Wild Rice"));
        rGroup.add(new ResourceClass("rice_wild_rori", "Rori Wild Rice"));
        rGroup.add(new ResourceClass("rice_wild_talus", "Talusian Wild Rice"));
        rGroup.add(new ResourceClass("rice_wild_tatooine", "Tatooinian Wild Rice"));
        rGroup.add(new ResourceClass("rice_wild_yavin4", "Yavinian Wild Rice"));
        rGroup.add(new ResourceClass("rice_wild_dxun", "Dxun Wild Rice"));
        rGroup.add(new ResourceClass("seafood", "Seafood"));
        rGroup.add(new ResourceClass("seafood_crustacean", "Crustacean"));
        rGroup.add(new ResourceClass("seafood_crustacean_corellia", "Corellian Crustacean Meat"));
        rGroup.add(new ResourceClass("seafood_crustacean_dantooine", "Dantooine Crustacean Meat"));
        rGroup.add(new ResourceClass("seafood_crustacean_dathomir", "Dathomirian Crustacean Meat"));
        rGroup.add(new ResourceClass("seafood_crustacean_endor", "Endorian Crustancean Meat"));
        rGroup.add(new ResourceClass("seafood_crustacean_kashyyyk", "Kashyyykian Crustacean Meat"));
        rGroup.add(new ResourceClass("seafood_crustacean_lok", "Lokian Crustacean Meat"));
        rGroup.add(new ResourceClass("seafood_crustacean_mustafar", "Mustafarian Crustacean Meat"));
        rGroup.add(new ResourceClass("seafood_crustacean_naboo", "Nabooian Crustacean Meat"));
        rGroup.add(new ResourceClass("seafood_crustacean_rori", "Rori Crustacean Meat"));
        rGroup.add(new ResourceClass("seafood_crustacean_talus", "Talusian Crustacean Meat"));
        rGroup.add(new ResourceClass("seafood_crustacean_tatooine", "Tatooinian Crustacean Meat"));
        rGroup.add(new ResourceClass("seafood_crustacean_yavin4", "Yavinian Crustacean Meat"));
        rGroup.add(new ResourceClass("seafood_crustacean_dxun", "Dxun Crustacean Meat"));
        rGroup.add(new ResourceClass("seafood_fish", "Fish"));
        rGroup.add(new ResourceClass("seafood_fish_corellia", "Corellian Fish Meat"));
        rGroup.add(new ResourceClass("seafood_fish_dantooine", "Dantooine Fish Meat"));
        rGroup.add(new ResourceClass("seafood_fish_dathomir", "Dathomirian Fish Meat"));
        rGroup.add(new ResourceClass("seafood_fish_endor", "Endorian Fish Meat"));
        rGroup.add(new ResourceClass("seafood_fish_kashyyyk", "Kashyyykian Fish Meat"));
        rGroup.add(new ResourceClass("seafood_fish_lok", "Lokian Fish Meat"));
        rGroup.add(new ResourceClass("seafood_fish_mustafar", "Mustafarian Fish Meat"));
        rGroup.add(new ResourceClass("seafood_fish_naboo", "Nabooian Fish Meat"));
        rGroup.add(new ResourceClass("seafood_fish_rori", "Rori Fish Meat"));
        rGroup.add(new ResourceClass("seafood_fish_talus", "Talusian Fish Meat"));
        rGroup.add(new ResourceClass("seafood_fish_tatooine", "Tatooinian Fish Meat"));
        rGroup.add(new ResourceClass("seafood_fish_yavin4", "Yavinian Fish Meat"));
        rGroup.add(new ResourceClass("seafood_fish_dxun", "Dxun Fish Meat"));
        rGroup.add(new ResourceClass("seafood_mollusk", "Mollusk"));
        rGroup.add(new ResourceClass("seafood_mollusk_corellia", "Corellian Mollusk Meat"));
        rGroup.add(new ResourceClass("seafood_mollusk_dantooine", "Dantooine Mollusk Meat"));
        rGroup.add(new ResourceClass("seafood_mollusk_dathomir", "Dathomirian Mollusk Meat"));
        rGroup.add(new ResourceClass("seafood_mollusk_endor", "Endorian Mollusk Meat"));
        rGroup.add(new ResourceClass("seafood_mollusk_kashyyyk", "Kashyyykian Mollusk Meat"));
        rGroup.add(new ResourceClass("seafood_mollusk_lok", "Lokian Mollusk Meat"));
        rGroup.add(new ResourceClass("seafood_mollusk_mustafar", "Mustafarian Mollusk Meat"));
        rGroup.add(new ResourceClass("seafood_mollusk_naboo", "Nabooian Mollusk Meat"));
        rGroup.add(new ResourceClass("seafood_mollusk_rori", "Rori Mollusk Meat"));
        rGroup.add(new ResourceClass("seafood_mollusk_talus", "Talusian Mollusk Meat"));
        rGroup.add(new ResourceClass("seafood_mollusk_tatooine", "Tatooinian Mollusk Meat"));
        rGroup.add(new ResourceClass("seafood_mollusk_yavin4", "Yavinian Mollusk Meat"));
        rGroup.add(new ResourceClass("seafood_mollusk_dxun", "Dxun Mollusk Meat"));
        rGroup.add(new ResourceClass("seeds", "Seeds"));
        rGroup.add(new ResourceClass("smelted_metal_ferrous_unknown", "Smelted Ferrous Metal"));
        rGroup.add(new ResourceClass("smelted_metal_nonferrous_unknown", "Smelted Non-Ferrous Metal"));
        rGroup.add(new ResourceClass("softwood", "Soft Wood"));
        rGroup.add(new ResourceClass("softwood_conifer_corellia", "Corellian Conifer Wood"));
        rGroup.add(new ResourceClass("softwood_conifer_dantooine", "Dantooine Conifer Wood"));
        rGroup.add(new ResourceClass("softwood_conifer_dathomir", "Dathomirian Conifer Wood"));
        rGroup.add(new ResourceClass("softwood_conifer_endor", "Endorian Conifer Wood"));
        rGroup.add(new ResourceClass("softwood_conifer_kashyyyk", "Kashyyykian Conifer Wood"));
        rGroup.add(new ResourceClass("softwood_conifer_lok", "Lokian Conifer Wood"));
        rGroup.add(new ResourceClass("softwood_conifer_mustafar", "Mustafarian Conifer Wood"));
        rGroup.add(new ResourceClass("softwood_conifer_naboo", "Nabooian Conifer Wood"));
        rGroup.add(new ResourceClass("softwood_conifer_rori", "Rori Conifer Wood"));
        rGroup.add(new ResourceClass("softwood_conifer_talus", "Talusian Conifer Wood"));
        rGroup.add(new ResourceClass("softwood_conifer_tatooine", "Tatooinian Conifer Wood"));
        rGroup.add(new ResourceClass("softwood_conifer_yavin4", "Yavinian Conifer Wood"));
        rGroup.add(new ResourceClass("softwood_conifer_dxun", "Dxun Conifer Wood"));
        rGroup.add(new ResourceClass("softwood_evergreen", "Evergreen Soft Wood"));
        rGroup.add(new ResourceClass("softwood_evergreen_corellia", "Corellian Evergreen Wood"));
        rGroup.add(new ResourceClass("softwood_evergreen_dantooine", "Dantooine Evergreen Wood"));
        rGroup.add(new ResourceClass("softwood_evergreen_dathomir", "Dathomirian Evergreen Wood"));
        rGroup.add(new ResourceClass("softwood_evergreen_endor", "Endorian Evergreen Wood"));
        rGroup.add(new ResourceClass("softwood_evergreen_kashyyyk", "Kashyyykian Evergreen Wood"));
        rGroup.add(new ResourceClass("softwood_evergreen_lok", "Lokian Evergreen Wood"));
        rGroup.add(new ResourceClass("softwood_evergreen_mustafar", "Mustafarian Evergreen Wood"));
        rGroup.add(new ResourceClass("softwood_evergreen_naboo", "Nabooian Evergreen Wood"));
        rGroup.add(new ResourceClass("softwood_evergreen_rori", "Rorian Evergreen Wood"));
        rGroup.add(new ResourceClass("softwood_evergreen_talus", "Talusian Evergreen Wood"));
        rGroup.add(new ResourceClass("softwood_evergreen_tatooine", "Tatooinian Evergreen Wood"));
        rGroup.add(new ResourceClass("softwood_evergreen_yavin4", "Yavinian Evergreen Wood"));
        rGroup.add(new ResourceClass("softwood_evergreen_dxun", "Dxun Evergreen Wood"));
        rGroup.add(new ResourceClass("space_chemical", "Asteroidal Chemical"));
        rGroup.add(new ResourceClass("space_chemical_acid", "Acidic Asteroid"));
        rGroup.add(new ResourceClass("space_chemical_cyanomethanic", "Cyanomethanic Asteroid"));
        rGroup.add(new ResourceClass("space_chemical_petrochem", "Petrochemical Asteroid"));
        rGroup.add(new ResourceClass("space_chemical_sulfuric", "Sulfuric Asteroid"));
        rGroup.add(new ResourceClass("space_gas", "Asteroidal Gas"));
        rGroup.add(new ResourceClass("space_gas_methane", "Methane Asteroid"));
        rGroup.add(new ResourceClass("space_gas_organometallic", "Organometallic Asteroid"));
        rGroup.add(new ResourceClass("space_gem", "Asteroidal Gemstone"));
        rGroup.add(new ResourceClass("space_gem_crystal", "Crystal Asteroid"));
        rGroup.add(new ResourceClass("space_gem_diamond", "Diamond Asteroid"));
        rGroup.add(new ResourceClass("space_metal", "Asteroidal Mineral"));
        rGroup.add(new ResourceClass("space_metal_carbonaceous", "Carbonaceous Asteroid"));
        rGroup.add(new ResourceClass("space_metal_ice", "Icy Asteroid"));
        rGroup.add(new ResourceClass("space_metal_iron", "Iron Asteroid"));
        rGroup.add(new ResourceClass("space_metal_obsidian", "Obsidian Asteroid"));
        rGroup.add(new ResourceClass("space_metal_silicaceous", "Silicaceous Asteroid"));
        rGroup.add(new ResourceClass("space_resource", "Space Resource"));
        rGroup.add(new ResourceClass("steel", "Steel"));
        rGroup.add(new ResourceClass("steel_arveshian", "Hardened Arveshium Steel"));
        rGroup.add(new ResourceClass("steel_bicorbanium", "Crystallized Bicorbantium Steel"));
        rGroup.add(new ResourceClass("steel_bicorbantium", "Crystallized Bicorbantium Steel"));
        rGroup.add(new ResourceClass("steel_carbonite", "Carbonite Steel"));
        rGroup.add(new ResourceClass("steel_cubirian", "Cubirian Steel"));
        rGroup.add(new ResourceClass("steel_ditanium", "Ditanium Steel"));
        rGroup.add(new ResourceClass("steel_duralloy", "Duralloy Steel"));
        rGroup.add(new ResourceClass("steel_duranium", "Duranium Steel"));
        rGroup.add(new ResourceClass("steel_kiirium", "Kiirium Steel"));
        rGroup.add(new ResourceClass("steel_mustafar", "Mustafarian Steel"));
        rGroup.add(new ResourceClass("steel_neutronium", "Neutronium Steel"));
        rGroup.add(new ResourceClass("steel_quadranium", "Quadranium Steel"));
        rGroup.add(new ResourceClass("steel_rhodium", "Rhodium Steel"));
        rGroup.add(new ResourceClass("steel_smelted", "Smelted Steel"));
        rGroup.add(new ResourceClass("steel_thoranium", "Thoranium Steel"));
        rGroup.add(new ResourceClass("synthesized_hides", "Synthesized Hides"));
        rGroup.add(new ResourceClass("vegetable", "Vegetables"));
        rGroup.add(new ResourceClass("vegetable_beans", "Beans"));
        rGroup.add(new ResourceClass("vegetable_beans_corellia", "Corellian Vegetable Beans"));
        rGroup.add(new ResourceClass("vegetable_beans_dantooine", "Dantooine Vegetable Beans"));
        rGroup.add(new ResourceClass("vegetable_beans_dathomir", "Dathomirian Vegetable Beans"));
        rGroup.add(new ResourceClass("vegetable_beans_endor", "Endorian Vegetable Beans"));
        rGroup.add(new ResourceClass("vegetable_beans_kashyyyk", "Kashyyykian Vegetable Beans"));
        rGroup.add(new ResourceClass("vegetable_beans_lok", "Lokian Vegetable Beans"));
        rGroup.add(new ResourceClass("vegetable_beans_mustafar", "Mustafarian Vegetable Beans"));
        rGroup.add(new ResourceClass("vegetable_beans_naboo", "Nabooian Vegetable Beans"));
        rGroup.add(new ResourceClass("vegetable_beans_rori", "Rori Vegetable Beans"));
        rGroup.add(new ResourceClass("vegetable_beans_talus", "Talusian Vegetable Beans"));
        rGroup.add(new ResourceClass("vegetable_beans_tatooine", "Tatooinian Vegetable Beans"));
        rGroup.add(new ResourceClass("vegetable_beans_yavin4", "Yavinian Vegetable Beans"));
        rGroup.add(new ResourceClass("vegetable_beans_dxun", "Dxun Vegetable Beans"));
        rGroup.add(new ResourceClass("vegetable_fungi", "Fungi"));
        rGroup.add(new ResourceClass("vegetable_fungi_corellia", "Corellian Vegetable Fungus"));
        rGroup.add(new ResourceClass("vegetable_fungi_dantooine", "Dantooine Vegetable Fungus"));
        rGroup.add(new ResourceClass("vegetable_fungi_dathomir", "Dathomirian Vegetable Fungus"));
        rGroup.add(new ResourceClass("vegetable_fungi_endor", "Endorian Vegetable Fungus"));
        rGroup.add(new ResourceClass("vegetable_fungi_kashyyyk", "Kashyyykian Vegetable Fungus"));
        rGroup.add(new ResourceClass("vegetable_fungi_lok", "Lokian Vegetable Fungus"));
        rGroup.add(new ResourceClass("vegetable_fungi_mustafar", "Mustafarian Vegetable Fungus"));
        rGroup.add(new ResourceClass("vegetable_fungi_naboo", "Nabooian Vegetable Fungus"));
        rGroup.add(new ResourceClass("vegetable_fungi_rori", "Rori Vegetable Fungus"));
        rGroup.add(new ResourceClass("vegetable_fungi_talus", "Talusian Vegetable Fungus"));
        rGroup.add(new ResourceClass("vegetable_fungi_tatooine", "Tatooinian Vegetable Fungus"));
        rGroup.add(new ResourceClass("vegetable_fungi_yavin4", "Yavinian Vegetable Fungus"));
        rGroup.add(new ResourceClass("vegetable_fungi_dxun", "Dxun Vegetable Fungus"));
        rGroup.add(new ResourceClass("vegetable_greens", "Greens"));
        rGroup.add(new ResourceClass("vegetable_greens_corellia", "Corellian Vegetable Greens"));
        rGroup.add(new ResourceClass("vegetable_greens_dantooine", "Dantooine Vegetable Greens"));
        rGroup.add(new ResourceClass("vegetable_greens_dathomir", "Dathomirian Vegetable Greens"));
        rGroup.add(new ResourceClass("vegetable_greens_endor", "Endorian Vegetable Greens"));
        rGroup.add(new ResourceClass("vegetable_greens_kashyyyk", "Kashyyykian Vegetable Greens"));
        rGroup.add(new ResourceClass("vegetable_greens_lok", "Lokian Vegetable Greens"));
        rGroup.add(new ResourceClass("vegetable_greens_mustafar", "Mustafarian Vegetable Greens"));
        rGroup.add(new ResourceClass("vegetable_greens_naboo", "Nabooian Vegetable Greens"));
        rGroup.add(new ResourceClass("vegetable_greens_rori", "Rori Vegetable Greens"));
        rGroup.add(new ResourceClass("vegetable_greens_talus", "Talusian Vegetable Greens"));
        rGroup.add(new ResourceClass("vegetable_greens_tatooine", "Tatooinian Vegetable Greens"));
        rGroup.add(new ResourceClass("vegetable_greens_yavin4", "Yavinian Vegetable Greens"));
        rGroup.add(new ResourceClass("vegetable_greens_dxun", "Dxun Vegetable Greens"));
        rGroup.add(new ResourceClass("vegetable_tubers", "Tubers"));
        rGroup.add(new ResourceClass("vegetable_tubers_corellia", "Corellian Vegetable Tubers"));
        rGroup.add(new ResourceClass("vegetable_tubers_dantooine", "Dantooine Vegetable Tubers"));
        rGroup.add(new ResourceClass("vegetable_tubers_dathomir", "Dathomirian Vegetable Tubers"));
        rGroup.add(new ResourceClass("vegetable_tubers_endor", "Endorian Vegetable Tubers"));
        rGroup.add(new ResourceClass("vegetable_tubers_kashyyyk", "Kashyyykian Vegetable Tubers"));
        rGroup.add(new ResourceClass("vegetable_tubers_lok", "Lokian Vegetable Tubers"));
        rGroup.add(new ResourceClass("vegetable_tubers_mustafar", "Mustafarian Vegetable Tubers"));
        rGroup.add(new ResourceClass("vegetable_tubers_naboo", "Nabooian Vegetable Tubers"));
        rGroup.add(new ResourceClass("vegetable_tubers_rori", "Rori Vegetable Tubers"));
        rGroup.add(new ResourceClass("vegetable_tubers_talus", "Talusian Vegetable Tubers"));
        rGroup.add(new ResourceClass("vegetable_tubers_tatooine", "Tatooinian Vegetable Tubers"));
        rGroup.add(new ResourceClass("vegetable_tubers_yavin4", "Yavinian Vegetable Tubers"));
        rGroup.add(new ResourceClass("vegetable_tubers_dxun", "Dxun Vegetable Tubers"));
        rGroup.add(new ResourceClass("water", "Water"));
        rGroup.add(new ResourceClass("water_solution", "Water Solution"));
        rGroup.add(new ResourceClass("water_vapor_corellia", "Corellian Water Vapor"));
        rGroup.add(new ResourceClass("water_vapor_dantooine", "Dantooine Water Vapor"));
        rGroup.add(new ResourceClass("water_vapor_dathomir", "Dathomirian Water Vapor"));
        rGroup.add(new ResourceClass("water_vapor_endor", "Endorian Water Vapor"));
        rGroup.add(new ResourceClass("water_vapor_kashyyyk", "Kashyyykian Water Vapor"));
        rGroup.add(new ResourceClass("water_vapor_lok", "Lokian Water Vapor"));
        rGroup.add(new ResourceClass("water_vapor_mustafar", "Mustafarian Water Vapor"));
        rGroup.add(new ResourceClass("water_vapor_naboo", "Nabooian Water Vapor"));
        rGroup.add(new ResourceClass("water_vapor_rori", "Rori Water Vapor"));
        rGroup.add(new ResourceClass("water_vapor_talus", "Talusian Water Vapor"));
        rGroup.add(new ResourceClass("water_vapor_tatooine", "Tatooinian Water Vapor"));
        rGroup.add(new ResourceClass("water_vapor_yavin4", "Yavinian Water Vapor"));
        rGroup.add(new ResourceClass("water_vapor_dxun", "Dxun Water Vapor"));
        rGroup.add(new ResourceClass("wheat", "Wheat"));
        rGroup.add(new ResourceClass("wheat_domesticated", "Domesticated Wheat"));
        rGroup.add(new ResourceClass("wheat_domesticated_corellia", "Corellian Domesticated Wheat"));
        rGroup.add(new ResourceClass("wheat_domesticated_dantooine", "Dantooine Domesticated Wheat"));
        rGroup.add(new ResourceClass("wheat_domesticated_dathomir", "Dathomirian Domesticated Wheat"));
        rGroup.add(new ResourceClass("wheat_domesticated_endor", "Endorian Domesticated Wheat"));
        rGroup.add(new ResourceClass("wheat_domesticated_kashyyyk", "Kashyyykian Domesticated Wheat"));
        rGroup.add(new ResourceClass("wheat_domesticated_lok", "Lokian Domesticated Wheat"));
        rGroup.add(new ResourceClass("wheat_domesticated_mustafar", "Mustafarian Domesticated Wheat"));
        rGroup.add(new ResourceClass("wheat_domesticated_naboo", "Nabooian Domesticated Wheat"));
        rGroup.add(new ResourceClass("wheat_domesticated_rori", "Rori Domesticated Wheat"));
        rGroup.add(new ResourceClass("wheat_domesticated_talus", "Talusian Domesticated Wheat"));
        rGroup.add(new ResourceClass("wheat_domesticated_tatooine", "Tatooinian Domesticated Wheat"));
        rGroup.add(new ResourceClass("wheat_domesticated_yavin4", "Yavinian Domesticated Wheat"));
        rGroup.add(new ResourceClass("wheat_domesticated_dxun", "Dxun Domesticated Wheat"));
        rGroup.add(new ResourceClass("wheat_wild", "Wild Wheat"));
        rGroup.add(new ResourceClass("wheat_wild_corellia", "Corellian Wild Wheat"));
        rGroup.add(new ResourceClass("wheat_wild_dantooine", "Dantooine Wild Wheat"));
        rGroup.add(new ResourceClass("wheat_wild_dathomir", "Dathomirian Wild Wheat"));
        rGroup.add(new ResourceClass("wheat_wild_endor", "Endorian Wild Wheat"));
        rGroup.add(new ResourceClass("wheat_wild_kashyyyk", "Kashyyykian Wild Wheat"));
        rGroup.add(new ResourceClass("wheat_wild_lok", "Lokian Wild Wheat"));
        rGroup.add(new ResourceClass("wheat_wild_mustafar", "Mustafarian Wild Wheat"));
        rGroup.add(new ResourceClass("wheat_wild_naboo", "Nabooian Wild Wheat"));
        rGroup.add(new ResourceClass("wheat_wild_rori", "Rori Wild Wheat"));
        rGroup.add(new ResourceClass("wheat_wild_talus", "Talusian Wild Wheat"));
        rGroup.add(new ResourceClass("wheat_wild_tatooine", "Tatooinian Wild Wheat"));
        rGroup.add(new ResourceClass("wheat_wild_yavin4", "Yavinian Wild Wheat"));
        rGroup.add(new ResourceClass("wheat_wild_dxun", "Dxun Wild Wheat"));
        rGroup.add(new ResourceClass("wood", "Wood"));
        rGroup.add(new ResourceClass("wood_deciduous", "Hard Wood"));
        rGroup.add(new ResourceClass("wood_deciduous_corellia", "Corellian Deciduous Wood"));
        rGroup.add(new ResourceClass("wood_deciduous_dantooine", "Dantooine Deciduous Wood"));
        rGroup.add(new ResourceClass("wood_deciduous_dathomir", "Dathomirian Deciduous Wood"));
        rGroup.add(new ResourceClass("wood_deciduous_endor", "Endorian Deciduous Wood"));
        rGroup.add(new ResourceClass("wood_deciduous_kashyyyk", "Kashyyykian Deciduous Wood"));
        rGroup.add(new ResourceClass("wood_deciduous_lok", "Lokian Deciduous Wood"));
        rGroup.add(new ResourceClass("wood_deciduous_mustafar", "Mustafarian Deciduous Wood"));
        rGroup.add(new ResourceClass("wood_deciduous_naboo", "Nabooian Deciduous Wood"));
        rGroup.add(new ResourceClass("wood_deciduous_rori", "Rori Deciduous Wood"));
        rGroup.add(new ResourceClass("wood_deciduous_talus", "Talusian Deciduous Wood"));
        rGroup.add(new ResourceClass("wood_deciduous_tatooine", "Tatooinian Deciduous Wood"));
        rGroup.add(new ResourceClass("wood_deciduous_yavin4", "Yavinian Deciduous Wood"));
        rGroup.add(new ResourceClass("wood_deciduous_dxun", "Dxun Deciduous Wood"));
        rGroup.add(new ResourceClass("metal_nonferrous_unknown", "Unknown Non-Ferrous Metal"));
        rGroup.add(new ResourceClass("none", "None"));
        rGroup.add(new ResourceClass("energy_renewable_site_limited_geothermal", "Geothermal Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_site_limited_hydron3", "Hyrdon-3 Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_site_limited_tidal", "Tidal Renewable Energy"));
        rGroup.add(new ResourceClass("bone_avian_hoth", "Hoth Avian Bones"));
        rGroup.add(new ResourceClass("bone_horn_hoth", "Hoth Horn"));
        rGroup.add(new ResourceClass("bone_mammal_hoth", "Hoth Animal Bones"));
        rGroup.add(new ResourceClass("corn_domesticated_hoth", "Hoth Domesticated Corn"));
        rGroup.add(new ResourceClass("corn_wild_hoth", "Hoth Wild Corn"));
        rGroup.add(new ResourceClass("meat_avian_hoth", "Hoth Avian Meat"));
        rGroup.add(new ResourceClass("fruit_berries_hoth", "Hoth Berry Fruit"));
        rGroup.add(new ResourceClass("hide_bristley_hoth", "Hoth Bristley Hide"));
        rGroup.add(new ResourceClass("meat_carnivore_hoth", "Hoth Carnivore Meat"));
        rGroup.add(new ResourceClass("softwood_conifer_hoth", "Hoth Conifer Wood"));
        rGroup.add(new ResourceClass("seafood_crustacean_hoth", "Hoth Crustacean Meat"));
        rGroup.add(new ResourceClass("wood_deciduous_hoth", "Hoth Deciduous Wood"));
        rGroup.add(new ResourceClass("meat_domesticated_hoth", "Hoth Domesticated Meat"));
        rGroup.add(new ResourceClass("milk_domesticated_hoth", "Hoth Domesticated Milk"));
        rGroup.add(new ResourceClass("oats_domesticated_hoth", "Hoth Domesticated Oats"));
        rGroup.add(new ResourceClass("rice_domesticated_hoth", "Hoth Domesticated Rice"));
        rGroup.add(new ResourceClass("wheat_domesticated_hoth", "Hoth Domesticated Wheat"));
        rGroup.add(new ResourceClass("meat_egg_hoth", "Hoth Egg"));
        rGroup.add(new ResourceClass("fiberplast_hoth", "Hoth Fiberplast"));
        rGroup.add(new ResourceClass("seafood_fish_hoth", "Hoth Fish Meat"));
        rGroup.add(new ResourceClass("energy_renewable_site_limited_geothermal_hoth", "Hoth Geothermal Renewable Energy"));
        rGroup.add(new ResourceClass("meat_herbivore_hoth", "Hoth Herbivore Meat"));
        rGroup.add(new ResourceClass("energy_renewable_site_limited_hydron3_hoth", "Hoth Hydron-3 Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_site_limited_tidal_hoth", "Hoth Tidal Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_unlimited_solar_hoth", "Hoth Solar Renewable Energy"));
        rGroup.add(new ResourceClass("energy_renewable_unlimited_wind_hoth", "Hoth Wind Renewable Energy"));
        rGroup.add(new ResourceClass("fruit_flowers_hoth", "Hoth Flower Fruit"));
        rGroup.add(new ResourceClass("fruit_fruits_hoth", "Hoth Fruit"));
        rGroup.add(new ResourceClass("hide_leathery_hoth", "Hoth Leathery Hide"));
        rGroup.add(new ResourceClass("hide_scaley_hoth", "Hoth Scaley Hide"));
        rGroup.add(new ResourceClass("hide_wooly_hoth", "Hoth Wooly Hide"));
        rGroup.add(new ResourceClass("meat_insect_hoth", "Hoth Insect Meat"));
        rGroup.add(new ResourceClass("meat_reptilian_hoth", "Hoth Reptilian Meat"));
        rGroup.add(new ResourceClass("meat_wild_hoth", "Hoth Wild Meat"));
        rGroup.add(new ResourceClass("milk_wild_hoth", "Hoth Wild Milk"));
        rGroup.add(new ResourceClass("oats_wild_hoth", "Hoth Wild Oats"));
        rGroup.add(new ResourceClass("rice_wild_hoth", "Hoth Wild Rice"));
        rGroup.add(new ResourceClass("seafood_mollusk_hoth", "Hoth Mollusk Meat"));
        rGroup.add(new ResourceClass("softwood_evergreen_hoth", "Hoth Evergreen Wood"));
        rGroup.add(new ResourceClass("vegetable_beans_hoth", "Hoth Vegetable Beans"));
        rGroup.add(new ResourceClass("vegetable_fungi_hoth", "Hoth Vegetable Fungus"));
        rGroup.add(new ResourceClass("vegetable_greens_hoth", "Hoth Vegetable Greens"));
        rGroup.add(new ResourceClass("vegetable_tubers_hoth", "Hoth Vegetable Tubers"));
        rGroup.add(new ResourceClass("water_vapor_hoth", "Hoth Water Vapor"));
        rGroup.add(new ResourceClass("wheat_wild_hoth", "Hoth Wild Wheat"));
        return true;
    }

}
