package com.clarkparsia.empire.test.api.nasa;

import com.clarkparsia.sesame.vocabulary.Vocabulary;

import org.openrdf.model.URI;

/**
 * Title: <br/>
 * Description: <br/>
 * Company: Clark & Parsia, LLC. <http://clarkparsia.com> <br/>
 * Created: Dec 31, 2009 11:01:14 AM <br/>
 *
 * @author Michael Grove <mike@clarkparsia.com>
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
