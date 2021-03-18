package com.xy.netdev.frame.entity.device;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Builder
public class AntennaControlEntity {

    private Byte stx;

    private Byte lc;

    private Byte sad;

    private Byte cmd;

    private byte[] data;

    private Byte vs;

    private Byte etx;
}
