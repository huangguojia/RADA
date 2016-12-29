package com.example.john.radar;

import java.io.Serializable;

public class Info implements Serializable {
    private String name;
    private String tele;

    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name=name;
    }

    public String getTele() {
        return this.tele;
    }
    public void setTele(String tele) {
        this.tele=tele;
    }
}
