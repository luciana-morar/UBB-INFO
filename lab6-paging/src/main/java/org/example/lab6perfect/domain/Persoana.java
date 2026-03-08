package org.example.lab6perfect.domain;

import java.time.LocalDate;

public class Persoana extends User{
    private String nume;
    private String prenume;
    private String ocupatie;
    private LocalDate dataNasterii;
    private int nivelEmpatie;

    public Persoana(Long id, String username, String email, String password, String nume, String prenume, String ocupatie, LocalDate dataNasterii, int nivelEmpatie) {
        super(id, username, email, password);
        this.nume = nume;
        this.prenume = prenume;
        this.ocupatie = ocupatie;
        this.dataNasterii = dataNasterii;
        this.nivelEmpatie = nivelEmpatie;
    }

    public String getNume() {
        return nume;
    }
    public String getPrenume() {
        return prenume;
    }

    public String getOcupatie() {
        return ocupatie;
    }

    public LocalDate getDataNasterii() {
        return dataNasterii;
    }

    public Integer getNivelEmpatie() {
        return nivelEmpatie;
    }
    public void setNume(String nume) {
        this.nume = nume;
    }

    public void setPrenume(String prenume) {
        this.prenume = prenume;
    }
}
