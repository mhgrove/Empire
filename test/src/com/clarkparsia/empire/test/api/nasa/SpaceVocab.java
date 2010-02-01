/*
 * Copyright (c) 2009-2010 Clark & Parsia, LLC. <http://www.clarkparsia.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.clarkparsia.empire.test.api.nasa;

import com.clarkparsia.openrdf.vocabulary.Vocabulary;

import org.openrdf.model.URI;

/**
 * <p></p>
 *
 * @author Michael Grove
 */
public class SpaceVocab extends Vocabulary {
    private static SpaceVocab INSTANCE;

    private SpaceVocab() {
        super("http://purl.org/net/schemas/space/");
    }

    public static SpaceVocab ontology() {
        if (INSTANCE == null) {
            INSTANCE = new SpaceVocab();
        }

        return INSTANCE;
    }

    public final URI mass = term("mass");
    public final URI agency = term("agency");
    public final URI alternateName = term("alternateName");
}
