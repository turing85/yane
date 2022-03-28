package de.turing85.yane.impl.cpu6502;

import de.turing85.yane.api.*;
import lombok.*;
import lombok.experimental.Delegate;

@Value
@EqualsAndHashCode
@Getter(AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
class AddressMode implements AddressModeFunction {
  static int IMMEDIATE_LOADED_ADDRESS = Integer.MIN_VALUE;
  static int IMPLIED_LOADED_ADDRESS = Integer.MIN_VALUE + 1;
  static int UNKNOWN_LOADED_ADDRESS = Integer.MIN_VALUE + 2;

  static int NOTHING_READ_VALUE = Integer.MIN_VALUE;

  private static final int IMPLIED_ADDRESSING_BYTES_TO_READ = 0;
  private static final int UNKNOWN_ADDRESSING_BYTES_TO_READ = 0;
  private static final int IMMEDIATE_ADDRESSING_BYTES_TO_READ = 1;
  private static final int INDIRECT_ADDRESSING_BYTES_TO_READ = 1;
  private static final int RELATIVE_ADDRESSING_BYTES_TO_READ = 1;
  private static final int ABSOLUTE_ADDRESSING_BYTES_TO_READ = 2;

  static final AddressMode ACCUMULATOR = new AddressMode(
      (register, bus) -> AddressResult.of(register, register.a(), IMPLIED_LOADED_ADDRESS, 0),
      "A",
      IMPLIED_ADDRESSING_BYTES_TO_READ);

  static final AddressMode ABSOLUTE = new AddressMode(
      (register, bus) -> {
        final int address = readAddressAtProgramPointer(register, bus);
        return AddressResult.of(register, bus.read(address), address, 0);
      },
      "abs",
      ABSOLUTE_ADDRESSING_BYTES_TO_READ);

  static final AddressMode ABSOLUTE_X = new AddressMode(
      (register, bus) -> {
        final int address = readAddressAtProgramPointer(register, bus);
        final int addressPlusX = (address + register.x()) & 0xFFFF;
        return AddressResult.of(
            register,
            bus.read(addressPlusX),
            addressPlusX,
            highBytesDiffers(address, addressPlusX) ? 1 : 0);
      },
      "abs,X",
      ABSOLUTE_ADDRESSING_BYTES_TO_READ);

  static final AddressMode ABSOLUTE_Y = new AddressMode(
      (register, bus) -> {
        final int address = readAddressAtProgramPointer(register, bus);
        final int addressPlusY = (address + register.y()) & 0xFFFF;
        return AddressResult.of(
            register,
            bus.read(addressPlusY),
            addressPlusY,
            highBytesDiffers(address, addressPlusY) ? 1 : 0);
      },
      "abs,y",
      ABSOLUTE_ADDRESSING_BYTES_TO_READ);

  static final AddressMode IMMEDIATE = new AddressMode(
      (register, bus) -> {
        final int address = register.getAndIncrementProgramCounter();
        return AddressResult.of(register, bus.read(address), address, 0);
      },
      "#",
      IMMEDIATE_ADDRESSING_BYTES_TO_READ);

  static final AddressMode IMPLIED = new AddressMode(
      (register, bus) -> AddressResult.of(register, NOTHING_READ_VALUE, IMPLIED_LOADED_ADDRESS, 0),
      "impl",
      IMPLIED_ADDRESSING_BYTES_TO_READ);

  static final AddressMode INDIRECT = new AddressMode(
      (register, bus) -> {
        final int addressForIndirectLow = readAddressAtProgramPointer(register, bus);
        final int addressForIndirectHigh = lowestByteIsAllOnes(addressForIndirectLow)
            // Hardware bug in 6502
            ? addressForIndirectLow & 0xFF00
            // normal behaviour
            : (addressForIndirectLow + 1) & 0xFFFF;
        final int address =
            bus.read(addressForIndirectLow) | (bus.read(addressForIndirectHigh) << 8);
        return AddressResult.of(register, bus.read(address), address, 0);
      },
      "ind",
      2);

  static final AddressMode INDIRECT_ZERO_PAGE_X = new AddressMode(
      (register, bus) -> {
        final int zeroPageIndirectAddress = bus.read(register.getAndIncrementProgramCounter());
        final int zeroPagePlusXOffsetIndirectAddress =
            (zeroPageIndirectAddress + register.x()) & 0xFF;
        final int address = bus.read(zeroPagePlusXOffsetIndirectAddress);
        return AddressResult.of(register, bus.read(address), address, 0);
      },
      "X,ind",
      1);

  static final AddressMode INDIRECT_ZERO_PAGE_Y = new AddressMode(
      (register, bus) -> {
        final int zeroPageIndirectAddress = bus.read(register.getAndIncrementProgramCounter());
        final int address = readAddressFromBus(zeroPageIndirectAddress, bus);
        final int addressPlusY = (address + register.y()) & 0xFFFF;
        return AddressResult.of(
            register,
            bus.read(addressPlusY),
            addressPlusY,
            highBytesDiffers(address, addressPlusY) ? 1 : 0);
      },
      "ind,Y",
      1);

  static final AddressMode RELATIVE = new AddressMode(
      (register, bus) -> {
        final int programCounter = register.getAndIncrementProgramCounter();
        final int relativeAddress = bus.read(programCounter);
        final int signedRelativeAddress;
        if ((relativeAddress & 0x80) > 0) {
          signedRelativeAddress = relativeAddress | 0xFF00;
        } else {
          signedRelativeAddress = relativeAddress;
        }
        final int address = (programCounter + signedRelativeAddress) & 0xFFFF;
        return AddressResult.of(register, bus.read(address), address, 0);
      },
      "rel",
      1);

  static final AddressMode ZERO_PAGE = new AddressMode(
      (register, bus) -> {
        final int zeroPageAddress = bus.read(register.getAndIncrementProgramCounter());
        return AddressResult.of(register, bus.read(zeroPageAddress), zeroPageAddress, 0);
      },
      "zpg",
      1);

  static final AddressMode ZERO_PAGE_X = new AddressMode(
      (register, bus) -> {
        final int zeroPageAddress = bus.read(register.getAndIncrementProgramCounter());
        final int zeroPageAddressPlusX = (zeroPageAddress + register.x()) & 0x00FF;
        return AddressResult.of(register, bus.read(zeroPageAddressPlusX), zeroPageAddressPlusX, 0);
      },
      "zpg,X",
      1);

  static final AddressMode ZERO_PAGE_Y = new AddressMode(
      (register, bus) -> {
        final int zeroPageAddress = bus.read(register.getAndIncrementProgramCounter());
        final int zeroPageAddressPlusY = (zeroPageAddress + register.y()) & 0x00FF;
        return AddressResult.of(register, bus.read(zeroPageAddressPlusY), zeroPageAddressPlusY, 0);
      },
      "zpg,Y",
      1);

  static final AddressMode UNKNOWN = new AddressMode(
      (register, bus) -> AddressResult.of(register, NOTHING_READ_VALUE, UNKNOWN_LOADED_ADDRESS, 0),
      "???",
      UNKNOWN_ADDRESSING_BYTES_TO_READ);

  @Delegate
  AddressModeFunction loadFunction;
  String mnemonic;
  int bytesToRead;

  @Override
  public String toString() {
    return mnemonic();
  }

  private static int readAddressAtProgramPointer(Register register, CpuBus bus) {
    final int addressLow = bus.read(register.getAndIncrementProgramCounter());
    final int addressHigh = bus.read(register.getAndIncrementProgramCounter());
    return (addressHigh << 8) | addressLow;
  }

  private static boolean highBytesDiffers(int lhs, int rhs) {
    final int addressHigh = lhs >> 8;
    final int addressPlusXHigh = rhs >> 8;
    return addressPlusXHigh != addressHigh;
  }

  private static boolean lowestByteIsAllOnes(int indirect) {
    return (indirect & 0xFF) == 0xFF;
  }

  private static int readAddressFromBus(int zeroPageAddress, CpuBus bus) {
    final int addressLow = bus.read(zeroPageAddress);
    final int addressHigh = bus.read((zeroPageAddress + 1) & 0x00FF);
    return (addressHigh << 8) | addressLow;
  }
}