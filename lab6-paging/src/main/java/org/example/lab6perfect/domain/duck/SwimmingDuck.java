package org.example.lab6perfect.domain.duck;

public class SwimmingDuck extends Duck implements Inotator{
    public SwimmingDuck(Long id, String username, String email, String password, double viteza, double rezistenta) {
        super(id, username, email, password, viteza, rezistenta);
    }
    @Override
    public String getType() {
        return "SWIMMING";
    }

    @Override
    public void inoata() {
        System.out.println(username+" inoata!");
    }
    @Override
    public void quack() {
        System.out.println(username+" Quack!Am terminat antrenamentul de inot");
    }
}
