package com.ciphertool.zenith.inference.evaluator;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class LstmPredictionRequest {
    List<String> sequences;
}
