import models.Move;
import models.Pokemon;

import java.io.*;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by benwunderlich on 12/02/2017.
 */
public class main {

    private static Pokemon player1;
    private static Pokemon player2;


    private static void pickPokemon() {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Pick a Pokemon by his number (between 1 and 721): ");
        try {
            boolean pickSuc = false;
            int playerNumber = Integer.parseInt(br.readLine());

            int lvl = 1 + (int)(Math.random() * 100);

            player1 = new Pokemon(playerNumber, lvl);
            player2 = new Pokemon(1 + (int)(Math.random() * 721), lvl);

            System.out.print("You call ");
            System.out.println(player1.getLvlStr()+ " "+ player1.getName());
            System.out.print("You opponent call ");
            System.out.println(player2.getLvlStr()+ " "+ player2.getName());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static int pickAction() {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Pick a Action:");
        int result = 0;
        do{
            System.out.println("[1] Attack");
            System.out.println("[2] Item");
            System.out.println("[3] models.Pokemon");
            System.out.println("[4] Escape");
            try {
                int selection = Integer.parseInt(br.readLine());
                switch(selection) {
                    case 1:
                        result = 1;
                        break;
                    case 2:
                        System.out.println("You have 0 Items in your bag.");
                        break;
                    case 3:
                        System.out.println(player1.getName() + "is your only models.Pokemon.");
                        break;
                    case 4:
                        System.out.println("You can't escape.");
                        break;
                    default:
                        System.out.println("Selection invalid.");
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } while(result == 0);
        return result;
    }

    private static int pickAttack(Pokemon pokemon, boolean random) {
        if(random) {
            return ThreadLocalRandom.current().nextInt(1, 4 + 1);
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Pick a Attack:");
        int result = 0;
        do{
            final int[] i = {0};
            pokemon.getMoves().forEach((move) -> System.out.println("["+Integer.toString(++i[0])+"] "+move.getName()+" ("+move.getPower()+")"));
            try {
                int selection = Integer.parseInt(br.readLine());;
                if(selection > 0  && selection<5) {
                    result = selection;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } while(result == 0);
        return result - 1;
    }

    private static float getWeakens(String...a){
        float d=1;
        for(String B:a[1].split("/"))
            d*=("222221201222222222421124104222214241242221421224122222222111210224222224220424124421422222214212421422224222211122211124242241022222242222242212222224221112124224222221424114224122222244222411222122221144121141222122224202222241122122242422221222212202224242221114221422222222221222222420212222242222242211242122221122222442"
                    .charAt(Type(a[0])*18+Type(B))-48)*.5;
        return Float.parseFloat((d+"").replace(".0",""));
    }

    private static int Type(String a){
        int i=0;
        for(;i<18;i++)
            if(a.contains("N,gh,ly,oi,ou,ck,B,ho,S,re,W,G,E,P,I,g,k,y".split(",")[i]))
                break;
        return i;
    }

    private static float calculateModifier(Move move, Pokemon attPokemon, Pokemon defPokemon){
        float stab = 1;
        String[] types = attPokemon.getTypes().split("/");
        if (types[0].equals(types[1])){
            for (String type : attPokemon.getTypes().split("/")) {
                if (type == move.getType()) stab = 1.5f;
            }
        } else {
            if(attPokemon.getTypes().split("/")[0] == move.getType()) stab = 1.5f;
        }
        float type = getWeakens(move.getType(), defPokemon.getTypes());
        float other = 1; // counts for things like held items, Abilities, field advantages, and whether the battle is a Double Battle or Triple Battle or not.
        Random rand = new Random();
        float random = rand.nextFloat() * (1.00f - 0.85f) + 0.85f;

        return (stab * type * other * random);
    }

    private static float calculateDamage(Move move, Pokemon attPokemon, Pokemon defPokemon, float mod) {
        float lvl = attPokemon.getLvl();
        float attack = attPokemon.getStats().get("attack");
        float defense = defPokemon.getStats().get("defense");
        float base = move.getPower();
        return (((2 * lvl + 10) / 250) * (attack / defense) * ( base + 2)) * mod;
    }

    private static void attack(Pokemon attPlayer, Pokemon defPlayer, int attackIntex) {
        Move selectedMove = attPlayer.getMoves().get(attackIntex);
        System.out.println(attPlayer.getName() + " attacks with " + selectedMove.getName());
        float mod = calculateModifier(selectedMove, attPlayer, defPlayer);
        float damage = calculateDamage(selectedMove, attPlayer, defPlayer, mod);
        defPlayer.degreseHP(damage);

        if(defPlayer.getHP() > 0f ) {
            System.out.println(defPlayer.getName() + " has now " + Float.toString(defPlayer.getHP()) + "HP left." );
        } else {
            System.out.println(defPlayer.getName() + " has  0HP left." );
            System.out.println(attPlayer.getName() + " wins." );
        }
        System.out.println("");
    }

    public static void main(String[] args) {
        System.out.println("Welcome to PokemonBattle");
        pickPokemon();
        int index = 0;
        do {
            if(index % 2 == 0) {
                int action = pickAction();
                if(action == 1) {
                    int attackIndex = pickAttack(player1, false);
                    attack(player1, player2, attackIndex);
                }
            } else {
                int attackIndex = pickAttack(player2, true);
                attack(player2, player1, attackIndex);
            }
            index++;
        } while (player1.getHP() > 0 && player2.getHP() > 0);


    }
}
