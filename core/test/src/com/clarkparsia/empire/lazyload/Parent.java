package com.clarkparsia.empire.lazyload;

import com.clarkparsia.empire.SupportsRdfId;
import com.clarkparsia.empire.annotation.*;
import java.util.*;
import javax.persistence.*;

@Entity
@RdfsClass("ex:Parent")
@Namespaces({"ex", "http://example.org/"})
public interface Parent extends SupportsRdfId{

    @OneToMany(fetch=FetchType.LAZY)
    @RdfProperty("ex:isParentOf")
    public List<Child> getIsParentOf();
    public void setIsParentOf(final List<Child> children);
}
