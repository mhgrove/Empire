package com.clarkparsia.empire.test.api;

import com.clarkparsia.sesame.vocabulary.Vocabulary;
import org.openrdf.model.URI;

/**
 * Title: <br/>
 * Description: <br/>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br/>
 * Created: Dec 29, 2009 5:28:34 PM <br/>
 *
 * @author Michael Grove <mike@clarkparsia.com>
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
