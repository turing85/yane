package de.turing85.yane.api;

public interface CpuBus {
  void write(short address, byte data);
  byte read(short address);
}
