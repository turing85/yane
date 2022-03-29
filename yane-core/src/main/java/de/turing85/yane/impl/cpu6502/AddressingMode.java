package de.turing85.yane.impl.cpu6502;

import de.turing85.yane.api.*;
import lombok.*;
import lombok.experimental.Delegate;

/**
 * <p>The addressing modes supported by the 6502 processor.</p>
 *
 * <p>When {@link #fetch(Register, CpuBus)} is called, data from the {@link CpuBus} and the
 * {@link Register} is read, and the {@link Register} is mutated. In particular, the {@link
 * Register#programCounter} is incremented. The result of a fetch is encapsulated in an {@link
 * AddressingResult}.</p>
 *
 * <p>Different modes read different number of bytes during execution, but the number of bytes
 * read for one mode is always the same, i.e. does not depend on the data read. The number of bytes 
 * read is equivalent to the number of times {@link Register#programCounter} is incremented.</p>
 */
@Value
@EqualsAndHashCode
@Getter(AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
class AddressingMode implements AddressModeFunction {
  /**
   * Since the {@link #IMPLIED} addressing mode does not read from any address (in fact, it does
   * not read anything since the data is implied in the command itself, e.g. {@link Command#TAX},
   * which transfers the data of {@link Register#a} into {@link Register#x}), this value is set to
   * {@link AddressingResult#address}. This indicates that the address should not be read.
   */
  static int IMPLIED_LOADED_ADDRESS = Integer.MIN_VALUE;
  /**
   * Only used by pseudo-{@link Instruction}s with illegal opcodes.
   */
  static int UNKNOWN_LOADED_ADDRESS = Integer.MIN_VALUE;

  /**
   * The {@link #IMPLIED} and {@link #UNKNOWN} addressing modes do not read any data, thus {@link
   * AddressingResult#value} is set to this value, indicating that no data has been read.
   */
  static int NOTHING_READ_VALUE = Integer.MIN_VALUE;

  /**
   * Number of bytes read by the {@link #IMPLIED} addressing mode.
   */
  private static final int IMPLIED_ADDRESSING_BYTES_TO_READ = 0;

  /**
   * Number of bytes read by the {@link #UNKNOWN} addressing mode.
   */
  private static final int UNKNOWN_ADDRESSING_BYTES_TO_READ = 0;

  /**
   * Number of bytes read by the {@link #IMMEDIATE} addressing mode.
   */
  private static final int IMMEDIATE_ADDRESSING_BYTES_TO_READ = 1;

  /**
   * Number of bytes read by the {@link #INDIRECT} addressing mode.
   */
  private static final int INDIRECT_ADDRESSING_BYTES_TO_READ = 1;

  /**
   * Number of bytes read by the {@link #RELATIVE} addressing mode.
   */
  private static final int RELATIVE_ADDRESSING_BYTES_TO_READ = 1;

  /**
   * Number of bytes read by the {@link #ABSOLUTE}, {@link #ABSOLUTE_X} and {@link #ABSOLUTE_Y}
   * addressing modes.
   */
  private static final int ABSOLUTE_ADDRESSING_BYTES_TO_READ = 2;

  /**
   * Number of bytes read by the {@link #INDIRECT_ZERO_PAGE_Y}, {@link #INDIRECT_ZERO_PAGE_X},
   * {@link #ZERO_PAGE}, {@link #ZERO_PAGE_X} and {@link #ZERO_PAGE_Y} addressing modes. addressing
   * modes.
   */
  private static final int ZERO_PAGE_ADDRESSING_BYTES_TO_READ = 1;

  /**
   * Accumulator addressing mode.
   *
   * <p>Loads the value of {@link Register#a}.
   *
   * <table border="1">
   *   <caption>behavioural summary</caption>
   *   <tr>
   *     <th>description</th> <th>value</th>
   *   </tr>
   *   <tr>
   *     <td>mnemonic</td> <td>{@code A}</td>
   *   </tr>
   *   <tr>
   *     <td>address</td> <td>{@link #IMPLIED_LOADED_ADDRESS} (marker-value)</td>
   *   </tr>
   *   <tr>
   *     <td>value</td> <td>value of {@link Register#a}</td>
   *   </tr>
   *   <tr>
   *     <td>program counter increased by</td> <td>{@code 0}</td>
   *   </tr>
   *   <tr>
   *     <td>additional cycle needed</td> <td>never</td>
   *   </tr>
   * </table>
   */
  static final AddressingMode ACCUMULATOR = new AddressingMode(
      (register, bus) -> new AddressingResult(register, IMPLIED_LOADED_ADDRESS, register.a()),
      "A",
      IMPLIED_ADDRESSING_BYTES_TO_READ);

  /**
   * Absolute addressing mode.
   *
   * <p>To construct the {@code address}, this mode reads two 8-bit values from the {@link CpuBus}
   * at addresses {@link Register#programCounter}({@code PC_LOW}) and {@link
   * Register#programCounter}{@code + 1} ({@code PC_HIGH}). Then constructs a 16-bit address by
   * using {@code PC_LOW} as the 8 lower bits and {@code PC_HIGH} as the 8 higher bits.</p>
   *
   * <p>The {@code value} of is then read from the {@code address}.
   *
   * <table border="1">
   *   <caption>behavioural summary</caption>
   *   <tr>
   *     <th>description</th> <th>value</th>
   *   </tr>
   *   <tr>
   *     <td>mnemonic</td> <td>{@code abs}</td>
   *   </tr>
   *   <tr>
   *     <td>address</td> <td>{@code address}, as described above</td>
   *   </tr>
   *   <tr>
   *     <td>value</td> <td>value of {@code address}</td>
   *   </tr>
   *   <tr>
   *     <td>program counter increased by</td> <td>{@code 2}</td>
   *   </tr>
   *   <tr>
   *     <td>additional cycle needed</td> <td>never</td>
   *   </tr>
   * </table>
   */
  static final AddressingMode ABSOLUTE = new AddressingMode(
      (register, bus) -> {
        final int address = readAddressAtProgramPointer(register, bus);
        return new AddressingResult(register, address, bus.read(address));
      },
      "abs",
      ABSOLUTE_ADDRESSING_BYTES_TO_READ);

  /**
   * Absolute addressing mode with X register offset.
   *
   * <p>To construct the {@code address}, this mode reads two 8-bit values from the {@link CpuBus}
   * at addresses {@link Register#programCounter}({@code addressLow}) and {@link
   * Register#programCounter}{@code + 1} ({@code addressHigh}). Then constructs a 16-bit address
   * by using {@code addressLow} as the 8 lower bits and {@code addressHigh} as the 8 higher bits.
   * We will call this address {@code address}. Finally, the value of {@link Register#x} is added
   * to {@code address}. We will call this address {@code addressPlusX}.
   *
   * <p>The {@code value} of is then read from the {@code addressPlusX}.
   *
   * <table border="1">
   *   <caption>behavioural summary</caption>
   *   <tr>
   *     <th>description</th> <th>value</th>
   *   </tr>
   *   <tr>
   *     <td>mnemonic</td> <td>{@code abs,x}</td>
   *   </tr>
   *   <tr>
   *     <td>address</td> <td>{@code address}, as described above</td>
   *   </tr>
   *   <tr>
   *     <td>value</td> <td>value of {@code addressPlusX}</td>
   *   </tr>
   *   <tr>
   *     <td>program counter increased by</td> <td>{@code 2}</td>
   *   </tr>
   *   <tr>
   *     <td>additional cycle needed</td>
   *     <td>
   *       {@code 1}, if {@code address} and {@code addressPlusX} reside in different memory pages,
   *       i.e. if the high byte of {@code address} and the high byte of {@code addressPlusX} are
   *       not equal.
   *       <br>{@code 0} otherwise.
   *     </td>
   *   </tr>
   * </table>
   */
  static final AddressingMode ABSOLUTE_X = new AddressingMode(
      (register, bus) -> {
        final int address = readAddressAtProgramPointer(register, bus);
        final int addressPlusX = (address + register.x()) & 0xFFFF;
        return new AddressingResult(
            register,
            addressPlusX, bus.read(addressPlusX),
            highBytesDiffers(address, addressPlusX) ? 1 : 0);
      },
      "abs,X",
      ABSOLUTE_ADDRESSING_BYTES_TO_READ);

  /**
   * Absolute addressing mode with Y register offset.
   *
   * <p>To construct the {@code address}, this mode reads two 8-bit values from the {@link CpuBus}
   * at addresses {@link Register#programCounter}({@code addressLow}) and {@link
   * Register#programCounter}{@code + 1} ({@code addressHigh}). Then constructs a 16-bit address
   * by using {@code addressLow} as the 8 lower bits and {@code addressHigh} as the 8 higher bits.
   * We will call this address {@code address}. Finally, the value of {@link Register#y} is added
   * to {@code address}. We will call this address {@code addressPlusY}.
   *
   * <p>The {@code value} of is then read from the {@code addressPlusY}.
   *
   * <table border="1">
   *   <caption>behavioural summary</caption>
   *   <tr>
   *     <th>description</th> <th>value</th>
   *   </tr>
   *   <tr>
   *     <td>mnemonic</td> <td>{@code abs,y}</td>
   *   </tr>
   *   <tr>
   *     <td>address</td> <td>{@code address}, as described above</td>
   *   </tr>
   *   <tr>
   *     <td>value</td> <td>value of {@code addressPlusY}</td>
   *   </tr>
   *   <tr>
   *     <td>program counter increased by</td> <td>{@code 2}</td>
   *   </tr>
   *   <tr>
   *     <td>additional cycle needed</td>
   *     <td>
   *       {@code 1}, if {@code address} and {@code addressPlusY} reside in different memory pages,
   *       i.e. if the high byte of {@code address} and the high byte of {@code addressPlusY} are
   *       not equal.
   *       <br>{@code 0} otherwise.
   *     </td>
   *   </tr>
   * </table>
   */
  static final AddressingMode ABSOLUTE_Y = new AddressingMode(
      (register, bus) -> {
        final int address = readAddressAtProgramPointer(register, bus);
        final int addressPlusY = (address + register.y()) & 0xFFFF;
        return new AddressingResult(
            register,
            addressPlusY, bus.read(addressPlusY),
            highBytesDiffers(address, addressPlusY) ? 1 : 0);
      },
      "abs,y",
      ABSOLUTE_ADDRESSING_BYTES_TO_READ);

  static final AddressingMode IMMEDIATE = new AddressingMode(
      (register, bus) -> {
        final int address = register.getAndIncrementProgramCounter();
        return new AddressingResult(register, address, bus.read(address));
      },
      "#",
      IMMEDIATE_ADDRESSING_BYTES_TO_READ);

  static final AddressingMode IMPLIED = new AddressingMode(
      (register, bus) ->
          new AddressingResult(register, IMPLIED_LOADED_ADDRESS, NOTHING_READ_VALUE),
      "impl",
      IMPLIED_ADDRESSING_BYTES_TO_READ);

  static final AddressingMode INDIRECT = new AddressingMode(
      (register, bus) -> {
        final int addressForIndirectLow = readAddressAtProgramPointer(register, bus);
        final int addressForIndirectHigh = lowestByteIsAllOnes(addressForIndirectLow)
            // Hardware bug in 6502
            ? addressForIndirectLow & 0xFF00
            // normal behaviour
            : (addressForIndirectLow + 1) & 0xFFFF;
        final int address =
            bus.read(addressForIndirectLow) | (bus.read(addressForIndirectHigh) << 8);
        return new AddressingResult(register, address, bus.read(address));
      },
      "ind",
      2);

  static final AddressingMode INDIRECT_ZERO_PAGE_X = new AddressingMode(
      (register, bus) -> {
        final int zeroPageIndirectAddress = bus.read(register.getAndIncrementProgramCounter());
        final int zeroPagePlusXOffsetIndirectAddress =
            (zeroPageIndirectAddress + register.x()) & 0xFF;
        final int address = bus.read(zeroPagePlusXOffsetIndirectAddress);
        return new AddressingResult(register, address, bus.read(address));
      },
      "X,ind",
      1);

  static final AddressingMode INDIRECT_ZERO_PAGE_Y = new AddressingMode(
      (register, bus) -> {
        final int zeroPageIndirectAddress = bus.read(register.getAndIncrementProgramCounter());
        final int address = readAddressFromBus(zeroPageIndirectAddress, bus);
        final int addressPlusY = (address + register.y()) & 0xFFFF;
        return new AddressingResult(
            register,
            addressPlusY, bus.read(addressPlusY),
            highBytesDiffers(address, addressPlusY) ? 1 : 0);
      },
      "ind,Y",
      ZERO_PAGE_ADDRESSING_BYTES_TO_READ);

  static final AddressingMode RELATIVE = new AddressingMode(
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
        return new AddressingResult(register, address, bus.read(address));
      },
      "rel",
      RELATIVE_ADDRESSING_BYTES_TO_READ);

  static final AddressingMode ZERO_PAGE = new AddressingMode(
      (register, bus) -> {
        final int zeroPageAddress = bus.read(register.getAndIncrementProgramCounter());
        return new AddressingResult(register, zeroPageAddress, bus.read(zeroPageAddress));
      },
      "zpg",
      ZERO_PAGE_ADDRESSING_BYTES_TO_READ);

  static final AddressingMode ZERO_PAGE_X = new AddressingMode(
      (register, bus) -> {
        final int zeroPageAddress = bus.read(register.getAndIncrementProgramCounter());
        final int zeroPageAddressPlusX = (zeroPageAddress + register.x()) & 0x00FF;
        return new AddressingResult(register, zeroPageAddressPlusX, bus.read(zeroPageAddressPlusX));
      },
      "zpg,X",
      ZERO_PAGE_ADDRESSING_BYTES_TO_READ);

  static final AddressingMode ZERO_PAGE_Y = new AddressingMode(
      (register, bus) -> {
        final int zeroPageAddress = bus.read(register.getAndIncrementProgramCounter());
        final int zeroPageAddressPlusY = (zeroPageAddress + register.y()) & 0x00FF;
        return new AddressingResult(register, zeroPageAddressPlusY, bus.read(zeroPageAddressPlusY));
      },
      "zpg,Y",
      ZERO_PAGE_ADDRESSING_BYTES_TO_READ);

  static final AddressingMode UNKNOWN = new AddressingMode(
      (register, bus) -> new AddressingResult(register, UNKNOWN_LOADED_ADDRESS, NOTHING_READ_VALUE),
      "???",
      UNKNOWN_ADDRESSING_BYTES_TO_READ);

  @Delegate
  AddressModeFunction loadFunction;
  String mnemonic;
  int bytesToRead;

  /** {@inheritDoc} */
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
