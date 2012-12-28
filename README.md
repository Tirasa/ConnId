<img src="http://connid.googlecode.com/svn/wiki/images/architecture.png" align="right"/>

=!ConnId=

!ConnId (Connectors for Identity Management) is built to help drive development of Connectors.

Connectors provide a consistent generic layer between applications and target resources. The main focus of the API is provisioning operations and password management. The toolkit is intended to facilitate development with as few dependences as possible.

!ConnId's main purpose is to build a new home for the [http://java.net/projects/identityconnectors/ Identity Connectors] project, with all that is required nowadays for a modern Open Source project: Apache Maven driven build, artifacts and mailing lists.

All the code and documentation are released under terms of [http://www.sun.com/cddl/cddl.html CDDL 1.0].

*Find out how to get !ConnId [Downloads artifacts].*

One of the main features of the framework is decoupling Connectors from any application that uses them. This means each Connector implementation can be easily replaced and not necessarily depend on a specific version of the framework. In addition an application may choose to use multiple Connectors which can require class path isolation. With class path isolation there is no issue with conflicting 3rd party libraries. An application is only required to couple to the Framework and not to any specific Connector.

----
[http://syncope.apache.org/ http://syncope.apache.org/images/apache-syncope-logo-small.jpg]

!ConnId is one of the foundations of [http://syncope.apache.org/syncope/ Apache Syncope].
----
