package com.clarkparsia.empire.lazyload;


import javax.persistence.Entity;

import com.clarkparsia.empire.annotation.Namespaces;
import com.clarkparsia.empire.annotation.RdfId;
import com.clarkparsia.empire.annotation.RdfsClass;


/**
 * Just a marker class to append prov:Entity RDF type.
 */
@Namespaces({
    "prov", "http://www.w3.org/ns/prov#" })
@Entity
@RdfsClass("prov:Entity")
public class ProvEntity extends BaseRdfEntity
{
    @RdfId
    private String uri;

    public ProvEntity() {
        // NOP
    }

    public ProvEntity(String uri) {
        this.setId(uri);
    }

    @Override
    protected void setId(String id) {
        this.uri = id;
    }
}
