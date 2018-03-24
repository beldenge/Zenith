/**
 * Copyright 2017 George Belden
 *
 * This file is part of Zenith.
 *
 * Zenith is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * Zenith is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Zenith. If not, see <http://www.gnu.org/licenses/>.
 */

package com.ciphertool.zenith.neural.generate.zodiac408;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "englishParagraphs")
public class EnglishParagraph {
    @Id
    private ObjectId id;

    private Long sequence;

    private String					paragraph;

    // Required by Spring Data
    public EnglishParagraph() {}

    public EnglishParagraph(Long sequence, String paragraph) {
        this.sequence = sequence;
        this.paragraph = paragraph;
    }

    /**
     * @return the id
     */
    public ObjectId getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(ObjectId id) {
        this.id = id;
    }

    /**
     * @return the sequence
     */
    public Long getSequence() {
        return sequence;
    }

    /**
     * @param sequence the sequence to set
     */
    public void setSequence(Long sequence) {
        this.sequence = sequence;
    }

    /**
     * @return the paragraph
     */
    public String getParagraph() {
        return paragraph;
    }

    /**
     * @param paragraph
     *            the paragraph to set
     */
    public void setParagraph(String paragraph) {
        this.paragraph = paragraph;
    }
}
