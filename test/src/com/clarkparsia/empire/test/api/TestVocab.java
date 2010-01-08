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

package com.clarkparsia.empire.test.api;

import com.clarkparsia.sesame.vocabulary.Vocabulary;
import org.openrdf.model.URI;

/**
 * <p>Test Vocabulary constants</a>
 *
 * @author Michael Grove
 */
public class TestVocab extends Vocabulary {
	private static final TestVocab VOCAB = new TestVocab();

    private TestVocab() {
        super("http://clarkparsia.com/empire/test/");
    }

    public static TestVocab ontology() {
        return VOCAB;
    }

    public final URI weight = term("weight");
	public final URI likesVideoGames = term("likesVideoGames");
}
