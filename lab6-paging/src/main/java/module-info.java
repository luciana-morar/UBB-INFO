module org.example.lab6perfect {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires javafx.graphics;
    requires java.desktop;

    requires javafx.base;
//    requires org.example.lab6perfect;
//
//
//    opens org.example.lab6perfect to javafx.fxml;
//    exports org.example.lab6perfect;

    exports org.example.lab6perfect;
    exports org.example.lab6perfect.controller;
    exports org.example.lab6perfect.domain;
    exports org.example.lab6perfect.repository;
    exports org.example.lab6perfect.service;
    exports org.example.lab6perfect.validator;
    exports org.example.lab6perfect.obs;
    exports org.example.lab6perfect.database;

    opens org.example.lab6perfect to javafx.fxml;
    opens org.example.lab6perfect.controller to javafx.fxml;
    opens org.example.lab6perfect.domain to javafx.base;
}