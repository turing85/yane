package de.turing85.yane.api;

public interface Instruction {
  String mnemonic();
  byte code();
  int bytesToRead();
  int cycles();
}