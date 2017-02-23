package models;

import Enums.Types;

import java.lang.reflect.Type;

/**
 * Created by benwunderlich on 17/02/2017.
 */
public class Move {
    private String name;
    private Types type;
    private Categorys category;
    private int power;
    private int acc;


    public enum Categorys {
        SPECIAL, PHYSICAL, STATUS
    }

    public Move (String name, Types type, Categorys category, int power, int acc) {
        this.name = name;
        this.type = type;
        this.category = category;
        this.power = power;
        this.acc = acc;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type.toString();
    }

    public Categorys getCategory() {
        return category;
    }

    public int getPower() {
        return power;
    }

    public int getAcc() {
        return acc;
    }
}
