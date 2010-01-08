# Empire: RDF for JPA

Empire provides a standard [JPA](http://java.sun.com/developer/technicalArticles/J2EE/jpa/) style interface to
[RDF](http://www.w3.org/RDF/ databases using [SPARQL](http://www.w3.org/TR/rdf-sparql-query/) or
[SeRQL](http://www.openrdf.org/doc/sesame/users/ch06.html).

Our immediate need for Empire was to be able to build RDF-backed web apps, including [Pelorus](http://clarkparsia.com/pelorus), while being
compatible with JPA/Hibernate-style interfaces to RDBMS.

The primary design goal of Empire is to implement as much of the JPA API as possible so that it can drop into existing
applications, providing a simple ORM layer for RDF.

## Resources

The docs directory contains some information on Empire including some usage examples and a short roadmap of what we
have planned for future versions.

## Licensing

Empire is available under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0.html).