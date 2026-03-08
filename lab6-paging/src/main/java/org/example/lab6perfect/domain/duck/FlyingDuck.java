package org.example.lab6perfect.domain.duck;

public class FlyingDuck extends Duck implements Zburator {
    public FlyingDuck(Long id, String username, String email, String password, double viteza, double rezistenta) {
        super(id, username, email, password, viteza, rezistenta);
    }
    @Override
    public String getType() {
        return "FLYING";
    }

    @Override
    public void zboara() {
        System.out.println(username+" zboara!");
    }
    @Override
    public void quack() {
        System.out.println(username+":  Quack! Am terminat zborul!");
    }

}
