package com.managementsystem.demo1;

/**
 * Classe inicializadora para contornar a restrição de módulos do JavaFX
 * quando executado diretamente pelo classpath na IDE (IntelliJ / VS Code).
 */
public class Launcher {
    public static void main(String[] args) {
        MainApp.main(args);
    }
}
