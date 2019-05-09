/*
 * Based on the UniPatcher by btimofeev,
 * see https://github.com/btimofeev/UniPatcher/blob/master/app/src/main/java/org/emunix/unipatcher/patcher/BPS.kt
 * for reference
 *
 * Copyright (c) 2016, 2018 Boris Timofeev, 2019 bartimaeusnek
 * This file is part of UniPatcher.
 * UniPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * UniPatcher is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with UniPatcher.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.github.bartimaeusnek.mbps;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

class BPSpatcher {
    private final File patchFile;
    private final File originalFile;
    private final File outputFile;

    BPSpatcher(File patchFile, File originalFile, File outputFile) {
        this.patchFile = patchFile;
        this.originalFile = originalFile;
        this.outputFile = outputFile;
        this.outputFile.deleteOnExit();
    }

    final void patch() throws IOException {
        byte[] patch = FileUtils.readFileToByteArray(this.patchFile);
        int patchPos = 4;
        Pair<Integer, Integer> decoded = this.decode(patch, patchPos);
        patchPos = decoded.getValue();
        byte[] rom = FileUtils.readFileToByteArray(this.originalFile);
        decoded = this.decode(patch, patchPos);
        patchPos = decoded.getValue();
        byte[] output = new byte[decoded.getKey()];
        int outputPos = 0;
        decoded = this.decode(patch, patchPos);
        patchPos = decoded.getValue() + decoded.getKey();
        int romRelOffset = 0;
        int outRelOffset = 0;
        int offset;
        int length;
        byte mode;
        while (patchPos < patch.length - 12) {
            decoded = this.decode(patch, patchPos);
            length = decoded.getKey();
            patchPos = decoded.getValue();
            mode = (byte) (length & 0x3);
            length = (length >> 2) + 1;
            switch (mode) {
                case 0:
                    System.arraycopy(rom, outputPos, output, outputPos, length);
                    outputPos += length;
                    continue;
                case 1:
                    System.arraycopy(patch, patchPos, output, outputPos, length);
                    patchPos += length;
                    outputPos += length;
                    continue;
                case 2:
                case 3: {
                    decoded = this.decode(patch, patchPos);
                    offset = decoded.getKey();
                    patchPos = decoded.getValue();
                    offset = (((offset & 0x1) == 0x1) ? -1 : 1) * (offset >> 1);
                    if (mode == 2) {
                        romRelOffset += offset;
                        System.arraycopy(rom, romRelOffset, output, outputPos, length);
                        romRelOffset += length;
                        outputPos += length;
                        continue;
                    }
                    outRelOffset += offset;
                    while (length-- > 0) {
                        output[outputPos++] = output[outRelOffset++];
                    }
                }
            }
        }
        FileUtils.writeByteArrayToFile(this.outputFile, output);
    }

    private Pair<Integer, Integer> decode(byte[] array, int pos) {
        int newPos = pos;
        int offset = 0;
        int shift = 1;
        int x;
        while (true) {
            x = array[newPos++];
            offset += (x & 0x7F) * shift;
            if ((x & 0x80) != 0x0) {
                break;
            }
            shift <<= 7;
            offset += shift;
        }
        return (Pair<Integer, Integer>) new Pair(offset, newPos);
    }
}
