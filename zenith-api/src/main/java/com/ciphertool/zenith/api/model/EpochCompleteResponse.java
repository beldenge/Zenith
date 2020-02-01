package com.ciphertool.zenith.api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class EpochCompleteResponse {
    public int epochsCompleted;
    public int epochsTotal;
}
