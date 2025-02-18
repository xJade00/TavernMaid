package dev.xjade.tavern.maid.utilities;

import com.github.luben.zstd.Zstd;
import java.nio.charset.StandardCharsets;

public class DebugEncoder {

  public static byte[] compressZstd(String data) {
    return Zstd.compress(data.getBytes(StandardCharsets.UTF_8));
  }

  public static String decompressZstd(byte[] compressedData) {
    byte[] decompressed = new byte[(int) Zstd.decompressedSize(compressedData)];
    Zstd.decompress(decompressed, compressedData);
    return new String(decompressed, StandardCharsets.UTF_8);
  }
}
