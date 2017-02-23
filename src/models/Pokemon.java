package models;

import Enums.Types;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/**
 * Created by benwunderlich on 12/02/2017.
 */
public class Pokemon {
    private int number;
    private String name;
    private List<Types> types = new ArrayList<>();
    private int lvl;
    private int HP;
    private Map<String, Float> stats = new HashMap<>();
    private List<Move> moves = new ArrayList<>();

    public Pokemon (int number, int lvl) {
        this.number = number;
        this.lvl = lvl;

        // get the pokemon data
        JSONObject objPoke = null;
        try {
            objPoke = fetchPokemon(number).getObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.name = objPoke.get("name").toString();

        // set Pokemon types
        JSONArray types = (JSONArray)objPoke.get("types");
        if (types.length() > 1) {
            JSONObject type1 = (JSONObject) ((JSONObject) types.get(1)).get("type");
            String type1String = Character.toUpperCase(type1.get("name").toString().charAt(0)) + type1.get("name").toString().substring(1);
            this.types.add(Types.valueOf(type1String));

            JSONObject type2 = (JSONObject) ((JSONObject) types.get(0)).get("type");
            String type2String = Character.toUpperCase(type2.get("name").toString().charAt(0)) + type2.get("name").toString().substring(1);
            this.types.add(Types.valueOf(type2String));
        } else  {
            JSONObject type = (JSONObject) ((JSONObject) types.get(0)).get("type");
            String typeString = Character.toUpperCase(type.get("name").toString().charAt(0)) + type.get("name").toString().substring(1);
            this.types.add(Types.valueOf(typeString));
            this.types.add(Types.valueOf(typeString));
        }

        // set Pokemon status data
        JSONArray stats = (JSONArray) objPoke.get("stats");
        for (int i = 0; i < stats.length(); i++) {
            JSONObject stat = (JSONObject) stats.get(i);
            String statName = ((JSONObject) stat.get("stat")).get("name").toString();
            int base = Integer.parseInt(stat.get("base_stat").toString());
            int EV = Integer.parseInt(stat.get("effort").toString());
            int IV = 0 + (int)(Math.random() * 31);
            if(statName == "hp") {
                this.HP = (int) calcHP((float) base, (float) IV, (float) EV, (float) lvl);
            } else {
                float nature;
                if(statName == "attack" || statName == "defense") {
                    nature = 1.1f;
                } else if(statName == "special-attack" || statName == "special-defense") {
                    nature = 0.9f;
                } else {
                    nature = 1;
                }
                this.stats.put(statName, (float) calcStat((float) base, (float) IV, (float) EV, (float) lvl, nature));
            }
        }

        // get and set pokemon moves
        JSONArray movesVersionsGroups = (JSONArray) objPoke.get("moves");
        ArrayList moves = filterMoves(movesVersionsGroups, lvl);
        this.moves.addAll(pickMoves(moves));
    }

    // Getter
    public String toString() {
        return "number: " + number + "; /n" + "name: " + name + "; /n" + "lvl: " + lvl + "; /n" + "HP: " + HP + "; /n";
    }

    public String getName() {
        return name;
    }

    public String getTypes() {
        if(types.size() > 1) {
            return types.get(0).toString() + "/" + types.get(1).toString();
        } else {
            return types.toString();
        }
    }

    public String getLvlStr() {
        return "Lvl."+lvl;
    }

    public int getLvl() {
        return lvl;
    }

    public float getHP() {
        return HP;
    }

    public Map<String, Float> getStats() {
        return stats;
    }

    public List<Move> getMoves() {
        return moves;
    }

    // Degres and ingrese HP
    public void degreseHP(float value) { this.HP = Math.round(this.HP - value); }

    public void ingreseHP(float value) { this.HP = Math.round(this.HP + value); }

    // Helper
    private static double calcHP(float Base, float IV, float EV, float Lvl) {
        return (int) (Math.floor((2 * Base + IV + Math.floor(EV / 4) * Lvl) / 100) + Lvl + 10);
    }

    private static double calcStat(float Base, float IV, float EV, float Lvl, float Nature) {
        return (int) Math.floor((Math.floor((2 * Base + IV + Math.floor(EV / 4)) * Lvl / 100) + 5) * Nature);
    }

    private static ArrayList filterMoves(JSONArray movesVersionsGroups, Integer lvl) {

        // filter moves if they lerned on level up
        ArrayList moves = new ArrayList();
        for(int i = 0; i < movesVersionsGroups.length();i++) {
            JSONArray versionGroupDetails =  (JSONArray) ((JSONObject) movesVersionsGroups.get(i)).get("version_group_details");
            JSONObject move =  (JSONObject) ((JSONObject) movesVersionsGroups.get(i)).get("move");
            String lernMethode = ((JSONObject)((JSONObject)versionGroupDetails.get(0)).get("move_learn_method")).get("name").toString();
            Integer levelLerndAt = Integer.parseInt(((JSONObject)versionGroupDetails.get(0)).get("level_learned_at").toString());
            if (!lernMethode.equals("machine") && !lernMethode.equals("tutor") && levelLerndAt <= lvl) {
                move.put("level-lernd-at", levelLerndAt);
                moves.add(move);
            }
        }
        //Sort moves by Lvl
        Collections.sort(moves, new Comparator<JSONObject>() {
            @Override public int compare(JSONObject m1, JSONObject m2) {
                return Integer.parseInt(m1.get("level-lernd-at").toString()) - Integer.parseInt(m2.get("level-lernd-at").toString());
            }

        });
        return moves;
    }

    private static ArrayList<Move> pickMoves(ArrayList moves) {
        ArrayList<Move> retMoveList = new ArrayList<>();
        moveLoop: for(int i = moves.size() - 1 ; i >= 0; i--) {

            JSONObject jsonMove = null;
            try {
                jsonMove = fetchMove(((JSONObject)moves.get(i)).get("url").toString()).getObject();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(!((JSONObject)jsonMove.get("damage_class")).get("name").toString().equals("status") && !jsonMove.get("power").toString().equals("null")){
                Integer accuracy = 0;
                if(jsonMove.get("accuracy").toString().equals("null")) {
                    accuracy = 100;
                } else {
                    accuracy = Integer.parseInt(jsonMove.get("accuracy").toString());
                }

                JSONObject typeObj = (JSONObject)jsonMove.get("type");
                String type = Character.toUpperCase(typeObj.get("name").toString().charAt(0)) + typeObj.get("name").toString().substring(1);

                Move move = new Move(
                        jsonMove.get("name").toString(),
                        Types.valueOf(type),
                        Move.Categorys.valueOf(((JSONObject)jsonMove.get("damage_class")).get("name").toString().toUpperCase()),
                        Integer.parseInt(jsonMove.get("power").toString()),
                        accuracy
                );


                retMoveList.add(move);
                if (retMoveList.size() == 4) {
                    break moveLoop;
                }
            }
        }
        return retMoveList;
    }

    // API calls
    private static JsonNode fetchPokemon(Integer number) throws Exception {
        return Unirest.get("http://pokeapi.co/api/v2/pokemon/{id}/")
                .routeParam("id", Integer.toString(number))
                .header("accept", "application/json")
                .header("Content-Type", "application/json")
                .asJson().getBody();
    }

    private static JsonNode fetchMove(String url) throws Exception {
        return Unirest.get(url)
                .header("accept", "application/json")
                .header("Content-Type", "application/json")
                .asJson().getBody();
    }
}
