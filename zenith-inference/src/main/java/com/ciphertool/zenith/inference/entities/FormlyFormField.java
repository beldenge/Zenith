/*
 * Copyright 2017-2026 George Belden
 *
 * This file is part of Zenith.
 *
 * Zenith is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * Zenith is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Zenith. If not, see <http://www.gnu.org/licenses/>.
 */

package com.ciphertool.zenith.inference.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.JsonNode;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FormlyFormField {
    private String id;
    private String key;
    private String type;
    private FormlyFieldProps props;
    private String defaultValue;
    private JsonNode hooks;
    private JsonNode modelOptions;
    private JsonNode validation;
    private boolean resetOnHide;
    private JsonNode wrappers;
    private JsonNode expressions;
    private JsonNode expressionProperties;
    private boolean focus;
}
