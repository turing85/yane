package de.turing85.yana;

import lombok.*;

/**
 * Main method, bootstrapping the application.
 */
@Value
public class Main {

  String foo;

  public static void main(String[] args) {
    System.out.println("Hello, world!");
  }
}
