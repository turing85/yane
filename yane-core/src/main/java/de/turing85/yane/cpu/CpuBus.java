package de.turing85.yane.cpu;

public interface CpuBus {

  void write(int address, int data);

  int read(int address);
}