package com.thebombzen.jxlatte.frame.group;

import java.io.IOException;

import com.thebombzen.jxlatte.frame.Frame;
import com.thebombzen.jxlatte.frame.FrameFlags;
import com.thebombzen.jxlatte.frame.modular.ModularChannelInfo;
import com.thebombzen.jxlatte.frame.modular.ModularStream;
import com.thebombzen.jxlatte.frame.vardct.HFCoefficients;
import com.thebombzen.jxlatte.frame.vardct.TransformType;
import com.thebombzen.jxlatte.frame.vardct.Varblock;
import com.thebombzen.jxlatte.io.Bitreader;
import com.thebombzen.jxlatte.util.IntPoint;
import com.thebombzen.jxlatte.util.MathHelper;

public class PassGroup {

    private static final float[][] AFV_BASIS = {{0.25f, 0.25f, 0.25f, 0.25f, 0.25f, 0.25f, 0.25f, 0.25f, 0.25f, 0.25f, 0.25f,
            0.25f, 0.25f, 0.25f, 0.25f, 0.25f}, {0.876902929799142f, 0.2206518106944235f, -0.10140050393753763f,
            -0.1014005039375375f, 0.2206518106944236f, -0.10140050393753777f, -0.10140050393753772f, -0.10140050393753763f,
            -0.10140050393753758f, -0.10140050393753769f, -0.1014005039375375f, -0.10140050393753768f, -0.10140050393753768f,
            -0.10140050393753759f, -0.10140050393753763f, -0.10140050393753741f}, {0.0f, 0.0f, 0.40670075830260755f,
            0.44444816619734445f, 0.0f, 0.0f, 0.19574399372042936f, 0.2929100136981264f, -0.40670075830260716f,
            -0.19574399372042872f, 0.0f, 0.11379074460448091f, -0.44444816619734384f, -0.29291001369812636f,
            -0.1137907446044814f, 0.0f}, {0.0f, 0.0f, -0.21255748058288748f, 0.3085497062849767f, 0.0f, 0.4706702258572536f,
            -0.1621205195722993f, 0.0f, -0.21255748058287047f, -0.16212051957228327f, -0.47067022585725277f,
            -0.1464291867126764f, 0.3085497062849487f, 0.0f, -0.14642918671266536f, 0.4251149611657548f}, {0.0f,
            -0.7071067811865474f, 0.0f, 0.0f, 0.7071067811865476f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f},
            {-0.4105377591765233f, 0.6235485373547691f, -0.06435071657946274f, -0.06435071657946266f, 0.6235485373547694f,
                    -0.06435071657946284f, -0.0643507165794628f, -0.06435071657946274f, -0.06435071657946272f, -0.06435071657946279f,
                    -0.06435071657946266f, -0.06435071657946277f, -0.06435071657946277f, -0.06435071657946273f, -0.06435071657946274f,
                    -0.0643507165794626f}, {0.0f, 0.0f, -0.4517556589999482f, 0.15854503551840063f, 0.0f, -0.04038515160822202f,
            0.0074182263792423875f, 0.39351034269210167f, -0.45175565899994635f, 0.007418226379244351f, 0.1107416575309343f,
            0.08298163094882051f, 0.15854503551839705f, 0.3935103426921022f, 0.0829816309488214f, -0.45175565899994796f},
            {0.0f, 0.0f, -0.304684750724869f, 0.5112616136591823f, 0.0f, 0.0f, -0.290480129728998f, -0.06578701549142804f,
                    0.304684750724884f, 0.2904801297290076f, 0.0f, -0.23889773523344604f, -0.5112616136592012f, 0.06578701549142545f,
                    0.23889773523345467f, 0.0f}, {0.0f, 0.0f, 0.3017929516615495f, 0.25792362796341184f, 0.0f, 0.16272340142866204f,
            0.09520022653475037f, 0.0f, 0.3017929516615503f, 0.09520022653475055f, -0.16272340142866173f, -0.35312385449816297f,
            0.25792362796341295f, 0.0f, -0.3531238544981624f, -0.6035859033230976f}, {0.0f, 0.0f, 0.40824829046386274f, 0.0f, 0.0f,
            0.0f, 0.0f, -0.4082482904638628f, -0.4082482904638635f, 0.0f, 0.0f, -0.40824829046386296f, 0.0f, 0.4082482904638634f,
            0.408248290463863f, 0.0f}, {0.0f, 0.0f, 0.1747866975480809f, 0.0812611176717539f, 0.0f, 0.0f, -0.3675398009862027f,
            -0.307882213957909f, -0.17478669754808135f, 0.3675398009862011f, 0.0f, 0.4826689115059883f, -0.08126111767175039f,
            0.30788221395790305f, -0.48266891150598584f, 0.0f}, {0.0f, 0.0f, -0.21105601049335784f, 0.18567180916109802f, 0.0f, 0.0f,
            0.49215859013738733f, -0.38525013709251915f, 0.21105601049335806f, -0.49215859013738905f, 0.0f, 0.17419412659916217f,
            -0.18567180916109904f, 0.3852501370925211f, -0.1741941265991621f, 0.0f}, {0.0f, 0.0f, -0.14266084808807264f,
            -0.3416446842253372f, 0.0f, 0.7367497537172237f, 0.24627107722075148f, -0.08574019035519306f, -0.14266084808807344f,
            0.24627107722075137f, 0.14883399227113567f, -0.04768680350229251f, -0.3416446842253373f, -0.08574019035519267f,
            -0.047686803502292804f, -0.14266084808807242f}, {0.0f, 0.0f, -0.13813540350758585f, 0.3302282550303788f, 0.0f,
            0.08755115000587084f, -0.07946706605909573f, -0.4613374887461511f, -0.13813540350758294f, -0.07946706605910261f,
            0.49724647109535086f, 0.12538059448563663f, 0.3302282550303805f, -0.4613374887461554f, 0.12538059448564315f,
            -0.13813540350758452f}, {0.0f, 0.0f, -0.17437602599651067f, 0.0702790691196284f, 0.0f, -0.2921026642334881f,
            0.3623817333531167f, 0.0f, -0.1743760259965108f, 0.36238173335311646f, 0.29210266423348785f, -0.4326608024727445f,
            0.07027906911962818f, 0.0f, -0.4326608024727457f, 0.34875205199302267f}, {0.0f, 0.0f, 0.11354987314994337f,
            -0.07417504595810355f, 0.0f, 0.19402893032594343f, -0.435190496523228f, 0.21918684838857466f, 0.11354987314994257f,
            -0.4351904965232251f, 0.5550443808910661f, -0.25468277124066463f, -0.07417504595810233f, 0.2191868483885728f,
            -0.25468277124066413f, 0.1135498731499429f},
    };

    public final HFCoefficients hfCoefficients;
    public final int[][][] modularPassGroupBuffer;
    public final ModularChannelInfo[] modularPassGroupInfo;
    public final Frame frame;
    public final int groupID;
    public final int passID;

    public PassGroup(Bitreader reader, Frame frame, int pass, int group,
                     ModularChannelInfo[] replacedChannels) throws IOException {
        this.frame = frame;
        this.groupID = group;
        this.passID = pass;
        if (frame.getFrameHeader().encoding == FrameFlags.VARDCT) {
            hfCoefficients = new HFCoefficients(reader, frame, pass, group);
        } else {
            hfCoefficients = null;
        }
        ModularStream stream = new ModularStream(reader, frame,
                18 + 3 * frame.getNumLFGroups() + frame.getNumGroups() * pass + group, replacedChannels);
        stream.decodeChannels(reader);
        modularPassGroupBuffer = stream.getDecodedBuffer();
        modularPassGroupInfo = new ModularChannelInfo[modularPassGroupBuffer.length];
        for (int c = 0; c < modularPassGroupInfo.length; c++)
            modularPassGroupInfo[c] = new ModularChannelInfo(stream.getChannelInfo(c));
    }

    private void layBlock(final float[][] block, final float[][] buffer, final IntPoint inPos,
                          final IntPoint outPos, final IntPoint inSize) {
        for (int y = 0; y < inSize.y; y++)
            System.arraycopy(block[y + inPos.y], inPos.x, buffer[y + outPos.y], outPos.x, inSize.x);
    }

    private void invertAFV(float[][] coeffs, float[][] buffer, Varblock varblock, IntPoint pixelPosInFrame, float[][][] scratchBlock, IntPoint shift) {
        IntPoint p = varblock.pixelPosInGroup.shiftRight(shift);
        IntPoint ps = pixelPosInFrame.shiftRight(shift);
        scratchBlock[0][0][0] = (coeffs[p.y][p.x] + coeffs[p.y + 1][p.x] + coeffs[p.y][p.x + 1]) * 4f;
        for (int iy = 0; iy < 4; iy++) {
            for (int ix = (iy == 0 ? 1 : 0); ix < 4; ix++) {
                scratchBlock[0][iy][ix] = coeffs[p.y + iy * 2][p.x + ix * 2];
            }
        }
        TransformType tt = varblock.transformType();
        int flipX = tt == TransformType.AFV1 || tt == TransformType.AFV3 ? 1 : 0;
        int flipY = tt == TransformType.AFV2 || tt == TransformType.AFV3 ? 1 : 0;
        for (int iy = 0; iy < 4; iy++) {
            for (int ix = 0; ix < 4; ix++) {
                float sample = 0f;
                for (int j = 0; j < 16; j++) {
                    final int jx = j & 0b11;
                    final int jy = j >> 2;
                    sample += scratchBlock[0][jy][jx] * AFV_BASIS[j][iy * 4 + ix];
                }
                scratchBlock[1][iy][ix] = sample;
            }
        }
        for (int iy = 0; iy < 4; iy++) {
            for (int ix = 0; ix < 4; ix++) {
                buffer[ps.y + flipY * 4 + iy][ps.x + flipX * 4 + ix] = scratchBlock[1][flipY == 1 ? 3 - iy : iy][flipX == 1 ? 3 - ix : ix];
            }
        }
        // SPEC: watch signs here
        scratchBlock[0][0][0] = coeffs[p.y][p.x] + coeffs[p.y + 1][p.x] - coeffs[p.y][p.x + 1];
        for (int iy = 0; iy < 4; iy++) {
            for (int ix = (iy == 0 ? 1 : 0); ix < 4; ix++) {
                scratchBlock[0][iy][ix] = coeffs[p.y + iy * 2][p.x + ix * 2 + 1];
            }
        }
        MathHelper.inverseDCT2D(scratchBlock[0], scratchBlock[1], IntPoint.ZERO, IntPoint.ZERO, new IntPoint(4), scratchBlock[2], scratchBlock[3], false);
        for (int iy = 0; iy < 4; iy++) {
            for (int ix = 0; ix < 4; ix++) {
                // transposed intentionally
                buffer[ps.y + flipY * 4 + iy][ps.x + (flipX == 1 ? 0 : 4) + ix] = scratchBlock[1][ix][iy];
            }
        }
        scratchBlock[0][0][0] = coeffs[p.y][p.x] - coeffs[p.y + 1][p.x];
        for (int iy = 0; iy < 4; iy++) {
            for (int ix = (iy == 0 ? 1 : 0); ix < 8; ix++) {
                scratchBlock[0][iy][ix] = coeffs[p.y + 1 + iy * 2][p.x + ix];
            }
        }
        MathHelper.inverseDCT2D(scratchBlock[0], scratchBlock[1], IntPoint.ZERO, IntPoint.ZERO, new IntPoint(8, 4), scratchBlock[2], scratchBlock[3], false);
        for (int iy = 0; iy < 4; iy++) {
            for (int ix = 0; ix < 8; ix++) {
                buffer[ps.y + (flipY == 1 ? 0 : 4) + iy][ps.x + ix] = scratchBlock[1][iy][ix];
            }
        }
    }

    private void auxDCT2(float[][] coeffs, float[][] result, IntPoint p, IntPoint ps, int s) {
        int num = s / 2;
        for (int iy = 0; iy < 4; iy++) {
            for (int ix = 0; ix < 4; ix++) {
                if (iy >= num || ix >= num) {
                    result[ps.y + iy * 2][ps.x + ix * 2] = coeffs[p.y + iy * 2][p.x + ix * 2];
                    result[ps.y + iy * 2 + 1][ps.x + ix * 2] = coeffs[p.y + iy * 2 + 1][p.x + ix * 2];
                    result[ps.y + iy * 2][ps.x + ix * 2 + 1] = coeffs[p.y + iy * 2][p.x + ix * 2 + 1];
                    result[ps.y + iy * 2 + 1][ps.x + ix * 2 + 1] = coeffs[p.y + iy * 2 + 1][p.x + ix * 2 + 1];
                    continue;
                }
                float c00 = coeffs[p.y + iy][p.x + ix];
                float c01 = coeffs[p.y + iy][p.x + ix + num];
                float c10 = coeffs[p.y + iy + num][p.x + ix];
                float c11 = coeffs[p.y + iy + num][p.x + ix + num];
                float r00 = c00 + c01 + c10 + c11;
                float r01 = c00 + c01 - c10 - c11;
                float r10 = c00 - c01 + c10 - c11;
                float r11 = c00 - c01 - c10 + c11;
                result[ps.y + iy * 2][ps.x + ix * 2] = r00;
                result[ps.y + iy * 2][ps.x + ix * 2 + 1] = r01;
                result[ps.y + iy * 2 + 1][ps.x + ix * 2] = r10;
                result[ps.y + iy * 2 + 1][ps.x + ix * 2 + 1] = r11;
            }
        }
    }

    public void invertVarDCT(float[][][] frameBuffer, PassGroup prev) {
        IntPoint[] shift = frame.getFrameHeader().jpegUpsampling;
        if (prev != null) {
            for (int i = 0; i < hfCoefficients.varblocks.length; i++) {
                Varblock varblock = hfCoefficients.varblocks[i];
                if (varblock == null)
                    continue;
                IntPoint size = varblock.sizeInPixels();
                for (int c = 0; c < 3; c++) {
                    if (!varblock.isCorner(shift[c]))
                        continue;
                    final IntPoint ppg = varblock.pixelPosInGroup.shiftRight(shift[c]);
                    for (int y = ppg.y; y < ppg.y + size.y; y++) {
                        for (int x = ppg.x; x < ppg.x + size.x; x++) {
                            hfCoefficients.quantizedCoeffs[c][y][x] +=
                                    prev.hfCoefficients.quantizedCoeffs[c][y][x];
                        }
                    }
                }
            }
        }
        hfCoefficients.bakeDequantizedCoeffs();
        hfCoefficients.finalizeLLF();
        final float[][][] coeffs = hfCoefficients.dequantHFCoeff;
        float[][][] scratchBlock = new float[4][256][256];
        for (int i = 0; i < hfCoefficients.varblocks.length; i++) {
            Varblock varblock = hfCoefficients.varblocks[i];
            if (varblock == null)
                continue;
            for (int c = 0; c < 3; c++) {
                if (!varblock.isCorner(shift[c]))
                    continue;
                TransformType tt = varblock.transformType();
                final IntPoint ppg = varblock.pixelPosInGroup.shiftRight(shift[c]);
                IntPoint pixelPosInFrame = varblock.pixelPosInGroup.plus(frame.groupXY(groupID).shiftLeft(8))
                        .shiftRight(shift[c]);
                float coeff0, coeff1;
                float[] lfs = new float[2];
                IntPoint size = varblock.sizeInPixels();
                switch (tt.transformMethod) {
                    case TransformType.METHOD_DCT:
                        MathHelper.inverseDCT2D(coeffs[c], frameBuffer[c], ppg, pixelPosInFrame,
                                size, scratchBlock[0], scratchBlock[1], false);
                        break;
                    case TransformType.METHOD_DCT8_4:
                        coeff0 = coeffs[c][ppg.y][ppg.x];
                        coeff1 = coeffs[c][ppg.y + 1][ppg.x];
                        lfs[0] = coeff0 + coeff1;
                        lfs[1] = coeff0 - coeff1;
                        for (int x = 0; x < 2; x++) {
                            scratchBlock[0][0][0] = lfs[x];
                            for (int iy = 0; iy < 4; iy++) {
                                for (int ix = (iy == 0 ? 1 : 0); ix < 8; ix++) {
                                    scratchBlock[0][iy][ix] = coeffs[c][ppg.y + x + iy * 2][ppg.x + ix];
                                }
                            }
                            MathHelper.inverseDCT2D(scratchBlock[0], frameBuffer[c], IntPoint.ZERO,
                                    new IntPoint(4 * x, 0).plus(pixelPosInFrame), new IntPoint(8, 4),
                                    scratchBlock[1], scratchBlock[2], true);
                        }
                        break;
                    case TransformType.METHOD_DCT4_8:
                        coeff0 = coeffs[c][ppg.y][ppg.x];
                        coeff1 = coeffs[c][ppg.y + 1][ppg.x];
                        lfs[0] = coeff0 + coeff1;
                        lfs[1] = coeff0 - coeff1;
                        for (int y = 0; y < 2; y++) {
                            scratchBlock[0][0][0] = lfs[y];
                            for (int iy = 0; iy < 4; iy++) {
                                for (int ix = (iy == 0 ? 1 : 0); ix < 8; ix++) {
                                    scratchBlock[0][iy][ix] = coeffs[c][ppg.y + y + iy * 2][ppg.x + ix];
                                }
                            }
                            MathHelper.inverseDCT2D(scratchBlock[0], frameBuffer[c], IntPoint.ZERO, new IntPoint(0, 4 * y).plus(pixelPosInFrame), new IntPoint(8, 4), scratchBlock[1], scratchBlock[2], false);
                        }
                        break;
                    case TransformType.METHOD_AFV:
                        invertAFV(coeffs[c], frameBuffer[c], varblock, pixelPosInFrame, scratchBlock, shift[c]);
                        break;
                    case TransformType.METHOD_DCT2:
                        auxDCT2(coeffs[c], scratchBlock[0], ppg, IntPoint.ZERO, 2);
                        auxDCT2(scratchBlock[0], scratchBlock[1], IntPoint.ZERO, IntPoint.ZERO, 4);
                        auxDCT2(scratchBlock[1], frameBuffer[c], IntPoint.ZERO, pixelPosInFrame, 8);
                        break;
                    case TransformType.METHOD_HORNUSS:
                        auxDCT2(coeffs[c], scratchBlock[1], ppg, IntPoint.ZERO, 2);
                        for (int y = 0; y < 2; y++) {
                            for (int x = 0; x < 2; x++) {
                                float blockLF = scratchBlock[1][y][x];
                                float residual = 0f;
                                for (int iy = 0; iy < 4; iy++) {
                                    for (int ix = (iy == 0 ? 1 : 0); ix < 4; ix++) {
                                        residual += coeffs[c][ppg.y + y + iy * 2][ppg.x + x + ix * 2];
                                    }
                                }
                                scratchBlock[0][4 * y + 1][4 * x + 1] = blockLF - residual / 16f;
                                for (int iy = 0; iy < 4; iy++) {
                                    for (int ix = 0; ix < 4; ix++) {
                                        if (ix == 1 && iy == 1)
                                            continue;
                                        scratchBlock[0][y * 4 + iy][x * 4 + ix] =
                                                coeffs[c][ppg.y + y + iy * 2][ppg.x + x + ix * 2]
                                                        + scratchBlock[0][4 * y + 1][4 * x + 1];

                                    }
                                }
                                scratchBlock[0][4 * y][4 * x] = coeffs[c][ppg.y + y + 2][ppg.x + x + 2]
                                        + scratchBlock[0][4 * y + 1][4 * x + 1];
                            }
                        }
                        layBlock(scratchBlock[0], frameBuffer[c], IntPoint.ZERO, pixelPosInFrame, size);
                        break;
                    case TransformType.METHOD_DCT4:
                        auxDCT2(coeffs[c], scratchBlock[1], ppg, IntPoint.ZERO, 2);
                        for (int y = 0; y < 2; y++) {
                            for (int x = 0; x < 2; x++) {
                                scratchBlock[0][0][0] = scratchBlock[1][y][x];
                                for (int iy = 0; iy < 4; iy++) {
                                    for (int ix = (iy == 0 ? 1 : 0); ix < 4; ix++)
                                        // This does look backwards
                                        scratchBlock[0][iy][ix] = coeffs[c][ppg.y + y + ix * 2][ppg.x + x + iy * 2];
                                }
                                // we're already using scratchblock[1] for the auxDCT2 coordinates
                                // but we're putting these far away at (8, 8) so there's no overlap
                                MathHelper.inverseDCT2D(scratchBlock[0], scratchBlock[1], IntPoint.ZERO,
                                        new IntPoint(8), new IntPoint(4), scratchBlock[2], scratchBlock[3], false);
                                for (int iy = 0; iy < 4; iy++) {
                                    for (int ix = 0; ix < 4; ix++) {
                                        frameBuffer[c][pixelPosInFrame.y + 4 * y + iy][pixelPosInFrame.x + 4 * x + ix]
                                                = scratchBlock[1][8 + iy][8 + ix];
                                    }
                                }
                            }
                        }
                        break;
                    default:
                        throw new UnsupportedOperationException("Transform not implemented: " + tt);
                }
            }
        }
    }
}
