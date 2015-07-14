package com.clarkparsia.empire.lazyload;

import com.clarkparsia.empire.SupportsRdfId;
import com.clarkparsia.empire.annotation.*;
import java.util.*;
import javax.persistence.*;

@Entity
@RdfsClass("ex:Child")
@Namespaces({"ex", "http://example.org/"})
public interface Child extends SupportsRdfId {

    @OneToMany(fetch=FetchType.LAZY)
    @RdfProperty("ex:isChildOf")
    public List<Parent> getIsChildOf();
    public void setIsChildOf(final List<Parent> parents);
}
